 package org.h2o.eval.script.workload;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.h2o.eval.interfaces.IWorker;
 import org.h2o.eval.script.workload.QueryLogEntry.QueryType;
 import org.h2o.util.exceptions.WorkloadParseException;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 /**
  * Parses workloads and executes them against given database connections.
  *
  * @author Angus Macdonald (angus.macdonald@st-andrews.ac.uk)
  */
 public class WorkloadExecutor {
 
     private static final String COMMENT = "#";
     private static final String SLEEP_OPEN_TAG = "<sleep>";
     private static final String SLEEP_CLOSE_TAG = "</sleep>";
 
     private static final String LOOP_START_OPEN_TAG = "<loop";
     private static final String LOOP_COUNTER_PLACEHOLDER = "<loop-counter/>";
     private static final String INCREMENT_COUNTER_TAG = "<increment/>";
     private static final String LOOP_END_TAG = "</loop>";
     private static final long MAX_WORKLOAD_DURATION = 60 * 60;
 
     /**
      * 
      * @param connection
      * @param queries
      * @param worker
      * @param duration How long this workload should execute for. '0' if it should just run to completion then terminate (up to a limit of 1 hour).
      * @return
      * @throws SQLException
      * @throws WorkloadParseException
      */
     public static WorkloadResult execute(final Connection connection, final ArrayList<String> queries, final IWorker worker, long duration) throws SQLException, WorkloadParseException {
 
         final long workloadStartTime = System.currentTimeMillis();
 
         if (duration <= 0) { //if no duration is specified, 'timeout' after a specified period.
             duration = MAX_WORKLOAD_DURATION;
         }
 
         final long workloadEndTime = workloadStartTime + duration;
 
         final Statement stat = connection.createStatement();
 
         long successfullyExecutedTransactions = 0;
         long attemptedTransactions = 0;
 
         long uniqueCounter = 0; //incremented alongside loopCounter, but not reset when workload restarts.
 
         final List<QueryLogEntry> queryLog = new LinkedList<QueryLogEntry>();
 
         timeLoop: while (System.currentTimeMillis() < workloadEndTime) {
 
             stat.execute("SET AUTOCOMMIT ON;");
 
             int loopCounter = -1; //the current iteration of the loop in this workload [nested loops are not supported].
             int loopStartPos = -1; //where the loop starts in this list of queries.
             int loopIterations = 1; //how many iterations of the loop are to be executed.
 
             boolean autoCommitEnabled = true;
 
             for (int i = 0; i < queries.size(); i++) {
 
                 String query = queries.get(i);
 
                 if (query.startsWith(COMMENT)) {
                     continue; //it's a comment. Ignore.
                 }
                 else if (query.startsWith(SLEEP_OPEN_TAG)) { //Sleep for a specified number of seconds.
 
                     try {
                         final int sleepTime = Integer.valueOf(query.substring(SLEEP_OPEN_TAG.length(), query.indexOf(SLEEP_CLOSE_TAG)));
                         try {
                             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Sleeping for " + sleepTime + " seconds.");
                             Thread.sleep(sleepTime);
                         }
                         catch (final InterruptedException e) {
                         }
                     }
                     catch (final NumberFormatException e) {
                         throw new WorkloadParseException("Incorrectly formatted workload request: " + query);
                     }
                 }
                 else if (query.startsWith(LOOP_START_OPEN_TAG)) { //We have reached the start of a loop.
 
                     try {
                         loopIterations = Integer.valueOf(query.substring(query.indexOf("iterations=\"") + "iterations=\"".length(), query.lastIndexOf("\">")));
                     }
                     catch (final Exception e) {
                         throw new WorkloadParseException("Incorrectly formatted workload request: " + query + " (exception: " + e.getMessage() + ").");
                     }
 
                     if (loopIterations < 0) { throw new WorkloadParseException("The number of loop iterations must be positive."); }
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Starting loop for " + loopIterations + " iterations.");
                     loopCounter = 0;
                     loopStartPos = i;
                     continue;
                 }
                 else if (query.startsWith(LOOP_END_TAG)) { //We have reached the end of the loop.
 
                     if (loopCounter >= loopIterations) {
                         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Ending loop at " + loopCounter + ".");
                         continue; //break out of loop
                     }
                     else {
                         i = loopStartPos;
                         loopCounter++;
                         uniqueCounter++;
                         continue;
                     }
                 }
                 else if (query.startsWith(INCREMENT_COUNTER_TAG)) { //We have reached the end of the loop.
 
                     loopCounter++;
                     uniqueCounter++;
                     continue;
                 }
                 else { //It's an SQL query. Execute it.
 
                     if (query.startsWith("SET AUTOCOMMIT OFF;")) {
                         autoCommitEnabled = false;
                     }
                     else if (query.startsWith("SET AUTCOMMIT ON;")) {
                         autoCommitEnabled = true;
                     }
 
                     if (autoCommitEnabled || query.startsWith("COMMIT")) {
                         attemptedTransactions++;
                     }
 
                     query = query.replaceAll(LOOP_COUNTER_PLACEHOLDER, uniqueCounter + "");
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Executing query: " + query);
 
                     boolean successfullyExecuted = true;
                     final long timeBeforeQueryExecution = System.currentTimeMillis();
 
                     try {
                        final boolean resultSet = stat.execute(query);

                        if (resultSet) {

                        }
                        else {
                            if (!query.contains("COMMIT") && stat.getUpdateCount() < 1) { throw new SQLException("Update count was lower than expected."); }
                        }

                         if (autoCommitEnabled || query.startsWith("COMMIT")) {
                             successfullyExecutedTransactions++;
                         }
                     }
                     catch (final SQLException e) {
                         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Failed to execute '" + query + "'. Error: " + e.getMessage());
                         successfullyExecuted = false;
                     }
 
                     final long timeAfterQueryExecution = System.currentTimeMillis();
 
                     queryLog.add(getQueryLogEntry(query, successfullyExecuted, timeAfterQueryExecution - timeBeforeQueryExecution));
 
                     if (System.currentTimeMillis() > workloadEndTime) {
                         try {
                             stat.execute("ROLLBACK;");
                         }
                         catch (final Exception e) {
                             //May throw an exception is there is nothing to roll back.
                         }
                         break timeLoop; // End the workload here.
                     }
                 }
             }
 
         }
 
         final WorkloadResult result = new WorkloadResult(queryLog, successfullyExecutedTransactions, attemptedTransactions, worker);
         return result;
     }
 
     private static QueryLogEntry getQueryLogEntry(final String query, final boolean successfullyExecuted, final long timeToExecute) {
 
         QueryType queryType = QueryType.UNKNOWN;
         String tableInvolved = "Unknown";
 
         if (query.contains("INSERT INTO")) {
             queryType = QueryType.INSERT;
 
             tableInvolved = query.substring("INSERT INTO ".length(), query.indexOf(" VALUES ("));
         }
         else if (query.contains("DELETE FROM")) {
             queryType = QueryType.DELETE;
             tableInvolved = query.substring("DELETE FROM ".length(), query.indexOf(" WHERE"));
         }
         else if (query.contains("CREATE TABLE")) {
             queryType = QueryType.CREATE;
             tableInvolved = query.substring("CREATE TABLE ".length(), query.indexOf(" ("));
         }
         else if (query.contains("DROP TABLE")) {
             queryType = QueryType.DROP;
             tableInvolved = query.substring("DROP TABLE ".length(), query.indexOf(";"));
         }
 
         return new QueryLogEntry(successfullyExecuted, timeToExecute, queryType, tableInvolved);
 
     }
 }
