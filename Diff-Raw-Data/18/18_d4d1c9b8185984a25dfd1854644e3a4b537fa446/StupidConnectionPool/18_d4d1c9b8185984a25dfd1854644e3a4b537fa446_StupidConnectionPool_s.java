 package com.shopservice;
 
import play.db.DB;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 public class StupidConnectionPool implements ConnectionPool {
     private String url = "jdbc:mysql://127.0.0.1:3306/domosed_dum";
 
     public StupidConnectionPool(String url) {
         this.url = url;
     }
 
     @Override
     public Connection getConnection() {
         try {
             return DriverManager.getConnection(url);
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     @Override
     public void releaseConnection(Connection connection) {
         try {
             connection.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 }
