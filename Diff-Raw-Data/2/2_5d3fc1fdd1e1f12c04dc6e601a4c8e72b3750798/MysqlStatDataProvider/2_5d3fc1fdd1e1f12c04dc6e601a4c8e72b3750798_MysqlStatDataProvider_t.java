 package me.tehbeard.BeardStat.DataProviders;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 
 
 import java.util.Properties;
 
 import net.dragonzone.promise.Deferred;
 import net.dragonzone.promise.Promise;
 
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
     private int port;
 
     //protected static PreparedStatement prepGetPlayerStat;
     private PreparedStatement prepGetAllPlayerStat;
     private PreparedStatement prepSetPlayerStat;
     private PreparedStatement keepAlive;
     private PreparedStatement prepDeletePlayerStat;
     private PreparedStatement prepHasPlayerStat;
     private PreparedStatement prepGetPlayerList;
 
     private HashMap<String,HashSet<PlayerStat>> writeCache = new HashMap<String,HashSet<PlayerStat>>();
 
 
 
     public MysqlStatDataProvider(String host,int port,String database,String table,String username,String password) throws SQLException{
 
         this.host = host;
         this.port = port;
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
         String conUrl = String.format("jdbc:mysql://%s:%s/%s",
                 host,
                 port,
                 database);
 
         BeardStat.printCon("Configuring....");
         Properties conStr = new Properties();
         conStr.put("user",username);
         conStr.put("password",password);
         conStr.put("autoReconnect", "true");
 
         BeardStat.printCon("Connecting....");
 
         try {
             conn = DriverManager.getConnection(conUrl,conStr);
             
             DatabaseMetaData metadata = conn.getMetaData();
             
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
                        ") ENGINE=InnoDB COMMENT='version:${project.database.version}' DEFAULT CHARSET=latin1;");
 
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
 
             prepDeletePlayerStat = conn.prepareStatement("DELETE FROM `" + table + "` WHERE player=?");
 
             prepHasPlayerStat = conn.prepareStatement("SELECT COUNT(*) from `" + table + "` WHERE player=?");
 
             prepGetPlayerList = conn.prepareStatement("SELECT DISTINCT(player) from `" + table + "`");
             BeardStat.printDebugCon("Set player stat statement created");
             BeardStat.printCon("Initaised MySQL Data Provider.");
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }
     }
 
 
 
     public Promise<PlayerStatBlob> pullPlayerStatBlob(String player) {
         return pullPlayerStatBlob(player,true);
     }
 
     public Promise<PlayerStatBlob> pullPlayerStatBlob(final String player, final boolean create) {
 
         final Deferred<PlayerStatBlob> promise = new Deferred<PlayerStatBlob>();
 
         Runnable run = new Runnable() {
             
             public void run() {
                 try {
                     if(!checkConnection()){
                         BeardStat.printCon("ERROR");
                         promise.resolve(null);
                         return;
                     }
                     long t1 = (new Date()).getTime();
                     PlayerStatBlob pb = null;
 
                     //try to pull it from the db
                     prepGetAllPlayerStat.setString(1, player);
                     ResultSet rs = prepGetAllPlayerStat.executeQuery();
                     pb = new PlayerStatBlob(player,"");
                     while(rs.next()){
                         //`category`,`stat`,`value`
                         PlayerStat ps = pb.getStat(rs.getString(2),rs.getString(3));
                         ps.setValue(rs.getInt(4));
                         ps.archive();
                     }
                     rs.close();
 
                     BeardStat.printDebugCon("time taken to retrieve: "+((new Date()).getTime() - t1) +" Milliseconds");
                     if(pb.getStats().size()==0 && create==false){promise.resolve(null);return;}
 
                     promise.resolve(pb);return;
                 } catch (SQLException e) {
                     BeardStat.mysqlError(e);
                 }
                 promise.resolve(null);return;
                 
             }
         };
         
         new Thread(run).start();
         
         return promise;
 
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
 
     private Runnable flush = new Runnable() {
 
         public void run() {
             synchronized (writeCache) {
                 try {
                     keepAlive.execute();
                 } catch (SQLException e1) {
                 }
 
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
     };
 
     public void flushSync(){
         BeardStat.printCon("Flushing in main thread! Game will lag!");
         flush.run();
         BeardStat.printCon("Flushed!");
     }
 
     public void flush() {
 
         new Thread(flush).start();
     }
 
     public void deletePlayerStatBlob(String player) {
         try {
             prepDeletePlayerStat.clearParameters();
             prepDeletePlayerStat.setString(1,player);
             prepDeletePlayerStat.execute();
         } catch (SQLException e) {
             checkConnection();
         }
     }
 
     public boolean hasStatBlob(String player) {
         try {
             prepHasPlayerStat.clearParameters();
             prepHasPlayerStat.setString(1,player);
             ResultSet rs = prepHasPlayerStat.executeQuery();
             if(rs.next()){
                 boolean b = (rs.getInt(1) > 0);
                 rs.close();
                 return b;
             }
 
         } catch (SQLException e) {
             checkConnection();
         }
         return false;
     }
 
     public List<String> getStatBlobsHeld() {
         List<String> list = new ArrayList<String>();
         try {
             prepGetPlayerList.clearParameters();
             ResultSet rs = prepGetPlayerList.executeQuery();
             while(rs.next()){
                 list.add(rs.getString(1));
             }
             rs.close();
 
         } catch (SQLException e) {
             checkConnection();
         }
         return list;
     }
 }
