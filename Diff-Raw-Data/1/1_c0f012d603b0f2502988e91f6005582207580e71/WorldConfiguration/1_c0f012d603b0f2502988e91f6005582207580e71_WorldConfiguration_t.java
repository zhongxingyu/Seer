 /* 
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  * Copyright (C) 2011 adamonline45 <adamonline45@gmail.com>
  * 
  * This file is part of Lupi.
  * 
  * Lupi is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Lupi is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Lupi.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.halvors.lupi.util;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import org.bukkit.util.config.Configuration;
 import org.halvors.lupi.Lupi;
 
 
 /**
  * Holds the configuration for individual worlds.
  * 
  * @author halvors
  */
 public class WorldConfiguration {
 //    private Lupi plugin;
     
     private final ConfigurationManager configManager;
     
     private String worldName;
     private Configuration config;
     private File configFile;
     
     /* Configuration data start */
     public boolean wolfEnable;
     public int wolfItem;
     public boolean wolfFriendly;
     public boolean wolfPvp;
     public int wolfLimit;
     public boolean wolfKeepChunksLoaded;
     
     public int infoItem;
     
     public boolean inventoryEnable;
     public int inventoryItem;
     /* Configuration data end */
     
     public WorldConfiguration(Lupi plugin, String worldName) {
 //        this.plugin = plugin;
         this.configManager = plugin.getConfigurationManager();
         this.worldName = worldName;
 
         File baseFolder = new File(plugin.getDataFolder(), "worlds/");
         configFile = new File(baseFolder, worldName + ".yml");
         
         configManager.createDefaultConfiguration(configFile, "config_world.yml");
         config = new Configuration(configFile);
         
         load();
 
         plugin.log(Level.INFO, "Loaded configuration for world '" + worldName + '"');
     }
     
     /**
      * Load the configuration.
      */
     private void load() {
         config.load();
 
         wolfEnable = config.getBoolean("wolf.enable", wolfEnable);
         wolfItem = config.getInt("wolf.item", wolfItem);
         wolfFriendly = config.getBoolean("wolf.friendly", wolfFriendly);
         wolfPvp = config.getBoolean("wolf.pvp", wolfPvp);
         wolfLimit = config.getInt("wolf.limit", wolfLimit);
        wolfKeepChunksLoaded = config.getBoolean("wolf.keepchunksloaded", wolfKeepChunksLoaded);
         
         infoItem = config.getInt("info.item", infoItem);
         
         inventoryEnable = config.getBoolean("inventory.enable", inventoryEnable);
         inventoryItem = config.getInt("inventory.item", inventoryItem);
         
         config.save();
     }
     
     public String getWorldName() {
         return this.worldName;
     }
 }
