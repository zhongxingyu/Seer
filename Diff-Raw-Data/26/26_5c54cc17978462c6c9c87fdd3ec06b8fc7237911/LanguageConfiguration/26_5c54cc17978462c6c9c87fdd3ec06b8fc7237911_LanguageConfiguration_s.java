 package com.almuramc.aqualock.bukkit.configuration;
 
 import java.io.File;
 import java.util.HashMap;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class LanguageConfiguration {
 	private final FileConfiguration config;
 	private final HashMap<String, String> nodes = new HashMap<String, String>();
 
 	public LanguageConfiguration(File langYml) {
 		config = YamlConfiguration.loadConfiguration(langYml);
 		construct();
 	}
 
 	private final void construct() {
 		ConfigurationSection language = config.getConfigurationSection("language");
 		if (language == null) {
 			throw new IllegalStateException("Missing language section in your lang.yml.");
 		}
 		for (String key : language.getKeys(false)) {
 			final String value = language.getString(key);
 			if (value.isEmpty() || value == null) {
 				Bukkit.getLogger().warning(AqualockPlugin.getPrefix() + "Encountered " + key + " in lang.yml but its either empty or invalid! Skipping...");
 				continue;
 			}
 			if (nodes.containsKey(key)) {
 				Bukkit.getLogger().warning(AqualockPlugin.getPrefix() + "Encountered duplicate " + key + " in lang.yml. Replacing with new value...");
 			}
 			nodes.put(key, value);
 		}
 	}
 
 	public void reload() {
 		nodes.clear();
 		construct();
 	}
 }
 
