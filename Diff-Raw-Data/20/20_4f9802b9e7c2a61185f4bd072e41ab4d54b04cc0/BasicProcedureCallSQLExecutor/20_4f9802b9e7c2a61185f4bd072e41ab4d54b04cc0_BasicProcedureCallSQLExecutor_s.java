 /**
  * 
  */
 package za.co.sindi.sql.impl;
 
 import java.sql.CallableStatement;
 import java.sql.SQLException;
 import java.util.logging.Level;
 
 import za.co.sindi.sql.AbstractSQLExecutor;
 import za.co.sindi.sql.CallableParameters;
 import za.co.sindi.sql.CallableStatementHandler;
 import za.co.sindi.sql.DatabaseExecutionException;
 import za.co.sindi.sql.ProcedureCallSQLExecutor;
 import za.co.sindi.sql.ResultSetInfo;
 import za.co.sindi.sql.utils.SQLUtils;
 
 /**
  * @author Bienfait Sindi
  * @since 07 January 2013
  *
  */
 public class BasicProcedureCallSQLExecutor extends AbstractSQLExecutor implements ProcedureCallSQLExecutor {
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.ProcedureCallSQLExecutor#executeCall(java.lang.String, java.lang.Object[], za.co.sindi.sql.CallableStatementHandler)
 	 */
 	public <T> T executeCall(String call, Object[] parameters, CallableStatementHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		return executeCall(call, parameters, null, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.ProcedureCallSQLExecutor#executeCall(java.lang.String, za.co.sindi.sql.CallableParameters, za.co.sindi.sql.CallableStatementHandler)
 	 */
 	public <T> T executeCall(String call, CallableParameters parameters, CallableStatementHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		return executeCall(call, parameters, null, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.ProcedureCallSQLExecutor#executeCall(java.lang.String, java.lang.Object[], za.co.sindi.sql.ResultSetInfo, za.co.sindi.sql.CallableStatementHandler)
 	 */
 	public <T> T executeCall(String call, Object[] parameters, ResultSetInfo info, CallableStatementHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		CallableParameters params = null;
 		if (parameters != null) {
 			params = new CallableStatementParameters();
 			
 			for (int i = 0; i < parameters.length; i++) {
 				params.setObject(i + 1, parameters[i]);
 			}
 		}
 		
 		return executeCall(call, params, info, handler);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.ProcedureCallSQLExecutor#executeCall(java.lang.String, za.co.sindi.sql.CallableParameters, za.co.sindi.sql.ResultSetInfo, za.co.sindi.sql.CallableStatementHandler)
 	 */
 	public <T> T executeCall(String call, CallableParameters parameters, ResultSetInfo info, CallableStatementHandler<T> handler) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		if (connection == null) {
 			throw new IllegalStateException("No JDBC Connection provided.");
 		}
 		
 		if (logger.isLoggable(Level.INFO)) {
 			logger.info("Executing stored procedure: " + call);
 		}
 		
 		CallableStatement statement = null;
 		T result = null;
 		
 		try {
 			if (info != null) {
 				if (info.getResultSetHoldability() > 0) {
 					statement = connection.prepareCall(call, info.getResultSetType(), info.getResultSetConcurrency(), info.getResultSetHoldability());
 				} else {
 					statement = connection.prepareCall(call, info.getResultSetType(), info.getResultSetConcurrency());
 				}
 			} else {
 				statement = connection.prepareCall(call);
 			}
 
 			//Prepare the statement before execution
 			prepareStatement(statement);
 			
 			//First, register out parameters.
 			parameters.visitOUTParameters(statement);
 			
 			//Populate parameters
 			if (parameters != null) {
 				parameters.visitParameters(statement);
 			}
 			
 			//Execute statement
 			boolean useResultSet = statement.execute();
 			if (!useResultSet) {
 				updateCount = statement.getUpdateCount();
 			}
 			
 			if (handler != null) {
 				result = handler.handle(statement);
 			}
 		} catch (SQLException sqle) {
 			String message = "SQLException from running call (" + call + ").";
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
 		
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.ProcedureCallSQLExecutor#executeBatch(java.lang.String, java.lang.Object[][])
 	 */
 	public int[] executeBatch(String call, Object[][] parameters) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		if (parameters == null) {
 			throw new IllegalArgumentException("Parameters are required to do CallableStatement batch insert/update.");
 		}
 		
 		CallableParameters[] params = new CallableParameters[parameters.length];
 		for (int i = 0; i < parameters.length; i++) {
 			params[i] = new CallableStatementParameters();
 			
 			for (int j = 0; j < parameters[i].length; j++) {
 				params[i].setObject(j + 1, parameters[i][j]);
 			}
 		}
 		
 		return executeBatch(call, params);
 	}
 
 	/* (non-Javadoc)
 	 * @see za.co.sindi.sql.ProcedureCallSQLExecutor#executeBatch(java.lang.String, za.co.sindi.sql.CallableParameters[])
 	 */
 	public int[] executeBatch(String call, CallableParameters[] parametersArray) throws DatabaseExecutionException {
 		// TODO Auto-generated method stub
 		if (parametersArray == null) {
 			throw new IllegalArgumentException("Parameters are required to do Statement batch insert/update.");
 		}
 		
 		if (connection == null) {
 			throw new IllegalStateException("No JDBC Connection provided.");
 		}
 		
 		if (logger.isLoggable(Level.INFO)) {
 			logger.info("Executing batch update procedure: " + call);
 		}
 		
 		CallableStatement statement = null;
 		int[] rows = null;
 		
 		try {
 			updateCount = 0;
 			statement = connection.prepareCall(call);
 			statement.clearBatch();
 			
 			//Prepare the statement before execution
 			prepareStatement(statement);
 			
 			//Populate parameters
 			if (parametersArray != null) {
 				for (CallableParameters parameter : parametersArray) {
 					parameter.visitParameters(statement);
 					statement.addBatch();
 				}
 			}
 			
 			//Update
 			rows = statement.executeBatch();
 		} catch (SQLException sqle) {
 			String message = "SQLException from running call (" + call + ").";
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
