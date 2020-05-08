 /*
  * Copyright (C) 2009-2010 School of Computer Science, University of St Andrews. All rights reserved. Project Homepage:
  * http://blogs.cs.st-andrews.ac.uk/h2o H2O is free software: you can redistribute it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. H2O
  * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
  * Public License along with H2O. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.h2o.test.fixture;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collection;
 
 import org.h2.engine.Constants;
 import org.h2.engine.Database;
 import org.h2.engine.Engine;
 import org.h2.tools.DeleteDbFiles;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.manager.PersistentSystemTable;
 import org.h2o.locator.server.LocatorServer;
 import org.h2o.run.AllTests;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 /**
  * Base class for JUnit tests. Performs a basic setup of two in-memory databases which are used for the rest of testing.
  * 
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class TestBase {
 
     public static final long SHUTDOWN_CHECK_DELAY = 1000;
 
     protected Connection ca = null;
     protected Connection cb = null;
     protected Statement sa = null;
     protected Statement sb = null;
     protected LocatorServer ls;
 
     private static int chordPort = 40000;
 
     /**
      * The number of rows that are in the test table after the initial @see {@link #setUp()} call.
      */
     protected static final int ROWS_IN_DATABASE = 2;
 
     @BeforeClass
     public static void initialSetUp() throws IOException {
 
         Constants.IS_TEAR_DOWN = false;
         Constants.IS_NON_SM_TEST = true;
 
         Diagnostic.setLevel(DiagnosticLevel.INIT);
         Diagnostic.addIgnoredPackage("uk.ac.standrews.cs.stachord");
 
         createProperties("jdbc:h2:mem:two", true);
         createProperties("jdbc:h2:mem:three", true);
     }
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
 
         Constants.IS_TEAR_DOWN = false;
         deleteDatabaseData("db_data/unittests/", "schema_test");
         setUpDescriptorFiles();
         ls = new LocatorServer(29999, "junitLocator");
         ls.createNewLocatorFile();
         ls.start();
 
         org.h2.Driver.load();
 
         ca = DriverManager.getConnection("jdbc:h2:sm:mem:one", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
         cb = DriverManager.getConnection("jdbc:h2:mem:two", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
 
         sa = ca.createStatement();
         sb = cb.createStatement();
 
         String sql = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255));";
         sql += "INSERT INTO TEST VALUES(1, 'Hello');";
         sql += "INSERT INTO TEST VALUES(2, 'World');";
 
         sa.execute(sql);
     }
 
     /**
      * @throws SQLException
      * @throws InterruptedException 
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws SQLException, InterruptedException {
 
         if (sa != null) {
             sa.execute("DROP ALL OBJECTS");
 
             if (!sa.isClosed()) {
                 sa.close();
             }
 
             if (!ca.isClosed()) {
                 ca.close();
             }
         }
 
         if (sb != null) {
             sb.execute("DROP ALL OBJECTS");
 
             if (!sb.isClosed()) {
                 sb.close();
             }
 
             if (!cb.isClosed()) {
                 cb.close();
             }
 
         }
 
         closeDatabaseCompletely();
 
         ca = null;
         cb = null;
         sa = null;
         sb = null;
 
         ls.setRunning(false);
         while (!ls.isFinished()) {
             Thread.sleep(SHUTDOWN_CHECK_DELAY);
         };
     }
 
     public static void setUpDescriptorFiles() throws IOException {
 
         createProperties("jdbc:h2:mem:one", true);
         createProperties("jdbc:h2:mem:two", true);
         createProperties("jdbc:h2:mem:three", true);
        createProperties("jdbc:h2:three", true);
         createProperties("jdbc:h2:sm:tcp://localhost:9081/db_data/unittests/schema_test", true);
     }
 
     /**
      * This is done because the server doesn't release the original port when it is stopped programmatically.
      * @throws IOException 
      */
     public static void resetLocatorFile() throws IOException {
 
         createProperties("jdbc:h2:db_data/test/scriptSimple", false);
         createProperties("jdbc:h2:sm:tcp://localhost:9081/db_data/unittests/schema_test", false);
     }
 
     /**
     * Close the database explicitly, in case it didn't shut down correctly between tests.
     */
     public static void closeDatabaseCompletely() {
 
         final Collection<Database> dbs = Engine.getInstance().getAllDatabases();
 
         for (final Database db : dbs) {
             db.close(false);
             db.shutdownImmediately();
         }
     }
 
     /**
      * Utility method which checks that the results of a test query match up to the set of expected values. The 'TEST' class is being used
      * in these tests so the primary keys (int) and names (varchar/string) are required to check the validity of the resultset.
      * 
      * @param key
      *            The set of expected primary keys.
      * @param secondCol
      *            The set of expected names.
      * @param rs
      *            The results actually returned.
      * @throws SQLException
      */
     protected void validateResults(final int[] pKey, final String[] secondCol, final ResultSet rs) throws SQLException {
 
         assertNotNull("Resultset was null. Probably an incorrectly set test.", rs);
 
         for (int i = 0; i < pKey.length; i++) {
             if (pKey[i] != 0 && secondCol[i] != null) { // indicates the entry was deleted as part of the test.
 
                 assertTrue(rs.next());
                 assertEquals(pKey[i], rs.getInt(1));
                 assertEquals(secondCol[i], rs.getString(2));
             }
         }
 
         assertFalse("Too many entries: " + rs.getInt(1) + ": " + rs.getString(2), rs.next());
 
         rs.close();
     }
 
     protected void createReplicaOnB() throws SQLException {
 
         createReplicaOnB("TEST");
     }
 
     /**
      * Validate the result of a query on the first replica against expected values by selecting everything in a table sorted by ID and
      * comparing with each entry.
      */
     protected void validateOnFirstMachine(final TestQuery testQuery) throws SQLException {
 
         validateOnFirstMachine(testQuery.getTableName(), testQuery.getPrimaryKey(), testQuery.getSecondColumn());
     }
 
     /**
      * Validate the result of a query on the second replica against expected values by selecting everything in a table sorted by ID and
      * comparing with each entry
      */
     protected void validateOnSecondMachine(final TestQuery testQuery) throws SQLException {
 
         validateOnSecondMachine(testQuery.getTableName(), testQuery.getPrimaryKey(), testQuery.getSecondColumn());
     }
 
     /**
      * Validate the result of a query on the second replica against expected values by selecting everything in a table sorted by ID and
      * comparing with each entry.
      * 
      * @param pKey
      *            Primary key value
      * @param secondCol
      *            Second column value in test table.
      * @throws SQLException
      */
     protected void validateOnSecondMachine(final String tableName, final int[] pKey, final String[] secondCol) throws SQLException {
 
         sb.execute("SELECT LOCAL ONLY * FROM " + tableName + " ORDER BY ID;");
         validateResults(pKey, secondCol, sb.getResultSet());
     }
 
     /**
      * Validate the result of a query on the first replica against expected values by selecting everything in a table sorted by ID and
      * comparing with each entry.
      * 
      * @param pKey
      *            Primary key value
      * @param secondCol
      *            Second column value in test table.
      * @throws SQLException
      */
     protected void validateOnFirstMachine(final String tableName, final int[] pKey, final String[] secondCol) throws SQLException {
 
         sa.execute("SELECT LOCAL ONLY * FROM " + tableName + " ORDER BY ID;");
         validateResults(pKey, secondCol, sa.getResultSet());
     }
 
     /**
      * @param stat
      * @param tableName
      * @throws SQLException
      */
     protected void createSecondTable(final Statement stat, final String tableName) throws SQLException {
 
         String sqlQuery = "CREATE TABLE " + tableName + "(ID INT PRIMARY KEY, NAME VARCHAR(255));";
         sqlQuery += "INSERT INTO " + tableName + " VALUES(4, 'Meh');";
         sqlQuery += "INSERT INTO " + tableName + " VALUES(5, 'Heh');";
 
         stat.execute(sqlQuery);
     }
 
     private static void createProperties(final String url, final boolean include_chord_port) throws IOException {
 
         final H2OPropertiesWrapper properties = H2OPropertiesWrapper.getWrapper(DatabaseID.parseURL(url));
 
         properties.createNewFile();
 
         properties.setProperty("descriptor", AllTests.TEST_DESCRIPTOR_FILE);
         properties.setProperty("databaseName", "testDB");
 
         if (include_chord_port) {
             properties.setProperty("chordPort", "" + chordPort++);
         }
 
         properties.saveAndClose();
     }
 
     /**
      * Delete all of the database files created in these tests
      * @throws SQLException 
      */
     private static void deleteDatabaseData(final String baseDir, final String db) throws SQLException {
 
         DeleteDbFiles.execute(baseDir, db, true);
     }
 
     /**
      * Create a replica on the second test database.
      * 
      * @throws SQLException
      */
     private void createReplicaOnB(final String tableName) throws SQLException {
 
         /*
          * Create replica on B.
          */
         sb.execute("CREATE REPLICA " + tableName + ";");
 
         assertEquals(0, sb.getUpdateCount());
     }
 }
