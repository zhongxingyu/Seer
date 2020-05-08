 package csheets.ext.persistence.adapter;
 
 import csheets.core.Cell;
 
 /**
  * Factory of connection adapters
  * @author João Carreira
  */
 public class DBConnectionAdapterFactory 
 {
     /* DBConnectionAdapterFactory is a singleton */
     private static DBConnectionAdapterFactory instance = null;
     
     /* private constructor (required in the singleton pattern */
     private DBConnectionAdapterFactory()
     {
     }
     
     /**
      * getInstance applying the singleton pattern
      * @return DBConnectionAdapterFactory object
      */
     public static DBConnectionAdapterFactory getInstance()
     {
         if(instance == null)
         {
             return new DBConnectionAdapterFactory();
         }
         return instance;
     }
     
     /**
      * gets the Connection adapter based on the url passed as argument
      * @param url url to the database
      * @return a DBConnectionAdapter object
      */
     public static DBConnectionAdapter getDBTechnology(String url)
     {
        return null;
     }
 }
