 package com.opower.connectionpool;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.DriverManager;
 import java.util.LinkedList;
 
 /**
  * Connection pool manager 
  */
 public class ConnectionPoolManager implements ConnectionPool {
     /**
      * URL with [host] [port] [database] eg., "jdbc:mysql://localhost:3306/beluga?autoReconnect=true"
      */
     private String url;
     /**
      * Username to connect to the database
      */
     private String user;
     /** 
      * Password to connect to the database
      */
     private String pass;
 
     /**
      * List of connections in the pool
      *
      * list of connections currently used by the clients
      * list of connections available to the clients
     */
     private LinkedList<Connection> usedConnections = new LinkedList<Connection>();
     private LinkedList<Connection> availableConnections = new LinkedList<Connection>();
 
     private PoolProperties props;
 
     public ConnectionPoolManager(PoolProperties props, String url, String user, String pass) throws SQLException {
         this.props = props;
         this.url = url;
         this.user = user;
         this.pass = pass;
         this.createConnectionPool();
     }
 
     /**
      * Constructor with default pool properties
      */
 
     public ConnectionPoolManager(String url, String user, String pass) throws SQLException {
         this(new PoolProperties(true), url, user, pass);
     }
 
     private void createConnectionPool() throws SQLException {
         try {
             Class.forName(this.props.driverName);
         } catch (ClassNotFoundException e) {
             throw new SQLException("Driver name " + this.props.driverName + " cannot be loaded."
                     + "Make sure driver classpath is included.", e);
         }
         for (int i = 0; i < this.props.initialSize; i++) {
             this.availableConnections.add(this.createNewConnection());
         }
     }
 
     /**
      * Gets a connection from the connection pool.
      * 
      * @return a valid connection from the pool.
      */
     @Override
     public Connection getConnection() throws SQLException {
         Connection conn = this.getFromAvailable();
         if (conn != null) {
             return conn;
         } else {
             conn = this.createAndAdd();
             if (conn != null) {
                 return conn;
             } else {
                 this.waitForAvailable();
                 conn = this.getFromAvailable();
                 if (conn != null) {
                     return conn;
                 }
                 throw new SQLException("Number of maximum connections exceeded: " + this.props.maxConnections);
             }
         }
     }
 
     /**
      * Releases a connection back into the connection pool.
      * 
      * @param connection the connection to return to the pool
      * @throws java.sql.SQLException
      */
     @Override
     public synchronized void releaseConnection(Connection connection) throws SQLException {
         if (!this.usedConnections.remove(connection)) {
             throw new SQLException("Failed to release connection. Connection is not being used by the pool.");
         }
         this.availableConnections.addFirst(connection);
     }
 
     public Connection createNewConnection() throws SQLException {
         return DriverManager.getConnection(this.url, this.user, this.pass);
     }
 
     public Connection getFromAvailable() throws SQLException {
         Connection conn = null;
         synchronized(this) {
             if (!this.availableConnections.isEmpty()) {
                 conn = this.availableConnections.removeFirst();
                 this.usedConnections.add(conn);
             }
         }
         return conn;
     }
 
     public Connection createAndAdd() throws SQLException {
         Connection conn = null;
         synchronized(this) {
             if (this.usedConnections.size() < this.props.maxConnections) {
                 conn = this.createNewConnection();
                 this.usedConnections.add(conn);
             }
         }
         return conn;
     }
 
     public void waitForAvailable() {
         long start = System.currentTimeMillis();
         while(this.availableConnections.isEmpty()) {
             long elapsed = System.currentTimeMillis() - start;
             if (elapsed > this.props.maxWait) {
                 return;
             }
         }
     }
 }
