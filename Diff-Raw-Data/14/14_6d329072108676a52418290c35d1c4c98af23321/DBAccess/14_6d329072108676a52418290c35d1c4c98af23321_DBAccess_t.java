 package com.grooveshark.util.db;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 import java.sql.Types;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSetMetaData;
 
 import org.apache.log4j.Logger;
 
 /**
  * Base class for DB access
  *
  */
 public class DBAccess
 {
     public static Map<String, Connection> connections = new HashMap<String, Connection>();
 
     public static final Logger log = Logger.getLogger(DBAccess.class);
     protected Connection connection = null;
 
     public LinkedList<Integer> stringSQLTypes = new LinkedList<Integer>(
             Arrays.asList(Types.DATE, Types.TIMESTAMP, Types.VARCHAR, Types.CHAR, Types.TIME, Types.LONGVARCHAR));
 
     protected String driver = "";
     protected String url = "";
     protected String user = "";
     protected String pass = "";
     protected boolean checkIfValid = true;
 
     /**
      * Constructors
      */
 
     public DBAccess()
         throws SQLException
     {
     }
 
     public DBAccess(String driver, String url, String user, String pass)
         throws SQLException
     {
         this.driver = driver;
         this.url = url;
         this.user = user;
         this.pass = pass;
         this.makeConnection();
     }
 
     public DBAccess(String driver, String url, String user, String pass, boolean checkIfValid)
         throws SQLException
     {
         this.driver = driver;
         this.url = url;
         this.user = user;
         this.pass = pass;
         this.checkIfValid = checkIfValid;
         log.debug("Url: " + this.url);
         this.makeConnection();
     }
 
     /**
      * Get instance method to get a connection
      */
     public static Connection getInstance(String url, String user, String pass, String driver, int numRetries, boolean checkIfValid)
         throws SQLException
     {
         Connection connection = null;
         String key = url + user + pass;
         if (DBAccess.connections.containsKey(key)) {
             connection = DBAccess.connections.get(key);
         } else {
             try {
                 Class.forName(driver).newInstance();
                 connection = DriverManager.getConnection(url, user, pass);
                 DBAccess.connections.put(key, connection);
             } catch (Exception e) {
                 log.error("Error getting a db connection. Url: " + url + "; User: " + user + "; Pass: " + pass + "; Driver: " + driver, e);
                 throw new SQLException("Error getting a db connection. Url: " + url + "; User: " + user + "; Pass: " + pass + "; Driver: " + driver, e);
             }
         }
         return checkConnection(connection, url, user, pass, driver, numRetries, checkIfValid);
     }
 
     /**
      * Checks whether a connection is valid/closed.
      *
      * @param connection: a connection to validate.
      * @param url,user,pass,driver: to form connection string
      * @param numRetries: number of retries if the connection is lost or not valid.
      * @return A valid connection
      */
     public static Connection checkConnection(Connection connection, String url, String user, String pass, String driver, int numRetries, boolean checkIfValid)
         throws SQLException
     {
         boolean retry = false;
         if (connection != null && !connection.isClosed()) {
             if (!checkIfValid) {
                 return connection;
             } else {
                 if (connection.isValid(0)) {
                     return connection;
                 }
             }
         }
         System.out.println("Connection lost");
         DBAccess.connections.remove(url + user + pass);
         if (numRetries > 3) {
             throw new SQLException("Number of retries exceeded 3 times. Check hive connection");
         }
         numRetries++;
         return DBAccess.getInstance(url, user, pass, driver, numRetries, checkIfValid);
     }
 
     public void makeConnection()
         throws SQLException
     {
         if (this.url.equals("") || this.driver.equals("")) {
             throw new SQLException("DB url and/or driver name are not set");
         }
         this.connection = DBAccess.getInstance(this.url, this.user, this.pass, this.driver, 1, this.checkIfValid);
     }
 
     public int executeUpdate(String sql)
         throws SQLException
     {
         this.makeConnection();
         return this.connection.createStatement().executeUpdate(sql);
     }
 
     public ResultSet executeQuery(String sql)
         throws SQLException
     {
         this.makeConnection();
        ResultSet res = null;
        java.sql.Statement stmt = null;
        try {
            stmt = this.connection.createStatement();
            res = stmt.executeQuery(sql);
        } catch (SQLException e) {
            log.debug(e.getMessage());
            log.debug("Trying again");
            res = stmt.executeQuery(sql);
        }
        return res;
     }
 
     public boolean[] findStringTypes(String table)
         throws SQLException
     {
         this.makeConnection();
         ResultSetMetaData metadata = this.connection.createStatement().executeQuery("SELECT * FROM " + table + " limit 1").getMetaData();
         int numColumns = metadata.getColumnCount();
         boolean[] stringTypes = new boolean[numColumns];
         for (int i = 1; i <= numColumns; i++) {
             if (this.stringSQLTypes.contains(metadata.getColumnType(i))) {
                 stringTypes[i-1] = true;
             }
         }
         return stringTypes;
     }
 
     /**
      * Setter method for driver
      */
     public void setDriver(String driver) {
         this.driver = driver;
     }
     /**
      * Getter method for driver
      */
     public String getDriver() {
         return this.driver;
     }
     /**
      * Setter method for url
      */
     public void setUrl(String url) {
         this.url = url;
     }
     /**
      * Getter method for url
      */
     public String getUrl() {
         return this.url;
     }
     /**
      * Setter method for user
      */
     public void setUser(String user) {
         this.user = user;
     }
     /**
      * Getter method for user
      */
     public String getUser() {
         return this.user;
     }
     /**
      * Setter method for pass
      */
     public void setPass(String pass) {
         this.pass = pass;
     }
     /**
      * Getter method for pass
      */
     public String getPass() {
         return this.pass;
     }
     /**
      * Setter method for checkIfValid
      */
     public void setCheckIfValid(boolean checkIfValid) {
         this.checkIfValid = checkIfValid;
     }
     /**
      * Getter method for checkIfValid
      */
     public boolean getCheckIfValid() {
         return this.checkIfValid;
     }
 }
