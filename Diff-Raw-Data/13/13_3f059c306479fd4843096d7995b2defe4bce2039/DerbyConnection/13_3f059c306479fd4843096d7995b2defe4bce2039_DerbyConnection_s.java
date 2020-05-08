 package com.georgep.jnotes.db;
 
 import java.io.Console;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import static com.georgep.jnotes.db.DbConstants.*;
 
 import javax.swing.JOptionPane;
 
 public class DerbyConnection {
 	
 	private Connection conn = null;
 	
 	/**
 	 * Connects to the database and creates the schema
 	 */
 	public DerbyConnection() {
 		createConnection();
 		createSchema();
 	}
 	
 	private void createSchema() {
 		try {
 			if(conn==null) throw new Exception();
 			DatabaseMetaData dmd = conn.getMetaData();
			ResultSet rs = dmd.getTables(null, "APP", NOTES_TABLE, null);
			if(!rs.next()) {
 				Statement stmt = conn.createStatement();
 				if(stmt==null) throw new Exception();
 				stmt.executeUpdate("CREATE TABLE " + NOTES_TABLE + 
 						"(" + ID_COLUMN + " int, " + TEXT_COLUMN +" CLOB)");
 				stmt.close();
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace(System.err);
 			JOptionPane.showMessageDialog(null, "Cannot create schema", 
 					"Error", JOptionPane.ERROR_MESSAGE);
 			System.exit(0);
 		}
 	}
 	
 	private void createConnection() {
 		try {
 			Class.forName(DbConstants.DB_DRIVER).newInstance();
 			conn = DriverManager.getConnection(DbConstants.DB_URL);
 		}
 		catch(Exception e) {
 			e.printStackTrace(System.err);
 			JOptionPane.showMessageDialog(null, "Cannot connect to the local database", 
 					"Error", JOptionPane.ERROR_MESSAGE);
 			System.exit(0);
 		}
 	}
 	
 	public Connection getConnection() {
 		return conn;
 	}
 	
 	public void closeConnection() {
 		try {
 			if(conn!=null) {
 				DriverManager.getConnection(DB_SHUTDOWN_URL);
 				conn.close();
 			}
 		}
 		catch(Exception e) {
			e.printStackTrace(System.err);
 		}
 	}
 
 }
