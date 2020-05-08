 package me.raniy.plugins.UserHistory;
 
 // First run at a Player Listener
 import java.sql.Timestamp;
 import java.util.logging.Logger;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import org.bukkit.ChatColor;
 
 @SuppressWarnings("unused")
 
 
 public class UserHistoryPlayerListener extends PlayerListener{
 	UserHistory myPlugin;
 
     
 	public UserHistoryPlayerListener(UserHistory instance) {
 		this.myPlugin = instance;	
     }
 	
     public void onPlayerJoin(PlayerJoinEvent event) {
     	// Start the DB connection
     	this.myPlugin.getMySQL();
 		
     	// Get the player
     	Player thePlayer=event.getPlayer();
     	int thePlayersUserID = this.myPlugin.playerInDB(thePlayer);	
     	// Check to see if they are already in our DB
     	if(thePlayersUserID == 0){
     		//First time user!
     		
     		// Add them to the DBs because they were not there.
     		this.insertPlayerintoDB(thePlayer);
         	// Record the login event
         	this.insertLoginintoDB(thePlayer);
         	//Tell the console
         	this.myPlugin.doLog("First Time Login by: " + thePlayer.getName() + " from IP: " + thePlayer.getAddress().getAddress().getHostAddress());
         	
     	} else {
     		// Returning user!
           	//Tell the console
         	this.myPlugin.doLog("Login by: " + thePlayer.getName() + " from IP: " + thePlayer.getAddress().getAddress().getHostAddress() + " last online: " + this.myPlugin.playerLastOnline(thePlayer));
        	thePlayer.sendMessage("We last saw you " + ChatColor.GREEN + this.myPlugin.timeToString(this.myPlugin.getLastSeen(thePlayer)) + ChatColor.WHITE + " ago at: " + ChatColor.GREEN + this.myPlugin.playerLastOffline(thePlayer).toString());
         	// Record the login event
         	this.insertLoginintoDB(thePlayer);
         	
         	// Tell the Player
         	thePlayer.sendMessage(ChatColor.WHITE + "You have played a total of: " + ChatColor.GREEN + this.myPlugin.timeToString(this.myPlugin.getPlayerAge(thePlayer)) + ChatColor.WHITE + " since: " + ChatColor.GREEN + this.myPlugin.playerFirstOnline(thePlayer) + ChatColor.WHITE);
         	
     	}
     	// Close the DB
     	this.myPlugin.closeMySQL();
     	return;
     }
     
 
 
 	public void onPlayerQuit(PlayerQuitEvent event) {
     	// Start the DB connection
     	this.myPlugin.getMySQL();
     	
     	// Get the player
     	Player thePlayer=event.getPlayer();
     	int thePlayersUserID = this.myPlugin.playerInDB(thePlayer);
     	
     	// Check to see if they are already in our DB
     	if(thePlayersUserID == 0){
     		// Add them to the DB because they were not there.
     		this.insertPlayerintoDB(thePlayer);
     	}
     	// Record the logout event
     	this.insertLogoutintoDB(thePlayer);
     	
     	// Get the length of their time online.
     	long playerOnFor = this.myPlugin.playerLastOnlineTime(thePlayer);
     	String playerOnForS = this.myPlugin.timeToString(playerOnFor);
   
     	// Update their total playtime.
     	this.myPlugin.updatePlayerAge(thePlayer, playerOnFor);
     	
 
     	
     	
     	// Tell the console
     	this.myPlugin.doLog("Logout by: " + thePlayer.getName() + " from IP: " + thePlayer.getAddress().getAddress().getHostAddress() + " on for: " + playerOnForS);
     	this.myPlugin.doLog(thePlayer.getName() + " has played a total of " + this.myPlugin.timeToString(this.myPlugin.getPlayerAge(thePlayer)));
     	
     	// Close the DB
     	this.myPlugin.closeMySQL();
     }
     
     
     
     private int insertPlayerintoDB(Player player){
     	int retval = 0;
     	try {
     		// Insert the new player into the database
     		String myUpdate = "INSERT INTO `" + this.myPlugin.getMySQLDB() + "`.`mcusers` (`UserID`,`UserMCName`) VALUES (NULL,'" + player.getName() + "');";
     		retval = this.myPlugin.myStatement.executeUpdate(myUpdate);
     		// Ensure new players are inserted also into Age, with a total of 0
     		myUpdate = "INSERT INTO `" + this.myPlugin.getMySQLDB() + "`.`mcusersage` (`UserID`, `TotalAge`, `LastUpdated`) VALUES ('" + this.myPlugin.playerInDB(player) + "',0,CURRENT_TIMESTAMP);";
     		this.myPlugin.myStatement.executeUpdate(myUpdate);
 		} catch (SQLException e) {
 			retval = 0;
 			this.myPlugin.doLog("SQL Error! " + e.getMessage());
 		}
     	return(retval);
     }
     
     private int insertLogoutintoDB(Player player){
     	int retval = 0;
     	  	
     	//Assemble the query 
     	final String myUpdate = "INSERT INTO `" + this.myPlugin.getMySQLDB() + "`.`mcuserslogout` (`logoutID`,`UserID`, `SeenIP`, `LogoutTIME`) VALUES (NULL,'" + this.myPlugin.playerInDB(player) + "', '" + player.getAddress().getAddress().getHostAddress() + "', CURRENT_TIMESTAMP);";
     	try {
 			retval = this.myPlugin.myStatement.executeUpdate(myUpdate);
 		} catch (SQLException e) {
 			retval = 0;
 			this.myPlugin.doLog("SQL Error! " + e.getMessage());
 		}
     	
     	return(retval);
     }
     private int insertLoginintoDB(Player player){
     	int retval = 0;
 	  	
     	//Assemble the query 
     	final String myUpdate = "INSERT INTO `" + this.myPlugin.getMySQLDB() + "`.`mcuserslogin` (`loginID`,`UserID`, `SeenIP`, `LoginTIME`) VALUES (NULL,'" + this.myPlugin.playerInDB(player) + "', '" + player.getAddress().getAddress().getHostAddress() + "', CURRENT_TIMESTAMP);";
     	try {
 			retval = this.myPlugin.myStatement.executeUpdate(myUpdate);
 		} catch (SQLException e) {
 			retval = 0;
 			this.myPlugin.doLog("SQL Error! " + e.getMessage());
 		}
     	
     	return(retval);
     	
     }
 
 }
 
     
 
 
 
 
