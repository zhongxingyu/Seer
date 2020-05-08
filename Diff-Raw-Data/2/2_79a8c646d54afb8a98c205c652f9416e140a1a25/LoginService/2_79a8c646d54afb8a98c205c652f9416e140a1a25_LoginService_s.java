 package com.computas.sublima.app.service;
 
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.cocoon.auth.AuthenticationException;
 
 import com.computas.sublima.query.service.DatabaseService;
 
 /**
  * Class for handling of user validation
  * @author ewinge
  *
  */
 public class LoginService {
     private DatabaseService dbService = new DatabaseService();
     private AdminService adminService = new AdminService();
 
     /**
      * Checks if a given username and password are valid
      * @param name The username
      * @param password The Password
      * @return true if the log on information is valid, otherwise false
      * @throws AuthenticationException
      */
     public boolean validateUser(String name, String password) throws AuthenticationException {
 	boolean validUser = true;
 
 	if (name == null) {
 	    return false;
 	} else {
 
 	    if (!name.equalsIgnoreCase("administrator")) {
 		if (adminService.isInactiveUser(name)) {
 		    validUser = false;
 		}
 	    }
 
 	    String sql = "SELECT * FROM DB.DBA.users WHERE username = '" + name + "'";
 	    Statement statement = null;
 
 	    try {
 		Connection connection = dbService.getJavaSQLConnection();
 		
 		if (connection == null) {
 		    throw new AuthenticationException("Could not connect to user database for authentication.");
 		}
 
 		statement = connection.createStatement();
 		ResultSet rs = statement.executeQuery(sql);
 
 		if (!rs.next()) { //empty
 		    validUser = false;
 		}
 
 		if (!adminService.generateSHA1(password).equals(rs.getString("password"))) {
 		    validUser = false;
 		}
 
 		statement.close();
 		connection.close();
 
 
 	    } catch (SQLException e) {
 		e.printStackTrace();
		throw new AuthenticationException("An error occured when trying to validate password.");
 	    }
 
 	    return validUser;
 	}
     }
 }
