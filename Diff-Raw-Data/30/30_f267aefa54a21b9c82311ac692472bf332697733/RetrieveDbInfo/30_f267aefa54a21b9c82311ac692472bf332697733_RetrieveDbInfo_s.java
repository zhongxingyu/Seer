 package com.WebQ.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 import com.WebQ.beans.User;
 
 public class RetrieveDbInfo implements RetrieveDbInfoImpl {
 
     Statement statement;
     Connection connection;
     private ResultSet resultSet;
 
     public static void main(String[] args) {
 	RetrieveDbInfo rb = new RetrieveDbInfo();
 	rb.init();
 	User user = rb.getUser("teja");
 	System.out.println(user.getUserId());
 	System.out.println(user.getFirstName());
 	System.out.println(user.getLastName());
 	System.out.println(user.getEmailId());
     }
 
     public RetrieveDbInfo() {
 	init();
     }
 
     public boolean containsUser(String userId) {
 	if (getUser(userId) == null) {
 	    return false;
 	} else {
 	    return true;
 	}
     }
 
     @Override
     public void init() {
 	try {
 	    Class.forName(DbConstants.DATABASE_DRIVER);
 	    connection = DriverManager.getConnection(
 		    DbConstants.CONNECTION_URL, DbConstants.DB_USER_NAME,
 		    DbConstants.DB_PASSWORD);
 	    statement = connection.createStatement();
 	} catch (Exception e) {
 	    Logger.getLogger(RetrieveDbInfo.class).error(e.toString());
 	}
 
     }
 
     public User getUser(String userId) {
 	User user = null;
 	ResultSet resultSet;
 	try {
 	    resultSet = getResultSet(DbConstants.USER_TABLE,
 		    DbConstants.USER_ID, userId);
 	    if (resultSet.next()) {
 		user = new User(resultSet.getString(DbConstants.USER_ID),
 			resultSet.getString(DbConstants.PASSWORD),
 			resultSet.getString(DbConstants.FIRST_NAME),
 			resultSet.getString(DbConstants.LAST_NAME),
 			resultSet.getString(DbConstants.EMAIL_ID));
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    Logger.getLogger(RetrieveDbInfo.class).error(e.toString());
 	}
 	return user;
     }
 
     private ResultSet getResultSet(String targetField, String table,
 	    String field1, String value) {
 	// Select [targetField] from [table] where [field1]="[value]";
 	try {
 	    resultSet = statement.executeQuery("SELECT " + targetField
 		    + " FROM " + table + " where " + field1 + "=\"" + value
 		    + "\"");
 
 	} catch (SQLException e) {
 	    Logger.getLogger(RetrieveDbInfo.class).error(e.toString());
 	}
 	return resultSet;
     }
 
     private ResultSet getResultSet(String table, String field1, String value) {
 	ResultSet resultSet = null;
 	// Select * from [table] where [field1]="[value]";
 	try {
 	    resultSet = statement.executeQuery("SELECT * FROM " + table
 		    + " where " + field1 + "=\"" + value + "\"");
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    Logger.getLogger(RetrieveDbInfo.class).error(e.toString());
 	}
 	return resultSet;
     }
 
     private ResultSet getResultSet(String targetField, String table) {
 	ResultSet resultSet = null;
 	// Select [targetField] from [table];
 	try {
 	    resultSet = statement.executeQuery("SELECT " + targetField
 		    + " FROM " + table);
 
 	} catch (SQLException e) {
 	    Logger.getLogger(RetrieveDbInfo.class).error(e.toString());
 	}
 	return resultSet;
     }
 
     private ResultSet getResultSet(String table) {
 	ResultSet resultSet = null;
 	// Select * from [table];
 	try {
 	    resultSet = statement.executeQuery("SELECT * FROM " + table);
 
 	} catch (SQLException e) {
 	    Logger.getLogger(RetrieveDbInfo.class).error(e.toString());
 	}
 	return resultSet;
     }
 
     public boolean addUser(User user) {
 	int i = 0;
 	// Select * from [table];
 	try {
 	    String queryString = "INSERT INTO " + DbConstants.USER_TABLE
 		    + " VALUES('" + user.getUserId() + "','"
 		    + user.getPassword() + "','" + user.getFirstName() + "','"
		    + user.getLastName() + "','" + user.getEmailId() + "')";
 	    i = statement.executeUpdate(queryString);
 
 	} catch (SQLException e) {
 	    Logger.getLogger(RetrieveDbInfo.class).error(e.toString());
 	}
 	if (i == 1) {
 	    return true;
 	} else {
 	    return false;
 	}
 
     }
 
 }
