 // Copyright (C) 2011 Antony Derham <admin@districtmine.net>
 
 package net.districtmine.ant59.albus;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import com.mysql.jdbc.Driver;
 
 public class MySQL {
 	private final Albus m_Plugin;
 
 	private Connection MySQLConnection;
 	private Statement MySQLStatement;
 	@SuppressWarnings("unused")
 	private Driver MySQLDriver;
	private String MySQLUser, MySQLPass, MySQLHost, MySQLPort, MySQLDataBase, MySQLURL;
 
 	public MySQL(Albus instance, String user, String pass, String host,
 			String port, String db) {
 		m_Plugin = instance;
 		m_Plugin.consoleLog("Running database connection...");
 		try {
 			MySQLUser = user;
 			MySQLPass = pass;
 			MySQLHost = host;
 			MySQLPort = port;
 			MySQLDataBase = db;
 			MySQLURL = "jdbc:mysql://" + MySQLHost + ":" + MySQLPort + "/"
 					+ MySQLDataBase;
 			Class.forName("com.mysql.jdbc.Driver");
 			MySQLConnection = DriverManager.getConnection(MySQLURL, MySQLUser,
 					MySQLPass);
 			MySQLStatement = MySQLConnection.createStatement();
 			MySQLConnection.setAutoCommit(true);
 		} catch (Exception e) {
 			m_Plugin.consoleWarning("MySQL connection failed: " + e.toString());
 		} finally {
 		}
 	}
 
 	public Connection getConnection() {
 		return MySQLConnection;
 	}
 
 	public Statement getStatement() {
 		return MySQLStatement;
 	}
 
 	public void tryUpdate(String sqlString) {
 		try {
 			getStatement().executeUpdate(sqlString);
 		} catch (Exception e) {
 			m_Plugin.consoleWarning("The following statement failed: "
 					+ sqlString);
 			m_Plugin.consoleWarning("Statement failed: " + e.toString());
 		} finally {
 		}
 	}
 
 	public ResultSet trySelect(String sqlString) {
 		try {
 			System.out.println(getStatement().toString());
 			return getStatement().executeQuery(sqlString);
 		} catch (Exception e) {
 			m_Plugin.consoleWarning("The following statement failed: "
 					+ sqlString);
 			m_Plugin.consoleWarning("Statement failed: " + e.toString());
 		} finally {
 		}
 		return null;
 	}
 }
