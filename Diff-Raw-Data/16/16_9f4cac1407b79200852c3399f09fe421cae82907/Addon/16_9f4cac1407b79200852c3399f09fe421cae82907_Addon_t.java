 package com.titankingdoms.nodinchan.titanchat.addon;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.Listener;
 
 import com.nodinchan.loader.Loadable;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.channel.CustomChannel;
 import com.titankingdoms.nodinchan.titanchat.command.Command;
 
 /**
  * Addon - Addon base
  * 
  * @author NodinChan
  *
  */
 public class Addon extends Loadable implements Listener {
 	
 	protected final TitanChat plugin;
 	
 	private final AddonManager manager;
 	
 	private Logger log = Logger.getLogger("TitanLog");
 	
 	private File configFile = null;
 	private FileConfiguration config = null;
 	
 	public Addon(String name) {
 		super(name);
 		this.plugin = TitanChat.getInstance();
 		this.manager = plugin.getAddonManager();
 	}
 	
 	/**
 	 * Check if an Addon equals another
 	 */
 	@Override
 	public boolean equals(Object object) {
 		if (object instanceof Addon)
 			return ((Addon) object).getName().equals(getName());
 		
 		return false;
 	}
 	
 	/**
 	 * Gets the config
 	 * 
 	 * @return The config
 	 */
 	public final FileConfiguration getConfig() {
 		if (config == null) { reloadConfig(); }
 		return config;
 	}
 	
 	/**
 	 * Gets the data folder
 	 * 
 	 * @return The data folder
 	 */
 	public final File getDataFolder() {
 		File dir = new File(plugin.getAddonDir(), super.getName());
 		dir.mkdir();
 		return dir;
 	}
 	
 	/**
 	 * Gets the Logger
 	 * 
 	 * @return The Logger
 	 */
 	public Logger getLogger() {
 		return log;
 	}
 	
 	/**
 	 * Gets the file from the JAR file
 	 * 
 	 * @param fileName The name of the file
 	 * 
 	 * @return The file if found, otherwise null
 	 */
 	public final InputStream getResource(String fileName) {
 		return manager.getResource(this, fileName);
 	}
 	
 	/**
 	 * Registers the Command
 	 * 
 	 * @param command The Command to register
 	 */
 	public final void register(Command command) {
 		plugin.getCommandManager().register(command);
 	}
 	
 	/**
 	 * Registers the Custom Channel
 	 * 
 	 * @param channel The Custom Channel to register
 	 */
 	public final void register(CustomChannel channel) {
 		plugin.getChannelManager().register(channel);
 	}
 	
 	/**
 	 * Registers the Listener
 	 * 
 	 * @param listener The Listener to register
 	 */
 	public final void register(Listener listener) {
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
 	}
 	
 	/**
 	 * Reloads the config
 	 */
 	public final void reloadConfig() {
 		if (configFile == null) { configFile = new File(new File(plugin.getAddonDir(), super.getName()), "config.yml"); }
 		
 		config = YamlConfiguration.loadConfiguration(configFile);
 		
 		InputStream defConfigStream = getResource("config.yml");
 		
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			config.setDefaults(defConfig);
 		}
 	}
 	
 	/**
 	 * Saves the config
 	 */
 	public final void saveConfig() {
 		if (config == null || configFile == null) { return; }
 		try { config.save(configFile); } catch (IOException e) {}
 	}
 	
 	/**
 	 * Returns the Addon as a String
 	 */
 	@Override
 	public String toString() {
 		return "Addon:" + super.getName();
 	}
 }
