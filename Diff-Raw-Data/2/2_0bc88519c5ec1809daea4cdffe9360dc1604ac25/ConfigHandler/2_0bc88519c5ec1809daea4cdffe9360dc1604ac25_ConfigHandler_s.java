 package com.mitsugaru.WorldChannels.config;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 
 import com.mitsugaru.WorldChannels.WorldChannels;
 
 public class ConfigHandler {
     private WorldChannels plugin;
     private Map<String, WorldConfig> configs = new HashMap<String, WorldConfig>();
     private String formatterString, shoutFormat, nobodyString;
     private boolean formatterUse;
     public boolean debugTime;
 
     public ConfigHandler(WorldChannels plugin) {
 	this.plugin = plugin;
 	// Load defaults
 	final ConfigurationSection config = plugin.getConfig();
 	// LinkedHashmap of defaults
 	final Map<String, Object> defaults = new LinkedHashMap<String, Object>();
 	defaults.put("formatter.use", true);
 	defaults.put("formatter.defaultFormat",
 		"%world %group %prefix%name%suffix: %message");
 	defaults.put("shout.format", "%prefix%name%suffix shouts: %message");
	defaults.put("nobody.message", "&No one can hear you...");
 	defaults.put("debug.time", false);
 	defaults.put("version", plugin.getDescription().getVersion());
 	// Insert defaults into config file if they're not present
 	for (final Entry<String, Object> e : defaults.entrySet()) {
 	    if (!config.contains(e.getKey())) {
 		config.set(e.getKey(), e.getValue());
 	    }
 	}
 	// Save config
 	plugin.saveConfig();
 	// Load settings
 	this.loadSettings(config);
 	// Check if worlds folder exists
 	final File file = new File(plugin.getDataFolder().getAbsolutePath()
 		+ "/worlds");
 	if (!file.exists()) {
 	    // Create directory
 	    if (!file.mkdir()) {
 		plugin.getLogger()
 			.warning(
 				"Something went wrong! Could not create worlds directory.");
 	    }
 	}
 	// Load config per world
 	final List<World> worlds = plugin.getServer().getWorlds();
 	for (World world : worlds) {
 	    final String worldName = world.getName();
 	    configs.put(worldName, new WorldConfig(plugin, worldName));
 	}
 	plugin.getLogger().info("Configuration loaded");
     }
 
     public void reloadConfigs() {
 	plugin.reloadConfig();
 	for (WorldConfig config : configs.values()) {
 	    config.reload();
 	}
 	this.loadSettings(plugin.getConfig());
     }
 
     private void loadSettings(ConfigurationSection config) {
 	/**
 	 * Formatter
 	 */
 	formatterUse = config.getBoolean("formatter.use", true);
 	formatterString = config.getString("formatter.defaultFormat",
 		"%world %group %prefix%name%suffix: %message");
 	/**
 	 * Shout
 	 */
 	shoutFormat = config.getString("shout.format",
 		"%prefix%name%suffix shouts: %message");
 	/**
 	 * Nobody
 	 */
 	nobodyString = config.getString("nobody.message", "&No one can hear you...");
 	/**
 	 * Debug
 	 */
 	debugTime = config.getBoolean("debug.time", false);
     }
 
     public WorldConfig getWorldConfig(String worldName) {
 	WorldConfig out = configs.get(worldName);
 	if (out == null) {
 	    out = new WorldConfig(plugin, worldName);
 	    configs.put(worldName, out);
 	}
 	return out;
     }
 
     public Set<String> getWorldChannels(String worldName) {
 	Set<String> listeners = new HashSet<String>();
 	if (configs.containsKey(worldName)) {
 	    final List<String> list = configs.get(worldName).getWorldList();
 	    if (!list.isEmpty()) {
 		listeners = new HashSet<String>(list);
 	    }
 	}
 	return listeners;
     }
 
     public boolean useFormatter() {
 	return formatterUse;
     }
 
     public String getFormat() {
 	return formatterString;
     }
 
     public String getShoutFormat() {
 	return shoutFormat;
     }
     
     public String getNobodyMessage()
     {
 	return nobodyString;
     }
 }
