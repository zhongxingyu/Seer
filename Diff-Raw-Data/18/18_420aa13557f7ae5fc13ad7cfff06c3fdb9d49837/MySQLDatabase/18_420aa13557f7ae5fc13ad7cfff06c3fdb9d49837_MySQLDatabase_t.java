 /*
 Copyright (C) 2011 Serge Humphrey
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
 
 package com.btbb.figadmin;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class MySQLDatabase extends Database {
 
     private FigAdmin plugin;
 
     public MySQLDatabase(FigAdmin instance) {
         plugin = instance;
     }
 
     public Connection getSQLConnection() {
         FileConfiguration Config = plugin.getConfig();
         String mysqlDatabase = Config.getString("mysql-database",
                 "jdbc:mysql://localhost:3306/minecraft");
         String mysqlUser = Config.getString("mysql-user", "root");
         String mysqlPassword = Config.getString("mysql-password", "root");
         try {
 
             return DriverManager.getConnection(mysqlDatabase + "?autoReconnect=true&user="
                     + mysqlUser + "&password=" + mysqlPassword);
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "Unable to retreive connection", ex);
         }
         return null;
     }
 
     public void initialize(FigAdmin plugin) {
         this.plugin = plugin;
 
     }
 
     public String getAddress(String pName) {
 
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             conn = getSQLConnection();
             ps = conn.prepareStatement("SELECT `ip` FROM banlist WHERE name = ?");
             ps.setString(1, pName);
             rs = ps.executeQuery();
             while (rs.next()) {
                 String ip = rs.getString("ip");
                 return ip;
             }
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
                 if (rs != null)
                     rs.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
 
         }
         return null;
     }
 
     public boolean removeFromBanlist(String player) {
 
         String mysqlTable = plugin.getConfig().getString("mysql-table");
 
         Connection conn = null;
         PreparedStatement ps = null;
         try {
             conn = getSQLConnection();
             ps = conn.prepareStatement("DELETE FROM " + mysqlTable
                     + " WHERE name = ? AND (type = 0 or type = 1) ORDER BY time DESC");
             ps.setString(1, player);
             ps.executeUpdate();
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
             return false;
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
         return true;
 
     }
 
     public void addPlayer(EditBan e) {
 
         String mysqlTable = plugin.getConfig().getString("mysql-table");
 
         Connection conn = null;
         PreparedStatement ps = null;
         try {
             conn = getSQLConnection();
             ps = conn.prepareStatement("INSERT INTO " + mysqlTable
                     + " (name,reason,admin,temptime,type,time) VALUES(?,?,?,?,?,?)");
             ps.setString(1, e.name);
             ps.setString(2, e.reason);
             ps.setString(3, e.admin);
             if (e.endTime < 1) {
                 ps.setLong(4, 0);
             } else
                 ps.setLong(4, e.endTime);
             ps.setInt(5, e.type);
             ps.setLong(6, System.currentTimeMillis() / 1000);
             ps.executeUpdate();
             // Update banedit ID
             PreparedStatement getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
             ResultSet rsID = getID.executeQuery();
             rsID.next();
             e.id = rsID.getInt(1);
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
     }
 
     public String getBanReason(String player) {
         Connection conn = getSQLConnection();
         PreparedStatement ps = null;
         ResultSet rs = null;
         String mysqlTable = plugin.getConfig().getString("mysql-table");
         try {
             ps = conn.prepareStatement("SELECT * FROM " + mysqlTable
                     + " WHERE name = ? ORDER BY id DESC LIMIT 1");
             ps.setString(1, player);
             rs = ps.executeQuery();
             while (rs.next()) {
                 String reason = rs.getString("reason");
                 return reason;
             }
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
         return null;
     }
 
     public void updateAddress(String p, String ip) {
 
         Connection conn = null;
         PreparedStatement ps = null;
         try {
             conn = getSQLConnection();
             ps = conn.prepareStatement("UPDATE `banlist` SET ip = ? WHERE name = ?");
             ps.setString(1, ip);
             ps.setString(2, p);
             ps.executeUpdate();
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
 
     }
 
     public ArrayList<EditBan> listRecords(String name, boolean exact) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             conn = getSQLConnection();
             ps = conn.prepareStatement("SELECT * FROM banlist WHERE name "+ 
              ((exact) ? "=" : "LIKE") +" ? ORDER BY time DESC LIMIT 10");
             if (exact) {
                 ps.setString(1, name);
             } else {
                 ps.setString(1, "%"+name+"%");
             }
             rs = ps.executeQuery();
             ArrayList<EditBan> bans = new ArrayList<EditBan>();
             while (rs.next()) {
                 bans.add(getEditBan(rs));
             }
             return bans;
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
                 if (rs != null)
                     rs.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
         return null;
     }
 
     protected EditBan loadFullRecord(String pName) {
         return loadFullRecord(pName, -1);
     }
 
     protected EditBan loadFullRecord(int id) {
         return loadFullRecord(null, id);
     }
 
     /*
      * if pName = null then it will look up for index id
      */
     private EditBan loadFullRecord(String pName, int id) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             String statement = "SELECT * FROM banlist WHERE name = ? ORDER BY time DESC LIMIT 1";
             if (pName == null) {
                 statement = "SELECT * FROM banlist WHERE id = ?";
             }
             conn = getSQLConnection();
             ps = conn.prepareStatement(statement);
             if (pName == null) {
                 ps.setInt(1, id);
             } else {
                 ps.setString(1, pName);
             }
             rs = ps.executeQuery();
             while (rs.next()) {
                 return new EditBan(rs.getInt("id"), rs.getString("name"), rs.getString("reason"),
                         rs.getString("admin"), rs.getLong("time"), rs.getLong("temptime"),
                         rs.getInt("type"), rs.getString("ip"));
             }
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
                 if (rs != null)
                     rs.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
         return null;
     }
 
     public boolean saveFullRecord(EditBan ban) {
 
         String mysqlTable = plugin.getConfig().getString("mysql-table");
 
         Connection conn = null;
         PreparedStatement ps = null;
         
         boolean success = false;
         try {
             conn = getSQLConnection();
             ps = conn
                     .prepareStatement("UPDATE "
                             + mysqlTable
                             + " SET name = ?, reason = ?, admin = ?, time = ?, temptime = ?, type = ? WHERE id = ? LIMIT 1");
             ps.setLong(5, ban.endTime);
             ps.setString(1, ban.name);
             ps.setString(2, ban.reason);
             ps.setString(3, ban.admin);
             ps.setLong(5, ban.time);
             ps.setLong(6, ban.type);
             ps.setInt(7, ban.id);
             ps.executeUpdate();
             success = ps.getUpdateCount() > 0;
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
         return success;
     }
 
     @Override
     public ArrayList<EditBan> getBannedPlayers() {
         ArrayList<EditBan> list = new ArrayList<EditBan>();
         Connection conn = getSQLConnection();
         String mysqlTable = plugin.getConfig().getString("mysql-table");
         if (conn == null) {
             FigAdmin.log.log(Level.SEVERE,
                     "[FigAdmin] Could not establish SQL connection. Disabling FigAdmin");
             plugin.getServer().getPluginManager().disablePlugin(plugin);
             return list;
         } else {
 
             PreparedStatement ps = null;
             ResultSet rs = null;
             try {
                 ps = conn.prepareStatement("SELECT * FROM " + mysqlTable
                         + " WHERE (type = 0 OR type = 1) AND (temptime > ? OR temptime = 0)");
                 ps.setLong(1, System.currentTimeMillis() / 1000);
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     EditBan e = getEditBan(rs);
                     list.add(e);
                 }
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ",
                         ex);
             } finally {
                 try {
                     if (ps != null)
                         ps.close();
                     if (rs != null)
                         rs.close();
                     if (conn != null)
                         conn.close();
                 } catch (SQLException ex) {
                     FigAdmin.log.log(Level.SEVERE,
                             "[FigAdmin] Failed to close MySQL connection: ", ex);
                 }
             }
 
             try {
                 conn.close();
                 FigAdmin.log.log(Level.INFO, "[FigAdmin] Initialized db connection");
             } catch (SQLException e) {
                 e.printStackTrace();
                 plugin.getServer().getPluginManager().disablePlugin(plugin);
             }
         }
         return list;
 
     }
 
     private EditBan getEditBan(ResultSet rs) throws SQLException {
         return new EditBan(rs.getInt("id"), rs.getString("name"), rs.getString("reason"),
                 rs.getString("admin"), rs.getLong("time"), rs.getLong("temptime"),
                 rs.getInt("type"), rs.getString("ip"));
     }
 
     @Override
     protected boolean deleteFullRecord(int id) {
 
         String mysqlTable = plugin.getConfig().getString("mysql-table");
 
         Connection conn = null;
         PreparedStatement ps = null;
         boolean success = false;
         try {
             conn = getSQLConnection();
             ps = conn.prepareStatement("DELETE FROM " + mysqlTable
                     + " WHERE id = ?  ORDER BY time DESC");
             ps.setInt(1, id);
             ps.executeUpdate();
             success = ps.getUpdateCount() > 0;
         } catch (SQLException ex) {
             FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Couldn't execute MySQL statement: ", ex);
             return false;
         } finally {
             try {
                 if (ps != null)
                     ps.close();
                 if (conn != null)
                     conn.close();
             } catch (SQLException ex) {
                 FigAdmin.log.log(Level.SEVERE, "[FigAdmin] Failed to close MySQL connection: ",
                         ex);
             }
         }
         return success;
     }
 
 }
