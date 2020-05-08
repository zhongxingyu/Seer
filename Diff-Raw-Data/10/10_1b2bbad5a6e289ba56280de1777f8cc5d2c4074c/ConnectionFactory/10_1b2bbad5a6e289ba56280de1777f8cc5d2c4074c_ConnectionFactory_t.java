 /*
  * ConnectionFactory.java -
  * Copyright (C) 2003 Klaus Rennecke, all rights reserved.
  *
  * Created on Apr 6, 2003 by marion@users.sourceforge.net
  */
 package net.sourceforge.fraglets.zeig.jdbc;
 
 import java.lang.ref.Reference;
 import java.lang.ref.SoftReference;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import org.apache.log4j.Category;
 
 /**
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.9 $
  */
 public class ConnectionFactory {
     
     public static final String RESOURCE_CONNECTION_URL =
         "net.sourceforge.fraglets.zeig.jdbc.connectionUrl";
     
     protected static ConnectionFactory instance;
     
     private Connection connection;
     
     private String connectionUrl;
     
     private HashMap preparedStatements;
     
     private Properties environment;
     
     public ConnectionFactory() {
         this(null, null);
     }
     
     /**
      * @param environment
      */
     public ConnectionFactory(Properties environment)  {
         this(null, environment);
     }
 
     public ConnectionFactory(String connectionUrl, Properties environment) {
         this.environment = new Properties(System.getProperties());
         this.environment.setProperty("useUnicode", "true");
         if (environment != null) {
             this.environment.putAll(environment);
         }
         if (connectionUrl != null) {
             this.connectionUrl = connectionUrl;
         } else {
             this.connectionUrl = this.environment
                 .getProperty(RESOURCE_CONNECTION_URL);
         }
         Category cat = Category.getInstance(ConnectionFactory.class);
         if (cat.isDebugEnabled()) {
             cat.debug("new connection factory: env="
                 + environment + ", url=" + connectionUrl);
         }
     }
     
     public PreparedStatement prepareStatement(String sql) throws SQLException {
         PreparedStatement result = null;
         if (preparedStatements != null) {
             Reference reference = (Reference)preparedStatements.get(sql);
             if (reference != null) {
                 result = (PreparedStatement)reference.get();
                 if (result == null) {
                     preparedStatements.remove(sql);
                 }
             }
         }
         
         if (result == null) {
             Category cat = Category.getInstance(ConnectionFactory.class);
             if (cat.isDebugEnabled()) {
                 cat.debug("prepare statement: "+sql);
             }
             result = getConnection().prepareStatement(sql);
             if (preparedStatements == null) {
                 synchronized (this) {
                     if (preparedStatements == null) {
                         preparedStatements = new HashMap();
                     }
                 }
             }
             synchronized (preparedStatements) {
                 preparedStatements.put(sql, new SoftReference(result));
             }
         }
         
         return result;
     }
     
     public int getLastId() throws SQLException {
         PreparedStatement ps = prepareStatement("select last_insert_id()");
         ResultSet rs = ps.executeQuery();
         try {
             if (rs.next()) {
                 return rs.getInt(1);
             } else {
                 throw new SQLException("could not get created ID");
             }
         } finally {
             rs.close();
         }
     }
     
     public int executeInsert(PreparedStatement ps, int count) throws SQLException {
         synchronized (ps.getConnection()) {
             int rows  = ps.executeUpdate();
             
             if (rows == count) {
                 return getLastId();
             } else {
                 throw new SQLException
                     ("unexpected number of rows updated: "+rows+", expected: "+count);
             }
         }
     }
     
     /**
      * @param connection
      */
     private void setConnection(Connection connection) {
         this.connection = connection;
     }
 
     /**
      * @return
      */
     private Connection getConnection() throws SQLException {
         if (connection == null) {
             synchronized (this) {
                 if (connection == null) {
                     try {
                         Category cat = Category.getInstance(ConnectionFactory.class);
                         if (cat.isDebugEnabled()) {
                             cat.debug("open connection: "+getConnectionUrl());
                         }
                         Class.forName("com.mysql.jdbc.Driver");
                         return connection = DriverManager
                             .getConnection(getConnectionUrl(), environment); 
                     } catch (ClassNotFoundException ex) {
                         throw new SQLException("driver not found: "+ex);
                     }
                 }
             }
         }
         return connection;
     }
 
     /**
      * @return
      */
     public String getConnectionUrl() {
         if (connectionUrl == null) {
             synchronized (this) {
                 if (connectionUrl == null) {
                     return connectionUrl = ResourceBundle.getBundle("connection")
                         .getString(RESOURCE_CONNECTION_URL);
                 }
             }
         }
         return connectionUrl;
     }
 
     /**
      * @param string
      */
     public void setConnectionUrl(String string) {
         connectionUrl = string;
     }
 
     public synchronized void close() throws SQLException {
         Category cat = Category.getInstance(ConnectionFactory.class);
         if (cat.isDebugEnabled()) {
             cat.debug("close connection: "+getConnectionUrl());
         }
         if (connection != null) {
             connection.close();
             connection = null;
         }
     }
     
     public static String getConnectionURL(String host, int port, String database) {
        if (port > 0) {
             return "jdbc:mysql://"+host+':'+port+'/'+database;
        } else {
            return "jdbc:mysql://"+host+'/'+database;
         }
     }
 }
