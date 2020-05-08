 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.util.Enumeration;
 
 import org.melati.poem.ColumnInfo;
 import org.melati.poem.DisplayLevel;
 import org.melati.poem.PoemTypeFactory;
 import org.melati.poem.Searchability;
 
 
 /**
  * @author timp
  *
  */
 public class ColumnInfoTest extends PoemTestCase {
 
   /**
    * @param name
    */
   public ColumnInfoTest(String name) {
     super(name);
   }
 
   /**
    * {@inheritDoc}
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   /**
    * {@inheritDoc}
    * @see junit.framework.TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * Test method for {@link org.melati.poem.ColumnInfo#assertCanRead(org.melati.poem.AccessToken)}.
    */
   public void testAssertCanReadAccessToken() {
 
   }
 
   /**
    * Test method for {@link org.melati.poem.ColumnInfo#setTableinfoTroid(java.lang.Integer)}.
    */
   public void testSetTableinfoTroid() {
     ColumnInfo ci = getDb().getUserTable().getNameColumn().getColumnInfo();
     try { 
       ci.setTableinfoTroid(new Integer(99));
       fail("Should have bombed");
     } catch (IllegalArgumentException e) { 
       e = null;
     }
   }
 
   /**
    * Test method for {@link org.melati.poem.ColumnInfo#setName(java.lang.String)}.
    */
   public void testSetName() {
 
   }
 
   /**
    * Test method for {@link org.melati.poem.ColumnInfo#
    * setDisplaylevelIndex(java.lang.Integer)}.
    */
   public void testSetDisplaylevelIndex() {
     ColumnInfo passwordCI = getDb().getUserTable().getPasswordColumn().getColumnInfo();
     ColumnInfo nameCI =     getDb().getUserTable().getNameColumn().getColumnInfo();
     assertEquals(DisplayLevel.record, passwordCI.getDisplaylevel());
     assertEquals(DisplayLevel.primary,    nameCI.getDisplaylevel());
 
     assertEquals("name", getDb().getUserTable().displayColumn().getName());
     
     passwordCI.setDisplaylevelIndex(DisplayLevel.primary.getIndex());
     assertEquals(DisplayLevel.primary, passwordCI.getDisplaylevel());
     assertEquals(DisplayLevel.summary, 
             getDb().getUserTable().getNameColumn().getDisplayLevel());
     assertEquals("password",  getDb().getUserTable().displayColumn().getName());
    passwordCI.setDisplaylevelIndex(DisplayLevel.record.getIndex());
     getDb().getUserTable().getNameColumn().getColumnInfo().
         setDisplaylevelIndex(DisplayLevel.primary.getIndex());
   }
 
   /**
    * Test method for {@link org.melati.poem.ColumnInfo#getDsdQualifiers()}.
    */
   public void testGetDsdQualifiers() {
     ColumnInfo ci = getDb().getUserTable().getPasswordColumn().getColumnInfo();
     Enumeration them = ci.getDsdQualifiers();
     int counter = 0;
     while (them.hasMoreElements()) {
       counter++;
       them.nextElement();
     }
     assertEquals(18, counter);
   }
 
   /**
    * As it is used in DSD.wm
    */
   public void testGetDsdQualifiers2() {
     ColumnInfo ci = getDb().getUserTable().getIdColumn().getColumnInfo();
     Enumeration them = ci.getDsdQualifiers();
     int counter = 0;
     while (them.hasMoreElements()) {
       counter++;
       them.nextElement();
     }
     assertEquals(15, counter);
   }
 
  /** 
   * Test the odd circumstance in which the troid is known but the column is not.
   * Does not look like there is a circumstance in which this would work.
   */
   public void putativeTestUnsetColumn() { 
     ColumnInfo ci = (ColumnInfo)getDb().getColumnInfoTable().newPersistent();
     ci.setId(0);
     ci.setName("testcolinfo");
     ci.setDescription("test colinfo");
     ci.setDisplayname("test colinfo");
     ci.setDisplayorder(1);
     ci.setDisplaylevel(DisplayLevel.primary);
     ci.setSearchability(Searchability.primary);
     ci.setUsercreateable(true);
     ci.setUnique(false);
     ci.setIndexed(true);
     ci.setUsereditable(true);
     ci.setTypefactory(PoemTypeFactory.TROID);
     ci.setNullable(false);
     ci.setSize(16);
     ci.setHeight(1);
     ci.setWidth(20);
     ci.setPrecision(22);
     ci.setScale(2);
     ci.setTableinfo(getDb().getColumnInfoTable().getTableInfo());
     
     ci.makePersistent();
     try { 
       ci.getDsdQualifiers();
     } catch (NullPointerException e) { 
       e = null;
     }
     ci.delete();
   }
 }
