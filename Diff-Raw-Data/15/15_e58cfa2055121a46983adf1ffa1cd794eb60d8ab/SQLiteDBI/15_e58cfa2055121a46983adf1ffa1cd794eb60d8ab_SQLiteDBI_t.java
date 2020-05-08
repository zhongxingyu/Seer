 package mavenp2versionmatch.db;
 
 import java.io.File;
 import java.sql.*;
 import java.util.Iterator;
 import java.util.Map;
 
 public class SQLiteDBI {
 	private Connection conn;
 	//TODO: standardize the dbName and tableName or load them from a config file
 	private static final String dbName = "my.db";
 	private static final String tableName = "maven_p2";
 	
 	public SQLiteDBI () throws SQLException {
 		
 		//make sure driver is loaded
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			throw new SQLException("SQLite Driver wasn't loaded");
 		}
 		//make sure DB exists
 		if(!new File(dbName).exists()) {
 			throw new SQLException("Database does not exist");
 		}
 		
 		conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
 		
 	}
 	
 	public void closeDB() throws SQLException {
 		if(!conn.isClosed()) {
 			conn.close();
 		}
 	}
 	
 	public void addRecord(Map<String, String> colMap) throws SQLException{
 		if(conn.isClosed())
 			throw new SQLException("Connection is closed, cannot add record");
 		
 		String colString = "";
 		String valString = "";
		Iterator<String> colIter = colMap.keySet().iterator();
 		
 		//Dynamically create the column string for inserting and
 		//add the proper amount of IN variables for the query
		while( colIter.hasNext()) {
			colString += colIter.next();
 			valString += "?"; //IN variable for statement
 			
			if( colIter.hasNext()) {
 				colString += ", ";
 				valString += ", ";
 			}
 		}
 		
 		String query = "INSERT INTO "+ tableName + "("+colString+")" + "VALUES ("+valString+");";
 		
 		PreparedStatement stmt = this.conn.prepareStatement(query);
 		
		Iterator<String> valueIter = colMap.values().iterator();
 		//add the values to the prepared statement
 		for( int i = 1; i < colMap.size() + 1; i++ ) {
			stmt.setString( i, valueIter.next());
 		}
 			
 		stmt.executeUpdate();
 	}
 }
