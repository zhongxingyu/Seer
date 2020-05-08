 package org.charry.lib.database_utility;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.charry.lib.database_utility.util.SleepManager;
 import org.charry.lib.database_utility.util.StackUtil;
 
 /**
  * Database utility, it's for commonly-used DML, for advanced feature, such as
  * transaction, please use getConnection() to get the database handler directly.
  * 
  * @version 0.1.1
  */
 public final class DatabaseFactory {
 	private static Log log = LogFactory.getLog(DatabaseFactory.class);
 	private static DatabaseFactory databaseInstance = null;
 	private Connection connection = null;
 	private String databaseAlias;
 	private static String defaultDatabaseAlias = "apple";
 	private static int waitTimeout = 28800; // in seconds, = 8 hours
 	private long lastActiveTime = 0;
 	private boolean bHasError = false; // for transaction only
 
 	// indicate if a transaction started
 	private boolean bTransactionStarted = false;
 
 	// some advanced and convenient operation
 	private String targetTable = "";
 	private ArrayList<String> fieldNameList = new ArrayList<String>();
 	private ArrayList<String> fieldValueList = new ArrayList<String>();
 	private String whereCondition = "1=2";
 
 	private String lastSQL = "";
 
 	private static Map<String, DatabaseFactory> databaseInstanceMap = new HashMap<String, DatabaseFactory>();
 
 	/**
 	 * By default, it'll return a database connection whose alias is 'apple',
 	 * with this setting, you must create a corresponding item(apple) in
 	 * configuration file. But you could specify the default database with:
 	 * 
 	 * <pre>
 	 * setDefaultDatabaseAlias(...);
 	 * </pre>
 	 * 
 	 * @return
 	 */
 	public static synchronized DatabaseFactory getInstance() {
 		return DatabaseFactory.getInstance(defaultDatabaseAlias);
 	}
 
 	public static synchronized DatabaseFactory getInstance(String alias) {
 		DatabaseConfig config = new DatabaseConfig(alias);
 
 		return DatabaseFactory.getInstance(config);
 	}
 
 	/**
 	 * Singleton, the public interface to retrieve the instance.
 	 * 
 	 * @param alias
 	 *            database alias
 	 * @return a shared database factory instance
 	 */
 	public static synchronized DatabaseFactory getInstance(DatabaseConfig config) {
 		Object obj = databaseInstanceMap.get(config.getAlias());
 		long lNow = new java.util.Date().getTime();
 
 		if (obj == null) {
 			databaseInstance = new DatabaseFactory(config);
 			log.info("create db connection:" + config.getAlias());
 			databaseInstanceMap.put(config.getAlias(), databaseInstance);
 		} else {
 			databaseInstance = (DatabaseFactory) obj;
 
 			try {
 				/**
 				 * If database connection is closed or idle time > X seconds,
 				 * recreate the connection
 				 */
 				if (databaseInstance.getConnection().isClosed()
 						|| (lNow - databaseInstance.lastActiveTime > waitTimeout * 1000)) {
 					databaseInstance.closeConnection();
 					databaseInstance = null;
 
 					databaseInstance = new DatabaseFactory(config);
 					log.info("db connection is closed or idle for a long time(>8hrs), recreate it:"
 							+ config.getAlias());
 					databaseInstanceMap
 							.put(config.getAlias(), databaseInstance);
 				}
 			} catch (Exception e) {
 				StackUtil.logStackTrace(log, e);
 			}
 		}
 
 		databaseInstance.lastActiveTime = lNow;
 
 		return databaseInstance;
 	}
 
 	/**
 	 * Save the database connection as a new alias, the corresponding database
 	 * connection won't be created until the client code calls it.
 	 * 
 	 * If the new alias exits, the connection will be overwritten/replaced.
 	 * 
 	 * @param alias
 	 *            new database connection alias
 	 */
 	public void saveAliasAs(String alias) {
 		// if alias already exists, do nothing.
 		if (this.databaseAlias.equals(alias)) {
 			log.info("alias exists, noop");
 			return;
 		}
 
 		DatabaseConfig config = DatabaseConfig.getConfig(databaseAlias);
 
 		// the object will be cached in constructor automatically
 		// if the new alias exits, it'll be overwritten/replaced.
 		new DatabaseConfig(alias, config.getUser(), config.getPassword(),
 				config.getConnectionString(), config.getDriver());
 	}
 
 	/**
 	 * Initialize the database factory.
 	 * 
 	 * @param alias
 	 *            database alias
 	 */
 	private DatabaseFactory(final DatabaseConfig config) {
 		createConnection(config);
 	}
 
 	/**
 	 * Initialize the database factory.
 	 * 
 	 * @param alias
 	 *            database alias
 	 */
 	private void createConnection(final DatabaseConfig config) {
 		String url = config.getConnectionString();
 		String user = config.getUser();
 		String pwd = config.getPassword();
 		String driverName = config.getDriver();
 
 		while (true) {
 			try {
 				Class.forName(driverName);
 				connection = DriverManager.getConnection(url, user, pwd);
 			} catch (SQLException e) {
 				log.error("SQLException:" + e);
 			} catch (ClassNotFoundException e) {
 				log.error("ClassNotFoundException:" + e);
 			} catch (Exception e) {
 				log.error("failed to connect to database:" + e + url + user
 						+ pwd);
 			}
 
 			if (connection == null) {
 				int sleepInterval = SleepManager.getNextSleepInterval() * 1000;
 				log.error("failed to connect to db, sleep " + sleepInterval
 						+ " secs, try again later" + url + user + pwd);
 				try {
 					Thread.sleep(sleepInterval);
 				} catch (Exception e) {
 					StackUtil.logStackTrace(log, e);
 				}
 			} else {
 				SleepManager.resetSleepTick();
 				break;
 			}
 		}
 
 		this.databaseAlias = config.getAlias();
 	}
 
 	/**
 	 * Query database and return an encapsulated result set.
 	 * 
 	 * <p />
 	 * The client code must call its close() method.For convenience, all
 	 * exceptions are caught, the client code doesn't need to handle it.
 	 * 
 	 * @param sql
 	 *            query string
 	 * @return result set
 	 */
 	public ResultSetEx executeQuery(final String sql) {
 		ResultSetEx rsEx = new ResultSetEx();
 
 		// if it's in transaction and there's an error, don't execute the SQL.
 		if (this.bTransactionStarted && this.bHasError)
 			return rsEx;
 
 		ResultSet rs = null;
 		Statement stmt = null;
 
 		try {
 			stmt = connection.createStatement(
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			rs = stmt.executeQuery(sql);
 
 			rsEx.setResultSet(rs);
 			rsEx.setStatement(stmt);
 		} catch (SQLException e) {
 			log.error("SQLException:" + e);
 			log.error("alias:" + this.databaseAlias + ":" + sql);
 			StackUtil.logStackTrace(log, e);
 			try {
 				this.bHasError = true;
 				if (this.bTransactionStarted)
 					this.connection.rollback();
 			} catch (SQLException ex) {
 				StackUtil.logStackTrace(log, ex);
 			}
 		} catch (Exception e) {
 			log.error("alias:" + this.databaseAlias + ":" + sql);
 			StackUtil.logStackTrace(log, e);
 		}
 
 		this.lastSQL = sql;
 
 		return rsEx;
 	}
 
 	/**
 	 * The client code must close the returned Statement and its corresponding
 	 * ResultSet, or else, it'll cause the resource leaks.
 	 * 
 	 * @param sql
 	 *            SQL string
 	 * @return prepared statement
 	 */
 	public PreparedStatement executePreparedQuery(final String sql) {
 		PreparedStatement stmt = null;
 
 		try {
 			stmt = connection.prepareStatement(sql);
 		} catch (SQLException e) {
 			log.error("SQLException:" + e);
 			log.error("alias:" + this.databaseAlias + ":" + sql);
 		} catch (Exception e) {
 			log.error("alias:" + this.databaseAlias + ":" + sql);
 			StackUtil.logStackTrace(log, e);
 		}
 
 		this.lastSQL = sql;
 
 		return stmt;
 	}
 
 	/**
 	 * Start a transaction, the database type must be InnoDB for MySQL.
 	 * 
 	 * It's <b>NOT</b> guaranteed that it's thread-safe.
 	 */
 	public void startTransaction() {
 		this.bTransactionStarted = true;
 
 		try {
 			this.connection.setAutoCommit(false);
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
 		}
 	}
 
 	public void endTransaction() {
 		this.bTransactionStarted = false;
 
 		// reset the error flag
 		this.bHasError = false;
 
 		try {
 			this.connection.setAutoCommit(true);
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
 		}
 	}
 
 	/**
 	 * Execute an SQL.
 	 * 
 	 * The client code must close the ResultSetEx explicitly. For convenience,
 	 * all exceptions are caught, the client code doesn't need to handle it.
 	 * 
 	 * @param sql
 	 *            SQL string
 	 * @return result set
 	 */
 	public ResultSetEx executeUpdate(final String sql) {
 		ResultSetEx rsEx = new ResultSetEx();
 
 		// if it's in transaction and there's an error, don't execute the SQL.
 		if (this.bTransactionStarted && this.bHasError)
 			return rsEx;
 
 		ResultSet rs = null;
 		Statement stmt = null;
 
 		try {
 			stmt = connection.createStatement(
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 
 			rsEx.setResultSet(rs);
 			rsEx.setStatement(stmt);
 
 		} catch (SQLException e) {
 			log.error("SQLException:" + e);
 			log.error("alias:" + this.databaseAlias + ":" + sql);
 			StackUtil.logStackTrace(log, e);
 
 			try {
 				this.bHasError = true;
 				if (this.bTransactionStarted)
 					this.connection.rollback();
 			} catch (SQLException ex) {
 				StackUtil.logStackTrace(log, ex);
 			}
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
 		}
 
 		this.lastSQL = sql;
 
 		return rsEx;
 	}
 
 	/**
 	 * If you want to enable/disable auto-commit explicitly, use the other one
 	 * instead.
 	 * 
 	 * Unless bAutoToggle=true, don't use this one.
 	 * 
 	 * @param sqlList
 	 * @param bAutoToggle
 	 */
 	public void executeUpdateBatch(final ArrayList<String> sqlList,
 			boolean bAutoToggle) {
 		boolean autoCommit = false;
 		try {
 			if (bAutoToggle) {
 				autoCommit = connection.getAutoCommit();
 				connection.setAutoCommit(false);
 			}
 
 			executeUpdateBatch(sqlList);
 
 			if (bAutoToggle)
 				connection.setAutoCommit(autoCommit);
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
 		}
 	}
 
 	/**
 	 * Execute SQL in batch, ATTENTION, you need to disable the auto-commit
 	 * explicitly.
 	 * 
 	 * @param sqlList
 	 *            list of the SQL string
 	 */
 	public void executeUpdateBatch(final ArrayList<String> sqlList) {
 		Statement stmt = null;
 		try {
 			stmt = connection.createStatement(
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 
 			for (int i = 0; i < sqlList.size(); i++) {
 				stmt.addBatch(sqlList.get(i));
 			}
 
 			stmt.executeBatch();
 			connection.commit();
 		} catch (SQLException e) {
 			log.error("SQLException:" + e);
 			log.error("alias:" + this.databaseAlias + ":" + sqlList);
 		} catch (Exception e) {
 			log.error("alias:" + this.databaseAlias + ":" + sqlList);
 			StackUtil.logStackTrace(log, e);
 		} finally {
 			try {
 				if (stmt != null)
 					stmt.close();
 			} catch (Exception e) {
 				StackUtil.logStackTrace(log, e);
 			}
 		}
 	}
 
 	/**
 	 * Close the database connection.
 	 */
 	public void closeConnection() {
 		try {
 			if (connection != null && connection.isClosed() == false)
 				connection.close();
 			connection = null;
 
 			databaseInstance = null;
 		} catch (SQLException e) {
 			log.error("SQLException:" + e);
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
 		}
 
 		databaseInstanceMap.remove(databaseAlias);
 		log.info("close connection, count of db instance:"
 				+ databaseInstanceMap.size());
 	}
 
 	public boolean notFoundRecord(final String sql) {
 		return !(foundRecord(sql));
 	}
 
 	/**
 	 * Return the database connection.
 	 * 
 	 * DO NOT use this interface to close the connection, use closeConnection()
 	 * instead
 	 * 
 	 * @return database connection
 	 */
 	public Connection getConnection() {
 		return this.connection;
 	}
 
 	public boolean foundRecord(final String sql) {
 		ResultSetEx rx = executeQuery(sql);
 		boolean bFound = false;
 		try {
 			bFound = rx.getResultSet().first();
 		} catch (SQLException e) {
			StackUtil.logStackTrace(log, e);
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
 		}
 
 		rx.close();
 
 		return bFound;
 	}
 
 	/**
 	 * A helper function to retrieve the field value, it only could get the
 	 * value of the first row.
 	 * 
 	 * @param sql
 	 * @param fieldName
 	 * @return Integer.MIN_VALUE if no record found
 	 */
 	public int getIntValue(final String sql, final String fieldName) {
 		ResultSetEx rx = executeQuery(sql);
 		int value = Integer.MIN_VALUE;
 		try {
 			if (rx.getResultSet().next()) {
 				value = rx.getResultSet().getInt(fieldName);
 			}
 		} catch (SQLException e) {
			StackUtil.logStackTrace(log, e);
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
		} finally {
			rx.close();
 		}
 
 		return value;
 	}
 
 	/**
 	 * A helper function to retrieve the field value, it only could get the
 	 * value of the first row.
 	 * 
 	 * @param sql
 	 * @param fieldName
 	 * @return null if no record found
 	 */
 	public String getStringValue(final String sql, final String fieldName) {
 		ResultSetEx rx = executeQuery(sql);
 		String value = null;
 		try {
 			if (rx.getResultSet().next()) {
 				value = rx.getResultSet().getString(fieldName);
 			}
 		} catch (SQLException e) {
			StackUtil.logStackTrace(log, e);
 		} catch (Exception e) {
 			StackUtil.logStackTrace(log, e);
		} finally {
			rx.close();
 		}
 
 		return value;
 	}
 
 	/**
 	 * 
 	 * Return true if there's an error in the latest transaction, this function
 	 * is only for transaction.
 	 */
 	public boolean hasError() {
 		return this.bHasError;
 	}
 
 	/**
 	 * Delete record from database.
 	 * 
 	 * @param targetTable
 	 *            target table
 	 * @param condition
 	 *            where condition
 	 */
 	public void delete(String targetTable, String condition) {
 		String sql = String.format("DELETE FROM %s WHERE %s", targetTable,
 				condition);
 
 		ResultSetEx rx = executeUpdate(sql);
 		rx.close();
 	}
 
 	/**
 	 * Set the target table for future operation.
 	 * 
 	 * @param targetTable
 	 *            target table
 	 * @return the DatabaseFactory instance, this could be used as method
 	 *         chaining.
 	 */
 	public DatabaseFactory setTargetTable(String targetTable) {
 		this.targetTable = targetTable;
 
 		return this;
 	}
 
 	/**
 	 * Set value of one field.
 	 * 
 	 * @param filedName
 	 *            field name
 	 * @param value
 	 *            field value
 	 * @return the DatabaseFactory instance, this could be used as method
 	 *         chaining.
 	 */
 	public DatabaseFactory setFieldValue(String filedName, Object value) {
 		this.fieldNameList.add(filedName);
 		this.fieldValueList.add("" + value);
 
 		return this;
 	}
 
 	/**
 	 * Set string value of one field
 	 * 
 	 * @param filedName
 	 *            field name
 	 * @param value
 	 *            field value
 	 * @return the DatabaseFactory instance, this could be used as method
 	 *         chaining.
 	 */
 	public DatabaseFactory setFieldStringValue(String filedName, Object value) {
 		this.fieldNameList.add(filedName);
 		this.fieldValueList.add("'" + value + "'");
 
 		return this;
 	}
 
 	/**
 	 * Reset the cached field names and field values, etc.
 	 * 
 	 * @return the DatabaseFactory instance, this could be used as method
 	 *         chaining.
 	 */
 	public DatabaseFactory resetSQLCache() {
 		this.fieldNameList.clear();
 		this.fieldValueList.clear();
 
 		this.whereCondition = "1=2";
 
 		return this;
 	}
 
 	/**
 	 * Set the where condition.
 	 * 
 	 * @param where
 	 *            where condition
 	 * @return the DatabaseFactory instance, this could be used as method
 	 *         chaining.
 	 */
 	public DatabaseFactory setWhere(String where) {
 		this.whereCondition = where;
 
 		return this;
 	}
 
 	/**
 	 * Insert a record into table.
 	 * 
 	 * @return A wrapper of ResultSet
 	 */
 	public ResultSetEx insertRecord() {
 		String sql = "INSERT INTO " + targetTable + "(";
 
 		for (int i = 0; i < fieldNameList.size(); ++i) {
 			if (i < fieldNameList.size() - 1)
 				sql += fieldNameList.get(i) + ", ";
 			else
 				sql += fieldNameList.get(i);
 		}
 		sql += ") VALUES(";
 
 		for (int i = 0; i < fieldValueList.size(); ++i) {
 			if (i < fieldValueList.size() - 1)
 				sql += fieldValueList.get(i) + ", ";
 			else
 				sql += fieldValueList.get(i);
 		}
 		sql += ")";
 
 		return executeUpdate(sql);
 	}
 
 	/**
 	 * Save a object
 	 * 
 	 * @param clazz
 	 *            class of object
 	 * @param object
 	 *            object
 	 * @return ResultSetEx
 	 */
 	public ResultSetEx saveObject(Object object) {
 		Orm orm = new Orm(object);
 		String tableName = "";
 		if (this.targetTable.equals("") == false)
 			tableName = this.targetTable;
 		else
 			tableName = orm.getTableName();
 
 		String sql = "INSERT INTO " + tableName + orm.getSQL();
 
 		return this.executeUpdate(sql);
 	}
 
 	/**
 	 * Insert if record doesn't exist, else update
 	 * 
 	 * @param keys
 	 *            to determine if record(s) exist
 	 * @return
 	 */
 	public ResultSetEx insertOrUpdate(String... keys) {
 		String condition = "";
 		for (String key : keys) {
 			for (int i = 0; i < fieldNameList.size(); ++i) {
 				String fname = fieldNameList.get(i);
 
 				if (key.equals(fname)) {
 					condition += key + "=" + fieldValueList.get(i);
 					condition += " AND ";
 				}
 			}
 		}
 
 		if (condition.contains("AND"))
 			condition = condition.substring(0, condition.length() - 4);
 
 		String sql = String.format("SELECT COUNT(*) AS R FROM %s WHERE %s",
 				this.targetTable, condition);
 
 		if (getIntValue(sql, "R") == 0) {
 			return this.insertRecord();
 		} else {
 			this.whereCondition = condition;
 			return this.updateRecord();
 		}
 	}
 
 	/**
 	 * Update record in table.
 	 * 
 	 * @return A wrapper of ResultSet
 	 */
 	public ResultSetEx updateRecord() {
 		String sql = "UPDATE " + targetTable + " SET ";
 
 		for (int i = 0; i < fieldNameList.size(); ++i) {
 			sql += fieldNameList.get(i) + "=" + fieldValueList.get(i);
 
 			if (i < fieldNameList.size() - 1)
 				sql += ", ";
 		}
 		sql += " WHERE " + this.whereCondition;
 
 		return executeUpdate(sql);
 	}
 
 	public String getLastSQL() {
 		return lastSQL;
 	}
 
 	/**
 	 * A wrapper for MySQL ResultSet.
 	 * 
 	 * @author wcharry
 	 * 
 	 */
 	public final class ResultSetEx {
 		private ResultSet resultSet = null;
 		private Statement statement = null;
 		private long lastId = -1;
 
 		/**
 		 * Only works with MySQL.
 		 * 
 		 * only get one last Id, for more, use statement instead
 		 * 
 		 * @return
 		 */
 		public long getLastId() {
 			// this only works for one-insert-statement, if there's multiple
 			// inserts, use stmt.getGeneratedKeys instead.
 			ResultSet rsKeys;
 			try {
 				rsKeys = statement.getGeneratedKeys();
 
 				if (rsKeys.next()) {
 					lastId = rsKeys.getLong(1);
 				}
 
 				if (rsKeys != null)
 					rsKeys.close();
 			} catch (SQLException e) {
 				StackUtil.logStackTrace(log, e);
 			}
 
 			return this.lastId;
 		}
 
 		public void setLastId(long id) {
 			this.lastId = id;
 		}
 
 		public ResultSet getResultSet() {
 			return resultSet;
 		}
 
 		public void setResultSet(ResultSet resultSet) {
 			this.resultSet = resultSet;
 		}
 
 		public Statement getStatement() {
 			return statement;
 		}
 
 		public void setStatement(Statement statement) {
 			this.statement = statement;
 		}
 
 		public List toList(Class clazz) {
 			Orm orm = new Orm();
 
 			List list = orm.dumpResultSet(resultSet, clazz);
 
 			this.close();
 
 			return list;
 		}
 
 		/**
 		 * Close the resources, this function must be called by the client code.
 		 */
 		public void close() {
 			try {
 				if (resultSet != null)
 					resultSet.close();
 
 				if (statement != null)
 					statement.close();
 			} catch (Exception e) {
				StackUtil.logStackTrace(log, e);
 			}
 		}
 	}
 
 	public final static class FactoryFacade {
 		public static void setConfigXML(String config) {
 			DatabaseConfig.setConfigXML(config);
 		}
 
 		/**
 		 * Set timeout, when the connection is idle for a long time,
 		 * reconnection is required. the timeout value is the minimum one of all
 		 * database which are configured in config_database.xml
 		 * 
 		 * @param timeout
 		 *            max timeout
 		 */
 		public static void setWaitTimeout(int timeout) {
 			DatabaseFactory.waitTimeout = timeout;
 		}
 
 		/**
 		 * Set the default database alias, the default value is: apple
 		 * 
 		 * @param alias
 		 *            database alias name
 		 */
 		public static void setDefaultDatabaseAlias(String alias) {
 			DatabaseFactory.defaultDatabaseAlias = alias;
 		}
 
 		/**
 		 * Sleep n seconds to retry if failed to connect to database.
 		 * 
 		 * @param interval
 		 *            sleep interval, unit: second, 0 means increased interval
 		 */
 		public static void setRetryInterval(int interval) {
 			SleepManager.setSleepInterval(interval);
 		}
 	}
 }
