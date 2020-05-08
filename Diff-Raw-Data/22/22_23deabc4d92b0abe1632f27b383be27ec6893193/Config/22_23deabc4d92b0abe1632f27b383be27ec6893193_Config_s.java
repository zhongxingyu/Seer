 /**
  *  Name: Config.java
  *  Date: 14:27:53 - 8 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *  Filedescription:
  *  
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Config {
 
 	@SuppressWarnings("serial")
 	public static void load(JavaPlugin plugin) {
 		
 		FileConfiguration config = plugin.getConfig();
 		boolean save = false;
 		
 		// -- Fix old entries
 		if(config.contains("worldnames")) {
 			for(String name : config.getStringList("worldnames")) {
 				config.set("worlds." + name + ".players_to_wait_for", 2);
 				config.set("worlds." + name + ".enable_healthregeneration", true);
 				config.set("worlds." + name + ".blockfilter", false);
 			}
 			
 			// Remove the now obsolete entry
 			config.set("worldnames", null);
 			
			save = true;
 		}
 		
		// Add .blockfilter to already configure worlds
		for(String key : config.getConfigurationSection("worlds").getKeys(false)) {
			if(!config.contains("worlds." + key + ".blockfilter")) {
				config.set("worlds." + key + ".blockfilter", false);
				save = true;
 			}
 		}
 		
 		// -- Default entries
 		
 		HashMap<String, Object> defaults = new HashMap<String, Object>() {{
 			
 			// General
 			put("debug", false);
 			put("lobbyworld", "world");
 			
 			// Update
 			put("auto-update", true);
 			
 			// Backup
 			put("backup.inventories", true);
 			
 			// Halloween
 			put("halloween.enabled", false);
 			put("halloween.forcepumpkin", false);
 			
 			// Time
 			put("timeoutTillStart", 120);
 			put("timeoutTillArenaInSeconds", 180);
 			put("timeoutAfterArena", 60);
 			
 			// Database
 			put("database.enabled", false);
 			put("database.auth.username", "username");
 			put("database.auth.password", "password");
 			put("database.settings.host", "localhost");
 			put("database.settings.port", 3306);
 			put("database.settings.database", "survivalgames");
 			put("database.settings.tablename", "sg_stats");
 		}};
 		
 		if(!config.contains("worlds")) {
 			config.set("worlds.survivalgames1.players_to_wait_for", 2);
 			config.set("worlds.survivalgames1.enable_healthregeneration", true);
 			config.set("worlds.survivalgames2.players_to_wait_for", 2);
 			config.set("worlds.survivalgames2.enable_healthregeneration", true);
 			save = true;
 		}
 		
 		if(!config.contains("allowedCommandsInGame")) {
 			ArrayList<String> allowedcommands = new ArrayList<String>() {{ 
 				add("/sgplayers");
 				add("/sgleave");
 			}};
 			config.set("allowedCommandsInGame", allowedcommands);
 			save = true;
 		}
 		
 		for(Entry<String, Object> entry : defaults.entrySet()) {
 			if(!config.contains(entry.getKey())) {
 				config.set(entry.getKey(), entry.getValue());
 				save = true;
 			}
 		}
 		
 		if(save) {
 			plugin.saveConfig();
 		}
 	}
 }
