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
    statement.execute("DROP TABLE TABLE1");
    statement.execute("DROP TABLE TABLE2");
     statement.close();
     connection.close();
   }
   
   private void initializeConnection() throws SQLException {
     connection = DriverManager.getConnection(getConnectionUrl(), getUsername(), getPassword());
     statement = connection.createStatement();
   }
   
   private void createTable() throws SQLException {
    statement.execute("CREATE TABLE TABLE1 (ID INT PRIMARY KEY, COL1 VARCHAR(64))");
    statement.execute("CREATE TABLE TABLE2 (ID INT PRIMARY KEY, COL1 VARCHAR(64), COL2 VARCHAR(64))");
   }
   
   @Test
   public void canListColumnsForTable() throws Exception {
    assertEquals(asList("ID", "COL1"), metadataChecker.listColumnsForTable("TABLE1"));
    assertEquals(asList("ID", "COL1", "COL2"), metadataChecker.listColumnsForTable("TABLE2"));
   }
   
   @Test
   public void canCheckIfColumnsExistInTable() throws Exception {
    assertTrue(metadataChecker.columnsExist("TABLE1", asList("COL1")));
    assertFalse(metadataChecker.columnsExist("TABLE2", asList("ACOLUMNTHATDOESNOTEXIST")));
   }
 }
