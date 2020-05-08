 package cn.hit.sqat.info;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public final class Database {
 
     private static final String URL_DATABASE = "jdbc:mysql://localhost:3306/commissionDB";
 
     private static final String DATABASE_USERNAME = "root";
 
     private static final String DATABASE_PASSWORD = "root";
 
     private static Connection connection;
 
     static {
         try {
             Class.forName("com.mysql.jdbc.Driver").newInstance();
         } catch(final Exception e) {
             e.printStackTrace();
         }
     }
 
     public static void connect() {
         try {
             if(!isConnected())
                 connection = DriverManager.getConnection(URL_DATABASE, DATABASE_USERNAME, DATABASE_PASSWORD);
         } catch(final Exception e) {
             e.printStackTrace();
         }
     }
 
     public static void disconnect() {
         try {
             if(isConnected()) {
                 connection.close();
                 connection = null;
             }
         } catch(final SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static boolean isConnected() {
         try {
             return (connection != null && !connection.isClosed());
         } catch(final SQLException e) {
             e.printStackTrace();
         }
 
         return false;
     }
 
     public static ResultSet query(final String query) {
         try {
             return connection.prepareStatement(query).executeQuery();
         } catch(final SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static boolean insert(final String insert) {
         try {
             connection.prepareStatement(insert).executeUpdate();
             return true;
         } catch (final SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 }
