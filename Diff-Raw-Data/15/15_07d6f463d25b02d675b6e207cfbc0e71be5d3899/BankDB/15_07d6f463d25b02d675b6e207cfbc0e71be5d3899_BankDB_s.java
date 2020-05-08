 /**
  * Database for Bank Plugin
  * @author Doug Frazer
  */
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.LinkedList;
 
 public class BankDB extends MySQLSource {
 
 	public LinkedList<BankItem> getBankItems (Player player) {
 		LinkedList<BankItem> items = new LinkedList<BankItem>();
 		Connection conn = null;
 		PreparedStatement prepped = null;
 		ResultSet results = null;
 		int i = 0;
 
 		try {
             conn = etc.getSQLConnection();
 			prepped = conn.prepareStatement("SELECT * FROM minebank WHERE player=? ORDER BY item ASC;");
 			prepped.setString(1, player.getName());
 			results = prepped.executeQuery();
 
 			while(results.next()) {
                 BankItem currItem = new BankItem();
 				currItem.item_id = results.getInt("item");
 				currItem.quantity = results.getInt("quantity");
                 currItem.player = player.getName();
                 items.add(currItem);
 			}
 
 		} catch (SQLException e) {
 			log.log(Level.SEVERE, "BankDB: Failed to execute SQL Query 1");
             log.log(Level.SEVERE, "BankDB: " + e);
 		} finally {
 			try {
 				if (prepped != null) prepped.close();
 				if (results != null) results.close();
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
 		}
 		return items;
 	}
 
 	public void addItemToBank (Player player, int item_id, int quantity) {
 		Connection conn = null;
 		PreparedStatement query = null;
 		PreparedStatement update = null;
 		ResultSet results = null;
 
 		try {
             conn = etc.getSQLConnection();
 		    query = conn.prepareStatement("SELECT * FROM minebank WHERE player=? AND item=?;");
 			query.setString(1, player.getName());
 			query.setInt(2, item_id);
 			results = query.executeQuery();
 
 			if(results.next()) {
 				update = conn.prepareStatement("UPDATE minebank SET quantity=? WHERE player=? AND item=? LIMIT 1;");
 				update.setInt(1, quantity+results.getInt("quantity"));
 				update.setString(2, player.getName());
 				update.setInt(3, item_id);
                 update.executeUpdate();
             } else {
                 // The item doesnt already exist for the player, so insert it
                 update = conn.prepareStatement("INSERT INTO minebank (player, item, quantity) VALUES (?,?,?);");
                 update.setString(1, player.getName());
                 update.setInt(2, item_id);
                 update.setInt(3, quantity);
                 update.executeUpdate();
             }
 		} catch (SQLException e) {
 			log.log(Level.SEVERE, "BankDB: Failed to addItemToBank for " + player.getName());
             log.log(Level.SEVERE, "BankDB: " + e);
 		} finally {
 			try {
 				if (update != null) update.close();
                 if (query != null) query.close();
 				if (results != null) results.close();
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
 		}
     }
     
     public int withdraw(Player player, int item_id, int amount) {
         Connection conn = null;
         PreparedStatement query = null;
         PreparedStatement update = null;
         ResultSet results = null;
 
         try {
             conn = etc.getSQLConnection();
             query = conn.prepareStatement("SELECT quantity FROM minebank WHERE player=? AND item=?;");
             query.setString(1, player.getName());
             query.setInt(2, item_id);
             results = query.executeQuery();
 
             if(results.next()) {
                 if(results.getInt("quantity") < amount) {
                     player.sendMessage("You only have " + results.getInt("quantity") + " of that item");
                     return 0;
                 } else if (results.getInt("quantity") == amount || amount == -1) {
                     update = conn.prepareStatement("DELETE FROM minebank WHERE player=? AND item=? AND quantity=? LIMIT 1;");
                     update.setString(1, player.getName());
                     update.setInt(2, item_id);
                     update.setInt(3, results.getInt("quantity"));
                     update.executeUpdate();
                     return results.getInt("quantity");
                 } else {
                     update = conn.prepareStatement("UPDATE minebank SET quantity=? WHERE player=? AND item=? LIMIT 1;");
                     update.setInt(1, results.getInt("quantity")-amount);
                     update.setString(2, player.getName());
                     update.setInt(3, item_id);
                     update.executeUpdate();
                     return amount;
                 }
             } else {
                 player.sendMessage("Could not find that item in your bank");
             }
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: Failed to withdraw item for player " +player.getName());
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
 			try {
 				if (update != null) update.close();
                 if (query != null) query.close();
 				if (results != null) results.close();
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
         }
     return 0;
     }
 
     public int getNumItemsInBank (Player player) {
 		Connection conn = null;
 		PreparedStatement query = null;
 		ResultSet results = null;
         int ret_val = 0;
         
         try {
             conn = etc.getSQLConnection();
             query = conn.prepareStatement("SELECT COUNT(DISTINCT item) as num_items FROM minebank WHERE player=?;");
             query.setString(1, player.getName());
             results = query.executeQuery();
 
             if(results.next()) {
                 ret_val = results.getInt("num_items");
             }
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: Failed to get item count for player " + player.getName());
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (query != null) query.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
         return ret_val;
     }
 
     public String getItemNameFromID(int item_id) {
         Connection conn = null;
         PreparedStatement query = null;
         ResultSet results = null;
         String ret_string = "";
 
         try {
             conn = etc.getSQLConnection();
             query = conn.prepareStatement("SELECT name FROM `items` WHERE itemid=?;");
             query.setInt(1, item_id);
             results = query.executeQuery();
 
             if(results.next()) {
                 ret_string = results.getString("name");
             }
 
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: Failed to get item name for itemid " + item_id);
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (query != null) query.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
         return ret_string;
     }
 
     public LinkedList<BankLocation> getBankLocations() {
         LinkedList<BankLocation> locations = new LinkedList<BankLocation>();
         Connection conn = null;
         PreparedStatement query = null;
         ResultSet results = null;
 
         try {
             conn = etc.getSQLConnection();
             query = conn.prepareStatement("SELECT * FROM banks;");
             results = query.executeQuery();
     
             while(results.next()) {
                 BankLocation newLoc = new BankLocation(results.getString("name"),
                                                    results.getDouble("x"),
                                                    results.getDouble("y"),
                                                    results.getDouble("z"));
                 newLoc.distance = results.getDouble("distance");
                 locations.add(newLoc);
             }
 
             return locations;
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: Failed to get list of banks");
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (query != null) query.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
         return null;
     }
 
     public boolean setBank(BankLocation loc) {
         Connection conn = null;
         PreparedStatement query = null;
         PreparedStatement update = null;
         ResultSet results = null;
         
 
         try { 
             conn = etc.getSQLConnection();
             query = conn.prepareStatement("SELECT * FROM banks WHERE name=?;");
             query.setString(1, loc.name);
             results = query.executeQuery();
 
             if(results.next()) {
                 return false;
             }
 
             update = conn.prepareStatement("INSERT INTO banks (x,y,z,distance,name) VALUES (?,?,?,?,?);");
             update.setDouble(1, loc.x);
             update.setDouble(2, loc.y);
             update.setDouble(3, loc.z);
             update.setDouble(4, loc.distance);
             update.setString(5, loc.name);
             update.executeUpdate();
 
             return true;
     
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: Failed to setBank " + loc.name);
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (query != null) query.close();
                 if (update != null) update.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
         return false;
     }
     
     public boolean removeBank(String name) {
         Connection conn = null;
         PreparedStatement query = null;
         PreparedStatement update = null;
         ResultSet results = null;
         
 
         try { 
             conn = etc.getSQLConnection();
             update = conn.prepareStatement("DELETE FROM banks WHERE name=?;");
             update.setString(1, name);
             update.executeUpdate();
 
             return true;
     
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: Failed to removeBank " + name);
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (query != null) query.close();
                 if (update != null) update.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
         return false;
     }
     /**
      * In development, still buggy
      * */
     public LinkedList<BankItem> total_item_list() {
         Connection conn = null;
         LinkedList<BankItem> items;
         PreparedStatement count_q = null;
         PreparedStatement item_q = null;
         ResultSet count_results = null;
         ResultSet item_results = null;
 
         try {
             conn = etc.getSQLConnection();
             count_q = conn.prepareStatement("SELECT COUNT(DISTINCT id) as num_items FROM `items-new`;");
             count_results = count_q.executeQuery();
             if(count_results.next())
                 items = new LinkedList<BankItem>();
             else
                 return null;
 
             item_q = conn.prepareStatement("SELECT id,name,code FROM `items-new` order by id asc;");
             item_results = item_q.executeQuery();
 
             while(item_results.next()) {
                 BankItem newItem = new BankItem();
                 newItem.item_id = item_results.getInt("id");
                 newItem.item_string = item_results.getString("name");
                 newItem.code = item_results.getInt("code");
                 items.add(newItem);
             }
             return items;
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (count_q != null) count_q.close();
                 if (item_q != null) item_q.close();
                 if (count_results != null) count_results.close();
                 if (item_results != null) item_results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) { }
         }
         return null;
     }
 
     public int max_code() {
         Connection conn = null;
         PreparedStatement count_q = null;
         ResultSet results = null;
 
         try {
             conn = etc.getSQLConnection();
             count_q = conn.prepareStatement("SELECT MAX(code) as max_code from `items-new`;");
             results = count_q.executeQuery();
 
             if(results.next()) return results.getInt("max_code");
             
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (count_q != null) count_q.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
         return 0;
     }
     
     public void setInventoryLock(Player p, boolean state) {
         Connection conn = null;
         PreparedStatement update = null;
         PreparedStatement query = null;
         ResultSet results = null;
 
         try {
             conn =  etc.getSQLConnection();
             query = conn.prepareStatement("SELECT * FROM users WHERE name=?;");
             query.setString(1, p.getName());
             results = query.executeQuery();
             if (!results.next()) {
                 // User does not exist in database, add it to the database
                 update = conn.prepareStatement("INSERT INTO users (name,inv_lock) VALUES (?,?);");
                 update.setString(1, p.getName());
                 if (state) update.setInt(2, 1);
                 else update.setInt(2, 0);
                 update.executeQuery();
                 return;
             } else {
                 update = conn.prepareStatement("UPDATE users SET inv_lock=? WHERE name=?");
                 if (state) update.setInt(1, 1);
                 else update.setInt(1, 0);
                 update.setString(2, p.getName());
                 update.executeUpdate();
             }
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (update != null) update.close();
                 if (query != null) query.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
 
     }
 
     public boolean getInventoryLock(Player p) {
         Connection conn = null;
         PreparedStatement query = null;
         ResultSet results = null;
 
         try {
             conn = etc.getSQLConnection();
             query = conn.prepareStatement("SELECT inv_lock FROM users WHERE name=?;");
             query.setString(1, p.getName());
             results = query.executeQuery();
             if(results.next()) {
                 int i = results.getInt("inv_lock");
                 if (i==1) return true;
                 else return false;
             }
         } catch (SQLException e) {
             log.log(Level.SEVERE, "BankDB: " + e);
         } finally {
             try {
                 if (query != null) query.close();
                 if (results != null) results.close();
                 if (conn != null) conn.close();
             } catch (SQLException e) {}
         }
 
     return false; 
     }
 
 }
