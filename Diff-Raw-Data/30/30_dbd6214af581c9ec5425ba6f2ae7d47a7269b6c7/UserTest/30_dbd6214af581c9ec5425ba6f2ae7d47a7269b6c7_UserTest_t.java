 /**
  * 
  */
 package org.melati.poem.test;
 
import org.melati.poem.Group;
import org.melati.poem.GroupMembership;
 import org.melati.poem.PoemThread;
 import org.melati.poem.ReadPasswordAccessPoemException;
import org.melati.poem.RowDisappearedPoemException;
 import org.melati.poem.User;
 import org.melati.poem.WriteFieldAccessPoemException;
 import org.melati.poem.WritePersistentAccessPoemException;
 
 /**
  * @author timp
  * @since 9 Jan 2007
  *
  */
 public class UserTest extends PoemTestCase {
 
   /**
    * @param name
    */
   public UserTest(String name) {
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
 
   protected void melatijunitUnchanged() { 
     User tester = (User)getDb().getUserTable().getNameColumn().firstWhereEq("tester");
     if (tester != null)
       tester.delete();
     super.melatijunitUnchanged();
 
   }
   
   /**
    * Test method for {@link org.melati.poem.User#toString()}.
    */
   public void testToString() {
     
   }
 
   /**
    * The guest user should not be able to read 
    * another users password.
    * Test method for {@link org.melati.poem.User#getPassword()}.
    */
   public void testGetPassword() {
     assertEquals("guest",getDb().guestUser().getPassword());
     User tester = (User)getDb().getUserTable().newPersistent();
     tester.setName("tester");
     tester.setLogin("tester");
     tester.setPassword("pwd");
     tester.makePersistent();
     assertEquals("pwd",tester.getPassword());
     
     PoemThread.setAccessToken(getDb().guestUser());
     assertEquals("guest",getDb().guestUser().getPassword());
     try {
       assertEquals("pwd",tester.getPassword());
       fail("should have blown up");
     } catch (ReadPasswordAccessPoemException e) {
       e = null;
     }
     assertFalse(tester.isGuest());
     assertFalse(tester.isAdministrator());
   }
 
   /**
    * A user may read her own password but not set it. 
    * Test method for {@link org.melati.poem.User#setPassword(java.lang.String)}.
    */
   public void testSetPassword() {
     assertEquals("guest",getDb().guestUser().getPassword());
     getDb().guestUser().setPassword("fred");
     assertEquals("fred",getDb().guestUser().getPassword());
     getDb().guestUser().setPassword("guest");
     assertEquals("guest",getDb().guestUser().getPassword());
     User tester = (User)getDb().getUserTable().newPersistent();
     tester.setName("tester");
     tester.setLogin("tester");
     tester.setPassword("pwd");
     tester.makePersistent();
     assertEquals("pwd",tester.getPassword());
 
     PoemThread.setAccessToken(tester);
     assertEquals("pwd",tester.getPassword());
     try {
       tester.setPassword("new");
       fail("should have blown up");
     } catch (WritePersistentAccessPoemException e) {
       e = null;
     }
   }
   /**
    * Need another go as root.
    * Test method for {@link org.melati.poem.User#setPassword(java.lang.String)}.
    */
   public void testSetPassword2() {
     User tester = (User)getDb().getUserTable().newPersistent();
     tester.setName("tester");
     tester.setLogin("tester");
     tester.setPassword("pwd");
     tester.makePersistent();
     assertEquals("pwd",tester.getPassword());
 
     PoemThread.setAccessToken(getDb().guestAccessToken());
     try {
       tester.setPassword("new");
       fail("should have blown up");
     } catch (WriteFieldAccessPoemException e) {
       e = null;
     }
   }
 
   /**
    * Test method for {@link org.melati.poem.User#User()}.
    */
   public void testUser() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.User#
    * User(java.lang.String, java.lang.String, java.lang.String)}.
    */
   public void testUserStringStringString() {
    User u = new User("tester","tester","tester");
    getDb().getUserTable().create(u); 
    assertEquals("tester",u.getName());
    u.delete();
   }
 
   /**
    * Test method for {@link org.melati.poem.User#givesCapability(org.melati.poem.Capability)}.
    */
   public void testGivesCapability() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.User#isGuest()}.
    */
   public void testIsGuest() {
     assertTrue(getDb().guestUser().isGuest());
     assertFalse(getDb().administratorUser().isGuest());
   }
 
   /**
    * Test method for {@link org.melati.poem.User#isAdministrator()}.
    */
   public void testIsAdministrator() {
     assertFalse(getDb().guestUser().isAdministrator());
     assertTrue(getDb().administratorUser().isAdministrator());    
   }
 
  public void testIntegrityFixDelete() {
    User u = new User("tester","tester","tester");
    getDb().getUserTable().create(u); 
    assertEquals("tester",u.getName());
    Group g = getDb().getGroupTable().administratorsGroup();
    GroupMembership gm = (GroupMembership)getDb().getGroupMembershipTable().newPersistent();
    gm.setGroup(g);
    gm.setUser(u);
    gm.makePersistent();
    u.delete();
    try {
      gm.delete();
    } catch(RowDisappearedPoemException e) { 
      e = null;
    }
  }
 
 }
