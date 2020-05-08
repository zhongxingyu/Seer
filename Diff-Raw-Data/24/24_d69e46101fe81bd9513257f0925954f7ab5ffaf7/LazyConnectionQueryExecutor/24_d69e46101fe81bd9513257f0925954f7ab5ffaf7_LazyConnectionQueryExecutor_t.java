 package org.dajo.framework.db;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.dajo.chronometer.Chronometer;
 import org.dajo.loggers.db.DatabaseLogger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LazyConnectionQueryExecutor implements QueryExecutor {
 
     static private final Logger LOGGER = LoggerFactory.getLogger(LazyConnectionQueryExecutor.class);
 
     private final Lock lock = new ReentrantLock();
 
     private Connection connection = null;
 
    private Long lastTime = null;
 
     private final DatabaseConfig dbConfig;
 
     public LazyConnectionQueryExecutor(final DatabaseConfig dbConfig) {
         this.dbConfig = dbConfig;
     }
 
     @Override
     protected void finalize() {
         lock.lock();
         try {
             if (connection != null) {
                 DatabaseConnectionUtil.getInstance().closeConnection(connection);
                 connection = null;
             }
         } catch (RuntimeException re) {
             LOGGER.error("Unexpected.", re);
         } finally {
             lock.unlock();
         }
     }
 
     public boolean close() {
         lock.lock();
         try {
             if (connection != null) {
                 boolean result = DatabaseConnectionUtil.getInstance().closeConnection(connection);
                 connection = null;
                 return result;
             } else {
                 return false;
             }
         } catch (RuntimeException re) {
             LOGGER.error("Unexpected.", re);
             return false;
         } finally {
             lock.unlock();
         }
     }
 
     public long getIdleTime() {
         lock.lock();
         try {
            final long now = System.currentTimeMillis();
            final long validLastTime = lastTime != null ? lastTime.longValue() : 0l;
            return now - validLastTime;
         } finally {
             lock.unlock();
         }
     }
 
     @Override
     public InsertQueryResult executeInsertQuery(final InsertQueryInterface insertQuery) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public BatchInsertQueryResult executeBatchInsertQuery(final BatchInsertQueryInterface batchInsertQuery) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public UpdateQueryResult executeUpdateQuery(final UpdateQueryInterface updateQuery) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public <T> SelectQueryResult<T> executeSelectQuery(final SelectQueryInterface selectQuery, final SelectQueryResultAdapter<T> queryResultAdapter) {
 
         DatabaseLogger.logSelectQuery(selectQuery);
 
         final Chronometer chronometer = new Chronometer(this, "executeSelectQuery", selectQuery);
         chronometer.start();
 
         lock.lock();
         try {
 
             if (connection == null || connection.isClosed()) {
                 connection = DatabaseConnectionUtil.getInstance().getConnection(dbConfig);
             }
 
             final SelectQueryResult<T> result;
             if (connection != null) {
                 result = SelectQueryExecuter.executeSelectQuery(connection, selectQuery, queryResultAdapter);
             } else {
                 result = new SelectQueryResult<T>();
             }
 
 
             DatabaseLogger.logSelectResult(chronometer, result);
 
             return result;
         } catch (SQLException e) {
             LOGGER.error("Unexpected SQLException.", e);
             SelectQueryResult<T> error = new SelectQueryResult<T>();
             return error;
         } finally {
            lastTime = Long.valueOf(System.currentTimeMillis());
             lock.unlock();
             chronometer.close();
         }
 
     }
 
     static public void launchDatabaseReleaser(final LazyConnectionQueryExecutor queryExecutor, final int maxIdleTime, final long delay,
             final long peridToCheckAlive) {
         DatabaseReleaser releaserTask = new LazyConnectionQueryExecutor.DatabaseReleaser(queryExecutor, maxIdleTime);
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(releaserTask, delay, peridToCheckAlive);
     }
 
     private static final class DatabaseReleaser extends TimerTask {
         static private final Logger INNERLOGGER = LoggerFactory.getLogger(DatabaseReleaser.class);
         private final LazyConnectionQueryExecutor queryExecutor;
        private final long maxIdleTime;
 
        protected DatabaseReleaser(final LazyConnectionQueryExecutor queryExecutor, final long maxIdleTime) {
             this.queryExecutor = queryExecutor;
             this.maxIdleTime = maxIdleTime;
         }
         @Override
         public void run() {
             long idleTime = queryExecutor.getIdleTime();
             if (idleTime > maxIdleTime) {
                 if (queryExecutor.close() == true) {
                    INNERLOGGER.info("Max idle time reached, releasing the connection. maxIdleTime=" + maxIdleTime + ", idleTime=" + idleTime);
                 }
             }
         }
     }// class
 
 }// class
