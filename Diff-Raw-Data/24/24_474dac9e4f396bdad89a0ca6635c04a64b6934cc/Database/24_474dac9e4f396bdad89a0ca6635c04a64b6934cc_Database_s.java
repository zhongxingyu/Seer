 package org.jailsframework.database;
 
 import org.jailsframework.exceptions.JailsException;
 
 import java.sql.*;
 
 /**
  * @author <a href="mailto:sanjusoftware@gmail.com">Sanjeev Mishra</a>
  * @version $Revision: 0.1
  *          Date: May 11, 2010
  *          Time: 12:10:35 AM
  */
 public abstract class Database implements IDatabase {
     private String name;
     private String user;
     private String password;
     private String driver;
     private String url;
     Statement statement = null;
     Connection connection = null;
 
     public Database(String url, String driver, String name, String user, String password) {
         this.driver = driver;
         this.user = user;
         this.name = name;
         this.password = password;
         this.url = url;
     }
 
     public boolean execute(String query) {
         loadDriver();
         try {
             connection = DriverManager.getConnection(url + name, user, password);
             statement = connection.createStatement();
             return statement.execute(query);
         }
         catch (Exception e) {
             throw new JailsException("Error executing query: " + e);
         } finally {
            closeStatement();
             closeConnection();
         }
     }
 
     public ResultSet executeQuery(String query) {
         loadDriver();
         try {
             connection = DriverManager.getConnection(url + name, user, password);
             statement = connection.createStatement();
            return statement.executeQuery(query);
         }
         catch (Exception e) {
             throw new JailsException("Error executing query: " + e);
         } finally {
            closeStatement();
             closeConnection();
         }
     }
 
     public int executeUpdate(String query) {
         loadDriver();
         try {
             connection = DriverManager.getConnection(url + name, user, password);
             statement = connection.createStatement();
             return statement.executeUpdate(query);
         }
         catch (Exception e) {
             throw new JailsException("Error executing query: " + e);
         } finally {
            closeStatement();
             closeConnection();
         }
     }
 
     private boolean loadDriver() {
         try {
             Class.forName(driver);
         }
         catch (Exception e) {
             throw new JailsException("Unable to load the driver class!" + e);
         }
         return true;
     }
 
     private void closeConnection() {
         if (connection != null) try {
             connection.close();
         } catch (SQLException e) {
             throw new JailsException("Could not close connection!" + e);
         }
     }

    private void closeStatement() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                throw new JailsException("Could not close statement!" + e);
            }
        }
    }
 }
