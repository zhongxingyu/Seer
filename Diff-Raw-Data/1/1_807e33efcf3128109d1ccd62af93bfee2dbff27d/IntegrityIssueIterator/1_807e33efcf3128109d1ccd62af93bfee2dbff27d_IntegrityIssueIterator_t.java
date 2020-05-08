 package org.bitrepository.integrityservice.cache.database;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to handle iteration over large set of integrity issues, delivering only IDs 
  */
 public class IntegrityIssueIterator {
     
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     private ResultSet issueResultSet = null;
     private Connection conn = null;
     private final PreparedStatement ps;
     
     public IntegrityIssueIterator(PreparedStatement ps) {
         this.ps = ps;
     }
     
     /**
      * Method to explicitly close the ResultSet in the IntegrityIssueIterator 
      * @throws SQLException in case of a sql error
      */
     public void close() throws SQLException {
         if(issueResultSet != null) {
             issueResultSet.close();
         }
         
         if(ps != null) {
             ps.close();
         }
         
         if(conn != null) {
             conn.setAutoCommit(true);
             conn.close();
            conn = null;
         }
     }
     
     /**
      * Method to return the next AuditTrailEvent in the ResultSet
      * When no more AuditTrailEvents are available, null is returned and the internal ResultSet closed. 
      * @return The next AuditTrailEvent available in the ResultSet, or null if no more events are available. 
      * @throws SQLException In case of a sql error. 
      */
     public String getNextIntegrityIssue() {
         try {
             String issue = null;
             if(issueResultSet == null) {
                 conn = ps.getConnection();
                 conn.setAutoCommit(false);
                 ps.setFetchSize(100);
                 long tStart = System.currentTimeMillis();
                 log.debug("Executing query to get issues resultset");
                 issueResultSet = ps.executeQuery();
                 log.debug("Finished executing issues query, it took: " + (System.currentTimeMillis() - tStart) + "ms");
             }
             if(issueResultSet.next()) {
                 
                 issue = issueResultSet.getString(1);
             } else {
                 close();
             }
     
             return issue;
         } catch (Exception e) {
             try {
                 close();
             } catch (SQLException e1) {
                 throw new RuntimeException("Failed to close ResultSet or PreparedStatement", e1);
             }
             throw new IllegalStateException("Could not extract the wanted integrity issues", e);
         } 
     }
 }
