 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags.data;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Modified YAML manager from http://wiki.bukkit.org/Configuration_API_Reference
  */
 public final class CustomYML {
 	private static JavaPlugin plugin;
 	private final String dataFile;
 	private FileConfiguration customConfig = null;
 	private File customConfigFile = null;
 
 	// Construct a new CustomYML file
 	public CustomYML(JavaPlugin plugin, String dataFile) {
		CustomYML.plugin = plugin;
 		this.dataFile = dataFile;
 	}
 
 	// Gets the custom config file.
 	public FileConfiguration getConfig() {
 		if (customConfig == null) {
 			reload();
 		}
 		return customConfig;
 	}
 
 	// Reloads the file to the MemorySection
 	public void reload() {
 		if (customConfigFile == null) {
 			customConfigFile = new File(plugin.getDataFolder(), dataFile);
 		}
 		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
 
 		// Look for defaults in the jar
 		final InputStream defConfigStream = plugin.getResource(dataFile);
 		if (defConfigStream != null) {
 			final YamlConfiguration defConfig = YamlConfiguration
 					.loadConfiguration(defConfigStream);
 			customConfig.setDefaults(defConfig);
 		}
 	}
 
 	// Saves all changes
 	public void saveConfig() {
 		if (customConfig == null || customConfigFile == null) {
 			return;
 		}
 		try {
 			getConfig().save(customConfigFile);
 		} catch (final IOException ex) {
 			plugin.getLogger().log(Level.SEVERE,
 					"Could not save config to " + customConfigFile, ex);
 		}
 	}
 
 	// Save a default config file to the data folder.
 	public void saveDefaultConfig() {
 		if (customConfigFile == null) {
 			customConfigFile = new File(plugin.getDataFolder(), dataFile);
 		}
 
 		if (!customConfigFile.exists()) {
 			plugin.saveResource(dataFile, false);
 		}
 	}
 }
