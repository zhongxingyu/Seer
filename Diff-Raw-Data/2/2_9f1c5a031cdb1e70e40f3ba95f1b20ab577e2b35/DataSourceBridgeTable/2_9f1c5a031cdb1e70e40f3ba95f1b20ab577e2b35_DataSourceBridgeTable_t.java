 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.bridgetable;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.mule.module.bridgetable.dialect.DatabaseDialect;
 import org.mule.module.bridgetable.dialect.DatabaseDialectFactory;
 
 /**
  * Concrete implementation of a bridge table that uses a JDBC compliant database to store key mappings.
  */
 public class DataSourceBridgeTable implements BridgeTable
 {
     private static final Logger LOGGER = Logger.getLogger(DataSourceBridgeTable.class);
     private DataSource ds;
     private String tableName;
     private DatabaseDialect dialect;
     
     /**
      * 
      */
     public DataSourceBridgeTable(DataSource dataSource)
     {
         this.ds = dataSource;
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#insert(java.lang.Object, java.lang.Object)
      */
     @Override
     public void insert(Object key1, Object key2) throws BridgeTableException
     {
         try
         {
             int result = executeUpdate(dialect.getInsertSQL(getTableName()), new Object[] {key1, key2});
             
            LOGGER.info(result + " row/s were inserted for key1 = [" + key1 + "] and key2 = [" + key2 + "] in " + getTableName() + ".");
         }
         catch(SQLException ex)
         {
             String msg = "Could not insert key1 = [" + key1 + "] and key2 = [" + key2 + "] in " + getTableName() + ".";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#updateByKey1(java.lang.Object, java.lang.Object)
      */
     @Override
     public void updateByKey1(Object key1, Object newKey2) throws BridgeTableException
     {
         try
         {
             int result = executeUpdate(dialect.getUpdateByKey1SQL(getTableName()), new Object[] {newKey2, key1});
             
             if(result > 0)
             {
                 LOGGER.info(result + " row/s were updated for key1 = [" + key1 + "] with new key2 = [" + newKey2 + "] in " + getTableName() + ".");
             }
             else
             {
                 String msg = "No row matched key1 = [" + key1 + "] in " + getTableName() + ". Nothing was updated.";
                 LOGGER.warn(msg);
                 throw new KeyDoesNotExistException(msg);
             }
         }
         catch(SQLException ex)
         {
             String msg = "Could not update key1 = [" + key1 + "] to new key2 = [" + newKey2 + "] in " + getTableName() + ".";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }        
         
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#updateByKey2(java.lang.Object, java.lang.Object)
      */
     @Override
     public void updateByKey2(Object key2, Object newKey1) throws BridgeTableException
     {
         try
         {
             int result = executeUpdate(dialect.getUpdateByKey2SQL(getTableName()), new Object[] {newKey1, key2});
             
             if(result > 0)
             {
                 LOGGER.info(result + " row/s were updated for key2 = [" + key2 + "] with new key1 = [" + newKey1 + "] in " + getTableName() + ".");
             }
             else
             {
                 String msg = "No row matched key2 = [" + key2 + "] in " + getTableName() + ". Nothing was updated.";
                 LOGGER.warn(msg);
                 throw new KeyDoesNotExistException(msg);
             }            
             
         }
         catch(SQLException ex)
         {
             String msg = "Could not update key2 = [" + key2 + "] to new key1 = [" + newKey1 + "] in " + getTableName() + ".";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }            
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#removeByKey1(java.lang.Object)
      */
     @Override
     public void removeByKey1(Object key1) throws BridgeTableException
     {
         try
         {
             int result = executeUpdate(dialect.getDeleteByKey1SQL(getTableName()), new Object[] {key1});
             
             if(result > 0)
             {
                 LOGGER.info(result + " row/s were deleted for key1 = [" + key1 + "]. in " + getTableName() + ".");
             }
             else
             {
                 String msg = "No row matched key1 = [" + key1 + "] in " + getTableName() + ". Nothing was deleted.";
                 LOGGER.warn(msg);
                 throw new KeyDoesNotExistException(msg);                
             }
         }
         catch(SQLException ex)
         {
             String msg = "Could not delete row for key1 = [" + key1 + "] in " + getTableName();
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }         
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#removeByKey2(java.lang.Object)
      */
     @Override
     public void removeByKey2(Object key2) throws BridgeTableException
     {
         try
         {
             int result = executeUpdate(dialect.getDeleteByKey2SQL(getTableName()), new Object[] {key2});
             
             if(result > 0)
             {
                 LOGGER.info(result + " row/s were deleted for key2 = [" + key2 + "]. in " + getTableName() + ".");
             }
             else
             {
                 String msg = "No row matched key2 = [" + key2 + "] in " + getTableName() + ". Nothing was deleted.";
                 LOGGER.warn(msg);
                 throw new KeyDoesNotExistException(msg);                
             }
 
         }
         catch(SQLException ex)
         {
             String msg = "Could not delete row for key2 = [" + key2 + "] in " + getTableName() + ".";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }             
         
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#retrieveByKey1(java.lang.Object)
      */
     @Override
     public Object retrieveByKey1(Object key1) throws BridgeTableException
     {
         try
         {
             List<Object> result = executeQuery(dialect.getLookupByKey1SQL(getTableName()), new Object[] {key1});
             
             if(result.size() > 0)
             {
                 if(result.size() > 1)
                 {
                     LOGGER.warn("More than one row matched key1 = [" + key1 + "] in " + getTableName() + ".");
                 }
                 return result.get(0);
             }
             else
             {
                 String msg = "No row matched key1 = [" + key1 + "] in " + getTableName() + ".";
                 LOGGER.warn(msg);
                 throw new KeyDoesNotExistException(msg);                
             }            
         }
         catch(SQLException ex)
         {
             String msg = "Could not retrieve row for key1 = [" + key1 + "] in " + getTableName() + ".";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }          
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#retrieveByKey2(java.lang.Object)
      */
     @Override
     public Object retrieveByKey2(Object key2) throws BridgeTableException
     {
         try
         {
             List<Object> result = executeQuery(dialect.getLookupByKey2SQL(getTableName()), new Object[] {key2});
             
             if(result.size() > 0)
             {
                 if(result.size() > 1)
                 {
                     LOGGER.warn("More than one row matched key2 = [" + key2 + "] in " + getTableName() + ".");
                 }
                 return result.get(0);
             }
             else
             {
                 String msg = "No row matched key2 = [" + key2 + "] in " + getTableName() + ".";
                 LOGGER.warn(msg);
                 throw new KeyDoesNotExistException(msg);                
             }            
         }
         catch(SQLException ex)
         {
             String msg = "Could not retrieve row for key2 = [" + key2 + "] in " + getTableName() + ".";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }         
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#isPersistent()
      */
     @Override
     public boolean isPersistent()
     {
         return true;
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#containsKey1(java.lang.Object)
      */
     @Override
     public boolean containsKey1(Object key1) throws BridgeTableException
     {
         try
         {
             //TODO: Avoid warning messages in log?
             retrieveByKey1(key1);
             return true;
         }
         catch(KeyDoesNotExistException ex)
         {
             return false;
         }
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#containsKey2(java.lang.Object)
      */
     @Override
     public boolean containsKey2(Object key2) throws BridgeTableException
     {
         try
         {
             //TODO: Avoid warning messages in log?
             retrieveByKey2(key2);
             return true;
         }
         catch(KeyDoesNotExistException ex)
         {
             return false;
         }
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#init(java.lang.String, org.mule.module.bridgetable.KeyType, org.mule.module.bridgetable.KeyType, java.lang.String, java.lang.String, boolean)
      */
     @Override
     public void init(String tableName, KeyType key1Type, KeyType key2Type, String key1Name, String key2Name, boolean autoCreateTable) throws BridgeTableException
     {
         Connection conn = null;
         ResultSet rs = null;
         setTableName(tableName);
         try
         {
             conn = ds.getConnection();
             
             DatabaseMetaData md = conn.getMetaData();
             
             this.dialect = DatabaseDialectFactory.create(md.getDatabaseProductName(), md.getDatabaseProductVersion(), key1Type, key2Type, key1Name, key2Name);
             
             if(dialect == null)
             {
                 LOGGER.error("No dialect found for database " + md.getDatabaseProductName() + " (" + md.getDatabaseProductVersion() + ")");
                 throw new BridgeTableException("No dialect found for database " + md.getDatabaseProductName() + " (" + md.getDatabaseProductVersion() + ")");
             }
             
             rs = md.getTables(null, null, getTableName(), null);
             
             boolean tableExists = rs.next();
             
             if(!tableExists && autoCreateTable)
             {
                 LOGGER.info("Table [" + getTableName() + "] is not present and will be created.");
                 createBridgeTable();
             }
             else if(!tableExists)
             {
                 LOGGER.error("Table " + getTableName() + " doesn't exist. Set autoCreateTable to true or create the table yourself.");
                 throw new BridgeTableException("Table " + getTableName() + " doesn't exist. Set autoCreateTable to true or create the table yourself.");
             } else {
                 //TODO: Validate that the table column names and types match or are compatible!
             }
         }
         catch(SQLException ex)
         {
             String msg = "Could not initialize bridge table for key1 type = [" + key1Type + "], key2 type = [" + key2Type + "] and auto create table = [" + autoCreateTable + "]";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);           
         }
         finally
         {
             close(conn, rs);
         }
     }
 
     /*
      * Executes a SQL query
      */
     private List<Object> executeQuery(String sql, Object params[]) throws SQLException
     {
         Connection conn = null;
         ResultSet rs = null;
         List<Object> result = new ArrayList<Object>();
         
         try
         {
             conn = ds.getConnection();
             
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             if(params != null && params.length > 0)
             {
                 for(int i=0; i < params.length; i++)
                 {
                     if(params[i] != null)
                     {
                         stmt.setObject(i + 1, params[i]);
                     }
                     else
                     {
                         stmt.setNull(i + 1, Types.JAVA_OBJECT);
                     }
                 }
             }
             
             rs = stmt.executeQuery();
             
             while(rs.next())
             {
                 result.add(rs.getObject(1));
             }
             
             return result;
         }
         finally
         {
             close(conn, rs);
         }
     }
 
     /*
      * Executes a SQL update statement
      */
     private int executeUpdate(String sql, Object params[]) throws SQLException
     {
         Connection conn = null;
         try
         {
             conn = ds.getConnection();
             
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             if(params != null && params.length > 0)
             {
                 for(int i=0; i < params.length; i++)
                 {
                     if(params[i] != null)
                     {
                         stmt.setObject(i + 1, params[i]);
                     }
                     else
                     {
                         stmt.setNull(i + 1, Types.JAVA_OBJECT);
                     }
                 }
             }
             
             return stmt.executeUpdate();
         }
         finally
         {
             close(conn);
         }
     }
     
     /*
      * Creates the bridge table
      */
     private void createBridgeTable() throws BridgeTableException
     {
         try
         {
             int result = executeUpdate(dialect.getCreateTableSQL(getTableName()), null);
             
             LOGGER.info("Created table = [" + getTableName() + "]. Return: " + result);
         }
         catch(SQLException ex)
         {
             String msg = "Could not create table = [" + getTableName() + "]";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }        
         
     }
 
     /*
      * Helper method to silently close JDBC connections
      */
     private void close(Connection conn)
     {
         close(conn, null);
     }
     
     /*
      * Helper method to silently close JDBC connections and result set
      */
     private void close(Connection conn, ResultSet rs)
     {
         try
         {
             if(rs != null)
             {
                 try
                 {
                     rs.close();
                 }
                 catch(SQLException ex)
                 {
                     LOGGER.error("Could not close result set", ex);
                 }
             }
         }
         finally
         {
             if(conn != null)
             {
                 try
                 {
                     conn.close();
                 }
                 catch(SQLException ex)
                 {
                     LOGGER.error("Could not close connection", ex);
                 }
             }
         }
     }
     
     /**
      * @see org.mule.module.bridgetable.BridgeTable#destroy()
      */
     @Override
     public void destroy() throws BridgeTableException
     {
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#keys1()
      */
     @Override
     public List<Object> keys1() throws BridgeTableException
     {
         try
         {
             return executeQuery(dialect.getAllKey1SQL(getTableName()), null);
         }
         catch(SQLException ex)
         {
             String msg = "Could not retrieve all values for key1";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }          
     }
 
     /**
      * @see org.mule.module.bridgetable.BridgeTable#keys2()
      */
     @Override
     public List<Object> keys2() throws BridgeTableException
     {
         try
         {
             return executeQuery(dialect.getAllKey2SQL(getTableName()), null);
         }
         catch(SQLException ex)
         {
             String msg = "Could not retrieve all values for key2";
             LOGGER.error(msg, ex);
             throw dialect.translateException(ex, msg);
         }  
     }
 
     /**
      * @return The name of the bridge table.
      */
     public String getTableName()
     {
         return tableName;
     }
 
     /**
      * @param tableName The name of the bridge table.
      */
     public void setTableName(String tableName)
     {
         this.tableName = tableName;
     }    
 }
 
 
