 package com.herocraftonline.dev.heroes.persistance;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import com.herocraftonline.dev.heroes.Heroes;
 
 public class SQLite {
 
     // Create a String to hold the Database location.
     private final String dbname = Heroes.dataFolder + "/heroes.db";
 
     /**
      * Grab a new Database Connection
      * @return
      */
     public Connection getConnection() {
         Connection connection = null;
         try {
             Class.forName("org.sqlite.JDBC");
             connection = DriverManager.getConnection("jdbc:sqlite:" + dbname);
             connection.setAutoCommit(true);
         } catch (Exception e) {
             Heroes.log.warning(("SQLite connection failed: " + e.toString()));
         }
         return connection;
     }
 
     /**
      * This function performs Inserts & Updates.
      * @param sqlString
      */
     public void tryUpdate(String sqlString) {
         try {
             System.out.println(sqlString);
 
             Connection conn = getConnection();
             Statement st = conn.createStatement();
             st.executeUpdate(sqlString);
             st.close();
             conn.close();
         } catch (Exception e) {
             Heroes.log.warning("The following statement failed: " + sqlString);
             Heroes.log.warning("Statement failed: " + e.toString());
         }
 
     }
 
     /**
      * This function performs Selects and returns a ResultSet.
      * @param sqlString
      * @return
      */
     public ResultSet trySelect(String sqlString) {
         try {
             System.out.println(sqlString);
 
             Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet result = st.executeQuery(sqlString);
             st.close();
             conn.close();
             return result;
         } catch (Exception e) {
             Heroes.log.warning("Statement failed: " + e.toString());
         }
         return null;
     }
     
     /**
      * Return the Row Count of the Query.
      * @param query
      * @return
      */
     public int rowCount(String query){
         int count = 0;
         try {
             Connection conn = Heroes.sql.getConnection();
             Statement s = conn.createStatement();
             ResultSet r = s.executeQuery(query);
            r.next();
            count = r.getInt("rowcount");
             r.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return count;
     }
 }
