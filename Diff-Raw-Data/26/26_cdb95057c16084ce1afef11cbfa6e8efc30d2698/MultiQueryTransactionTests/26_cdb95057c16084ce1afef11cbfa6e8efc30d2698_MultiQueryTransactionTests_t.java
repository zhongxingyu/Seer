 /*
  * Copyright (C) 2009-2010 School of Computer Science, University of St Andrews. All rights reserved. Project Homepage:
  * http://blogs.cs.st-andrews.ac.uk/h2o H2O is free software: you can redistribute it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. H2O
  * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
  * Public License along with H2O. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.h2o.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.h2.engine.Constants;
 import org.h2.tools.DeleteDbFiles;
 import org.h2.tools.Server;
 import org.h2o.db.manager.PersistentSystemTable;
 import org.h2o.locator.server.LocatorServer;
 import org.junit.Test;
 
 /**
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class MultiQueryTransactionTests extends TestBase {
 
     /**
      * The number of times a command should be repeated.
      */
     private static final int TOTAL_ITERATIONS = 100;
 
     /**
      * Attempts to insert a number of queries into the database in the same transaction. Update on a single replica.
      */
     @Test
     public void basicMultiQueryInsert() {
 
         try {
 
             final TestQuery queryToExecute = createInsertsForTestTable();
 
             System.out.println("About to do the big set of inserts:");
             sa.execute(queryToExecute.getSQL());
 
             validateOnFirstMachine(queryToExecute);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Attempts to insert a number of queries into the database in the same transaction. Same as @see {@link #basicMultiQueryInsert()} but
      * this executes the query remotely with a linked table connection.
      */
     @Test
     public void basicMultiQueryInsertRemote() {
 
         try {
 
             final TestQuery queryToExecute = createInsertsForTestTable();
 
             System.out.println("About to do the big set of inserts:");
             sb.execute(queryToExecute.getSQL());
 
             validateOnFirstMachine(queryToExecute);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Attempts to insert a number of queries into the database in the same transaction, then deletes a large number of these queries
      * (again, part of the same transaction), and checks the result.
      */
     @Test
     public void basicMultiQueryDelete() {
 
         try {
 
             final TestQuery queryToExecute = createInsertsForTestTable();
 
             final int[] pKey = queryToExecute.getPrimaryKey();
             final String[] secondCol = queryToExecute.getSecondColumn();
 
             final StringBuilder sqlToExecute = new StringBuilder(queryToExecute.getSQL());
             /*
              * Delete some of these entries...
              */
             final int toDelete = TOTAL_ITERATIONS / 2;
 
             for (int i = toDelete / 2; i < toDelete; i++) {
                 sqlToExecute.append("DELETE FROM TEST WHERE ID = " + i + ";");
                 pKey[i - 1] = 0;
                 secondCol[i - 1] = null;
             }
 
             sb.execute(sqlToExecute.toString());
 
             validateOnFirstMachine(queryToExecute.getTableName(), pKey, secondCol);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Attempts to insert a number of queries into the database in the same transaction, this time in the case where there are multiple
      * replicas (meaning the updates must be propagated).
      */
     @Test
     public void multiQueryPropagatedInserts() {
 
         try {
 
             createReplicaOnB();
 
             /*
              * Create then execute INSERTS for TEST table.
              */
             final TestQuery queryToExecute = createInsertsForTestTable();
 
             sa.execute(queryToExecute.getSQL()); // Insert test rows.
 
             validateOnFirstMachine(queryToExecute);
             validateOnSecondMachine(queryToExecute);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Tests that a local multi-query transaction will rollback if there is a failure before a commit.
      */
     @Test
     public void testFailureLocal() {
 
         try {
             Constants.IS_TESTING_QUERY_FAILURE = true;
 
             /*
              * Create then execute INSERTS for TEST table.
              */
 
             final TestQuery queryToExecute = createInsertsForTestTable();
 
             try {
                 sa.execute(queryToExecute.getSQL()); // Insert test rows.
 
                 fail("This should have thrown an exception");
             }
             catch (final SQLException e) {
                 // Expected.
             }
 
             // Re-set row contents (nothing should have been inserted by this transaction.
 
             final int[] pKey = new int[ROWS_IN_DATABASE];
             final String[] secondCol = new String[ROWS_IN_DATABASE];
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "Hello";
             secondCol[1] = "World";
 
             validateOnFirstMachine(queryToExecute.getTableName(), pKey, secondCol);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Tests that a multi-query transaction involving more than one database instance will rollback if there is a failure before a commit.
      */
     @Test
     public void testFailureRemote() {
 
         try {
             Constants.IS_TESTING_QUERY_FAILURE = true;
 
             createReplicaOnB();
 
             /*
              * Create then execute INSERTS for TEST table.
              */
             final TestQuery queryToExecute = createInsertsForTestTable();
 
             try {
                 sa.execute(queryToExecute.getSQL()); // Insert test rows.
 
                 fail("This should have thrown an exception");
             }
             catch (final SQLException e) {
                 // Expected.
             }
 
             // Re-set row contents (nothing should have been inserted by this transaction.
             final int[] pKey = new int[ROWS_IN_DATABASE];
             final String[] secondCol = new String[ROWS_IN_DATABASE];
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "Hello";
             secondCol[1] = "World";
 
             validateOnFirstMachine(queryToExecute.getTableName(), pKey, secondCol);
 
             validateOnSecondMachine(queryToExecute.getTableName(), pKey, secondCol);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Tests that a multi-query transaction involving more than one table. The result should be a lot of successful inserts into each table.
      * Only involves one database instance.
      */
     @Test
     public void testMultiTableTransactionSuccessLocal() {
 
         try {
             // Constants.IS_TESTING_QUERY_FAILURE = true;
 
             // createReplicaOnB();
 
             createSecondTable(sa, "TEST2");
 
             /*
              * Create then execute INSERTS for TEST table.
              */
 
             final TestQuery testQuery = createInsertsForTestTable();
 
             final int[] pKey = new int[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
             final String[] secondCol = new String[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
 
             pKey[0] = 4;
             pKey[1] = 5;
             secondCol[0] = "Meh";
             secondCol[1] = "Heh";
 
             final TestQuery test2query = createMultipleInsertStatements("TEST2", pKey, secondCol, 6);
 
             sa.execute(testQuery.getSQL() + test2query.getSQL()); // Insert test rows.
 
             validateOnFirstMachine(testQuery);
 
             validateOnFirstMachine("TEST2", pKey, secondCol);
             // validateOnSecondReplica(queryToExecute);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Tests that a multi-query transaction involving more than one table works when involving multiple machines.
      */
     @Test
     public void testMultiTableTransactionSuccessRemote() {
 
         try {
             createSecondTable(sb, "TEST2");
 
             /*
              * Create then execute INSERTS for TEST table.
              */
 
             final TestQuery testQuery = createInsertsForTestTable();
 
             final int[] pKey = new int[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
             final String[] secondCol = new String[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
 
             pKey[0] = 4;
             pKey[1] = 5;
             secondCol[0] = "Meh";
             secondCol[1] = "Heh";
 
             final TestQuery test2query = createMultipleInsertStatements("TEST2", pKey, secondCol, 6);
 
             sa.execute(testQuery.getSQL() + test2query.getSQL()); // Insert test rows.
 
             validateOnFirstMachine(testQuery);
 
             validateOnSecondMachine("TEST2", pKey, secondCol);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Tests that a multi-query transaction involving:
      * <ul>
      * <li><Multiple tables</li>
      * <li>Multiple replicas for a single table</li>
      * </ul>
      * <p>
      * Where the outcome should be a successful query execution.
      */
     @Test
     public void multiTableAndReplicaSuccess() {
 
         try {
             // Constants.IS_TESTING_QUERY_FAILURE = true;
 
             createSecondTable(sb, "TEST2");
             createReplicaOnB();
 
             /*
              * Create then execute INSERTS for TEST table.
              */
 
             final TestQuery testQuery = createInsertsForTestTable();
 
             final int[] pKey = new int[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
             final String[] secondCol = new String[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
 
             pKey[0] = 4;
             pKey[1] = 5;
             secondCol[0] = "Meh";
             secondCol[1] = "Heh";
 
             final TestQuery test2query = createMultipleInsertStatements("TEST2", pKey, secondCol, 6);
 
             sa.execute(testQuery.getSQL() + test2query.getSQL()); // Insert test rows.
 
             validateOnFirstMachine(testQuery);
             validateOnSecondMachine(testQuery);
 
             validateOnSecondMachine("TEST2", pKey, secondCol);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Tests that a multi-query transaction involving:
      * <ul>
      * <li><Multiple tables</li>
      * <li>Multiple replicas for a single table</li>
      * </ul>
      * <p>
      * Where the outcome should be a FAILED QUERY EXECUTION.
      */
     @Test
     public void multiTableAndReplicaFailure() {
 
         try {
 
             createSecondTable(sb, "TEST2");
             createReplicaOnB();
 
             /*
              * Create then execute INSERTS for TEST table.
              */
 
             final TestQuery testQuery = createInsertsForTestTable();
 
             final TestQuery test2query = createMultipleInsertStatements("TEST2", new int[TOTAL_ITERATIONS + ROWS_IN_DATABASE], new String[TOTAL_ITERATIONS + ROWS_IN_DATABASE], 6);
 
             /*
              * Test that the state of the database is as expected, before attempting to insert anything else.
              */
             final int[] pKey = new int[ROWS_IN_DATABASE];
             final String[] secondCol = new String[ROWS_IN_DATABASE];
 
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "Hello";
             secondCol[1] = "World";
             validateOnFirstMachine("TEST", pKey, secondCol);
 
             Constants.IS_TESTING_QUERY_FAILURE = true;
             try {
                 sa.execute(testQuery.getSQL() + test2query.getSQL()); // Insert test rows.
                 fail("This query should have failed.");
             }
             catch (final SQLException e) {
                 // Expected
             }
 
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "Hello";
             secondCol[1] = "World";
 
             validateOnFirstMachine("TEST", pKey, secondCol);
 
             // validateOnSecondMachine(testQuery);
 
             pKey[0] = 4;
             pKey[1] = 5;
             secondCol[0] = "Meh";
             secondCol[1] = "Heh";
 
             validateOnSecondMachine("TEST2", pKey, secondCol);
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
     }
 
     /**
      * Tests that when a transaction fails to create a table the System Table is not updated with information on that table.
      * 
      * <p>
      * TESTS AFTER A CREATE TABLE STATEMENT HAS BEEN RUN, BUT BEFORE ANYTHING ELSE.
      */
     @Test
     public void testSystemTableContents() {
 
         try {
 
             sa.execute("SELECT * FROM H2O.H2O_TABLE;");
 
             ResultSet rs = sa.getResultSet();
 
             if (rs.next() && rs.next()) {
                 fail("There should only be one table in the System Table.");
             }
 
             Constants.IS_TESTING_CREATETABLE_FAILURE = true;
             Constants.IS_TESTING_QUERY_FAILURE = true;
 
             try {
                 createSecondTable(sb, "TEST2");
                 fail("This should have failed.");
             }
             catch (final SQLException e) {
                 // Expected.
             }
 
             sa.execute("SELECT * FROM H2O.H2O_TABLE;");
 
             rs = sa.getResultSet();
 
             if (rs.next() && rs.next()) {
                 fail("There should only be one table in the System Table.");
             }
 
         }
         catch (final SQLException e) {
             e.printStackTrace();
             fail("An Unexpected SQLException was thrown.");
         }
 
         Constants.IS_TESTING_CREATETABLE_FAILURE = false;
         Constants.IS_TESTING_QUERY_FAILURE = false;
 
     }
 
     // /**
     // * Tests that when a transaction fails to create a table the System Table
     // * is not updated with information on that table.
     // *
     // * <p>TESTS AFTER A CREATE TABLE STATEMENT HAS BEEN RUN, AND AFTER SOME INSERTS
     // * INTO THAT TABLE.
     // */
     // @Test
     // public void testSystemTableContentsAfterInsert(){
     // try{
     //
     // sa.execute("SELECT * FROM H2O.H2O_TABLE;");
     //
     // ResultSet rs = sa.getResultSet();
     //
     // if (rs.next() && rs.next()){
     // fail("There should only be one table in the System Table.");
     // }
     //
     // Constants.IS_TESTING_QUERY_FAILURE = true;
     //
     // try{
     // createSecondTable(sb, "TEST2");
     // fail("This should have failed.");
     // } catch (SQLException e){
     // //Expected.
     // }
     //
     // try {
     // sa.execute("SELECT * FROM TEST2");
     // fail("This should have failed: the transaction was not committed.");
     // } catch (SQLException e){
     // //Expected.
     // }
     //
     //
     // sa.execute("SELECT * FROM H2O.H2O_TABLE;");
     //
     // rs = sa.getResultSet();
     //
     // if (rs.next() && rs.next()){
     // fail("There should only be one table in the System Table.");
     // }
     // } catch (SQLException e){
     // e.printStackTrace();
     // fail("An Unexpected SQLException was thrown.");
     // }
     // }
 
     /**
      * Test executing a set of queries where the external application explicitly turns auto-commit off.
      * @throws SQLException 
      */
     @Test
     public void testAutoCommitOffExternal() throws SQLException {
 
         ca.setAutoCommit(false);
 
         // Execute some queries
 
         sa.execute("INSERT INTO TEST VALUES(3, 'Quite');");
         sa.execute("INSERT INTO TEST VALUES(4, 'A');");
         sa.execute("INSERT INTO TEST VALUES(5, 'Few');");
         sa.execute("INSERT INTO TEST VALUES(6, 'Cases');");
 
         ca = DriverManager.getConnection("jdbc:h2:sm:mem:one", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
         cb = DriverManager.getConnection("jdbc:h2:mem:two", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
 
         final Statement sa2 = cb.createStatement();
 
         try {
             sa2.executeQuery("SELECT LOCAL * FROM TEST ORDER BY ID;");
 
             fail("Query timeout expected.");
 
         }
         catch (final SQLException e) {
             // Timeout expected.
         }
 
         // Commit
         ca.commit();
 
         // Check that changes have now been committed.
 
         sa.execute("SELECT LOCAL * FROM TEST ORDER BY ID;");
 
         final int[] pKey2 = {1, 2, 3, 4, 5, 6};
         final String[] secondCol2 = {"Hello", "World", "Quite", "A", "Few", "Cases"};
 
         validateResults(pKey2, secondCol2, sa.getResultSet());
 
         sa.execute("SELECT LOCAL * FROM TEST ORDER BY ID;");
 
         ca.setAutoCommit(true);
     }
 
     /**
      * Tests that prepared statements work in the system where no replication is involved.
      * @throws SQLException 
      */
     @Test
     public void testPreparedStatementsNoReplication() throws SQLException {
 
         PreparedStatement mStmt = null;
         try {
             mStmt = ca.prepareStatement("insert into PUBLIC.TEST (id,name) values (?,?)");
 
             for (int i = 3; i < 100; i++) {
                 mStmt.setInt(1, i);
                 mStmt.setString(2, "helloNumber" + i);
                 mStmt.addBatch();
             }
 
             mStmt.executeBatch();
 
             final int[] pKey = new int[100];
             final String[] secondCol = new String[100];
 
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "Hello";
             secondCol[1] = "World";
 
             final TestQuery test2query = createMultipleInsertStatements("TEST", pKey, secondCol, 3);
 
             sa.execute("SELECT LOCAL * FROM PUBLIC.TEST ORDER BY ID;");
 
             validateResults(test2query.getPrimaryKey(), test2query.getSecondColumn(), sa.getResultSet());
         }
         finally {
             if (mStmt != null) {
                 mStmt.close();
             }
         }
 
     }
 
     /**
      * Tests that prepared statements work in the system where no replication is involved.
      * 
      * @throws SQLException
      */
     @Test
     public void testPreparedStatementsUpdateNoReplication() throws SQLException {
 
         // update bahrain set Name=? where ID=? {1: 'PILOT_1', 2: 1};
         createReplicaOnB();
 
         PreparedStatement mStmt1 = null;
         PreparedStatement mStmt2 = null;
         try {
             mStmt1 = ca.prepareStatement("insert into PUBLIC.TEST (id,name) values (?,?)");
 
             for (int i = 3; i < 10; i++) {
                 mStmt1.setInt(1, i);
                 mStmt1.setString(2, "helloNumber" + i);
                 mStmt1.addBatch();
             }
 
             mStmt1.executeBatch();
 
             mStmt2 = ca.prepareStatement("update PUBLIC.TEST set name=? where id=?;");
             mStmt2.setString(1, "New Order");
             mStmt2.setInt(2, 1);
             mStmt2.addBatch();
             mStmt2.executeBatch();
 
             final int[] pKey = new int[10];
             final String[] secondCol = new String[10];
 
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "New Order";
             secondCol[1] = "World";
 
             final TestQuery test2query = createMultipleInsertStatements("TEST", pKey, secondCol, 3);
 
             sa.execute("SELECT LOCAL * FROM PUBLIC.TEST ORDER BY ID;");
 
             validateResults(test2query.getPrimaryKey(), test2query.getSecondColumn(), sa.getResultSet());
 
         }
         finally {
             mStmt1.close();
             mStmt2.close();
         }
     }
 
     /**
      * Tests that prepared statements work in the system where no replication is involved.
      * 
      * @throws SQLException
      */
     @Test
     public void testPreparedStatementsDeleteNoReplication() throws SQLException {
 
         // update bahrain set Name=? where ID=? {1: 'PILOT_1', 2: 1};
         createReplicaOnB();
 
         PreparedStatement mStmt1 = null;
         PreparedStatement mStmt2 = null;
         try {
             mStmt1 = ca.prepareStatement("insert into PUBLIC.TEST (id,name) values (?,?)");
 
             for (int i = 3; i < 10; i++) {
                 mStmt1.setInt(1, i);
                 mStmt1.setString(2, "helloNumber" + i);
                 mStmt1.addBatch();
             }
 
             mStmt1.executeBatch();
 
             mStmt2 = ca.prepareStatement("delete from PUBLIC.TEST where id=?;");
             mStmt2.setInt(1, 9);
             mStmt2.addBatch();
             mStmt2.executeBatch();
 
             final int[] pKey = new int[9];
             final String[] secondCol = new String[9];
 
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "Hello";
             secondCol[1] = "World";
 
             final TestQuery test2query = createMultipleInsertStatements("TEST", pKey, secondCol, 3);
 
             sa.execute("SELECT LOCAL * FROM PUBLIC.TEST ORDER BY ID;");
 
             validateResults(test2query.getPrimaryKey(), test2query.getSecondColumn(), sa.getResultSet());
         }
         finally {
             mStmt1.close();
             mStmt2.close();
         }
     }
 
     // /**
     // * Tests that prepared statements work in the system where no replication is involved.
     // */
     // @Test
     // public void testPreparedStatementsMultipleTransactions(){
     //
     // PreparedStatement mStmt = null;
     // try
     // {
     // Connection cc = DriverManager.getConnection("jdbc:h2:sm:mem:one", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
     //
     // cc.setAutoCommit(false);
     // mStmt = cc.prepareStatement( "CREATE TABLE PUBLIC.TEST5 (ID INT, NAME VARCHAR(255));" );
     //
     //
     // mStmt.execute();
     //
     // cc.commit();
     //
     // cc.close();
     //
     // while (!cc.isClosed()){};
     //
     // } catch ( SQLException ex ) {
     // ex.printStackTrace();
     // fail("Unexpected SQL Exception was thrown. Not cool.");
     // }
     //
     //
     // mStmt = null;
     // try
     // {
     //
     //
     // mStmt = ca.prepareStatement( "insert into PUBLIC.TEST5 (id,name) values (?,?)" );
     //
     //
     // for (int i = 3; i < 100; i++){
     // mStmt.setInt(1, i);
     // mStmt.setString(2, "helloNumber" + i);
     // mStmt.addBatch();
     // }
     //
     // mStmt.executeBatch();
     //
     // int[] pKey = new int[100];
     // String[] secondCol = new String[100];
     //
     // pKey[0] = 1; pKey[1] = 2;
     // secondCol[0] = "Hello"; secondCol[1] = "World";
     //
     // TestQuery test2query = createMultipleInsertStatements("TEST5", pKey, secondCol, 3);
     //
     // sa.execute("SELECT LOCAL * FROM PUBLIC.TEST5 ORDER BY ID;");
     //
     // validateResults(test2query.getPrimaryKey(), test2query.getSecondColumn(), sa.getResultSet());
     //
     // } catch ( SQLException ex ) {
     // ex.printStackTrace();
     // fail("Unexpected SQL Exception was thrown. Not cool.");
     // }
     // }
 
     /**
      * Tests that prepared statements work in the system where replication is involved.
      * @throws SQLException 
      */
     @Test
     public void testPreparedStatementsReplication() throws SQLException {
 
         PreparedStatement mStmt = null;
         try {
             createReplicaOnB();
 
             mStmt = cb.prepareStatement("insert into PUBLIC.TEST (id,name) values (?,?)");
 
             for (int i = 3; i < 100; i++) {
                 mStmt.setInt(1, i);
                 mStmt.setString(2, "helloNumber" + i);
                 mStmt.addBatch();
             }
 
             mStmt.executeBatch();
 
             final int[] pKey = new int[100];
             final String[] secondCol = new String[100];
 
             pKey[0] = 1;
             pKey[1] = 2;
             secondCol[0] = "Hello";
             secondCol[1] = "World";
 
             final TestQuery test2query = createMultipleInsertStatements("TEST", pKey, secondCol, 3);
 
             /*
              * If the query hangs and fails at this point its probably because the lock isn't being relinquished when executeBatch is
              * called.
              */
             sa.execute("SELECT LOCAL * FROM PUBLIC.TEST ORDER BY ID;");
 
             validateResults(test2query.getPrimaryKey(), test2query.getSecondColumn(), sa.getResultSet());
         }
         finally {
             mStmt.close();
         }
     }
 
     /**
      * Tests that prepared statements work in the system where no replication is involved, and the connection is made through a TCP server.
      * This test uses some of the *Remote classes in H2 which have slightly different behaviour.
      * @throws SQLException 
      */
     @Test
     public void testPreparedStatementsTcpServer() throws SQLException {
 
         /*
          * Reset the locator file. This test doesn't use the in-memory database.
          */
         ls.setRunning(false);
         while (!ls.isFinished()) {
         };
 
         ls = new LocatorServer(29999, "junitLocator");
         ls.createNewLocatorFile();
         ls.start();
 
         try {
             DeleteDbFiles.execute("db_data/unittests/", "schema_test", true);
         }
         catch (final SQLException e) {
         }
         Connection conn = null;
         // start the server, allows to access the database remotely
         Server server = null;
         Statement sa = null;
         PreparedStatement mStmt = null;
 
         try {
             try {
                 server = Server.createTcpServer(new String[]{"-tcpPort", "9990", "-SMLocation", "jdbc:h2:sm:tcp://localhost:9990/db_data/unittests/schema_test"});
                 server.start();
 
                 conn = DriverManager.getConnection("jdbc:h2:sm:tcp://localhost:9990/db_data/unittests/schema_test", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
 
                 sa = conn.createStatement();
 
                 sa.execute("DROP ALL OBJECTS;");
                 sa.execute("CREATE TABLE TEST5(ID INT PRIMARY KEY, NAME VARCHAR(255));");
                 sa.execute("INSERT INTO TEST5 VALUES(1, 'Hello');");
                 sa.execute("INSERT INTO TEST5 VALUES(2, 'World');");
             }
             finally {
                if (conn != null) {
                    conn.close();
                }

                 server.shutdown();
                 server.stop();
             }
 
             TestBase.resetLocatorFile();
 
             try {
                 server = Server.createTcpServer(new String[]{"-tcpPort", "9990", "-SMLocation", "jdbc:h2:sm:tcp://localhost:9990/db_data/unittests/schema_test"});
 
                 server.start();
 
                 conn = DriverManager.getConnection("jdbc:h2:sm:tcp://localhost:9990/db_data/unittests/schema_test", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
 
                 mStmt = conn.prepareStatement("insert into PUBLIC.TEST5 (id,name) values (?,?)");
 
                 for (int i = 3; i < 100; i++) {
                     mStmt.setInt(1, i);
                     mStmt.setString(2, "helloNumber" + i);
                     mStmt.addBatch();
                 }
 
                 mStmt.executeBatch();
 
                 final int[] pKey = new int[100];
                 final String[] secondCol = new String[100];
 
                 pKey[0] = 1;
                 pKey[1] = 2;
                 secondCol[0] = "Hello";
                 secondCol[1] = "World";
 
                 final TestQuery test2query = createMultipleInsertStatements("TEST5", pKey, secondCol, 3);
 
                 sa = conn.createStatement();
 
                 sa.execute("SELECT LOCAL * FROM PUBLIC.TEST5 ORDER BY ID;");
 
                 validateResults(test2query.getPrimaryKey(), test2query.getSecondColumn(), sa.getResultSet());
             }
             finally {
                 server.shutdown();
                 server.stop();
             }
         }
         finally {
 
            if (conn != null) {
                conn.close();
            }
            if (mStmt != null) {
                mStmt.close();
            }
            if (sa != null) {
                sa.close();
            }
 
             try {
                 DeleteDbFiles.execute("db_data/unittests/", "schema_test", true);
             }
             catch (final SQLException e) {
             }
         }
     }
 
     /**
      * Tests that prepared statements work in the system where no replication is involved.
      * 
      * @throws SQLException
      */
     @Test
     public void testPreparedStatementAutoCommitOff() throws SQLException {
 
         // update bahrain set Name=? where ID=? {1: 'PILOT_1', 2: 1};
         createReplicaOnB();
 
         PreparedStatement mStmt = null;
         Statement stt = null;
         try {
             ca.setAutoCommit(false);
 
             stt = ca.createStatement();
             stt.execute("drop table if exists australia");
             stt.execute("create table australia (ID  INTEGER NOT NULL, Name VARCHAR(100), FirstName VARCHAR(100), Points INTEGER, LicenseID INTEGER, PRIMARY KEY(ID))");
 
             ca.commit();
 
             ca.setAutoCommit(false);
 
             mStmt = ca.prepareStatement("insert into australia values(?,?,?,?,?)");
 
             mStmt.setInt(1, 1);
             mStmt.setString(2, "name");
             mStmt.setString(3, "firstName");
             mStmt.setInt(4, 10);
             mStmt.setInt(5, 400);
             mStmt.addBatch();
 
             ResultSet rs = stt.executeQuery("select * from australia");
 
             /*
              * H2O used to throw an error here because the prepared statement command would use the 'CALL AUTOCOMMIT()' query to find
              * whether auto commit was on, and this call would somehow result in a commit being made.
              */
             if (rs.next()) {
                 fail("The table shouldn't have any entries yet. Update hasn't happened.");
             }
 
             System.err.println("About to execute batch.");
             mStmt.executeBatch();
             System.err.println("Executed batch.");
 
             System.err.println("About to execute select.");
 
            final Statement statement = cb.createStatement();
            statement.executeQuery("select * from australia");
            statement.close();
 
             System.err.println("Executed select.");
 
             // Execute batch seems to auto-commit, so the table's already exist before this point.
 
             ca.commit();
 
             ca.setAutoCommit(false);
 
             rs = stt.executeQuery("select * from australia");
 
             if (!rs.next()) {
                 fail("The table should have one entry.");
             }
 
             ca.setAutoCommit(false);
 
             stt.execute("drop table australia");
 
             stt.execute("create table australia (ID  INTEGER NOT NULL, Name VARCHAR(100), " + "FirstName VARCHAR(100), Points INTEGER, LicenseID INTEGER, PRIMARY KEY(ID))");
 
             ca.commit();
 
         }
         finally {
             stt.close();
             mStmt.close();
         }
     }
 
     /**
      * Tests that the system can cope with a large number of inserts onto a single table. It tries to recreate a bug where the row count
      * becomes incorrect on a table scan after a while.
      * 
      * @throws SQLException
      * @throws ClassNotFoundException
      */
     // @Test
     public void largeNumberOfInsertsCoupleAtATime() throws SQLException, ClassNotFoundException {
 
         // update bahrain set Name=? where ID=? {1: 'PILOT_1', 2: 1};
         // createReplicaOnB();
         /*
          * Reset the locator file. This test doesn't use the in-memory database.
          */
         ls.setRunning(false);
         while (!ls.isFinished()) {
         };
 
         ls = new LocatorServer(29999, "junitLocator");
         ls.createNewLocatorFile();
         ls.start();
 
         try {
             DeleteDbFiles.execute("db_data/unittests/", "schema_test", true);
         }
         catch (final SQLException e) {
         }
         Connection conn = null;
         // start the server, allows to access the database remotely
         Server server = null;
 
         Statement sa = null;
         try {
             server = Server.createTcpServer(new String[]{"-tcpPort", "9990", "-SMLocation", "jdbc:h2:sm:tcp://localhost:9990/db_data/unittests/schema_test"});
             server.start();
 
             Class.forName("org.h2.Driver");
             conn = DriverManager.getConnection("jdbc:h2:sm:tcp://localhost:9990/db_data/unittests/schema_test", PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
             sa = conn.createStatement();
             conn.setAutoCommit(true);
 
             sa.execute("create schema if not exists RESOURCE_MONITORING");
 
             sa.execute("drop table if exists RESOURCE_MONITORING.PROCESS");
             sa.execute("drop table if exists RESOURCE_MONITORING.SYS_INFO");
 
             sa.execute("CREATE TABLE IF NOT EXISTS RESOURCE_MONITORING.SYS_INFO( machine_id VARCHAR(40), hostname VARCHAR(255), " + "primary_ip VARCHAR(15), cpu_vendor VARCHAR(100), cpu_model VARCHAR(100), num_cores TINYINT(2), num_cpus TINYINT(2), cpu_mhz INT, "
                             + "cpu_cache_size BIGINT, os_name VARCHAR(255), os_version VARCHAR(100), " + "default_gateway VARCHAR(15), memory_total BIGINT, swap_total BIGINT, PRIMARY KEY (machine_id));");
 
             sa.execute("INSERT INTO RESOURCE_MONITORING.SYS_INFO VALUES('" + "MY_MACHINE_ID" + "', '" + "data.hostname" + "', '" + "data.primary_ip" + "', '" + "data.cpu_vendor" + "', '" + "data.cpu_model" + "', " + 2 + ", " + "2" + ", " + "2000" + ", " + "-1" + ", '" + "data.os_name" + "', '"
                             + "data.os_version" + "', '" + "gateway" + "', " + "2000" + ", " + "2000" + ");");
 
             sa.execute("CREATE TABLE IF NOT EXISTS RESOURCE_MONITORING.PROCESS( machine_id VARCHAR(40), start_ts TIMESTAMP, end_ts TIMESTAMP, measurements INT, " + "process_name VARCHAR(255), "
                             + "process_start_time BIGINT, process_cpu_percent_avg DOUBLE, process_cpu_percent_min DOUBLE, process_cpu_percent_max DOUBLE, " + "process_mem_avg BIGINT, process_mem_min BIGINT,process_mem_max BIGINT,"
                             + "process_resident_avg BIGINT, process_resident_min BIGINT, process_resident_max BIGINT" + ", FOREIGN KEY (machine_id) REFERENCES SYS_INFO(machine_id));");
 
             final int numberOfInserts = 5000000;
             for (int i = 0; i < numberOfInserts; i++) {
                 final String insert = "INSERT INTO RESOURCE_MONITORING.PROCESS VALUES('" + "MY_MACHINE_ID" + "',  '" + "2010-09-22" + "', '" + "2010-09-22" + "', " + 5 + ", '" + "num:" + i + "', '" + "000022442" + "', " + 0.2 + ", " + 0.2 + ", " + 0.2 + ", " + 3000 + ", " + 3000 + ", " + 3000
                                 + ", " + 3000 + ", " + 3000 + ", " + 3000 + ");";
 
                 sa.executeUpdate(insert);
 
                 System.out.println(i);
             }
 
             sa.executeQuery("select * from RESOURCE_MONITORING.PROCESS");
 
             final ResultSet rs = sa.executeQuery("select count(*) from RESOURCE_MONITORING.PROCESS");
 
             if (rs.next()) {
                 assertEquals(numberOfInserts, rs.getInt(1));
             }
             sa.execute("drop table if exists RESOURCE_MONITORING.PROCESS");
         }
         finally {
             sa.close();
             conn.close();
         }
     }
 
     /*
      * ############### Utility Methods ###############
      */
 
     /**
      * Creates lots of insert statements for testing.
      */
     private TestQuery createInsertsForTestTable() {
 
         final int[] pKey = new int[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
         final String[] secondCol = new String[TOTAL_ITERATIONS + ROWS_IN_DATABASE];
 
         pKey[0] = 1;
         pKey[1] = 2;
         secondCol[0] = "Hello";
         secondCol[1] = "World";
 
         return createMultipleInsertStatements("TEST", pKey, secondCol, ROWS_IN_DATABASE + 1);
     }
 
     /**
      * Creates lots of insert statements for testing with the stated table.
      * 
      * @param tableName
      *            Name of the table where values are being inserted.
      * @param pKey
      *            Array of primary key values in the table. This is a parameter in case the calling method wants to add some custom values
      *            initially.
      * @param secondCol
      *            Array of values for the second column in the table. This is a parameter in case the calling method wants to add some
      *            custom values initially.
      * @return The query to be executed and the expected results from this execution.
      */
     private TestQuery createMultipleInsertStatements(final String tableName, final int[] pKey, final String[] secondCol, final int startPoint) {
 
         final StringBuilder query = new StringBuilder();
 
         for (int i = startPoint; i < pKey.length; i++) {
             pKey[i - 1] = i;
             secondCol[i - 1] = "helloNumber" + i;
 
             query.append("INSERT INTO ");
             query.append(tableName);
             query.append(" VALUES(");
             query.append(pKey[i - 1]);
             query.append(", '");
             query.append(secondCol[i - 1]);
             query.append("');\n");
         }
         return new TestQuery(query.toString(), tableName, pKey, secondCol);
     }
 }
