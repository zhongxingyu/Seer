 package com.legit2.Demigods.Utilities;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 import com.legit2.Demigods.DDatabase;
 
 public class DPlayerUtil
 {
 	/*
 	 *  createPlayer() : Adds (Player)player to the database.
 	 */
 	public static boolean createNewPlayer(Player player)
 	{
 		// First check to see if the player exists
 		if(!DDataUtil.newPlayer(player))
 		{
 			DUtil.info(player.getName() + " already exists in the database.");
 			return false;
 		}
 		
 		DUtil.info("Adding new player (" + player.getName() + ") to the database.");
 		
 		// Define variables
 		int playerID = DObjUtil.generateInt(5);
 
 		// Create their HashMap data
 		DDataUtil.addPlayer(player, playerID);
 		DDataUtil.savePlayerData(player, "player_id", playerID);
 		DDataUtil.savePlayerData(player, "player_kills", 0);
 		DDataUtil.savePlayerData(player, "player_deaths", 0);
 		DDataUtil.savePlayerData(player, "player_characters", null);
 		
 		// Add them to the database
 		try
 		{
 			DDatabase.addPlayerToDB(player);
 		}
 		catch(SQLException e)
 		{
 			player.kickPlayer("Please contact an admin and give them this error code: " + ChatColor.RED + "2001");
 		}
 		
 		return true;
 	}
 	
 	/*
 	 *  getPlayerID() : Returns the ID of (Player)player.
 	 */
 	public static int getPlayerID(OfflinePlayer player)
 	{
 		return DObjUtil.toInteger(DDataUtil.getPlayerData(player, "player_id"));
 	}
 	
 	/*
 	 *  getPlayerFromID() : Returns the (Player)player for (int)player_id.
 	 */
 	public static Player getPlayerFromID(int player_id)
 	{
 		return null;
 	}
 	
 	/*
 	 *  definePlayer() : Defines a Player from sender.
 	 */
 	public static OfflinePlayer definePlayer(String name)
 	{
 		return Bukkit.getOfflinePlayer(name);
 	}
 	
 	/*
 	 *  getCurrentChar() : Returns the current charID for (Player)player.
 	 */
 	public static int getCurrentChar(OfflinePlayer player)
 	{
		try
		{
			return DObjUtil.toInteger(DDataUtil.getPlayerData(player, "current_char").toString());
		}
		catch (Exception e)
		{
			return -1;
		}
 	}
 	
 	/*
 	 *  getCurrentAlliance() : Returns the current alliance for (Player)player.
 	 */
 	public static String getCurrentAlliance(OfflinePlayer player)
 	{
 		return DDataUtil.getCharData(player, getCurrentChar(player), "alliance").toString();
 	}
 	
 	/*
 	 *  getChars() : Returns an ArrayList of (Player)player's characters.
 	 */
 	public static ArrayList<String> getChars(OfflinePlayer player)
 	{
 		if(DDataUtil.getPlayerData(player, "player_characters") != null) 
 		{
 			String playerChars = (String) DDataUtil.getPlayerData(player, "player_characters");
 			ArrayList<String> chars = new ArrayList<String>(Arrays.asList(playerChars.split(",")));
 			
 			return chars;
 		}
 		else return null;
 	}
 	
 	/*
 	 *  hasChar() : Checks to see if (Player)player has (int)charID.
 	 */
 	public static boolean hasChar(OfflinePlayer player, int charID)
 	{
 		if(getChars(player) != null && getChars(player).contains(charID)) return true;
 		return false;
 	}
 	
 	/*
      *  regenerateAllFavor() : Regenerates favor for every player based on their stats.
      */
 	public static void regenerateAllFavor()
 	{
 		Player[] onlinePlayers = DUtil.getOnlinePlayers();
 		
 		for(Player player : onlinePlayers)
 		{
 			int charID = DPlayerUtil.getCurrentChar(player);
 			int regenRate = DCharUtil.getAscensions(player, charID);
 			if (regenRate < 1) regenRate = 1;
 			DCharUtil.giveFavor(player, charID, regenRate);
 		}
 	}
 	
 	/*
 	 *  getNumberOfSouls() : Returns the number of souls (Player)player has in their inventory.
 	 *
 	public static int getNumberOfSouls(OfflinePlayer player)
 	{
 		// Define inventory contents & other variables
 		ItemStack[] inventory = player.getInventory().getContents();
 		ArrayList<ItemStack> allSouls = DSouls.returnAllSouls();
 		int numberOfSouls = 0;
 		
 		for(ItemStack soul : allSouls)
 		{
 			for(ItemStack inventoryItem : inventory)
 			{
 				if(inventoryItem != null && inventoryItem.isSimilar(soul))
 				{
 					// Find amount of souls and subtract 1 upon use
 					int amount = inventoryItem.getAmount();
 					
 					numberOfSouls = numberOfSouls + amount;
 				}
 			}
 		}
 		return numberOfSouls;
 	}
 
 	
 	/*
 	 *  useSoul() : Uses first soul found in (Player)player's inventory.
 	 *
 	public static ItemStack useSoul(OfflinePlayer player)
 	{	
 		if(getNumberOfSouls(player) == 0) return null;
 		// Define inventory contents
 		ItemStack[] inventory = player.getInventory().getContents();
 		ArrayList<ItemStack> allSouls = DSouls.returnAllSouls();
 		
 		for(ItemStack soul : allSouls)
 		{
 			for(ItemStack inventoryItem : inventory)
 			{
 				if(inventoryItem != null && inventoryItem.isSimilar(soul))
 				{
 					// Find amount of souls and subtract 1 upon use
 					int amount = inventoryItem.getAmount();
 					player.getInventory().removeItem(inventoryItem);
 					inventoryItem.setAmount(amount - 1);
 					player.getInventory().addItem(inventoryItem);
 					
 					return inventoryItem;
 				}
 			}
 		}
 		return null;
 	}
 	*/
 	
 	/*
 	 *  getKills() : Returns (int)kills for (Player)player.
 	 */
 	public static int getKills(OfflinePlayer player)
 	{
 		if(DDataUtil.getPlayerData(player, "kills") != null) return Integer.parseInt(DDataUtil.getPlayerData(player, "kills").toString());
 		return -1;
 	}
 	
 	/*
 	 *  setKills() : Sets the (Player)player's kills to (int)amount.
 	 */
 	public static void setKills(OfflinePlayer player, int amount)
 	{
 		DDataUtil.savePlayerData(player, "kills", amount);
 	}
 	
 	/*
 	 *  addKill() : Gives (Player)player 1 kill.
 	 */
 	public static void addKill(OfflinePlayer player)
 	{
 		DDataUtil.savePlayerData(player, "kills", getKills(player) + 1);
 	}
 	
 	/*
 	 *  getDeaths() : Returns (int)deaths for (Player)player.
 	 */
 	public static int getDeaths(OfflinePlayer player)
 	{
 		if(DDataUtil.getPlayerData(player, "deaths") != null) return Integer.parseInt(DDataUtil.getPlayerData(player, "deaths").toString());
 		return -1;
 	}
 	
 	/*
 	 *  setDeaths() : Sets the (Player)player's deaths to (int)amount.
 	 */
 	public static void setDeaths(OfflinePlayer player, int amount)
 	{
 		DDataUtil.savePlayerData(player, "deaths", amount);
 	}
 	
 	/*
 	 *  addDeath() : Gives (Player)player 1 death.
 	 */
 	public static void addDeath(OfflinePlayer player)
 	{
 		DDataUtil.savePlayerData(player, "deaths", getDeaths(player) + 1);
 	}
 }
