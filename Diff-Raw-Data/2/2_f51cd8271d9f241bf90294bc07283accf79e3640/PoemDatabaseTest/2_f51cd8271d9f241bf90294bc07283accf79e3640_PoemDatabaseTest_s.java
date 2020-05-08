 package org.melati.poem.test;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Enumeration;
 
 import org.melati.poem.AccessToken;
 import org.melati.poem.Capability;
 import org.melati.poem.ExecutingSQLPoemException;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemTask;
 import org.melati.poem.Table;
 import org.melati.poem.User;
 import org.melati.poem.UserTable;
 import org.melati.poem.dbms.Dbms;
 import org.melati.poem.Database;
 
 /**
  * Test the features of all Poem databases.
  * 
  * @author timp
  */
 public class PoemDatabaseTest extends PoemTestCase {
 
   /**
    * Constructor for PoemTest.
    * 
    * @param arg0
    */
   public PoemDatabaseTest(String arg0) {
     super(arg0);
   }
 
   /**
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   /**
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
     getDb().setLogCommits(false);
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.generated.PoemDatabaseBase#getUserTable()
    * @see org.melati.poem.Database#getTable(String)
    */
   public void testGetUserTable() {
     UserTable ut1 = getDb().getUserTable();
     UserTable ut2 = (UserTable)getDb().getTable("user");
     assertEquals(ut1, ut2);
   }
 
   /**
    * @see org.melati.poem.Database#transactionsMax()
    */
   public void testTransactionsMax() {
     assertEquals(maxTrans, getDb().transactionsMax());
   }
 
   /**
    * @see org.melati.poem.Database#getDisplayTables()
    */
   public void testGetDisplayTables() {
     final String expected = "user (from the data structure definition)"
             + "group (from the data structure definition)"
             + "capability (from the data structure definition)"
             + "groupmembership (from the data structure definition)"
             + "groupcapability (from the data structure definition)"
             + "tableinfo (from the data structure definition)"
             + "columninfo (from the data structure definition)"
             + "tablecategory (from the data structure definition)"
             + "setting (from the data structure definition)";
 
     Enumeration en = getDb().getDisplayTables();
     String result = "";
     while (en.hasMoreElements()) {
       result += en.nextElement().toString();
     }
     assertEquals(expected, result);
   }
 
   /**
    * @see org.melati.poem.Database#sqlQuery(String)
    */
   public void testSqlQuery() {
     String query = "select * from " + getDb().getUserTable().quotedName();
     getDb().setLogSQL(true);
     ResultSet rs = getDb().sqlQuery(query);
     getDb().setLogSQL(false);
     int count = 0;
     try {
       while (rs.next()) {
         count++;
       }
     } catch (SQLException e) {
       e.printStackTrace();
       fail();
     }
     assertEquals(2, count);
   }
   /**
    * @see org.melati.poem.Database#sqlQuery(String)
    */
   public void testSqlQueryThrows() {
     String query = "select * from nonexistanttable" ;
     try { 
       getDb().sqlQuery(query);
       fail("Should have blown up");
     } catch (ExecutingSQLPoemException e) { 
       e = null;
     }
   }
 
   /**
    * @see org.melati.poem.Database#hasCapability(User, Capability)
    */
   public void testHasCapability() {
     assertTrue(getDb().hasCapability(
             getDb().getUserTable().administratorUser(),
             getDb().getCanAdminister()));
     // This is only true for a db that thas never had its administration set
     //assertTrue(getDb().hasCapability(getDb().getUserTable().guestUser(),
     //        getDb().getCanAdminister()));
     assertTrue(getDb().hasCapability(getDb().getUserTable().guestUser(), null));
   }
 
   /**
    * @see org.melati.poem.test.DatabaseTest#administerCapability()
    * @see org.melati.poem.Database#administerCapability()
    */
   public void testAdministerCapability() {
   }
 
   /**
    * @see org.melati.poem.Database#getCanAdminister()
    */
   public void testGetCanAdminister() {
     // starts as null, but once set cannot be set to null again
     //assertNull(getDb().getCanAdminister());
   }
 
   /**
    * @see org.melati.poem.UserTable#guestUser()
    */
   public final void testGuestUser() {
     User u = getDb().getUserTable().guestUser();
     assertEquals(u.getLogin(), "_guest_");
   }
 
   /**
    * @see org.melati.poem.UserTable#administratorUser()
    */
   public final void testAdministratorUser() {
     User u = getDb().getUserTable().administratorUser();
     assertEquals(u.getPassword(), "FIXME");
   }
 
   /**
    * @see org.melati.poem.Database#referencesTo(Table)
    */
   public void testReferencesToTable() {
     String expected = "groupmembership.user: reference to user "
             + "(INT (org.melati.poem.ReferencePoemType)) "
             + "(from the data structure definition)";
     String result = "";
     Enumeration en = getDb().referencesTo(getDb().getUserTable());
     while (en.hasMoreElements())
       result += en.nextElement();
     assertEquals(expected, result);
   }
 
   /**
    * @see org.melati.poem.Database#getDbms()
    */
   public void testGetDbms() {
     Dbms dbms = getDb().getDbms();
     System.err.println(dbms);
   }
 
   /**
    * @see org.melati.poem.Database#toString()
    */
   public void testToString() {
     String name = getDb().toString();
     assertTrue(name.endsWith(databaseName));
   }
 
   /**
    * @see org.melati.poem.Database#logSQL()
    */
   public void testLogSQL() {
     assertFalse(getDb().logSQL());
   }
 
   /**
    * @see org.melati.poem.Database#setLogSQL(boolean)
    */
   public void testSetLogSQL() {
     assertFalse(getDb().logSQL());
     getDb().setLogSQL(true);
     assertTrue(getDb().logSQL());
     getDb().setLogSQL(false);
     assertFalse(getDb().logSQL());
   }
 
   /**
    * @see org.melati.poem.test.DatabaseTest#testConnect()
    * @see Database#connect(String, String, String, String, int)
    */
   public void testConnect() {
 
   }
 
   /**
    * @see org.melati.poem.Database#disconnect()
    */
   public void testDisconnect() {
     // getDb().disconnect();
   }
 
   /**
    * @see org.melati.poem.Database#shutdown()
    */
   public void testShutdown() {
     // getDb().shutdown();
   }
 
   /**
    * Note this is here so that we have the expected number of tables.
    * 
    * @see org.melati.poem.Database#tables()
    */
   public void testTables() {
     // tested in PoemTestCase
   }
 
   /**
    * Note this is here so that we have the expected number of columns.
    * 
    * @see org.melati.poem.Database#columns()
    */
   public void testColumns() {
     // tested in PoemTestCase
   }
 
   /**
    * @see DynamicTableTest
    * @see org.melati.poem.Database#addTableAndCommit(TableInfo, String)
    */
 
   /**
    * @see org.melati.poem.Database#addConstraints()
    * @see org.melati.poem.test.DatabaseTest#testAddConstraints()
    */
   public void testAddConstraints() {
   }
 
   /**
    * @see org.melati.poem.Database#setTransactionsMax(int)
    */
   public void testSetTransactionsMax() {
     int current = 0;
     current = getDb().transactionsMax();
     assertEquals(maxTrans, current);
     getDb().setTransactionsMax(12);
     assertTrue(getDb().transactionsMax() == 12);
     getDb().setTransactionsMax(current);
   }
 
   /**
    * @see org.melati.poem.Database#getTransactionsCount()
    */
   public void testGetTransactionsCount() {
     assertEquals(maxTrans, getDb().getTransactionsCount());
   }
 
   /**
    * @see org.melati.poem.Database#getFreeTransactionsCount()
    */
   public void testGetFreeTransactionsCount() {
     assertEquals(maxTrans -1, getDb().getFreeTransactionsCount());
   }
 
   /**
    * @see org.melati.poem.Database#poemTransaction(int)
    */
   public void testPoemTransaction() {
     assertEquals(getDb().poemTransaction(0).toString(), "transaction0");
   }
 
   /**
    * @see org.melati.poem.Database#transaction(int)
    */
   public void testTransaction() {
     assertEquals(getDb().transaction(0).toString(), "transaction0");
   }
 
   /**
    * @see org.melati.poem.Database#isFree(PoemTransaction)
    */
   public void testIsFree() {
     for (int i = 0; i < maxTrans-1; i++)
       assertTrue(getDb().isFree(getDb().poemTransaction(i)));
     assertFalse(getDb().isFree(getDb().poemTransaction(maxTrans-1)));
     
     try {
       System.err.println(getDb().isFree(getDb().poemTransaction(maxTrans)));
       fail("Should have bombed.");
     } catch (ArrayIndexOutOfBoundsException e) {
       e = null;
     }
   }
 
   /**
    * @see org.melati.poem.Database#beginExclusiveLock()
    */
   public void testBeginExclusiveLock() {
 
   }
 
   /**
    * @see org.melati.poem.Database#endExclusiveLock()
    */
   public void testEndExclusiveLock() {
 
   }
 
   /**
    * @see org.melati.poem.Database#inSession(AccessToken, PoemTask)
    */
   public void testInSession() {
 
   }
 
   /**
    * @see org.melati.poem.Database#beginSession(AccessToken)
    */
   public void testBeginSession() {
 
   }
 
   /**
    * @see org.melati.poem.Database#endSession()
    */
   public void testEndSession() {
 
   }
 
   /**
    * @see org.melati.poem.Database#getTable(String)
    */
   public void testGetTable() {
     assertEquals(getDb().getUserTable(), getDb().getTable("user"));
   }
 
   /**
    * @see org.melati.poem.Database#getTableInfoTable()
    */
   public void testGetTableInfoTable() {
     assertEquals(getDb().getTableInfoTable(), getDb().getTable("tableinfo"));
   }
 
   /**
    * @see org.melati.poem.Database#getTableCategoryTable()
    */
   public void testGetTableCategoryTable() {
     assertEquals(getDb().getTableCategoryTable(), getDb().getTable(
             "tablecategory"));
   }
 
   /**
    * @see org.melati.poem.Database#getColumnInfoTable()
    */
   public void testGetColumnInfoTable() {
     assertEquals(getDb().getColumnInfoTable(), getDb().getTable("columninfo"));
   }
 
   /**
    * @see org.melati.poem.Database#getCapabilityTable()
    */
   public void testGetCapabilityTable() {
     assertEquals(getDb().getCapabilityTable(), getDb().getTable("capability"));
   }
 
   /**
    * @see org.melati.poem.Database#getGroupTable()
    */
   public void testGetGroupTable() {
     assertEquals(getDb().getGroupTable(), getDb().getTable("group"));
   }
 
   /**
    * @see org.melati.poem.Database#getGroupMembershipTable()
    */
   public void testGetGroupMembershipTable() {
     assertEquals(getDb().getGroupMembershipTable(), getDb().getTable(
             "groupmembership"));
   }
 
   /**
    * @see org.melati.poem.Database#getGroupCapabilityTable()
    */
   public void testGetGroupCapabilityTable() {
     assertEquals(getDb().getGroupCapabilityTable(), getDb().getTable(
             "groupcapability"));
   }
 
   /**
    * @see org.melati.poem.Database#getSettingTable()
    */
   public void testGetSettingTable() {
     assertEquals(getDb().getSettingTable(), getDb().getTable("setting"));
   }
 
   /**
    * Need to be in separate transactions on postgresql. 
    * @see org.melati.poem.Database#sqlUpdate(String)
    */
   public void testSqlUpdate() {
     try {
       getDb().sqlUpdate("insert");
       fail("should have blown up");
     } catch (ExecutingSQLPoemException e) {
       e = null;
       // All ok
     }
   }
   /**
    * Need to be in separate transactions on postgresql. 
    * @see org.melati.poem.Database#sqlUpdate(String)
    */
   public void testSqlUpdate1() {
     try {
       getDb()
               .sqlUpdate(
                       "INSERT INTO \"COLUMNINFO\" VALUES('Name','A human-readable name for the group',TRUE,-7,FALSE,60,20,1,22,2,NULL,NULL,NULL,15,1,'name',1,TRUE,0,0,0,FALSE,FALSE,TRUE,2)");
       fail("should have blown up");
     } catch (ExecutingSQLPoemException e) {
       e = null;
       // All ok
     }
   }
   /**
    * Need to be in separate transactions on postgresql. 
    * @see org.melati.poem.Database#sqlUpdate(String)
    */
   public void testSqlUpdate2() {
     getDb().setLogSQL(true);
     getDb().sqlUpdate(getDb().getDbms().createTableSql() + "RAWSQL (ID INT)");
     getDb().setLogSQL(false);
     getDb().sqlUpdate("DROP TABLE RAWSQL");
   }
 
   /**
    * @see org.melati.poem.Database#givesCapabilitySQL(User, Capability)
    */
   public void testGivesCapabilitySQL() {
     assertTrue(getDb().givesCapabilitySQL((User)getDb().guestAccessToken(),
             getDb().administerCapability()).indexOf("SELECT") == 0);
   }
 
   /**
    * @see org.melati.poem.Database#guestAccessToken()
    */
   public void testGuestAccessToken() {
     AccessToken guest = getDb().guestAccessToken();
     User guestUser = getDb().getUserTable().guestUser();
     assertEquals(guest, guestUser);
   }
 
   /**
    * @see org.melati.poem.Database#setCanAdminister()
    * @see org.melati.poem.Database#setCanAdminister(String)
    */
   public void testSetCanAdminister() {
     // There is no way to set it back to null so do not check
     //assertNull(getDb().getCanAdminister());
     getDb().setCanAdminister();
     assertEquals(getDb().getCapabilityTable().get("_administer_"), getDb()
             .getCanAdminister());
     getDb().setCanAdminister("testing");
     assertEquals(getDb().getCapabilityTable().get("testing"), getDb()
             .getCanAdminister());
     getDb().setCanAdminister();
     getDb().getCapabilityTable().getNameColumn().firstWhereEq("testing")
             .delete();
 
   }
 
   /**
    * @see org.melati.poem.Database#trimCache(int)
    */
   public void testTrimCache() {
     getDb().trimCache(12);
   }
 
   /**
    * @see org.melati.poem.Database#uncacheContents()
    */
   public void testUncacheContents() {
     getDb().uncacheContents();
   }
 
   /**
    * @see org.melati.poem.Database#referencesTo(Persistent)
    */
   public void testReferencesToPersistent() {
     Enumeration en = getDb().referencesTo(
             getDb().getUserTable().administratorUser());
     int count = 0;
     while (en.hasMoreElements()) {
       en.nextElement();
       count++;
     }
     System.err.println(count);
     assertTrue(count == 1);
 
   }
 
   /**
    * @see org.melati.poem.Database#dumpCacheAnalysis()
    */
   public void testDumpCacheAnalysis() {
     getDb().dumpCacheAnalysis();
   }
 
   /**
    * @see org.melati.poem.Database#dump()
    */
   public void testDump() {
     getDb().dump();
   }
 
   /**
    * @see org.melati.poem.Database#quotedName(String)
    */
   public void testQuotedName() {
    assertEquals("\"user\"", getDb().quotedName("user").toLowerCase());
   }
 
   /**
    * @see org.melati.poem.Database#getCommittedConnection()
    */
   public void testGetCommittedConnection() {
 
   }
 
   /**
    * @see org.melati.poem.Database#logCommits()
    */
   public void testLogCommits() {
     assertFalse(getDb().logCommits());
   }
 
   /**
    * @see org.melati.poem.Database#setLogCommits(boolean)
    */
   public void testSetLogCommits() {
     assertFalse(getDb().logCommits());
     getDb().setLogCommits(true);
     assertTrue(getDb().logCommits());
     getDb().setLogCommits(false);
     assertFalse(getDb().logCommits());
 
   }
 
 }
