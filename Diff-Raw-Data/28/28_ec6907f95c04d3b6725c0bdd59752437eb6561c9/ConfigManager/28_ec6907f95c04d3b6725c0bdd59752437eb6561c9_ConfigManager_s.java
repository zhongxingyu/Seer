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
 
         if (!configFile.exists()) {
            InputStream defConfigStream = plugin.getResource("config.yml");
             try {
                 config.load(defConfigStream);
                 config.save(configFile);
             } catch (Exception e) {
                 System.out.println("ExpansiveTerrain: Unable to create config file for world " + worldName);
                 return;
             }
         }
         configs.put(worldName, config);
     }
 
     public FileConfiguration getConfig(String worldName) {
         return configs.get(worldName);
     }
 }
