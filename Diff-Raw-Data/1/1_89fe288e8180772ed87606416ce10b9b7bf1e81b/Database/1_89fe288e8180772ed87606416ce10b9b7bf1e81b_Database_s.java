 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Main;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 import org.sqlite.SQLiteJDBCLoader;
 
 /**
  *
  * @author Christiaan
  */
 public class Database {
     
     /**
      * in-memory database connection
      */
     private static Connection connection = null;
     
     /**
      * Get connection to in-memory database
      * @return Connection
      */
     public static Connection getConnection() {
         if(connection == null) {
             Statement statement = null;
             try {
                 Class.forName("org.sqlite.JDBC");
                 connection = DriverManager.getConnection("jdbc:sqlite::memory:");
                 
                 statement = connection.createStatement();
          
                 BufferedReader input = new BufferedReader(new FileReader("db/schema.sql"));
                 String contents;
                 String sql = "";
                 while((contents = input.readLine()) != null) {
                     sql += contents;
                 }
                 input.close();
          
                 statement.executeUpdate(sql);
             }
             catch (Exception e) 
             {  
                 e.printStackTrace();  
             }
             finally {
                 try {
                     statement.close();
                 }
                 catch (Exception e) 
                 {  
                     e.printStackTrace();  
                 }
             }
         }
         return connection;
     }
 
     /**
      * Close in-memory database connection
      */
     public static void closeConnection() {
         if(connection != null) {
             try {
                 connection.close();
             }
             catch (Exception e) 
             {  
                 e.printStackTrace();  
             }
         }
     }
     
     /**
      * Check if SQLite database is running in native mode or pure-java
      * @return boolean true if running in native mode, false if running pure-java
      */
     public static boolean isNativeMode() {
         return SQLiteJDBCLoader.isNativeMode();
     }
     
     /**
      * Dump in-memory database to file
      * @return boolean true if succeeded
      */
     public static boolean dumpDatabase() {
         Statement stm = null;
         try {
             stm = getConnection().createStatement();
             stm.executeUpdate("backup to db/backup.db");
         }
         catch(Exception e) {
             e.printStackTrace();
             return false;
         }
         finally {
             try {
                 stm.close();
             }
             catch (Exception e) {  
                 e.printStackTrace();
             }
         }
         return true;
     }
     
     /**
      * 
      * @return boolean true if succeeded
      */
     public static boolean restoreDump() {
         Statement stm = null;
         try {
             stm = getConnection().createStatement();
             stm.executeUpdate("restore from db/backup.db");
         }
         catch(Exception e) {
             e.printStackTrace();
             return false;
         }
         finally {
             try {
                 stm.close();
             }
             catch (Exception e) {  
                 e.printStackTrace();
             }
         }
         return true;
     }
 }
