 package com.dynamobi.ws.util;
 
 import java.beans.PropertyVetoException;
 import java.sql.SQLException;
 import javax.sql.DataSource;
 import java.lang.ClassNotFoundException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 // Base class
 import org.springframework.security.userdetails.jdbc.JdbcDaoImpl;
 
 // Factory class to construct our pooled datasource
 import com.mchange.v2.c3p0.DataSources;
 
 import com.dynamobi.ws.util.DBAccess;
 
 public class DBDao extends JdbcDaoImpl {
 
   private DataSource ds_pooled = null;
 
   public DBDao() throws ClassNotFoundException, SQLException {
     super();
     Class.forName("org.luciddb.jdbc.LucidDbClientDriver");
     DataSource ds_unpooled = DataSources.unpooledDataSource(
         "jdbc:luciddb:http://localhost",
         "sa",
         "sa");
     
    Map overrides = new HashMap();
     overrides.put("minPoolSize", "5");
     ds_pooled = DataSources.pooledDataSource(ds_unpooled, overrides);
 
     setDataSource(ds_pooled);
     DBAccess.connDataSource = ds_pooled;
   }
 
   public void cleanup() throws SQLException {
     DataSources.destroy(ds_pooled);
   }
 
 }
