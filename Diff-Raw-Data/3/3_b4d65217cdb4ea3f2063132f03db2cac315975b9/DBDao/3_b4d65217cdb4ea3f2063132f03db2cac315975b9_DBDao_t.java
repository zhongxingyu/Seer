 /*
 Dynamo Web Services is a web service project for administering LucidDB
 Copyright (C) 2010 Dynamo Business Intelligence Corporation
 
 This program is free software; you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the Free
 Software Foundation; either version 2 of the License, or (at your option)
 any later version approved by Dynamo Business Intelligence Corporation.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 package com.dynamobi.ws.util;
 
 import java.sql.SQLException;
 import javax.sql.DataSource;
 import java.lang.ClassNotFoundException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.io.InputStream;
 import java.io.IOException;
 
 // Base class
 import org.springframework.security.userdetails.jdbc.JdbcDaoImpl;
 
 // Factory class to construct our pooled datasource
 import com.mchange.v2.c3p0.DataSources;
 
 import com.dynamobi.ws.util.DBAccess;
 import com.dynamobi.ws.util.DB;
 
 public class DBDao extends JdbcDaoImpl {
 
   private DataSource ds_pooled = null;
 
   public DBDao() throws ClassNotFoundException, SQLException, IOException {
     super();
 
     Properties pro = new Properties();
 
     InputStream user_props = this.getClass().getResourceAsStream("/luciddb-jdbc.properties");
     if (user_props != null) {
       pro.load(user_props);
     } else {
       pro.load(this.getClass().getResourceAsStream("/luciddb-jdbc-default.properties"));
     }
 
     Class.forName(pro.getProperty("jdbc.driver"));
 
     String username = pro.getProperty("jdbc.username");
     String password = pro.getProperty("jdbc.password");
     String url      = pro.getProperty("jdbc.url");
 
     DataSource ds_unpooled = DataSources.unpooledDataSource(
         url,
         username,
         password);
 
     Map<String,String> overrides = new HashMap<String,String>();
    //causes problems when DB server is not running
    //overrides.put("minPoolSize", "3");
     overrides.put("maxIdleTimeExcessConnections", "600");
     overrides.put("breakAfterAcquireFailure", "true");
     overrides.put("acquireRetryAttempts", "15");
     ds_pooled = DataSources.pooledDataSource(ds_unpooled, overrides);
 
     setDataSource(ds_pooled);
     DBAccess.connDataSource = ds_pooled;
     DB.connDataSource = ds_pooled;
   }
 
   public void cleanup() throws SQLException {
     DataSources.destroy(ds_pooled);
   }
 
 }
