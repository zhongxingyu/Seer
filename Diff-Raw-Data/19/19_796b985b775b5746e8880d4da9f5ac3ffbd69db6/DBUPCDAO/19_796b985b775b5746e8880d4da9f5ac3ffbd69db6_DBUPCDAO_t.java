 package adapters.db.sqlite.upcMap;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class DBUPCDAO extends UPCDAO  {
 	private static DBUPCDAO singleton = null;
 	
 	private Connection conn = null;
 	public static final String mapName = Config.Statics.UPCMapName;
 	public static final String customName = Config.Statics.UPCCustomName;
 	
 	// path by default should be db/test.db
 	public DBUPCDAO(String path) throws ClassNotFoundException, SQLException {		
 		Class.forName(Config.Statics.JDBCPath);
 		conn = DriverManager.getConnection(path);
 	}
 	
 	public synchronized UPCEntry lookUp(String upc) {
 		PreparedStatement ps = null;
 		try {
 			String select = "SELECT * FROM " + mapName + " WHERE upc=?";
 			ps = conn.prepareStatement(select);
 			ps.setString(1, upc);
 			ResultSet rs = ps.executeQuery();
 			while(rs.next()) {
 				return new UPCEntry(Integer.parseInt(rs.getString(1)), rs.getString(2), rs.getString(3), rs.getString(4));
 			}
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		finally {
 			if(ps != null) {
 		        try {
 					ps.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		    }
 		}
 		return null;
 	}
 	
 	//TODO make source enum?
 	public synchronized boolean addEntry(UPCEntry entry, String source){
		if(entry == null || source == null || !(source == "user" || source == "online")){
 			throw new IllegalArgumentException();
 		}else if (lookUp(entry.getUPC()) != null){
 			return false;
 		}
 		
 		PreparedStatement ps = null;
 		PreparedStatement ps2 = null;
 		int count = 0;
 		int count2 = 0;
 		try {
 			conn.setAutoCommit(false);
 			String insert = "INSERT INTO " + mapName + " (upc, name, amount) VALUES (?, ?, ?)";
 			ps = conn.prepareStatement(insert);
 			ps.setString(1, entry.getUPC());
 			ps.setString(2, entry.getItemName());
 			ps.setString(3, entry.getAmount());
 			count = ps.executeUpdate();
 						
 			String select = "INSERT INTO " + customName + " (upc_id, source, created) VALUES ((select last_insert_rowid()), ?, ?)";
 			ps2 = conn.prepareStatement(select);
 			ps2.setString(1, source);
 			ps2.setString(2, Long.toString( System.currentTimeMillis() / 1000));
 			count2 = ps2.executeUpdate();
 			
 			conn.commit();
 		}
 		catch(SQLException ex) {
 			ex.printStackTrace();
 			
 	        if (conn != null) {
 	            try {
 	                System.out.println(
 	                    "Transaction is being rolled back");
 	                conn.rollback();
 	            } catch(SQLException excep) {
 	                excep.printStackTrace();
 	            }
 	        }
 	    } finally {
 	        if (ps != null) {
 	            try {
 					ps.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        }
 	        if (ps2 != null) {
 	            try {
 					ps2.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        }
 	        try {
 				conn.setAutoCommit(true);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
 		return (count != 0 && count2 != 0);
 	}
 	
 	public synchronized boolean removeEntry(String entry){
 		PreparedStatement ps1 = null;
         PreparedStatement ps2 = null;
         boolean success1 = false;
         boolean success2 = false;
         
         try {
 			UPCEntry delete = lookUp(entry);
             if(delete == null){
                 return true;
             }
             
             String statement;
             conn.setAutoCommit(false);
             
             statement = "DELETE FROM " + mapName + " WHERE upc=? AND name=? AND amount=?";
             ps1 = conn.prepareStatement(statement);
             ps1.setString(1, delete.getUPC());
 			ps1.setString(2, delete.getItemName());
 			ps1.setString(3, delete.getAmount());
             
             success1 = ps1.execute();
             
             statement = "DELETE FROM " + customName + " WHERE upc_id=?";
             ps2 = conn.prepareStatement(statement);
             ps2.setInt(1, delete.getID());
             
             success2 = ps2.execute();
             
             conn.commit();
         } catch (SQLException e){
             e.printStackTrace();
 			
 	        if (conn != null) {
 	            try {
 	                System.out.println(
 	                    "Transaction is being rolled back");
 	                conn.rollback();
 	            } catch(SQLException excep) {
 	                excep.printStackTrace();
 	            }
 	        }
         } finally {
             if (ps1 != null) {
 	            try {
 					ps1.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        }
 	        if (ps2 != null) {
 	            try {
 					ps2.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        }
 	        try {
 				conn.setAutoCommit(true);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         }
 		return (success1 && success2);
 	}
 }
