 package com.opower.connectionpool;
 
 import java.util.Properties;
 import org.apache.log4j.Logger;
 
 /**
  * A {@link PoolConfiguration} implementation to configure {@link ConnectionPoolManager} properties
  */
 public class PoolProperties implements PoolConfiguration {
 
     /**
      * DEFAULT Values
      */
     public static final String DEFAULT_DRIVERNAME = "com.mysql.jdbc.Driver";
     public static final int DEFAULT_MAX_CONNECTIONS = 20;
     public static final int DEFAULT_INITIAL_SIZE = 10;
     public static final int DEFAULT_MAX_WAIT = 40000; // 40 seconds
 
 
     /**
      * Logger
      */
     public static final Logger log = Logger.getLogger(PoolProperties.class);
 
     private volatile String driverName;
     private volatile int maxConnections;
     private volatile int initialSize;
     private volatile int maxWait;
     private Properties URLProperties;
 
     /**
      * Constructor with default properties for the pool
      *
      * @param - setDefaultValues: boolean value if true, sets the default properties
      */
     public PoolProperties(boolean setDefaultValues) {
         if (setDefaultValues) {
             this.setDefaults();
         }
     }
 
     /**
      * Constructor using {@link java.util.Properties}
      *
      * @param - props: {@link java.util.Properties} to be used to set the pool properties
      */
     public PoolProperties(Properties props) {
         this.setUsingProperties(props);
     }
 
     /**
      * Blank Constructor
      */
     public PoolProperties() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setDriverName(String driverName) {
         this.driverName = driverName;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getDriverName() {
         return this.driverName;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setMaxConnections(int maxConnections) {
         this.maxConnections = maxConnections;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getMaxConnections() {
         return this.maxConnections;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setInitialSize(int initialSize) {
         this.initialSize = initialSize;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getInitialSize() {
         return this.initialSize;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setMaxWait(int maxWait) {
         this.maxWait = maxWait;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getMaxWait() {
         return this.maxWait;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setURLProperties(Properties properties) {
         this.URLProperties = properties;
     }
 
     /**
      * Update {@link #URLProperties} with user and password for reconnection
      * @param user user to be set
      * @param pass password to be set
      */
     public void updateURLProperties(String user, String pass) {
         if (this.URLProperties != null) {
             this.URLProperties = new Properties(this.URLProperties);
         } else {
            this.URLproperties = new Properties();
         }
         this.URLProperties.setProperty(PoolConfiguration.RECONNECT_USER_PROP, user);
         this.URLProperties.setProperty(PoolConfiguration.RECONNECT_PASSWORD_PROP, pass);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Properties getURLProperties() {
        return this.URLproperties;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setDefaults() {
         this.driverName = DEFAULT_DRIVERNAME;
         this.maxConnections = DEFAULT_MAX_CONNECTIONS;
         this.initialSize = DEFAULT_INITIAL_SIZE;
         this.maxWait = DEFAULT_MAX_WAIT;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setUsingProperties(Properties props) {
         this.driverName = props.getProperty("driverName", DEFAULT_DRIVERNAME);
         this.maxConnections = Integer.parseInt(
                 props.getProperty("maxConnections", "" + DEFAULT_MAX_CONNECTIONS));
         this.initialSize = Integer.parseInt(
                 props.getProperty("initialSize", "" + DEFAULT_INITIAL_SIZE));
         this.maxWait = Integer.parseInt(
                 props.getProperty("maxWait", "" + DEFAULT_MAX_WAIT));
     }
 
 }
