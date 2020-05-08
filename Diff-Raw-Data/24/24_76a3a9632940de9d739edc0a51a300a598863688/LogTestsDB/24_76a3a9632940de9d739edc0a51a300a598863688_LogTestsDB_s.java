 /**
  * 
  */
 package server.database;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import model.LogTest;
 
 /**
  * @author Derek Carr
  *
  */
 public class LogTestsDB {
 	private Database db;
 	
 	/**
 	 * @param database
 	 */
 	public LogTestsDB(Database database) {
 		this.db = database;
 	}
 
 	public int insert(LogTest logTest) {
 		PreparedStatement stmt = null; 
		Statement keyStmt = null;
 		ResultSet rs = null;
 		int id = -1;
 		try {
 			String sql = "INSERT INTO log_tests(log_queue_id, test_classname, test_name, test_time) VALUES(?, ?, ?, ?);";
			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, logTest.getLogQueueId());
 			stmt.setString(2, logTest.getTestClassname());
 			stmt.setString(3, logTest.getTestName());
 			stmt.setDouble(4, logTest.getTime());
			if (stmt.executeUpdate() == 1) {
				keyStmt = db.getConnection().createStatement();
				rs = keyStmt.executeQuery("select last_insert_rowid()");
				rs.next();
 				id = rs.getInt(1); 
 			} else {
 				System.out.println("ERROR GETTING ID");
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
 		return id;
 		
 	}
 }
