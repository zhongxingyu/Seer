 package net.worldoftomorrow.eventcontrol;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import net.worldoftomorrow.eventcontrol.permcache.PermCache;
 import net.worldoftomorrow.eventcontrol.permcache.CacheListener;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EventControl extends JavaPlugin {
 	
 	private static EventControl instance;
 	private final PermCache permCache = new PermCache();
 	private I18n local;
 	
 	@Override
 	public void onEnable() {
 		instance = this;
 		// Load configuration
 		this.setConfigDefaults(); // Set the defaults
 		this.saveConfig(); // Save the config
 		this.reloadConfig(); // Now reload it
 		
 		// Load localization
 		local = new I18n(this.getConfig().getString("Language"));
 		this.getLogger().log(Level.INFO, local.$("localization.loaded"));
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		// Register Cache listener.
 		pm.registerEvents(new CacheListener(permCache), this);
 	}
 	
 	public static EventControl instance() {
 		return instance;
 	}
 	
 	public File geti18nFolder() {
		return new File(this.getDataFolder() + File.separator + "lang");
 	}
 	
 	private void setConfigDefaults() {
 		FileConfiguration config = this.getConfig();
 		config.options().copyDefaults(true); //Append new keys to existing file.
 		// All configuration defaults go here
 		config.addDefault("Language", "en_US");
 	}
 	
 	public I18n getLocalization() {
 		return local;
 	}
 }
