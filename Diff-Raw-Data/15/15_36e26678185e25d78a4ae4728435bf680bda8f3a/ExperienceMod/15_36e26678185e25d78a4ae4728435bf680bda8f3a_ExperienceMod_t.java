 /*
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
 
 package com.comphenix.xp;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.commands.CommandExperienceMod;
 import com.comphenix.xp.commands.CommandSpawnExp;
 import com.comphenix.xp.extra.Permissions;
 import com.comphenix.xp.extra.Service;
 import com.comphenix.xp.extra.ServiceProvider;
 import com.comphenix.xp.history.HawkeyeService;
 import com.comphenix.xp.history.HistoryProviders;
 import com.comphenix.xp.history.LogBlockService;
 import com.comphenix.xp.history.MemoryService;
 import com.comphenix.xp.listeners.*;
 import com.comphenix.xp.lookup.*;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.messages.HeroService;
 import com.comphenix.xp.messages.MessageFormatter;
 import com.comphenix.xp.messages.StandardService;
 import com.comphenix.xp.mods.BlockResponse;
 import com.comphenix.xp.mods.CustomBlockProviders;
 import com.comphenix.xp.mods.StandardBlockService;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.parser.Utility;
 import com.comphenix.xp.rewards.*;
 
 public class ExperienceMod extends JavaPlugin implements Debugger {
 	
 	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
 	
 	private Logger currentLogger;
 	private PluginManager manager;
 	
 	// Scheduling
 	private PlayerScheduler playerScheduler;
 
 	private Economy economy;
 	private Chat chat;
 	
 	private ExperienceBlockListener xpBlockListener;
 	private ExperienceItemListener xpItemListener;
 	private ExperienceMobListener xpMobListener;
 	private ExperienceEnhancementsListener xpEnchancer;
 	private ExperienceCleanupListener xpCleanup;
 	
 	private ExperienceInformerListener informer;
 	private ItemRewardListener itemListener;
 	private PlayerInteractionListener interactionListener;
 
 	// Allows for plugin injection
 	private RewardProvider rewardProvider;
 	private ChannelProvider channelProvider;
 	private CustomBlockProviders customProvider;
 	private HistoryProviders historyProviders;
 	
 	private GlobalSettings globalSettings;
 	private ConfigurationLoader configLoader;
 	private Presets presets;
 	
 	// Repeating task
 	private static final int TICK_DELAY = 4; // 50 ms * 4 = 200 ms
 	private int serverTickTask;
 	
 	// Commands
 	private CommandExperienceMod commandExperienceMod;
 	private CommandSpawnExp commandSpawn;
 	
 	private boolean debugEnabled;
 	
 	@Override
 	public void onLoad() {
 
 		RewardEconomy rewardEconomy;
 		
 		// Initialize scheduler
 		playerScheduler = new PlayerScheduler(Bukkit.getScheduler(), this);
 		manager = getServer().getPluginManager();
 		
 		// Initialize rewards
 		currentLogger = this.getLogger();
 		rewardProvider = new RewardProvider();
 		
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
 		
 		// Load history
 		historyProviders = new HistoryProviders();
 		
 		// Load reward types
 		rewardProvider.register(new RewardExperience());
 		rewardProvider.register(new RewardVirtual());
 		rewardProvider.setDefaultReward(RewardTypes.EXPERIENCE);
 		
 		// Initialize channel providers
 		channelProvider = new ChannelProvider();
 		channelProvider.setMessageFormatter(new MessageFormatter());
 		
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
 			
 			// Inform the player
 			currentLogger.info("Economy enabled.");
 		}
 		
 		// Initialize block providers
 		customProvider = new CustomBlockProviders();
 		customProvider.register(new StandardBlockService());
 		
 		// Initialize configuration loader
 		configLoader = new ConfigurationLoader(getDataFolder(), this, rewardProvider, channelProvider);
 	}
 
 	@Override
 	public void onEnable() {
 		
 		informer = new ExperienceInformerListener();
 		interactionListener = new PlayerInteractionListener();
 		
 		// Commands
 		commandExperienceMod = new CommandExperienceMod(this);
 		commandSpawn = new CommandSpawnExp(this);
 		
 		// Block provider
 		customProvider.setLastInteraction(interactionListener);
 		
 		if (hasEconomy()) {
 			// Register listener
 			manager.registerEvents(itemListener, this);
 		}
 		
 		try {
 			// Initialize configuration
 			loadDefaults(false);
 			
 			// Register listeners
 			manager.registerEvents(interactionListener, this);
 			manager.registerEvents(xpBlockListener, this);
 			manager.registerEvents(xpItemListener, this);
 			manager.registerEvents(xpMobListener, this);
 			manager.registerEvents(xpEnchancer, this);
 			manager.registerEvents(xpCleanup, this);
 			manager.registerEvents(informer, this);
 		
 		} catch (IOException e) {
 			currentLogger.severe("IO error when loading configurations: " + e.getMessage());
 		}
 		
 		// Create memory history
 		historyProviders.register(new MemoryService(
 				globalSettings.getMaxBlocksInHistory(), 
 				globalSettings.getMaxAgeInHistory()
 		));
 	
 		registerHistoryServices();
 		
 		// Register commands
 		getCommand(CommandExperienceMod.COMMAND_RELOAD).setExecutor(commandExperienceMod);
 		getCommand(CommandSpawnExp.COMMAND_SPAWN_XP).setExecutor(commandSpawn);
 		
 		// Begin server tick
 		serverTickTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				onServerTick();
 			}
 		}, TICK_DELAY, TICK_DELAY); 
 		
 		// Inform of this problem
 		if (serverTickTask < 0)
 			printWarning(this, "Could not start repeating task for sending messages.");
 	}
 	
 	private void registerHistoryServices() {
 		
 		// Register log block if exists
 		if (LogBlockService.exists(manager)) {
 			if (!historyProviders.containsService(LogBlockService.NAME)) {
 				historyProviders.register(LogBlockService.create(manager));
 			}
 			
 			currentLogger.info("Connected to LogBlock.");
 		} else {
 			currentLogger.info("Cannot connect to LogBlock.");
 		}
 		
 		// Register Hawkeye if it exists
 		if (HawkeyeService.exists(manager)) {
 			if (!historyProviders.containsService(HawkeyeService.NAME)) {
 				historyProviders.register(new HawkeyeService(this));
 			}
 			
 			currentLogger.info("Connected to Hawkeye.");
 		} else {
 			currentLogger.info("Cannot connect to Hawkeye.");
 		}
 	}
 	
 	/**
 	 * Disable every service in the given list of services.
 	 * @param provider - registry of services.
 	 * @param serviceNames - services to disable.
 	 */
 	private <TService extends Service> void disableServices(ServiceProvider<TService> provider, List<String> serviceNames) {
 		
 		provider.enableAll();
 		
 		// Disable all such services
 		for (String name : serviceNames) {
 			if (provider.containsService(name)) {
 				provider.setEnabled(name, false);
 			}
 		}
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
 
 		// Read from disk again
 		if (reload || presets == null) {
 			
 			// Reset warnings
 			informer.clearMessages();
 			
 			// Remove previously loaded files
 			configLoader.clearCache();
 			
 			// Load globals
 			globalSettings = new GlobalSettings(this);
 			globalSettings.loadFromConfig(loadConfig("global.yml", "Creating default global settings."));
 			Permissions.setGlobalSettings(globalSettings);
 			
 			// Disable stuff
 			disableServices(historyProviders, globalSettings.getDisabledServices());
 			disableServices(channelProvider, globalSettings.getDisabledServices());
 			disableServices(rewardProvider, globalSettings.getDisabledServices());
 			disableServices(customProvider, globalSettings.getDisabledServices());
 			
 			// Load parts of the configuration
 			YamlConfiguration presetList = loadConfig("presets.yml", "Creating default preset list.");
 			loadConfig("config.yml", "Creating default configuration.");
 			
 			// Load it
 			presets = new Presets(presetList, this, chat, configLoader);
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
 						worldName, group, Presets.OPTION_PRESET_SETTING, null);
 
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
 	
 	public ExperienceInformerListener getInformer() {
 		return informer;
 	}
 	
 	public RewardProvider getRewardProvider() {
 		return rewardProvider;
 	}
 
 	public ChannelProvider getChannelProvider() {
 		return channelProvider;
 	}
 	
 	public CustomBlockProviders getCustomBlockProvider() {
 		return customProvider;
 	}
 	
 	/**
 	 * Retrieves the object responsible for parsing and loading configuration files.
 	 * @return The current configuration loader.
 	 */
 	public ConfigurationLoader getConfigLoader() {
 		return configLoader;
 	}
 
 	/**
 	 * Sets the object responsible for parsing and loading configuration files.
 	 * @param configLoader - the new configuration loader.
 	 */
 	public void setConfigLoader(ConfigurationLoader configLoader) {
 		this.configLoader = configLoader;
 	}
 
 	/**
 	 * Gets the registry of history plugins.
 	 * @return Registry of history plugins.
 	 */
 	public HistoryProviders getHistoryProviders() {
 		return historyProviders;
 	}
 
 	/**
 	 * Sets the registry of history plugins.
 	 * @param historyProviders - new registry of history plugins.
 	 */
 	public void setHistoryProviders(HistoryProviders historyProviders) {
 		this.historyProviders = historyProviders;
 	}
 	
 	public ItemRewardListener getItemListener() {
 		return itemListener;
 	}
 
 	public Presets getPresets() {
 		return presets;
 	}
 		
 	public PlayerScheduler getPlayerScheduler() {
 		return playerScheduler;
 	}
 	
 	/**
 	 * Retrieves the global plugin settings.
 	 * @return Global settings.
 	 */
 	public GlobalSettings getGlobalSettings() {
 		return globalSettings;
 	}
 	
 	/**
 	 * Retrieves the current registered action types.
 	 * @return Registry of action types.
 	 */
 	public ActionTypes getActionTypes() {
 		return configLoader.getActionTypes();
 	}
 
 	/**
 	 * Retrieves a list of action rewards that applies when a mob is killed, either by the environment (when KILLER is NULL), 
 	 * or by a player. 
 	 * <p>
 	 * Note that the returned list contains every possible reward that matches the given mob. In reality, only the 
 	 * first item will be awarded.
 	 * 
 	 * @param killer - the player that killed this mob, or NULL if the mob died naturally.
 	 * @param query - query representing the mob that was killed.
 	 * @return A list of possible rewards. Only the first item will be chosen when rewards are actually awarded.
 	 * @throws ParsingException If the stored preset option associated with the killer is malformed.
 	 */
 	public List<Action> getMobReward(Player killer, MobQuery query) throws ParsingException {
 		
 		Configuration config = getPresets().getConfiguration(killer);
 		
 		// Mirror the function below
 		return config.getExperienceDrop().getAllRanked(query);
 	}
 	
 	/**
 	 * Retrieves a list of action rewards that applies when a player performs a given action to the item or block
 	 * specified by the query.
 	 * <p>
 	 * The query must be a ItemQuery for every trigger except brewing, where it also can be a PotionQuery.
 	 * <p>
 	 * Also note that this list contains every possible reward that matches the given parameters. In reality, only the 
 	 * first item will be awarded.
 	 * 
 	 * @param player - player performing the given action, or NULL if the default configuration file should be used.
 	 * @param trigger - action the player performs. 
 	 * @param query - query representing the item or block that was the target of the action.
 	 * @return A list of possible rewards. Only the first item will be chosen when rewards are actually awarded.
 	 * @throws ParsingException If the stored preset option associated with this player is malformed.
 	 */
 	public List<Action> getPlayerReward(Player player, Integer trigger, Query query) throws ParsingException {
 		
 		Configuration config = getPresets().getConfiguration(player);
 		
 		Integer brewing = getActionTypes().getType(ActionTypes.BREWING);
 		ItemTree current = config.getActionReward(trigger);
 		
 		// Special brewing type
 		if (trigger == brewing && query instanceof PotionQuery) {
 			
 			// Use the complex brewing reward rules
 			return config.getComplexBrewingReward().getAllRanked((PotionQuery) query);
 			
 		} else {
 			// Check for incorrect action types/triggers
 			if (current == null) {
 				throw new IllegalArgumentException(String.format("Unknown trigger ID: %s", trigger));
 			}
 			
 			// Standard item reward rules
 			return current.getAllRanked((ItemQuery) query);
 		}
 	}
 	
 	/**
 	 * Handles the given inventory event using the default behavior for the given inventory type.
 	 * @param event - inventory click event.
 	 * @param response - block response detailing how to process the inventory.
 	 */
 	public void processInventoryClick(InventoryClickEvent event, BlockResponse response) {
 		if (xpItemListener == null)
 			throw new RuntimeException("ExperienceMod isn't loaded yet.");
 
 		xpItemListener.processInventory(event, response);
 	}
 	
 	private void setPresets(Presets presets) {
 		
 		// Create a new listener if necessary
 		if (xpBlockListener == null || xpItemListener == null || xpMobListener == null) {
 			xpItemListener = new ExperienceItemListener(this, playerScheduler, customProvider, presets);
 			xpBlockListener = new ExperienceBlockListener(this, presets, historyProviders);
 			xpMobListener = new ExperienceMobListener(this, presets);
 			xpEnchancer = new ExperienceEnhancementsListener(this);
 			xpCleanup = new ExperienceCleanupListener(presets, interactionListener, playerScheduler);
 			
 		} else {
 			xpItemListener.setPresets(presets);
 			xpBlockListener.setPresets(presets);
 			xpMobListener.setPresets(presets);
 			xpCleanup.setPlayerCleanupListeners(presets, interactionListener, playerScheduler);
 		}
 	}
 	
 	@Override
 	public void printDebug(Object sender, String message, Object... params) {
 		if (debugEnabled) {
 			
 			String formattedMessage = String.format("[ExperienceMod] " + message, params);
 			
 			// Every player with the info permission will also see this message
 			getServer().broadcast(formattedMessage, Permissions.INFO);
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
 		
 		if (debugEnabled) {
 			currentLogger.warning(String.format("Warning sent from %s.", sender));
 		}
 		
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
