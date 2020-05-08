 package com.ambantis.magic.dao;
 
 import com.ambantis.magic.exception.DaoConfigurationException;
 import com.ambantis.magic.exception.DaoConnectionException;
 
 import java.sql.Connection;
 
 /**
  * User: Alexandros Bantis
  * Date: 6/15/13
  * Time: 3:04 PM
  */
 public class DaoFactoryJson extends DaoFactory {
     private static DaoFactoryJson mDaoFactoryJson = null;
     private DaoFactoryJson() {}
 
     public static DaoFactoryJson getInstance() {
         if (mDaoFactoryJson == null) {
             //logger.error("cannot get instance of database as it has not yet been initialized");
             throw new DaoConfigurationException("Database has not been initialized.");
         }
         return mDaoFactoryJson;
     }
 
     @Override
     Connection getConnection() throws DaoConfigurationException, DaoConnectionException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
