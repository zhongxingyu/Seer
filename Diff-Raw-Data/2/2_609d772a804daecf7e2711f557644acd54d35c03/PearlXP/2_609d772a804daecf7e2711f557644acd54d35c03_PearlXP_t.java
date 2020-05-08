 /**
  * Rewrites of the original PearlXP created by Nebual of nebtown.info in March 2012.
  * 
  * Small plugin to enable the storage of experience points in an item  la soul gem.
  * 
  * Contributors: Marex, Zonta.
  * 
  * Copyrights belongs to their respective owners.
  */
 
 package info.nebtown.PearlXP;
 
 import java.util.logging.Logger;
 import java.io.File;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class PearlXP extends org.bukkit.plugin.java.JavaPlugin {
 
 
 	/**
 	 * Name of the plugin
 	 */
 	public static final String NAME = "PearlXP";
 
 	/**
 	 * Maximum storage capacity of a item.
 	 */
 	public static final int MAX_STORAGE = 32767; // max of a short
 
 	/****** Configuration options ******/
 
 	/**
 	 * Configuration value of the maximum storage capacity
 	 */
 	private static int maxLevel;
 
 	/**
 	 * Configuration value of the item id used
 	 */
 	private static int itemId;
 
 	private static Logger logger;
 
 	@Override
 	public void onEnable() {
 
 		logger = Logger.getLogger("Minecraft");
 
 		// Initializing config options
 		itemId = this.getConfig().getInt("itemid");
 		setMaxLevel(this.getConfig().getInt("maxlevel"));
 
 		// Check if a config file is missing and create it
 		if (YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"))
 				.getInt("configversion", 0) < 2) {
 
 			saveResource("config.yml",true);
 			reloadConfig();
 
 		} else {
 			getConfig().options().copyDefaults(true);
 		}
 
 		this.getServer().getPluginManager().registerEvents(new PearlXPListener(), this);
 
 		logger.info(NAME + ": Plugin loading complete. Plugin enabled.");
 	}
 
 	@Override
 	public void onDisable() {
 		logger.info(NAME + ": Plugin disabled.");
 	}
 
 	/**
 	 * @return the maxLevel
 	 */
 	public static int getMaxLevel() {
 		return maxLevel;
 	}
 
 	/**
 	 * @return the logger
 	 */
 	public static Logger getPluginLogger() {
 		return logger;
 	}
 
 	/**
 	 * @return the itemId
 	 */
 	public static int getItemId() {
 		return itemId;
 	}
 
 	/**
 	 * @param maxLevel the maxLevel to set
 	 */
 	public static void setMaxLevel(int maxLevel) {
 
		// check if maxLevel fits in a short (2^15 - 1)
 		if (maxLevel > MAX_STORAGE) {
 			PearlXP.maxLevel = MAX_STORAGE;
 			logger.info(NAME+ ": WARNING: maxLevel exceeds possible limits! Please modify your config file.");
 			logger.info(NAME+ ": Setting maxLevel to " + maxLevel);
 		} else { 
 			PearlXP.maxLevel = maxLevel;
 		}
 	}
 
 
 }
