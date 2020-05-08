 /*
  * RapidContext JDBC plug-in <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.plugin.jdbc;
 
 import org.rapidcontext.app.ApplicationContext;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.data.PropertiesSerializer;
 import org.rapidcontext.core.storage.StorageException;
 import org.rapidcontext.core.type.Channel;
 import org.rapidcontext.core.type.Connection;
 import org.rapidcontext.core.type.ConnectionException;
 
 import java.sql.Driver;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 /**
  * A JDBC connectivity adapter. This adapter allows execution of SQL
  * queries and statements to any JDBC data source. Connections may be
  * pooled for maximum resource utilization.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class JdbcConnection extends Connection {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(JdbcConnection.class.getName());
 
     /**
      * The JDBC driver configuration parameter name.
      */
     protected static final String JDBC_DRIVER = "driver";
 
     /**
      * The JDBC URL configuration parameter name.
      */
     protected static final String JDBC_URL = "url";
 
     /**
      * The JDBC user configuration parameter name.
      */
     protected static final String JDBC_USER = "user";
 
     /**
      * The JDBC password configuration parameter name.
      */
     protected static final String JDBC_PASSWORD = "password";
 
     /**
      * The JDBC SQL ping configuration parameter name (optional,
      * defaults to 'SELECT 1').
      */
     protected static final String JDBC_PING = "sqlping";
 
     /**
      * The JDBC auto-commit configuration parameter name (optional,
      * defaults to false).
      */
     protected static final String JDBC_AUTOCOMMIT = "autocommit";
 
     /**
      * The JDBC connection and query timeout configuration parameter name.
      */
     protected static final String JDBC_TIMEOUT = "timeout";
 
     /**
      * Creates a new JDBC connection from a serialized representation.
      *
      * @param id             the object identifier
      * @param type           the object type name
      * @param dict           the serialized representation
      */
     public JdbcConnection(String id, String type, Dict dict) {
         super(id, type, dict);
     }
 
     /**
      * Initializes this connection after loading it from a storage.
      *
      * @throws StorageException if the initialization failed
      */
     protected void init() throws StorageException {
         String  driver;
         String  url;
         String  ping;
 
         driver = dict.getString(JDBC_DRIVER, "").trim();
         url = dict.getString(JDBC_URL, "").trim().toLowerCase();
         ping = dict.getString(JDBC_PING, "").trim();
         if (driver.isEmpty()) {
             if (url.startsWith("jdbc:odbc")) {
                 dict.set("_" + JDBC_DRIVER, "sun.jdbc.odbc.JdbcOdbcDriver");
             } else if (url.startsWith("jdbc:mysql:")) {
                 dict.set("_" + JDBC_DRIVER, "com.mysql.jdbc.Driver");
             } else if (url.startsWith("jdbc:postgresql:")) {
                 dict.set("_" + JDBC_DRIVER, "org.postgresql.Driver");
             } else if (url.startsWith("jdbc:oracle:")) {
                 dict.set("_" + JDBC_DRIVER, "oracle.jdbc.driver.OracleDriver");
             } else if (url.startsWith("jdbc:db2:")) {
                 dict.set("_" + JDBC_DRIVER, "COM.ibm.db2.jdbc.app.DB2Driver");
             } else if (url.startsWith("jdbc:microsoft:")) {
                 dict.set("_" + JDBC_DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
             }
         } else {
             dict.set("_" + JDBC_DRIVER, driver);
         }
         if (ping.isEmpty() && url.startsWith("jdbc:oracle:")) {
            dict.set("_" + JDBC_PING, "SELECT 1 FROM dual");
         } else if (ping.isEmpty()) {
             dict.set("_" + JDBC_PING, "SELECT 1");
         } else {
             dict.set("_" + JDBC_PING, ping);
         }
         dict.setBoolean("_" + JDBC_AUTOCOMMIT, autoCommit());
         dict.setInt("_" + JDBC_TIMEOUT, timeout());
         super.init();
     }
 
     /**
      * Destroys this connection. This method overrides the default to
      * provide package access to it when testing connections.
      */
     protected void destroy() {
         super.destroy();
     }
 
     /**
      * Returns the JDBC driver class for this connection. The class
      * will be loaded using the application context class loader.
      *
      * @return the JDBC driver class
      *
      * @throws ConnectionException if the class couldn't be found
      *             or wasn't of the correct Java type
      */
     public Driver driver() throws ConnectionException {
         ClassLoader  loader;
         String       driverClass;
         String       msg;
 
         if (dict.containsKey("_" + JDBC_DRIVER)) {
             driverClass = dict.getString("_" + JDBC_DRIVER, "");
         } else {
             driverClass = dict.getString(JDBC_DRIVER, "");
         }
         try {
             loader = ApplicationContext.getInstance().getClassLoader();
             return (Driver) loader.loadClass(driverClass).newInstance();
         } catch (ClassNotFoundException e) {
             msg = "couldn't find or load JDBC driver class " + driverClass +
                   ": " + e.getMessage();
             throw new ConnectionException(msg);
         } catch (ClassCastException e) {
             msg = "couldn't load JDBC driver, must be an instance of " +
                   "java.sql.Driver: " + driverClass;
             throw new ConnectionException(msg);
         } catch (Exception e) {
             msg = "couldn't create JDBC driver instance of " + driverClass +
                   ": " + e.getMessage();
             throw new ConnectionException(msg);
         }
     }
 
     /**
      * Returns the JDBC connection URL.
      *
      * @return the JDBC connection URL
      */
     public String url() {
         return dict.getString(JDBC_URL, "");
     }
 
     /**
      * Returns the SQL ping query.
      *
      * @return the SQL ping query, or
      *         null if not configured
      */
     public String ping() {
         if (dict.containsKey("_" + JDBC_PING)) {
             return dict.getString("_" + JDBC_PING, null);
         } else {
             return dict.getString(JDBC_PING, null);
         }
     }
 
     /**
      * Returns the auto-commit (after each SQL) flag.
      * 
      * @return the auto-commit flag
      */
     public boolean autoCommit() {
         if (dict.containsKey("_" + JDBC_AUTOCOMMIT)) {
             return dict.getBoolean("_" + JDBC_AUTOCOMMIT, false);
         } else {
             return dict.getBoolean(JDBC_AUTOCOMMIT, false);
         }
     }
 
     /**
      * Returns the connection and query timeout (in seconds).
      *
      * @return the connection and query timeout (in seconds)
      */
     public int timeout() {
         try {
             if (dict.containsKey("_" + JDBC_TIMEOUT)) {
                 return dict.getInt("_" + JDBC_TIMEOUT, 30);
             } else {
                 return dict.getInt(JDBC_TIMEOUT, 30);
             }
         } catch (Exception e) {
             LOG.warning(this + ": failed to parse timeout value: " +
                         dict.get(JDBC_TIMEOUT));
             dict.setInt("_" + JDBC_TIMEOUT, 30);
             return 30;
         }
     }
 
     /**
      * Creates a new connection channel.
      *
      * @return the channel created
      *
      * @throws ConnectionException if the channel couldn't be created
      *             properly
      */
     protected Channel createChannel() throws ConnectionException {
         Properties   props;
 
         props = PropertiesSerializer.toProperties(dict);
         props.remove(KEY_ID);
         props.remove(KEY_TYPE);
         props.remove(KEY_MAX_OPEN);
         props.remove(KEY_MAX_IDLE_SECS);
         return new JdbcChannel(this, props);
     }
 
     /**
      * Destroys a connection channel, freeing any resources used
      * (such as database connections, networking sockets, etc).
      *
      * @param channel        the channel to destroy
      */
     protected void destroyChannel(Channel channel) {
         ((JdbcChannel) channel).close();
     }
 }
