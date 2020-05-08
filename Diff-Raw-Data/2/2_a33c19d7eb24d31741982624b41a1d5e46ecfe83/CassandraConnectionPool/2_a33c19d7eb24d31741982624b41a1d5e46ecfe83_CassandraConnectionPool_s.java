 package com.mindplex.cassandra.connection;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
import com.mindplex.cassandra.CassandraConnectionException;

 /**
  *
  * @author Abel Perez
  */
 public class CassandraConnectionPool
 {
     /**
      * The default logger for this connection pool. 
      */
     private static final Logger logger = Logger.getLogger(CassandraConnectionPool.class);
 
     /**
      * The list of connections available in this queue.
      */
     private final ArrayBlockingQueue<CassandraConnection> connections;
     
     /**
      * The default amount of max connections this pool will keep open
      * at any given time.
      */
     private static final int DEFAULT_MAX_CONNECTIONS = 5;
 
     /**
      * This constant value represents blocking forever in the context of
      * controlling thread access. 
      */
     private static final int BLOCK_FOREVER = -1;
 
     /**
      * The default max time to wait for a connection when this pool is
      * exhausted.  The default value -1 is to wait indefinitely. 
      */
     private static final int DEFAULT_MAX_WAIT_TIME_WHEN_EXHAUSTED = BLOCK_FOREVER;
 
     /**
      * The time interval to wait when polling this pool for a connection
      * while it's exhausted.
      */
     private static final int POLL_INTERVAL = 100;
 
     /**
      * The Cassandra host that the connections in this pool point to.
      */
     private String host;
 
     /**
      * The Cassandra port that the connections in this pool point to.
      */
     private int port;
 
     /**
      * The Cassandra keyspace that the connections in this pool point to.
      */
     private String keyspace;
 
     /**
      * The max time to wait for the next available connection in this pool
      * while the pool is exhausted.
      */
     private final int maxWaitTimeWhenExhausted;
     
     /**
      * Constructs this connection pool with the specified host, port, keyspace
      * pool access fairness, and max wait time when pool is exhausted.
      *
      * @param host the Cassandra host connections in the pool point to.
      * @param port the Cassandra port connections in the pool point to.
      * @param keyspace the Cassandra keyspace connections in the pool point to.
      * @param maxWaitTimeWhenExhausted the max wait time for the next available
      * connection when this pool is exhausted.     
      */
     private CassandraConnectionPool(String host, int port, String keyspace, int maxWaitTimeWhenExhausted) {
 
         this.host = host;
         this.port = port;
         this.keyspace = keyspace;
         this.maxWaitTimeWhenExhausted = maxWaitTimeWhenExhausted;
 
         connections = new ArrayBlockingQueue<CassandraConnection>(DEFAULT_MAX_CONNECTIONS);
         for (int i = 0; i < DEFAULT_MAX_CONNECTIONS; i++) {
             CassandraConnection connection = CassandraConnection.getInstance(keyspace);
             if (connection.open()) {
                 connections.add(connection);
             }
         }
     }
 
     /**
      * Creates an instance of this connection pool with the specified host,
      * port, keyspace and max wait time when for connections when pool is
      * exhausted.
      *
      * @param host the Cassandra host connections in the pool point to.
      * @param port the Cassandra port connections in the pool point to.
      * @param keyspace the Cassandra keyspace connections in the pool point to.
      *
      * @return a new instance of this connection pool based on the specified paramters.
      */
     public static CassandraConnectionPool getInstance(String host, int port, String keyspace) {
         return new CassandraConnectionPool(host, port, keyspace, DEFAULT_MAX_WAIT_TIME_WHEN_EXHAUSTED);
     }
 
     /**
      * Creates an instance of this connection pool with the specified host,
      * port, keyspace and max wait time when for connections when pool is
      * exhausted.
      *
      * @param host the Cassandra host connections in the pool point to.
      * @param port the Cassandra port connections in the pool point to.
      * @param keyspace the Cassandra keyspace connections in the pool point to.
      * @param maxWaitTimeWhenExhausted the max time to wait for the next
      * available connection in this pool while the pool is exhausted.
      *
      * @return a new instance of this connection pool based on the specified parameters.
      */
     public static CassandraConnectionPool getInstance(String host, int port, String keyspace, int maxWaitTimeWhenExhausted) {
         return new CassandraConnectionPool(host, port, keyspace, maxWaitTimeWhenExhausted);
     }
 
     /**
      * Gets the next available connection from this pool of Cassandra
      * connections.  When this pool is exhausted, this operation will block for
      * the total wait time this pool has been configured for.
      * 
      * @return a connection to Cassandra from this pool.
      * 
      * @throws CassandraConnectionException can occur if a connection cannot be
      * acquired.
      */
     public CassandraConnection borrowConnection() throws CassandraConnectionException {
 
         CassandraConnection connection = null;
 
         // If the max time to wait for a connection to become
         // available in the pool is forever, we continuously poll
         // the queue until a connection becomes available.
         
         if (maxWaitTimeWhenExhausted == BLOCK_FOREVER) {
             while (connection == null) {
                 try {
                     connection = connections.poll(POLL_INTERVAL, TimeUnit.MILLISECONDS);
                   
                 } catch (InterruptedException exception) {
                     logger.error("Interrupted while acquiring connection. [wait-time:"
                             +maxWaitTimeWhenExhausted+"]", exception);
                     break;
                 }
             }
         } else {
             // Wait the max time allowable for a connection to
             // become available in the pool; otherwise bail.
             try {
                 connection = connections.poll(maxWaitTimeWhenExhausted, TimeUnit.MILLISECONDS);
             } catch (InterruptedException exception) {
                 logger.error("Interrupted while acquiring connection. [wait-time:"
                         +maxWaitTimeWhenExhausted+"]", exception);
             }
         }
 
         // we are in bad shape, lets just throw up on the client.
         if (connection == null) {
             throw new CassandraConnectionException("Failed to acquire connection from pool.");
         }
         
         return connection;
     }
 
     /**
      * Returns the specified connection back to this pool.  If the specified
      * connection is no longer valid, then a new connection is created and
      * added to this pool.
      * 
      * @param connection the connection to return back to this pool.
      *
      * @return <tt>true</tt> if the connection is successfully returned
      * to this pool; otherwise <tt>false</tt>.
      */
     public boolean releaseConnection(CassandraConnection connection) {
 
         // no need to continue if the specified connection is bogus.
         if (connection == null) return false;
 
         try {
             // if the connection is valid we added back to this pool;
             // otherwise we create a new connection in its place and
             // add it to this pool.
 
             if (connection.isOpen()) {
                 return connections.add(connection);
             } else {
                 return connections.add(CassandraConnection.getInstance(host, port, keyspace));
             }
             
         } catch (IllegalStateException exception) {
             logger.error("Failed to return connection to pool. [max connections exceeded].");
             connection.close();
             return false;
         }
     }
 
     /**
      * Gets the Cassandra host connections in this pool point to.
      *
      * @return the Cassandra host connections in this pool point to.
      */
     public String getHost() {
         return host;
     }
 
     /**
      * Gets the Cassandra port connections in this pool point to.
      *
      * @return the Cassandra port connections in this pool point to.
      */
     public int getPort() {
         return port;
     }
 
     /**
      * Gets the Cassandra keyspace connections in this pool point to.
      *
      * @return the Cassandra keyspace connections in this pool point to.
      */
     public String getKeyspace() {
         return keyspace;
     }
 
     /**
      * Gets the max time to when for connections when this pool is exhausted.
      *
      * @return the max time to when for connections when this pool is exhausted.
      */
     public int getMaxWaitTimeWhenExhausted() {
         return maxWaitTimeWhenExhausted;
     }    
 }
