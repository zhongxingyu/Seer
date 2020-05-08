 package me.limebyte.endercraftessentials;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Represents the main class for EndercraftEssentials.
  */
 public class EndercraftEssentials extends JavaPlugin {
 
 	/**
 	 * The current running EndercraftEssentials Object instance.
 	 */
 	private static EndercraftEssentials instance;
 	
 	/**
 	 * The CraftBukkit console logger for this JavaPlugin.
 	 */
 	private static Logger logger;
 	
 	/**
 	 * Cast when this JavaPlugin is enabled by the server instance.
 	 */
 	@Override
 	public final void onEnable() {
 		// Set instance variables
 		instance = this;
 		logger = this.getLogger();
 		
 		// Register Events
 		this.getServer().getPluginManager().registerEvents(new EventListener(), this);
 		
 		// Log enable message
 		this.log().info("Enabled!");
 	}
 	
 	/**
 	 * Cast when this JavaPlugin is disabled by the server instance.
 	 */
 	@Override
 	public final void onDisable() {
 		// Log disable message
 		this.log().info("Disabled.");
 	}
 	
 	/**
 	 * Gets the current running EndercraftEssentials Object instance.
 	 * @return The current instance
 	 */
	public static EndercraftEssentials getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Gets the current console logger for EndercraftEssentials.
 	 * @return The current logger
 	 */
 	public final Logger log() {
 		return logger;
 	}
 	
 }
