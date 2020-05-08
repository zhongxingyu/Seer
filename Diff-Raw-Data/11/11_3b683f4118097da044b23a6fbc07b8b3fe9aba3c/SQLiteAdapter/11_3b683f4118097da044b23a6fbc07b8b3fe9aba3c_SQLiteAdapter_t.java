 package edu.cmu.cs214.hw9.db;
 
 import java.sql.*;
 
 public class SQLiteAdapter {
 	protected Connection conn;
 	
 	public SQLiteAdapter() throws Exception {
 		Class.forName(Constants.JDBC_NAME);//loads the driver
 		conn = DriverManager.getConnection("jdbc:sqlite:214hw9.db");//connects to the database. 
 		//214hw9.db is the name of the database in this case, can be changed to anything *.db
 	}
 	
 	public void createTables(){
 		Statement stat;
 		try {
 			
 			// Create the four tables: users, posts, friends, and subscriptions
 			stat = conn.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + Constants.USERS_TABLE + " (email varchar(50) PRIMARY KEY, name varchar(50), password varchar(50));");
 			
 			stat = conn.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + Constants.POSTS_TABLE + " (email varchar(50) PRIMARY KEY, content varchar(200), is_post boolean, date_added integer);");
 			
 			stat = conn.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + Constants.FRIENDS_TABLE + " (email1 varchar(50) PRIMARY KEY, email2 varchar(50));");
 			
 			stat = conn.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + Constants.SUBSCRIPTIONS_TABLE + " (email1 varchar(50) PRIMARY KEY, email2 varchar(50));");	
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public ResultSet select(String statement) {
 		try {
 			
 			Statement st = conn.createStatement();
 			return st.executeQuery(statement);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}	
 		return null; 	
 	}
 }
