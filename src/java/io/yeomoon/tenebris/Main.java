package io.yeomoon.tenebris;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Main extends JavaPlugin {

    private int checkPeriod;
    private int ignoreRadiusSquared;
    private boolean checkPlayers;
    private final Set<EntityType> excludedTypes = new HashSet<>();
    private final Set<String> excludedWorlds = new HashSet<>();
    private boolean pluginEnabled = true;
    
    @Override
    public void onEnable() {
        getLogger().info("Loading configs");
        saveDefaultConfig();
        loadConfig();

        getLogger().info("Plugin enabled");
        getLogger().info("Made by Yeomoon");
        
        if (!pluginEnabled) {
            getLogger().info("Due to config, plugin is disabled");
            getLogger().info("You can enable plugin in 'config.yml'");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    World world = player.getWorld();
                    if (excludedWorlds.contains(world.getName())) continue;

                    for (Entity entity : world.getEntities()) {
                        if (entity.equals(player)) continue;
                        if (!checkPlayers && entity.getType() == EntityType.PLAYER) continue;
                        if (excludedTypes.contains(entity.getType())) continue;
                        if (entity.getLocation().distanceSquared(player.getLocation()) > ignoreRadiusSquared) continue;

                        if (player.hasLineOfSight(entity)) {
                            player.showEntity(Main.this, entity);
                        } else {
                            player.hideEntity(Main.this, entity);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, checkPeriod);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        this.pluginEnabled = config.getBoolean("enabled", true);
        this.checkPeriod = config.getInt("check-period", 10);
        int ignoreRadius = config.getInt("ignore-radius", 128);
        this.ignoreRadiusSquared = ignoreRadius * ignoreRadius;
        this.checkPlayers = config.getBoolean("check-players", true);

        List<String> list = config.getStringList("excluded-entities");
        excludedTypes.clear();
        for (String typeName : list) {
            try {
                EntityType type = EntityType.valueOf(typeName.toUpperCase());
                excludedTypes.add(type);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Config | unknown entity type : " + typeName);
            }
        }

        this.excludedWorlds.clear();
        for (String worldName : config.getStringList("excluded-worlds")) {
            if (Bukkit.getWorld(worldName) != null) {
                this.excludedWorlds.add(worldName);
            } else {
                getLogger().warning("Config | unknown world : " + worldName);
            }
        }

        getLogger().info("Config loaded");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled");
    }
}
