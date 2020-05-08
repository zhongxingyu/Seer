 package com.aconex.scrutineer.jdbc;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import com.aconex.scrutineer.IdAndVersion;
 import com.aconex.scrutineer.IdAndVersionStream;
 import com.aconex.scrutineer.LogUtils;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 import org.apache.log4j.Logger;
 
 public class JdbcIdAndVersionStream implements IdAndVersionStream {
 
     private final DataSource dataSource;
     private final String sql;
     private Connection connection;
     private Statement statement;
     private static final Logger LOG = LogUtils.loggerForThisClass();
     private ResultSet resultSet;
 
     public JdbcIdAndVersionStream(DataSource dataSource, String sql) {
         this.dataSource = dataSource;
         this.sql = sql;
     }
 
     @Override
     public void open() {
         try {
             connection = dataSource.getConnection();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public Iterator<IdAndVersion> iterator() {
         try {
             statement = connection.createStatement();
             resultSet = statement.executeQuery(sql);
             return new IdAndVersionResultSetIterator(resultSet);
         } catch (SQLException e) {
             throw new RuntimeException();
         }
     }
 
     @Override
     public void close() {
         throwExceptionIfAnyCloseFails(closeResultSet(), closeStatement(), closeConnection());
     }
 
     private void throwExceptionIfAnyCloseFails(SQLException... sqlExceptions) {
        if (!gIterables.all(Arrays.asList(sqlExceptions), Predicates.<Object>isNull())) {
             throw new RuntimeException("At least one error occured during close, see logs for more details, there may be multiple");
         }
     }
 
     private SQLException closeConnection() {
         SQLException sqlException = null;
         if (connection != null) {
             try {
                 connection.close();
             } catch (SQLException e) {
                 sqlException = e;
                 LogUtils.error(LOG, "Cannot close connection", e);
             }
         }
         return sqlException;
     }
 
     private SQLException closeStatement() {
         SQLException sqlException = null;
         if (statement != null) {
             try {
                 statement.close();
             } catch (SQLException e) {
                 sqlException = e;
                 LogUtils.error(LOG, "Cannot close statement", e);
             }
         }
         return sqlException;
     }
 
     private SQLException closeResultSet() {
         SQLException sqlException = null;
         if (resultSet != null) {
             try {
                 resultSet.close();
             } catch (SQLException e) {
                 sqlException = e;
                 LogUtils.error(LOG, "Cannot close resultset", e);
             }
         }
         return sqlException;
     }
 
 }
