 /*
  * SQLBans
  * Copyright 2012 Matt Baxter
  *
  * Google Gson
  * Copyright 2008-2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kitteh.sqlbans;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.kitteh.sqlbans.BackupTask.BanItem;
 import org.kitteh.sqlbans.SQLManager.SQLConnection;
 import org.kitteh.sqlbans.exceptions.SQLBansException;
 
 public class SQLHandler {
 
     private static SQLHandler instance = null;
     private static String banTableName;
     private static String logTableName;
 
     public static void banIP(InetAddress ip, String reason, String admin) throws SQLBansException, SQLException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getUpdateConnection();
             final BanType type = BanType.IP;
             final PreparedStatement statement = con.getConnection().prepareStatement("INSERT INTO `" + SQLHandler.banTableName + "` (`ip`, `type`, `reason`, `admin`, `timestamp`, `server`) VALUES (?, ?, ?, ?, ?, ?)");
             statement.setBytes(1, ip.getAddress());
             statement.setInt(2, type.getID());
             statement.setString(3, reason);
             statement.setString(4, admin);
             statement.setTimestamp(5, new Timestamp(new Date().getTime()));
             statement.setString(6, SQLHandler.instance().plugin.getServerName());
             statement.executeUpdate();
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     public static void banName(String user, String reason, String admin) throws SQLBansException, SQLException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getUpdateConnection();
             final BanType type = BanType.NAME;
             final PreparedStatement statement = con.getConnection().prepareStatement("INSERT INTO `" + SQLHandler.banTableName + "` (`username`, `type`, `reason`, `admin`, `timestamp`, `server`) VALUES (?, ?, ?, ?, ?, ?)");
             statement.setString(1, user);
             statement.setInt(2, type.getID());
             statement.setString(3, reason);
             statement.setString(4, admin);
             statement.setTimestamp(5, new Timestamp(new Date().getTime()));
             statement.setString(6, SQLHandler.instance().plugin.getServerName());
             statement.executeUpdate();
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     public static boolean canJoin(InetAddress address) throws SQLBansException, SQLException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getQueryConnection();
             final PreparedStatement banQuery = con.getConnection().prepareStatement("SELECT `id` FROM `" + SQLHandler.banTableName + "` WHERE `ip` = ? AND `isbanned` = 1");
             banQuery.setBytes(1, address.getAddress());
             final boolean ret = !banQuery.executeQuery().first();
             return ret;
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     public static boolean canJoin(String name) throws SQLBansException, SQLException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getQueryConnection();
             final PreparedStatement banQuery = con.getConnection().prepareStatement("SELECT `id` FROM `" + SQLHandler.banTableName + "` WHERE `username` = ? AND `isbanned` = 1");
             banQuery.setString(1, name);
             final boolean ret = !banQuery.executeQuery().first();
             return ret;
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     public static Set<BanItem> getAllBans(BanType type) throws SQLException, SQLBansException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getQueryConnection();
             final Set<BanItem> list = new HashSet<BanItem>();
             final PreparedStatement statement = con.getConnection().prepareStatement("SELECT `" + (type == BanType.NAME ? "username" : "ip") + "`,`reason`,`admin`,`timestamp`,`banlength` FROM `" + SQLHandler.banTableName + "` WHERE `type` = ? AND `isbanned` = 1");
             statement.setInt(1, type.getID());
             final ResultSet result = statement.executeQuery();
             while (result.next()) {
                 String name;
                 if (type == BanType.NAME) {
                     name = result.getString("username");
                 } else {
                     try {
                         final InetAddress address = InetAddress.getByAddress(result.getBytes("ip"));
                         name = address.getHostAddress();
                     } catch (final UnknownHostException e) {
                         continue;
                     }
                 }
                 list.add(new BanItem(name, result.getString("admin"), result.getTimestamp("timestamp"), result.getInt("banlength"), result.getString("reason")));
             }
             return list;
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     public static void logJoin(String name, InetAddress address) throws SQLException, SQLBansException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getUpdateConnection();
             final PreparedStatement statement = con.getConnection().prepareStatement("INSERT INTO `" + SQLHandler.logTableName + "` (`username`,`ip`,`server`) VALUES (?,?,?);");
             statement.setString(1, name);
             statement.setBytes(2, address.getAddress());
             statement.setString(3, SQLHandler.instance().plugin.getServerName());
             statement.executeUpdate();
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     public static void nullifyInstance() {
         SQLHandler.instance = null;
     }
 
     public static void start(SQLBans plugin, String host, int port, String user, String pass, String db, String banTableName, String logTableName, String tableCreate) throws SQLBansException {
         if (SQLHandler.instance != null) {
             throw new SQLBansException("Thread already running! Something has gone terribly wrong!");
         }
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (final ClassNotFoundException e1) {
             throw new SQLBansException("What on earth are you doing. This isn't CraftBukkit.");
         }
         SQLHandler.instance = new SQLHandler(plugin, host, port, user, pass, db, tableCreate);
         SQLHandler.banTableName = banTableName;
         SQLHandler.logTableName = logTableName;
     }
 
     public static void unban(InetAddress address) throws SQLException, SQLBansException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getUpdateConnection();
             final PreparedStatement statement = con.getConnection().prepareStatement("UPDATE `" + SQLHandler.banTableName + "` SET `isbanned` = 0 WHERE `ip` = ?");
             statement.setBytes(1, address.getAddress());
             statement.executeUpdate();
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     public static void unban(String user) throws SQLBansException, SQLException {
         SQLConnection con = null;
         try {
             con = SQLHandler.instance().manager.getUpdateConnection();
             final PreparedStatement statement = con.getConnection().prepareStatement("UPDATE `" + SQLHandler.banTableName + "` SET `isbanned` = 0 WHERE `username` = ?");
             statement.setString(1, user);
             statement.executeUpdate();
         } finally {
             try {
                 con.myWorkHereIsDone();
             } catch (final Exception e) {
             }
         }
     }
 
     private static SQLHandler instance() throws SQLBansException {
         if (SQLHandler.instance == null) {
             throw new SQLBansException("Not loaded!");
         }
         return SQLHandler.instance;
     }
 
     private SQLManager manager;
 
     private SQLBans plugin;
 
     public SQLHandler() throws SQLBansException {
         throw new SQLBansException("Stop right there, criminal scum");
     }
 
     private SQLHandler(SQLBans plugin, String host, int port, String user, String pass, String db, String tableCreate) throws SQLBansException {
         this.plugin = plugin;
         try {
             this.manager = new SQLManager("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&user=" + user + "&password=" + pass);
         } catch (final Exception e) {
             throw new SQLBansException("SQL connection failure!", e);
         }
         try {
             final SQLConnection con = this.manager.getQueryConnection();
             final ResultSet bansExists = con.getConnection().getMetaData().getTables(null, null, SQLHandler.banTableName, null);
             final ResultSet logsExists = con.getConnection().getMetaData().getTables(null, null, SQLHandler.logTableName, null);
             if (!bansExists.first() || !logsExists.first()) {
                 if (tableCreate != null) {
                    con.getConnection().createStatement().executeUpdate(tableCreate);
                 } else {
                     new SQLBansException("You will need to create the tables manually. Import create.sql from this plugin jar file.");
                 }
             }
             con.myWorkHereIsDone();
         } catch (final SQLException e) {
             throw new SQLBansException("Failure while checking for table!", e);
         }
     }
 }
