 package org.melati.poem.test;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Enumeration;
 
 import org.melati.poem.AccessToken;
 import org.melati.poem.Capability;
 import org.melati.poem.ColumnInfo;
 import org.melati.poem.DisplayLevel;
 import org.melati.poem.DuplicateTableNamePoemException;
 import org.melati.poem.ExecutingSQLPoemException;
 import org.melati.poem.NoSuchTablePoemException;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemTask;
 import org.melati.poem.PoemThread;
 import org.melati.poem.PoemTypeFactory;
 import org.melati.poem.ReadPersistentAccessPoemException;
 import org.melati.poem.RowDisappearedPoemException;
 import org.melati.poem.Searchability;
 import org.melati.poem.Table;
 import org.melati.poem.TableCategory;
 import org.melati.poem.TableInfo;
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
   protected void setUp()
       throws Exception {
     super.setUp();
   }
 
   /**
    * @see TestCase#tearDown()
    */
   protected void tearDown()
       throws Exception {
     super.tearDown();
     getDb().setLogCommits(false);
     getDb().setLogSQL(false);
   }
 
   protected void poemDatabaseUnchanged() { 
     ColumnInfo ci = (ColumnInfo)getDb().getColumnInfoTable().getNameColumn().firstWhereEq("extra");
     if (ci != null) { 
       System.err.println("Cleaning up: " + ci);
       ci.delete();
     }
     Table extra = null;
     try { 
       extra = getDb().getTable("test");
     } catch (NoSuchTablePoemException e) {
       
     }
     if (extra != null ) {
       if (extra.troidColumn().getColumnInfo().statusExistent()) {
         extra.troidColumn().getColumnInfo().delete();
         System.err.println("Cleaning up troid ");
       }
     }
     TableInfo extraTI = (TableInfo)getDb().getTableInfoTable().getNameColumn().firstWhereEq("test");        
     if (extraTI != null) {
       extraTI.delete();
     }
     
     TableCategory normal = (TableCategory)getDb() .getTableCategoryTable().getNameColumn().firstWhereEq("Normal");
     if (normal != null ) {
       normal.delete();
     }
     try { 
       getDb().sqlUpdate("DROP TABLE TEST");
     } catch (ExecutingSQLPoemException e) { 
      assertTrue(e.getMessage().indexOf("it does not exist") > 0);
     }
     super.poemDatabaseUnchanged();
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
     if (getDb().getDbms().toString().indexOf("MSAccess") > 0)
       assertEquals(1, getDb().transactionsMax());
     else
       assertEquals(4, getDb().transactionsMax());
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
    * @see org.melati.poem.Database#hasCapability(User, Capability)
    */
   public void testHasCapability() {
     assertTrue(getDb().hasCapability(
         getDb().getUserTable().administratorUser(),
         getDb().getCanAdminister()));
     assertTrue(getDb().hasCapability(
         getDb().getUserTable().guestUser(),
         getDb().getCanAdminister()));
     assertTrue(getDb().hasCapability(
         getDb().getUserTable().guestUser(), null));
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
     assertNull(getDb().getCanAdminister());
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
     assertTrue(name.endsWith(getDbName()));
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
    * @see org.melati.poem.Database#addTableAndCommit(TableInfo, String)
    */
   public void testAddTableAndCommit() throws Exception {
     //getDb().setLogCommits(true);
     //getDb().setLogSQL(true);
     TableInfo info = (TableInfo) getDb().getTableInfoTable().newPersistent();
     info.setName("addedtable");
     info.setDisplayname("Junit created table");
     info.setDisplayorder(13);
     info.setSeqcached(new Boolean(false));
     info.setCategory_unsafe(new Integer(1));
     info.setCachelimit(0);
     info.makePersistent();
     PoemThread.commit();
     Table extra = getDb().addTableAndCommit(info, "id");
     ColumnInfo ci = (ColumnInfo)getDb().getColumnInfoTable().newPersistent();
     ci.setTableinfo(info);
     ci.setTypefactory(PoemTypeFactory.STRING);
     ci.setNullable(false);
     ci.setSize(-1);
     ci.setWidth(20);
     ci.setHeight(1);
     ci.setPrecision(1);
     ci.setScale(1);
     ci.setName("extra");
     ci.setDescription("Description of extra column");
     ci.setDisplayname("Extra");
     ci.setDisplayorder(10);
     ci.setIndexed(true);
     ci.setUnique(true);
     ci.setSearchability(Searchability.yes);
     ci.setDisplaylevel(DisplayLevel.primary);
     ci.setUsereditable(true);
     ci.setUsercreateable(true);
     
     ci.makePersistent();
     extra.addColumnAndCommit(ci);
     Persistent extraPersistent = extra.newPersistent();
     PoemThread.commit();
     assertNull(extraPersistent.troid());
     extraPersistent.setCooked("extra", "Test");
     extraPersistent.makePersistent();
     assertEquals("Test",extraPersistent.getField("extra").getRaw());
     
     extra.getTableInfo().setDefaultcanread(getDb().administerCapability());
     extraPersistent.getField("extra").getRaw();
 
     assertEquals(new Integer(0),extraPersistent.troid());
     Enumeration cols = getDb().getColumnInfoTable().getTableinfoColumn()
         .selectionWhereEq(info.troid());
     int colCount = 0;
     while (cols.hasMoreElements()) {
       ColumnInfo c = (ColumnInfo) cols.nextElement();
       c.delete();
       colCount++;
     }
     assertEquals(2,  colCount);
     
     PoemThread.commit(); 
     getDb().getCommittedConnection().commit();
     String q = "DROP TABLE " + getDb().getDbms().getQuotedName("addedtable");
     Statement dropStatement = getDb().getCommittedConnection()
         .createStatement();
     dropStatement.execute(q);
     getDb().getCommittedConnection().commit();
 
     try {
       getDb().addTableAndCommit(info, "id");
       fail("Should have blown up");
     } catch (DuplicateTableNamePoemException e) {
       e = null;
     }
     cols = getDb().getColumnInfoTable().getTableinfoColumn().selectionWhereEq(
         info.troid());
     colCount = 0;
     while (cols.hasMoreElements()) {
       ColumnInfo c = (ColumnInfo) cols.nextElement();
       c.delete();
       colCount++;
     }
     assertEquals(1,  colCount);
 
     info.deleteAndCommit();
     PoemThread.commit();
     try {
       getDb().addTableAndCommit(info, "id");
       fail("Should have blown up");
     } catch (RowDisappearedPoemException e) {
       e = null;
     }
     dropStatement.execute(q);
 
     TableInfo info3 = (TableInfo) getDb().getTableInfoTable().newPersistent();
     info3.setName("junit2");
     info3.setDisplayname("Junit created table");
     info3.setDisplayorder(13);
     info3.setSeqcached(new Boolean(false));
     info3.setCategory_unsafe(new Integer(1));
     info3.setCachelimit(0);
     info3.makePersistent();
     PoemThread.commit();
     getDb().addTableAndCommit(info3, "id");
     cols = getDb().getColumnInfoTable().getTableinfoColumn().selectionWhereEq(
         info3.troid());
     while (cols.hasMoreElements()) {
       ColumnInfo c = (ColumnInfo) cols.nextElement();
       c.delete();
     }
     info3.deleteAndCommit();
     PoemThread.commit();
 
     q = "DROP TABLE " + getDb().getDbms().getQuotedName("junit2");
     dropStatement.execute(q);
     dropStatement.close();
     PoemThread.commit();
   }
 
   public void testExtraColumnAsField () {
     TableInfo info = (TableInfo) getDb().getTableInfoTable().newPersistent();
     info.setName("test");
     info.setDisplayname("Junit created table");
     info.setDisplayorder(13);
     info.setSeqcached(new Boolean(false));
     info.setCategory_unsafe(new Integer(1));
     info.setCachelimit(0);
     info.makePersistent();
     //PoemThread.commit();
     Table extra = getDb().addTableAndCommit(info, "id");
     ColumnInfo ci = (ColumnInfo)getDb().getColumnInfoTable().newPersistent();
     ci.setTableinfo(info);
     ci.setTypefactory(PoemTypeFactory.STRING);
     ci.setNullable(false);
     ci.setSize(-1);
     ci.setWidth(20);
     ci.setHeight(1);
     ci.setPrecision(1);
     ci.setScale(1);
     ci.setName("extra");
     ci.setDescription("Description of extra column");
     ci.setDisplayname("Extra");
     ci.setDisplayorder(10);
     ci.setIndexed(true);
     ci.setUnique(true);
     ci.setSearchability(Searchability.yes);
     ci.setDisplaylevel(DisplayLevel.primary);
     ci.setUsereditable(true);
     ci.setUsercreateable(true);
     
     ci.makePersistent();
     extra.addColumnAndCommit(ci);
     
     Persistent extraInstance = extra.newPersistent();
     PoemThread.commit();
     assertNull(extraInstance.troid());
     extraInstance.setCooked("extra", "Test");
     extraInstance.makePersistent();
     assertEquals("Test",extraInstance.getField("extra").getRaw());
     
     // Show that guest cannot read
     extra.getTableInfo().setDefaultcanread(getDb().administerCapability());    
     PoemThread.setAccessToken(getDb().guestAccessToken());
     try {
       extraInstance.getField("extra").getRaw();
       fail("Should have bombed");
     } catch (ReadPersistentAccessPoemException e) {
       e = null;
     }
     // Do not tidy up here
     // as we no longer have write priviledges.
     // see our overidden version of poemDatabaseUnchanged()  
     //ci.delete();
     //extra.troidColumn().getColumnInfo().delete();
     //info.delete();
 
   }
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
     if (getDb().getDbms().toString().indexOf("MSAccess") > 0)
       assertEquals(1, getDb().transactionsMax());
     else
       assertEquals(4, getDb().transactionsMax());
     getDb().setTransactionsMax(12);
     assertTrue(getDb().transactionsMax() == 12);
     getDb().setTransactionsMax(current);
   }
 
   /**
    * @see org.melati.poem.Database#getTransactionsCount()
    */
   public void testGetTransactionsCount() {
     if (getDb().getDbms().toString().indexOf("MSAccess") > 0)
       assertTrue(getDb().getTransactionsCount() == 1);
     else
       assertTrue(getDb().getTransactionsCount() == 4);
   }
 
   /**
    * @see org.melati.poem.Database#getFreeTransactionsCount()
    */
   public void testGetFreeTransactionsCount() {
     if (getDb().getDbms().toString().indexOf("MSAccess") > 0)
       assertEquals(0,getDb().getFreeTransactionsCount());
     else
       assertEquals(3, getDb().getFreeTransactionsCount());
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
     if (!(getDb().getDbms().toString().indexOf("MSAccess") > 0)) {
       assertTrue(getDb().isFree(getDb().poemTransaction(0)));
       assertTrue(getDb().isFree(getDb().poemTransaction(1)));
       assertTrue(getDb().isFree(getDb().poemTransaction(2)));
       assertFalse(getDb().isFree(getDb().poemTransaction(3)));
       try {
         System.err.println(getDb().isFree(getDb().poemTransaction(4)));
         fail("Should have caused exception");
       } catch (ArrayIndexOutOfBoundsException e) {
         e = null;
       }
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
     try {
       getDb()
           .sqlUpdate(
               "INSERT INTO \"COLUMNINFO\" VALUES('Name','A human-readable name for the group',TRUE,-7,FALSE,60,20,1,22,2,NULL,NULL,NULL,15,1,'name',1,TRUE,0,0,0,FALSE,FALSE,TRUE,2)");
       fail("should have blown up");
     } catch (ExecutingSQLPoemException e) {
       e = null;
       // All ok
     }
     getDb().setLogSQL(true);  
     getDb().
     sqlUpdate(
         getDb().getDbms().createTableSql() + "RAWSQL (ID INT)" );
     getDb().setLogSQL(false);  
     getDb().
     sqlUpdate("DROP TABLE RAWSQL" );
   }
 
   /**
    * @see org.melati.poem.Database#givesCapabilitySQL(User, Capability)
    */
   public void testGivesCapabilitySQL() {
     assertTrue(getDb().givesCapabilitySQL((User) getDb().guestAccessToken(),
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
     assertNull(getDb().getCanAdminister());
     getDb().setCanAdminister();
     assertEquals(getDb().getCapabilityTable().get("_administer_"), getDb()
         .getCanAdminister());
     getDb().setCanAdminister("testing");
     assertEquals(getDb().getCapabilityTable().get("testing"), getDb()
         .getCanAdminister());
     getDb().getCapabilityTable().getNameColumn().firstWhereEq("testing").delete();
     
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
     assertEquals("\"user\"", getDb().quotedName("user").toLowerCase() );
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
