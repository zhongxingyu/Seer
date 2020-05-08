 package org.h2o.eval.script.workload;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.h2o.eval.interfaces.IWorker;
 import org.h2o.eval.interfaces.IWorkload;
 
 public class WorkloadResult implements Serializable {
 
     private static final long serialVersionUID = -5199895309642140637L;
 
     /**
      * Whether the workload was successfully started.
      */
     private final boolean successfullyStarted;
 
     /**
      * Will be null if {@link #successfullyStarted} is true. Otherwise, either an SQLException, thrown because a database connection 
      * could not be created, or a WorkloadParseException, because the workload script is not syntactically correct.
      */
     private final Exception exception;
 
     /**
      * The number of transactions in this workload that were successfully executed.
      */
     private long successfullyExecutedTransactions;
 
     /**
      * The number of transactions in this workload that were attempted (includes both successful and unsuccessful executions).
      */
     private long totalAttemptedTransactions;
 
     private final IWorker worker;
 
     private String locationOfExecution;
     private final List<QueryLogEntry> queryLog;
 
     private IWorkload workload;
 
     /**
      * Called when a workload has not successfully initiated.
      * @param e Either an SQLException, thrown because a database connection could not be created, or a WorkloadParseException, because the workload script
      * is not syntactically correct.
      */
     public WorkloadResult(final Exception e, final IWorker worker, final IWorkload workload) {
 
         successfullyStarted = false;
         exception = e;
         queryLog = null;
         this.worker = worker;
         this.workload = workload;
     }
 
     public WorkloadResult(final List<QueryLogEntry> queryLog, final long successfullyExecutedTransactions, final long totalAttemptedTransactions, final IWorker worker, final IWorkload workload) {
 
         successfullyStarted = true;
         exception = null;
 
         this.successfullyExecutedTransactions = successfullyExecutedTransactions;
         this.totalAttemptedTransactions = totalAttemptedTransactions;
         this.worker = worker;
         this.workload = workload;
         this.queryLog = queryLog;
 
         locationOfExecution = worker.toString();
     }
 
     @Override
     public String toString() {
 
         if (successfullyStarted) {
             return "WorkloadResult [successfullyExecutedTransactions=" + successfullyExecutedTransactions + ", totalAttemptedTransactions=" + totalAttemptedTransactions + "]";
         }
         else {
             return "WorkloadResult [exception=" + exception + "]";
         }
     }
 
     public IWorker getWorkloadID() {
 
         return worker;
     }
 
     public Throwable getException() {
 
         return exception;
     }
 
     public List<QueryLogEntry> getQueryLog() {
 
         return queryLog;
     }
 
     public String getLocationOfExecution() {
 
         return locationOfExecution;
     }
 
     public long getTotalAttemptedTransactions() {
 
         return totalAttemptedTransactions;
     }
 
     public long getTotalSuccessfulTransactions() {
 
         return successfullyExecutedTransactions;
     }
 
     public long getStartTime() {
 
         if (queryLog != null && queryLog.size() > 0) {
             final QueryLogEntry queryLogEntry = queryLog.get(0);
             if (queryLogEntry != null) {
                 return queryLogEntry.timeOfCommit - queryLogEntry.timeToExecute;
             }
             else {
                 return Long.MAX_VALUE;
             }
         }
         else {
             return 0;
         }
     }
 
     public long getEndTime() {
 
         if (queryLog != null && queryLog.size() > 0) {
             final QueryLogEntry queryLogEntry = queryLog.get(queryLog.size() - 1);
             if (queryLogEntry != null) {
                 return queryLogEntry.timeOfCommit - queryLogEntry.timeToExecute;
             }
             else {
                 return Long.MAX_VALUE;
             }
         }
         else {
             return 0;
         }
     }
 
     public IWorkload getWorkload() {
 
         return workload;
     }
 
     /**
      * This isn't serializable so it must be removed before sending the workload result over RMI.
      */
     public void removeWorkloadReference() {
 
         workload = null;
     }
 
     /**
      * Get the number of transactions that were successfully executed between the two given time points (starting at 0, where 0 is based on {@link #getStartTime()}).
      * @param currentTime
      * @param newTime
      * @return
      */
     public long getNumberOfSuccessfulTransactionsBetween(final long currentTime, final long newTime) {
 
         long transactionCount = 0;
 
         for (final QueryLogEntry entry : queryLog) {

             final long offsetTimeOfCommit = entry.timeOfCommit - getStartTime();
            if (offsetTimeOfCommit >= currentTime && offsetTimeOfCommit <= newTime && entry.queryTypes.size() > 0 && entry.successfulExecution) {
                 transactionCount += 1; //the following measures the number of queries: entry.queryTypes.size();
             }
         }
 
         return transactionCount;
     }
 
 }
