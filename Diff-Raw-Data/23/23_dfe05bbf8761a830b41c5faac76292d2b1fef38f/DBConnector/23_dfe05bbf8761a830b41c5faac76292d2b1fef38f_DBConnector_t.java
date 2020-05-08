 package com.dierkers.schedule.db;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
import com.dierkers.schedule.constants.Constants;

 public class DBConnector {
 	private Connection con;
 	private Statement stmt;
 	private String tableName = "public";
 
 	public DBConnector() {
 		try {
 			con = getConnection();
 		} catch (URISyntaxException e) {
 			System.err.println("Error connecting to the DB");
 			e.printStackTrace();
 		} catch (SQLException e) {
 			System.err.println("SQLException connecting to the DB");
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Query with the default FROM dbName.schedules appended
 	 * @param query the query to append to
 	 * @return the results of the query
 	 */
 	public ResultSet fquery(String query) {
 		return query(query + " FROM " + tableName + ".schedules");
 	}
 
 	public ResultSet query(String query) {
		if (Constants.isDebug()) {
 			System.out.println("Executing Query " + query);
 		}
 		try {
 			stmt = con.createStatement();
 			return stmt.executeQuery(query);
 		} catch (SQLException e) {
 			System.err.println("Error performing query: " + query);
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void update(String update) {
 		try {
 			stmt = con.createStatement();
 			stmt.executeUpdate(update);
 		} catch (SQLException e) {
 			System.err.println("Error performing update: " + update);
 			e.printStackTrace();
 		}
 	}
 
 	private Connection getConnection() throws URISyntaxException, SQLException {
 		String dbUriString = System.getenv("DATABASE_URL");
 		String dbName = dbUriString.substring(dbUriString.lastIndexOf("/") + 1);
 
 		URI dbUri = new URI(dbUriString);
 
 		String username = dbUri.getUserInfo().split(":")[0];
 		String password = dbUri.getUserInfo().split(":")[1];
 		String postfix = System.getenv("DATABASE_POSTFIX");
 		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + "/" + dbName
 				+ (postfix == null ? "" : "/?" + postfix);
 
 		return DriverManager.getConnection(dbUrl, username, password);
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 }
