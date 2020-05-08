 package com.formation.projet.business.dao;
 
 import com.formation.projet.business.exceptions.PropertiesLoadingException;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gcorre
  * Date: 24/05/13
  * Time: 15:25
  */
 public enum ConnectionFactory {
     instance;
 
     private DatabaseProperties properties;
 
     private ConnectionFactory() {
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
 
         try {
             properties = DaoUtils.getProperties();
         } catch (PropertiesLoadingException e) {
             e.printStackTrace();
         }
     }
 
     public Connection getConnection() {
         Connection connection = null;
         try {
             connection = DriverManager.getConnection(
                     properties.getUrl() + properties.getSchema(),
                     properties.getUser(),
                    properties.getSchema()
             );
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return connection;
     }
 }
