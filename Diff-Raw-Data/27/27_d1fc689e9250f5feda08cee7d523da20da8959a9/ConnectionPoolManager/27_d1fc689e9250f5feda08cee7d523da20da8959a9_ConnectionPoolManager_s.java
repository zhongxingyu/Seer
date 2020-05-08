 package com.opower.connectionpool;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.DriverManager;
 import java.sql.Driver;
 import java.util.LinkedList;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.log4j.Logger;
 
 /**
  * Connection pool manager
  */
 public class ConnectionPoolManager implements ConnectionPool {
 
     /**
      * URL with [host] [port] [database] eg., "jdbc:mysql://localhost:3306/beluga?autoReconnect=true"
      */
     private String url;
     public String getUrl() {
         return this.url;
     }
     /**
      * Username to connect to the database
      */
     private String user;
     public String getUser() {
         return this.user;
     }
     /**
      * Password to connect to the database
      */
     private String pass;
     public String getPass() {
         return this.pass;
     }
 
     private Driver driver = null;
     public Driver getDriver() { return this.driver; }
 
     /**
      * Logger
      */
     public static final Logger log = Logger.getLogger(ConnectionPoolManager.class);
 
     /**
      * Size of the pool at any given time.
      * Incremented only at two places
      * One at {@link #createAndAdd}, when we create new connection
      * if no connection is available and another at {@link #initializePool},
      * when a new {@link ConnectionPoolManager} is instantiated.
      */
     private AtomicInteger size = new AtomicInteger(0);
 
     /**
      * Atomic Flag to see if pool is closed
      */
     private AtomicBoolean closed = new AtomicBoolean(false);
 
     /**
      * List of connections currently used by the clients
      */
     private BlockingQueue<Connection> busyConnections;
 
     /**
      * List of connections available to the clients
      */
     private BlockingQueue<Connection> availableConnections;
 
     /**
      * Pool properties for this instance of {@link ConnectionPoolManager}
      */
     private PoolConfiguration props;
     public void setProps(PoolConfiguration props) { this.props = props; }
     public PoolConfiguration getProps() { return this.props; }
 
     /**
      * Constructor with a given {@link PoolConfiguration}
      */
     public ConnectionPoolManager(PoolConfiguration props, String url, String user, String pass) throws SQLException {
         this.props = props;
         this.url = url;
         this.user = user;
         this.pass = pass;
         this.initializePool();
     }
 
     /**
      * Constructor with default {@link PoolProperties}
      */
     public ConnectionPoolManager(String url, String user, String pass) throws SQLException {
         this(new PoolProperties(true), url, user, pass);
     }
 
     /**
      * Initialize connection pool with {@link PoolProperties}.initialSize
      */
     protected void initializePool() throws SQLException {
 
         try {
             this.propertiesCheck();
         } catch (PoolConfigurationException e) {
             throw new SQLException("Failed to initialize a Connection Pool", e);
         }
 
         this.availableConnections = new ArrayBlockingQueue<Connection>(this.props.getMaxConnections(), true);
         this.busyConnections = new ArrayBlockingQueue<Connection>(this.props.getMaxConnections(), false);
 
         for (int i = 0; i < this.props.getInitialSize(); i++) {
             this.availableConnections.offer(this.createNewConnection());
             this.size.addAndGet(1);
         }
     }
 
     /**
      * Sanity check for current {@link PoolConfiguration} properties
      */
     protected void propertiesCheck() throws PoolConfigurationException {
         if (this.props == null) {
             throw new PoolConfigurationException("Properties for the pool needs to be defined using " + PoolConfiguration.class);
         }
         try {
             this.driver = (Driver) Class.forName(
                                                 this.props.getDriverName(),
                                                 true,
                                                 this.getClass().getClassLoader()
                                                 ).newInstance();
         } catch (Exception e) {
             throw new PoolConfigurationException(
                     "Driver " + this.props.getDriverName() + " cannot be loaded." +
                     "Make sure driver classpath is included.", e);
         }
         if (this.props.getMaxConnections() <= 0) {
             log.warn("Maximum connections in the properties is <= 0. Setting it to default: " + PoolProperties.DEFAULT_MAX_CONNECTIONS);
             this.props.setMaxConnections(PoolProperties.DEFAULT_MAX_CONNECTIONS);
         }
         if (this.props.getInitialSize() > this.props.getMaxConnections() || this.props.getInitialSize() < 0) {
             log.warn("Initial size is " + this.props.getInitialSize() + ". Setting it to default: " + PoolProperties.DEFAULT_INITIAL_SIZE);
             this.props.setInitialSize(PoolProperties.DEFAULT_INITIAL_SIZE);
         }
         if (this.props.getMaxWait() <= 10) {
             log.warn("Maximum wait to throw an exception is set to less than 10. Setting it to default: " + PoolProperties.DEFAULT_MAX_WAIT);
             this.props.setMaxWait(PoolProperties.DEFAULT_MAX_WAIT);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Connection getConnection() throws SQLException {
         if (this.isClosed()) {
             throw new SQLException("Connection pool closed");
         }
         // Immediately return if a connection is available
         Connection conn = this.waitAndGet(0);
         if (conn != null) {
             // w00t!!
             return conn;
         } else {
             // Create a new connection iff pool-capacity not exceeded.
             conn = this.createAndAdd();
             if (conn != null) {
                 return conn;
             } else {
                 // Wait maxWait seconds for other threads to release a connection
                 long start = System.currentTimeMillis();
                 conn = this.waitAndGet(this.props.getMaxWait());
                 if (conn == null) {
                     if (System.currentTimeMillis() - start >= this.props.getMaxWait()) {
                         if (log.isDebugEnabled()) {
                             log.debug(this.capacityInfo("Timed out.", "\n"));
                         }
                         throw new SQLException("No available connection after waiting for " + (this.props.getMaxWait()/1000) + " seconds.");
                     }
                 }
 
             }
         }
         if (conn == null) {
             if (this.isClosed()) {
                 throw new SQLException("Connection pool closed");
             } else {
                 // Capacity exceeded?
                 if (log.isDebugEnabled()) {
                     log.debug(this.capacityInfo("Capacity might have been exceeded", "\n"));
                 }
                 throw new SQLException("Failed to retrieve a valid connection from the pool.");
             }
         } else {
             return conn;
         }
     }
 
     /**
      * Wait for an available valid connection and return one, if any.
      * If the connection is closed, try to reconnect it.
      * Otherwise remove it from busy queue and
      * return null, so the calling function can attempt to create a new one.
      *
      * @return {@link java.sql.Connection}
      */
     protected Connection waitAndGet(int wait) throws SQLException {
         Connection conn = null;
         try {
             conn = this.availableConnections.poll(wait, TimeUnit.MILLISECONDS);
             if (!this.offerToBusy(conn)) {
                 return null;
             }
         } catch (InterruptedException e) {
             throw new SQLException("Connection pool wait interrupted before " + wait + " milliseconds");
         }
         if (conn != null && conn.isClosed()) {
             if (!this.reconnect(conn)) {
                 if (!this.busyConnections.remove(conn)) {
                     if (log.isDebugEnabled()) {
                         log.debug(this.capacityInfo("Cannot remove Connection[" + conn +"] from busy connections.", "\n"));
                     }
                 }
                 conn = null;
             }
         }
         return conn;
     }
 
     protected Connection createAndAdd() throws SQLException {
         if (this.size.get() >= this.props.getMaxConnections()) {
             return null;
         }
         Connection conn = this.createNewConnection();
         if (!this.offerToBusy(conn)) {
             return null;
         } else {
             this.size.addAndGet(1);
             return conn;
         }
     }
 
    protected void reconnect(Connection conn) throws SQLException {
         this.disconnect(conn);
         if (this.driver != null) {
             this.props.updateURLProperties(this.user, this.pass);
             conn = this.driver.connect(this.url, this.props.getURLProperties());
             return true;
         }
         return false;
     }
 
     protected boolean disconnect(Connection conn) throws SQLException {
         if (conn != null) {
             conn.close();
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Create a brand new connection.
      *
      * @return {@link java.sql.Connection}
      */
     protected Connection createNewConnection() throws SQLException {
         return DriverManager.getConnection(this.url, this.user, this.pass);
     }
 
 
     private boolean offerToBusy(Connection conn) {
         if (conn == null) {
             return false;
         }
         if (!this.busyConnections.offer(conn)) {
             if (log.isDebugEnabled()) {
                 log.debug("Capacity exceeded!!");
             }
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void releaseConnection(Connection connection) throws SQLException {
         if (connection == null) {
             return;
         }
         if (this.isClosed()) {
             this.disconnect(connection);
             return;
         }
         boolean closeConnection = true;
         if (this.busyConnections.remove(connection)) {
             // Connection belongs to the pool. Decrement pool size
             this.size.decrementAndGet();
             if (this.size.get() >= this.props.getMaxConnections()) {
                 if (log.isDebugEnabled()) {
                     log.debug(this.capacityInfo("Maximum connections size exceeded. Cannot release Connection[" + connection + "] to the pool. Closing it.", "\n"));
                 }
             } else if (!this.availableConnections.offer(connection)) {
                 // Capacity exceeded?
                 if (log.isDebugEnabled()) {
                     log.debug(this.capacityInfo("Available connections size exceeded. Cannot release Connection[" + connection + "] to the pool. Closing it.", "\n"));
                 }
             } else {
                 // everything went fine. Connection released to the pool.
                 closeConnection = false;
             }
         } else {
             if (log.isDebugEnabled()) {
                 log.debug(this.capacityInfo("Failed to release a connection (Does not belong to the pool). Connection [" + connection + "] will be closed", "\n"));
             }
         }
         if (closeConnection) {
             connection.close();
         }
     }
 
     /**
      * Get the size of this pool.
      *
      * @return size - Thread safe read on the {@link #size size} of the pool
      */
     public int getSize() {
         return this.size.get();
     }
 
     /**
      * See if the current pool is closed and not usable.
      * Thread safe read on the {@link #close close} of the pool
      *
      * @return true if the connection pool has been closed
      */
     public boolean isClosed() {
         return this.closed.get();
     }
 
     /**
      * Get number of busy/used connections in this pool
      *
      * @return int - number of busy connections
      */
     public int getBusySize() {
         return this.busyConnections.size();
     }
 
     /**
      * Get number of available connections from this pool
      *
      * @return int number of available connections
      */
     public int getAvailableSize() {
         return this.availableConnections.size();
     }
 
     /**
      * Check if a connection belongs to a pool
      *
      * @param Connection - connection to check
      * @return boolean true if the connection belongs to the pool
      */
     public boolean containsConnection(Connection connection) {
         if (this.busyConnections.contains(connection)) {
             return true;
         } else if (this.availableConnections.contains(connection))  {
             return true;
         }
         return false;
     }
 
     /**
      * Closes and Clears all connections owned by this pool
      */
    public void close() throws SQLException {
         if (this.isClosed()) {
             return;
         }
         this.closed.set(true);
         BlockingQueue<Connection> pooledConnections = this.availableConnections;
         if (pooledConnections.size() == 0) {
             pooledConnections = busyConnections;
         }
         while (pooledConnections.size() > 0) {
             Connection conn = pooledConnections.poll(1, TimeUnit.SECONDS);
             if (pooledConnections == this.availableConnections) {
                 this.disconnect(conn);
                 if (pooledConnections.size() == 0) {
                     pooledConnections = busyConnections;
                 }
             }
         }
     }
 
     public String capacityInfo(String prefix, String delimiter) {
         return prefix + delimiter +
             "\tCurrent Capacity: " + this.size.get() + "; Specified Capacity: " + this.props.getMaxConnections() + delimiter +
             "\tAvailable Connections: " + this.availableConnections.size() + "; Busy Connections: " + this.busyConnections.size();
     }
 }
