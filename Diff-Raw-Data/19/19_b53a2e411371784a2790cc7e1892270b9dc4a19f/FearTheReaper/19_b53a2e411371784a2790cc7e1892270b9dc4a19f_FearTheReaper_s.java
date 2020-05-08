 /* This file is part of FearTheReaper.
 
     FearTheReaper is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     FearTheReaper is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with FearTheReaper.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.herocraftonline.fearthereaper;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.dynmap.DynmapAPI;
 
 import com.herocraftonline.fearthereaper.commands.ReaperCommands;
 import com.herocraftonline.fearthereaper.spawnpoint.Spawn;
 import com.herocraftonline.fearthereaper.spawnpoint.SpawnPoint;
 
 public class FearTheReaper extends JavaPlugin {
 
     public static Logger log = Logger.getLogger("Minecraft");
     public static File pointsDirectory;
     public static FileConfiguration config;
     public static HashMap<String, Spawn> SpawnPointList = new HashMap<String, Spawn>();    
     public static ReaperMarkers markers = null;
     
     public static HashMap<String, Spawn> getSpawnList() {
         return SpawnPointList;
     }
 
     public void onDisable() {
         saveConfig();
         log.info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " unloaded successfully!");
     }
 
     public void onEnable() {
         pointsDirectory = new File(getDataFolder(), "points");
         config = getConfig();
         config.options().copyDefaults(true);
         saveConfig();
         SpawnPoint.loadAllPoints();
 
         getServer().getPluginManager().registerEvents(new ReaperPlayerListener(), this);
 
         log.info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " loaded successfully!");
 
         getCommand("graveyard").setExecutor(new ReaperCommands(this));
 
         Plugin dm = getServer().getPluginManager().getPlugin("dynmap");
         if (dm != null) {
            markers = new ReaperMarkers(this, (DynmapAPI) dm);
         }
     }
 
     public static void reloadConfig(FearTheReaper plugin) {
         System.out.println("[FearTheReaper] - Reloading config.");
         plugin.reloadConfig();
         FearTheReaper.config = plugin.getConfig();
     }
 
     public static boolean isBedsEnabled() {
         return FearTheReaper.config.getBoolean("enable-beds", false);
     }
 }
