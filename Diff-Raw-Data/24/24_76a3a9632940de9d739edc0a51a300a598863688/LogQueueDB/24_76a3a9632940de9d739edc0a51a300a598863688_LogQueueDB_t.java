 /**
  * 
  */
 package server.database;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.HashMap;
 
 import model.LogQueue;
 
 /**
  * @author Derek Carr
  *
  */
 public class LogQueueDB {
 	private Database db;
 	/**
 	 * 
 	 */
 	public LogQueueDB(Database db) {
 		this.db = db;
 	}
 
 	/**
 	 * 
 	 * @param logQueue
 	 * @return int of log_queue id just inserted
 	 */
 	public int insertLogQueue(LogQueue logQueue) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		int id = 0;
 		try {
 			String sql = "INSERT INTO log_queue(vm_batch_id, vm_job_id, "+
 					"testcase_id, subtest_id, " +
           "cloud_id, browser_id, os_id, " +
           "hostname, num_tests, " +
           "num_errors, num_failures, "+
           "username, url, environment, git_branch, git_commit_version,"+
          "time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
 			stmt = db.getConnection().prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
 			
 			stmt.setInt(1, logQueue.getVmBatchId());
 			stmt.setInt(2, logQueue.getVmJobId());
 			stmt.setInt(3, logQueue.getTestCaseId());
 			stmt.setInt(4, logQueue.getSubTestId());
 			stmt.setInt(5, logQueue.getVmCloudId());
 			stmt.setInt(6, logQueue.getBrowserId());
 			stmt.setInt(7, logQueue.getOsId());
 			stmt.setString(8, logQueue.getHostname());
 			stmt.setInt(9, logQueue.getNumTests());
 			stmt.setInt(10, logQueue.getNumErrors());
 			stmt.setInt(11, logQueue.getNumFailures());
 			stmt.setString(12, logQueue.getUsername());
 			stmt.setString(13, logQueue.getUrl());
 			stmt.setString(14, logQueue.getEnvironment());
 			stmt.setString(15, logQueue.getGitBranch());
 			stmt.setString(16, logQueue.getGitCommitVersion());
 			stmt.setDouble(17, logQueue.getTime());
 			
 			int rowCount = stmt.executeUpdate();
 			 if (rowCount == 0) {
          throw new SQLException("Creating user failed, no rows affected.");
 			}
 			rs = stmt.getGeneratedKeys();
 			if (rs.next()) {
 				id = rs.getInt(1); 
 			} else {
 				System.out.println("ERROR GETTING ID");
 			}
 		} catch (SQLException e) {
 			System.out.println("Insert LogQueue Failed");
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
 	
 	
 	/**
 	 * 
 	 * @param batchId
 	 * @return
 	 */
 	public int numFailuresPerBatch(int batchId) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		int numFailures = 0;
 		try {
 			String sql = "SELECT SUM(num_failures) FROM log_queue WHERE vm_batch_id = ?";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, batchId);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				numFailures = rs.getInt(1);
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
 		return numFailures;
 	}
 	
 	/**
 	 * 
 	 * @param batchId
 	 * @return
 	 */
 	public int numErrorsPerBatch(int batchId) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		int numErrors = 0;
 		try {
 			String sql = "SELECT SUM(num_errors) FROM log_queue WHERE vm_batch_id = ?";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, batchId);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				numErrors = rs.getInt(1);
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
 		return numErrors;
 	}
 	
 	/**
 	 * 
 	 * @param batchId
 	 * @return
 	 */
 	public HashMap<Integer, LogQueue> getListByBatch(int batchId) {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		HashMap<Integer, LogQueue> queue = new HashMap<Integer, LogQueue>();
 		try {
 			String sql = "SELECT * FROM log_queue WHERE vm_batch_id = ?";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, batchId);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				int id = rs.getInt(1);
 				int vmBatchId = rs.getInt(2);
 				int jobId = rs.getInt(3);
 				int testCaseId = rs.getInt(4);
 				int subtestId = rs.getInt(5);
 				int cloud = rs.getInt(6);
 				int browser = rs.getInt(7);
 				int os = rs.getInt(8);
 				String hostname = rs.getString(9);
 				int numTests = rs.getInt(10);
 				int numErrors = rs.getInt(11);
 				int numFailures = rs.getInt(12);
 				String username = rs.getString(13);
 				String url = rs.getString(14);
 				String env = rs.getString(15);
 				String gitBranch = rs.getString(16);
 				String gitVersion = rs.getString(17);
 				double time = rs.getDouble(18);
 				Timestamp stamp = rs.getTimestamp(19);
 				
 				LogQueue log = new LogQueue(id, vmBatchId, jobId, testCaseId, subtestId, cloud, browser, os, hostname, numTests, 
 						numFailures, numErrors, username, url, env, gitBranch, gitVersion, time, stamp);
 				queue.put(id,log);
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
 		return queue;
 	}
 	
 }
