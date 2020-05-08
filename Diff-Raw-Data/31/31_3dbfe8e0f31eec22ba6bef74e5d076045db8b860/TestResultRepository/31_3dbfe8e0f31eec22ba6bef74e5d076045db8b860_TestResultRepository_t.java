 package com.kwc.testen.db;
 
 import com.kwc.testen.model.TestResult;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Marius Kristensen
  */
 public class TestResultRepository {
 
     private final DatabaseHandler databaseHandler;
 
     public TestResultRepository() {
         databaseHandler = new DatabaseHandler();
     }
 
     public List<TestResult> getAllTestResults() {
         Connection connection = databaseHandler.getConnection();
         ArrayList<TestResult> testResults = new ArrayList<>();
         try {
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM TESTEN");
            while (rs.next()) {
                 TestResult testResult = new TestResult(
                         rs.getInt(1),
                         rs.getString(2),
                         rs.getString(3),
                         rs.getString(4),
                         rs.getString(5),
                         rs.getInt(6),
                         rs.getInt(7),
                         rs.getString(8),
                         rs.getInt(9),
                         rs.getInt(10),
                         rs.getString(11),
                         rs.getString(12),
                         rs.getString(13)
                 );
                 testResults.add(testResult);
             }
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return testResults;
     }
 
     public Integer countRows() {
         Connection connection = null;
         connection = databaseHandler.getConnection();
         Statement statement = null;
         try {
             statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM TESTEN");
             if (resultSet.next()) {
                 return resultSet.getInt(1);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
 }
