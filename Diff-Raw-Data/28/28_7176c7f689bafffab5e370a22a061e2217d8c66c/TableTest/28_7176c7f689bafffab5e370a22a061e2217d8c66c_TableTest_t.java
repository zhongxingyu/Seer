 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import org.melati.poem.CachedCount;
 import org.melati.poem.CachedExists;
 import org.melati.poem.ColumnInfo;
 import org.melati.poem.DisplayLevel;
 import org.melati.poem.Field;
 import org.melati.poem.Initialiser;
 import org.melati.poem.NoSuchColumnPoemException;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemThread;
 import org.melati.poem.Table;
 import org.melati.poem.TableInfo;
 import org.melati.poem.User;
 import org.melati.poem.UserTable;
 import org.melati.poem.util.EmptyEnumeration;
 import org.melati.poem.util.EnumUtils;
 
 /**
  * @author timp
  * 
  */
 public class TableTest extends PoemTestCase {
 
   /**
    * Constructor for PoemTest.
    * @param arg0
    */
   public TableTest(String arg0) {
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
   }
 
   
   
   /**
    * @see org.melati.poem.Table#Table(Database, String, DefinitionSource)
    */
   public void testTable() {
 
   }
 
   /**
    * @see org.melati.poem.Table#postInitialise()
    */
   public void testPostInitialise() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getDatabase()
    */
   public void testGetDatabase() {
     assertTrue(getDb().getUserTable().getDatabase().equals(getDb()));
   }
 
   /**
    * @see org.melati.poem.Table#getName()
    */
   public void testGetName() {
     Table ut = getDb().getUserTable();
     assertEquals("user", ut.getName());
   }
 
   /**
    * @see org.melati.poem.Table#quotedName()
    */
   public void testQuotedName() {
     Table ut = getDb().getUserTable();
     assertEquals(getDb().getDbms().getQuotedName("user"), ut.quotedName());
 
   }
 
   /**
    * @see org.melati.poem.Table#getDisplayName()
    */
   public void testGetDisplayName() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getDescription()
    */
   public void testGetDescription() {
     assertEquals("A registered user of the database", getDb().getUserTable()
         .getDescription());
   }
 
   /**
    * @see org.melati.poem.Table#getCategory()
    */
   public void testGetCategory() {
     assertEquals("User", getDb().getUserTable().getCategory().getName());
   }
 
   /**
    * @see org.melati.poem.Table#getInfo()
    */
   public void testGetInfo() {
     assertEquals("User", getDb().getUserTable().getInfo().getCategory()
         .getName());
   }
 
   /**
    * @see org.melati.poem.Table#tableInfoID()
    */
   public void testTableInfoID() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getColumn(String)
    */
   public void testGetColumn() {
     try { 
       getDb().getUserTable().getColumn("nonexistantcolum");
       fail("Should have blown up");
     } catch (NoSuchColumnPoemException e) { 
       e = null; 
     }
   }
 
   /**
    * @see org.melati.poem.Table#_getColumn(String)
    */
   public void test_getColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#columns()
    */
   public void testColumns() {
 
   }
 
   /**
    * Not actually used in java, maybe in templates.
    * 
    * @see org.melati.poem.Table#getColumnsCount()
    */
   public void testGetColumnsCount() {
     assertEquals(4, getDb().getUserTable().getColumnsCount());
 
   }
 
   /**
    * Null return not testable from public name space.
    * 
    * @see org.melati.poem.Table#columnWithColumnInfoID(int)
    */
   public void testColumnWithColumnInfoID() {
   }
 
   /**
    * @see org.melati.poem.Table#troidColumn()
    */
   public void testTroidColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#deletedColumn()
    */
   public void testDeletedColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#displayColumn()
    */
   public void testDisplayColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#setDisplayColumn(Column)
    */
   public void testSetDisplayColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#primaryCriterionColumn()
    */
   public void testPrimaryCriterionColumn() {
     assertEquals("name", getDb().getUserTable().primaryCriterionColumn().getName());
   }
 
   /**
    * @see org.melati.poem.Table#setSearchColumn(Column)
    */
   public void testSetSearchColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#defaultOrderByClause()
    */
   public void testDefaultOrderByClause() {
 
   }
 
   /**
    * @see org.melati.poem.Table#notifyColumnInfo(ColumnInfo)
    */
   public void testNotifyColumnInfo() {
 
   }
 
   /**
    * @see org.melati.poem.Table#displayColumns(DisplayLevel)
    */
   public void testDisplayColumns() {
 
   }
 
   /**
    * @see org.melati.poem.Table#displayColumnsCount(DisplayLevel)
    */
   public void testDisplayColumnsCount() {
     assertEquals(1, getDb().getUserTable().displayColumnsCount(DisplayLevel.primary));
   }
 
   /**
    * @see org.melati.poem.Table#getDetailDisplayColumns()
    */
   public void testGetDetailDisplayColumns() {
     Enumeration them = getDb().getUserTable().getDetailDisplayColumns();
     int counter = 0;
     while (them.hasMoreElements()) {
       them.nextElement();
       counter++;
     }
     assertEquals(4, counter);
   }
 
   /**
    * @see org.melati.poem.Table#getDetailDisplayColumnsCount()
    */
   public void testGetDetailDisplayColumnsCount() {
     assertEquals(4, getDb().getUserTable().getDetailDisplayColumnsCount());
   }
 
   /**
    * @see org.melati.poem.Table#getRecordDisplayColumns()
    */
   public void testGetRecordDisplayColumns() {
     Enumeration them = getDb().getUserTable().getRecordDisplayColumns();
     int counter = 0;
     while (them.hasMoreElements()) {
       them.nextElement();
       counter++;
     }
     assertEquals(3, counter);
   }
 
   /**
    * @see org.melati.poem.Table#getRecordDisplayColumnsCount()
    */
   public void testGetRecordDisplayColumnsCount() {
     assertEquals(3, getDb().getUserTable().getRecordDisplayColumnsCount());
   }
 
   /**
    * @see org.melati.poem.Table#getSummaryDisplayColumns()
    */
   public void testGetSummaryDisplayColumns() {
     Enumeration them = getDb().getUserTable().getSummaryDisplayColumns();
     int counter = 0;
     while (them.hasMoreElements()) {
       them.nextElement();
       counter++;
     }
     assertEquals(2, counter);
   }
 
   /**
    * @see org.melati.poem.Table#getSummaryDisplayColumnsCount()
    */
   public void testGetSummaryDisplayColumnsCount() {
     assertEquals(2, getDb().getUserTable().getSummaryDisplayColumnsCount());
   }
 
   /**
    * @see org.melati.poem.Table#getSearchCriterionColumns()
    */
   public void testGetSearchCriterionColumns() {
 
   }
 
   /**
    * Not used in the java, possibly in templates.
    * @see org.melati.poem.Table#getSearchCriterionColumnsCount()
    */
   public void testGetSearchCriterionColumnsCount() {
     assertEquals(3, getDb().getUserTable().getSearchCriterionColumnsCount());
   }
 
   /**
    * @see org.melati.poem.Table#dbModifyStructure(String)
    */
   public void testDbModifyStructure() {
 
   }
 
   /**
    * @see org.melati.poem.Table#load(PoemTransaction, Persistent)
    */
   public void testLoad() {
 
   }
 
   /**
    * @see org.melati.poem.Table#delete(Integer, PoemTransaction)
    */
   public void testDelete() {
 
   }
 
   /**
    * @see org.melati.poem.Table#writeDown(PoemTransaction, Persistent)
    */
   public void testWriteDown() {
 
   }
 
   /**
    * @see org.melati.poem.Table#uncacheContents()
    */
   public void testUncacheContents() {
 
   }
 
   /**
    * @see org.melati.poem.Table#trimCache(int)
    */
   public void testTrimCache() {
 
   }
 
   /**
    * Used in cache dump servlet.
    * @see org.melati.poem.Table#getCacheInfo()
    */
   public void testGetCacheInfo() {
     getDb().uncacheContents();
     Enumeration them = getDb().getUserTable().getCacheInfo().getHeldElements();
     int counter = 0;
     while(them.hasMoreElements()){
       them.nextElement();
       counter++;
     }
     assertEquals(2,counter);
   }
 
   /**
    * @see org.melati.poem.Table#addListener(TableListener)
    */
   public void testAddListener() {
 
   }
 
   /**
    * @see org.melati.poem.Table#notifyTouched(PoemTransaction, Persistent)
    */
   public void testNotifyTouched() {
 
   }
 
   /**
    * @see org.melati.poem.Table#serial(PoemTransaction)
    */
   public void testSerial() {
 
   }
 
   /**
    * @see org.melati.poem.Table#readLock()
    */
   public void testReadLock() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getObject(Integer)
    */
   public void testGetObjectInteger() {
     int count1 = getDb().getQueryCount();
     User u = getDb().guestUser();
     int count2 = getDb().getQueryCount();
     UserTable ut = getDb().getUserTable();
     int count3 = getDb().getQueryCount();
     User u2 = (User)ut.getObject(new Integer(0));
     int count4 = getDb().getQueryCount();
     User u3 = (User)ut.getObject(new Integer(0));
     int count5 = getDb().getQueryCount();
     assertEquals(u,u2);
     System.err.println(u3.getName());
     int count6 = getDb().getQueryCount();
     System.err.println(count1 + ":" + count2 + ":" +  count3 + ":" + count4 + ":"+  count5 + ":" + count6);    
   }
 
   /**
    * @see org.melati.poem.Table#getObject(int)
    */
   public void testGetObjectInt() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selectionSQL(String, String, boolean)
    */
   public void testSelectionSQLStringStringBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selectionSQL(String, String, String, boolean,
    *      boolean)
    */
   public void testSelectionSQLStringStringStringBooleanBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#troidSelection(String, String, boolean,
    *      PoemTransaction)
    */
   public void testTroidSelectionStringStringBooleanPoemTransaction() {
 
   }
 
   /**
    * @see org.melati.poem.Table#troidSelection(Persistent, String, boolean,
    *      boolean, PoemTransaction)
    */
   public void testTroidSelectionPersistentStringBooleanBooleanPoemTransaction() {
 
   }
 
   /**
    * @see org.melati.poem.Table#rememberAllTroids(boolean)
    */
   public void testRememberAllTroids() {
 
   }
 
   /**
    * @see org.melati.poem.Table#setCacheLimit(Integer)
    */
   public void testSetCacheLimit() {
 
   }
 
   /**
    * @see org.melati.poem.Table#troidSelection(String, String, boolean)
    */
   public void testTroidSelectionStringStringBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selection()
    */
   public void testSelection() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selection(String)
    */
   public void testSelectionString() {
 
   }
 
   /**
    * @see org.melati.poem.Table#firstSelection(String)
    */
   public void testFirstSelection() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selection(String, String, boolean)
    */
   public void testSelectionStringStringBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selection(Persistent)
    */
   public void testSelectionPersistent() {
     User exemplar = (User)getDb().getUserTable().newPersistent();
     Enumeration found  = getDb().getUserTable().selection(exemplar);
     int count = 0;
     User result = null;
     while (found.hasMoreElements()) {
       count++;
       result = (User)found.nextElement();
       //System.err.println(result);
     }
     assertNotNull(result);
     assertEquals(2, count);
     exemplar.setLogin("_administrator_");
     result = null;
     count = 0;
     found  = getDb().getUserTable().selection(exemplar, 
         getDb().getUserTable().getNameColumn().fullQuotedName());
     while (found.hasMoreElements()) {
       count++;
       result = (User)found.nextElement();
       //System.err.println(result);
     }
     assertNotNull(result);
     assertEquals(1, count);
 
     exemplar.setLogin("notSet");
     result = null;
     count = 0;
     found  = getDb().getUserTable().selection(exemplar);
     while (found.hasMoreElements()) {
       count++;
       result = (User)found.nextElement();
      //System.err.println(result);
     }
     assertNull(result);
     assertEquals(0, count);
   }
 
   /**
    * @see org.melati.poem.Table#selection(Persistent, String)
    */
   public void testSelectionPersistentString() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selection(Persistent, String, boolean, boolean)
    */
   public void testSelectionPersistentStringBooleanBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#selection(String, String, boolean, int, int)
    */
   public void testSelectionStringStringBooleanIntInt() {
     Enumeration en = getDb().getUserTable().selection(null, null, false, 1, 10);
     assertEquals(2, EnumUtils.vectorOf(en).size());
   }
 
   /**
    * @see org.melati.poem.Table#selection(Persistent, String, boolean, boolean,
    *      int, int)
    */
   public void testSelectionPersistentStringBooleanBooleanIntInt() {
     Enumeration en = getDb().getUserTable().selection(
             getDb().getUserTable().newPersistent(), null, false, true, 1, 10);
     assertEquals(2, EnumUtils.vectorOf(en).size());
 
   }
 
   /**
    * @see org.melati.poem.Table#countSQL(String)
    */
   public void testCountSQLString() {
 
   }
 
   /**
    * @see org.melati.poem.Table#countSQL(String, String, boolean, boolean)
    */
   public void testCountSQLStringStringBooleanBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#count(String, boolean, boolean)
    */
   public void testCountStringBooleanBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#count(String, boolean)
    */
   public void testCountStringBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#count(String)
    */
   public void testCountString() {
 
   }
 
   /**
    * @see org.melati.poem.Table#count()
    */
   public void testCount() {
 
   }
 
   /**
    * @see org.melati.poem.Table#exists(String)
    */
   public void testExistsString() {
 
   }
 
   /**
    * @see org.melati.poem.Table#exists(Persistent)
    */
   public void testExistsPersistent() {
 
   }
 
   /**
    * @see org.melati.poem.Table#appendWhereClause(StringBuffer, Persistent)
    */
   public void testAppendWhereClause() {
 
   }
 
   /**
    * @see org.melati.poem.Table#whereClause(Persistent)
    */
   public void testWhereClausePersistent() {
 
   }
 
   /**
    * @see org.melati.poem.Table#whereClause(Persistent, boolean, boolean)
    */
   public void testWhereClausePersistentBooleanBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#cnfWhereClause(Enumeration)
    */
   public void brokentestCnfWhereClauseEnumeration() {
     String cnf = getDb().getUserTable().cnfWhereClause(
             getDb().getUserTable().selection());
    String expected = "((" +  getDb().getDbms().getQuotedName("user") +"." + getDb().getDbms().getQuotedName("id") + 
    " = 0 AND " +
    getDb().getDbms().caseInsensitiveRegExpSQL(
            getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("name"), "Melati guest user") + 
            " AND " +
    getDb().getDbms().caseInsensitiveRegExpSQL(
            getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("login"),"_guest_") + 
            " AND " +
    getDb().getDbms().caseInsensitiveRegExpSQL(
            getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("password"),"guest") +
     ") OR" +
    " (" + getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("id") + " = 1 AND " +
    getDb().getDbms().caseInsensitiveRegExpSQL(
            getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("name"),"Melati database administrator") + " AND " +
    getDb().getDbms().caseInsensitiveRegExpSQL(
            getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("login"),"_administrator_") + " AND " +
    getDb().getDbms().caseInsensitiveRegExpSQL(
            getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("password"),"FIXME") + "))";
     assertEquals(expected.toUpperCase(),
             cnf.toUpperCase());
     cnf = getDb().getUserTable().cnfWhereClause(
             EmptyEnumeration.it);
     assertEquals("", cnf);
     Vector v = new Vector();
     v.addElement(getDb().getUserTable().newPersistent());
     cnf = getDb().getUserTable().cnfWhereClause(
             v.elements());
     assertEquals("", cnf);
     //getDb().getUserTable().selection(cnf);
     
     
   }
 
   /**
    * @see org.melati.poem.Table#cnfWhereClause(Enumeration, boolean, boolean)
    */
   public void testCnfWhereClauseEnumerationBooleanBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#referencesTo(Persistent)
    */
   public void testReferencesToPersistent() {
 
   }
 
   /**
    * @see org.melati.poem.Table#referencesTo(Table)
    */
   public void testReferencesToTable() {
 
   }
 
   /**
    * @see org.melati.poem.Table#troidFor(Persistent)
    */
   public void testTroidFor() {
 
   }
 
   /**
    * @see org.melati.poem.Table#create(Persistent)
    */
   public void testCreatePersistent() {
 
   }
 
   /**
    * @see org.melati.poem.Table#create(Initialiser)
    */
   public void testCreateInitialiser() {
 
   }
 
   /**
    * @see org.melati.poem.Table#newPersistent()
    */
   public void testNewPersistent() {
 
   }
 
   /**
    * @see org.melati.poem.Table#_newPersistent()
    */
   public void test_newPersistent() {
 
   }
 
   /**
    * Looks like you can sucessfully delete 
    * the same record twice.
    * @see org.melati.poem.Table#delete_unsafe(String)
    */
   public void testDelete_unsafe() {
     User tester = (User)getDb().getUserTable().newPersistent();
     tester.setName("tester");
     tester.setLogin("tester");
     tester.setPassword("pwd");
     tester.makePersistent();
     getDb().getUserTable().delete_unsafe(
             getDb().getUserTable().getNameColumn().fullQuotedName()
             + " = 'tester'"
             );
     PoemThread.commit();
     //System.err.println(tester.dump());
     tester.delete();
     tester = (User)getDb().getUserTable().getNameColumn().firstWhereEq("tester");
     assertNull(tester);
   }
 
   /**
    * @see org.melati.poem.Table#extrasCount()
    */
   public void testExtrasCount() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getDefaultCanRead()
    */
   public void testGetDefaultCanRead() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getDefaultCanWrite()
    */
   public void testGetDefaultCanWrite() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getDefaultCanDelete()
    */
   public void testGetDefaultCanDelete() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getCanCreate()
    */
   public void testGetCanCreate() {
 
   }
 
   /**
    * @see org.melati.poem.Table#canReadColumn()
    */
   public void testCanReadColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#canSelectColumn()
    */
   public void testCanSelectColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#canWriteColumn()
    */
   public void testCanWriteColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#canDeleteColumn()
    */
   public void testCanDeleteColumn() {
 
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitTroid()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitTroid() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitDeleted()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDeleted() throws Exception {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitType()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitType() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitBoolean()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitBoolean() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitInteger()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitInteger() {
   }
   /**
    * @see DynamicTableTest#testAddColumnAndCommitNullableInteger()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitNullableInteger() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitDouble()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDouble() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitLong()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitLong() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitBigDecimal()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitBigDecimal() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitString()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitString() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitPassword()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitPassword() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitDate()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDate() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitTimestamp()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitTimestamp() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitBinary()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitBinary() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitDisplayLevel()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDisplaylevel() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitSearchability()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitSearchability() {
   }
 
   /**
    * @see DynamicTableTest#testAddColumnAndCommitIntegrityfix()
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitIntegrityfix() {
   }
 
   /**
    * @see org.melati.poem.Table#toString()
    */
   public void testToString() {
     Table ut = getDb().getUserTable();
     assertEquals("user (from the data structure definition)", ut.toString());
 
   }
 
   /**
    * @see org.melati.poem.Table#dumpCacheAnalysis()
    */
   public void testDumpCacheAnalysis() {
 
   }
 
   /**
    * @see org.melati.poem.Table#dump()
    */
   public void testDump() {
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     PrintStream ps = new PrintStream(baos);
     getDb().getUserTable().dump(ps);
     System.err.println(baos.toString());
     assertTrue(baos.toString().startsWith("=== table user (tableinfo id 0"));
   }
 
   /**
    * @see org.melati.poem.Table#cachedSelection(String, String)
    */
   public void testCachedSelection() {
 
   }
 
   /**
    * @see org.melati.poem.Table#cachedCount(String, boolean)
    */
   public void testCachedCountStringBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#cachedCount(String, boolean, boolean)
    */
   public void testCachedCountStringBooleanBoolean() {
   }
 
   /**
    * Ensure that sqlBooleanValue is tested.
    * @see org.melati.poem.Table#cachedCount(Persistent, boolean, boolean)
    */
   public void testCachedCountPersistentBooleanBoolean() {
     TableInfo ti = (TableInfo)getDb().getTableInfoTable().newPersistent();
     CachedCount cached = getDb().getTableInfoTable().cachedCount(ti,false,false); 
     assertEquals(9, cached.count());
     ti.setSeqcached(true);
     cached = getDb().getTableInfoTable().cachedCount(ti,false,false); 
     assertEquals(7, cached.count());
   }
   
 
   /**
    * @see org.melati.poem.Table#cachedCount(String)
    */
   public void testCachedCountString() {
 
   }
 
   /**
    * @see org.melati.poem.Table#cachedCount(String)
    */
   public void testCachedCountPersistent() {
     TableInfo ti = (TableInfo)getDb().getTableInfoTable().newPersistent();
     CachedCount cached = getDb().getTableInfoTable().cachedCount(ti); 
     assertEquals(9, cached.count());
     ti.setSeqcached(true);
     cached = getDb().getTableInfoTable().cachedCount(ti); 
     assertEquals(7, cached.count());
   }
 
   /**
    * @see org.melati.poem.Table#cachedExists(String)
    */
   public void testCachedExists() {
     CachedExists cached = getDb().getUserTable().cachedExists(null); 
     assertEquals(2, cached.count());
     User t = (User)getDb().getUserTable().newPersistent();
     t.setName("TestUser");
     t.setLogin("TestUser");
     t.setPassword("TestUser");
     t.makePersistent();
     assertEquals(3, cached.count());
     t.delete();
   }
 
   /**
    * @see org.melati.poem.Table#cachedSelectionType(String, String, boolean)
    */
   public void testCachedSelectionType() {
 
   }
 
   /**
    * @see org.melati.poem.Table#cachedSelectionField(String, String, boolean,
    *      Integer, String)
    */
   public void testCachedSelectionField() {
     // getDb().setLogSQL(true);
     String whereClause = getDb().getTableInfoTable().getDisplayorderColumn().fullQuotedName() + 
     "  <3000 AND " + getDb().getTableInfoTable().getDisplayorderColumn().fullQuotedName() + "  > 2000";
     Field userTableFields = getDb().getTableInfoTable()
         .cachedSelectionField(whereClause, null,
             true, null, "userTables");
     Enumeration them = userTableFields.getPossibilities();
     assertEquals("userTables: ", them.nextElement().toString());
     assertEquals("userTables: User", them.nextElement().toString());
     assertEquals("userTables: Group", them.nextElement().toString());
     assertEquals("userTables: Capability",them.nextElement().toString());
     assertEquals("userTables: Group membership",them.nextElement().toString());
     assertEquals("userTables: Group capability",them.nextElement().toString());
     assertFalse(them.hasMoreElements());
     assertEquals(null, userTableFields.getRaw());
 
     // with order by
     userTableFields = getDb().getTableInfoTable()
     .cachedSelectionField(whereClause, 
             getDb().getTableInfoTable().getDisplaynameColumn().fullQuotedName(), true, null, "userTables");
     them = userTableFields.getPossibilities();
     assertEquals(them.nextElement().toString(), "userTables: ");
     assertEquals(them.nextElement().toString(), "userTables: Capability");
     assertEquals(them.nextElement().toString(), "userTables: Group");
     assertEquals(them.nextElement().toString(), "userTables: Group capability");
     assertEquals(them.nextElement().toString(), "userTables: Group membership");
     assertEquals(them.nextElement().toString(), "userTables: User");
     assertFalse(them.hasMoreElements());
     assertEquals(null, userTableFields.getRaw());
 
     // without null option
     userTableFields = getDb().getTableInfoTable().cachedSelectionField(
         whereClause,
         getDb().getTableInfoTable().getDisplaynameColumn().fullQuotedName(), false, null, "userTables");
     them = userTableFields.getPossibilities();
     assertEquals(them.nextElement().toString(), "userTables: Capability");
     assertEquals(them.nextElement().toString(), "userTables: Group");
     assertEquals(them.nextElement().toString(), "userTables: Group capability");
     assertEquals(them.nextElement().toString(), "userTables: Group membership");
     assertEquals(them.nextElement().toString(), "userTables: User");
     assertFalse(them.hasMoreElements());
     assertEquals(null, userTableFields.getRaw());
 
     // with a troid
     userTableFields = getDb().getTableInfoTable().cachedSelectionField(
         whereClause,
         getDb().getTableInfoTable().getDisplaynameColumn().fullQuotedName(), false, new Integer(0), "userTables");
     them = userTableFields.getPossibilities();
     assertEquals(them.nextElement().toString(), "userTables: Capability");
     assertEquals(them.nextElement().toString(), "userTables: Group");
     assertEquals(them.nextElement().toString(), "userTables: Group capability");
     assertEquals(them.nextElement().toString(), "userTables: Group membership");
     assertEquals(them.nextElement().toString(), "userTables: User");
     assertFalse(them.hasMoreElements());
     assertEquals(new Integer(0), userTableFields.getRaw());
   }
 
   /**
    * @see org.melati.poem.Table#defineColumn(Column, boolean)
    */
   public void testDefineColumnColumnBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#defineColumn(Column)
    */
   public void testDefineColumnColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Table#setTableInfo(TableInfo)
    */
   public void testSetTableInfo() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getTableInfo()
    */
   public void testGetTableInfo() {
 
   }
 
   /**
    * @see org.melati.poem.Table#defaultDisplayName()
    */
   public void testDefaultDisplayName() {
 
   }
 
   /**
    * @see org.melati.poem.Table#getDsdName()
    */
   public void testGetDsdName() {
     assertEquals("User",getDb().getUserTable().getDsdName());
   }
 
   /**
    * @see org.melati.poem.Table#defaultDisplayOrder()
    */
   public void testDefaultDisplayOrder() {
 
   }
 
   /**
    * @see org.melati.poem.Table#defaultDescription()
    */
   public void testDefaultDescription() {
 
   }
 
   /**
    * @see org.melati.poem.Table#defaultCacheLimit()
    */
   public void testDefaultCacheLimit() {
 
   }
 
   /**
    * @see org.melati.poem.Table#defaultRememberAllTroids()
    */
   public void testDefaultRememberAllTroids() {
 
   }
 
   /**
    * @see org.melati.poem.Table#defaultCategory()
    */
   public void testDefaultCategory() {
 
   }
 
   /**
    * @see org.melati.poem.Table#createTableInfo()
    */
   public void testCreateTableInfo() {
 
   }
 
   /**
    * @see org.melati.poem.Table#unifyWithColumnInfo()
    */
   public void testUnifyWithColumnInfo() {
 
   }
 
   /**
    * @see org.melati.poem.Table#unifyWithDB(ResultSet)
    */
   public void testUnifyWithDB() {
 
   }
 
   /**
    * @see org.melati.poem.Table#init()
    */
   public void testInit() {
 
   }
 
   /**
    * @see org.melati.poem.Table#hashCode()
    */
   public void testHashCode() {
     Table ut = getDb().getUserTable();
     Table ut2 = getDb().getUserTable();
     assertTrue(ut.equals(ut2));
     assertEquals(ut.hashCode(), ut2.hashCode());
   }
 
   /**
    * @see org.melati.poem.Table#equals(Object)
    */
   public void testEqualsObject() {
     Table ut = getDb().getUserTable();
     Table ct = getDb().getCapabilityTable();
     assertFalse(ut.equals(ct));
     Table ut2 = getDb().getUserTable();
     assertTrue(ut.equals(ut2));
     assertTrue(ut2.equals(ut));
     assertTrue(ut.equals(ut));
     assertFalse(ut.equals(null));
     assertFalse(ut.equals(new Object()));
 
   }
 
 }
