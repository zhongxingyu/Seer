 package org.wyona.security.test;
 
 
 import java.io.File;
 
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.GroupManager;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.api.Item;
 import org.wyona.security.core.api.User;
 import org.wyona.security.core.api.UserManager;
 import org.wyona.security.impl.yarep.YarepIdentityManagerImpl;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import junit.framework.TestCase;
 
 /**
  * Test for the IdentityManager.
  */
 public class IdentityManagerImplTest extends TestCase {
 
     protected Repository repo;
     protected IdentityManager identityManager;
     
     public void setUp() throws Exception {
         RepositoryFactory repoFactory = new RepositoryFactory();
         repo = repoFactory.newRepository("identities-repository", new File(
                 "repository2/repository.xml"));
        identityManager = new YarepIdentityManagerImpl(repo);
     }
     
     /*
      * Test simple authentication
      */
     public void testAuthenticate1() throws Exception {
         RepositoryFactory repoFactory = new RepositoryFactory();
         repo = repoFactory.newRepository("identities-repository", new File(
                 "repository1/config/repository-identities.xml"));
        IdentityManager identityManager = new YarepIdentityManagerImpl(repo);
         String user = "lenya";
         String rightPassword = "levi";
         String wrongPassword = "lala";
         assertTrue(identityManager.authenticate(user, rightPassword));
         assertFalse(identityManager.authenticate(user, wrongPassword));
     }
     
     /*
      * Test salt authentication
      */
     public void testAuthenticate2() throws Exception {
         RepositoryFactory repoFactory = new RepositoryFactory();
         repo = repoFactory.newRepository("identities-repository", new File(
         "repository1/config/repository-identities.xml"));
        IdentityManager identityManager = new YarepIdentityManagerImpl(repo);
         String user = "alice";
         String rightPassword = "levi";
         String wrongPassword = "lala";
         assertTrue(identityManager.authenticate(user, rightPassword));
         assertFalse(identityManager.authenticate(user, wrongPassword));	
     }
     
     
     public void testGetUser() throws Exception {
         String userID = "lenya";
         User user = identityManager.getUserManager().getUser(userID);
         assertNotNull(user);
         assertEquals("lenya@wyona.org", user.getEmail());
     }
 
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
    
     public void testGetGroups() throws Exception {
         String groupID = "editors";
         Group group = identityManager.getGroupManager().getGroup(groupID);
         assertNotNull(group);
         assertEquals("Editors", group.getName());
     }
 
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
 }
