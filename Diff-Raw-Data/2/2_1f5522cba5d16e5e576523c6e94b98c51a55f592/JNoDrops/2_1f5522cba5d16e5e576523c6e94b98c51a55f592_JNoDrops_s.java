 /*
  * Copyright (C) 2013 jsne10.  All rights reserved.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation,  version 3.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package com.jsne10.jnodrops;
 
 import com.jsne10.jnodrops.command.Admin;
 import com.jsne10.jnodrops.event.*;
 import com.jsne10.jnodrops.util.ConfigManager;
 import com.jsne10.jnodrops.util.Metrics;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class JNoDrops extends JavaPlugin {
 
 	private static JNoDrops plugin;
 	private ConfigManager configManager;
 	
 	@Override
 	public void onEnable() {
 		
 		// Initialize objects
 		plugin = this;
 		configManager = new ConfigManager();
 
 		// Registers the plugin events.
 		this.getServer().getPluginManager().registerEvents(new DropsDisable(), this);
 		this.getServer().getPluginManager().registerEvents(new PotionDisable(), this);
 		this.getServer().getPluginManager().registerEvents(new PotionDespawner(), (this));
 		this.getServer().getPluginManager().registerEvents(new DropOnDeathDisable(), this);
 		
 		// Register admin commands.
 		this.getCommand("jnodrops").setExecutor(new Admin());
 		
 		// Plugin Metrics.
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		    this.getLogger().info("Successfully hooked to Plugin Metrics.");
 		} catch (IOException e) {
 		    this.getLogger().warning("Failed to hook to Plugin Metrics");
 		}
 		
 		// Check for updates..
 		this.checkForUpdate();
 
 	}
 
 	@Override
 	public void onDisable() {}
 	
 	/** Called to check for updates. If one is available, then register an alter listener and log it in the console. */
 	private void checkForUpdate() {
 		
 		if (this.getConfig().getBoolean("checkForUpdates")) {
 			try {
 				URL url = new URL("https://raw.github.com/jsne10/jNoDrops/master/lastestversion");
 				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
 
 				if (!reader.readLine().equals(this.getDescription().getVersion())) {
 					this.getServer().getPluginManager().registerEvents(new UpdateAlert(), this);
					this.getLogger().info("A new version of jNoDrops is avialable! LINK: http://dev.bukkit.org/bukkit-plugins/jnodrops/");
 				}
 
 				reader.close();
 			} catch (MalformedURLException e) {
 				this.getLogger().warning("Unable to check for updates.");
 			} catch (IOException e) {
 				this.getLogger().warning("Unable to check for updates.");
 			}
 		}
 		
 	}
 	
 	/** Plugin instance getter. */
 	public static JNoDrops getPlugin() {
 		return plugin;
 	}
  
 	/** Config Manager instance getter. */
 	public ConfigManager getConfigManager() {
 		return configManager;
 	}
 	
 }
 
