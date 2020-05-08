 package com.opower.connectionpool;
 
 import java.util.Properties;
 
 /**
  * An interface to configure {@link ConnectionPoolManager} properties
  */
 public interface PoolConfiguration {
 
    public static final int RECONNECT_USER_PROP = "user";
    public static final int RECONNECT_PASSWORD_PROP = "password";
 
     /**
      * Drivername to be used by the pool to make a connection.
      * Make sure you have the corresponding jar file in the classpath.
      *
      * @param - driverName to be used by this pool
      */
     public void setDriverName(String driverName);
 
     /**
      * Drivername to be used by the pool to make a connection.
      * Make sure you have the corresponding jar file in the classpath.
      *
      * @return - driverName used by this pool
      */
     public String getDriverName();
 
     /**
      * Maximum number of connections that a {@link ConnectionPoolManager} can hold.
      * Set this to -1 for unlimited connections
      * typical values are from 10-100
      *
      * @param - maxConnections for this pool
      */
     public void setMaxConnections(int maxConnections);
 
     /**
      * Maximum number of connections that a {@link ConnectionPoolManager} can hold.
      * Typical values are from 10-100
      * We cannot allow unlimited connections for a pool
      * Cannot be less than or equal to 0.
      *
      * @return - maximum number of connections of this pool
      */
     public int getMaxConnections();
 
     /**
      * Initial number of connections created by the connection pool
      *
      * @param - initialSize for this pool
      */
     public void setInitialSize(int initialSize);
 
     /**
      * Initial number of connections created by the connection pool
      *
      * @return - initial size of this pool
      */
     public int getInitialSize();
 
     /**
      * Time {@link ConnectionPoolManager} waits for a connection to be
      * available before throwing an exception {@link java.sql.SQLException},
      * when maximum number of connections is reached.
      * Typical values are from 10,000 to 60,000
      *
      * @param - maxWait in milliseconds
      */
     public void setMaxWait(int maxWait);
 
     /**
      * Time {@link ConnectionPoolManager} waits for a connection to be
      * available before throwing an exception {@link java.sql.SQLException},
      * when maximum number of connections is reached.
      *
      * @return - maxWait in milliseconds used by this pool
      */
     public int getMaxWait();
 
     /**
      * {@link java.util.Properties} required by the {@link ConnectionPoolManager}
      * to reconnect using {@link java.sql.Driver}
      *
      * @param properties with username and password for reconnection at the minimum
      */
     public void setURLProperties(Properties properties);
 
     /**
      * {@link java.util.Properties} required by the {@link ConnectionPoolManager}
      * to reconnect using {@link java.sql.Driver}
      *
      * @return properties used by the  {@link ConnectionPoolManager} to reconnect
      */
     public Properties getURLProperties();
 
     /**
      * Set all the default properties for this pool.
      */
     public void setDefaults();
 
 
     /**
      * Set the configuration properties using {@link java.util.Properties}.
      *
      * @param - props: {@link java.util.Properties} to be used to set the pool properties
      */
     public void setUsingProperties(Properties props);
 
 }
