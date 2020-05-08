 package de.consistec.syncframework.impl.adapter;
 
 /*
  * #%L
  * Project - doppelganger
  * File - DumpDataSource.java
  * %%
  * Copyright (C) 2011 - 2013 consistec GmbH
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 import static de.consistec.syncframework.common.util.CollectionsUtil.newArrayList;
 import static de.consistec.syncframework.common.util.PropertiesUtil.readString;
 import static de.consistec.syncframework.common.util.StringUtil.isNullOrEmpty;
 
 import de.consistec.syncframework.common.ConfigConstants;
 import de.consistec.syncframework.common.util.StringUtil;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
import java.util.logging.Logger;
 import javax.sql.DataSource;
 import org.powermock.reflect.Whitebox;
 
 /**
  * Imitates {@link javax.sql.DataSource DataSource} object.
  * <p>This class provides only implementation for {@link javax.sql.DataSource#getConnection() getConnection()} method.
  * All others will throw {@link UnsupportedOperationException} </p>
  *
  * @author Piotr Wieczorek
  * @company consistec Engineering and Consulting GmbH
  * @date 10.12.2012 16:11:34
  * @since 0.0.1-SNAPSHOT
  */
 public class DumpDataSource implements DataSource {
 
     //<editor-fold defaultstate="expanded" desc=" Class fields " >
     private SupportedDatabases dbType;
     private ConnectionType conType;
     private List<Connection> createdConnections;
     private String propertiesPrefix;
     private Properties properties;
 //</editor-fold>
 
     //<editor-fold defaultstate="expanded" desc=" Class constructors " >
     public DumpDataSource(SupportedDatabases dbType, ConnectionType conType) {
         this.dbType = dbType;
         this.conType = conType;
         createdConnections = newArrayList();
         try {
             if (conType == ConnectionType.CLIENT) {
                 propertiesPrefix = String.valueOf(
                     Whitebox.getField(ConfigConstants.class, "OPTIONS_COMMON_CLIENT_DB_ADAP_GROUP").get(null));
             } else {
                 propertiesPrefix = String.valueOf(
                     Whitebox.getField(ConfigConstants.class, "OPTIONS_COMMON_SERV_DB_ADAP_GROUP").get(null));
             }
 
             propertiesPrefix += ".";
             readConfig();
         } catch (IllegalArgumentException ex) {
             throw new RuntimeException(ex);
         } catch (IllegalAccessException ex) {
             throw new RuntimeException(ex);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }
 //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc=" Class accessors and mutators " >
     public SupportedDatabases getDbType() {
         return dbType;
     }
 
     public ConnectionType getConType() {
         return conType;
     }
 //</editor-fold>
 
     //<editor-fold defaultstate="expanded" desc=" Class methods " >
     private Connection create(String username, String password) throws Exception {
 
         Connection con = null;
         switch (dbType) {
             case MYSQL:
                 con = createMySql(username, password);
                 break;
             case POSTGRESQL:
                 con = createPostgres(username, password);
                 break;
             case SQLITE:
                 con = createSqlLite(username, password);
                 break;
             default:
                 throw new UnsupportedOperationException("Unsupported database");
         }
 
         createdConnections.add(con);
         return con;
 
     }
 
     private Connection createPostgres(String username, String password) throws Exception {
         String driver = readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_DRIVER_NAME, false);
         if (isNullOrEmpty(driver)) {
             driver = "org.postgresql.Driver";
         }
         String url = readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_URL, false);
 
         if (isNullOrEmpty(url)) {
             String host = readString(properties, propertiesPrefix + PostgresDatabaseAdapter.PROPS_HOST, true);
             String dbName = readString(properties, propertiesPrefix + PostgresDatabaseAdapter.PROPS_DB_NAME, true);
             String tmpPort = readString(properties, propertiesPrefix + PostgresDatabaseAdapter.PROPS_PORT, false);
 
             url = Whitebox.<String>invokeMethod(PostgresDatabaseAdapter.class, "createUrl", host,
                 StringUtil.isNullOrEmpty(tmpPort) ? null : Integer.valueOf(tmpPort), dbName);
         }
         return buildConnection(driver, url, username, password);
     }
 
     private Connection createMySql(String username, String password) throws Exception {
         String driver = readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_DRIVER_NAME, false);
         if (isNullOrEmpty(driver)) {
             driver = "com.mysql.jdbc.Driver";
         }
         String url = readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_URL, false);
 
         if (isNullOrEmpty(url)) {
             String host = readString(properties, propertiesPrefix + PostgresDatabaseAdapter.PROPS_HOST, true);
             String dbName = readString(properties, propertiesPrefix + PostgresDatabaseAdapter.PROPS_DB_NAME, true);
             String tmpPort = readString(properties, propertiesPrefix + PostgresDatabaseAdapter.PROPS_PORT, false);
 
             url = Whitebox.<String>invokeMethod(MySqlDatabaseAdapter.class, "createUrl", host,
                 StringUtil.isNullOrEmpty(tmpPort) ? null : Integer.valueOf(tmpPort), dbName);
         }
         return buildConnection(driver, url, username, password);
     }
 
     private Connection createSqlLite(String username, String password) throws IOException, ClassNotFoundException,
         SQLException {
         return createGeneric(username, password);
     }
 
     private Connection createGeneric(String username, String password) throws ClassNotFoundException,
         SQLException {
         String dbDriver = readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_DRIVER_NAME, true);
         String dbUrl = readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_URL, true);
         return buildConnection(dbDriver, dbUrl, username, password);
     }
 
     private Connection buildConnection(String driver, String url, String dbUser, String dbPass) throws
         ClassNotFoundException, SQLException {
         Class.forName(driver);
         if (StringUtil.isNullOrEmpty(dbUser) || StringUtil.isNullOrEmpty(dbPass)) {
             return DriverManager.getConnection(url);
         } else {
             return DriverManager.getConnection(url, dbUser, dbPass);
         }
     }
 
     public String getSyncUserName() {
         return readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_SYNC_USERNAME, false);
     }
 
     public String getSyncUserPassword() {
         return readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_SYNC_PASSWORD, false);
     }
 
     public String getExternUserName() {
         return readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_EXTERN_USERNAME, false);
     }
 
     public String getExternUserPassword() {
         return readString(properties, propertiesPrefix + GenericDatabaseAdapter.PROPS_EXTERN_PASSWORD, false);
     }
 
     @Override
     public Connection getConnection() throws SQLException {
         return getConnection(getSyncUserName(), getSyncUserPassword());
     }
 
     @Override
     public Connection getConnection(String username, String password) throws SQLException {
         try {
             return create(username, password);
         } catch (Exception ex) {
             throw new SQLException("Can't read connection data", ex);
         }
     }
 
     private void readConfig() throws IOException {
         String filePath;
         switch (dbType) {
             case MYSQL:
                 filePath = MySqlDatabaseAdapter.MYSQL_CONFIG_FILE;
                 break;
             case POSTGRESQL:
                 filePath = PostgresDatabaseAdapter.POSTGRE_CONFIG_FILE;
                 break;
             case SQLITE:
                 filePath = GenericDatabaseAdapter.SQLITE_CONFIG_FILE;
                 break;
             default:
                 throw new UnsupportedOperationException("Unsupported database");
         }
         InputStream in = getClass().getResourceAsStream(filePath);
         properties = new Properties();
         properties.load(in);
     }
 
     /**
      * @return Unmodifiable list of created connections.
      */
     public List<Connection> getCreatedConnections() {
         return Collections.unmodifiableList(createdConnections);
     }
 
     //<editor-fold defaultstate="collapsed" desc=" -------- Not implemented methods -------- " >
     @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
     public PrintWriter getLogWriter() throws SQLException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setLogWriter(PrintWriter out) throws SQLException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setLoginTimeout(int seconds) throws SQLException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int getLoginTimeout() throws SQLException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public <T> T unwrap(Class<T> iface) throws SQLException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean isWrapperFor(Class<?> iface) throws SQLException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     //</editor-fold>
 
     //</editor-fold>
     //<editor-fold defaultstate="expanded" desc=" Inner types " >
     public enum SupportedDatabases {
 
         POSTGRESQL, MYSQL, SQLITE;
     };
     //</editor-fold>
 }
