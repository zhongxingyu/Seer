 package com.jackwilsdon.PvPoints;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.bukkit.configuration.MemorySection;
 
 /*
  * PvPointsPlayerManager
  * Player manager for PvPoints
  */
 public class PvPointsPlayerManager {
 	
 	/*
 	 * Variable for plugin
 	 */
 	private static PvPointsPlugin plugin;
 	
 	/*
 	 * PvPointsPlayerManager
 	 * Constructor
 	 */
 	PvPointsPlayerManager(PvPointsPlugin pl)
 	{
 		/*
 		 * Set plugin variable
 		 */
 		plugin = pl;
 	}
 	
 	/*
 	 * getInfo()
 	 * Returns all data about a player
 	 */
 	private static PvPointsPlayer getInfo(String username)
 	{
 		/*
 		 * Retrieve the player list
 		 */
		if (plugin.getConfig().getConfigurationSection("Players").getValues(true).size() == 0 || plugin.getConfig().getConfigurationSection("Players."+username).getValues(true).size() == 0)
 		{
 			return new PvPointsPlayer(-1, -1);
 		}
 		int kills = plugin.getConfig().getInt("Players."+username+".kills");
 		int deaths = plugin.getConfig().getInt("Players."+username+".deaths");
 		int points = plugin.getConfig().getInt("Players."+username+".points");
 		
 		/*
 		 * Create PvPlayer
 		 */
 		PvPointsPlayer currentPlayer = new PvPointsPlayer(kills, deaths, points);
 		
 		/*
 		 * Return the data
 		 */
 		return currentPlayer;
 	}
 	
 	/*
 	 * playerExists()
 	 * Returns whether a player is already in the system
 	 */
 	public static boolean playerExists(String username)
 	{
 		/*
 		 * Retrieve player
 		 */
 		PvPointsPlayer player = getInfo(username);
 		
 		/*
 		 * Return if the player exists
 		 */
 		return player.real;
 	}
 	
 	/*
 	 * getKills()
 	 * Gets the kills for a player
 	 */
 	public static int getKills(String username)
 	{
 		/*
 		 * Retrieve player
 		 */
 		PvPointsPlayer player = getInfo(username);
 		
 		/*
 		 * Return kills
 		 */
 		if (!player.real) return 0;
 		return player.kills;
 	}
 	
 	/*
 	 * getDeaths()
 	 * Gets the deaths for a player
 	 */
 	public static int getDeaths(String username)
 	{
 		/*
 		 * Retrieve player
 		 */
 		PvPointsPlayer player = getInfo(username);
 		
 		/*
 		 * Return deaths
 		 */
 		if (!player.real) return 0;
 		return player.deaths;
 	}
 	
 	/*
 	 * getPoints()
 	 * Works out how many points a player has, using the configuration
 	 */
 	public static int getPoints(String username)
 	{
 		return getInfo(username).points;
 	}
 	
 	/*
 	 * addKill()
 	 * Adds a kill to the player
 	 */
 	public static void addKill(String username)
 	{
 		/*
 		 * Define new kill count
 		 */
 		int kills = getKills(username)+1;
 		
 		/*
 		 * Ensure the player is real
 		 */
 		if (!playerExists(username)) return;
 		
 		/*
 		 * Set the kill count
 		 */
 		plugin.getConfig().set("Players."+username+".kills", kills);
 		
 		/*
 		 * Add kill point
 		 */
 		addKillPoint(username);
 		
 		/*
 		 * Save configuration
 		 */
 		plugin.saveConfig();
 	}
 	
 	/*
 	 * addDeath()
 	 * Adds a death to the player
 	 */
 	public static void addDeath(String username)
 	{
 		/*
 		 * Define new death count
 		 */
 		int deaths = getDeaths(username)+1;
 		
 		/*
 		 * Ensure the player is real
 		 */
 		if (!playerExists(username)) return;
 		
 		/*
 		 * Set the death count
 		 */
 		plugin.getConfig().set("Players."+username+".deaths", deaths);
 		
 		/*
 		 * Add death point
 		 */
 		addDeathPoint(username);
 		
 		/*
 		 * Save configuration
 		 */
 		plugin.saveConfig();
 	}
 	
 	/*
 	 * addKillPoint()
 	 * Adds a point for a kill
 	 */
 	private static void addKillPoint(String username)
 	{
 		/*
 		 * Get current points
 		 */
 		int points = getPoints(username);
 		
 		/*
 		 * Calculate new points
 		 */
 		int increment = plugin.getConfig().getInt("PvPoints.add-on-kill.amount");
 		if (!plugin.getConfig().getBoolean("PvPoints.add-on-kill.enabled"))
 		{
 			increment = 0;
 		}
 		
 		/*
 		 * Set points
 		 */
 		points += increment;
 		plugin.getConfig().set("Players."+username+".points", points);
 	}
 	
 	/*
 	 * addDeathPoint()
 	 * Subtracts a point for a death
 	 */
 	private static void addDeathPoint(String username)
 	{
 		/*
 		 * Get current points
 		 */
 		int points = getPoints(username);
 		
 		/*
 		 * Calculate new points
 		 */
 		int decrement = plugin.getConfig().getInt("PvPoints.subtract-on-death.amount");
 		if (!plugin.getConfig().getBoolean("PvPoints.subtract-on-death.enabled"))
 		{
 			decrement = 0;
 		}
 		
 		/*
 		 * Set points
 		 */
 		points -= decrement;
 		plugin.getConfig().set("Players."+username+".points", points);
 	}
 	
 	/*
 	 * reset()
 	 * Resets a user's kills and deaths
 	 */
 	public static void reset(String username)
 	{
 		/*
 		 * Clear kills
 		 */
 		plugin.getConfig().set("Players."+username+".kills", 0);
 		
 		/*
 		 * Clear deaths
 		 */
 		plugin.getConfig().set("Players."+username+".deaths", 0);
 		
 		/*
 		 * Set points back to default
 		 */
 		int points = plugin.getConfig().getInt("PvPoints.starting-points");
 		plugin.getConfig().set("Players."+username+".points", points);
 	}
 	
 	/*
 	 * getAllPlayers()
 	 * Return a list of all PvPointsPlayers
 	 */
 	public static Map<String, PvPointsPlayer> getAllPlayers()
 	{
 		/*
 		 * Create the list of players
 		 */
 		Map<String, PvPointsPlayer> playerList = new HashMap<String, PvPointsPlayer>();
 		
 		/*
 		 * Check there are some players
 		 */
 		if (plugin.getConfig().getConfigurationSection("Players") == null) return null;
 		
 		/*
 		 * Iterate through the players
 		 */
 		Map<String, Object> players = plugin.getConfig().getConfigurationSection("Players").getValues(true);
 		Iterator<Entry<String, Object>> it = players.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
 			if (!(pairs.getValue() instanceof MemorySection))
 			{
 				String[] key = pairs.getKey().toString().split("\\.");
 				int kills = -1;
 				int deaths = -1;
 				int points = -99999;
 				switch (key[1])
 				{
 				case "kills":
 					kills = Integer.parseInt(pairs.getValue().toString());
 					break;
 				case "deaths":
 					deaths = Integer.parseInt(pairs.getValue().toString());
 					break;
 				case "points":
 					points = Integer.parseInt(pairs.getValue().toString());
 					break;
 				}
 				if (playerList.get(key[0]) == null)
 				{
 					playerList.put(key[0], new PvPointsPlayer(kills, deaths, points));
 				} else {
 					PvPointsPlayer pV = playerList.get(key[0]);
 					if (kills != -1)
 					{
 						pV.kills = kills;
 					}
 					if (deaths != -1)
 					{
 						pV.deaths = deaths;
 					}
 					if (points != -99999);
 					{
 						pV.points = points;
 					}
 				}
 			}
 			it.remove();
 		}
 		
 		/*
 		 * Return the players
 		 */
 		return playerList;
 	}
 }
