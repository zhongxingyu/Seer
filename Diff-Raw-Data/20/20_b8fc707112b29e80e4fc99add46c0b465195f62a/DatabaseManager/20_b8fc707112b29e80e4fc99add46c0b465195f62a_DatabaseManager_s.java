 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.experttag.util.pooling.database;
 
import com.experttag.util.constant.DatabaseConstant;
 import java.sql.Connection;
 import org.apache.log4j.Logger;
import org.smartlib.pool.core.ConnectionPoolException;
import org.smartlib.pool.core.SmartPoolFactory;
 
 /**
  *
  * @author NANDUS
  */
 public class DatabaseManager {
 
     private static Logger log = Logger.getLogger(DatabaseManager.class);
 
     /**
      * This method rtrieves a new connection from database pool and return connection
      * @return
      */
     public static Connection getConnection(){
 
         java.sql.Connection con =null;
         try {
             log.info("Start : new database connection ");
             con = SmartPoolFactory.getConnection(DatabaseConstant.databaseName);
             log.info("End : new database connection ");
         } catch (ConnectionPoolException ex) {
             log.fatal("new database connection  - " + ex);
         }
         return con;
     }
 
 }
