 package com.Zolli.EnderCore;
 
 import java.io.File;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.Zolli.EnderCore.Configuration.Configuration;
 import com.Zolli.EnderCore.Listeners.blockListener;
 import com.Zolli.EnderCore.Listeners.entityListener;
 import com.Zolli.EnderCore.Listeners.inventoryListener;
 import com.Zolli.EnderCore.Listeners.playerListener;
 import com.Zolli.EnderCore.Listeners.serverListeners;
 import com.Zolli.EnderCore.Listeners.worldListener;
 import com.Zolli.EnderCore.Localization.localizationManager;
 import com.Zolli.EnderCore.Logger.simpleLogger;
 import com.Zolli.EnderCore.Logger.simpleLogger.Level;
 import com.Zolli.EnderCore.Storage.Storage;
import com.Zolli.EnderCore.Storage.Storage.storageEngine;
 import com.Zolli.EnderCore.Storage.storageActions;
 
 public class EnderCore extends JavaPlugin {
 	
 	/**
 	 * Bukkit PluginManager instance
 	 */
 	public PluginManager pluginManager;
 	
 	/**
 	 * SimpleLogger object
 	 */
 	public simpleLogger logger;
 	
 	/**
 	 * Configuration object for main config
 	 */
 	private Configuration mainConfig;
 	
 	/**
 	 * Configuration object for flatfile storage engine
 	 */
 	private Configuration storageConfig;
 	
 	/**
 	 * PluginDescriptionFile object
 	 */
 	private PluginDescriptionFile pluginDescription;
 	
 	/**
 	 * Location of plugin data folder
 	 */
 	private File dataFolder;
 	
 	/**
 	 * Main configuration object
 	 */
 	public YamlConfiguration config;
 	
 	/**
 	 * Storage engine class
 	 */
 	public Storage storage;
 	
 	/**
 	 * StorageActions class
 	 */
 	public storageActions dbAction;
 	
 	/**
 	 * LocalizationManager class
 	 */
 	public localizationManager local;
 	
 	/**
 	 * Runs when plugin initialization started
 	 */
 	public void onLoad() {
 		this.pluginDescription = getDescription();
 		this.pluginManager = this.getServer().getPluginManager();
 		
 		this.dataFolder = this.getDataFolder();
 		this.logger = new simpleLogger(this.pluginDescription, this.dataFolder, "EnderCore.Log");
 		this.mainConfig = new Configuration(this, "config.yml");
 	}
 	
 	/**
 	 * Runs when initialization end (loaded plugin.yml file) 
 	 */
 	public void onEnable() {
 		config = mainConfig.config;
 		String driver = config.getString("database.type");
 		
 		this.storage = new Storage(this, driver, this.dataFolder, this.config, this.logger);
 		this.dbAction = new storageActions(this.storage);
 		this.local = new localizationManager(this);
 		
 		this.registerListeners();
 		
 		this.logger.log(Level.INFO, "Sucessfully enabled!");
 	}
 	
 	/**
 	 * Runs when disabling signal sent
 	 */
 	public void onDisable() {
 		this.logger.log(Level.INFO, "Disabling...");
 	}
 	
 	/**
 	 * Register all listeners into EventHandler
 	 */
 	private void registerListeners() {
 		this.pluginManager.registerEvents(new blockListener(this), this);
 		this.pluginManager.registerEvents(new entityListener(this), this);
 		this.pluginManager.registerEvents(new inventoryListener(this), this);
 		this.pluginManager.registerEvents(new playerListener(this), this);
 		this.pluginManager.registerEvents(new serverListeners(this), this);
 		this.pluginManager.registerEvents(new worldListener(this), this);
 	}
 	
 	/**
 	 * Initialize flatfile storage engine, if needed. Called in storageEngine
 	 * @return 
 	 */
 	public Configuration initializeFlatfile() {
 		this.storageConfig = new Configuration(this, "storage.yml");
 		return this.storageConfig;
 	}
 	
 }
