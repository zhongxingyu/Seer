 package net.rageland.ragemod.dbqueries;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.rageland.ragemod.RageConfig;
 import net.rageland.ragemod.RageMod;
 import net.rageland.ragemod.data.Location2D;
 import net.rageland.ragemod.data.NPCTown;
 import net.rageland.ragemod.data.PlayerData;
 import net.rageland.ragemod.data.PlayerTown;
 import net.rageland.ragemod.data.Towns;
 import net.rageland.ragemod.data.Players;
 import net.rageland.ragemod.database.RageDB;
 
 public class TownQueries {
 	
 	private RageDB rageDB;
 	private RageMod plugin;
 	
 	public TownQueries(RageDB rageDB, RageMod plugin)
 	{
 		this.rageDB = rageDB;
 		this.plugin = plugin;
 	}
 	
 	// Load all PlayerTown data
 	public HashMap<String, PlayerTown> loadPlayerTowns()
     {
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 	    HashMap<String, PlayerTown> towns = new HashMap<String, PlayerTown>();
 	    
 		PlayerTown currentTown = null;
 		
     	try
     	{
     		conn = rageDB.getConnection();
         	preparedStatement = conn.prepareStatement(
 				"SELECT pt.ID_PlayerTown, pt.TownName, pt.XCoord, pt.YCoord, pt.ZCoord, " +
 				"	pt.TownLevel, " +
 				"	IFNULL(f.ID_Faction, 0) as ID_Faction, pt.TreasuryBalance, pt.MinimumBalance, pt.BankruptDate, p.Name AS Mayor " +
 				"FROM PlayerTowns pt " +
 				"LEFT JOIN Factions f ON pt.ID_Faction = f.ID_Faction " +
 				"INNER JOIN Players p ON pt.ID_PlayerTown = p.ID_PlayerTown " +
 				"WHERE p.IsMayor = 1 AND pt.IsDeleted = 0");
         	
         	rs = preparedStatement.executeQuery();
         	
         	while ( rs.next() ) 
         	{	        		
         		currentTown = new PlayerTown(plugin, rs.getInt("ID_PlayerTown"), rs.getString("TownName"), plugin.getServer().getWorld("world"));
         		currentTown.centerPoint = new Location(plugin.getServer().getWorld("world"), rs.getInt("XCoord"), rs.getInt("YCoord"), rs.getInt("ZCoord"));
         		currentTown.id_Faction = rs.getInt("ID_Faction");
         		currentTown.treasuryBalance = rs.getFloat("TreasuryBalance");
         		currentTown.minimumBalance = rs.getFloat("MinimumBalance");
         		currentTown.bankruptDate = rs.getTimestamp("BankruptDate");
         		currentTown.townLevel = plugin.config.townLevels.get(rs.getInt("TownLevel"));
         		currentTown.mayor = rs.getString("Mayor");
         		currentTown.residents = getTownResidents(currentTown.getID());
         		
         		currentTown.createRegions();	 
         		
         		if(currentTown.getName() != null)
         			towns.put(currentTown.getName().toLowerCase(), currentTown);	 
         		else
         			System.out.println("Town name was null for " + rs.getInt("ID_PlayerTown") + " in loadPlayerTowns()");
         	}			
         		        	
     	} catch (Exception e) {
     		System.out.println("Error in TownQueries.LoadPlayerTowns: " + e.getMessage());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
     	
    	return towns;
     }
 	
 	// Adds a new resident to the town
 	public void townAdd(String targetPlayerName, String townName)
     {
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 		PlayerData playerData = plugin.players.get(targetPlayerName);
 		
     	try
     	{
     		conn = rageDB.getConnection();
     		// Update the Players table to create the town association
     		preparedStatement = conn.prepareStatement(
     				"UPDATE Players SET ID_PlayerTown = " +
     				"	(SELECT ID_PlayerTown FROM PlayerTowns WHERE TownName = '" + townName + "') " +
     				", IsMayor = 0 WHERE ID_Player = " + playerData.id_Player);
     		preparedStatement.executeUpdate();	
         		        		        	
     	} catch (SQLException e) {
     		System.out.println("Error in TownQueries.TownAdd(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
     }
 	
 	// Creates a new town
 	public int townCreate(Player player, String townName)
     {
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 		PlayerData playerData = plugin.players.get(player.getName());
 		
     	try
     	{    		
     		conn = rageDB.getConnection();
     		// Insert the new town into the PlayerTowns table
     		preparedStatement = conn.prepareStatement(
     				"INSERT INTO PlayerTowns (TownName, XCoord, YCoord, ZCoord, ID_Faction, TreasuryBalance, MinimumBalance, TownLevel, DateCreated) " +
     				"VALUES ('" + townName + "', " + (int)player.getLocation().getX() + ", " + (int)player.getLocation().getY() + ", " + 
     						(int)player.getLocation().getZ() + ", " +  
     				"(SELECT ID_Faction FROM Players WHERE ID_Player = " + playerData.id_Player + "), " + 
     				plugin.config.townLevels.get(1).minimumBalance + ", " + plugin.config.townLevels.get(1).minimumBalance + ", 1, NOW())",
     				Statement.RETURN_GENERATED_KEYS);   
     		preparedStatement.executeUpdate();
     		
     		// Retrieve the new auto-increment town ID 
     		rs = preparedStatement.getGeneratedKeys();
     		rs.next();
     		int townID = rs.getInt(1);
     		
     		// Update the Players table
     		preparedStatement = conn.prepareStatement(
     				"UPDATE Players SET ID_PlayerTown = " + townID + ", IsMayor = 1 " +
     				"WHERE ID_Player = " + playerData.id_Player);        		
     		preparedStatement.executeUpdate();
     		
     		return townID;
         		        		        	
     	} catch (SQLException e) {
     		System.out.println("Error in TownQueries.TownCreate(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
     	
     	System.out.println("Error: RageDB.TownCreate() returned -1");
     	return -1;
     }
 	
 	// TODO: Remove this, update() will make it redundant
 	// Reset the player's town affiliation - used by both Leave and Evict
 	public void townLeave(String playerName)
     { 
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 		PlayerData playerData = plugin.players.get(playerName);
 		
     	try
     	{
     		conn = rageDB.getConnection();
     		// Update the Players table to remove the town association
     		preparedStatement = conn.prepareStatement(
     				"UPDATE Players SET ID_PlayerTown = NULL, IsMayor = 0, Spawn_XCoord = NULL WHERE ID_Player = " + playerData.id_Player);
     		preparedStatement.executeUpdate();	
     	} 
     	catch (SQLException e) {
     		System.out.println("Error in TownQueries.TownLeave(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
     }
 	
 	// Increments the TownLevel value by 1
 	public void townUpgrade(String townName, int cost) 
 	{    
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
     	try
     	{
     		conn = rageDB.getConnection();
     		// Update the Players table to remove the town association
     		preparedStatement = conn.prepareStatement(
     				"UPDATE PlayerTowns SET TownLevel = (TownLevel + 1), TreasuryBalance = (TreasuryBalance - " + cost + ") " +
     				"WHERE TownName = '" + townName + "'" +
     				"");
     		preparedStatement.executeUpdate();	
     	} 
     	catch (SQLException e) {
     		System.out.println("Error in TownQueries.TownUpgrade(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
 	}
 	
 	// Returns the number of players associated to the specified town
 	public int countResidents(String townName) 
 	{
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null; 
 		PlayerTown playerTown = (PlayerTown)plugin.towns.get(townName);
 		
     	try
     	{
     		conn = rageDB.getConnection();
     		preparedStatement = conn.prepareStatement(
     				"SELECT COUNT(*) FROM Players WHERE ID_PlayerTown = " + playerTown.getID());
     		rs = preparedStatement.executeQuery();
     		rs.next();
     		return rs.getInt(1);
     	} 
     	catch (SQLException e) {
     		System.out.println("Error in TownQueries.CountResidents(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
     	
     	System.out.println("Error: RageDB.CountResidents() returned -1");
     	return -1;
 	}
 	
 	// Returns all residents for a particular town, with mayor first
 	public ArrayList<String> getTownResidents(int id_PlayerTown) 
 	{
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 	    
 		ArrayList<String> residents = new ArrayList<String>();
 		
     	try
     	{
     		conn = rageDB.getConnection();
     		preparedStatement = conn.prepareStatement(
     				"SELECT Name FROM Players p " +
     				"WHERE ID_PlayerTown = " + id_PlayerTown + " " +
     				"ORDER BY IsMayor DESC ");
     		rs = preparedStatement.executeQuery();
         	
         	while ( rs.next() ) 
         	{	        	
         		residents.add(rs.getString("Name"));	        		
         	}
         		
         		
     	} 
     	catch (SQLException e) {
     		System.out.println("Error in TownQueries.ListTownResidents(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		    return residents;
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 			
 		}    
 		return residents;
 	}
 	
 	// Add money to town treasury
 	public void townDeposit(int id_PlayerTown, int id_Player, double amount) 
 	{
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 	    
 		try
     	{
 			conn = rageDB.getConnection();
     		// Update the treasury balance
     		preparedStatement = conn.prepareStatement(
     				"UPDATE PlayerTowns SET TreasuryBalance = (TreasuryBalance + " + amount + ") WHERE ID_PlayerTown = " + id_PlayerTown);
     		preparedStatement.executeUpdate();	
     		
     		// Record the player's deposit
     		preparedStatement = conn.prepareStatement(
     				"INSERT INTO TreasuryTransactions (ID_PlayerTown, ID_Player, Amount, Timestamp) VALUES (" +
     				id_PlayerTown + ", " + id_Player + ", " + amount + ", NOW() )");
     		preparedStatement.executeUpdate();	
     	} 
     	catch (SQLException e) {
     		System.out.println("Error in TownQueries.townDeposit(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
 		
 	}
 	
 	// Set minimum balance
 	public void townSetMinimumBalance(int id_PlayerTown, double amount) 
 	{
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 		try
     	{
 			conn = rageDB.getConnection();
     		// Update the treasury balance
     		preparedStatement = conn.prepareStatement(
     				"UPDATE PlayerTowns SET MinimumBalance = " + amount + " WHERE ID_PlayerTown = " + id_PlayerTown);
     		preparedStatement.executeUpdate();	
     	} 
     	catch (SQLException e) {
     		System.out.println("Error in TownQueries.townSetMinimumBalance(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
 		
 	}
 
 	// Updates all of the town's information in the database
 	public void update(PlayerTown playerTown) 
 	{
 		Connection conn = null;
 	    PreparedStatement preparedStatement = null;
 	    ResultSet rs = null;
 		try
     	{
 			conn = rageDB.getConnection();
     		// Update all town info
     		preparedStatement = conn.prepareStatement(
     				"UPDATE PlayerTowns SET " +
     				"TreasuryBalance = " + playerTown.treasuryBalance + ", " + 
     				"MinimumBalance = " + playerTown.minimumBalance + ", " + 
     				"BankruptDate = " + (playerTown.bankruptDate == null ? "null" : "'" + playerTown.bankruptDate + "'") + ", " +
     				"IsDeleted = " + playerTown.isDeleted + " " + 
     				"WHERE ID_PlayerTown = " + playerTown.getID());
     		preparedStatement.executeUpdate();	
     	} 
     	catch (SQLException e) {
     		System.out.println("Error in TownQueries.update(): " + e.getMessage());
 		    System.out.println("SQLState: " + e.getSQLState());
 		    System.out.println("VendorError: " + e.getErrorCode());
 		} finally {
 			rageDB.close(rs, preparedStatement, conn);
 		}
 		
 	}
 
 
 
 }
