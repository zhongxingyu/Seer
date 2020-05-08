 /**
  * 
  */
 package com.dynamobi.ws.impl;
 
 import javax.jws.WebService;
 
 import com.dynamobi.ws.api.FlexSQLAdmin;
 import com.dynamobi.ws.util.DBAccess;
 
 /**
  * Implementation of the interface FlexSQLAdmin
  * 
  * @author Ray Zhang
  * @since Feb 04,2010
  *
  * @author Kevin Secretan
  * @since June 15, 2010
  */
 @WebService(
 
 endpointInterface = "com.dynamobi.ws.api.FlexSQLAdmin"
 
 )
 public class FlexSQLAdminImpl
     implements FlexSQLAdmin
 {
 
     /*
      * (non-Javadoc)
      * 
      * @see com.flexsqladmin.api.FlexSQLAdmin#getDBMetaData()
      */
    public String getDBMetaData(String connection, String schema)
     {
        // Note: "schema" is really "catalog", fixme for future.

         String ret = "";
         try {
            ret = DBAccess.getDBMetaData(connection, schema);
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return ret;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.flexsqladmin.api.FlexSQLAdmin#execSQL(java.lang.String,
      *      java.lang.String, java.lang.String, int)
      */
     public String execSQL(
         String connection,
         String sqlquerytype,
         String sql,
         int toomany)
     {
 
         String ret = DBAccess.execSQL(connection, sqlquerytype, sql, toomany);
         return ret;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.flexsqladmin.api.FlexSQLAdmin#handleUpdate(java.lang.String,
      *      java.lang.String, java.lang.String, int)
      */
     public String handleUpdate(
         String connection,
         String testsql,
         String sql,
         int toomany)
     {
         String ret = DBAccess.handleUpdate(connection, testsql, sql, toomany);
         return ret;
     }
 
 }
