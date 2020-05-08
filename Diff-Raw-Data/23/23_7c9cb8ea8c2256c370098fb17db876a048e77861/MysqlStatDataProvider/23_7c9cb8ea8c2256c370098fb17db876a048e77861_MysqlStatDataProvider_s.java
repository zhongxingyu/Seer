 package me.tehbeard.BeardStat.DataProviders;
 
 import java.sql.*;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 
 import java.util.Properties;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStat;
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 import me.tehbeard.BeardStat.containers.StaticPlayerStat;
 
 /**
  * Provides backend storage to a mysql database
  * @author James
  *
  */
 public class MysqlStatDataProvider implements IStatDataProvider {
 
     protected Connection conn;
 
     private String host;
     private String database;
     private String table;
     private String username;
     private String password;
 
     //protected static PreparedStatement prepGetPlayerStat;
     protected static PreparedStatement prepGetAllPlayerStat;
     protected static PreparedStatement prepSetPlayerStat;
     protected static PreparedStatement keepAlive;
 
     private HashMap<String,HashSet<PlayerStat>> writeCache = new HashMap<String,HashSet<PlayerStat>>();
 
     public MysqlStatDataProvider(String host,String database,String table,String username,String password) throws SQLException{
 
         this.host = host;
         this.database = database;
         this.table = table;
         this.username = username;
         this.password = password;
         try {
             Class.forName("com.mysql.jdbc.Driver");
 
             createConnection();
 
             checkAndMakeTable();
             prepareStatements();
             if(conn == null){
                 throw new SQLException("Failed to start");
             }
         } catch (ClassNotFoundException e) {
             BeardStat.printCon("MySQL Library not found!");
         }
 
 
 
     }
 
     /**
      * Connection to the database.
      * @throws SQLException
      */
     private void createConnection() {
         String conUrl = String.format("jdbc:mysql://%s/%s",
                 host, 
                 database);
 
         BeardStat.printCon("Configuring....");
         Properties conStr = new Properties();
         conStr.put("user",username);
         conStr.put("password",password);
         conStr.put("autoReconnect", "true");
 
         BeardStat.printCon("Connecting....");
 
         try {
             conn = DriverManager.getConnection(conUrl,conStr);
             //conn.setAutoCommit(false);
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
             conn = null;
         }
 
     }
 
     /**
      * 
      * @return
      */
     private synchronized boolean checkConnection(){
         BeardStat.printDebugCon("Checking connection");
         try {
             if(conn == null || !conn.isValid(0)){
                 BeardStat.printDebugCon("Something is derp, rebooting connection.");
                 createConnection();
                 if(conn!=null){
                     BeardStat.printDebugCon("Rebuilding statements");
                     prepareStatements();
                 }
                 else
                 {
                     BeardStat.printDebugCon("Reboot failed!");
                 }
 
             }
         } catch (SQLException e) {
             conn = null;
             return false;
         }
         BeardStat.printDebugCon("Checking is " + conn != null ? "up" : "down");
         return conn != null;
     }
 
     protected void checkAndMakeTable(){
         BeardStat.printCon("Checking for table");
 
         try{
             ResultSet rs = conn.getMetaData().getTables(null, null, table, null);
             if (!rs.next()) {
                 BeardStat.printCon("Stats table not found, creating table");
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table + "` ("+
                         " `player` varchar(32) NOT NULL DEFAULT '-',"+
                         " `category` varchar(32) NOT NULL DEFAULT 'stats',"+
                         " `stat` varchar(32) NOT NULL DEFAULT '-',"+
                         " `value` int(11) NOT NULL DEFAULT '0',"+
                         " PRIMARY KEY (`player`,`category`,`stat`)"+
                         ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
 
                 ps.executeUpdate();
                 ps.close();
                 BeardStat.printCon("created table");
             }
             else
             {
                 BeardStat.printCon("Table found");
             }
             rs.close();
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }		
     }
 
     protected void prepareStatements(){
         try{
             BeardStat.printDebugCon("Preparing statements");
 
             keepAlive = conn.prepareStatement("SELECT COUNT(*) from `" + table + "`");
             prepGetAllPlayerStat = conn.prepareStatement("SELECT * FROM " + table + " WHERE player=?");
             BeardStat.printDebugCon("Player stat statement created");
 
             prepSetPlayerStat = conn.prepareStatement("INSERT INTO `" + table + "`" +
                     "(`player`,`category`,`stat`,`value`) " +
                     "values (?,?,?,?) ON DUPLICATE KEY UPDATE `value`=?;",Statement.RETURN_GENERATED_KEYS);
 
             BeardStat.printDebugCon("Set player stat statement created");
             BeardStat.printCon("Initaised MySQL Data Provider.");
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }
     }
 
 
 
     public PlayerStatBlob pullPlayerStatBlob(String player) {
         return pullPlayerStatBlob(player,true);
     }
 
     public PlayerStatBlob pullPlayerStatBlob(String player, boolean create) {
         try {
             if(!checkConnection()){
                 BeardStat.printCon("ERROR");
                 return null;
             }
             long t1 = (new Date()).getTime();
             PlayerStatBlob pb = null;
 
             //try to pull it from the db
             prepGetAllPlayerStat.setString(1, player);
             ResultSet rs = prepGetAllPlayerStat.executeQuery();
            pb = new PlayerStatBlob(player,0);
             while(rs.next()){
                 //`category`,`stat`,`value`
                 PlayerStat ps = new StaticPlayerStat(rs.getString(2),rs.getString(3),rs.getInt(4));
                 pb.addStat(ps);
             }
             rs.close();
 
             BeardStat.printDebugCon("time taken to retrieve: "+((new Date()).getTime() - t1) +" Milliseconds");
             if(pb.getStats().size()==0 && create==false){return null;}
 
             return pb;
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }
         return null;
     }
 
     public void pushPlayerStatBlob(PlayerStatBlob player) {
 
         synchronized (writeCache) {
 
 
             HashSet<PlayerStat> copy = writeCache.containsKey(player.getName()) ? writeCache.get(player.getName()) : new HashSet<PlayerStat>();
 
             for(PlayerStat ps : player.getStats()){
                 if(ps.isArchive()){
 
                     PlayerStat ns = new  StaticPlayerStat(ps.getCat(),ps.getName(),ps.getValue());
                     copy.add(ns);
                 }
             }
 
             if(!writeCache.containsKey(player.getName())){
                 writeCache.put(player.getName(), copy);
             }
         }
 
     }
 
     public void flush() {
 
         new Thread(new Runnable() {
 
             public void run() {
                 synchronized (writeCache) {
 
 
                     if(!checkConnection()){
                         Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not restablish connection, will try again later, WARNING: CACHE WILL GROW WHILE THIS HAPPENS");
                     }
                     else{
                         BeardStat.printDebugCon("Saving to database");
                         for(Entry<String, HashSet<PlayerStat>> entry : writeCache.entrySet()){
                             try {
                                 HashSet<PlayerStat> pb = entry.getValue();
 
                                 BeardStat.printDebugCon(entry.getKey() + " " + entry.getValue() +  " [" + pb.size() + "]");
                                 prepSetPlayerStat.clearBatch();
                                 for(PlayerStat ps : pb){
 
                                     prepSetPlayerStat.setString(1, entry.getKey());
 
                                     prepSetPlayerStat.setString(2, ps.getCat());
                                     prepSetPlayerStat.setString(3, ps.getName());
                                     prepSetPlayerStat.setInt(4, ps.getValue());
                                     prepSetPlayerStat.setInt(5, ps.getValue());
                                     prepSetPlayerStat.addBatch();
                                 }
                                 prepSetPlayerStat.executeBatch();
                                
                             } catch (SQLException e) {
                                 checkConnection();
                             }
                         }
                         BeardStat.printDebugCon("Clearing write cache");
                         writeCache.clear();
                     }
                 }
 
             }
         }).start();
     }
 
 
 
 
 }
