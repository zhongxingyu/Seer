 package com.volkan.db;
 
 import java.io.StringReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.volkan.Utility;
 
 public class H2Helper {
 
 	private static final Logger logger = LoggerFactory.getLogger(H2Helper.class);
 	
 	private static String table = " NEORESULTS ";
 	private Connection con;
 
 	public void updateJobWithResults(long jobID, String cypherResult) throws SQLException {
 		String sql = "UPDATE " +table+ " SET VRESULT = ? ,FINISHED_AT=? WHERE ID = ?";
 		PreparedStatement prepStatement = null;
 		try {
 			prepStatement = con.prepareStatement(sql);
 			prepStatement.setClob(1, new StringReader(cypherResult));
 			prepStatement.setTimestamp(2, new java.sql.Timestamp(new Date().getTime()));
 			prepStatement.setLong(3, jobID);
 			int affectedRows = prepStatement.executeUpdate();
 			if (affectedRows == 0) {
 				throw new SQLException("Creating job failed, no rows affected.");
 			}
 		} catch (Exception e) {
			logger.error("Job could not be updated => "+e.getMessage());
 			throw new SQLException("Job could not be updated", e);
 		}
 		
 		closePreparedStatement(prepStatement);
 	}
 
 	public void updateParentOfJob(long jobID) throws SQLException {
 		String sql = "UPDATE " +table+ " SET PARENT_ID = ? WHERE ID = ?";
 		PreparedStatement prepStatement = con.prepareStatement(sql);
 		prepStatement.setLong(1, jobID);
 		prepStatement.setLong(2, jobID);
 		int affectedRows = prepStatement.executeUpdate();
 		if (affectedRows == 0) {
 			throw new SQLException(
 					"Updating parent of job failed, no rows affected.");
 		}
 		
 		closePreparedStatement(prepStatement);
 	}
 
 	public void updateJobMarkAsDeleted(long jobID) throws SQLException {
 		String sql = "UPDATE " +table+ " SET IS_DELETED = TRUE WHERE ID = ?";
 		PreparedStatement prepStatement = con.prepareStatement(sql);
 		prepStatement.setLong(1, jobID);
 		int affectedRows = prepStatement.executeUpdate();
 
 		closePreparedStatement(prepStatement);
 		
 		if (affectedRows == 0) {
 			throw new SQLException(
 					"Updating job mark as deleted failed, no rows affected.");
 		}
 	}
 
 	public void updateJobsMarkAsDeleted(List<Long> jobIDs) throws SQLException {
 		StringBuilder sql = new StringBuilder("UPDATE " +table+ " SET IS_DELETED = TRUE WHERE ");
 		sql.append(buildWhereClauseForIDList(jobIDs));
 		
 		PreparedStatement prepStatement = con.prepareStatement(sql.toString());
 		
 		for (int i = 0; i < jobIDs.size(); i++) {
 			prepStatement.setLong(i + 1, jobIDs.get(i));
 		}
 		
 		int affectedRows = prepStatement.executeUpdate();
 
 		closePreparedStatement(prepStatement);
 		
 		if (affectedRows == 0) {
 			throw new SQLException(
 					"Updating job mark as deleted failed, no rows affected.");
 		}
 	}
 
 	protected String buildWhereClauseForIDList(List<Long> jobIDs) throws SQLException {
 		StringBuilder sb = new StringBuilder();
 		
 		if (jobIDs.isEmpty()) {
 			throw new SQLException("An empty jobIDs list is passed!");
 		} else {//List holds more than 1 element
 			sb.append(" (ID = ?");
 			for (int i = 0; i < jobIDs.size() - 1; i++) {
 				sb.append(" OR ID = ?");
 			}
 			sb.append(") ");
 		}
 		
 		return sb.toString();
 	}
 	
 	public long generateJob(long parentJobID, String traversalQuery) throws SQLException {
 		ResultSet resultSet = null;
 		PreparedStatement prepStatement = null;
 		long generatedJobID = 0;
 
 		// prepared statement
 		prepStatement = con.prepareStatement("INSERT INTO " + table
 				+ " (PARENT_ID, VQUERY, CREATED_AT) VALUES (?,?,?)");
 		prepStatement.setLong(1, parentJobID);
 		prepStatement.setString(2, traversalQuery);
 		prepStatement.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
 		int affectedRows = prepStatement.executeUpdate();
 		if (affectedRows == 0) {
 			throw new SQLException("Creating job failed, no rows affected.");
 		}
 
 		resultSet = prepStatement.getGeneratedKeys();
 		if (resultSet.next()) {
 			generatedJobID = resultSet.getLong(1);
 		} else {
 			throw new SQLException(
 					"Creating job failed, no generated key obtained.");
 		}
 
 		closeResultSet(resultSet);
 		prepStatement.close();
 		
 		return generatedJobID;
 	}
 	
 	public VJobEntity fetchJob(long jobID) throws SQLException {
 		VJobEntity vJobEntity = new VJobEntity();
 		
 		String sql = "SELECT * FROM " + table + " WHERE ID = ?";
 		PreparedStatement preparedStatement = con.prepareStatement(sql);
 		preparedStatement.setLong(1, jobID);
 		ResultSet rs = preparedStatement.executeQuery();
 		if (rs.next()) {
 			vJobEntity.id = rs.getLong("ID");
 			vJobEntity.is_deleted 	= rs.getBoolean("IS_DELETED");
 			vJobEntity.parent_id 	= rs.getLong("PARENT_ID");
 			vJobEntity.vquery 		= rs.getString("VQUERY");
 			vJobEntity.vresult 		= rs.getString("VRESULT");
 			vJobEntity.created_at	= rs.getTimestamp("CREATED_AT");
 			vJobEntity.finished_at	= rs.getTimestamp("FINISHED_AT");
 		} else {
 			throw new SQLException("No job found with ID = " + jobID);
 		}
 		
 		closeResultSet(rs);
 		closePreparedStatement(preparedStatement);
 		
 		return vJobEntity;
 	}
 	
 	public List<VJobEntity> fetchJobNotDeletedWithParentID(long parentID) throws SQLException {
 		
 		List<VJobEntity> results = new ArrayList<VJobEntity>();
 		
 		String sql = "SELECT * FROM " + table + " WHERE PARENT_ID = ? AND IS_DELETED = FALSE";
 		PreparedStatement preparedStatement = con.prepareStatement(sql);
 		preparedStatement.setLong(1, parentID);
 		ResultSet rs = preparedStatement.executeQuery();
 		while (rs.next()) {
 			VJobEntity vJobEntity = new VJobEntity();
 			vJobEntity.id = rs.getLong("ID");
 			vJobEntity.is_deleted 	= rs.getBoolean("IS_DELETED");
 			vJobEntity.parent_id 	= rs.getLong("PARENT_ID");
 			vJobEntity.vquery 		= rs.getString("VQUERY");
 			vJobEntity.vresult 		= rs.getString("VRESULT");
 			vJobEntity.created_at	= rs.getTimestamp("CREATED_AT");
 			vJobEntity.finished_at	= rs.getTimestamp("FINISHED_AT");
 			results.add(vJobEntity);
 		} 
 		
 		closeResultSet(rs);
 		closePreparedStatement(preparedStatement);
 		return results;
 	}
 	
 	public int deleteAll() throws SQLException {
 		int affectedRows = 0;
 		
 		String sql = "DELETE FROM " + table;
 		PreparedStatement ps = con.prepareStatement(sql);
 		affectedRows = ps.executeUpdate();
 		
 		return affectedRows;
 	}
 
 	public H2Helper() throws ClassNotFoundException, SQLException {
 		con = getConnection();
 	}
 	
 	public H2Helper(String tableName) throws ClassNotFoundException, SQLException {
 		this();
 		table = tableName;
 	}
 
 	private Connection getConnection() throws ClassNotFoundException, SQLException {
 		// driver for H2 db get from http://www.h2database.com
 		Class.forName("org.h2.Driver");
 
 		// Connection con = DriverManager.getConnection("jdbc:h2:mem:mytest", "sa", "");
 //		Connection con = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test", "sa","");
 		String defaultConnectionString = "jdbc:h2:tcp://localhost/~/test";
 		String connectionString = 
 					Utility.getValueOfProperty("H2connectionstring", defaultConnectionString);
 		
 		String userName = Utility.getValueOfProperty("H2user", "sa");
 		String pwd 		= Utility.getValueOfProperty("H2pwd", "");
 //		logger.info("H2 Connection: {} {} {}", connectionString, userName, pwd);
 		Connection con = DriverManager.getConnection(connectionString, userName, pwd);
 		return con;
 	}
 
 	private void closePreparedStatement(PreparedStatement prepStatement) throws SQLException {
 		if(prepStatement != null)
 			prepStatement.close();
 	}
 
 	private void closeResultSet(ResultSet rs) throws SQLException {
 		if(rs != null)
 			rs.close();
 	}
 	
 	public void closeConnection() throws SQLException {
 		con.close();
 	}
 
 }
 
 // insert 10 row data
 //for (int i = 0; i < 10; i++) {
 //	prep.setLong(1, i);
 //	prep.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
 //	prep.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
 //	prep.setString(4, "Activity-" + i);
 //
 //	// batch insert
 //	prep.addBatch();
 //}
 //con.setAutoCommit(false);
 //prep.executeBatch();
 //con.setAutoCommit(true);
 //
 //// query to database
 //try {
 //	ResultSet rs = stat
 //			.executeQuery("Select STARTTIME, ENDTIME, ACTIVITY_NAME from ACTIVITY");
 //	while (rs.next()) {
 //
 //		Date start = rs.getTimestamp(1);
 //		Date end = rs.getTimestamp(2);
 //		String activityName = rs.getString(3);
 //
 //		// print query result to console
 //		System.out.println("activity: " + activityName);
 //		System.out.println("start: " + start);
 //		System.out.println("end: " + end);
 //		System.out.println("--------------------------");
 //	}
 //	rs.close();
 //} catch (SQLException e) {
 //	e.printStackTrace();
 //}
