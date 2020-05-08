 package org.h2o.eval.script.workload;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.h2o.eval.interfaces.IWorker;
 import org.h2o.eval.interfaces.IWorkload;
 import org.h2o.util.exceptions.WorkloadParseException;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 
 /**
  * Parses workloads and executes them against given database connections.
  *
  * @author Angus Macdonald (angus.macdonald@st-andrews.ac.uk)
  */
 public class WorkloadExecutor {
 
     private static final int MAX_CHAR_ARRAY_BYTES = 50;
     private static final int MAX_BIG_INTEGER_BYTES = 20;
     public static final String GENERATED_LONG_PLACEHOLDER = "<generated-long/>";
     public static final String GENERATED_STRING_PLACEHOLDER = "<generated-string/>";
     public static final String COMMENT = "#";
     public static final String SLEEP_OPEN_TAG = "<sleep>";
     public static final String SLEEP_CLOSE_TAG = "</sleep>";
 
     private static final String LOOP_START_OPEN_TAG = "<loop";
     public static final String LOOP_COUNTER_PLACEHOLDER = "<loop-counter/>";
     public static final String LAST_LOOP_COUNTER_PLACEHOLDER = "<last-loop-counter/>";
     public static final String INCREMENT_COUNTER_TAG = "<increment/>";
     public static final String LOOP_END_TAG = "</loop>";
     private static final long MAX_WORKLOAD_DURATION = 50000;
 
     /**
      * If the system is failing quickly (within 5 milliseconds), sleep for a while so you don't get lots of junk failed transactions. This determines how long to sleep for.
      */
     private static final int FAIL_QUICK_AVOIDANCE_SLEEP_TIME = 500;
 
     private final static SecureRandom random = new SecureRandom();
 
     private boolean stalled = false;
 
     long workloadEndTime = 0;
     long totalTimeOfStall = 0;
 
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
     public WorkloadResult execute(final Connection connection, final ArrayList<String> queries, final IWorker worker, long duration, final IWorkload workload) throws SQLException, WorkloadParseException {
 
         final long workloadStartTime = currentTime();
 
         boolean singleRun = false; // if set to true the workload completes after a single execution, and doesn't start again.
 
         if (duration <= 0) { //if no duration is specified, 'timeout' after a specified period.
             duration = MAX_WORKLOAD_DURATION;
             singleRun = true;
         }
 
         workloadEndTime = workloadStartTime + duration;
 
         final Statement stat = connection.createStatement();
 
         long successfullyExecutedTransactions = 0;
         long attemptedTransactions = 0;
 
         long uniqueCounter = 0; //incremented alongside loopCounter, but not reset when workload restarts.
 
         final List<QueryLogEntry> queryLog = new LinkedList<QueryLogEntry>();
 
         boolean startOfExecution = true;
 
         while (currentTime() < currentWorkloadEndTime()) { // Will repeat the same workload multiple times if its end is reached before the scheduled time.
 
             try {
                 stat.execute("SET AUTOCOMMIT ON;");
             }
             catch (final Exception e2) {
                 ErrorHandling.exceptionErrorNoEvent(e2, "Failed to turn on auto-commit."); //can occur if there is no active system table (which some tests require).
             }
 
             long timeBeforeQueryExecution = 0; //when a particular transaction started.
 
             if (startOfExecution) {
                 timeBeforeQueryExecution = currentTime();
                 // startOfExecution = false;
             }
 
             final List<String> queriesInThisTransaction = new LinkedList<String>();
 
             int loopCounter = -1; //the current iteration of the loop in this workload [nested loops are not supported].
             int loopStartPos = -1; //where the loop starts in this list of queries.
             int loopIterations = 1; //how many iterations of the loop are to be executed.
 
             boolean autoCommitEnabled = true;
 
             queryLoop: for (int i = 0; i < queries.size(); i++) {
 
                 if (isStalled()) {
 
                     final long stallStartTime = currentTime();
 
                     while (isStalled()) {
                         try {
                             Thread.sleep(50);
                         }
                         catch (final InterruptedException e) {
                         }
                     }
 
                     totalTimeOfStall += currentTime() - stallStartTime;
 
                 }
 
                 String query = queries.get(i);
 
                 if (query.startsWith(COMMENT) || query.equals("")) {
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
                         loopIterations = Integer.valueOf(query.substring(query.indexOf("iterations=\"") + "iterations=\"".length(), query.lastIndexOf("\"/>")));
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
                         timeBeforeQueryExecution = currentTime();
                     }
                     else if (query.startsWith("SET AUTCOMMIT ON;")) {
                         autoCommitEnabled = true;
                         timeBeforeQueryExecution = currentTime();
                     }
 
                     boolean successfullyExecuted = true;
 
                     query = replacePlaceholderValues(uniqueCounter, query);
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Executing query: " + query);
 
                     try {
                         final boolean resultSet = stat.execute(query);
 
                         if (resultSet) {
                             //Not currently checked.
                         }
                         else {
                             if (!query.contains("COMMIT") && stat.getUpdateCount() < 0) { throw new SQLException("Update count was lower than expected."); }
                         }
 
                     }
                     catch (final SQLException e) {
                         e.printStackTrace();
                         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Failed to execute '" + query + "'. Error: " + e.getMessage());
                         successfullyExecuted = false;
 
                         if (autoCommitEnabled) {
                             /*
                              * Rollback the entire transaction if an operation some way through it failed. 
                              */
                             try {
                                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Executing rollback because transaction has failed.");
                                stat.execute("ROLLBACK;");
                                 queriesInThisTransaction.clear();
                             }
                             catch (final Exception e1) {
                                 //May throw an exception is there is nothing to roll back.
                             }
                         }
                     }
 
                     final long timeAfterQueryExecution = currentTime();
 
                     final long timeToExecute = timeAfterQueryExecution - timeBeforeQueryExecution;
                     if (autoCommitEnabled) {
 
                         if (!query.contains("SET AUTOCOMMIT")) {
                             queryLog.add(QueryLogEntry.createQueryLogEntry(currentTime(), query, successfullyExecuted, timeToExecute));
                         }
 
                         attemptedTransactions++;
 
                         if (successfullyExecuted) {
                             successfullyExecutedTransactions++;
                         }
                         else if (!successfullyExecuted && timeToExecute < 5) {
                             try {
                                 Thread.sleep(500);
                             }
                             catch (final InterruptedException e) {
                             }
                         }
 
                         timeBeforeQueryExecution = currentTime();
                     }
                     else if (!autoCommitEnabled && query.contains("COMMIT;") && !query.contains("SET AUTOCOMMIT") || !successfullyExecuted) {
 
                         attemptedTransactions++;
 
                         if (successfullyExecuted) {
                             successfullyExecutedTransactions++;
                         }
                         else if (!successfullyExecuted && timeToExecute < 5) {
                             try {
                                 //If the system is failing quickly, sleep for a while so you don't get lots of junk failed transactions.
                                 Thread.sleep(FAIL_QUICK_AVOIDANCE_SLEEP_TIME);
                             }
                             catch (final InterruptedException e) {
                             }
                         }
 
                         queryLog.add(QueryLogEntry.createQueryLogEntry(currentTime(), queriesInThisTransaction, successfullyExecuted, timeToExecute));
                         queriesInThisTransaction.clear();
                         timeBeforeQueryExecution = currentTime(); // when auto-commit isn't enabled, the transaction starts after the previous one finishes.
                     }
                     else if (!query.contains("SET AUTOCOMMIT")) {
                         queriesInThisTransaction.add(query);
                     }
 
                     if (!successfullyExecuted) {
                         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Re-starting workload after unsuccessful execution.");
                         startOfExecution = true;
                         break queryLoop; //restart the workload.
                     }
 
                     if (currentTime() > currentWorkloadEndTime()) {
                         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Workload complete.");
                         try {
                             stat.execute("ROLLBACK;");
                             queriesInThisTransaction.clear();
                         }
                         catch (final Exception e) {
                             //May throw an exception is there is nothing to roll back.
                         }
                         break; // End the workload here.
                     }
 
                 }
             }
 
             if (singleRun) {
                 break;
             }
         }
 
         final WorkloadResult result = new WorkloadResult(queryLog, successfullyExecutedTransactions, attemptedTransactions, worker, workload);
         return result;
     }
 
     /**
      * The time in this workloads execution. This is the actual time - the time of any stall operations which have taken place.
      * 
      * <p>This ensures results show the stall operation as having been over in an instant.
      * @return
      */
     public long currentTime() {
 
         return System.currentTimeMillis() - totalTimeOfStall;
     }
 
     public long currentWorkloadEndTime() {
 
         return workloadEndTime;
     }
 
     public static String replacePlaceholderValues(final long uniqueCounter, String query) {
 
         query = query.replaceAll(LOOP_COUNTER_PLACEHOLDER, uniqueCounter + "");
 
         query = query.replaceAll(LAST_LOOP_COUNTER_PLACEHOLDER, uniqueCounter + "");
 
         while (query.contains(GENERATED_STRING_PLACEHOLDER)) {
             query = query.replaceFirst(GENERATED_STRING_PLACEHOLDER, generateRandom40CharString());
         }
 
         while (query.contains(GENERATED_LONG_PLACEHOLDER)) {
             query = query.replaceFirst(GENERATED_LONG_PLACEHOLDER, generateBigIntegerValue());
         }
         return query;
     }
 
     public static String generateBigIntegerValue() {
 
         return new BigInteger(MAX_BIG_INTEGER_BYTES, random) + "";
     }
 
     public static String generateRandom40CharString() {
 
         return "'" + new BigInteger(MAX_CHAR_ARRAY_BYTES, random).toString(32) + "'";
     }
 
     /**
      * Stall the execution of a workload.
      */
     public synchronized void stall() {
 
         stalled = true;
     }
 
     /**
      * Resume the execution of a stalled workload.
      */
     public synchronized void resume() {
 
         stalled = false;
 
     }
 
     public synchronized boolean isStalled() {
 
         return stalled;
     }
 
 }
