 package com.aconex.scrutineer.jdbc;
 
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
 
     private final String sql;
     private Connection connection;
     private Statement statement;
     private static final Logger LOG = LogUtils.loggerForThisClass();
     private ResultSet resultSet;
 
     public JdbcIdAndVersionStream(Connection connection, String sql) {
         this.connection = connection;
         this.sql = sql;
     }
 
     @Override
     public void open() {
     }
 
     @Override
     public Iterator<IdAndVersion> iterator() {
         try {
             statement = connection.createStatement();
             resultSet = statement.executeQuery(sql);
             return new IdAndVersionResultSetIterator(resultSet);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void close() {
         throwExceptionIfAnyCloseFails(closeResultSet(), closeStatement());
     }
 
     private void throwExceptionIfAnyCloseFails(SQLException... sqlExceptions) {
         if (!Iterables.all(Arrays.asList(sqlExceptions), Predicates.<Object>isNull())) {
             throw new RuntimeException("At least one error occured during close, see logs for more details, there may be multiple");
         }
     }
 
    @SuppressWarnings("PMD.NcssMethodCount")
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
 
    @SuppressWarnings("PMD.NcssMethodCount")
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
