 package me.fogest.mctrade;
 
 import java.io.File;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 
 import me.fogest.mctrade.MCTrade;
 import me.fogest.mctrade.SQLibrary.*;
 
 public class DatabaseManager {
 	private static MCTrade plugin = MCTrade.getPlugin();
     public static File dbFolder = new File("plugins/MCTrade");
 
     public static MySQL db = new MySQL(MCTrade.getPlugin().getLogger(),"[MCTrade]","localhost", "3306","mctrade","root","");
 
     /**
      * Initializes, opens and confirms the tables and database.
      */
     
     public static void enableDB(){
     	plugin.getLogger().info("Enable DB");
     	plugin.getLogger().info("Initializing");
         db.initialize();
         plugin.getLogger().info("Initializing Done");
         plugin.getLogger().info("Opening DB");
         db.open();
         plugin.getLogger().info("Opening Done");
         plugin.getLogger().info("Confirming Tables");
         confirmTables();
         plugin.getLogger().info("Confirmed!");
     }
 
     /**
      * Closes the database.
      */
     public static void disableDB(){ if(db.checkConnection()) db.close(); }
 
     private static void confirmTables(){
         if(!db.checkTable("MCTrade_users")){
             String queryString = "CREATE TABLE IF NOT EXISTS `MCTrade_users` ("
 			  +"`user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
 			  +"`username` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`password` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`minecraft_name` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`email` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`user_level` int(11) NOT NULL,"
 			  +"`Current_Open_Trades` int(11) NOT NULL,"
 			  +"`code` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`active` tinyint(4) NOT NULL,"
 			  +"`ip` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`mc_code` int(11) NOT NULL,"
 			  +"PRIMARY KEY (`user_id`))";
 									
             try {
                 db.query(queryString);
                 MCTrade.getPlugin().getLogger().log(Level.INFO, "Successfully created the requests table.");
             } catch (Exception e) {
                 MCTrade.getPlugin().getLogger().log(Level.SEVERE, "Unable to create the requests table.");
                 e.printStackTrace();
             }
         }
         if(!db.checkTable("MCTrade_trades")){
             String queryString = "CREATE TABLE IF NOT EXISTS `MCTrade_trades` ("
 			  +"`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
 			  +"`Minecraft_Username` text NOT NULL,"
 			  +"`Block_ID` int(5) NOT NULL,"
			  +"`Block_Name` text COLLATE latin1_general_ci NOT NULL,"
 			  +"`Quantity` int(3) NOT NULL,"
 			  +"`CostPer` text NOT NULL,"
 			  +"`TradeNotes` text NOT NULL,"
 			  +"`AccountName` text NOT NULL,"
 			  +"`IP` text NOT NULL,"
 			  +"`Trade_Status` int(11) NOT NULL COMMENT '1 = Open Trade, 2 = Closed Trade, 3 = Hidden Trade',"
 			  +"PRIMARY KEY (`id`))";
             try {
                 db.query(queryString);
                 MCTrade.getPlugin().getLogger().log(Level.INFO, "Successfully created the trades table.");
             } catch (Exception e) {
                 MCTrade.getPlugin().getLogger().log(Level.SEVERE, "Unable to create the trades table.");
                 e.printStackTrace();
             }
         }
     }
     public static void setMcCode(int code, int UserId) {
     	if(!db.checkConnection()) MCTrade.getPlugin().getLogger().log(Level.INFO, "There was an issue retrieving a db conntection");
     	String query = "UPDATE `mctrade_users` SET  `mc_code` =  '125' WHERE  `mctrade_users`.`user_id` =1;";
     	try {
     	PreparedStatement ps = db.getConnection().prepareStatement("UPDATE  `mctrade`.`mctrade_users` SET  `mc_code` =  ? WHERE  `mctrade_users`.`user_id` = ?;");
     	ps.setInt(1,code);
     	ps.setInt(2,UserId);
     	ps.executeUpdate();
     	ps.close();
     	
     	} catch(SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static int getUserId(String player){
         if(!db.checkConnection()) return 0;
         int userId = 0;
         try {
             PreparedStatement ps = db.getConnection().prepareStatement("SELECT `user_id` FROM `mctrade_users` WHERE `minecraft_name` = ?");
             ps.setString(1, player);
             ResultSet rs = ps.executeQuery();
             if(rs.next()) {
             userId = rs.getInt("user_id");
             }
             ps.close();
             rs.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return userId;
     }
     public static String getUsername(String player){
         if(!db.checkConnection()) return "";
         String username = "";
         try {
             PreparedStatement ps = db.getConnection().prepareStatement("SELECT `username` FROM `mctrade_users` WHERE `minecraft_name` = ?");
             ps.setString(1, player);
             ResultSet rs = ps.executeQuery();
             if(rs.next()) {
             username = rs.getString("username");
             }
             ps.close();
             rs.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return username;
     }
     public static int getCurOpenTrades(String player) {
     	int curTrades = 0;
         try {
             PreparedStatement ps = db.getConnection().prepareStatement("SELECT `Current_Open_Trades` FROM `mctrade_users` WHERE `minecraft_name` = ?");
             ps.setString(1, player);
             ResultSet rs = ps.executeQuery();
             if(rs.next()) {
             	curTrades = rs.getInt("Current_Open_Trades");
             }
             ps.close();
             rs.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	
     	
     	return curTrades;
     }
 
     public static int createUser(String player){
         if(!db.checkConnection()) return 0;
         int userId = 0;
         try {
             PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO `MCTrade_users` (`name`, `banned`) VALUES (?, '0')");
             ps.setString(1, player);
             if(ps.executeUpdate() < 1) return 0;
             ps.close();
             ps = db.getConnection().prepareStatement("SELECT `id` FROM `MCTrade_user` WHERE `name` = ?");
             ps.setString(1, player);
             ResultSet rs = ps.executeQuery();
 
             if(!rs.isBeforeFirst()) return 0;
             userId = rs.getInt(1);
             ps.close();
             rs.close();
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return userId;
     }
     /**
      * Files a request, inserting it into the database.
      * @param player String Name of player
      * @param world String Name of world
      * @param location getLocation() object
      * @param message String help request message
      * @return True if successful.
      */
     public static int createTrade(String player, int blockId,String block, int amount, String cost, String Ip){
        String username = getUsername(player);
        int id = 0;
        try {
     	   PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO mctrade_trades VALUES (NULL,?,?,?,?,?,?,?,?,'1')");
     	   ps.setString(1, player);
     	   ps.setInt(2, blockId);
     	   ps.setString(3,"");
     	   ps.setInt(4, amount);
     	   ps.setString(5,cost);
     	   ps.setString(6, "Trade Created using MCTrade Plugin");
     	   ps.setString(7,username);
     	   ps.setString(8,Ip);
     	   ps.executeUpdate();
     	   ps.close();
     	   ps = db.getConnection().prepareStatement("SELECT MAX(id) FROM mctrade_trades");
     	   ResultSet rs = ps.executeQuery();
     	   while(rs.next()){
     	   id = rs.getInt("MAX(id)");
     	   }
     	   ps.close();
     	   rs.close();
     	   
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
     	return id;
     }
 
 
     public static boolean resetDB(){
         db.query("DELETE FROM MCTrade_request");
         db.query("DELETE FROM MCTrade_user");
         return true;
     }
 }
