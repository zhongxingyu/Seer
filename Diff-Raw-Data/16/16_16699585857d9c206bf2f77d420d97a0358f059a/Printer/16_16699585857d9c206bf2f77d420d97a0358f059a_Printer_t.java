 package org.h2o.eval.printing;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.h2o.eval.script.workload.QueryLogEntry;
 import org.h2o.eval.script.workload.WorkloadResult;
 
 public class Printer {
 
     protected static final DateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm:ss");
 
     /**
      * Get the time when the first benchmark started.
      * @param workloadResults
      * @return unix time.
      */
     protected static long getStartTime(final List<WorkloadResult> workloadResults) {
 
         long min = Long.MAX_VALUE;
 
         for (final WorkloadResult workloadResult : workloadResults) {
 
             min = Math.min(min, workloadResult.getStartTime());
 
         }
         return min;
     }
 
     /**
      * Get the time when the last benchmark started.
      * @param workloadResults
      * @return unix time.
      */
     protected static long getEndTime(final List<WorkloadResult> workloadResults) {
 
         long min = Long.MIN_VALUE;
 
         for (final WorkloadResult workloadResult : workloadResults) {
 
             min = Math.max(min, workloadResult.getStartTime());
 
         }
         return min;
     }
 
     protected static long getNumberOfSuccessfulTransactions(final List<WorkloadResult> workloadResults) {
 
         long successful = 0;
 
         for (final WorkloadResult workloadResult : workloadResults) {
             successful += workloadResult.getTotalSuccessfulTransactions();
         }
 
         return successful;
     }
 
     protected static long getNumberOfAttemptedTransactions(final List<WorkloadResult> workloadResults) {
 
         long attempted = 0;
 
         for (final WorkloadResult workloadResult : workloadResults) {
             attempted += workloadResult.getTotalAttemptedTransactions();
         }
 
         return attempted;
     }
 
     /**
      * 
      * @param workloadResults
      * @param successful if true this counts the time to taken to successfully execute transactions; otherwise it counts unsuccessfully executed transaction time.
     * @param startTime 
      * @return
      */
    protected static long getTotalTimeOfTransactions(final List<WorkloadResult> workloadResults, final boolean successful, final long startTime) {
 
         long count = 0;
 
         for (final WorkloadResult workloadResult : workloadResults) {
             final List<QueryLogEntry> queryLog = workloadResult.getQueryLog();
 
             for (final QueryLogEntry queryLogEntry : queryLog) {
                 if (successful == queryLogEntry.successfulExecution) {
                    count += queryLogEntry.timeOfExecution - startTime;
                 }
             }
         }
 
         return count;
     }
 
     protected static Set<String> getAllTableNames(final List<WorkloadResult> workloadResults) {
 
         final Set<String> allNames = new HashSet<String>();
 
         for (final WorkloadResult workloadResult : workloadResults) {
             for (final QueryLogEntry entry : workloadResult.getQueryLog()) {
                 allNames.addAll(entry.tablesInvolved);
             }
         }
 
         return allNames;
     }
 
     @SuppressWarnings("unused")
     protected static StringBuilder printBasicWorkloadDetails(final WorkloadResult workloadResult) {
 
         final StringBuilder csv = new StringBuilder();
 
         csv.append("Evaluation results:\n");
         csv.append("Total attempted transactions:, " + workloadResult.getTotalAttemptedTransactions() + "\n");
         csv.append("Total successful transactions:, " + workloadResult.getTotalSuccessfulTransactions() + "\n\n");
 
         return csv;
     }
 }
