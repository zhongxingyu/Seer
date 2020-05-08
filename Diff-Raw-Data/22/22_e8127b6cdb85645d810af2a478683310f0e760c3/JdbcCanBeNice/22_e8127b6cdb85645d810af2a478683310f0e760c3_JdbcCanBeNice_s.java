 package org.nothing;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.sql.DataSource;
 
 /**
  * This class mainlty consists of static factory methods that make working with
  * JDBC less painful and more pleasant. Here an example of it's usage :
  * 
  * <pre>
  * <code>ConnectionProvider connectionProvider = cachingConnectionProvider(driverManagerConnectionProvider(
  * 		"org.apache.derby.jdbc.Driver40",
  * 		"jdbc:derby:crud;create=true", "", ""));
  * Number key = doWithConnection(
  * 		sqlTx(
  * 				sqlUpdate("delete * from table").then(
  * 						sqlUpdate("insert into table values(?, ?)",
  * 								true, "a"))).thenReturn(
  * 				dontThrowSqlException(sqlUpdateAndReturnKey(
  * 						"insert into table2 values(?)", 8), -1)),
  * 		connectionProvider);
  * RowMapper<String> namesMapper = new RowMapper<String>() {
  * 
  * 	public String mapRow(ResultSet resultSet, int row)
  * 			throws SQLException {
  * 		return resultSet.getString("name");
  * 	}
  * };
  * 
  * List<String> names = doWithConnection(sqlQuery(
  * 		"select name from table where age < ?", namesMapper, 20),
  * 		connectionProvider);</code>
  * </pre>
  * 
  * 
  * 
  * @author Jawher
  * 
  */
 public class JdbcCanBeNice {
 	/**
 	 * A driver manager based data provider. The connection is recreated upon
 	 * every invocation of the {@link ConnectionProvider#get()} method
 	 * 
 	 * @param driverClassName
 	 * @param url
 	 * @param user
 	 * @param password
 	 * @return A connection provider based on a driver manager.
 	 */
 	public static ConnectionProvider driverManagerConnectionProvider(
 			final String driverClassName, final String url, final String user,
 			final String password) {
 		return new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				try {
 					Class.forName(driverClassName);
 				} catch (ClassNotFoundException e) {
 					throw new RuntimeException(e);
 				}
 				return DriverManager.getConnection(url, user, password);
 			}
 		};
 	}
 
 	/**
 	 * A datasource based connection provider. The connection is recreated upon
 	 * every invocation of the {@link ConnectionProvider#get()} method
 	 * 
 	 * @param dataSource
 	 * @return A connection provider based on a datasource.
 	 */
 	public static ConnectionProvider datasourceConnectionProvider(
 			final DataSource dataSource) {
 		return new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return dataSource.getConnection();
 			}
 		};
 	}
 
 	/**
 	 * Encapsulates a connection provider and caches the underlying connection
 	 * so that it is retrieved lazily and only once.
 	 * 
 	 * @param connectionProvider
 	 *            the connection provider to encapsulate.
 	 * @return the caching data provider.
 	 */
 	public static ConnectionProvider cachingConnectionProvider(
 			final ConnectionProvider connectionProvider) {
 		return new ConnectionProvider() {
 			private Connection connection;
 			private Lock lock = new ReentrantLock();
 
 			public Connection get() throws SQLException {
 				lock.lock();
 				try {
 					if (connection == null) {
 						connection = connectionProvider.get();
 					}
 				} finally {
 					lock.unlock();
 				}
 				return connection;
 			}
 		};
 	}
 
 	/**
 	 * This is the main entry point of this library. Executes a
 	 * {@link JdbcAction} with the connection provided by a
 	 * {@link ConnectionProvider}.
 	 * 
 	 * @param <T>
 	 *            The return type of the JDBC Action.
 	 * @param action
 	 * @param connectionProvider
 	 * @return The result of the JDBC Action.
 	 */
 	public static <T> T doWithConnection(JdbcAction<T> action,
 			ConnectionProvider connectionProvider) {
 		try {
 			return action.doWithConnection(connectionProvider.get());
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Wraps a {@link JdbcAction} into a {@link ChainableJdbcAction} so that it
 	 * can be chained with other actions
 	 * 
 	 * @param <T>
 	 *            the return type of the action
 	 * @param action
 	 * @return
 	 */
 	public static <T> ChainableJdbcAction<T> sqlMakeChainable(
 			final JdbcAction<T> action) {
 		return new BaseChainableJdbcAction<T>() {
 
 			public T doWithConnection(Connection connection)
 					throws SQLException {
 				return action.doWithConnection(connection);
 			}
 
 			@Override
 			public String toString() {
 				return action.toString();
 			}
 		};
 	}
 
 	/**
 	 * Wraps a {@link JdbcAction} into a transaction. If you need to wrap
 	 * multiple actions into a single transaction, consider using actions
 	 * chaining (see {@link ChainableJdbcAction} and
 	 * {@link #sqlMakeChainable(JdbcAction)}) to group them into a single action
 	 * and make the latter transactional.
 	 * 
 	 * @param <T>
 	 *            the return type of the action
 	 * @param action
 	 * @return The result of the wrapped JDBC Action.
 	 */
 	public static <T> ChainableJdbcAction<T> sqlTx(final JdbcAction<T> action) {
 		return new BaseChainableJdbcAction<T>() {
 
 			public T doWithConnection(Connection connection)
 					throws SQLException {
 				boolean originalAutoCommit = connection.getAutoCommit();
 				connection.setAutoCommit(false);
 
 				try {
 					T res = action.doWithConnection(connection);
 					connection.commit();
 					return res;
 				} catch (RuntimeException e) {
 					try {
 						connection.rollback();
 					} catch (SQLException e1) {
 						throw new RuntimeException(e);
 					}
 					throw e;
 				} finally {
 					connection.setAutoCommit(originalAutoCommit);
 				}
 			}
 
 			@Override
 			public String toString() {
 				return "tx {" + action + "}";
 			}
 		};
 
 	}
 
 	/**
 	 * A factory method that creates a jdbc update action (update, delete,
 	 * insert, etc.). Here's how the resulting {@link JdbcAction} works :
 	 * <ul>
 	 * <li>Creates a {@link PreparedStatement}</li>
 	 * <li>Iterates on the parameters calling
 	 * {@link PreparedStatement#setObject(int, Object)} on each of them</li>
 	 * <li>calls {@link PreparedStatement#executeUpdate()}</li>
 	 * </ul>
 	 * 
 	 * 
 	 * @param sql
 	 *            the sql query, which can use the ? placeholders as with
 	 *            regular JDBC prepared statements
 	 * @param params
 	 *            the list of the query params, as with regular JDBC prepared
 	 *            statements
 	 * @return the number of affected rows
 	 */
 	public static ChainableJdbcAction<Integer> sqlUpdate(final String sql,
 			final Object... params) {
 		return new BaseChainableJdbcAction<Integer>() {
 
 			public Integer doWithConnection(Connection connection)
 					throws SQLException {
 				PreparedStatement ps = connection.prepareStatement(sql);
 				for (int i = 0; i < params.length; i++) {
 					ps.setObject(i + 1, params[i]);
 				}
 				try {
 					return ps.executeUpdate();
				} catch (SQLException e) {
 					try {
 						ps.close();
 					} catch (SQLException e1) {
 
 					}
					throw e;
 				}
 			}
 
 			@Override
 			public String toString() {
 				return sql;
 			}
 		};
 	}
 
 	/**
 	 * A factory method that creates a jdbc update action (update, delete,
 	 * insert, etc.) that returns the generated key. Here's how the resulting
 	 * {@link JdbcAction} works :
 	 * <ul>
 	 * <li>Creates a {@link PreparedStatement} initialized with the parameter
 	 * {@link Statement#RETURN_GENERATED_KEYS}</li>
 	 * <li>Iterates on the parameters calling
 	 * {@link PreparedStatement#setObject(int, Object)} on each of them</li>
 	 * <li>calls {@link PreparedStatement#executeUpdate()}</li>
 	 * <li>And finally uses the result set returned by
 	 * {@link PreparedStatement#getGeneratedKeys()} to retrieve the generated
 	 * key</li>
 	 * </ul>
 	 * 
 	 * If you need to trieve the query's generated key, please consider using
 	 * {@link #sqlUpdateAndReturnKey(String, Object...)}.
 	 * 
 	 * @param sql
 	 *            the sql query, which can use the ? placeholders as with
 	 *            regular JDBC prepared statements
 	 * @param params
 	 *            the list of the query params, as with regular JDBC prepared
 	 *            statements
 	 * @return The generated key
 	 */
 	public static ChainableJdbcAction<Number> sqlUpdateAndReturnKey(
 			final String sql, final Object... params) {
 		return new BaseChainableJdbcAction<Number>() {
 
 			public Number doWithConnection(Connection connection)
 					throws SQLException {
 				PreparedStatement ps = connection.prepareStatement(sql,
 						Statement.RETURN_GENERATED_KEYS);
 				for (int i = 0; i < params.length; i++) {
 					ps.setObject(i + 1, params[i]);
 				}
 				try {
 					ps.executeUpdate();
					ResultSet generatedKeys = ps.getGeneratedKeys();
 					generatedKeys.next();
 					return (Number) generatedKeys.getObject(1);
				} catch (SQLException e) {
 					try {
 						ps.close();
 					} catch (SQLException e1) {

 					}
					throw e;
 				}
 			}
 
 			@Override
 			public String toString() {
 				return sql + " -> key";
 			}
 		};
 	}
 
 	/**
 	 * A factory method that creates a jdbc select action. Here's how the
 	 * resulting {@link JdbcAction} works :
 	 * <ul>
 	 * <li>Creates a {@link PreparedStatement}</li>
 	 * <li>Iterates on the parameters calling
 	 * {@link PreparedStatement#setObject(int, Object)} on each of them</li>
 	 * <li>calls {@link PreparedStatement#executeQuery()}</li>
 	 * <li>Iterates over the returned result set, calling the
 	 * {@link RowMapper#mapRow(ResultSet, int) method on each iteration and
 	 * accumulating it's result into a list}</li>
 	 * </ul>
 	 * 
 	 * 
 	 * @param sql
 	 *            the sql query, which can use the ? placeholders as with
 	 *            regular JDBC prepared statements
 	 * @param params
 	 *            the list of the query params, as with regular JDBC prepared
 	 *            statements
 	 * @return A list of entities corresponding to the rows returned by the
 	 *         select query
 	 */
 	public static <T> ChainableJdbcAction<List<T>> sqlQuery(final String sql,
 			final RowMapper<T> rowMapper, final Object... params) {
 		return new BaseChainableJdbcAction<List<T>>() {
 
 			public List<T> doWithConnection(Connection connection)
 					throws SQLException {
 				PreparedStatement ps = connection.prepareStatement(sql);
 				for (int i = 0; i < params.length; i++) {
 					ps.setObject(i + 1, params[i]);
 				}
 				ResultSet rs = null;
 				try {
 					rs = ps.executeQuery();
 					List<T> res = new ArrayList<T>();
 					int row = 0;
 					while (rs.next()) {
 						res.add(rowMapper.mapRow(rs, row++));
 					}
 					return res;
 				} finally {
 					try {
 						rs.close();
 					} catch (SQLException e1) {
 
 					}
 					try {
 						ps.close();
 					} catch (SQLException e1) {
 
 					}
 				}
 			}
 
 			@Override
 			public String toString() {
 				return sql;
 			}
 		};
 	}
 
 	/**
 	 * Wraps a {@link JdbcAction} into an action that catches any thrown
 	 * {@link SQLException} and returns a user supplied value instead of
 	 * failing.
 	 * 
 	 * @param <T>
 	 *            the return type of the action
 	 * @param action
 	 * @param resInCasOfException
 	 *            the result to return if an SQLException was thrown
 	 * @return the wrapped action's result if no {@link SQLException} was
 	 *         thrown, resInCasOfException otherwise.
 	 */
 	public static <T> ChainableJdbcAction<T> dontThrowSqlException(
 			final JdbcAction<T> action, final T resInCasOfException) {
 		return new BaseChainableJdbcAction<T>() {
 
 			public T doWithConnection(Connection connection)
 					throws SQLException {
 				try {
 					return action.doWithConnection(connection);
 				} catch (SQLException e) {
 					return null;
 				}
 			}
 
 			@Override
 			public String toString() {
 				return "noSqlException {" + action + "}";
 			}
 		};
 	}
 
 }
