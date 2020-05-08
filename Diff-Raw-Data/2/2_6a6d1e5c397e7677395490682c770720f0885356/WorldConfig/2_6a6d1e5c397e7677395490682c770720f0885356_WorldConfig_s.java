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
 import com.mitsugaru.WorldChannels.tasks.WorldAnnouncerTask;
 
 public class WorldConfig
 {
 	private String worldName, formatterString;
 	private WorldChannels plugin;
 	private File file;
 	private YamlConfiguration config;
 	private boolean formatterUse, includeLocal, announcerUse;
	private static final int minutesToTicks = 3600;
 	private int announcerInterval = 15, announcerId = -1;
 	private List<String> announcements = new ArrayList<String>(),
 			broadcastWorlds = new ArrayList<String>();
 
 	public WorldConfig(WorldChannels plugin, String worldName)
 	{
 		this.plugin = plugin;
 		this.worldName = worldName;
 		// Grab file
 		this.file = new File(plugin.getDataFolder().getAbsolutePath()
 				+ "/worlds/" + worldName + ".yml");
 		if (!file.exists())
 		{
 			try
 			{
 				file.createNewFile();
 			}
 			catch (IOException e)
 			{
 				plugin.getLogger().severe(
 						"Could not create config file for world: " + worldName);
 				e.printStackTrace();
 			}
 		}
 		this.config = YamlConfiguration.loadConfiguration(file);
 		reload();
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
 			plugin.getLogger().severe(
 					"Could not find config file for world: " + worldName);
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			plugin.getLogger().severe(
 					"IOException for config file for world: " + worldName);
 			e.printStackTrace();
 		}
 		catch (InvalidConfigurationException e)
 		{
 			plugin.getLogger().severe(
 					"Invalid config file for world: " + worldName);
 			e.printStackTrace();
 		}
 		loadDefaults();
 		loadVariables();
 		boundsCheck();
 		startAnnouncer();
 	}
 
 	private void loadDefaults()
 	{
 		// LinkedHashmap of defaults
 		final Map<String, Object> defaults = new LinkedHashMap<String, Object>();
 		defaults.put("formatter.use", false);
 		defaults.put("formatter.format",
 				"%world %group %prefix%name%suffix: %message");
 		defaults.put("announcer.use", false);
 		defaults.put("announcer.interval", 15);
 		defaults.put("announcer.annoucements", new ArrayList<String>());
 		defaults.put("includeLocalPlayers", true);
 		defaults.put("broadcastToWorlds", new ArrayList<String>());
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
 				"%world %group %prefix%name%suffix: %message");
 		includeLocal = config.getBoolean("includeLocalPlayers", true);
 		announcerUse = config.getBoolean("announcer.use", false);
 		announcerInterval = config.getInt("announcer.interval", 15);
 		announcements = config.getStringList("announcer.annoucements");
 	}
 
 	private void boundsCheck()
 	{
 		if (announcerInterval <= 0)
 		{
 			announcerInterval = 15;
 		}
 		if (announcements == null)
 		{
 			announcements = new ArrayList<String>();
 		}
 		broadcastWorlds = config.getStringList("broadcastToWorlds");
 		if (broadcastWorlds == null)
 		{
 			broadcastWorlds = new ArrayList<String>();
 		}
 	}
 
 	private void startAnnouncer()
 	{
 		if (announcerId != -1)
 		{
 			// Stop previous announcer
 			plugin.getServer().getScheduler().cancelTask(announcerId);
 		}
 		if (!announcerUse || announcements.isEmpty())
 		{
 			return;
 		}
 		announcerId = plugin
 				.getServer()
 				.getScheduler()
 				.scheduleSyncRepeatingTask(plugin,
 						new WorldAnnouncerTask(worldName, announcements), 0,
 						announcerInterval * minutesToTicks);
 	}
 
 	public List<String> getWorldList()
 	{
 		return broadcastWorlds;
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
