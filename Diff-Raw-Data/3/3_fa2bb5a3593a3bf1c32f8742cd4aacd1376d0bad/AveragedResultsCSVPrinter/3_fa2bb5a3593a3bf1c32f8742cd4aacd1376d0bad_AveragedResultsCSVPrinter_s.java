 package org.h2o.eval.printing;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.h2o.eval.script.workload.WorkloadResult;
 
 import uk.ac.standrews.cs.nds.util.FileUtil;
 
 /**
  * Utility class to print a summary of results from a co-ordinator script execution to a common CSV file.
  *
  * @author Angus Macdonald (angus.macdonald@st-andrews.ac.uk)
  */
 public class AveragedResultsCSVPrinter extends Printer {
 
     /**
      * 
      * @param fileLocation The file results are to be written to.
      * @param workloadResults The results to be written.
      * @throws IOException 
      */
     public static void printResults(final String fileLocation, final List<WorkloadResult> workloadResults, final String scriptName, final int totalNumOfMachines, final int replFactor) throws IOException {
 
         final long startTime = getStartTime(workloadResults);
 
         final long workloadLength = getEndTime(workloadResults) - startTime;
         final long numOfTransactionsExecuted = getNumberOfAttemptedTransactions(workloadResults);
         final long numOfTransactionsSuccessful = getNumberOfSuccessfulTransactions(workloadResults);
 
         final long totalTimeOfSuccessfulTranscations = getTotalTimeOfTransactions(workloadResults, true);
         final long totalTimeOfUnSuccessfulTransactions = getTotalTimeOfTransactions(workloadResults, false);
 
         if (!new File(fileLocation).exists()) {
             FileUtil.writeToFile(fileLocation, printHeader(), true);
         }
 
         FileUtil.writeToFile(fileLocation, printRow(startTime, scriptName, workloadLength, numOfTransactionsExecuted, numOfTransactionsSuccessful, totalTimeOfSuccessfulTranscations, totalTimeOfUnSuccessfulTransactions), false);
     }
 
     private static String printHeader() {
 
         StringBuilder row = new StringBuilder("Start Time");
 
         row = appendToRow(row, "Script Name");
         row = appendToRow(row, "Workload Length");
         row = appendToRow(row, "Executed Transactions");
         row = appendToRow(row, "Successful Transactions");
         row = appendToRow(row, "Time of Successful Transactions");
         row = appendToRow(row, "Time of Unsuccessful Transactions");
         row = endRow(row);
         return row.toString();
     }
 
     private static String printRow(final long startTime, final String scriptName, final Long workloadLength, final Long numOfTransactionsExecuted, final Long numOfTransactionsSuccessful, final Long totalTimeOfSuccessfulTranscations, final Long totalTimeOfUnSuccessfulTransactions) {
 
         StringBuilder row = new StringBuilder(startTime + "");
 
        row = appendToRow(row, workloadLength);
         row = appendToRow(row, scriptName);
         row = appendToRow(row, numOfTransactionsExecuted);
         row = appendToRow(row, numOfTransactionsSuccessful);
         row = appendToRow(row, totalTimeOfSuccessfulTranscations);
         row = appendToRow(row, totalTimeOfUnSuccessfulTransactions);
         row = endRow(row);
         return row.toString();
     }
 
     private static StringBuilder appendToRow(final StringBuilder row, final Object obj) {
 
         return row.append("," + obj);
 
     }
 
     private static StringBuilder endRow(final StringBuilder row) {
 
         return row.append("\n");
     }
 
 }
