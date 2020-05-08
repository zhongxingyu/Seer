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
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.MissingResourceException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.commands.CommandExperienceMod;
 import com.comphenix.xp.commands.CommandSpawnExp;
 import com.comphenix.xp.listeners.ExperienceBlockListener;
 import com.comphenix.xp.listeners.ExperienceEnhancements;
 import com.comphenix.xp.listeners.ExperienceItemListener;
 import com.comphenix.xp.listeners.ExperienceMobListener;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.messages.HeroService;
 import com.comphenix.xp.messages.MessageFormatter;
 import com.comphenix.xp.messages.StandardService;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.parser.Utility;
 import com.comphenix.xp.rewards.ItemRewardListener;
 import com.comphenix.xp.rewards.RewardEconomy;
 import com.comphenix.xp.rewards.RewardExperience;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.comphenix.xp.rewards.RewardVirtual;
 import com.comphenix.xp.rewards.RewardTypes;
 
 public class ExperienceMod extends JavaPlugin implements Debugger {
 	
 	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
 
 	private final String permissionInfo = "experiencemod.info";
 	private final String commandReload = "experiencemod";
 	private final String commandSpawnExp = "spawnexp";
 	
 	private Logger currentLogger;
 	private PluginManager manager;
 	
 	private Economy economy;
 	private Chat chat;
 	
 	private ExperienceBlockListener xpBlockListener;
 	private ExperienceItemListener xpItemListener;
 	private ExperienceMobListener xpMobListener;
 	private ExperienceEnhancements xpEnchancer;
 	
 	private ExperienceInformer informer;
 	private ItemRewardListener itemListener;
 	
 	private RewardProvider rewardProvider;
 	private ChannelProvider channelProvider;
 	
 	private Presets presets;
 	
 	// Repeating task
	private static final int tickDelay = 10; // 50 ms * 10 = 500 ms
 	private int serverTickTask;
 	
 	// Commands
 	private CommandExperienceMod commandExperienceMod;
 	private CommandSpawnExp commandSpawn;
 	
 	private boolean debugEnabled;
 	
 	@Override
 	public void onEnable() {
 		RewardEconomy rewardEconomy;
 		
 		manager = getServer().getPluginManager();
 		
 		currentLogger = this.getLogger();
 		informer = new ExperienceInformer();
 		
 		commandExperienceMod = new CommandExperienceMod(this);
 		commandSpawn = new CommandSpawnExp(this);
 		rewardProvider = new RewardProvider();
 		channelProvider = new ChannelProvider();
 		channelProvider.setMessageFormatter(new MessageFormatter());
 		
 		// Load economy, if it exists
 		try {
 			if (!hasEconomy())
 				economy = getRegistration(Economy.class);
 			if (!hasChat())
 				chat = getRegistration(Chat.class);
 		
 		} catch (NoClassDefFoundError e) {
 			// No vault
 		} catch (NullPointerException e) {
 		}
 		
 		// Load reward types
 		rewardProvider.register(new RewardExperience());
 		rewardProvider.register(new RewardVirtual());
 		rewardProvider.setDefaultReward(RewardTypes.EXPERIENCE);
 		
 		// Load channel providers if we can
 		if (HeroService.exists()) {
 			channelProvider.register(new HeroService());
 			channelProvider.setDefaultName(HeroService.NAME);
 			currentLogger.info("Using HeroChat for channels.");
 			
 		} else {
 			channelProvider.register(new StandardService( getServer() ));
 			channelProvider.setDefaultName(StandardService.NAME);
 			currentLogger.info("Using standard chat.");
 		}
 		
 		// Don't register economy rewards unless we can
 		if (hasEconomy()) {
 			itemListener = new ItemRewardListener(this);
 			rewardEconomy = new RewardEconomy(economy, this, itemListener); 
 			
 			// Associate everything
 			rewardProvider.register(rewardEconomy);
 			itemListener.setReward(rewardEconomy);
 			
 			// Register listener
 			manager.registerEvents(itemListener, this);
 			
 			// Inform the player
 			currentLogger.info("Economy enabled.");
 		}
 		
 		try {
 			// Initialize configuration
 			loadDefaults(false);
 			
 			// Register listeners
 			manager.registerEvents(xpBlockListener, this);
 			manager.registerEvents(xpItemListener, this);
 			manager.registerEvents(xpMobListener, this);
 			manager.registerEvents(xpEnchancer, this);
 			manager.registerEvents(informer, this);
 		
 		} catch (IOException e) {
 			currentLogger.severe("IO error when loading configurations: " + e.getMessage());
 		}
 		
 		// Register commands
 		getCommand(commandReload).setExecutor(commandExperienceMod);
 		getCommand(commandSpawnExp).setExecutor(commandSpawn);
 		
 		// Begin server tick
 		serverTickTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				onServerTick();
 			}
 		}, tickDelay, tickDelay); 
 		
 		// Inform of this problem
 		if (serverTickTask < 0)
 			printWarning(this, "Could not start repeating task for sending messages.");
 	}
 	
 	@Override
 	public void onDisable() {
 
 		// Cancel server tick
 		if (serverTickTask >= 0)
 			getServer().getScheduler().cancelTask(serverTickTask);
 	}
 	
 	public YamlConfiguration loadConfig(String name, String createMessage) throws IOException {
 		
 		File savedFile = new File(getDataFolder(), name);
 		File directory = savedFile.getParentFile();
 		
 		// Reload the saved configuration
 		if (!savedFile.exists()) {
 
 			// Get the default file
 			InputStream input = ExperienceMod.class.getResourceAsStream("/" + name);
 			
 			// Make sure the directory exists 
 			if (!directory.exists()) {
 				directory.mkdirs();
 				
 				if (!directory.exists())
 					throw new IOException("Could not create the directory " + directory.getAbsolutePath());
 			}
 			
 			OutputStream output = new FileOutputStream(savedFile);
 			
 			// Check just in case
 			if (input == null) {
 				throw new MissingResourceException(
 						"Cannot find built in resource file.", "ExperienceMod", name);
 			}
 
 			copyLarge(input, output);
 		}
 		
 		// Retrieve the saved file
 		return YamlConfiguration.loadConfiguration(savedFile);
 	}
 	
 	/**
 	 * Reloads (if reload is TRUE) configurations. There's no need to call this after adding reward providers.
 	 * @param reload - if TRUE; reload configuration.
 	 * @throws IOException An I/O error occurred.
 	 */
 	public void loadDefaults(boolean reload) throws IOException {
 		
 		ConfigurationLoader loader;
 		
 		// Read from disk again
 		if (reload || presets == null) {
 			
 			// Reset warnings
 			informer.clearMessages();
 			
 			// Load parts of the configuration
 			YamlConfiguration presetList = loadConfig("presets.yml", "Creating default preset list.");
 			loadConfig("config.yml", "Creating default configuration.");
 			
 			// Load it
 			loader = new ConfigurationLoader(getDataFolder(), this, rewardProvider, channelProvider);
 			presets = new Presets(presetList, this, chat, loader);
 			setPresets(presets);
 			
 			// Vault is required here
 			if (chat == null && presets.usesPresetParameters()) {
 				printWarning(this, "Cannot use presets. VAULT plugin was not found");
 				
 			} else {
 				
 				// Show potentially more warnings
 				checkIllegalPresets();
 			}
 			
 		}
 	}
 	
 	/**
 	 * Invoked every server tick.
 	 */
 	public void onServerTick() {
 	
 		// Send messages
 		if (presets != null)
 			presets.onTick();
 	}
 	
 	// Check for illegal presets
 	private void checkIllegalPresets() {
 
 		// With no Vault this is impossible
 		if (chat == null)
 			return;
 		
 		for (String group : chat.getGroups()) {
 			for (World world : getServer().getWorlds()) {
 				
 				String worldName = world.getName();
 				String possibleOption = chat.getGroupInfoString(
 						worldName, group, Presets.optionPreset, null);
 
 				try {
 					if (!Utility.isNullOrIgnoreable(possibleOption) && 
 						!presets.containsPreset(possibleOption, worldName)) {
 						
 						// Complain about this too. Is likely an error.
 						printWarning(this, 
 								"Could not find preset %s. Please check spelling.", possibleOption);
 					}
 					
 				} catch (ParsingException e) {
 					printWarning(this, "Preset '%s' causes error: %s", possibleOption, e.getMessage());
 				}
 			}
 		}
 	}
 
 	private <TClass> TClass getRegistration(Class<TClass> type)
     {
         RegisteredServiceProvider<TClass> registry = getServer().getServicesManager().getRegistration(type);
         
         if (registry != null) 
             return registry.getProvider();
         else
         	return null;
     }
 	
 	private boolean hasEconomy() {
 		return economy != null;
 	}
 	
 	private boolean hasChat() {
 		return chat != null;
 	}
 	
 	@Override	
 	public boolean isDebugEnabled() {
 		return debugEnabled;
 	}
 	
 	/**
 	 * Toggles debug messages.
 	 */
 	public void toggleDebug() {
 		debugEnabled = !debugEnabled;
 	}
 
 	public Chat getChat() {
 		return chat;
 	}
 	
 	public Economy getEconomy() {
 		return economy;
 	}
 	
 	public ExperienceInformer getInformer() {
 		return informer;
 	}
 	
 	public RewardProvider getRewardProvider() {
 		return rewardProvider;
 	}
 
 	public ChannelProvider getChannelProvider() {
 		return channelProvider;
 	}
 	
 	public ItemRewardListener getItemListener() {
 		return itemListener;
 	}
 
 	public Presets getPresets() {
 		return presets;
 	}
 	
 	private void setPresets(Presets presets) {
 		
 		// Create a new listener if necessary
 		if (xpBlockListener == null || xpItemListener == null || xpMobListener == null) {
 			xpItemListener = new ExperienceItemListener(this, this, presets);
 			xpBlockListener = new ExperienceBlockListener(this, presets);
 			xpMobListener = new ExperienceMobListener(this, presets);
 			xpEnchancer = new ExperienceEnhancements(this);
 		} else {
 			xpItemListener.setPresets(presets);
 			xpBlockListener.setPresets(presets);
 			xpMobListener.setPresets(presets);
 		}
 	}
 	
 	@Override
 	public void printDebug(Object sender, String message, Object... params) {
 		if (debugEnabled) {
 			
 			String formattedMessage = String.format("[ExperienceMod] " + message, params);
 			
 			// Every player with the info permission will also see this message
 			getServer().broadcast(formattedMessage, permissionInfo);
 		}
 	}
 
 	public void respond(CommandSender sender, String message) {
 		if (sender == null) // Sent by the console
 			currentLogger.info(message);
 		else
 			sender.sendMessage(message);
 	}
 
 	@Override
 	public void printWarning(Object sender, String message, Object... params) {
 		String warningMessage = ChatColor.RED + "Warning: " + message;
 		
 		// Print immediately
 		currentLogger.warning(String.format(warningMessage, params));
 		
 		// Add to list of warnings
 	    informer.addWarningMessage(String.format(message, params));
 	}
 	
 	
 	// Taken from Apache Commons-IO
 	private static long copyLarge(InputStream input, OutputStream output) throws IOException {
 		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
 		long count = 0;
 		int n = 0;
 		
 		while (-1 != (n = input.read(buffer))) {
 			output.write(buffer, 0, n);
 			count += n;
 		}
 		return count;
 	}
 }
