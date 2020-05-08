 /**
  * 
  */
 package server.database;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import model.Batch;
 
 /**
  * @author Derek Carr
  *
  */
 public class BatchDB {
 	private Database db;
 	
 	/**
 	 * 
 	 * @param db
 	 */
 	public BatchDB(Database db) {
 		this.db = db;
 	}
 	
 	public Batch getBatch(int id) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Batch batch = null;
 		try {
 			String sql = "SELECT * FROM vm_batch WHERE id = ?";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, id);
 			rs = stmt.executeQuery();
 			while(rs.next()) {
 				int bid = rs.getInt(1);
 				int numJobs = rs.getInt(2);
 				String email = rs.getString(3);
 				double timeEst = rs.getDouble(4);
 				double timeAct = rs.getDouble(5);
 				boolean completed = rs.getBoolean(6);
 				Timestamp createdDate = rs.getTimestamp(7);
 				Timestamp modifiedDate = rs.getTimestamp(8);
 				
 				batch = new Batch(bid, numJobs, email, timeEst, timeAct, completed, createdDate, modifiedDate);
 			}
			sql = "SELECT sum(num_tests) FROM log_queue WHERE vm_batch_id = ?";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, id);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				int numberTests = rs.getInt(1);
 				batch.setNumberTests(numberTests);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (stmt != null) { 
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return batch;
 	}
 	
 	public void setBatchCompleted(int batchId) {
 		PreparedStatement stmt = null;
 		try {
 			String sql = "UPDATE vm_batch SET completed = 1 WHERE id = ?";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, batchId);
 			stmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public boolean isBatchComplete(int batchId) {
   	PreparedStatement stmt = null;
   	ResultSet rs = null;
   	boolean complete = true;
   	try {
 			String sql = "SELECT completed FROM vm_batch WHERE id = ?;";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, batchId);
 			
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				if (!rs.getBoolean("completed")) {
 					complete = false;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			} 
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
   	return complete;
   }
 }
