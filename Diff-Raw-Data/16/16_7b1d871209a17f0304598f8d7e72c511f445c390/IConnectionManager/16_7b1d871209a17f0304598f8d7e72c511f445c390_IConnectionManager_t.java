 package com.mymed.controller.core.manager.connection;
 
 /**
  * Common interface for a connection manager
  * 
  * @author Milo Casagrande
  * 
  */
 public interface IConnectionManager {
 	/**
 	 * Take a connection from the pool. If there is no pool, a new one is
 	 * created with capacity set to {@code capacity}.
 	 * 
 	 * @param address
 	 *            the address of the connection
 	 * @param port
 	 *            the port of the connection
 	 * @param capacity
 	 *            the capacity of the pool
 	 * @return an open connection, or null
 	 */
 	IConnection checkOut(String address, int port, int capacity);
 
 	/**
 	 * Take a connection from the pool. If there is no pool, a new one is
 	 * initialized with capacity set to zero, meaning that there is no limit in
 	 * the pool.
 	 * 
 	 * @param address
 	 *            the address of the connection
 	 * @param port
 	 *            the port of the connection
 	 * @return an open connection, or null
 	 */
 	IConnection checkOut(String address, int port);
 
 	/**
 	 * Give a connection back to the pool
 	 * 
 	 * @param connection
 	 *            the connection to return to the pool
 	 */
 	void checkIn(IConnection connection);
 
 	/**
 	 * Release all the connection in the pool
	 * <p>
	 * NOT IMPLEMENTED YET
 	 */
 	void release();
 
 	/**
 	 * Retrieve the actual size of the pool identified by this address and port
 	 * 
 	 * @param address
 	 *            the address of the connection
 	 * @param port
 	 *            the port of the connection
 	 * @return the size of the pool identified by address and port
 	 */
 	int getPoolSize(String address, int port);
 }
