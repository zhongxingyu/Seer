 package nl.pojoquery;
 
 import static nl.pojoquery.util.Strings.implode;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 public class DB {
 	public static final class DatabaseException extends RuntimeException {
 
 		public DatabaseException(SQLException e) {
 			super(e);
 		}
 
 	}
 
 	public interface Transaction<T> {
 		public T run(Connection connection);
 	}
 
 	private enum QueryType {
 		DDL, SELECT, UPDATE, INSERT
 	}
 
 	public interface ResultSetProcessor<T> {
 		T process(ResultSet resultSet);
 	}
 
 	public static List<Map<String, Object>> queryRows(Connection connection, SqlExpression queryStatement) {
 		return execute(connection, QueryType.SELECT, queryStatement.getSql(), queryStatement.getParameters(), new RowProcessor() );
 	}
 
 	public static List<Map<String, Object>> queryRows(DataSource db, SqlExpression queryStatement) {
 		return execute(db, QueryType.SELECT, queryStatement.getSql(), queryStatement.getParameters(), new RowProcessor() );
 	}
 	
 	public static List<Map<String, Object>> queryRows(Connection connection, String sql, Object... params) {
 		return execute(connection, QueryType.SELECT, sql, Arrays.asList(params), new RowProcessor() );
 	}
 	
 	public static List<Map<String, Object>> queryRows(DataSource db, String sql, Object... params) {
 		return execute(db, QueryType.SELECT, sql, Arrays.asList(params), new RowProcessor() );
 	}
 	
 	private static class RowProcessor implements ResultSetProcessor<List<Map<String, Object>>> {
 		public List<Map<String, Object>> process(ResultSet rs) {
 			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 			try {
 				List<String> fieldNames = extractResultSetFieldNames(rs);
 				while (rs.next()) {
 					Map<String, Object> row = new HashMap<String, Object>(fieldNames.size());
 					for (String fieldName : fieldNames) {
 						row.put(fieldName, rs.getObject(fieldName));
 					}
 					result.add(row);
 				}
 				return result;
 			} catch (SQLException e) {
 				throw new DatabaseException(e);
 			}
 		}
 	}
 
 	public static int update(DataSource db, String tableName, Map<String, Object> values, Map<String, Object> ids) {
 		SqlExpression updateSql = buildUpdate(tableName, values, ids);
		return (Integer)execute(db, QueryType.UPDATE, updateSql.getSql(), updateSql.getParameters(), null);
 	}
 	
 	public static int update(Connection connection, String tableName, Map<String, Object> values, Map<String, Object> ids) {
 		SqlExpression updateSql = buildUpdate(tableName, values, ids);
		return (Integer)execute(connection, QueryType.UPDATE, updateSql.getSql(), updateSql.getParameters(), null);
 	}
 	
 	public static int update(DataSource db, SqlExpression update) {
		return (Integer)execute(db, QueryType.UPDATE, update.getSql(), update.getParameters(), null);
 	}
 
 	public static int update(Connection conn, SqlExpression update) {
		return (Integer)execute(conn, QueryType.UPDATE, update.getSql(), update.getParameters(), null);
 	}
 	
 	public static <PK> PK insert(DataSource db, String tableName, Map<String, ? extends Object> values) {
 		SqlExpression insertSql = buildInsertOrUpdate(tableName, values, false);
 		return execute(db, QueryType.INSERT, insertSql.getSql(), insertSql.getParameters(), null);
 	}
 	
 	public static <PK> PK insert(Connection connection, String tableName, Map<String, ? extends Object> values) {
 		SqlExpression insertSql = buildInsertOrUpdate(tableName, values, false);
 		return execute(connection, QueryType.INSERT, insertSql.getSql(), insertSql.getParameters(), null);
 	}
 	
 	public static <PK> PK insertOrUpdate(DataSource db, String tableName, Map<String, ? extends Object> values) {
 		SqlExpression insertSql = buildInsertOrUpdate(tableName, values, true);
 		return execute(db, QueryType.INSERT, insertSql.getSql(), insertSql.getParameters(), null);
 	}
 	
 	public static <PK> PK insertOrUpdate(Connection connection, String tableName, Map<String, ? extends Object> values) {
 		SqlExpression insertSql = buildInsertOrUpdate(tableName, values, true);
 		return execute(connection, QueryType.INSERT, insertSql.getSql(), insertSql.getParameters(), null);
 	}
 	
 	private static SqlExpression buildInsertOrUpdate(String tableName, Map<String, ? extends Object> values, boolean addOnDuplicateKeyClause) {
 		List<String> qmarks = new ArrayList<String>();
 		List<String> quotedFields = new ArrayList<String>();
 		List<Object> params = new ArrayList<Object>();
 		List<String> updateList = new ArrayList<String>();
 
 		for (String f : values.keySet()) {
 			qmarks.add("?");
 			String quotedField = "`" + f + "`";
 			quotedFields.add(quotedField);
 			params.add(values.get(f));
 			updateList.add(quotedField + "=?");
 		}
 		if (addOnDuplicateKeyClause) {
 			params.addAll(new ArrayList<Object>(params));
 		}
 		String sql = "INSERT INTO `" + tableName + "` (" + implode(",", quotedFields) + ")" + " VALUES (" + implode(",", qmarks) + ")";
 		
 		if (addOnDuplicateKeyClause) {
 			sql += " ON DUPLICATE KEY UPDATE " + implode(",", updateList);
 		}
 		
 		return new SqlExpression(sql, params);
 	}
 	
 	private static SqlExpression buildUpdate(String tableName, Map<String, ? extends Object> values, Map<String, ? extends Object> ids) {
 		List<String> qmarks = new ArrayList<String>();
 		List<String> assignments = new ArrayList<String>();
 		List<Object> params = new ArrayList<Object>();
 		List<String> wheres = new ArrayList<String>();
 
 		for (String f : values.keySet()) {
 			qmarks.add("?");
 			assignments.add("`" + f + "`=?");
 			params.add(values.get(f));
 		}
 		
 		for (String idField : ids.keySet()) {
 			qmarks.add("?");
 			wheres.add("`" + idField + "`=?");
 			params.add(ids.get(idField));
 		}
 		
 		String sql = "UPDATE `" + tableName + "` SET " + implode(", ", assignments) + " WHERE " + implode(" AND ", wheres);
 		return new SqlExpression(sql, params);
 	}
 
 	public static void executeDDL(DataSource db, String ddl) {
 		execute(db, QueryType.DDL, ddl, null, null);
 	}
 
 	public static void executeDDL(Connection connection, String ddl) {
 		execute(connection, QueryType.DDL, ddl, null, null);
 	}
 
 	public static <T> T execute(DataSource db, QueryType type, String sql, Iterable<Object> params, ResultSetProcessor<T> processor) {
 		Connection connection = null;
 		try {
 			connection = db.getConnection();
 			return execute(connection, type, sql, params, processor);
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		} finally {
 			try {
 				if (connection != null) {
 					connection.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private static <T> T execute(Connection connection, QueryType type, String sql, Iterable<Object> params, ResultSetProcessor<T> processor) {
 		try {
 			PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 			if (params != null) {
 				int index = 1;
 				for (Object val : params) {
 					if (val != null && val.getClass().isEnum()) {
 						stmt.setObject(index++, ((Enum<?>)val).name());
 					} else {
 						stmt.setObject(index++, val);
 					}
 				}
 			}
 			boolean success = false;
 			try {
 				switch (type) {
 				case DDL:
 					stmt.executeUpdate();
 					success = true;
 					break;
 				case SELECT:
 					ResultSet resultSet = stmt.executeQuery();
 					success = true;
 					if (processor != null) {
 						return processor.process(resultSet);
 					}
 					return (T) resultSet;
 				case INSERT:
 					stmt.executeUpdate();
 					success = true;
 					ResultSet keysResult = stmt.getGeneratedKeys();
 					if (keysResult != null && keysResult.next()) {
 						return (T) keysResult.getObject(1);
 					} else {
 						return null;
 					}
 				case UPDATE:
 					Integer affectedRows = stmt.executeUpdate();
 					success = true;
 					return (T) affectedRows;
 				}
 			} finally {
 				stmt.close();
 				if (!success) {
 					System.err.println("QUERY FAILED: " + sql);
 				}
 			}
 			return null;
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 			throw new DatabaseException(e);
 		}
 	}
 
 	private static List<String> extractResultSetFieldNames(ResultSet result) throws SQLException {
 		ResultSetMetaData metaData = result.getMetaData();
 		List<String> fieldNames = new ArrayList<String>();
 		for (int i = 0; i < metaData.getColumnCount(); i++) {
 			fieldNames.add(metaData.getColumnLabel(i + 1));
 		}
 		return fieldNames;
 	}
 
 	public static <T> T runInTransaction(Connection connection, Transaction<T> transaction) {
 		boolean success = false;
 		try {
 			connection.setAutoCommit(false);
 			T result = transaction.run(connection);
 			connection.commit();
 			success = true;
 			return result;
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		} finally {
 			if (!success) {
 				try {
 					connection.rollback();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static <T> T runInTransaction(DataSource dataSource, Transaction<T> transaction) {
 		Connection connection = null;
 		try {
 			connection = dataSource.getConnection();
 			return runInTransaction(connection, transaction);
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		} finally {
 			try {
 				if (connection != null) {
 					connection.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
