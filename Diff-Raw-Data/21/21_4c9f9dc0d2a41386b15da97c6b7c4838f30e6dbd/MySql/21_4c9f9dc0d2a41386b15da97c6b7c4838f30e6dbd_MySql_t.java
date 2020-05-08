 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.dao.mysql;
 
 import java.sql.*;
 import java.util.logging.Level;
 import me.stutiguias.mcpk.Mcpk;
 import me.stutiguias.mcpk.MCPlayer;
 
 /**
  *
  * @author Daniel
  */
 public class MySql {
             
     private McpkConnectionPool pool;
     public String dbHost;
     public String dbPort;
     public String dbDatabase;
     
     public MySql(String dbHost, String dbUser, String dbPass, String dbPort, String dbDatabase) {
         try {
              this.dbHost = dbHost;
              this.dbDatabase = dbDatabase;
              this.dbPort = dbPort;
              pool = new McpkConnectionPool("com.mysql.jdbc.Driver", "jdbc:mysql://"+ dbHost +":"+ dbPort +"/"+ dbDatabase, dbUser, dbPass);
         }catch(Exception e) { }
     }
     
     public McpkConnection GetConnection() throws SQLException {
          try {
             return pool.getConnection();
         } catch (SQLException e) {
           //  core.getPluginLogger().severe("Unable to connect to the database: "+e.getMessage());
             return null;
         } 
     }
     
     private boolean tableExists(String tableName) {
 		boolean exists = false;
                 McpkConnection conn = null;
                 PreparedStatement st = null;
                 ResultSet rs = null;
 		try {
                         conn = GetConnection();
 			st = conn.prepareStatement("SHOW TABLES LIKE ?");
 			st.setString(1, tableName);
 			rs = st.executeQuery();
 			while (rs.next()) {
 				exists = true;
 			}
 		} catch (SQLException e) {
 			Mcpk.log.log(Level.WARNING,Mcpk.logPrefix + "Unable to check if table exists: {0}", tableName);
 			Mcpk.log.warning(e.getMessage());
 		} finally {
 			closeResources(conn, st, rs);
 		}
 		return exists;
     }
     
     private void executeRawSQL(String sql) {
             McpkConnection conn = null;
             Statement st = null;
             ResultSet rs = null;
 
             try {
                     conn = GetConnection();
                     st = conn.createStatement();
                     st.executeUpdate(sql);
             } catch (SQLException e) {
                     Mcpk.log.log(Level.WARNING,Mcpk.logPrefix + "Exception executing raw SQL {0}", sql);
                     Mcpk.log.warning(e.getMessage());
             } finally {
                     closeResources(conn, st, rs);
             }
     }
     
     private void closeResources(McpkConnection conn, Statement st, ResultSet rs) {
             if (null != rs) {
                     try {
                             rs.close();
                     } catch (SQLException e) {
                     }
             }
             if (null != st) {
                     try {
                             st.close();
                     } catch (SQLException e) {
                     }
             }
             if (null != conn) {
                    conn.close();
             }
     }
     
     public void InitTables() {
         if (!tableExists("MCPK_player")) {
 			Mcpk.log.info(Mcpk.logPrefix + "Creating table MCPK_player");
			executeRawSQL("CREATE TABLE MCPlayer (id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), pass VARCHAR(255), pkCount INTEGER, newbieCount TIMESTAMP);");
         }
     }
     
     public void createPlayer(String player, String pass, int pkCount, Timestamp newbieCount) {
             McpkConnection conn = null;
             PreparedStatement st = null;
             ResultSet rs = null;
 
             try {
                     conn = GetConnection();
                    st = conn.prepareStatement("INSERT INTO MCPlayer (name, pass, pkCount, newbieCount) VALUES (?, ?, ?, ?)");
                     st.setString(1, player);
                     st.setString(2, pass);
                     st.setDouble(3, pkCount);
                     st.setTimestamp(4, newbieCount);
                     st.executeUpdate();
             } catch (SQLException e) {
                     Mcpk.log.warning(Mcpk.logPrefix + "Unable to update player permissions in DB");
                     Mcpk.log.warning(e.getMessage());
             } finally {
                     closeResources(conn, st, rs);
             }
     }
         
     public MCPlayer getPlayer(String player) {
             MCPlayer _Player = null;
 
             McpkConnection conn = null;
             PreparedStatement st = null;
             ResultSet rs = null;
 
             try {
                     conn = GetConnection();
                    st = conn.prepareStatement("SELECT name, pass, pkCount, newbieCount FROM MCPlayer WHERE name = ?");
                     st.setString(1, player);
                     rs = st.executeQuery();
                     while (rs.next()) {
                             _Player = new MCPlayer();
                             _Player.setName(rs.getString("name"));
                             _Player.setKills(rs.getInt("pkCount"));
                             _Player.setNewBie(rs.getTimestamp("newbieCount"));
                     }
             } catch (SQLException e) {
                     Mcpk.log.log(Level.WARNING,Mcpk.logPrefix + "Unable to get player {0}", player);
                     Mcpk.log.warning(e.getMessage());
             } finally {
                     closeResources(conn, st, rs);
             }
             return _Player;
     }
     
     public boolean UpdateKill(String player,Integer kill) {
         	McpkConnection conn = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
                         conn = GetConnection();
			st = conn.prepareStatement("UPDATE MCPlayer SET pkCount = ? WHERE name = ?");
 			st.setInt(1, kill);
 			st.setString(2, player);
 			st.executeUpdate();
                         return true;
 		} catch (SQLException e) {
 			Mcpk.log.log(Level.WARNING, "{0} Unable to update item quantity in DB", Mcpk.logPrefix);
 			Mcpk.log.warning(e.getMessage());
                         return false;
 		} finally {
 			closeResources(conn, st, rs);
 		}
     }
 }
