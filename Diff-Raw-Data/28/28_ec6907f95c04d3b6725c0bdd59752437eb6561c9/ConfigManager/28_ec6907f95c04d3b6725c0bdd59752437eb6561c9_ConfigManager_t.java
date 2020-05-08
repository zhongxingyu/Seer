 package com.xpansive.bukkit.expansiveterrain;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class ConfigManager {
 
     private final String CONFIG_NAME = "expansiveconfig.yml";
     private HashMap<String, FileConfiguration> configs = new HashMap<String, FileConfiguration>();
 
     public void loadConfig(ExpansiveTerrain plugin, String worldName) {
         File configFile = new File(worldName, CONFIG_NAME);
         FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        InputStream defConfigStream = plugin.getResource("config.yml");
 
         if (!configFile.exists()) {
             try {
                 config.load(defConfigStream);
                 config.save(configFile);
             } catch (Exception e) {
                 System.out.println("ExpansiveTerrain: Unable to create config file for world " + worldName);
                System.out.println("This is bad! Tell a dev!");
                return;
            }
        } else {
            FileConfiguration defaults = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defaults);
            try {
                config.load(configFile);
            } catch (Exception e) {
                System.out.println("ExpansiveTerrain: Unable to load config file for world " + worldName);
                System.out.println("Is it malformed?");
                 return;
             }
         }

         configs.put(worldName, config);
     }
 
     public FileConfiguration getConfig(String worldName) {
         return configs.get(worldName);
     }
 }
