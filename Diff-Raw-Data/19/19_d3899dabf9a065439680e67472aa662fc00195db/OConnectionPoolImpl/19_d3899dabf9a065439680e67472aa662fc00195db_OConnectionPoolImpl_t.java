 package com.opower.connectionpool;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
import javax.management.RuntimeErrorException;

 import org.apache.log4j.Logger;
 
 /**
  * A singleton ConnectionPool that maintains a pool of OConnections. (Uses a LinkedList data structure).
  * It eagerly initializes the connections on instantiation so first time callers don't have to wait.
  * Uses a DataSource bean to determine the user settings. 
  * 
  * Maintains a map of connections to FutureTask. This is used to track the idle time of connections given from the pool.
  * Idle time is reset every time a connection method is invoked. Hence active connections are not considered abandoned
  * and are not forcibly recovered. Once idle time surpasses set limit, the connection
  * is released back into the pool by means of a scheduledTask. 
  * 
  * @author VinayG
  */
 public class OConnectionPoolImpl implements ConnectionPool {
 	
 	/* The One and only instance of the connection pool. */
 	private static final ConnectionPool connectionPoolInstance  = new OConnectionPoolImpl();
 	
 	/* The pool of connections.*/
 	private List<Connection> _connectionList;
 	
 	/* Map of releaseOnAbandonmentTaskHandle for every borrowed connection. */
 	private Map<Connection, ScheduledFuture> releaseOnAbandonmentTaskMap;
 	
 	/* Scheduler service that spawns time-delay based future tasks. */
 	private ScheduledExecutorService scheduler;
 	
 	/* The datasource bean for user properties. */
 	private DataSource ds;
 
 	private Logger log = Logger.getLogger(OConnectionPoolImpl.class.getName());
 	
 	public static ConnectionPool getInstance(){
 		return OConnectionPoolImpl.connectionPoolInstance;
 	}
 	
 	/* Constructor eagerly initializes the OConnection Pool. 
 	 * The list and map are synchronized in order to prevent multiple threads from simultaneously invoking getConnection
 	 * and corrupting the pool. */
 	private OConnectionPoolImpl() {
 		ds = DataSource.getInstance();
 		_connectionList = Collections.synchronizedList(new LinkedList<Connection>());
 		registerDriver();
 		initializePool();
 		initializeAbandonReleaseResources();
 	}
 	
 	private void initializeAbandonReleaseResources() {
 		releaseOnAbandonmentTaskMap = Collections.synchronizedMap(new HashMap<Connection, ScheduledFuture>());
 	    scheduler =  Executors.newScheduledThreadPool(ds._poolSize);
 	}
 
 	private void registerDriver(){
 		try {
 		   Class.forName(ds._driver);
 		}
 		catch(ClassNotFoundException ex) {
 		   log.error("Unable to load driver class!");
 		}
 	}
 	
 	/* Creates the physical connection and wraps it in the custom oConnection wrapper and returns it. */
	private Connection createConnection() {
 		OConnection oConnection  = null;
		Connection connection;
 		try {
			connection = DriverManager.getConnection(ds._url, ds._username, ds._password);
 		} catch (SQLException e) {
			throw new RuntimeException("Unable to establish connections to the database. Is the datasource.properties properfly formed? ");
 		}
		oConnection = new OConnection(connection, this);
 		return oConnection;
 	}
 
 	/* Fetches a connection from the pool provided one is available. Invokes addNewReleaseOnAbandonmentHandleToMap which
 	 * in turn schedules a releaseOnAbandonmentTask and Inserts the corresponding releaseOnAbandonmentHandle into the map. 
 	 * Throws exception if the pool is empty.*/
 	@Override
 	public Connection getConnection() throws SQLException {
 		if(_connectionList.isEmpty()){
 			throw new IllegalStateException("Connection Pool Currently Empty.");
 		} 
 		Connection connectionToReturn = _connectionList.remove(0);
         addNewReleaseOnAbandonmentHandleToMap(connectionToReturn);
         
 		return connectionToReturn;
 	}
 
 	/* Schedules a releaseOnAbandonmentTask and Inserts the corresponding releaseOnAbandonmentHandle into the map. 
 	 * releaseOnAbandonmentHandle is used to reset the  task when any activity is detected on the borrowed connection. */
 	private void addNewReleaseOnAbandonmentHandleToMap(Connection connectionBeingScheduled) {
 		Callable<?> releaseOnAbandonmentTask = getReleaseOnAbandonmentTask(connectionBeingScheduled);
 		final ScheduledFuture<?> releaseOnAbandonmentHandle =
 	            scheduler.schedule(releaseOnAbandonmentTask, ds._maxIdleTimeInSeconds, TimeUnit.SECONDS);
 		releaseOnAbandonmentTaskMap.put(connectionBeingScheduled, releaseOnAbandonmentHandle);
 		return;
 	}
 	
 	/* Utility method that returns the task being scheduled by the scheduler. 
 	 * This task is executed when a connection is deemed as abandoned. The task essentially
 	 * removes the handler from the map and reinserts the abandoned connection back into the pool. */
 	private Callable<?> getReleaseOnAbandonmentTask(final Connection connectionToReturn){
 		return new Callable<Object>() {
             public Object call() throws SQLException {
             	releaseOnAbandonmentTaskMap.remove(connectionToReturn);
             	OConnection oConnection = new OConnection(((OConnection)connectionToReturn)._connection, connectionPoolInstance);
             	((OConnection)connectionToReturn)._connection = null;
             	_connectionList.add(oConnection);
             	return null;
             }
 		};
 	}
 
 	/* If the connection that is passed in is already closed, it creates a new connection to reinsert in pool.
 	 * Else extracts the physical connection from the wrapper and reinserts it with a new wrapper in the pool.
 	 * The old wrappers physical connection is nullified. 
 	 * 
 	 * Creating/deleting Wrapper connections is not expensive as the physical connection is always 
 	 * extracted out (unless it was closed by the client).
 	 * 
 	 * Since its idempotent, multiple invocations can easily corrupt the pool. Hence, synchronizing to ensure the
 	 * connection is returned to the pool only once.
 	 */
 	@Override
 	public synchronized void releaseConnection(Connection connection) throws SQLException, IllegalStateException {
 		Connection oConnectionToBeInserted = null;
 		if(connection != null && _connectionList.size() < ds._poolSize){
 			if(connection instanceof OConnection){
 				OConnection oConnectionToBeReleased = (OConnection) connection;
 				if(oConnectionToBeReleased._connection == null){
 					log.debug("This connection has already been released.");
 				} else if(oConnectionToBeReleased.isClosed()){
 					log.debug("This connection has already been closed but not released.");
 					oConnectionToBeInserted = createConnection();
 					oConnectionToBeReleased._connection = null;
 				} else {
 					log.debug("This connection has not been closed. And releaseConnection has now been invoked.");
 					oConnectionToBeInserted = new OConnection(oConnectionToBeReleased._connection, this);
 					oConnectionToBeReleased._connection = null;
 				}
 			} else {
 				throw new IllegalStateException("Cannot release this connection as it didnt come from this pool."); 
 			}
 			if(oConnectionToBeInserted != null){
 				removeReleaseOnAbandonmentHandleFromMap(connection);
 				_connectionList.add(oConnectionToBeInserted);
 			}
 		}
 	}
 
 	/* Remove the  releaseOnAbandonmentHandle from the map for connections that are being released back to the pool. */
 	private void removeReleaseOnAbandonmentHandleFromMap(Connection connection) {
 		ScheduledFuture<?> releaseOnAbandonmentHandle = releaseOnAbandonmentTaskMap.remove(connection);
 		//Checking for null in case of multiple release or close invocations.
 		if(releaseOnAbandonmentHandle != null){
 			releaseOnAbandonmentHandle.cancel(true);
 		}
 	}
 
 	/* Invoked by the connection wrapper when any method on it is called by the client.
 	 * This ensures that the timer is reset and active connections dont get considered as abandoned. */
 	protected void resetAbandonedCheckTimer(OConnection oConnection) {
 		removeReleaseOnAbandonmentHandleFromMap(oConnection);
 		addNewReleaseOnAbandonmentHandleToMap(oConnection);
 	}
 	
 	/* destroys the pool by getting rid of all the connections. */
 	protected void destroyPool(){
 		try {
 			synchronized(_connectionList){
 				Iterator<Connection> it = _connectionList.iterator();
 				while(it.hasNext()){
 					Connection connection = it.next();
 					((OConnection)connection)._connection.close();
 				}
 			}
 			_connectionList.clear();
 		} catch (SQLException e) {
 			log.error(e.getMessage());
 		}
 	}
 	
 	/* populates the pool with new connections.  */
	protected void initializePool() {
 		for(int i = 0; i < ds._poolSize; i++){
 			Connection connection  = createConnection();
 			_connectionList.add(connection);
 		}
 	}
 	
 	/* returns the number of available connections in the pool. */
 	protected int getNumberOfAvailableConnections(){
 		return _connectionList.size();
 	}
 	
 	/* In case the garbage collector picks up this instance, this gives it a fighting chance of cleaning up after itself. */
 	@Override
 	public void finalize(){
 		destroyPool();
 	}
 
 }
