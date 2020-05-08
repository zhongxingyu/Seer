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
 
 public class ConfigHandler
 {
 	private WorldChannels plugin;
 	private Map<String, Config> configs = new HashMap<String, Config>();
 	private String formatterString;
 	private boolean formatterUse;
 	public boolean debugTime;
 
 	public ConfigHandler(WorldChannels plugin)
 	{
 		this.plugin = plugin;
 		// Load defaults
 		final ConfigurationSection config = plugin.getConfig();
 		// LinkedHashmap of defaults
 		final Map<String, Object> defaults = new LinkedHashMap<String, Object>();
 		defaults.put("formatter.use", true);
 		defaults.put("formatter.defaultFormat",
				"%world %group %prefix%name%suffix");
 		defaults.put("debug.time", false);
 		defaults.put("version", plugin.getDescription().getVersion());
 		// Insert defaults into config file if they're not present
 		for (final Entry<String, Object> e : defaults.entrySet())
 		{
 			if (!config.contains(e.getKey()))
 			{
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
 		if (!file.exists())
 		{
 			// Create directory
 			file.mkdir();
 		}
 		// Load config per world
 		final List<World> worlds = plugin.getServer().getWorlds();
 		for (World world : worlds)
 		{
 			final String worldName = world.getName();
 			configs.put(worldName, new Config(plugin, worldName));
 		}
 		plugin.getLogger().info("Configuration loaded");
 	}
 
 	public void reloadConfigs()
 	{
 		plugin.reloadConfig();
 		for (Config config : configs.values())
 		{
 			config.reload();
 		}
 		this.loadSettings(plugin.getConfig());
 	}
 
 	private void loadSettings(ConfigurationSection config)
 	{
 		/**
 		 * Formatter
 		 */
		formatterUse = config.getBoolean("formatter.format.use", true);
		formatterString = config.getString("formatter.format.defaultFormat",
				"%world %group %prefix%name%suffix");
 		/**
 		 * Debug
 		 */
 		debugTime = config.getBoolean("debug.time", false);
 	}
 
 	public Config getWorldConfig(String worldName)
 	{
 		Config out = configs.get(worldName);
 		if (out == null)
 		{
 			out = new Config(plugin, worldName);
 			configs.put(worldName, out);
 		}
 		return out;
 	}
 
 	public Set<String> getWorldChannels(String worldName)
 	{
 		Set<String> listeners = new HashSet<String>();
 		if (configs.containsKey(worldName))
 		{
 			final List<String> list = configs.get(worldName).getWorldList();
 			if (!list.isEmpty())
 			{
 				listeners = new HashSet<String>(list);
 			}
 		}
 		return listeners;
 	}
 
 	public boolean useFormatter()
 	{
 		return formatterUse;
 	}
 
 	public String getFormat()
 	{
 		return formatterString;
 	}
 }
