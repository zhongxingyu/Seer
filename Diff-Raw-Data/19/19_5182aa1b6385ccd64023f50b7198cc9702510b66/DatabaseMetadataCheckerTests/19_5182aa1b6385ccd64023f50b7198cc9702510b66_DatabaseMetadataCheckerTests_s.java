 package com.igorpopov.sscce;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 /**
  * @author Igor Popov
  */
 public abstract class DatabaseMetadataCheckerTests {
   
   Connection connection;
   Statement statement;
   
   protected DatabaseMetadataChecker metadataChecker;
   
   protected abstract String getConnectionUrl();
   
   protected abstract String getUsername();
   
   protected abstract String getPassword();
   
   @Before
   public void setup() throws Exception {
     metadataChecker = new DatabaseMetadataChecker(getConnectionUrl(), getUsername(), getPassword());
     initializeConnection();
     createTable();
   }
   
   @After
   public void teardown() throws Exception {
    statement.execute("DROP TABLE table1");
    statement.execute("DROP TABLE table2");
     statement.close();
     connection.close();
   }
   
   private void initializeConnection() throws SQLException {
     connection = DriverManager.getConnection(getConnectionUrl(), getUsername(), getPassword());
     statement = connection.createStatement();
   }
   
   private void createTable() throws SQLException {
    statement.execute("CREATE TABLE table1 (id INT PRIMARY KEY, col1 VARCHAR(64))");
    statement.execute("CREATE TABLE table2 (id INT PRIMARY KEY, col1 VARCHAR(64), col2 VARCHAR(64))");
   }
   
   @Test
   public void canListColumnsForTable() throws Exception {
    assertEquals(asList("id", "col1"), metadataChecker.listColumnsForTable("table1"));
    assertEquals(asList("id", "col1", "col2"), metadataChecker.listColumnsForTable("table2"));
   }
   
   @Test
   public void canCheckIfColumnsExistInTable() throws Exception {
    assertTrue(metadataChecker.columnsExist("table1", asList("col1")));
    assertFalse(metadataChecker.columnsExist("table1", asList("aColumnThatDoesNotExist")));
   }
 }
