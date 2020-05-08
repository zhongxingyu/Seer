 package com.dynamobi.ws.api;
 
 import javax.jws.WebService;
 import javax.annotation.security.PermitAll;
 import javax.annotation.security.RolesAllowed;
 
 /**
  * WebService interface for FlexSQLAdmin
  * @author Ray Zhang
  * @since Feb 04,2010
  *
  * @author Kevin Secretan
  * @since June 15, 2010
  * 
  */
 @WebService
 @PermitAll
 public interface FlexSQLAdmin
 {
     /**
      * @param connection
     * @param catalog
      * @return
      */
     @RolesAllowed( {"Admin","Authenticated"} )
    public String getDBMetaData(String connection, String catalog);
     /**
      * exec SQL
      * @param connection
      * @param sqlquerytype
      * @param sql
      * @param toomany
      * @return 
      */
     @RolesAllowed( {"Admin","Authenticated"} )
     public String execSQL(String connection, String sqlquerytype, String sql, int toomany);
     
     /**
      * update data
      * @param connection
      * @param testsql
      * @param sql
      * @param toomany
      * @return
      */
     @RolesAllowed( {"Admin","Authenticated"} )
     public String handleUpdate(String connection, String testsql, String sql, int toomany);
 }
