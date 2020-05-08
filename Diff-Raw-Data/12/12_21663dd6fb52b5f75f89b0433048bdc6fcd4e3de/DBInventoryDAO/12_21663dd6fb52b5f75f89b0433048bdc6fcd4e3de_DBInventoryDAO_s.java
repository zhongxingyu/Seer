 package adapters.db.sqlite.inventory;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import adapters.db.sqlite.upcMap.UPCEntry;
 
 public class DBInventoryDAO extends InventoryDAO {	
 	private Connection conn = null;
 	private static final String mapName = Config.Statics.InventoryMapName;
 	private static final String inventoryName = Config.Statics.InventoryEntryName;
 	
 	// path by default should be db/test.db
 	//TODO: stop throwing exceptions and gracefully handle them everywhere
 	public DBInventoryDAO(String path) throws ClassNotFoundException, SQLException{		
 		Class.forName(Config.Statics.JDBCPath);
 		conn = DriverManager.getConnection(path);
 	}
 	
 	public ArrayList<InventoryEntry> lookUp(long start, long end) {
 		PreparedStatement ps = null;
 		try {
 			String select = "SELECT " + mapName + ".id, " + mapName + ".upc, " + mapName + ".name, " + mapName + ".amount, " + inventoryName + ".created " +
 							"FROM " + mapName + ", " + inventoryName + 
 							" WHERE " + mapName + ".id=" + inventoryName + ".upc_id AND " + inventoryName + ".created > ? AND " + inventoryName + ".created < ?";
 			ps = conn.prepareStatement(select);
 			ps.setLong(1, start);
 			ps.setLong(2, end);
 			ResultSet rs = ps.executeQuery();
 			ArrayList<InventoryEntry> out = new ArrayList<InventoryEntry>();
 			while(rs.next()) {
 				out.add(new InventoryEntry(new UPCEntry(Integer.parseInt(rs.getString(1)), rs.getString(2), rs.getString(3), rs.getString(4)), rs.getString(5)));
 			}
 			return out;
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
 	
 	public boolean addEntry(InventoryEntry entry) {
 		if(entry == null) throw new NullPointerException();
 		
 		PreparedStatement ps = null;
 		int count = 0;
 		
 		try {
 			conn.setAutoCommit(false);
 			String insert = "INSERT INTO " + inventoryName + " (upc_id, created) VALUES (?, ?)";
 			ps = conn.prepareStatement(insert);
 			ps.setString(1, Integer.toString(entry.getUPC().getID()));
			ps.setString(2, entry.getCreated());
 			count = ps.executeUpdate();
 			
 			conn.commit();
 		}
 		catch(SQLException ex) {
 			ex.printStackTrace();
 			
 	        if (conn != null) {
 	            try {
 	                System.out.println("Transaction is being rolled back");
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
 	       
 	        try {
 				conn.setAutoCommit(true);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
 		return (count != 0);
 	}
 	
 	public boolean removeEntry(UPCEntry upc) {
 		PreparedStatement select = null;
         PreparedStatement remove = null;
 		boolean success = false;
         
 		try {
 			//conn.setAutoCommit(false);
 			String statement = "SELECT * FROM " + inventoryName + " WHERE upc_id=? ORDER BY created DESC";
             select = conn.prepareStatement(statement);
             select.setInt(1, upc.getID());
             
             ResultSet results = select.executeQuery();
             
             results.next();
             
             statement = "DELETE FROM " + inventoryName + " WHERE created=?";
             remove = conn.prepareStatement(statement);
             remove.setInt(1, results.getInt(3));
             
             success = remove.execute();
            
 			//conn.commit();
 		}
 		catch(SQLException ex) {
 			ex.printStackTrace();
 			
 	        if (conn != null) {
 	            try {
 	                System.out.println("Transaction is being rolled back");
 	                conn.rollback();
 	            } catch(SQLException excep) {
 	                excep.printStackTrace();
 	            }
 	        }
 	    } finally {
 	        if (select != null) {
 	            try {
 					select.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        }
             
             if (remove != null) {
 	            try {
 					remove.close();
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
 		return success;
 	}
 }
