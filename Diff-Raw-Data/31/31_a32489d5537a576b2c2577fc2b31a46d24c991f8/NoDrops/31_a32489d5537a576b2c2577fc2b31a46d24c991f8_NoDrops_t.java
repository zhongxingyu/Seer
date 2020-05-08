 package com.jsne10.nodrops;
 
 import com.jsne10.nodrops.listeners.*;
 
 import java.io.IOException;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.jsne10.nodrops.util.Metrics;
 
 public class NoDrops extends JavaPlugin {
 	
 	public static NoDrops plugin;
 
 	@Override
 	public void onEnable() {
 		
 		plugin = this;
 
 		// Registers the Drop listener events.
 		this.getServer().getPluginManager().registerEvents(new DropsDisable(), this);
 		this.getServer().getPluginManager().registerEvents(new DropOnDeathDisable(), this);
 		
 		// Plugin Metrics.
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		    this.getLogger().info("Successfully connected to Plugin Metrics.");
 		} catch (IOException e) {
 		    this.getLogger().warning("Failed to connect to Plugin Metrics");
 		}
 
 	}
 
 	@Override
 	public void onDisable() {}
 
 }
 
