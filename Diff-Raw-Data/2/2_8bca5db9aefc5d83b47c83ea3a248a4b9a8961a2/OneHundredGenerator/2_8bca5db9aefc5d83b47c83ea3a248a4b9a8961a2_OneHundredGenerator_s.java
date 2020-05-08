 package com.djdch.bukkit.onehundredgenerator;
 
 import java.util.HashMap;
 
 import org.bukkit.World;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.generator.ChunkGenerator;
 
 import com.djdch.bukkit.onehundredgenerator.configuration.WorldConfiguration;
 import com.djdch.bukkit.onehundredgenerator.listener.WorldListener;
 import com.djdch.bukkit.utils.Logger;
 
 /**
  * Main class of the <b>OneHundredGenerator</b> plugin for Bukkit.
  * <p>
  * Minecraft 1.0.0 terrain generator plugin for Bukkit.
  *
  * @author DjDCH
  */
 public class OneHundredGenerator extends JavaPlugin {
 	/**
 	 * Contains the Logger instance.
 	 */
 	protected final Logger logger = new Logger();
 
 	/**
 	 * Contains the deathListener instance.
 	 */
 	protected final WorldListener worldListener = new WorldListener(this);
 
 	/**
 	 *
 	 */
	protected final HashMap<String, WorldConfiguration> worldsSettings = new HashMap();
 
 	/**
 	 * Method execute when the plugin is enable.
 	 */
 	public void onEnable() {
 		this.logger.setPrefix(getDescription().getName());
 
 		// Register the plugin events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.WORLD_INIT, this.worldListener, Event.Priority.High, this);
 
 		this.logger.info("Version " + getDescription().getVersion() + " enable.");
 	}
 
 	/**
 	 * Method execute when the plugin is disable.
 	 */
 	public void onDisable() {
 		this.logger.info("Version " + getDescription().getVersion() + " disable.");
 	}
 
 	/**
 	 *
 	 */
 	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
 		return null;
 	}
 
 	/**
 	 *
 	 * @param world
 	 */
 	public void WorldInit(World world) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Accessor who return the logger instance.
 	 *
 	 * @return Logger instance.
 	 */
 	public Logger getLogger() {
 		return this.logger;
 	}
 }
