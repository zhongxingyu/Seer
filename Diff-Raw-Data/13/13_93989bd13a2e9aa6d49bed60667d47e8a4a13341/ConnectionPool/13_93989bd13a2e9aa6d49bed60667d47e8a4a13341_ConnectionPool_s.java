 package org.nikki.threadedsql;
 
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 /**
  * A Thread-Safe connection pool, In the future, if needed a class can be a
  * DatabaseConnection sub-instance to add onto the pooled connection types
  * 
  * @author Nikki
  * 
  */
 public class ConnectionPool<T extends DatabaseConnection> {
 
 	/**
 	 * The configuration to use for this pool
 	 */
 	private DatabaseConfiguration configuration;
 
 	/**
 	 * The max connections this pool can have.
 	 */
 	private int maxConnections;
 
 	/**
 	 * The amount of current connections..
 	 */
 	private int currentConnections = 0;
 
 	/**
 	 * A thread-safe linked queue which contains our connections
 	 */
 	private Queue<DatabaseConnection> pool = new ConcurrentLinkedQueue<DatabaseConnection>();
 
 	/**
 	 * Create a new database pool
 	 * 
 	 * @param configuration
 	 *            The configuration
 	 */
 	public ConnectionPool(DatabaseConfiguration configuration) {
 		this(configuration, 10);
 	}
 
 	/**
 	 * Create a new database pool
 	 * 
 	 * @param configuration
 	 *            The configuration
 	 * @param maxConnections
 	 *            The connection limit
 	 */
 	public ConnectionPool(DatabaseConfiguration configuration,
 			int maxConnections) {
 		this.configuration = configuration;
 		this.maxConnections = maxConnections;
 		
 		
 		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
 			public void run() {
 				//Ping!
				List<DatabaseConnection> remove = new LinkedList<DatabaseConnection>();
				for(DatabaseConnection connection : pool) {
 					try {
 						connection.getConnection().createStatement().execute("/* ping */ SELECT 1");
 					} catch (SQLException e) {
						remove.add(connection);
 					}
 				}
				for(DatabaseConnection r : remove) {
					pool.remove(r);
				}
 			}
 		}, 0, 30000, TimeUnit.MILLISECONDS);
 	}
 
 	/**
 	 * Get the next free database connection
 	 * 
 	 * @return The connection if found, or a new connection.
 	 */
 	@SuppressWarnings("unchecked")
 	public DatabaseConnection nextFree() {
 		// First check if a connection is free
 		DatabaseConnection connection = pool.poll();
 		if (connection != null) {
 			if (!connection.isFresh()) {
 				// DISCARD, since the connection is bad
 				currentConnections--;
 				return nextFree();
 			}
 			return connection;
 		}
 		if (currentConnections >= maxConnections) {
 			return null;
 		}
 		// If we don't find a connection, create a new one!
 		connection = configuration.newConnection();
 		connection.setPool((ConnectionPool<DatabaseConnection>) this);
 		if (!connection.connect()) {
 			throw new RuntimeException("Connection was unable to connect!");
 		} else {
 			currentConnections++;
 		}
 		return connection;
 	}
 
 	/**
 	 * Return a connection to this pool
 	 * 
 	 * @param connection
 	 *            The connection
 	 */
 	public void returnConnection(DatabaseConnection connection) {
 		pool.offer(connection);
 	}
 }
