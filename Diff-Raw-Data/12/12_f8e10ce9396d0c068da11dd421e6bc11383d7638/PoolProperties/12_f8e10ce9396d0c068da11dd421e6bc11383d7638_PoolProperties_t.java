 package com.opower.connectionpool;
 
 import java.util.Properties;
 
 /**
  * Connection pool properties 
  */
 public class PoolProperties {
 
     /**
      * Drivername to be used by the ConnectionPoolManager.
      * Make sure you have the corresponding jar file in the classpath. 
      */
     public String driverName;
 
     /**
      * Maximum number of connections that a {@link ConnectionPoolManager} should hold.
      */
     public int maxConnections;
 
     /**
      * Initial number of connections created by the connection pool 
      */
     public int initialSize;
 
     /**
     * Maxinum number of milliseconds {@link ConnectionPoolManager} waits for a connection to be available before throwing an exception, when maximum number of connections is reached.
      */
     public int maxWait;
 
     /**
      * DEFAULT Values
      */
    public static final String DEFAULT_DRIVERNAME = "com.mysql.jdbc.Driver";
 
     public static final int DEFAULT_MAX_CONNECTIONS = 50;
     public static final int DEFAULT_INITIAL_SIZE = 10;
     public static final int DEFAULT_MAX_WAIT = 3000; // 3 seconds
 
     public PoolProperties(boolean setDefaultValues) {
         if (setDefaultValues) {
             this.setDefaults();
         }
     }
 
     public PoolProperties(Properties props) {
         this.driverName = props.getProperty("driverName", DEFAULT_DRIVERNAME);
         this.maxConnections = Integer.parseInt(
                 props.getProperty("maxConnections", "" + DEFAULT_MAX_CONNECTIONS));
         this.initialSize = Integer.parseInt(
                 props.getProperty("initialSize", "" + DEFAULT_INITIAL_SIZE));
         this.maxWait = Integer.parseInt(
                 props.getProperty("maxWait", "" + DEFAULT_MAX_WAIT));
     }
 
     private void setDefaults() {
         this.driverName = DEFAULT_DRIVERNAME;
         this.maxConnections = DEFAULT_MAX_CONNECTIONS;
         this.initialSize = DEFAULT_INITIAL_SIZE;
         this.maxWait = DEFAULT_MAX_WAIT;
     }
 }
