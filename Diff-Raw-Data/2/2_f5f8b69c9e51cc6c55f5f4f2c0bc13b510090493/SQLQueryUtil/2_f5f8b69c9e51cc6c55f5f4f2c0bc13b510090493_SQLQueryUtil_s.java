 package edu.wustl.cab2b.server.util;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 
 /**
  * This provides utility methods to execute UPDATE and SELECT type of queries using Data-source.
  * The SELECT queries can be parameterized.
  * @author Chandrakant Talele
  */
 public class SQLQueryUtil {
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(SQLQueryUtil.class);
     /**
      * This method executes given update SQL and returns either the row count for <br>
      * INSERT, UPDATE or DELETE statements, or 0 for SQL statements that return nothing. 
      * @param sql SQL to execute
      * @param connection database connection to be used
      * @return Returns the result of execution
      */
     public static int executeUpdate(String sql, Connection connection) {
         Statement statement = null;
         int result = -1;
 
         try {
             statement = connection.createStatement();
             logger.debug("Executing the SQL :  " + sql);
             result = statement.executeUpdate(sql);
 
         } catch (SQLException e) {
             throw new RuntimeException("SQL Exception while firing an Update Query",e,ErrorCodeConstants.DB_0004);
         } finally {
             close(statement);
         }
         return result;
     }
     /**
      * This method executes the given SELECT SQL query using passes parameters.
      * It uses prepare statement and maintains a static map of those to reuse those if same query is fired again.
      * @param sql The SQL statement.
      * @param connection database connection to be used
      * @param params All the parameter objects.
      * @return String[][] as result of the query with each row represents one record of result set and 
      * each column in array is a column present in SELECT clause. 
      * The order of columns is same as that present in the passes SQL.   
      */
     public static String[][] executeQuery(String sql, Connection connection, Object... params) {
         
         PreparedStatement prepareStatement = null;
         ResultSet rs = null;
         logger.debug("Executing the SQL :  " + sql);
         try {
             prepareStatement = connection.prepareStatement(sql);    
             
             int index = 1;
             for (Object param : params) {
                 logger.debug("Param " + index + " : " + param);
                 if (param instanceof BigDecimal) {
                     prepareStatement.setBigDecimal(index++, (BigDecimal) param);
                 } else if (param instanceof String) {
                     prepareStatement.setString(index++, (String) param);
                 } else if (param instanceof Long) {
                     prepareStatement.setLong(index++, (Long) param);
                 } else if (param instanceof Integer) {
                     prepareStatement.setInt(index++, (Integer) param);
                 } else if (param instanceof Float) {
                     prepareStatement.setFloat(index++, (Float) param);
                 } else {
                     logger.error("Object Type not identfied for param " + param.toString());
                     prepareStatement.setObject(index++, param);
                 }
             }
             rs = prepareStatement.executeQuery();
             return getResult(rs);
         } catch (SQLException e) {
             throw new RuntimeException("Exception while firing Parameterized query.", e,ErrorCodeConstants.DB_0003);
         }
     }
 
     /**
      * Closes the statement
      * @param statement Statement to be closed.
      */
     protected static void close(Statement statement) {
 
         if (statement != null) {
             try {
                 statement.close();
             } catch (SQLException e) {
                 //DO Nothing
                 logger.debug(e.toString());
             }
         }
     }
     
     /**
      * This method executes the given SELECT SQL query using passes parameters.
      * It uses prepare statement and maintains a static map of those to reuse those if same query is fired again.
      * @param prepareStatement prepared statement to execute
      * @return String[][] as result of the query with each row represents one record of result set and 
      * each column in array is a column present in SELECT clause. 
      * The order of columns is same as that present in the passes SQL.   
      */
     public static String[][] executeQuery(PreparedStatement prepareStatement) {
         try {
             ResultSet rs = prepareStatement.executeQuery();
             return getResult(rs);
         } catch (SQLException e) {
             throw new RuntimeException("Exception while firing Parameterized query.", e,ErrorCodeConstants.DB_0003);
         }
     }
     
     /**
      * @param rs Result set to process
      * @return String[][] created by getting all records of result set
      * @throws SQLException if some error occurred while processing result set
      */
     private static String[][] getResult(ResultSet rs) throws SQLException {
         List<String[]> results = new ArrayList<String[]>();
         int noOfColumns = rs.getMetaData().getColumnCount();
 
         while (rs.next()) {
             String[] oneRow = new String[noOfColumns];
             for (int i = 1; i <= noOfColumns; i++) {
                 oneRow[i - 1] = rs.getString(i);
             }
             results.add(oneRow);
         }
 
        return results.toArray(new String[0][0]);
     }
 }
