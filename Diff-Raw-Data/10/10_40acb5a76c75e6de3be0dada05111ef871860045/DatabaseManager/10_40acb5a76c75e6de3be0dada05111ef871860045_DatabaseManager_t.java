 package escada.tpc.common.database;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Date;
 
 import escada.tpc.common.PerformanceCounters;
 import escada.tpc.logger.PerformanceLogger;
 
 /**
  * It implements a generic database interface with connection control
  * (connection pool).
  */
 public class DatabaseManager {
 
 	private ConnectionManager cn = new ConnectionManager();
 
 	private boolean connectionpool = true;
 
 	private boolean virtualdatabase = false;
 
 	private Date baseTime = new java.util.Date();
 
 	/**
 	 * It instanciates the CommonDatabase class.
 	 */
 	public DatabaseManager() {
 	}
 
 	/**
 	 * It defines the bahaivor of the database. It it is defined as virtual, in
 	 * fact nothing is done to acquire and release connections.
 	 * 
 	 * @param boolean
 	 *            (true) the database is virtual, (false) it is a normal
 	 *            database
 	 * 
 	 * TODO: SUMIR COM ESSA IDEIA DE BASE VIRTUAL
 	 */
 	public void setVirtualDatabase(boolean v) {
 		virtualdatabase = v;
 	}
 
 	/**
 	 * It defines the maximum number of available connections. This information
 	 * must be configured according to the sets of the database, therefore
 	 * avoiding to exceed the maximum allowed number of connections of the
 	 * database.
 	 * 
 	 * @param int
 	 *            the maximum number of connections
 	 */
 	public void setMaxConnection(int mConn) {
 		if (!virtualdatabase)
 			cn.setMaxConnection(mConn);
 	}
 
 	/**
 	 * It is used to enable or disable the connection pool.
 	 * 
 	 * @param boolean
 	 *            (true) enables the connection pool or (false) disables the
 	 *            connection pool
 	 */
 	public void setConnectionPool(boolean pool) {
 		if (!virtualdatabase)
 			connectionpool = pool;
 	}
 
 	/**
 	 * It sets the driver used to connect to the database. It is important to
 	 * notice that this information is dependent of the database used. It is
 	 * also important to remember to set the classpath in order to locate the
 	 * correct driver.
 	 * 
 	 * @param String
 	 *            the driver
 	 */
 	public void setDriverName(String dName) {
 		if (!virtualdatabase)
 			cn.setDriverName(dName);
 	}
 
 	/**
 	 * It sets the jdbc path used to connect to the database. It is important to
 	 * notice that this information is dependent of the database used. For that
 	 * reason, it must be set carefully.
 	 * 
 	 * @param String
 	 *            the jdbc path
 	 */
 	public void setjdbcPath(String jdbc) {
 		if (!virtualdatabase)
 			cn.setjdbcPath(jdbc);
 	}
 
 	/**
 	 * It sets the information about the user indentification and its password
 	 * in order to connect to the database.
 	 * 
 	 * @param user
 	 *            the user identification
 	 * @param passwd
 	 *            the password used to connect
 	 */
 	public void setUserInfo(String usr, String pass) {
 		if (!virtualdatabase)
 			cn.setUserInfo(usr, pass);
 	}
 
 	/**
 	 * It retrieves a new connection in order to access the database. It is
 	 * important to notice that this new connection is usually obtained from a
 	 * pool. However, if there is not an idle connection available, it tries to
 	 * create a new one whenever the number of open connections does not exceed
 	 * the maximun configured value.
 	 */
 	public Connection getConnection() throws SQLException {
 		if (!virtualdatabase)
 			if (!connectionpool)
 				return cn.createConnection();
 			else {
 				return cn.getConnection();
 			}
 		return (null);
 	}
 
 	public void releaseConnections() throws SQLException {
 		cn.releaseConnections();
 	}
 
 	/**
 	 * It returns the connection to the pool in order to improve performance,
 	 * instead of closing the connection.
 	 * 
 	 * @param ConnectionInterface
 	 *            the connection be released and stored into the pool
 	 */
 	public void returnConnection(Connection con) {
 		if (!virtualdatabase)
 			if (!connectionpool)
 				cn.closeConnection(con);
 			else
 				cn.returnConnection(con);
 	}
 
 	public void processLog(Date startTime, Date finishTime, String transResult,
 			String transAccess, String transName) {
 
 		if (PerformanceLogger.isPerformanceLoggerEnabled()) {
 
 			if (transResult.equalsIgnoreCase("beginning")) {
 
 				PerformanceCounters.setIncommingRate();
 
 			} else if (transResult.equalsIgnoreCase("committing")) {
 
 				PerformanceCounters.setCommitRate();
				
				// register latency time
				PerformanceCounters.setLatency(finishTime.getTime() - startTime.getTime());
 
 			} else if (transResult.equalsIgnoreCase("aborting")) {
 
 				PerformanceCounters.setAbortRate();
 
 			}
 
 			PerformanceLogger.info((startTime.getTime() - baseTime.getTime())
 					+ ":0:" + (finishTime.getTime() - baseTime.getTime()) + ":"
 					+ (finishTime.getTime() - startTime.getTime()) + ":"
 					+ transResult + ":" + transAccess + ":" + transName);
 		}
 	}
 }
 // arch-tag: 87008d61-42d7-4640-8d35-51c4e47def08
