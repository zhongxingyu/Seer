 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.util;
 
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

 public class TangerineDataSource implements DataSource {
 
     protected final Log logger = OLLogger.getLog(getClass());
 
 
     // The default schema should have no tables. Change to this first so in case something goes wrong, we don't want to still be pointing to the old database.
     private static final String TANGERINE_DEFAULT_SCHEMA = "orangeleap";
     private static final String USE_SQL = "USE ";
 
     private boolean splitDatabases = true;
     private DataSource dataSource;
     private TangerineUserHelper tangerineUserHelper;
     double count;
 
     @Override
     public Connection getConnection() throws SQLException {
 
    	
    	// Avoid error: "Cannot convert value '0000-00-00 00:00:00' from column 11 to TIMESTAMP."
    	if (dataSource instanceof MysqlDataSource) {
    		((MysqlDataSource)dataSource).setZeroDateTimeBehavior("convertToNull"); 
    	}
    	
    	
         Connection conn = dataSource.getConnection();
         if (logger.isTraceEnabled()) {
             count++;
             logger.trace("getConnection() called, count = " + (int) count);
             //new Exception().fillInStackTrace().printStackTrace();
         }
 
         String siteName = tangerineUserHelper.lookupUserSiteName();
         boolean hasSite = siteName != null && siteName.trim().length() > 0;
         if (hasSite) {
             //logger.debug("getConnection() called for site " + siteName);
         }
 
         if (!splitDatabases) {
 
             // We have split databases turned off - use default schema.
             return conn;
 
         } else {
 
             // We have split databases turned on - use site schema if one applies.
 
             // This default schema must exist, and should have no tables in it.
             // Want to remain here if error occurs setting schema to site schema.
             changeSchema(conn, TANGERINE_DEFAULT_SCHEMA);
 
             if (!hasSite) {
                 // This path is used by container when initializing a pool connection outside of a site context.
                 return conn;
             }
 
             //logger.debug("Setting schema for site = "+siteName + "...");
             changeSchema(conn, siteName);
             //logger.debug("Set schema for site = "+siteName + ".");
 
             return conn;
 
         }
     }
 
     private void changeSchema(Connection conn, String schema) throws SQLException {
 
         if (schema == null || schema.length() == 0 || !StringUtils.isAlphanumeric(schema)) {
             throw new RuntimeException("Invalid schema name.");
         }
 
         // Mysql appears to prefer a Statement to a PreparedStatement in order to execute non-standard SQL commands.
         // Must therefore validate parameter for non-alphanumeric characters.
         Statement s = conn.createStatement();
         try {
             s.execute(USE_SQL + schema);
         } catch (Throwable e) {
             e.printStackTrace();
             throw new RuntimeException("Unable to change databases to " + schema, e);
         } finally {
             s.close();
         }
 
     }
 
     @Override
     public Connection getConnection(String username, String password)
             throws SQLException {
         throw new RuntimeException("getConnection(username, password) is not supported by TangerineDataSource.");
     }
 
     @Override
     public PrintWriter getLogWriter() throws SQLException {
         return dataSource.getLogWriter();
     }
 
     @Override
     public int getLoginTimeout() throws SQLException {
         return dataSource.getLoginTimeout();
     }
 
     @Override
     public void setLogWriter(PrintWriter out) throws SQLException {
         dataSource.setLogWriter(out);
     }
 
     @Override
     public void setLoginTimeout(int seconds) throws SQLException {
         dataSource.setLoginTimeout(seconds);
     }
 
     @Override
     public boolean isWrapperFor(Class<?> iface) throws SQLException {
         return dataSource.isWrapperFor(iface);
     }
 
     @Override
     public <T> T unwrap(Class<T> iface) throws SQLException {
         return dataSource.unwrap(iface);
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     public DataSource getDataSource() {
         return dataSource;
     }
 
     public void setTangerineUserHelper(TangerineUserHelper tangerineUserHelper) {
         this.tangerineUserHelper = tangerineUserHelper;
     }
 
     public TangerineUserHelper getTangerineUserHelper() {
         return tangerineUserHelper;
     }
 
     public void setSplitDatabases(boolean splitDatabases) {
         this.splitDatabases = splitDatabases;
     }
 
     public boolean isSplitDatabases() {
         return splitDatabases;
     }
 
 }
