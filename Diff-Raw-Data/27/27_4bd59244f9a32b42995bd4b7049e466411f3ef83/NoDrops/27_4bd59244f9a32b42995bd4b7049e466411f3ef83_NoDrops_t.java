 package com.jsne10.nodrops;
 
 import com.jsne10.nodrops.listeners.*;
import com.jsne10.nodrops.util.Metrics;
 
 import java.io.IOException;
 
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NoDrops extends JavaPlugin {
 	
 	public static NoDrops plugin;
	
	private Configuration config;
	private FileConfiguration configFile;
 
 	@Override
	public void onLoad() {
 		
 		plugin = this;
		configFile = getConfig();
		
	}
	
	@Override
	public void onEnable() {
 
 		// Registers the Drop listener events.
 		this.getServer().getPluginManager().registerEvents(new DropsDisable(), this);
 		this.getServer().getPluginManager().registerEvents(new DropOnDeathDisable(), this);
 		
 		// Plugin Metrics.
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
		    this.getLogger().info("Successfully hooked to Plugin Metrics.");
 		} catch (IOException e) {
		    this.getLogger().warning("Failed to hook to Plugin Metrics");
 		}
 
 	}
 
 	@Override
 	public void onDisable() {}
 
 }
 
