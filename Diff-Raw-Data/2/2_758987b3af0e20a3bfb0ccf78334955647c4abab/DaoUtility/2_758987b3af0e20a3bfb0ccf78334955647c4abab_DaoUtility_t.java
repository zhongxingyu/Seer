 package com.criticcomrade.etl.query.db;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 
 public class DaoUtility {
     
     private static String userName;
     private static String password;
     private static String serverName;
     private static String portNumber;
     private static String database;
     
     private static void loadProperties() {
 	Scanner in = null;
 	try {
 	    in = new Scanner(new File("db.properties"));
 	} catch (FileNotFoundException e) {
	    throw new RuntimeException("Unable to find db.properties file, expecting it at " + System.getProperty("user.dir") + "\\");
 	}
 	
 	Map<String, String> props = new HashMap<String, String>();
 	while (in.hasNext()) {
 	    String[] items = in.nextLine().split("=");
 	    props.put(items[0], items[1]);
 	}
 	
 	userName = props.get("username");
 	password = props.get("password");
 	serverName = props.get("servername");
 	portNumber = props.get("serverport");
 	database = props.get("database");
 	
     }
     
     public static Connection getConnection() throws SQLException {
 	
 	loadProperties();
 	Properties connectionProps = new Properties();
 	connectionProps.put("user", userName);
 	connectionProps.put("password", password);
 	
 	Connection conn = DriverManager.getConnection("jdbc:mysql://" + serverName + ":" + portNumber + "/" + database, connectionProps);
 	
 	return conn;
     }
     
 }
