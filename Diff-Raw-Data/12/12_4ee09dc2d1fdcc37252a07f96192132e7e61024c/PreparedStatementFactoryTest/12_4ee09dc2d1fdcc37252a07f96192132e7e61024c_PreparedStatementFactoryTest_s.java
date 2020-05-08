 /**
  * 
  */
 package org.melati.poem.test.throwing;
 
 import org.melati.poem.Database;
 import org.melati.poem.PoemDatabaseFactory;
 import org.melati.poem.PreparedSQLSeriousPoemException;
 import org.melati.poem.PreparedStatementFactory;
 import org.melati.poem.SQLPoemException;
 import org.melati.poem.dbms.test.ThrowingConnection;
 import org.melati.poem.dbms.test.ThrowingPreparedStatement;
 
 /**
  * @author timp
  * @since 10 Feb 2007
  * 
  */
 public class PreparedStatementFactoryTest extends
     org.melati.poem.test.PreparedStatementFactoryTest {
 
   /**
    * @param name
    */
   public PreparedStatementFactoryTest(String name) {
     super(name);
   }
   
   protected void setUp() throws Exception {
     PoemDatabaseFactory.removeDatabase(databaseName);
     super.setUp();
     assertEquals("org.melati.poem.dbms.test.HsqldbThrower",getDb().getDbms().getClass().getName());
   }
   protected void tearDown() throws Exception {
     super.tearDown();
     PoemDatabaseFactory.removeDatabase(databaseName);
   }
 
 
   public Database getDatabase(String name) {
     maxTrans = 4;
     Database db = PoemDatabaseFactory.getDatabase(name, 
         "jdbc:hsqldb:mem:" + name,
         "sa", 
         "",
         "org.melati.poem.PoemDatabase",
         "org.melati.poem.dbms.test.HsqldbThrower", 
         false, 
         false, 
         false, maxTrans);
     return db;
   }
 
   public void testGet() {
     super.testGet();
   }
 
  public void brokentestPreparedStatement() throws Exception {
     ThrowingConnection.startThrowing("prepareStatement");
     try {
     // super.testPreparedStatement();
       fail("Should have bombed");
     } catch (SQLPoemException e) {
       assertEquals("Connection bombed", e.innermostException().getMessage());
     }
     ThrowingConnection.stopThrowing("prepareStatement");
   }
 
   public void testPreparedStatementFactory() {
     super.testPreparedStatementFactory();
   }
 
   public void testPreparedStatementPoemTransaction() {
     super.testPreparedStatementPoemTransaction();
   }
 
   public void testResultSet() throws Exception {
     PreparedStatementFactory it = new PreparedStatementFactory(getDb(),
         getDb().getUserTable().selectionSQL(null,null,null,true,false));
     try {
       ThrowingPreparedStatement.startThrowing("executeQuery");
       it.resultSet();
       fail("Should have bombed");
     } catch (PreparedSQLSeriousPoemException e) {
       assertEquals("PreparedStatement bombed", e.innermostException().getMessage());
     }
     ThrowingPreparedStatement.stopThrowing("executeQuery");
   }
 
 }
   
