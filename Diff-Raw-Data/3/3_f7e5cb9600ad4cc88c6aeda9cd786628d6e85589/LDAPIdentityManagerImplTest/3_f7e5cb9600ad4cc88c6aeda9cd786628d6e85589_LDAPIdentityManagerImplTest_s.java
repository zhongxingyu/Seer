 package org.wyona.security.test;
 
 import java.io.File;
 
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.GroupManager;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.api.Item;
 import org.wyona.security.core.api.User;
 import org.wyona.security.core.api.UserManager;
 import org.wyona.security.impl.ldap.LDAPIdentityManagerImpl;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.Logger;
 
 /**
  * Test of the LDAPIdentityManagerImpl
  */
 public class LDAPIdentityManagerImplTest extends TestCase {
 
     private static Logger log = Logger.getLogger(LDAPIdentityManagerImplTest.class);
 
     private Repository repo;
     private IdentityManager identityManager;
     
     public void setUp() throws Exception {
         RepositoryFactory repoFactory = new RepositoryFactory();
         repo = repoFactory.newRepository("ldap-identities-repository", new File("repository-ldap-local-cache/repository.xml"));
         identityManager = new LDAPIdentityManagerImpl(repo, true);
     }
 
     /**
      * Get all users from cache (Yarep repository)
      */
     public void testGetAllUsersFromCache() throws Exception {
         User[] users = identityManager.getUserManager().getUsers(false);
         assertNotNull(users);
         log.debug("Number of users: " + users.length);
         for (int i = 0; i < users.length; i++) {
             log.debug("User: " + users[i].getName());
         }
         assertEquals(1, users.length);
         assertEquals("bob@wyona.org", users[0].getEmail());
     }
 
     /**
      * Get all users from LDAP
      */
     public void testGetAllUsersFromLDAP() throws Exception {
         User[] users = identityManager.getUserManager().getUsers(true);
        assertNull(users);
 /*
         log.warn("DEBUG: Number of users: " + users.length);
         for (int i = 0; i < users.length; i++) {
             log.warn("DEBUG: User: " + users[i].getName());
         }
         assertEquals(1, users.length);
         assertEquals("bob@wyona.org", users[0].getEmail());
 */
     }
 
 /*
     public void testGetUser() throws Exception {
         String userID = "bob";
         User user = identityManager.getUserManager().getUser(userID);
         assertNotNull(user);
         assertEquals("bob@wyona.org", user.getEmail());
     }
 */
 
 /*
     public void testGetUserGroups() throws Exception {
         String userID = "lenya";
         String groupID = "editors";
         User user = identityManager.getUserManager().getUser(userID);
         assertNotNull(user);   
         Group group = identityManager.getGroupManager().getGroup(groupID);        
         assertTrue(group.isMember(user));     
         Group[] userGroups = user.getGroups();
         assertEquals(1,userGroups.length);
         assertEquals(groupID , userGroups[0].getID());
     }
 */
 
 /* TODO: Delete testuser if it already exists
     public void testAddUser() throws Exception {
         UserManager userManager = identityManager.getUserManager(); 
         String id = "testuser";
         String name = "Test User";
         String email = "test@wyona.org";
         String password = "test123";
         assertFalse("User already exists: " + id, userManager.existsUser(id));
         User user = userManager.createUser(id, name, email, password);
         assertTrue(userManager.existsUser(id));
         assertNotNull(user);
         assertEquals(id, user.getID());
         assertEquals(email, user.getEmail());
         assertEquals(name, user.getName());
         assertTrue(user.authenticate(password));
     }
 */
    
 /*
     public void testGetGroups() throws Exception {
         String groupID = "editors";
         Group group = identityManager.getGroupManager().getGroup(groupID);
         assertNotNull(group);
         assertEquals("Editors", group.getName());
     }
 */
 
 /* TODO: Delete testgroup1 if it already exists
     public void testAddGroup() throws Exception {
         GroupManager groupManager = identityManager.getGroupManager(); 
         String id = "testgroup1";
         String name = "Test Group 1";
         assertFalse("Group already exists: " + id, groupManager.existsGroup(id));
         Group group = groupManager.createGroup(id, name);
         assertTrue(groupManager.existsGroup(id));
         assertNotNull(group);
         assertEquals(id, group.getID());
         assertEquals(name, group.getName());
         
         // add member:
         UserManager userManager = identityManager.getUserManager();
         String userID = "user789";
         User user = userManager.createUser(userID, "Some Name", "foo@bar.org", "pwd123");
         assertFalse(group.isMember(user));
         group.addMember(user);
         group.save();
         assertTrue(group.isMember(user));
         
         // delete user:
         userManager.removeUser(userID);
         assertFalse(group.isMember(user));
     }
 */
 
 /* TODO: Delete testgroup2 if it already exists
     public void testGroupMembers() throws Exception {
         GroupManager groupManager = identityManager.getGroupManager(); 
         UserManager userManager = identityManager.getUserManager();
         String id = "testgroup2";
         String name = "Test Group 2";
         Group group = groupManager.createGroup(id, name);
         String userID1 = "user-test1";
         User user1 = userManager.createUser(userID1, "Some Name 1", "foo1@bar.org", "pwd123");
         String userID2 = "user-test2";
         User user2 = userManager.createUser(userID2, "Some Name 2", "foo2@bar.org", "pwd123");
         String userID3 = "user-test3";
         User user3 = userManager.createUser(userID3, "Some Name 3", "foo3@bar.org", "pwd123");
 
         group.addMember(user1);
         group.addMember(user2);
         group.addMember(user3);
         group.save();
         
         Item[] items = group.getMembers();
         assertEquals(3, items.length);
         for (int i=0; i<items.length; i++) {
             User user = (User)items[i];
             assertTrue(group.isMember(user));
             assertTrue(user.getName().startsWith("Some Name"));
         }
     }
 */
 }
