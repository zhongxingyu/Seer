 package com.scholastic.sbam.server.database.util;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class SqlExecution {
 	protected Connection	conn;
 	protected Statement		sqlStmt;
 	protected ResultSet		results;
 	
 	public SqlExecution() {
 		
 	}
 	
 	public SqlExecution(String sql) throws SQLException {
 		executeStatement(sql);	// Do nothing with the result... let the programmer get it later with the getter.
 	}
 	
 	/**
 	 * Execute a statement.
 	 * 
 	 * @param sql
 	 * The statement to execute.
 	 * @return
 	 * The result set.
 	 * @throws SQLException
 	 */
 	public ResultSet executeStatement(String sql) throws SQLException {
 		//	Close any previous result set and statement
 		closeResult();
 		
 		//	Create the connection, if not already present
 		if (conn == null)
 			conn   = HibernateUtil.getConnection();
 		
 		//	Create the new statement
		sqlStmt = conn.createStatement();
 		
 		//	Execute the query
 		try  {
 			results = sqlStmt.executeQuery(sql);
 		} catch (SQLException sqlExc) {
 			System.out.println(sql);
 			System.out.println(sqlExc.getMessage());
 			sqlExc.printStackTrace();
 			throw sqlExc;
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Close any open result set and statement
 	 * @throws SQLException
 	 */
 	public void closeResult() throws SQLException {
 		if (results != null)
 			results.close();
 		if (sqlStmt != null)
 			sqlStmt.close();
 	}
 		
 	/**
 	 * Close everything.
 	 * 
 	 * @throws SQLException
 	 */
 	public void close() throws SQLException {
 		closeResult();
 		
 		if (conn != null)
 			HibernateUtil.freeConnection(conn);	//	conn.close();
 	}
 
 	public Connection getConn() {
 		return conn;
 	}
 
 	public void setConn(Connection conn) {
 		this.conn = conn;
 	}
 
 	public Statement getSqlStmt() {
 		return sqlStmt;
 	}
 
 	public void setSqlStmt(Statement sqlStmt) {
 		this.sqlStmt = sqlStmt;
 	}
 
 	public ResultSet getResults() {
 		return results;
 	}
 
 	public void setResults(ResultSet results) {
 		this.results = results;
 	}
 	
 }
