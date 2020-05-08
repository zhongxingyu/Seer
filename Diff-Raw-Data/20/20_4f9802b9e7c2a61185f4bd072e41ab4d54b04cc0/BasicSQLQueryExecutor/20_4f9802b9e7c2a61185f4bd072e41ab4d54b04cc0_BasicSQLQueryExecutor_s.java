 /**
  * 
  */
 package za.co.sindi.sql.impl;
 
import java.security.Policy.Parameters;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 
 import za.co.sindi.sql.AbstractSQLExecutor;
 import za.co.sindi.sql.DatabaseExecutionException;
 import za.co.sindi.sql.GeneratedKeysHandler;
 import za.co.sindi.sql.PreparedParameters;
 import za.co.sindi.sql.ResultSetHandler;
 import za.co.sindi.sql.ResultSetInfo;
 import za.co.sindi.sql.SQLQueryExecutor;
 import za.co.sindi.sql.utils.SQLUtils;
 
 /**
  * @author Bienfait Sindi
  * @since 03 January 2013
  *
  */
 public class BasicSQLQueryExecutor extends AbstractSQLExecutor implements SQLQueryExecutor {
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeQuery(java.lang.String, za.co.sindi.sql.ResultSetHandler)
 	 */
 	public <T> T executeQuery(String query, ResultSetHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		return executeQuery(query, (PreparedParameters)null, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeQuery(java.lang.String, java.lang.Object[], za.co.sindi.sql.ResultSetHandler)
 	 */
 	public <T> T executeQuery(String query, Object[] parameters, ResultSetHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		return executeQuery(query, parameters, null, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeQuery(java.lang.String, za.co.sindi.sql.PreparedParameters, za.co.sindi.sql.ResultSetHandler)
 	 */
 	public <T> T executeQuery(String query, PreparedParameters parameters, ResultSetHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub		
 		return executeQuery(query, parameters, null, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeQuery(java.lang.String, za.co.sindi.sql.ResultSetInfo, za.co.sindi.sql.ResultSetHandler)
 	 */
 	public <T> T executeQuery(String query, ResultSetInfo info, ResultSetHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		return executeQuery(query, (PreparedParameters)null, info, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeQuery(java.lang.String, java.lang.Object[], za.co.sindi.sql.ResultSetInfo, za.co.sindi.sql.ResultSetHandler)
 	 */
 	public <T> T executeQuery(String query, Object[] parameters, ResultSetInfo info, ResultSetHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		PreparedParameters params = null;
 		if (parameters != null) {
 			params = new PreparedStatementParameters();
 			
 			for (int i = 0; i < parameters.length; i++) {
 				params.setObject(i + 1, parameters[i]);
 			}
 		}
 		
 		return executeQuery(query, params, info, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeQuery(java.lang.String, za.co.sindi.sql.PreparedParameters, za.co.sindi.sql.ResultSetInfo, za.co.sindi.sql.ResultSetHandler)
 	 */
 	public <T> T executeQuery(String query, PreparedParameters parameters, ResultSetInfo info, ResultSetHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub	
 		if (connection == null) {
 			throw new IllegalStateException("No JDBC Connection provided.");
 		}
 		
 		if (logger.isLoggable(Level.INFO)) {
 			logger.info("Executing SQL query: " + query);
 		}
 		
 		PreparedStatement statement = null;
 		ResultSet rs = null;
 		T result = null;
 		
 		try {
 			updateCount = 0;
 			if (info != null) {
 				if (info.getResultSetHoldability() > 0) {
 					statement = connection.prepareStatement(query, info.getResultSetType(), info.getResultSetConcurrency(), info.getResultSetHoldability());
 				} else {
 					statement = connection.prepareStatement(query, info.getResultSetType(), info.getResultSetConcurrency());
 				}
 			} else {
 				statement = connection.prepareStatement(query);
 			}
 
 			//Prepare the statement before execution
 			prepareStatement(statement);
 			
 			//Populate parameters
 			if (parameters != null) {
 				parameters.visitParameters(statement);
 			}
 			
 			//Get RestSet
 			rs = statement.executeQuery();
 			if (rs != null && handler != null) {
 				result = handler.handle(rs);
 			}
 		} catch (SQLException sqle) {
 			String message = "SQLException from running query (" + query + ").";
 			logger.log(Level.SEVERE, message, sqle);
 			throw new DatabaseExecutionException(message, sqle);
 		} finally {
 			try {
 				SQLUtils.close(rs);
 				SQLUtils.close(statement);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				logger.log(Level.WARNING, "Error closing a ResultSet or Statement.", e);
 			} finally {
 				if (closeConnection) {
 					try {
 						SQLUtils.close(connection);
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						logger.log(Level.WARNING, "Error closing a Connection.", e);
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeUpdate(java.lang.String)
 	 */
 	public int executeUpdate(String query) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
		return executeUpdate(query, (Parameters)null);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeUpdate(java.lang.String, java.lang.Object[])
 	 */
 	public int executeUpdate(String query, Object... parameters) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		return executeUpdate(query, parameters, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeUpdate(java.lang.String, za.co.sindi.sql.PreparedParameters)
 	 */
 	public int executeUpdate(String query, PreparedParameters parameters) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		executeUpdate(query, parameters, null);
 		return updateCount;
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeUpdate(java.lang.String, java.lang.Object[], za.co.sindi.sql.GeneratedKeysResultSetHandler)
 	 */
 	public <K> K executeUpdate(String query, Object[] parameters, GeneratedKeysHandler<K> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		PreparedParameters params = null;
 		if (parameters != null) {
 			params = new PreparedStatementParameters();
 			
 			for (int i = 0; i < parameters.length; i++) {
 				params.setObject(i + 1, parameters[i]);
 			}
 		}
 		
 		return executeQuery(query, params, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeUpdate(java.lang.String, za.co.sindi.sql.PreparedParameters, za.co.sindi.sql.GeneratedKeysResultSetHandler)
 	 */
 	public <K> K executeUpdate(String query, PreparedParameters parameters, GeneratedKeysHandler<K> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		if (connection == null) {
 			throw new IllegalStateException("No JDBC Connection provided.");
 		}
 		
 		if (logger.isLoggable(Level.INFO)) {
 			logger.info("Executing SQL query update: " + query);
 		}
 		
 		PreparedStatement statement = null;
 		ResultSet keys = null;
 		K keyValue = null;
 		
 		try {
 			if (handler != null) {
 				if (handler.getColumnIndexes() != null && handler.getColumnIndexes().length > 0) {
					statement = connection.prepareStatement(query);
 				} else if (handler.getColumnNames() != null && handler.getColumnNames().length > 0) {
 					statement = connection.prepareStatement(query, handler.getColumnNames());
 				} else {
 					statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
 				}
 			} else {
 				statement = connection.prepareStatement(query);
 			}
 				
 			//Prepare the statement before execution
 			prepareStatement(statement);
 			
 			//Populate parameters
 			if (parameters != null) {
 				parameters.visitParameters(statement);
 			}
 			
 			//Update
 			updateCount = statement.executeUpdate();
 			
 			//Handle generated keys
 			if (handler != null) {
 				keys = statement.getGeneratedKeys();
 				keyValue = handler.handle(keys);
 			}
 		} catch (SQLException sqle) {
 			String message = "SQLException from running query (" + query + ").";
 			logger.log(Level.SEVERE, message, sqle);
 			throw new DatabaseExecutionException(message, sqle);
 		} finally {
 			try {
 				SQLUtils.close(keys);
 				SQLUtils.close(statement);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				logger.log(Level.WARNING, "Error closing a ResultSet or Statement.", e);
 			} finally {
 				if (closeConnection) {
 					try {
 						SQLUtils.close(connection);
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						logger.log(Level.WARNING, "Error closing a Connection.", e);
 					}
 				}
 			}
 		}
 		
 		return keyValue;
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeBatch(java.lang.String, java.lang.Object[][])
 	 */
 	public int[] executeBatch(String query, Object[][] parameters) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		if (parameters == null) {
 			throw new IllegalArgumentException("Parameters are required to do Statement batch insert/update.");
 		}
 		
 		PreparedParameters[] params = new PreparedParameters[parameters.length];
 		for (int i = 0; i < parameters.length; i++) {
 			params[i] = new PreparedStatementParameters();
 			
 			for (int j = 0; j < parameters[i].length; j++) {
 				params[i].setObject(j + 1, parameters[i][j]);
 			}
 		}
 		
 		return executeBatch(query, params);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.SQLQueryExecutor#executeBatch(java.lang.String, za.co.sindi.sql.PreparedParameters[])
 	 */
 	public int[] executeBatch(String query, PreparedParameters[] parametersArray) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		if (parametersArray == null) {
 			throw new IllegalArgumentException("Parameters are required to do Statement batch insert/update.");
 		}
 		
 		if (connection == null) {
 			throw new IllegalStateException("No JDBC Connection provided.");
 		}
 		
 		if (logger.isLoggable(Level.INFO)) {
 			logger.info("Executing batch update query: " + query);
 		}
 		
 		PreparedStatement statement = null;
 		int[] rows = null;
 		
 		try {
 			updateCount = 0;
 			statement = connection.prepareStatement(query);
 			statement.clearBatch();
 			
 			//Prepare the statement before execution
 			prepareStatement(statement);
 			
 			//Populate parameters
 			if (parametersArray != null) {
 				for (PreparedParameters parameter : parametersArray) {
 					parameter.visitParameters(statement);
 					statement.addBatch();
 				}
 			}
 			
 			//Update
 			rows = statement.executeBatch();
 		} catch (SQLException sqle) {
 			String message = "SQLException from running query (" + query + ").";
 			logger.log(Level.SEVERE, message, sqle);
 			throw new DatabaseExecutionException(message, sqle);
 		} finally {
 			try {
 				SQLUtils.close(statement);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
				logger.log(Level.WARNING, "Error closing a ResultSet or Statement.", e);
 			} finally {
 				if (closeConnection) {
 					try {
 						SQLUtils.close(connection);
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						logger.log(Level.WARNING, "Error closing a Connection.", e);
 					}
 				}
 			}
 		}
 		
 		if (rows != null) {
 			updateCount = rows.length;
 		} else {
 			updateCount = 0;
 		}
 		
 		return rows;
 	}
 }
