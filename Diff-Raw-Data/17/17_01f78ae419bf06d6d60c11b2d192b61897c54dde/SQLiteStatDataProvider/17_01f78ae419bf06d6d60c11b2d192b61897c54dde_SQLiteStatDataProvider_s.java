 package me.tehbeard.BeardStat.DataProviders;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 
 import net.dragonzone.promise.Deferred;
 import net.dragonzone.promise.Promise;
 
 
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStat;
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 import me.tehbeard.BeardStat.containers.StaticPlayerStat;
 
 /**
  * Provides backend storage to a mysql database
  * @author James
  *
  */
 public class SQLiteStatDataProvider implements IStatDataProvider {
 
     protected Connection conn;
 
     private String filename;
     private String table;
 
     //protected static PreparedStatement prepGetPlayerStat;
     protected static PreparedStatement prepGetAllPlayerStat;
     protected static PreparedStatement prepSetPlayerStat;
     protected static PreparedStatement keepAlive;
     protected static PreparedStatement prepDeletePlayerStat;
     protected static PreparedStatement prepHasPlayerStat;
     protected static PreparedStatement prepGetPlayerList;
 
     private HashMap<String,HashSet<PlayerStat>> writeCache = new HashMap<String,HashSet<PlayerStat>>();
 
     public SQLiteStatDataProvider(String filename,String table) throws SQLException{
 
         this.filename = filename;
         this.table = table;
         try {
             Class.forName("org.sqlite.JDBC");
 
             createConnection();
 
             checkAndMakeTable();
             prepareStatements();
             if(conn == null){
                 throw new SQLException("Failed to start");
             }
         } catch (ClassNotFoundException e) {
             BeardStat.printCon("SQLite Library not found!");
         }
 
 
 
     }
 
     /**
      * Connection to the database.
      * @throws SQLException
      */
     private void createConnection() {
         String conUrl = String.format("jdbc:sqlite:%s",filename);
 
         BeardStat.printCon("Connecting....");
 
         try {
             conn = DriverManager.getConnection(conUrl);
             //conn.setAutoCommit(false);
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
             conn = null;
         }
 
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
                         ");");
 
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
 
             prepSetPlayerStat = conn.prepareStatement("INSERT OR REPLACE INTO `" + table + "`" +
                     "(`player`,`category`,`stat`,`value`) " +
                     "values (?,?,?,?); ");
 
             prepDeletePlayerStat = conn.prepareStatement("DELETE FROM `" + table + "` WHERE player=?");
 
             prepHasPlayerStat = conn.prepareStatement("SELECT COUNT(*) from `" + table + "` WHERE player=?");
 
             prepGetPlayerList = conn.prepareStatement("SELECT DISTINCT(player) from `" + table + "`");
 
             BeardStat.printDebugCon("Set player stat statement created");
             BeardStat.printCon("Initaised SQLite Data Provider.");
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }
     }
 
 
 
     public Promise<PlayerStatBlob> pullPlayerStatBlob(String player) {
         return pullPlayerStatBlob(player,true);
     }
 
     public Promise<PlayerStatBlob> pullPlayerStatBlob(final String player, final boolean create) {
         /*try {
 
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
             if(pb.getStats().size()==0 && create==false){return null;}
 
             return new Deferred<PlayerStatBlob>(pb);
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
         }
         return null;*/
         
         final Deferred<PlayerStatBlob> promise = new Deferred<PlayerStatBlob>();
 
         Runnable run = new Runnable() {
             
             public void run() {
                 try {
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
 
 
 
                 long t = System.currentTimeMillis();
                 BeardStat.printDebugCon("Saving to database");
                 try {
                     prepSetPlayerStat.clearBatch();
                     for(Entry<String, HashSet<PlayerStat>> entry : writeCache.entrySet()){
 
                         HashSet<PlayerStat> pb = entry.getValue();
 
                         BeardStat.printDebugCon(entry.getKey() + " " + entry.getValue() +  " [" + pb.size() + "]");
                         
                         for(PlayerStat ps : pb){
 
                             prepSetPlayerStat.setString(1, entry.getKey());
 
                             prepSetPlayerStat.setString(2, ps.getCat());
                             prepSetPlayerStat.setString(3, ps.getName());
                             prepSetPlayerStat.setInt(4, ps.getValue());
 
 
                             prepSetPlayerStat.addBatch();
                         }
                         
 
 
 
                     }
                     prepSetPlayerStat.executeBatch();
                 } catch (SQLException e) {
                 }
                 BeardStat.printDebugCon("Clearing write cache");
                 BeardStat.printDebugCon("Time taken to write: " +((System.currentTimeMillis() - t)/1000L));
                 writeCache.clear();
             }
 
         }
     };
 
     public void flush() {
 
         new Thread(flush).start();
     }
 
     public void flushSync(){
         BeardStat.printCon("Flushing in main thread! Game will lag!");
         flush.run();
         BeardStat.printCon("Flushed!");
     }
 
     public void deletePlayerStatBlob(String player) {
         try {
             prepDeletePlayerStat.clearParameters();
             prepDeletePlayerStat.setString(1,player);
             prepDeletePlayerStat.execute();
         } catch (SQLException e) {
             BeardStat.mysqlError(e);
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
             BeardStat.mysqlError(e);
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
             BeardStat.mysqlError(e);
         }
         return list;
     }
 }
