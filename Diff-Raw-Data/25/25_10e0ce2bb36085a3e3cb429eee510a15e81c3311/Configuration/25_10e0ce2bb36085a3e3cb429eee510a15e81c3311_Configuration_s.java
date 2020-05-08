 /*
  * Copyright (c) 2012 Sean Porter <glitchkey@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.enderspawn;
 
 //* IMPORTS: JDK/JRE
 	import java.io.File;
 	import java.lang.Integer;
 	import java.lang.Long;
 	import java.lang.String;
 	import java.sql.Timestamp;
 	import java.util.Date;
 	import java.util.HashMap;
 	import java.util.List;
 	import java.util.logging.Logger;
 	import java.util.Map;
 	import java.util.Set;
 //* IMPORTS: BUKKIT
 	import org.bukkit.configuration.ConfigurationSection;
 	import org.bukkit.configuration.file.YamlConfiguration;
 	import org.bukkit.World;
 	import org.bukkit.World.Environment;
 //* IMPORTS: SPOUT
 	//* NOT NEEDED
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class Configuration extends YamlConfiguration
 {
 	private	File config;
 	private Logger log;
 	private EnderSpawn plugin;
 
 	public	HashMap<String, Integer>	worlds;
 	public	HashMap<String, Integer> 	xCoords;
 	public	HashMap<String, Integer> 	yCoords;
 	public	HashMap<String, Integer> 	zCoords;
 
 	public	boolean	destroyBlocks;
 	public	boolean	spawnEgg;
 	public	boolean	spawnPortal;
 	public	boolean teleportEgg;
 	public	boolean	useCustomExp;
 	public	long	maxSpawnMinutes;
 	public	long	minSpawnMinutes;
 	public	long	expResetMinutes;
 	public	long	expMaxDistance;
 	public	int	customExp;
 
 	public Configuration(File config, Logger log, EnderSpawn plugin)
 	{
 		this.config	= config;
 		this.log	= log;
 		this.plugin	= plugin;
 
 		worlds		= new HashMap<String, Integer>();
 		xCoords		= new HashMap<String, Integer>();
 		yCoords		= new HashMap<String, Integer>();
 		zCoords		= new HashMap<String, Integer>();
 
 		destroyBlocks	= false;
 		spawnEgg	= true;
 		spawnPortal	= false;
 		teleportEgg	= false;
 		useCustomExp	= false;
 		maxSpawnMinutes	= 5;
 		minSpawnMinutes = 5;
 		expResetMinutes	= 1200;
 		expMaxDistance	= 75;
 		customExp	= 20000;
 	}
 
 	public void load()
 	{
 		try
 		{
 			super.load(config);
 		}
 		catch(Exception e)
 		{
 			log.warning("Unable to load configuration, using defaults instead.");
 		}
 
 		if(contains("Configuration"))
 		{
 			loadLegacy();
 			return;
 		}
 
 		destroyBlocks	= getBoolean("DestroyBlocks",		destroyBlocks);
 		spawnEgg	= getBoolean("SpawnEgg",		spawnEgg);
 		spawnPortal	= getBoolean("SpawnPortal",		spawnPortal);
 		teleportEgg	= getBoolean("EggsCanTeleport",		teleportEgg);
 		maxSpawnMinutes	= getLong("MaxRespawnMinutes",		maxSpawnMinutes);
 		minSpawnMinutes	= getLong("MinRespawnMinutes",		minSpawnMinutes);
 		expResetMinutes	= getLong("EXPResetMinutes",		expResetMinutes);
 		expMaxDistance	= getLong("EXPMaxDistance",		expMaxDistance);
 		useCustomExp	= getBoolean("UseCustomEXPTotal",	useCustomExp);
 		customExp	= getInt("CustomEXPTotal",		customExp);
 
 		getWorlds();
 	}
 
 	public void loadLegacy()
 	{
 		log.info("Converting configuration to the current format.");
 		destroyBlocks	= getBoolean("Configuration.DestroyBlocks",	destroyBlocks);
 		spawnEgg	= getBoolean("Configuration.SpawnEgg",		spawnEgg);
 		spawnPortal	= getBoolean("Configuration.SpawnPortal",	spawnPortal);
 		teleportEgg	= getBoolean("Configuration.EggsCanTeleport",	teleportEgg);
 		maxSpawnMinutes	= getLong("Configuration.MaxRespawnMinutes",	maxSpawnMinutes);
 		minSpawnMinutes	= getLong("Configuration.MinRespawnMinutes",	minSpawnMinutes);
 		expResetMinutes	= getLong("Configuration.EXPResetMinutes",	expResetMinutes);
 		expMaxDistance	= getLong("Configuration.EXPMaxDistance",	expMaxDistance);
 		useCustomExp	= getBoolean("Configuration.UseCustomEXPTotal",	useCustomExp);
 		customExp	= getInt("Configuration.CustomEXPTotal",	customExp);
 
 		addWorlds();
 		getPlayers();
 		getBannedPlayers();
 		save();
 	}
 
 	public void save()
 	{
 		YamlConfiguration newConfig = new YamlConfiguration();
 
 		newConfig.set("DestroyBlocks",		destroyBlocks);
 		newConfig.set("SpawnEgg",		spawnEgg);
 		newConfig.set("SpawnPortal",		spawnPortal);
 		newConfig.set("EggsCanTeleport",	teleportEgg);
 		newConfig.set("MaxRespawnMinutes",	maxSpawnMinutes);
 		newConfig.set("MinRespawnMinutes",	minSpawnMinutes);
 		newConfig.set("EXPResetMinutes",	expResetMinutes);
 		newConfig.set("EXPMaxDistance",		expMaxDistance);
 		newConfig.set("UseCustomEXPTotal",	useCustomExp);
 		newConfig.set("CustomEXPTotal",		customExp);
 
 		ConfigurationSection worldSection = newConfig.createSection("Worlds");
 
 		for(String key : worlds.keySet())
 		{
 			if(key == null)
 				continue;
 
 			if(!worlds.containsKey(key))
 				continue;
 
 			if(!xCoords.containsKey(key))
 				continue;
 
 			if(!yCoords.containsKey(key))
 				continue;
 
 			if(!zCoords.containsKey(key))
 				continue;
 
 			ConfigurationSection world = worldSection.createSection(key);
 			world.set("MaxDragons", worlds.get(key));
 
 			ConfigurationSection spawn = world.createSection("SpawnPoint");
 			spawn.set("X", xCoords.get(key));
 			spawn.set("Y", yCoords.get(key));
 			spawn.set("Z", zCoords.get(key));
 		}
 
 		File configurationFile = new File(plugin.getDataFolder(), "config.yml");
 
 		try
 		{
 			newConfig.save(configurationFile);
 		}
 		catch(Exception e)
 		{
 			log.warning("Unable to save configuration.");
 		}
 	}
 
 	public void getPlayers()
 	{
 		Timestamp currentTime = new Timestamp(new Date().getTime());
 		ConfigurationSection playerSection = getConfigurationSection("Players");
 
 		if(playerSection == null)
 			return;
 
 		Map<String, Object> playerValues = playerSection.getValues(false);
 
 		if(playerValues.isEmpty())
 			return;
 
 		for(Object key : playerValues.keySet())
 		{
 			if(!(key instanceof String))
 				continue;
 
 			String player = (String) key;
 			if(!playerValues.containsKey(player))
 				continue;
 
 			Object tempLong = playerValues.get(player);
 			if(!(tempLong instanceof Long))
 				continue;
 
 			Timestamp time = new Timestamp((Long) tempLong);
 			player = player.toUpperCase().toLowerCase();
 
 			if(currentTime.getTime() >= (time.getTime() + (expResetMinutes * 60000)))
 				continue;
 
 			this.plugin.data.players.put(player, time);
 		}
 	}
 
 	public void getBannedPlayers()
 	{
 		String name = "BannedPlayers";
 		ConfigurationSection playerSection = getConfigurationSection(name);
 
 		if(playerSection == null)
 			return;
 
 		Map<String, Object> playerValues = playerSection.getValues(false);
 
 		if(playerValues.isEmpty())
 			return;
 
 		for(Object key : playerValues.keySet())
 		{
 			if(!(key instanceof String))
 				continue;
 
 			String player = (String) key;
 			if(!playerValues.containsKey(player))
 				continue;
 
 			Object tempString = playerValues.get(player);
 			if(!(tempString instanceof String))
 				continue;
 
 			String banReason = (String) tempString;
 			player = player.toUpperCase().toLowerCase();
 
 			this.plugin.data.bannedPlayers.put(player, banReason);
 		}
 	}
 
 	public void getWorlds()
 	{
 		ConfigurationSection worldSection = getConfigurationSection("Worlds");
 
 		if(worldSection == null)
 			return;
 
 		Map<String, Object> worldValues = worldSection.getValues(false);
 
 		if(worldValues.isEmpty())
 			return;
 
 		for(Object key : worldValues.keySet())
 		{
 			if(!(key instanceof String))
 				continue;
 
 			String world	= (String) key;
 			String name	= world.toUpperCase().toLowerCase();
 			if(!worldValues.containsKey(world))
 				continue;
 
 			Object tempObject = worldValues.get(world);
 			if(!(tempObject instanceof ConfigurationSection))
 				continue;
 
 			ConfigurationSection section = (ConfigurationSection) tempObject;
 
 			worlds.put(name, section.getInt("MaxDragons", 1));
 			xCoords.put(name, section.getInt("X", 0));
 			yCoords.put(name, section.getInt("Y", 128));
 			zCoords.put(name, section.getInt("Z", 0));
 		}
 	}
 
 	public void addWorlds()
 	{
 	
 		List<World> worldList = plugin.getServer().getWorlds();
 		for (World world : worldList)
 		{
 			if(world.getEnvironment() != World.Environment.valueOf("THE_END"))
 				continue;
 
 			String name = world.getName().toUpperCase().toLowerCase();
 			worlds.put(name, getInt("Configuration.MaxDragons", 1));
 			xCoords.put(name, 0);
 			yCoords.put(name, 128);
 			zCoords.put(name, 0);
 
 			long deathLong = getLong("LastDeath", 0);
 			this.plugin.data.lastDeath.put(name, new Timestamp(deathLong));
 		}
 	}
 }
