 package com.mymed.controller.core.manager.connection;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The manager of the connections and of the pools. It is a singleton since all
  * it does is providing the methods for accessing the pool. The logic is all in
  * the pool.
  * 
  * To request a connection it is just necessary to call the {@code checkOut()}
  * methods with the appropriate parameters. If a pool for that connection is not
  * available, it will be created, otherwise it will get the one already
  * available and return a connection from there.
  * <p>
  * To return a connection to the pool, it is necessary to use the
  * {@code checkIn()} method.
  * <p>
  * The {@code release()} method is not yet implemented.
  * 
  * @author Milo Casagrande
  * 
  */
 public final class ConnectionManager implements IConnectionManager {
 
	/**
	 * The singleton instance
	 */
 	private static final ConnectionManager INSTANCE = new ConnectionManager();
 
	/**
 	 * This is where we store all the pools. Each key is the "name" of the pool
 	 * that is composed from the address and the port of the connection.
 	 */
 	private final Map<String, IConnectionPool> pools;
 
	/**
	 * Private constructor to initialize all the stuff.
	 */
 	private ConnectionManager() {
 		pools = new HashMap<String, IConnectionPool>();
 	}
 
 	/**
 	 * Get the instance of the manager
 	 * 
 	 * @return the singleton instance of the ConnectionManager
 	 */
 	public static ConnectionManager getInstance() {
 		return INSTANCE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.manager.connection.IConnectionManager#checkOut
 	 * (java.lang.String, int)
 	 */
 	@Override
 	public IConnection checkOut(final String address, final int port) {
 		return checkOut(address, port, 0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.manager.connection.IConnectionManager#checkOut
 	 * (java.lang.String, int, int)
 	 */
 	@Override
 	public IConnection checkOut(final String address, final int port, final int capacity) {
 		IConnectionPool pool = pools.get(address + port);
 
 		if (pool == null) {
 			pool = createPool(address, port, capacity);
 		}
 
 		return pool.checkOut();
 	}
 
 	/**
 	 * Create a new pool for the given connection parameters
 	 * 
 	 * @param address
 	 *            the address of the connection
 	 * @param port
 	 *            the port to use
 	 * @param capacity
 	 *            how many connection to keep in the pool
 	 * @return a new pool for the connection
 	 */
 	private IConnectionPool createPool(final String address, final int port, final int capacity) {
 		final IConnectionPool pool = new ConnectionPool(address, port, capacity);
 		pools.put(address + port, pool);
 
 		return pool;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.manager.connection.IConnectionManager#checkIn
 	 * (com.mymed.controller.core.manager.connection.IConnection)
 	 */
 	@Override
 	public void checkIn(final IConnection connection) {
 		final String address = ((Connection) connection).getAddress();
 		final int port = ((Connection) connection).getPort();
 		final IConnectionPool pool = pools.get(address + port);
 
 		pool.checkIn(connection);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.manager.connection.IConnectionManager#getPoolSize
 	 * (java.lang.String, int)
 	 */
 	@Override
 	public int getPoolSize(final String address, final int port) {
 		final IConnectionPool pool = pools.get(address + port);
 		return pool.getSize();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mymed.controller.core.manager.connection.IConnectionManager#release()
 	 */
 	@Override
 	public void release() {
 		// TODO Not yet implemented
 	}
 }
