 package com.minecarts.dbconnector.providers;
 
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.SQLException;
 import com.mysql.jdbc.Driver;
 import snaq.db.ConnectionPool; //http://www.snaq.net/java/DBPool/
 
 public class MySQLPool extends Provider{
     public ConnectionPool pool = null;
     
     public boolean connected;
     public boolean connect(String url, String username, String password, int min_conn, int max_conn, int max_create, int timeout){
         try {
             //Ensure the JDBC driver is registered with the java.sql.DriverManager
             Class c = Class.forName("com.mysql.jdbc.Driver");
             Driver driver = (Driver)c.newInstance();
             DriverManager.registerDriver(driver);
 
             //Create our connection pool
             this.pool = new ConnectionPool("minecarts", //Pool name
                     min_conn, //Minimum connections held in the pool
                     max_conn, //Maximum connections held in the pool
                     max_create, //Maxmimum connections that can be created
                     timeout, //Idle timeout of connections in seconds (1 hour)
                     url,
                     username,
                     password);
             pool.registerMBean();
 
            //By default the pool doesn't connect to minecarts, only when a connection is
             //requested from the pool, so we can force it to connect by calling init on the pool
             //but this isn't required
             pool.init();
             connected = true;
            return true;
         } catch (Exception e) {
            e.printStackTrace();
             return false;
         }
     }
 
     public Connection getConnection(){
         if(connected && this.pool != null){
             try{
                 Connection con = pool.getConnection(3000); //3 second timeout
                 if (con != null){
                     return con;
                 } else {
                     System.out.println("Timeout getting a connection from the connection pool.");
                 }
             } catch (SQLException sqlx) {
                 System.out.println("Unexpected SQL error getting a connection from the pool:");
                 sqlx.printStackTrace();
             }
         }else{
             System.out.println("Pool was not connected and attempted to get a connection from it");
         }
         return null;
     }
 }
