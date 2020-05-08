 package com.rcosnita.experiments.rdbmsreduce.reductor;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 
 import org.apache.log4j.Logger;
 
 /**
  * Class used to provide a thread that can execute a query and retrieve 
  * the result set.
  * 
  * @author Radu Viorel Cosnita
  * @version 1.0
  * @since 12.07.2012
  */
 public class ReduceThread implements Callable<List<Map<String, Object>>> {
 	private final static Logger logger = Logger.getLogger(ReduceThread.class);
 	
 	private Connection conn;
 	private String sql;
 	
 	/**
 	 * Constructor used to initialize the connection and sql used during this thread.
 	 * 
 	 * @param conn
 	 * @param sql
 	 */
 	public ReduceThread(Connection conn, String sql) {
 		this.conn = conn;
 		this.sql = sql;
 	}
 
 	/**
 	 * Method that executes the sql query against the given connection and 
 	 * retrieving a result set.
 	 * 
 	 * @return
 	 */
 	@Override
 	public List<Map<String, Object>> call() throws SQLException {
 		logger.info(String.format("Obtaining results for query %s.", sql));
 		
 		Statement stmt = conn.createStatement();
 		ResultSet resultSet = stmt.executeQuery(sql);
 		
 		String[] colNames = this.getColumnNames(resultSet.getMetaData());
 		
 		List<Map<String, Object>> results= new ArrayList<Map<String, Object>>();
 
 		while(resultSet.next()) {
 			Map<String, Object> row = new HashMap<String, Object>();
 			
 			for(String colName : colNames) {
 				row.put(colName, resultSet.getObject(colName));
 			}
 			
 			results.add(row);
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Method used to return the column names from a result set.
 	 * 
 	 * @param res
 	 * @return
 	 * @throws SQLException
 	 */
 	private String[] getColumnNames(ResultSetMetaData resMeta) throws SQLException {
 		String[] colNames = new String[resMeta.getColumnCount()];
 		
 		for(int i = 1; i <= resMeta.getColumnCount(); i++) {
			colNames[i] = resMeta.getColumnName(i);
 		}
 		
 		return colNames;
 	}
 }
