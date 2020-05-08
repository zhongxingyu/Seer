 package me.tehbeard.BeardAch.dataSource;
 
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.bukkit.Bukkit;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.AchievementPlayerLink;
 
 @DataSourceDescriptor(tag="mysql",version="1.1")
 public class SqlDataSource implements IDataSource{
 
     private HashMap<String,HashSet<AchievementPlayerLink>> writeCache = new HashMap<String,HashSet<AchievementPlayerLink>>();
     Connection conn;
 
 
     //protected static PreparedStatement prepGetPlayerStat;
     protected static PreparedStatement prepGetAllPlayerAch;
     protected static PreparedStatement prepAddPlayerAch;
     
     protected static PreparedStatement ping;
 
 
     protected void createConnection(){
         String conUrl = String.format("jdbc:mysql://%s/%s",
                 BeardAch.self.getConfig().getString("ach.database.host"), 
                 BeardAch.self.getConfig().getString("ach.database.database"));
         BeardAch.printCon("Configuring....");
         Properties conStr = new Properties();
         conStr.put("user",BeardAch.self.getConfig().getString("ach.database.username",""));
         conStr.put("password",BeardAch.self.getConfig().getString("ach.database.password",""));
         BeardAch.printCon("Connecting....");
         try {
             conn = DriverManager.getConnection(conUrl,conStr);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     protected void checkAndBuildTable(){
         try{
             BeardAch.printCon("Checking for storage table");
             ResultSet rs = conn.getMetaData().getTables(null, null, "achievements", null);
             if (!rs.next()) {
                 BeardAch.printCon("Achievements table not found, creating table");
                 PreparedStatement ps = conn.prepareStatement(
                         "CREATE TABLE IF NOT EXISTS `achievements` " +
                                 "( `player` varchar(32) NOT NULL,  " +
                                 "`achievement` varchar(255) NOT NULL,  " +
                                 "`timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                                 " KEY `player` (`player`)) " +
                         "ENGINE=InnoDB DEFAULT CHARSET=latin1;");
                 ps.executeUpdate();
                 ps.close();
                 BeardAch.printCon("created storage table");
             }
             else
             {
                 BeardAch.printCon("Table found");
             }
             rs.close();
 
             BeardAch.printCon("Checking for meta table");
             rs = conn.getMetaData().getTables(null, null, "ach_map", null);
             if (!rs.next()) {
                 BeardAch.printCon("meta table not found, creating table");
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `ach_map` (`slug` char(255) NOT NULL,`name` char(255) NOT NULL,`description` char(255) NOT NULL,UNIQUE KEY `slug` (`slug`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
                 ps.executeUpdate();
                 ps.close();
                 BeardAch.printCon("created meta table");
             }
             else
             {
                 BeardAch.printCon("Table found");
             }
             rs.close();
 
 
 
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     protected void prepareStatements(){
         BeardAch.printDebugCon("Preparing statements");
         try{
             prepGetAllPlayerAch = conn.prepareStatement("SELECT `achievement`,`timestamp` FROM `achievements` WHERE player=?");
             prepAddPlayerAch = conn.prepareStatement("INSERT INTO `achievements` (`player` ,`achievement`,`timestamp`) values (?,?,?)",Statement.RETURN_GENERATED_KEYS);
             
             ping = conn.prepareStatement("SELECT count(*) from `achievement`");
             
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
     }
     
     private synchronized boolean checkConnection(){
 		BeardAch.printDebugCon("Checking connection");
 		try {
 			if(conn == null || !conn.isValid(0)){
 				BeardAch.printDebugCon("Something is derp, rebooting connection.");
 				createConnection();
 				if(conn!=null){
 					BeardAch.printDebugCon("Rebuilding statements");
 					prepareStatements();
 				}
 				else
 				{
 					BeardAch.printDebugCon("Reboot failed!");
 				}
 
 			}
 		} catch (SQLException e) {
 			conn = null;
 			return false;
 		}catch(AbstractMethodError e){
 
 		}
 		BeardAch.printDebugCon("Checking is " + conn != null ? "up" : "down");
 		return conn != null;
 	}
 
     public SqlDataSource() {
         try {
             Class.forName("com.mysql.jdbc.Driver");
 
 
             createConnection();
             checkAndBuildTable();
             prepareStatements();
 
 
             Bukkit.getScheduler().scheduleSyncRepeatingTask(BeardAch.self, new Runnable(){
                 public void run() {
                     Runnable r = new SqlFlusher(writeCache);
                     writeCache = new HashMap<String,HashSet<AchievementPlayerLink>>();
                     Bukkit.getScheduler().runTaskAsynchronously(BeardAch.self, r);
                     
                 }
 
             }, 2400L, 2400L);
             BeardAch.printCon("Initaised MySQL Data Provider.");
         } catch (ClassNotFoundException e) {
             BeardAch.printCon("MySQL Library not found!");
         }
     }
 
 
 
 
     public HashSet<AchievementPlayerLink> getPlayersAchievements(String player) {
         HashSet<AchievementPlayerLink> c = new HashSet<AchievementPlayerLink>();
         try {
         	if(!checkConnection()){
         		throw new SQLException("Failed to reconnect to server, aborting load");
         	}
             prepGetAllPlayerAch.setString(1, player);
             ResultSet rs = prepGetAllPlayerAch.executeQuery();
 
             while(rs.next()){
                 c.add(new AchievementPlayerLink(rs.getString(1),rs.getTimestamp(2)));
 
             }
             rs.close();
             if(writeCache.containsKey(player)){
                 c.addAll(writeCache.get(player));
             }
             return c;
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
 
     public void setPlayersAchievements(String player,
             String achievements) {
 
 
         if(!writeCache.containsKey(player)){
             writeCache.put(player, new HashSet<AchievementPlayerLink>());
         }
         writeCache.get(player).add(new AchievementPlayerLink(achievements,new Timestamp((new java.util.Date()).getTime())));
 
     }
 
 
     class SqlFlusher implements Runnable {
 
         private HashMap<String, HashSet<AchievementPlayerLink>> write;
         SqlFlusher(HashMap<String, HashSet<AchievementPlayerLink>> writeCache){
             write = writeCache;
         }
 
         public void run() {
             if(BeardAch.self.getConfig().getBoolean("general.verbose",false)){
                 BeardAch.printCon("Flushing to database");
             }
             try {
                 //if connection is closed, attempt to rebuild connection
             	if(!checkConnection()){
             		throw new SQLException("Failed to reconnect to SQL server");
             	}
             	
             	ping.execute();
 
                 for( Entry<String, HashSet<AchievementPlayerLink>> es :write.entrySet()){
 
                     prepAddPlayerAch.setString(1, es.getKey());
 
                     for(AchievementPlayerLink val : es.getValue()){
                         prepAddPlayerAch.setString(2,val.getSlug());
                         prepAddPlayerAch.setTimestamp(3,val.getDate());
                         prepAddPlayerAch.addBatch();
                     }
                 }
                 prepAddPlayerAch.executeBatch();
                 prepAddPlayerAch.clearBatch();
                 if(BeardAch.self.getConfig().getBoolean("general.verbose",false)){
                     BeardAch.printCon("Flushed to database");
                 }
 
 
             } catch (SQLException e) {
                 BeardAch.printCon("Connection Could not be established, attempting to reconnect...");
                 createConnection();
                 prepareStatements();
             }
         }
 
     }
 
     public void flush() {
         (new SqlFlusher(writeCache)).run();
         writeCache = new HashMap<String,HashSet<AchievementPlayerLink>>();
 
     }
 
 
     public void clearAchievements(String player) {
 
     }
 
     public void dumpFancy() {
         try {
             conn.prepareStatement("DELETE FROM `ach_map`").execute();
             PreparedStatement fancyStat = conn.prepareStatement("INSERT INTO `ach_map`  values (?,?,?)");
 
             fancyStat.clearBatch();
 
 
             for(Achievement ach : BeardAch.self.getAchievementManager().getAchievementsList()){
                 fancyStat.setString(1, ach.getSlug());
                 fancyStat.setString(2, ach.getName());
                 fancyStat.setString(3, ach.getDescrip());
                 fancyStat.addBatch();
             }
             fancyStat.executeBatch();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
     }
 }
