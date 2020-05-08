 /**
  * 
  */
 package org.melati.poem.test;
 
import org.melati.poem.Capability;
 import org.melati.poem.TableRenamePoemException;
 
 /**
  * @author timp
  *
  */
 public class TableInfoTest extends PoemTestCase {
 
   /**
    * @param name
    */
   public TableInfoTest(String name) {
     super(name);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * Test method for {@link org.melati.poem.TableInfo#assertCanRead(org.melati.poem.AccessToken)}.
    */
   public void testAssertCanReadAccessToken() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TableInfo#setName(java.lang.String)}.
    */
   public void testSetName() {
     try {
       getDb().getUserTable().getTableInfo().setName("newname");
       fail("Should have blown up");
     } catch (TableRenamePoemException e) {
       e = null;
     }
   }
 
   /**
    * Test method for {@link org.melati.poem.TableInfo#setCachelimit(java.lang.Integer)}.
    */
   public void testSetCachelimitInteger() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TableInfo#setSeqcached(java.lang.Boolean)}.
    */
   public void testSetSeqcachedBoolean() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TableInfo#TableInfo()}.
    */
   public void testTableInfo() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TableInfo#actualTable()}.
    */
   public void testActualTable() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TableInfo#TableInfo(org.melati.poem.Table)}.
    */
   public void testTableInfoTable() {
     
   }
 
  /**
   * Test that the 'clear' integrityfix works.
   */
  public void testClearIntegrityFix() {
    Capability c = getDb().getCapabilityTable().ensure("tableInfoReading");
    getDb().getUserTable().getTableInfo().setDefaultcanread(c);
    assertEquals("tableInfoReading", 
            getDb().getUserTable().getTableInfo().getDefaultcanread().getName());
    c.delete();
    assertNull(getDb().getUserTable().getTableInfo().getDefaultcanread());
    
  }
  
 }
