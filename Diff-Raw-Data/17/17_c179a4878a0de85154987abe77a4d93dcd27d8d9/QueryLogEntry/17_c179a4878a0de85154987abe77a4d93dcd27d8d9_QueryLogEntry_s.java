 package org.h2o.eval.script.workload;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public class QueryLogEntry implements Serializable {
 
     private static final long serialVersionUID = 6421309505616188993L;
 
     public enum QueryType {
         CREATE, DROP, INSERT, DELETE, UNKNOWN, SELECT
     };
 
     /**
      * Type of query being performed.
      */
     public List<QueryType> queryTypes;
 
     /**
      * Name of the table on which this operation was performed.
      */
     public final Set<String> tablesInvolved;
 
     /**
      * Time taken to execute the query (either successfully, or for it to fail).
      */
     public long timeToExecute;
 
     public long timeOfLogEntry;
 
     /**
      * Whether the query executed successfully.
      */
     public boolean successfulExecution;
 
     /**
      * @param successfulExecution
      * @param timeToExecute
      * @param queryTypes
      * @param tablesInvolved
      * @param timeOfExecution 
      */
     public QueryLogEntry(final boolean successfulExecution, final long timeToExecute, final List<QueryType> queryTypes, final Set<String> tablesInvolved) {
 
         timeOfLogEntry = System.currentTimeMillis();
         this.successfulExecution = successfulExecution;
         this.timeToExecute = timeToExecute;
         this.queryTypes = queryTypes;
         this.tablesInvolved = tablesInvolved;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
 
         return "QueryLogEntry [queryType=" + PrettyPrinter.toString(queryTypes) + ", tableInvolved=" + PrettyPrinter.toString(tablesInvolved) + ", timeToExecute=" + timeToExecute + ", timeOfLogEntry=" + timeOfLogEntry + ", successfulExecution=" + successfulExecution + "]";
     }
 
     public static QueryLogEntry createQueryLogEntry(final String query, final boolean successfullyExecuted, final long timeToExecute) {
 
         final List<String> queries = new LinkedList<String>();
         queries.add(query);
 
         return createQueryLogEntry(queries, successfullyExecuted, timeToExecute);
     }
 
     public static QueryLogEntry createQueryLogEntry(final List<String> queriesInThisTransaction, final boolean successfullyExecuted, final long timeToExecute) {
 
         final Set<String> tablesInvolved = new HashSet<String>();
         final List<QueryType> queryTypes = new LinkedList<QueryLogEntry.QueryType>();
         for (final String query : queriesInThisTransaction) {
 
             QueryType queryType = QueryType.UNKNOWN;
             String tableInvolved = null;
 
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
 
             else if (query.contains("SELECT * FROM ")) {
                 queryType = QueryType.SELECT;
                 tableInvolved = query.substring("SELECT * FROM ".length(), query.indexOf(";"));
             }
 
             if (tableInvolved != null) {
                 tablesInvolved.add(tableInvolved);
             }
             queryTypes.add(queryType);
         }
 
         return new QueryLogEntry(successfullyExecuted, timeToExecute, queryTypes, tablesInvolved);
 
     }
 
     public String toCSV(final DateFormat dateformatter, final String locationOfExecution, final Collection<String> tableNames) {
 
         final String tablesInvolvedString = PrettyPrinter.toString(tablesInvolved, ";", false);
        String row = dateformatter.format(new Date(timeOfLogEntry)) + ", " + timeToExecute + ", " + locationOfExecution + ", " + tablesInvolvedString + ", " + getNumberOf(QueryType.INSERT) + ", " + getNumberOf(QueryType.SELECT) + ", ";
 
         for (final String tableName : tableNames) {
             if (tableName.equals(tablesInvolvedString)) {
                 row += timeToExecute;
             }
             else {
                 row += "=NA()";
             }
 
            row += ", ";
         }
 
         row += "\n";
 
         return row;
 
     }
 
     private int getNumberOf(final QueryType type) {
 
         int count = 0;
         for (final QueryType singleQueryType : queryTypes) {
             if (singleQueryType.equals(type)) {
                 count++;
             }
         }
         return count;
     }
 
     public static String toCSVHeader(final Collection<String> tableNames) {
 
         String header = "Time of Transaction, Time To Execute, Location of Execution, Tables Involved, Insert Queries, Select Queries, ";
 
         for (final String tableName : tableNames) {
            header += tableName + ", ";
         }
 
         header += "\n";
 
         return header;
     }
 }
