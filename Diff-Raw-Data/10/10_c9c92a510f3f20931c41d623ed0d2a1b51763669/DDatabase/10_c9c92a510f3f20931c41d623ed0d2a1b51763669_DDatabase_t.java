 package com.legit2.Demigods;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 import com.google.common.base.Joiner;
 
 public class DDatabase
 {
 	/*
 	 *  initializeDatabase() : Loads the MySQL or SQLite database.
 	 */
 	public static void initializeDatabase()
 	{
 		// Check if MySQL is enabled in the configuration and if so, attempts to connect.
 		if(DConfig.getSettingBoolean("mysql"))
 		{
 			DMySQL.createConnection();
 			DMySQL.initializeMySQL();
 			loadAllData();
 			
 			// Save data just to be safe...
 			saveAllData();
 		}
 		else if(DConfig.getSettingBoolean("sqlite"))
 		{
 			// TODO: SQLite
 		}
 	}
 
 	/*
 	 *  uninitializeDatabase() : Unloads the MySQL or SQLite database.
 	 */
 	public static void uninitializeDatabase()
 	{
 		saveAllData();
 		
 		if(DConfig.getSettingBoolean("mysql") && DMySQL.checkConnection())
 		{
 			DMySQL.uninitializeMySQL();
 		}
 		else if(DConfig.getSettingBoolean("sqlite"))
 		{
 			// TODO: SQLite
 		}
 	}
 
 	/*
 	 *  addPlayer() : Adds the (String)username to the database with default values.
 	 */
 	public static void addPlayer(String username) throws SQLException
 	{
 		// First we save the player to a HashMap
 		DSave.newPlayer(username);
 
 		// Next we add them to the Database if needed
 		if(DConfig.getSettingBoolean("mysql") && DMySQL.checkConnection())
 		{	
 			if(!DMySQL.dataExists(DMySQL.player_table, "player", username))
 			{
 				// Setup query string to add player to MySQL database
 				String addPlayerQuery = "INSERT INTO " + DMySQL.player_table + " (player, alliance, deities, favor, ascensions, kills, deaths) VALUES ('" + username + "', NULL, NULL, 100, 1, 0, 0);";
 				DMySQL.runQuery(addPlayerQuery);
 				DUtil.info("User \"" + username + "\" added to database!");
 			}
 			else
 			{
 				DUtil.info("User " + username + " has already been added.");
 			}
 		}
 		else if(DConfig.getSettingBoolean("sqlite"))
 		{
 			// TODO: SQLite
 		}
 	}
 	
 	/*
 	 *  addPlayer() : Adds the (String)username to the database with default values.
 	 */
 	public static void removePlayer(String username) throws SQLException
 	{
 		// Next we add them to the Database if needed
 		if(DConfig.getSettingBoolean("mysql") && DMySQL.checkConnection())
 		{	
 			// Remove their HashMap data first
 			DSave.removeAllPlayerData(username);
 			DSave.removeAllDeityData(username, "ALL");
 			
 			// Remove their MySQL info
			if(DMySQL.dataExists(DMySQL.player_table, "player", username))
 			{
 				DMySQL.runQuery("DELETE FROM " + DMySQL.player_table + " WHERE player='" + username + "';");
 				DMySQL.runQuery("DELETE FROM " + DMySQL.playerdata_table + " WHERE player='" + username + "';");
 				DMySQL.runQuery("DELETE FROM " + DMySQL.deitydata_table + " WHERE player='" + username + "';");
 			}
 			else
 			{
 				DUtil.info("User " + username + " could not be found.");
 			}
 		}
 		else if(DConfig.getSettingBoolean("sqlite"))
 		{
 			// TODO: SQLite
 		}
 	}
 
 	/*
 	 *  getPlayerInfo() : Grabs the player info from MySQL/FlatFile and returns (ResultSet)result.
 	 */
 	public static ResultSet getPlayerInfo(String username) throws SQLException
 	{
 		if(DConfig.getSettingBoolean("mysql") && DMySQL.checkConnection())
 		{
 			// Check to see if user exists
 			String query = "SELECT * FROM " + DMySQL.player_table + " WHERE player = '" + username + "';";
 			ResultSet result = null;
 
 			result = DMySQL.runQuery(query);
 
 			if(result.next()) return result;
 			else
 			{
 				DUtil.severe(username + " tried to check their info but they aren't entered in the database!");
 			}
 		}
 		else if(DConfig.getSettingBoolean("sqlite"))
 		{
 			// TODO: SQLite
 		}
 
 		return null;
 	}
 
 	/*
 	 *  saveAllData() : Saves all HashMap data to database.
 	 */
 	public static boolean saveAllData()
 	{
 		if(DConfig.getSettingBoolean("mysql") && DMySQL.checkConnection())
 		{
 			DUtil.info("Saving player data...");
 			
 			// Define variables
 			int playerCount = 0;
 			long startTimer = System.currentTimeMillis();
 			HashMap<String, Object> player_data = null;
 			HashMap<String, HashMap<String, Object>> player_deities = null;
 			
 			for(Player player : DUtil.getOnlinePlayers())
 			{
 				// Define variables
 				String username = player.getName();
 				
 				// Remove all data for re-saving
 				DMySQL.runQuery("DELETE FROM " + DMySQL.deitydata_table + " WHERE player='" + username + "';");
 				DMySQL.runQuery("DELETE FROM " + DMySQL.playerdata_table + " WHERE player='" + username + "';");
 
 				if(DSave.getAllPlayerData(username) != null && DSave.getAllDeityData(username) != null)
 				{
 					player_data = DSave.getAllPlayerData(username);	
 					player_deities = DSave.getAllDeityData(username);	
 				}
 				else
 				{
 					try
 					{
 						// No HashMap data was found, let's create some...
 						addPlayer(username);
 						
 						player_data = DSave.getAllPlayerData(username);	
 						player_deities = DSave.getAllDeityData(username);	
 					}
 					catch(SQLException e)
 					{
						DUtil.plugin.getPluginLoader().disablePlugin(DUtil.plugin);
 						DUtil.severe("There was a severe problem with saving...");
						DUtil.severe("Demigods has been disabled. Please do a full restart of your Bukkit server.");
 					}
 				}
 				
 				// Add 1 to player count
 				playerCount++;
 				
 				// Loop through deity data entry set and add to database
 				for(Map.Entry<String, HashMap<String, Object>> deity : player_deities.entrySet())
 				{
 					String deity_name = deity.getKey();
 					HashMap<String, Object> deity_data = deity.getValue();
 					
 					for(Map.Entry<String, Object> entry : deity_data.entrySet())
 					{
 						String id = entry.getKey();
 						Object data = entry.getValue();
 						
 						// Don't save if it's temporary data
 						if(id.contains("temp")) continue;
 												
 						if(!id.equalsIgnoreCase("bindings"))
 						{
 							DMySQL.runQuery("INSERT INTO " + DMySQL.deitydata_table + " (player, deity, datakey, datavalue) VALUES ('" + username + "', '" + deity_name + "', '" + id + "', '" + data.toString() + "');");
 						}
 					}
 				}
 				
 				// Loop through player data entry set and add to database
 				for(Map.Entry<String, Object> entry : player_data.entrySet())
 				{
 					String id = entry.getKey();
 					Object data = entry.getValue();
 
 					// Don't save if it's temporary data
 					if(id.contains("temp")) continue;
 
 					// Don't save them to the data table if they belong in the user table
 					if(!id.equalsIgnoreCase("immortal") && !id.equalsIgnoreCase("favor") && !id.equalsIgnoreCase("alliance") && !id.equalsIgnoreCase("ascensions") && !id.equalsIgnoreCase("kills") && !id.equalsIgnoreCase("deaths") && !id.equalsIgnoreCase("deities"))
 					{						
 						String[] column = new String[2];
 						String[] values = new String[2];
 						column[0] = "player";
 						column[1] = "datakey";
 						values[0] = username;
 						values[1] = id;
 						
 						DMySQL.runQuery("INSERT INTO " + DMySQL.playerdata_table + " (player, datakey, datavalue) VALUES ('" + username + "', '" + id + "', " + data + ");");
 					}
 				}
 				
 				String deities = null;
 				if(DUtil.getDeities(username) != null) deities = Joiner.on(",").join(DUtil.getDeities(username));
 				// Save specific data to user table
 				DMySQL.runQuery("UPDATE " + DMySQL.player_table + " SET immortal=" + DUtil.isImmortal(username) + ", alliance='" + DUtil.getAlliance(username) + "', deities='" + deities + "', favor=" + DUtil.getFavor(username) + ", ascensions=" + DUtil.getAscensions(username) + ", kills=" + DUtil.getKills(username) + ", deaths=" + DUtil.getDeaths(username) + " WHERE player='" + username + "';");
 			}
 
 			// Stop the timer
 			long stopTimer = System.currentTimeMillis();
 			double totalTime = (double) (stopTimer - startTimer);
 
 			DUtil.info("Success! Saved " + playerCount + " of " + DMySQL.getRows(DMySQL.runQuery("SELECT * FROM " + DMySQL.player_table + ";")) + " total players in " + totalTime/1000 + " seconds.");
 			return true;
 		}
 		else if(DConfig.getSettingBoolean("sqlite"))
 		{
 			// TODO: SQLite
 		}
 		return false;
 	}
 	
 	/*
 	 *  loadAllData() : Loads all data from database into HashMaps.
 	 */
 	public static void loadAllData()
 	{
 		if(DConfig.getSettingBoolean("mysql") && DMySQL.checkConnection())
 		{	
 			DUtil.info("Loading player data...");
 
 			// Define variables
 			int playerCount = 0;
 			long startStopwatch = System.currentTimeMillis();
 
 			// Load data from player table
 			ResultSet all_players = DMySQL.runQuery("SELECT * FROM " + DMySQL.player_table + ";");
 			
 			try 
 			{
 				while(all_players.next())
 				{
 					playerCount++;
 
 					// Define variables
 					String username = all_players.getString("player");
 					ArrayList<String> deities = null;
					if(!all_players.getString("deities").isEmpty() && !all_players.getString("deities").equalsIgnoreCase("null")) deities = new ArrayList<String>(Arrays.asList(all_players.getString("deities").split(",")));
 
 					// Add HashMaps
 					DSave.newPlayer(username);
 					DUtil.setFavor(username, all_players.getInt("favor"));
 					DUtil.setAlliance(username, all_players.getString("alliance"));
 					DUtil.setAscensions(username, all_players.getInt("ascensions"));
 					DUtil.setKills(username, all_players.getInt("kills"));
 					DUtil.setDeaths(username, all_players.getInt("deaths"));
 					DUtil.setImmortal(username, all_players.getBoolean("immortal"));
 					
 					// Save stuff from ArrayLists
 					DSave.savePlayerData(username, "deities", deities);
 					
 					ResultSet player_deitydata = DMySQL.runQuery("SELECT * FROM " + DMySQL.deitydata_table + " WHERE player = '" + username + "';");
 					if(player_deitydata.next())
 					{
 						while(player_deitydata.next())
 						{
 							if(player_deitydata.getString("datakey").contains("_boolean"))
 							{
 								DSave.saveDeityData(username, player_deitydata.getString("deity"), player_deitydata.getString("datakey"), player_deitydata.getBoolean("datavalue"));
 							}
 							else
 							{
 								DSave.saveDeityData(username, player_deitydata.getString("deity"), player_deitydata.getString("datakey"), player_deitydata.getObject("datavalue"));
 							}
 						}
 					}
 				}
 			}
 			catch(SQLException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Load data from other tables
 			String selectAllPlayerData = "SELECT * FROM " + DMySQL.playerdata_table + ";";
 			ResultSet all_playerdata = DMySQL.runQuery(selectAllPlayerData);
 			String selectAllPlayerDeityData = "SELECT * FROM " + DMySQL.deitydata_table + ";";
 			ResultSet all_playerdeitydata = DMySQL.runQuery(selectAllPlayerDeityData);
 			try
 			{
 				while(all_playerdata.next())
 				{					
 					// Define variables
 					String username = all_playerdata.getString("player");
 										
 					// Save the data
 					DSave.savePlayerData(username, all_playerdata.getString("datakey"), all_playerdata.getObject("datavalue"));
 				}
 				
 				while(all_playerdeitydata.next())
 				{					
 					// Define variables
 					String id = all_playerdeitydata.getString("datakey");
 					Object data =  all_playerdeitydata.getObject("datavalue");
 					String username = all_playerdeitydata.getString("player");
 					String deity = all_playerdeitydata.getString("deity");
 					ArrayList<Material> bindings = new ArrayList<Material>();
 
 					if(all_playerdeitydata.getString("datakey").contains("_bind"))
 					{
 						String ability_bind = all_playerdeitydata.getString("datakey");
 						Material material = Material.getMaterial(all_playerdeitydata.getString("datavalue").toUpperCase());
 						
 						bindings.add(material);
 						DSave.saveDeityData(username, deity, "bindings", bindings);
 						DSave.saveDeityData(username, deity, ability_bind, material);
 						 
 					}
 					
 					// Save the data
 					if(!id.contains("bind")) DSave.saveDeityData(username, deity, id, data);
 				}
 			}
 			catch(SQLException e)
 			{
 				// TODO
 			}
 
 			long stopStopwatch = System.currentTimeMillis();
 			double totalTime = (double) (stopStopwatch - startStopwatch);
 
 			DUtil.info("Loaded data for " + playerCount + " players in " + totalTime/1000 + " seconds.");
 		}
 		else if(DConfig.getSettingBoolean("sqlite"))
 		{
 			// TODO: SQLite
 		}
 	}
 }
