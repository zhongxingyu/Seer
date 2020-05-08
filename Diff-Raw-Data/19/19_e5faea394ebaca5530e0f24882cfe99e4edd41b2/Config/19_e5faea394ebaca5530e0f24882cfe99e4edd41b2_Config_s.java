 package com.mitsugaru.WorldChannels.config;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.mitsugaru.WorldChannels.WorldChannels;
 
 public class Config
 {
 	private String worldName;
 	private WorldChannels plugin;
 	private File file;
 	private YamlConfiguration config;
 	private boolean formatterUse, includeLocal;
 	private String formatterString;
 
 	public Config(WorldChannels plugin, String worldName)
 	{
 		this.plugin = plugin;
 		this.worldName = worldName;
 		// Grab file
 		this.file = new File(plugin.getDataFolder().getAbsolutePath()
 				+ "/worlds/" + worldName + ".yml");
 		this.config = YamlConfiguration.loadConfiguration(file);
 		loadDefaults();
 		loadVariables();
 	}
 
 	public void save()
 	{
 		// Set config
 		try
 		{
 			// Save the file
 			config.save(file);
 		}
 		catch (IOException e1)
 		{
 			plugin.getLogger().warning(
 					"File I/O Exception on saving heroes config");
 			e1.printStackTrace();
 		}
 	}
 
 	public void reload()
 	{
 		try
 		{
 			config.load(file);
 		}
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (InvalidConfigurationException e)
 		{
 			e.printStackTrace();
 		}
 		loadDefaults();
 		loadVariables();
 	}
 
 	private void loadDefaults()
 	{
 		// LinkedHashmap of defaults
 		final Map<String, Object> defaults = new LinkedHashMap<String, Object>();
 		// TODO defaults
 		defaults.put("formatter.use", false);
		defaults.put("formatter.form", "%world %group %prefix%name%suffix");
 		defaults.put("includeLocalPlayers", true);
 		defaults.put("worlds", new ArrayList<String>());
 		// Add to config if missing
 		for (final Entry<String, Object> e : defaults.entrySet())
 		{
 			if (!config.contains(e.getKey()))
 			{
 				config.set(e.getKey(), e.getValue());
 			}
 		}
 		save();
 	}
 
 	private void loadVariables()
 	{
 		// load variables
 		formatterUse = config.getBoolean("formatter.use", false);
 		formatterString = config.getString("formatter.format",
				"%world %group %prefix%name%suffix");
 		includeLocal = config.getBoolean("includeLocalPlayers", true);
 	}
 
 	public List<String> getWorldList()
 	{
 		List<String> listeners = config.getStringList("worlds");
 		if (listeners == null)
 		{
 			listeners = new ArrayList<String>();
 		}
 		return listeners;
 	}
 
 	public String getWorldName()
 	{
 		return worldName;
 	}
 
 	public boolean useFormatter()
 	{
 		return formatterUse;
 	}
 	
 	public String getFormat()
 	{
 		return formatterString;
 	}
 	
 	public boolean includeLocalPlayers()
 	{
 		return includeLocal;
 	}
 }
