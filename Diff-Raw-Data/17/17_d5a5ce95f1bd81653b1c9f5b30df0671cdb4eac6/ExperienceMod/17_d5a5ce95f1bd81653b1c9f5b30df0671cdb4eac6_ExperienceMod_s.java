 package com.comphenix.xp;
 
 /**
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.lookup.Parsing;
 
 public class ExperienceMod extends JavaPlugin {
 	// Mod command(s)
 	private final String commandReload = "experiencemod";
 	private final String toggleDebug = "DEBUG";
 	
 	private Logger currentLogger;
 	private ExperienceListener listener;
 	private Configuration configuration;
 	
 	private boolean debugEnabled;
 	
 	@Override
 	public void onEnable() {
 		PluginManager manager = getServer().getPluginManager();
 		
 		currentLogger = this.getLogger();
 		
		// Intialize configuration and listeners
 		loadDefaults();
 		listener = new ExperienceListener(this, configuration);
 		
 		// Begin changing stuff
 		manager.registerEvents(listener, this);
 	}
 	
 	private void loadDefaults() {
 		FileConfiguration config = getConfig();
 
		// Supply default values if empty
		if (!config.contains("multiplier")) {
 			config.options().copyDefaults(true);
 			saveConfig();
 		}
 		
 		// Load it
 		configuration = new Configuration(config, currentLogger);
 	}
 	
 	@Override
 	public void onDisable() {
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
     	if(cmd.getName().equalsIgnoreCase(commandReload)) {
     		
     		// Undocumented.
     		if (args.length > 0) {
     			
    			// Toggle debuging
     			if (Parsing.getEnumName(args[0]).equals(toggleDebug)) {
     				debugEnabled = !debugEnabled;
     				respond(sender, "Debug " + (debugEnabled ? " enabled " : " disabled"));
     				return true;
     			} else {
     				respond(sender, "Error: Unknown subcommand.");
     			}
     
     		} else {
     			
     			loadDefaults();
 	    		listener.setConfiguration(configuration);
 	    		respond(sender, "Reloaded ExperienceMod.");
 	    		return true;
     		}
     	}
   
     	return false; 
     }
 	
 	public boolean isDebugEnabled() {
 		return debugEnabled;
 	}
 	
 	public void printDebug(String message) {
 		if (debugEnabled)
 			currentLogger.info("DEBUG: " + message);
 	}
 	
 	private void respond(CommandSender sender, String message) {
 		if (sender == null) // Sent by the console
 			currentLogger.info(message);
 		else
 			sender.sendMessage(message);
 	}
 }
