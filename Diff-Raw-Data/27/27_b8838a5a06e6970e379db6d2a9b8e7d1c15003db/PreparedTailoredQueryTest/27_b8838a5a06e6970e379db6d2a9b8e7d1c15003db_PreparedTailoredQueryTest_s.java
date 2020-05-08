 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.util.Enumeration;
 
 import org.melati.poem.AccessPoemException;
 import org.melati.poem.Capability;
 import org.melati.poem.Column;
 import org.melati.poem.FieldSet;
 import org.melati.poem.PoemTask;
 import org.melati.poem.PoemThread;
 import org.melati.poem.PreparedTailoredQuery;
 import org.melati.poem.Table;
 import org.melati.poem.User;
 import org.melati.util.EnumUtils;
 
 /**
  * @author timp
  * @since 22 Jan 2007
  *
  */
 public class PreparedTailoredQueryTest extends PoemTestCase {
 
   /**
    * Constructor.
    * @param name
    */
   public PreparedTailoredQueryTest(String name) {
     super(name);
     setDbName("poemtest");
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
     setDbName("poemtest");
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * Test method for {@link org.melati.poem.PreparedTailoredQuery#selection()}.
    */
   public void testSelection() {
     TestDatabase db = (TestDatabase)getDb();
     Capability spyMaster = db.getCapabilityTable().ensure("spyMaster");
     final Capability moneyPenny = db.getCapabilityTable().ensure("moneyPenny");
     
     User spy = (User)db.getUserTable().newPersistent();
     spy.setLogin("spy");
     spy.setName("Spy");
     spy.setPassword("spy");
     spy.makePersistent();
 
     Protected spyMission = (Protected)db.getProtectedTable().newPersistent();
     spyMission.setCanRead(moneyPenny);
     spyMission.setCanSelect(moneyPenny);
     spyMission.setCanWrite(moneyPenny);
     spyMission.setCanDelete(spyMaster);
     spyMission.setSpy(spy);
     spyMission.setMission("impossible");
     spyMission.makePersistent();
     
     final Column canReadColumn = db.getProtectedTable().getCanReadColumn();
     final PreparedTailoredQuery ptq = new PreparedTailoredQuery(
             new Column[] {canReadColumn},
             new Table[0],
             canReadColumn.fullQuotedName() + "=" + moneyPenny.troid(),
             null) ;
     assertEquals(new Integer(1), 
             new Integer(
                     EnumUtils.vectorOf(
                             ptq.selection()).size()));
     Enumeration en = ptq.selection();
     while (en.hasMoreElements()) {
       //Object ne = 
         en.nextElement();
       //System.err.println(ne);
     }
 
     PoemTask readAsGuest = new PoemTask() {
       public void run() {
         assertEquals(new Integer(1), 
                 new Integer(
                         EnumUtils.vectorOf(
                                 ptq.selection()).size()));
         Enumeration en = ptq.selection();
         try { 
           while (en.hasMoreElements()) { 
             System.err.println(en.nextElement());
           }
           fail("Should have blown up");
         } catch (AccessPoemException e) { 
           e = null;
         }
 
       }
     };
     
     PoemThread.withAccessToken(db.guestAccessToken(), readAsGuest);
     
    // Check that default works as well
   // db.getProtectedTable().getTableInfo().setDefaultcanread(moneyPenny);
     final Column missionColumn = db.getProtectedTable().getMissionColumn();

     final PreparedTailoredQuery ptq2 = new PreparedTailoredQuery(
             new Column[] {missionColumn, db.getUserTable().getPasswordColumn()},
             new Table[] {db.getUserTable()},
             missionColumn.fullQuotedName() + " = 'impossible' AND " + 
             db.getProtectedTable().getSpyColumn().fullQuotedName() + 
             " = " + 
             db.getUserTable().troidColumn().fullQuotedName(),
             null) ;
    System.err.println(ptq2);
     PoemTask readAsGuest2 = new PoemTask() {
       public void run() {
         assertEquals(new Integer(1), 
                 new Integer(
                         EnumUtils.vectorOf(
                                 ptq2.selection()).size()));
         Enumeration en = ptq2.selection();
         try { 
           while (en.hasMoreElements()) { 
             FieldSet tuple = (FieldSet)en.nextElement();
             System.err.println(tuple);
           }
           fail("Should have blown up");
         } catch (AccessPoemException e) { 
           e = null;
         }
 
       }
     };
     
    spyMission.setCanRead(null);
     PoemThread.withAccessToken(db.guestAccessToken(), readAsGuest2);
     
     
     spyMission.delete();
     spy.delete();
     spyMaster.delete();
     moneyPenny.delete();
 
   }
 
   /**
    * Test method for {@link org.melati.poem.PreparedTailoredQuery#selection_firstRaw()}.
    */
   public void testSelection_firstRaw() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.PreparedTailoredQuery#PreparedTailoredQuery(java.lang.String, org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testPreparedTailoredQueryStringColumnArrayTableArrayStringString() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.PreparedTailoredQuery#PreparedTailoredQuery(org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testPreparedTailoredQueryColumnArrayTableArrayStringString() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TailoredQuery#TailoredQuery(org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testTailoredQueryColumnArrayTableArrayStringString() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TailoredQuery#TailoredQuery(java.lang.String, org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testTailoredQueryStringColumnArrayTableArrayStringString() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TailoredQuery#toString()}.
    */
   public void testToString() {
     
   }
 
 }
