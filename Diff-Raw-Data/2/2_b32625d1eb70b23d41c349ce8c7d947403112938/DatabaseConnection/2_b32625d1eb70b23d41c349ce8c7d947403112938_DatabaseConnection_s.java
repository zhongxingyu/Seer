 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.db;
 
 import java.lang.reflect.Method;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Properties;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDriver;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.jamwiki.Environment;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.Encryption;
 import org.springframework.util.StringUtils;
 
 /**
  * This class provides methods for retrieving database connections, executing queries,
  * and setting up connection pools.
  */
 public class DatabaseConnection {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseConnection.class.getName());
 	/** Any queries that take longer than this value (specified in milliseconds) will print a warning to the log. */
 	protected static final int SLOW_QUERY_LIMIT = 250;
 	private static boolean poolInitialized = false;
 	private static GenericObjectPool connectionPool = null;
 
 	/**
 	 * Utility method for closing a database connection, a statement and a result set.
 	 * This method must ALWAYS be called for any connection retrieved by the
 	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
 	 * connection SHOULD NOT have already been closed.
 	 *
 	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
 	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
 	 * @param stmt A statement object that is to be closed.  May be <code>null</code>.
 	 * @param rs A result set object that is to be closed.  May be <code>null</code>.
 	 */
 	public static void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (Exception e) {}
 		}
 		DatabaseConnection.closeConnection(conn, stmt);
 	}
 
 	/**
 	 * Utility method for closing a database connection and a statement.  This method
 	 * must ALWAYS be called for any connection retrieved by the
 	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
 	 * connection SHOULD NOT have already been closed.
 	 *
 	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
 	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
 	 * @param stmt A statement object that is to be closed.  May be <code>null</code>.
 	 */
 	public static void closeConnection(Connection conn, Statement stmt) {
 		if (stmt != null) {
 			try {
 				stmt.close();
 			} catch (Exception e) {}
 		}
 		DatabaseConnection.closeConnection(conn);
 	}
 
 	/**
 	 * Utility method for closing a database connection.  This method must ALWAYS be
 	 * called for any connection retrieved by the
 	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
 	 * connection SHOULD NOT have already been closed.
 	 *
 	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
 	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
 	 */
 	public static void closeConnection(Connection conn) {
 		if (conn == null) {
 			return;
 		}
 		try {
 			conn.close();
 		} catch (Exception e) {
 			logger.severe("Failure while closing connection", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static void closeConnectionPool() throws Exception {
 		if (connectionPool == null) {
 			return;
 		}
 		connectionPool.clear();
 		try {
 			connectionPool.close();
 		} catch (Exception e) {
 			logger.severe("Unable to close connection pool", e);
 			throw e;
 		}
 		poolInitialized = false;
 	}
 
 	/**
 	 *
 	 */
 	protected static WikiResultSet executeQuery(String sql) throws Exception {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			return executeQuery(sql, conn);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static WikiResultSet executeQuery(String sql, Connection conn) throws Exception {
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			long start = System.currentTimeMillis();
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(sql);
 			long execution = System.currentTimeMillis() - start;
 			if (execution > DatabaseConnection.SLOW_QUERY_LIMIT) {
 				logger.warning("Slow query: " + sql + " (" + (execution / 1000.000) + " s.)");
 			}
 			logger.fine("Executed " + sql + " (" + (execution / 1000.000) + " s.)");
 			return new WikiResultSet(rs);
 		} catch (Exception e) {
 			throw new Exception("Failure while executing " + sql, e);
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (Exception e) {}
 			}
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (Exception e) {}
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static void executeUpdate(String sql) throws Exception {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			executeUpdate(sql, conn);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static int executeUpdate(String sql, Connection conn) throws Exception {
 		Statement stmt = null;
 		try {
 			long start = System.currentTimeMillis();
 			stmt = conn.createStatement();
 			logger.info("Executing SQL: " + sql);
 			int result = stmt.executeUpdate(sql);
 			long execution = System.currentTimeMillis() - start;
 			if (execution > DatabaseConnection.SLOW_QUERY_LIMIT) {
 				logger.warning("Slow query: " + sql + " (" + (execution / 1000.000) + " s.)");
 			}
 			logger.fine("Executed " + sql + " (" + (execution / 1000.000) + " s.)");
 			return result;
 		} catch (Exception e) {
 			throw new Exception("Failure while executing " + sql, e);
 		} finally {
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (Exception e) {}
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static Connection getConnection() throws Exception {
 		String url = Environment.getValue(Environment.PROP_DB_URL);
 		String userName = Environment.getValue(Environment.PROP_DB_USERNAME);
 		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, null);
 		Connection conn = null;
 		if (url.startsWith("jdbc:")) {
 			if (!poolInitialized) {
 				setUpConnectionPool(url, userName, password);
 			}
 			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:jamwiki");
 		} else {
 			// Use Reflection here to avoid a compile time dependency
 			// on the DataSource interface. It's not available by default
 			// on j2se 1.3.
 			Context ctx = new InitialContext();
 			Object dataSource = ctx.lookup(url);
 			Method m;
 			Object args[];
 			if (userName.length() == 0) {
 				Class[] parameterTypes = null;
 				m = dataSource.getClass().getMethod("getConnection", parameterTypes);
 				args = new Object[]{};
 			} else {
 				m = dataSource.getClass().getMethod("getConnection", new Class[]{String.class, String.class});
 				args = new Object[]{userName, password};
 			}
 			conn = (Connection)m.invoke(dataSource, args);
 		}
 		conn.setAutoCommit(true);
 		return conn;
 	}
 
 	/**
 	 *
 	 */
 	protected static void handleErrors(Connection conn) {
 		if (conn == null) return;
 		try {
 			logger.warning("Rolling back database transactions");
 			conn.rollback();
 		} catch (Exception e) {
 			logger.severe("Unable to rollback connection", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static void setPoolInitialized(boolean poolInitialized) {
 		DatabaseConnection.poolInitialized = poolInitialized;
 	}
 
 	/**
 	 * Set up the database connection.
 	 *
 	 * @param url The database connection url.
 	 * @param userName The user name to use when connecting to the database.
 	 * @param password The password to use when connecting to the database.
 	 * @throws Exception Thrown if any error occurs while initializing the connection pool.
 	 */
 	private static void setUpConnectionPool(String url, String userName, String password) throws Exception {
 		closeConnectionPool();
 		if (StringUtils.hasText(Environment.getValue(Environment.PROP_DB_DRIVER))) {
 			Class.forName(Environment.getValue(Environment.PROP_DB_DRIVER), true, Thread.currentThread().getContextClassLoader());
 		}
 		connectionPool = new GenericObjectPool();
 		connectionPool.setMaxActive(Environment.getIntValue(Environment.PROP_DBCP_MAX_ACTIVE));
 		connectionPool.setMaxIdle(Environment.getIntValue(Environment.PROP_DBCP_MAX_IDLE));
 		connectionPool.setMinEvictableIdleTimeMillis(Environment.getIntValue(Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME) * 1000);
 		connectionPool.setTestOnBorrow(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_BORROW));
 		connectionPool.setTestOnReturn(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_RETURN));
 		connectionPool.setTestWhileIdle(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_WHILE_IDLE));
 		connectionPool.setTimeBetweenEvictionRunsMillis(Environment.getIntValue(Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS) * 1000);
 		connectionPool.setNumTestsPerEvictionRun(Environment.getIntValue(Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN));
 		connectionPool.setWhenExhaustedAction((byte) Environment.getIntValue(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION));
 		Properties properties = new Properties();
 		properties.setProperty("user", userName);
 		properties.setProperty("password", password);
 		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ORACLE)) {
 			// handle clobs as strings, Oracle 10g and higher drivers (ojdbc14.jar)
 			properties.setProperty("SetBigStringTryClob", "true");
 		}
 		DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, properties);
 		new PoolableConnectionFactory(connectionFactory, connectionPool, null, DatabaseHandler.getConnectionValidationQuery(), false, true);
 		PoolingDriver driver = new PoolingDriver();
 		driver.registerPool("jamwiki", connectionPool);
 		Connection conn = null;
 		try {
 			// try to get a test connection
 			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:jamwiki");
 		} finally {
 			if (conn != null) closeConnection(conn);
 		}
 		poolInitialized = true;
 	}
 
 	/**
 	 *
 	 */
 	public static boolean testDatabase(String driver, String url, String user, String password, boolean existence) {
 		Connection conn = null;
 		try {
 			if (StringUtils.hasText(driver)) {
 				Class.forName(driver, true, Thread.currentThread().getContextClassLoader());
 			}
 			conn = DriverManager.getConnection(url, user, password);
 			if (existence) {
 				// test to see if database exists
				executeQuery(DatabaseHandler.getConnectionValidationQuery(), conn);
 			}
 		} catch (Exception e) {
 			// database settings incorrect
 			logger.severe("Invalid database settings", e);
 			return false;
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (Exception e) {}
 			}
 		}
 		return true;
 	}
 }
