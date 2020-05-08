 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Enumeration;
 
 import org.melati.poem.ColumnInfo;
 import org.melati.poem.ColumnRenamePoemException;
 import org.melati.poem.DisplayLevel;
 import org.melati.poem.DuplicateColumnNamePoemException;
 import org.melati.poem.DuplicateDeletedColumnPoemException;
 import org.melati.poem.DuplicateTroidColumnPoemException;
 import org.melati.poem.Field;
 import org.melati.poem.Initialiser;
 import org.melati.poem.IntegrityFix;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemThread;
 import org.melati.poem.PoemTypeFactory;
 import org.melati.poem.Searchability;
 import org.melati.poem.StandardIntegrityFix;
 import org.melati.poem.Table;
 import org.melati.poem.TableInfo;
 import org.melati.poem.User;
 import org.melati.poem.UserTable;
 import org.melati.util.EnumUtils;
 
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
     setDbName("poemtest");
   }
 
   /**
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
     setDbName("poemtest");
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
     assertEquals("\"USER\"", ut.quotedName());
 
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
     found  = getDb().getUserTable().selection(exemplar);
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
 
   }
 
   /**
    * @see org.melati.poem.Table#selection(Persistent, String, boolean, boolean,
    *      int, int)
    */
   public void testSelectionPersistentStringBooleanBooleanIntInt() {
 
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
   public void testCnfWhereClauseEnumeration() {
 
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
    * @see org.melati.poem.Table#delete_unsafe(String)
    */
   public void testDelete_unsafe() {
 
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
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitTroid() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testtroidcol");
     columnInfo.setDisplayname("Test String Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Troid column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.TROID);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.detail);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     try {
       columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
       fail("Should have blown up");
     } catch (DuplicateTroidColumnPoemException e) {
       e = null;
     }
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDeleted() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testdeletedcol");
     columnInfo.setDisplayname("Test Deleted Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Deleted column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.DELETED);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdeletedcol").selectionWhereEq(Boolean.FALSE)).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdeletedcol").selectionWhereEq(Boolean.FALSE)).size());
     assertEquals(Boolean.FALSE, ut.administratorUser().getRaw("testdeletedcol"));
     assertEquals(Boolean.FALSE, ut.administratorUser().getCooked(
         "testdeletedcol"));
     assertEquals(Boolean.FALSE, ut.getObject(0).getCooked("testdeletedcol"));
     try {
       columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
       fail("Should have blown up");
     } catch (DuplicateColumnNamePoemException e) {
       assertEquals("Can't add duplicate column user.testdeletedcol: "
           + "deleted (BOOLEAN (org.melati.poem.DeletedPoemType)) "
           + "(from the running application) to user "
           + "(from the data structure definition)", e.getMessage());
       e = null;
     }
     try {
       columnInfo.setName("testdeletedcol2");
     } catch (ColumnRenamePoemException e) {
       e = null;
     }
     ColumnInfo columnInfo2 = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     columnInfo2.setTableinfo(ti);
     columnInfo2.setName("testdeletedcol2");
     columnInfo2.setDisplayname("Test duplicate Deleted Column");
     columnInfo2.setDisplayorder(99);
     columnInfo2.setSearchability(Searchability.yes);
     columnInfo2.setIndexed(false);
     columnInfo2.setUnique(false);
     columnInfo2.setDescription("A non-nullable extra Deleted column");
     columnInfo2.setUsercreateable(true);
     columnInfo2.setUsereditable(true);
     columnInfo2.setTypefactory(PoemTypeFactory.DELETED);
     columnInfo2.setSize(-1);
     columnInfo2.setWidth(20);
     columnInfo2.setHeight(1);
     columnInfo2.setPrecision(0);
     columnInfo2.setScale(0);
     columnInfo2.setNullable(false);
     columnInfo2.setDisplaylevel(DisplayLevel.summary);
     columnInfo2.makePersistent();
     try {
       columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo2);
       fail("Should have blown up");
     } catch (DuplicateDeletedColumnPoemException e) {
       assertEquals("Can't add testdeletedcol2 to user as a deleted column, "
           + "because it already has one, "
           + "i.e. user.testdeletedcol: deleted "
           + "(BOOLEAN (org.melati.poem.DeletedPoemType)) "
           + "(from the running application)", e.getMessage());
       e = null;
     }
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitType() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testtypecol");
     columnInfo.setDisplayname("Test Type Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Type column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.TYPE);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     Integer t = null;
     Enumeration en = ut.selection();
     t = (Integer) ((User) en.nextElement()).getRaw("testtypecol");
     while (en.hasMoreElements()) {
       assertEquals(t, (Integer) ((User) en.nextElement()).getRaw("testtypecol"));
     }
 
     PoemTypeFactory t2 = null;
     Enumeration en2 = ut.selection();
     t2 = (PoemTypeFactory) ((User) en2.nextElement()).getCooked("testtypecol");
     while (en2.hasMoreElements()) {
       System.err.println(t2.getName());
       assertEquals(t2.getName(), ((PoemTypeFactory) ((User) en2.nextElement())
           .getCooked("testtypecol")).getName());
     }
 
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testtypecol").selectionWhereEq(new Integer(0))).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testtypecol").selectionWhereEq(new Integer(0))).size());
     assertEquals(new Integer(0), ut.administratorUser().getRaw("testtypecol"));
     assertEquals("user", ((PoemTypeFactory) ut.administratorUser().getCooked(
         "testtypecol")).getName());
     assertEquals("user", ((PoemTypeFactory) ut.getObject(0).getCooked(
         "testtypecol")).getName());
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitBoolean() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testbooleancol");
     columnInfo.setDisplayname("Test Boolean Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Boolean column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.BOOLEAN);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testbooleancol").selectionWhereEq(Boolean.FALSE)).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testbooleancol").selectionWhereEq(Boolean.FALSE)).size());
     assertEquals(Boolean.FALSE, ut.administratorUser().getRaw("testbooleancol"));
     assertEquals(Boolean.FALSE, ut.administratorUser().getCooked(
         "testbooleancol"));
     assertEquals(Boolean.FALSE, ut.getObject(0).getCooked("testbooleancol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitInteger() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testintegercol");
     columnInfo.setDisplayname("Test Integer Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Integer column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.INTEGER);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testintegercol").selectionWhereEq(new Integer(0))).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testintegercol").selectionWhereEq(new Integer(0))).size());
     assertEquals(new Integer(0), ut.administratorUser()
         .getRaw("testintegercol"));
     assertEquals(new Integer(0), ut.administratorUser().getCooked(
         "testintegercol"));
     assertEquals(new Integer(0), ut.getObject(0).getCooked("testintegercol"));
     getDb().setLogSQL(false);
   }
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitNullableInteger() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testnullableintegercol");
     columnInfo.setDisplayname("Test Nullable Integer Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A nullable extra Integer column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.INTEGER);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(true);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(0, EnumUtils.vectorOf(
         ut.getColumn("testnullableintegercol").selectionWhereEq(new Integer(0))).size());
     PoemThread.commit();
     assertEquals(0, EnumUtils.vectorOf(
         ut.getColumn("testnullableintegercol").selectionWhereEq(new Integer(0))).size());
     assertNull(ut.administratorUser()
         .getRaw("testnullableintegercol"));
     assertNull(ut.administratorUser().getCooked(
         "testnullableintegercol"));
     assertNull(ut.getObject(0).getCooked("testnullableintegercol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDouble() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testdoublecol");
     columnInfo.setDisplayname("Test Double Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Double column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.DOUBLE);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdoublecol").selectionWhereEq(new Double(0))).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdoublecol").selectionWhereEq(new Double(0))).size());
     assertEquals(new Double(0), ut.administratorUser().getRaw("testdoublecol"));
     assertEquals(new Double(0), ut.administratorUser().getCooked(
         "testdoublecol"));
     assertEquals(new Double(0), ut.getObject(0).getCooked("testdoublecol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitLong() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testlongcol");
     columnInfo.setDisplayname("Test Long Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Long column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.LONG);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testlongcol").selectionWhereEq(new Long(0))).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testlongcol").selectionWhereEq(new Long(0))).size());
     assertEquals(new Long(0), ut.administratorUser().getRaw("testlongcol"));
     assertEquals(new Long(0), ut.administratorUser().getCooked("testlongcol"));
     assertEquals(new Long(0), ut.getObject(0).getCooked("testlongcol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitBigDecimal() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testbigdecimalcol");
     columnInfo.setDisplayname("Test Big Decimal Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Big Decimal column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.BIGDECIMAL);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
        ut.getColumn("testbigdecimalcol").selectionWhereEq(new BigDecimal(0)))
         .size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
        ut.getColumn("testbigdecimalcol").selectionWhereEq(new BigDecimal(0)))
         .size());
     assertEquals(new BigDecimal(0), ut.administratorUser().getRaw(
         "testbigdecimalcol"));
     assertEquals(new BigDecimal(0), ut.administratorUser().getCooked(
         "testbigdecimalcol"));
     assertEquals(new BigDecimal(0), ut.getObject(0).getCooked(
         "testbigdecimalcol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitString() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("teststringcol");
     columnInfo.setDisplayname("Test String Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra String column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.STRING);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("teststringcol").selectionWhereEq("default")).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("teststringcol").selectionWhereEq("default")).size());
     assertEquals("default", ut.administratorUser().getRaw("teststringcol"));
     assertEquals("default", ut.administratorUser().getCooked("teststringcol"));
     assertEquals("default", ut.getObject(0).getCooked("teststringcol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitPassword() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testpasswordcol");
     columnInfo.setDisplayname("Test Password Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Password column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.PASSWORD);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testpasswordcol").selectionWhereEq("default")).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testpasswordcol").selectionWhereEq("default")).size());
     assertEquals("default", ut.administratorUser().getRaw("testpasswordcol"));
     assertEquals("default", ut.administratorUser().getCooked("testpasswordcol"));
     assertEquals("default", ut.getObject(0).getCooked("testpasswordcol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDate() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testdatecol");
     columnInfo.setDisplayname("Test Date Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Date column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.DATE);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdatecol").selectionWhereEq(
             new java.sql.Date(new Date().getTime()))).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdatecol").selectionWhereEq(
             new java.sql.Date(new Date().getTime()))).size());
     assertEquals(new java.sql.Date(new Date().getTime()).toString(), ut
         .administratorUser().getRaw("testdatecol").toString());
     assertEquals(new java.sql.Date(new Date().getTime()).toString(), ut
         .administratorUser().getCooked("testdatecol").toString());
     assertEquals(new java.sql.Date(new Date().getTime()).toString(), ut
         .getObject(0).getCooked("testdatecol").toString());
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitTimestamp() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testtimestampcol");
     columnInfo.setDisplayname("Test Timestamp Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Timestamp column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.TIMESTAMP);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     Timestamp t = null;
     Enumeration en = ut.selection();
     t = (Timestamp) ((User) en.nextElement()).getRaw("testtimestampcol");
     while (en.hasMoreElements()) {
       assertEquals(t, (Timestamp) ((User) en.nextElement())
           .getRaw("testtimestampcol"));
     }
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testtimestampcol").selectionWhereEq(t)).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testtimestampcol").selectionWhereEq(t)).size());
     assertEquals(t, ut.administratorUser().getRaw("testtimestampcol"));
     assertEquals(t, ut.administratorUser().getCooked("testtimestampcol"));
     assertEquals(t, ut.getObject(0).getCooked("testtimestampcol"));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitBinary() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testbinarycol");
     columnInfo.setDisplayname("Test Binary Column");
     columnInfo.setDisplayorder(199);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Binary column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.BINARY);
     columnInfo.setSize(8);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     byte[] t = null;
     Enumeration en = ut.selection();
     t = (byte[]) ((User) en.nextElement()).getRaw("testbinarycol");
     while (en.hasMoreElements()) {
       assertEquals(t.length, ((byte[]) ((User) en.nextElement())
           .getRaw("testbinarycol")).length);
     }
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testbinarycol").selectionWhereEq(t)).size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testbinarycol").selectionWhereEq(t)).size());
    assertEquals(t, ut.administratorUser().getRaw("testbinarycol"));
    assertEquals(t, ut.administratorUser().getCooked("testbinarycol"));
     assertEquals(t.length,
         ((byte[]) ut.getObject(0).getCooked("testbinarycol")).length);
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitDisplaylevel() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testdisplaylevelcol");
     columnInfo.setDisplayname("Test Displaylevel Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Displaylevel column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.DISPLAYLEVEL);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     Integer t = null;
     Enumeration en = ut.selection();
     t = (Integer) ((User) en.nextElement()).getRaw("testdisplaylevelcol");
     while (en.hasMoreElements()) {
       assertEquals(t, (Integer) ((User) en.nextElement())
           .getRaw("testdisplaylevelcol"));
     }
 
     DisplayLevel t2 = null;
     Enumeration en2 = ut.selection();
     t2 = (DisplayLevel) ((User) en2.nextElement())
         .getCooked("testdisplaylevelcol");
     while (en2.hasMoreElements()) {
       System.err.println(t2);
       assertEquals(t2, ((DisplayLevel) ((User) en2.nextElement())
           .getCooked("testdisplaylevelcol")));
     }
 
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdisplaylevelcol").selectionWhereEq(new Integer(0)))
         .size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testdisplaylevelcol").selectionWhereEq(new Integer(0)))
         .size());
     assertEquals(new Integer(0), ut.administratorUser().getRaw(
         "testdisplaylevelcol"));
     assertEquals(DisplayLevel.primary, ((DisplayLevel) ut.administratorUser()
         .getCooked("testdisplaylevelcol")));
     assertEquals(DisplayLevel.primary, ((DisplayLevel) ut.getObject(0)
         .getCooked("testdisplaylevelcol")));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitSearchability() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testsearchabilitycol");
     columnInfo.setDisplayname("Test searchability Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Searchability column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.SEARCHABILITY);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     Integer t = null;
     Enumeration en = ut.selection();
     t = (Integer) ((User) en.nextElement()).getRaw("testsearchabilitycol");
     while (en.hasMoreElements()) {
       assertEquals(t, (Integer) ((User) en.nextElement())
           .getRaw("testsearchabilitycol"));
     }
 
     Searchability t2 = null;
     Enumeration en2 = ut.selection();
     t2 = (Searchability) ((User) en2.nextElement())
         .getCooked("testsearchabilitycol");
     while (en2.hasMoreElements()) {
       System.err.println(t2);
       assertEquals(t2, ((Searchability) ((User) en2.nextElement())
           .getCooked("testsearchabilitycol")));
     }
 
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testsearchabilitycol").selectionWhereEq(new Integer(0)))
         .size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testsearchabilitycol").selectionWhereEq(new Integer(0)))
         .size());
     assertEquals(new Integer(0), ut.administratorUser().getRaw(
         "testsearchabilitycol"));
     assertEquals(Searchability.primary, ((Searchability) ut.administratorUser()
         .getCooked("testsearchabilitycol")));
     assertEquals(Searchability.primary, ((Searchability) ut.getObject(0)
         .getCooked("testsearchabilitycol")));
     getDb().setLogSQL(false);
   }
 
   /**
    * @see org.melati.poem.Table#addColumnAndCommit(ColumnInfo)
    */
   public void testAddColumnAndCommitIntegrityfix() {
     UserTable ut = getDb().getUserTable();
     ColumnInfo columnInfo = (ColumnInfo) getDb().getColumnInfoTable()
         .newPersistent();
     TableInfo ti = ut.getTableInfo();
     columnInfo.setTableinfo(ti);
     columnInfo.setName("testintegrityfixcol");
     columnInfo.setDisplayname("Test Integrityfix Column");
     columnInfo.setDisplayorder(99);
     columnInfo.setSearchability(Searchability.yes);
     columnInfo.setIndexed(false);
     columnInfo.setUnique(false);
     columnInfo.setDescription("A non-nullable extra Integrityfix column");
     columnInfo.setUsercreateable(true);
     columnInfo.setUsereditable(true);
     columnInfo.setTypefactory(PoemTypeFactory.INTEGRITYFIX);
     columnInfo.setSize(-1);
     columnInfo.setWidth(20);
     columnInfo.setHeight(1);
     columnInfo.setPrecision(0);
     columnInfo.setScale(0);
     columnInfo.setNullable(false);
     columnInfo.setDisplaylevel(DisplayLevel.record);
     columnInfo.makePersistent();
     getDb().setLogSQL(true);
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
     Integer t = null;
     Enumeration en = ut.selection();
     t = (Integer) ((User) en.nextElement()).getRaw("testIntegrityfixcol");
     while (en.hasMoreElements()) {
       assertEquals(t, (Integer) ((User) en.nextElement())
           .getRaw("testIntegrityfixcol"));
     }
 
     IntegrityFix t2 = null;
     Enumeration en2 = ut.selection();
     t2 = (IntegrityFix) ((User) en2.nextElement())
         .getCooked("testIntegrityfixcol");
     while (en2.hasMoreElements()) {
       System.err.println(t2);
       assertEquals(t2, ((IntegrityFix) ((User) en2.nextElement())
           .getCooked("testIntegrityfixcol")));
     }
 
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testIntegrityfixcol").selectionWhereEq(new Integer(2)))
         .size());
     PoemThread.commit();
     assertEquals(2, EnumUtils.vectorOf(
         ut.getColumn("testIntegrityfixcol").selectionWhereEq(new Integer(2)))
         .size());
     assertEquals(new Integer(2), ut.administratorUser().getRaw(
         "testIntegrityfixcol"));
     assertEquals(StandardIntegrityFix.prevent, ((IntegrityFix) ut
         .administratorUser().getCooked("testIntegrityfixcol")));
     assertEquals(StandardIntegrityFix.prevent, ((IntegrityFix) ut.getObject(0)
         .getCooked("testIntegrityfixcol")));
     getDb().setLogSQL(false);
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
    * @see org.melati.poem.Table#cachedCount(Persistent, boolean, boolean)
    */
   public void testCachedCountPersistentBooleanBoolean() {
 
   }
 
   /**
    * @see org.melati.poem.Table#cachedCount(String)
    */
   public void testCachedCountString() {
 
   }
 
   /**
    * @see org.melati.poem.Table#cachedExists(String)
    */
   public void testCachedExists() {
 
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
     Field userTableFields = getDb().getTableInfoTable()
         .cachedSelectionField("\"TABLEINFO\".\"DISPLAYORDER\"  <3000 AND \"TABLEINFO\".\"DISPLAYORDER\"  > 2000", null,
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
     .cachedSelectionField("\"TABLEINFO\".\"DISPLAYORDER\"  <3000 AND \"TABLEINFO\".\"DISPLAYORDER\"  > 2000", 
         "\"TABLEINFO\".\"DISPLAYNAME\"", true, null, "userTables");
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
         "\"TABLEINFO\".\"DISPLAYORDER\"  <3000 AND \"TABLEINFO\".\"DISPLAYORDER\"  > 2000",
         "\"TABLEINFO\".\"DISPLAYNAME\"", false, null, "userTables");
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
         "\"TABLEINFO\".\"DISPLAYORDER\"  <3000 AND \"TABLEINFO\".\"DISPLAYORDER\"  > 2000",
         "\"TABLEINFO\".\"DISPLAYNAME\"", false, new Integer(0), "userTables");
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
