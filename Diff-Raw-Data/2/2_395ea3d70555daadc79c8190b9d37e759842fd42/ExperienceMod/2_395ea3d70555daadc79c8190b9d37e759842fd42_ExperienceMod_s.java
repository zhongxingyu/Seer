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
 
 import java.awt.Color;
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.block.Block;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.Configuration.RewardTypes;
 import com.comphenix.xp.lookup.Parsing;
 
 public class ExperienceMod extends JavaPlugin implements Debugger {
 	
 	private final String permissionAdmin = "experiencemod.admin";
 	
 	// Mod command(s)
 	private final String commandReload = "experiencemod";
 	private final String commandSpawnExp = "spawnexp";
 	private final String subCommandToggleDebug = "debug";
 	private final String subCommandWarnings = "warnings";
 	
 	// Constants
 	private final int spawnExpMaxDistance = 50;
 	
 	private Logger currentLogger;
 	private PluginManager manager;
 	private Economy economy;
 	
 	private ExperienceListener listener;
 	private ExperienceInformer informer;
 	private Configuration configuration;
 	
 	private boolean debugEnabled;
 	
 	@Override
 	public void onEnable() {
 		manager = getServer().getPluginManager();
 		
 		currentLogger = this.getLogger();
 		informer = new ExperienceInformer();
 		
 		// Load economy, if it exists
 		if (!hasEconomy())
 			setupEconomy();
 		
 		// Initialize configuration
 		loadDefaults(false);
 		
 		// Register listeners
 		manager.registerEvents(listener, this);
 		manager.registerEvents(informer, this);
 	}
 	
 	private void loadDefaults(boolean reload) {
 		FileConfiguration config = getConfig();
 		File path = new File(getDataFolder(), "config.yml");
 		
 		// Reset warnings
 		informer.clearMessages();
 		
 		// See if we need to create the file
 		if (!path.exists()) {
 			// Supply default values if empty
 			config.options().copyDefaults(true);
 			saveConfig();
 			currentLogger.info("Creating default configuration file.");
 		}
 		
 		// Read from disk again
 		if (reload) {
 			reloadConfig();
 			
 			// Reload internal representation
 			configuration = null;
 			config = getConfig();
 		}
 		
 		// Load it
 		if (configuration == null) {
 			configuration = new Configuration(config, this);
 			setConfiguration(configuration);
 		}
 		
 		RewardTypes reward = configuration.getRewardType();
 		
 		// See if we actually can enable the economy
 		if (economy == null && reward == RewardTypes.ECONOMY) {
 			printWarning(this, "Cannot enable economy. VAULT plugin was not found.");
 			reward = RewardTypes.EXPERIENCE;
 		}
 		
 		// Set reward type
 		switch (reward) {
 		case EXPERIENCE:
 			listener.setRewardManager(new RewardExperience());
 			currentLogger.info("Using experience as reward.");
 			break;
 		case VIRTUAL:
 			listener.setRewardManager(new RewardVirtual());
 			currentLogger.info("Using virtual experience as reward.");
 			break;
 		case ECONOMY:
 			listener.setRewardManager(new RewardEconomy(economy, this));
 			currentLogger.info("Using the economy as reward.");
 			break;
 		default:
 			printWarning(this, "Unknown reward manager.");
 			break;
 		}
 	}
 	
 	private void setupEconomy()
     {
 		try {
 	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 	        
 	        if (economyProvider != null) {
 	            economy = economyProvider.getProvider();
 	        }
         
 		} catch (NoClassDefFoundError e) {
 			// No vault
 			return;
 		}
     }
 	
 	private boolean hasEconomy() {
 		return economy != null;
 	}
 	
 	private void setConfiguration(Configuration configuration) {
 		
 		// Create a new listener if necessary
 		if (listener == null) {
 			listener = new ExperienceListener(this, this, configuration);
 		} else {
 			listener.setConfiguration(configuration);
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		String cmdName = cmd != null ? cmd.getName() : "";
 		
 		// Execute the correct command
 		if (cmdName.equalsIgnoreCase(commandReload))
 			return handleMainCommand(sender, args);
 		else if (cmdName.equalsIgnoreCase(commandSpawnExp))
 			return handleSpawnExp(sender, args);
 		else
 			return false;
     }
 
 	private boolean handleMainCommand(CommandSender sender, String[] args) {
 		
 		// Make sure the sender has permissions
 		if (!hasCommandPermission(sender, permissionAdmin)) {
 			return true;
 		}
 		
 		if (args.length > 0) {
 			
 			// Toggle debugging
 			if (args[0].equalsIgnoreCase(subCommandToggleDebug)) {
 				debugEnabled = !debugEnabled;
 				respond(sender, ChatColor.DARK_BLUE + "Debug " + (debugEnabled ? " enabled " : " disabled"));
 				return true;
 			} else if (args[0].equalsIgnoreCase(subCommandWarnings)) {
 				if (sender != null)
 					informer.displayWarnings(sender);
 				return true;
 			} else {
 				respond(sender, ChatColor.RED + "Error: Unknown subcommand.");
 				return false; 
 			}
 
 		} else {
 			
 			loadDefaults(true);
     		respond(sender, ChatColor.DARK_BLUE + "Reloaded ExperienceMod.");
     		return true;
 		}
 	}
 	
 	private boolean handleSpawnExp(CommandSender sender, String[] args) {
 
 		// We don't support console yet
 		if (sender == null || !(sender instanceof Player)) {
 			respond(sender, ChatColor.RED + "This command can only be sent by a player");
 			return false;
 		}
 		
 		// Make sure the sender has permissions
 		if (!hasCommandPermission(sender, permissionAdmin)) {
 			return true;
 		}
 		
 		if (args.length == 1 && !Parsing.isNullOrIgnoreable(args[0])) {
 			
 			Integer experience = Integer.parseInt(args[0]);
 			Player player = (Player) sender;
 			
 			if (experience == null) {
 				respond(sender, ChatColor.RED + "Error: Parameter must be a valid integer.");
 				return false;
 			}
 			
 			Block startBlock = player.getEyeLocation().getBlock();
 			List<Block> list = player.getLastTwoTargetBlocks(null, spawnExpMaxDistance);
 			
 			// Remember the start location
 			list.add(0, startBlock);
 			
 			// We want to spawn the experience at the surface of the block.
 			list.remove(list.size() - 1);
 			
 			if (list.size() > 0) {
 				Block target = list.get(list.size() - 1);
 				Location loc = target.getLocation();
 				
 				// Spawn experience at this location
 				printDebug(this, "Spawning %d experience at %b.", experience, loc);
 				Server.spawnExperienceAtBlock(target, experience);
 				return true;
 			}
 				
 
 		} else {
 			respond(sender, ChatColor.RED + "Error: Incorrect number of parameters.");
 		}
 		
 		return false;
 	}
 	
 	private boolean hasCommandPermission(CommandSender sender, String permission) {
 		
 		// Make sure the sender has permissions
 		if (sender != null && !sender.hasPermission(permission)) {
 			respond(sender, ChatColor.RED + "You haven't got permission to execute this command.");
 			return false;
 		} else {
 			// We have permission
 			return true;
 		}
 	}
 
 	@Override	
 	public boolean isDebugEnabled() {
 		return debugEnabled;
 	}
 	
 	@Override
 	public void printDebug(Object sender, String message, Object... params) {
 		if (debugEnabled)
 			currentLogger.info(String.format("Debug: " + message, params));
 	}
 
 	private void respond(CommandSender sender, String message) {
 		if (sender == null) // Sent by the console
 			currentLogger.info(message);
 		else
 			sender.sendMessage(message);
 	}
 
 	@Override
 	public void printWarning(Object sender, String message, Object... params) {
 		String warningMessage = Color.RED + "Warning: " + message;
 		
 		// Print immediately
 		currentLogger.warning(String.format(warningMessage, params));
 		
 		// Add to list of warnings
	    informer.addWarningMessage(message);
 	}
 }
