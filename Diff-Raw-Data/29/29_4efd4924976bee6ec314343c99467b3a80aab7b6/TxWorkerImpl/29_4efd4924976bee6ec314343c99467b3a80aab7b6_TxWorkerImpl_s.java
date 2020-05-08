 package juitar.worker.jdbc;
 
 import org.juitar.monitoring.api.Monitored;
 import org.juitar.workerq.CompletionStatus;
 import org.juitar.workerq.Result;
 import org.juitar.workerq.Work;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  * @author sha1n
  * Date: 2/22/13
  */
 class TxWorkerImpl implements Runnable, TxWorker {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(TxWorkerImpl.class);
 
     private int index;
     private Queue<Work> txQueue;
     private DataSource dataSource;
     private Connection connection;
     private int commitInterval = 100;
 
     final void setIndex(int index) {
         this.index = index;
     }
 
     final void setTxQueue(Queue<Work> txQueue) {
         this.txQueue = txQueue;
     }
 
     final void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     final void setCommitInterval(int commitInterval) {
         this.commitInterval = commitInterval;
     }
 
 
     @Override
     public final void run() {
         Thread.currentThread().setName(getClass().getSimpleName() + "-" + index);
         obtainConnection();
         processQueue();
     }
 
     @Override
     public final void addTxWork(Work txWork) {
         txQueue.add(txWork);
     }
 
 
     private void obtainConnection() {
         try {
             if (connection == null || connection.isClosed()) {
                 this.connection = dataSource.getConnection();
                 this.connection.setAutoCommit(false);
             }
         } catch (SQLException e) {
             LOGGER.error("Failed to open database connection. Will retry next schedule.", e);
         }
     }
 
 
     @Monitored(threshold = 1000)
     private void processQueue() {
         long txStart = System.currentTimeMillis();
         AccumulativeTransaction tx = new AccumulativeTransaction(connection);
         LOGGER.debug("Database Transaction started.");
         try (Statement s = connection.createStatement()) {
             while (!txQueue.isEmpty()) {
                 Work work = null;
                 try {
                     work = txQueue.poll();
                     tx.addBatch(s, work);
                 } catch (Exception e) {
                     LOGGER.error("Worker caught an exception", e);
                     if (work != null) {
                         work.getCompletionCallback().onFailure(new Result(work.getId()), e, CompletionStatus.ERROR);
                     }
                 } catch (Throwable e) {
                     LOGGER.error("SEVERE: Worker caught non-java.lang.Exception type.", e);
                 }
 
                 if (System.currentTimeMillis() - txStart > (commitInterval)) {
                     LOGGER.debug("Going to commit with non-empty queue.");
                     break;
                 }
 
             }
         } catch (SQLException e) {
             LOGGER.error("Failed to process database operation.", e);
         } finally {
             LOGGER.debug("Database Transaction going to commit.");
             tx.commit(); // TODO should implement a robust retry and discrete result status population.
         }
     }
 
     private static class AccumulativeTransaction {
         private Connection connection;
         private List<Work> workList = new ArrayList<>();
 
         private AccumulativeTransaction(Connection connection) {
             this.connection = connection;
         }
 
 
         /**
          * This is a stupid implementation since the results are useless and one failure fails all with no tracking whatsoever...
          */
         public boolean addBatch(Statement s, Work work) throws SQLException {
             boolean success = false;
             if (work != null) {
                 String[] payload = (String[]) work.getPayload();
 
                 for (String sql : payload) {
                     s.addBatch(sql);
                 }
 
                 workList.add(work);
 
                 success = true;
             }
             return success;
         }
 
 
         public void commit() {
             try {
                 if (!workList.isEmpty()) {
                     connection.commit(); // TODO should implement a robust retry and discrete result status population.
                     reportTxSuccess();
                     LOGGER.debug("Committed %d logical transactions", workList.size());
                 }
             } catch (SQLException e) {
                 reportTxFailure(e);
             }
 
         }
 
         private void reportTxSuccess() {
             for (Work work : workList) {
                 Result result = new Result(work.getId());
                 result.setResultData("Transaction Committed Successfully!");
                 work.getCompletionCallback().onSuccess(result); // TODO not safe
             }
         }
 
         private void reportTxFailure(Exception exception) {
             LOGGER.error("Commit failed.");
             for (Work work : workList) {
                 Result result = new Result(work.getId());
                 result.setResultData("Transaction Failed!");
                 work.getCompletionCallback().onFailure(result, exception, CompletionStatus.ERROR); // TODO not safe
             }
         }
 
     }
 
 }
