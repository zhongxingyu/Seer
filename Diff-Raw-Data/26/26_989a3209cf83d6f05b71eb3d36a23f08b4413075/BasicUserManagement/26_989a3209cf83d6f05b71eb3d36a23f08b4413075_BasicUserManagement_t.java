 package de.enwida.web;
 
 import java.util.Calendar;
 
import javax.transaction.NotSupportedException;

 import org.apache.log4j.Logger;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import de.enwida.web.dao.interfaces.IGroupDao;
 import de.enwida.web.dao.interfaces.IRightDao;
 import de.enwida.web.dao.interfaces.IRoleDao;
 import de.enwida.web.db.model.CalendarRange;
 import de.enwida.web.model.Group;
 import de.enwida.web.model.Right;
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IUserService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/root-context-test.xml")
 public class BasicUserManagement {
 	private Logger logger = Logger.getLogger(getClass());
     
     @Autowired
     private IUserService userService;
 	@Autowired
 	private IGroupDao groupDao;
 	@Autowired
 	private IRoleDao roleDao;
 	@Autowired
 	private IRightDao rightDao;
 
 	@Before
 	public void cleanup() throws Exception {
 		for (final User user : userService.getAllUsers()) {
 			userService.deleteUser(user.getUserId());
 		}
 	}
 	
 	@Test
 	public void userIsSaved() throws Exception {
 		final User user = saveTestUser("testuser");
 		final User testee = userService.getUser("testuser");
 		
 		// Instance is fresh from database
 		Assert.assertFalse(user == testee);
 
 		Assert.assertNotNull(testee);
 		Assert.assertEquals(user, testee);
 		Assert.assertEquals(user.getUserId(), testee.getUserId());
 		Assert.assertEquals(user.getUserName(), testee.getUserName());
 		Assert.assertEquals(user.getPassword(), testee.getPassword());
 		Assert.assertEquals(user.getFirstName(), testee.getFirstName());
 		Assert.assertEquals(user.getLastName(), testee.getLastName());
 		Assert.assertEquals(user.getCompanyName(), testee.getCompanyName());
 		Assert.assertEquals(user.getActivationKey(), testee.getActivationKey());
 	}
 	
 	@Test
 	public void groupIsSaved() throws Exception {
 		final Group group = new Group("testgroup");
 		userService.saveGroup(group);
 		
 		final Group testee = userService.getGroup("testgroup");
 		
 		// Instance is fresh from database
 		Assert.assertFalse(group == testee);
 		
 		Assert.assertNotNull(testee);
 		Assert.assertEquals(group, testee);
 		Assert.assertEquals(group.getGroupID(), testee.getGroupID());
 		Assert.assertEquals(group.getGroupName(), testee.getGroupName());
 	}
 
 	@Test
 	public void addGroupsToUser() throws Exception {
 		final User user = saveTestUser("testuser");
 		final Group group1 = saveTestGroup("testgroup1");
 		final Group group2 = saveTestGroup("testgroup2");
 		
 		Assert.assertTrue(user.getGroups().isEmpty());
 		
 		final Group freshGroup1 = userService.assignGroupToUser(user, group1);
 		final Group freshGroup2 = userService.assignGroupToUser(user, group2);
 		
 		// Groups got added to user object
 		Assert.assertEquals(2, user.getGroups().size());
 		Assert.assertTrue(user.getGroups().contains(group1));
 		Assert.assertTrue(user.getGroups().contains(group2));
 		
 		// User got added to fresh group objects
 		Assert.assertEquals(1, freshGroup1.getAssignedUsers().size());
 		Assert.assertTrue(freshGroup1.getAssignedUsers().contains(user));
 		Assert.assertEquals(1, freshGroup2.getAssignedUsers().size());
 		Assert.assertTrue(freshGroup2.getAssignedUsers().contains(user));
 		
 		// User is NOT added to stale groups objects
 		Assert.assertTrue(group1.getAssignedUsers().isEmpty());
 		Assert.assertTrue(group2.getAssignedUsers().isEmpty());
 		
 		// Groups got added to freshly fetched user object
 		final User fetchedUser = userService.getUser("testuser");
 		Assert.assertEquals(2, fetchedUser.getGroups().size());
 		Assert.assertTrue(fetchedUser.getGroups().contains(group1));
 		Assert.assertTrue(fetchedUser.getGroups().contains(group2));
 		
 		// User got added to freshly fetched group objects
 		final Group fetchedGroup1 = userService.getGroup("testgroup1");
 		final Group fetchedGroup2 = userService.getGroup("testgroup2");
 
 		Assert.assertEquals(1, fetchedGroup1.getAssignedUsers().size());
 		Assert.assertTrue(fetchedGroup1.getAssignedUsers().contains(user));
 		Assert.assertEquals(1, fetchedGroup2.getAssignedUsers().size());
 		Assert.assertTrue(fetchedGroup2.getAssignedUsers().contains(user));
 	}
 	
 	@Test
 	public void addGroupTwice() throws Exception {
 		final User user = saveTestUser("testuser");
 		final Group group1 = saveTestGroup("testgroup1");
 		
 		userService.assignGroupToUser(user, group1);
 
 		// Second add succeeds due to set semantics
 		final Group freshGroup1 = userService.assignGroupToUser(user, group1);
 		
 		Assert.assertEquals(1, user.getGroups().size());
 		Assert.assertEquals(1, freshGroup1.getAssignedUsers().size());
 	}
 	
 	@Test
 	public void addNonPersistedGroup() throws Exception {
 		final User user = saveTestUser("testuser");
 
 		// Group is not persisted 
 		final Group group = new Group("testgroup1");
 		
 		// Assigning it should cause an exception
 		try {
 			userService.assignGroupToUser(user, group);
 			throw new Exception("Shouldn't be reachable; IllegalArgumentException expected");
 		} catch (IllegalArgumentException e) {
 			// Expected
 			Assert.assertTrue(e.getMessage().toLowerCase().contains("persisted"));
 		}
 	}
 	
 	@Test
 	public void revokeGroupsFromUser() throws Exception {
 		final User user = saveTestUser("testuser");
 		final Group group1 = saveTestGroup("testgroup1");
 		final Group group2 = saveTestGroup("testgroup2");
 		
 		userService.assignGroupToUser(user, group1);
 		userService.assignGroupToUser(user, group2);
 		
		final Group freshGroup1 = userService.revokeUserFromGroup(user, group1);
 		
 		// Group was removed from user object
 		Assert.assertEquals(1, user.getGroups().size());
 		Assert.assertTrue(user.getGroups().contains(group2));
 
 		// User is removed from fresh group object
 		Assert.assertTrue(freshGroup1.getAssignedUsers().isEmpty());
 
 		// Group is removed from freshly fetched user object
 		final User fetchedUser = userService.getUser("testuser");
 		Assert.assertEquals(1, fetchedUser.getGroups().size());
 		Assert.assertTrue(fetchedUser.getGroups().contains(group2));
 		
 		// User is removed from freshly fetched group object
 		final Group fetchedGroup = userService.getGroup("testgroup1");
 		Assert.assertTrue(fetchedGroup.getAssignedUsers().isEmpty());
 	}
 	
	@Test
	public void doNotAllowDirectModificationOfUserGroupsRelationshiop() throws Exception {
		final User user = saveTestUser("testuser");
		final Group group = saveTestGroup("testgroup");
		
		try {
			user.getGroups().add(group);
			throw new Exception("Shouldn't be reachable; UnsupportedOperationException expected");
		} catch (UnsupportedOperationException e) {
			// Expected
		}
		
		try {
			group.getAssignedUsers().add(user);
			throw new Exception("Shouldn't be reachable; UnsupportedOperationException expected");
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}
	
 
 //	@Test
 //	public void groupRoleTest() throws Exception {
 //		user = addTestUser("testuser");
 //		group = addTestGroup("testgroup1");
 //		role = addTestRole("testrole1");
 //		Role role2 = addTestRole("testrole2");
 //		Role role3 = addTestRole("testrole3");
 //
 //		assignUserToGroup(user, group);
 //		assignRoleToGroup(group, role);
 //		assignRoleToGroup(group, role2);
 //		assignRoleToGroup(group, role3);
 //
 //		removeRoleFromGroup(group, role2);
 //
 //	}
 //
 //	private void assignRoleToGroup(Group group, Role role) {
 //		userService.assignRoleToGroup(role, group);
 //		group = groupDao.fetchByName(group.getGroupName());
 //		System.out.println(role.getAssignedGroups());
 //		System.out.println(group.getAssignedRoles());
 //	}
 //
 //	private void removeRoleFromGroup(Group group, Role role) {
 //		userService.revokeRoleFromGroup(role, group);
 //		group = groupDao.fetchByName(group.getGroupName());
 //		System.out.println(role.getAssignedGroups());
 //		System.out.println(group.getAssignedRoles());
 //	}
 //
 //	@Test
 //	public void roleRightTest() throws Exception {
 //		role = addTestRole("testrole1");
 //		right = addTestRight(1);
 //		Right right2 = addTestRight(2);
 //		Right right3 = addTestRight(3);
 //
 //		assignRightToRole(right, role);
 //		assignRightToRole(right2, role);
 //		assignRightToRole(right3, role);
 //
 //		removeRightFromRole(right2, role);
 //		user = userService.getUser("testuser");
 //		logger.debug("Authorities : " + user.getAuthorities());
 //	}
 //
 //	private void assignRightToRole(Right right, Role role) {
 //		userService.assignRightToRole(right, role);
 //		right = rightDao.fetchById(right.getRightID());
 //		System.out.println(role.getRights());
 //		System.out.println(right.getRole());
 //	}
 //
 //	private void removeRightFromRole(Right right, Role role) {
 //		userService.revokeRightFromRole(right, role);
 //		right = rightDao.fetchById(right.getRightID());
 //		System.out.println(role.getRights());
 //		System.out.println(right.getRole());
 //	}
 //
 //	@Test
 //	@Ignore
 //	// @Transactional
 //	public void userIsAddedToDatabase() throws Exception {
 //		final String userName = "test";
 //		final User user = addTestUser(userName);
 //		
 //		final User testee = userService.getUser(userName);
 //		// userDao.refresh(testee);
 //		Assert.assertNotNull(testee);
 //		Assert.assertEquals(testee.getUsername(), user.getUserName());
 //		Assert.assertEquals(testee.getCompanyName(), user.getCompanyName());
 //		Assert.assertEquals(testee.getFirstName(), user.getFirstName());
 //		Assert.assertEquals(testee.getLastName(), user.getLastName());
 //		Assert.assertEquals(testee.getPassword(), user.getPassword());
 //	}
 //	
 //	@Test
 //	@Ignore
 //	public void groupIsAddedToDatabase() throws Exception {
 //		final String groupName = "test";
 //		final Group group = addTestGroup(groupName);
 //		
 //		// final Group testee =
 //		// userService.getGroupDao().fetchByName(groupName);
 //		// Assert.assertNotNull(testee);
 //		// Assert.assertEquals(group.getGroupName(), testee.getGroupName());
 //	}
 //	
 //	// @Test
 //	@Transactional
 //	@Ignore
 //	public void cannotModifyGroupsOfUserDirectly() throws Exception {
 //		final User user = addTestUser("test");
 //		final Group group = addTestGroup("test");
 //		
 //		// Assert.assertTrue(user.getGroups().isEmpty());
 //		// Assert.assertTrue(group.getAssignedUsers().isEmpty());
 //		
 //		try {
 //			user.getGroups().add(group); // should throw
 //			// throw new Exception("Read-only list expected");
 //		} catch (UnsupportedOperationException e) {
 //			// Expected
 //		}
 //		
 //		// userService.assignUserToGroup(user, group);
 //		Assert.assertTrue(user.getGroups().size() == 1);
 //
 //		try {
 //			// user.getGroups().clear();
 //			// throw new Exception("Read-only list expected");
 //		} catch (UnsupportedOperationException e) {
 //			// Expected
 //		}	
 //	}//	
 //
 //	@Test
 //	@Ignore
 //	public void cannotModifyUsersOfGroupDirectly() throws Exception {
 //		final User user = addTestUser("test");
 //		final Group group = addTestGroup("test");
 //		
 //		Assert.assertTrue(user.getGroups().isEmpty());
 //		Assert.assertTrue(group.getAssignedUsers().isEmpty());
 //		
 //		try {
 //			group.getAssignedUsers().add(user); // should throw
 //			// throw new Exception("Read-only list expected");
 //		} catch (UnsupportedOperationException e) {
 //			// Expected
 //		}
 //		
 //		// userService.assignUserToGroup(user, group);
 //		Assert.assertTrue(group.getAssignedUsers().size() == 1);
 //
 //		try {
 //			// group.getAssignedUsers().clear();
 //			// throw new Exception("Read-only list expected");
 //		} catch (UnsupportedOperationException e) {
 //			// Expected
 //		}	
 //	}
 //	
 //	@Test
 //	@Ignore
 //	public void groupsAreAssignedToUser() throws Exception {
 //		final User user = addTestUser("test");
 //		final Group group1 = addTestGroup("test1");
 //		final Group group2 = addTestGroup("test2");
 //		
 //		// Assert.assertTrue(user.getGroups().isEmpty());
 //		// Assert.assertTrue(group1.getAssignedUsers().isEmpty());
 //		// Assert.assertTrue(group2.getAssignedUsers().isEmpty());
 //		
 //		// userService.assignUserToGroup(user, group1);
 //		// userService.assignUserToGroup(user, group2);
 //
 //		// Assert.assertTrue(user.getGroups().size() == 2);
 //		Assert.assertTrue(user.getGroups().contains(group1));
 //		Assert.assertTrue(user.getGroups().contains(group2));
 //		
 //		Assert.assertTrue(group1.getAssignedUsers().size() == 1);
 //		Assert.assertTrue(group1.getAssignedUsers().contains(user));
 //
 //		Assert.assertTrue(group2.getAssignedUsers().size() == 1);
 //		Assert.assertTrue(group2.getAssignedUsers().contains(user));
 //		
 //		// Check with fresh instances
 //		
 //		final User freshUser = userService.getUser("test");
 //		// userDao.refresh(freshUser);
 //		// Assert.assertTrue(freshUser.getGroups().size() == 2);
 //		Assert.assertTrue(user.getGroups().contains(group1));
 //		Assert.assertTrue(user.getGroups().contains(group2));
 //
 //		// final Group freshGroup1 =
 //		// daoService.getGroupDao().fetchByName("test1");
 //		// // groupDao.refresh(freshGroup1);
 //		// Assert.assertTrue(freshGroup1.getAssignedUsers().size() == 1);
 //		// Assert.assertTrue(group1.getAssignedUsers().contains(user));
 //		//
 //		// final Group freshGroup2 =
 //		// daoService.getGroupDao().fetchByName("test2");
 //		// // groupDao.refresh(freshGroup2);
 //		// Assert.assertTrue(freshGroup2.getAssignedUsers().size() == 1);
 //		// Assert.assertTrue(group2.getAssignedUsers().contains(user));
 //	}
 //	
 //    @Test
 //    @Transactional
 //    public void roleIsAddedToDatabase() throws Exception {
 //        final String roleName = "test";
 //        final Role role = addTestRole(roleName);
 //
 //        final Role testee = roleDao.fetchByName(roleName);
 //        roleDao.refresh(testee);
 //        Assert.assertNotNull(testee);
 //        Assert.assertEquals(role.getRoleName(), testee.getRoleName());
 //    }
 
 //
 //    @Test
 //    @Transactional
 //	public void testMail() throws Exception {
 //		mailService.SendEmail("olcaytarazan@gmail.com", "User Management Test","Ignore");
 //	}
 //	    
 //    @Test
 //    @Transactional
 //	public void testRole()  {
 //	    //Create roles
 //		Role adminRole = new Role("Admin");
 //		Role anonymousRole = new Role("Anonymous");
 //		Role testRole = new Role("Test");
 //		//Add roles
 //		adminRole=roleDao.addRole(adminRole);
 //		anonymousRole=roleDao.addRole(anonymousRole);
 //		testRole=roleDao.addRole(testRole);
 // 
 //		//Get Groups
 //        Group adminGroup = new Group("Admin");
 //        adminGroup =groupDao.addGroup(adminGroup);
 //
 //        Group anonymousGroup = new Group("Anonymous");
 //        anonymousGroup = groupDao.addGroup(anonymousGroup);
 //
 //        //Assign
 //        userService.assignRoleToGroup(adminRole.getRoleID(), adminGroup.getGroupID());
 //        userService.assignRoleToGroup(anonymousRole.getRoleID(), anonymousGroup.getGroupID());
 //        
 //        
 //        //CheckRoles
 //        Assert.assertTrue(adminGroup.getAssignedRoles().contains(adminRole));
 //        Assert.assertTrue(anonymousGroup.getAssignedRoles().contains(anonymousRole));
 //		
 //	}
 //    @Test
 //    @Transactional
 //	public void testRight() throws Exception {
 //
 //		Right right1 = new Right();
 //		Right right2 = new Right();
 //		rightDao.addRight(right1);
 //		rightDao.addRight(right2);
 //        rightDao.enableDisableAspect(right1.getRightID(), true);
 //		rightDao.removeRight(right1);
 //		rightDao.removeRight(right2);
 //
 //	}
     
     private User saveTestUser(String name) throws Exception {
 		final User user = new User(name, "secret", "test", "test", true);
 		user.setCompanyName("enwida.de");
 		userService.saveUser(user);
 		return user;
     }
     
     private Group saveTestGroup(String name) throws Exception {
     	final Group group = new Group(name);
     	userService.addGroup(group);
     	return group;
     }
 
 	private Right addTestRight(long id) throws Exception {
 		Right right = rightDao.fetchById(id);
 		if (right == null) {
 			Calendar cal1 = Calendar.getInstance();
 			Calendar cal2 = Calendar.getInstance();
 			right = new Right(99, 110, "WEEKLY", new CalendarRange(cal1, cal2),
 					"POWER", true);
 		}
 		rightDao.addRight(right);
 		return right;
 	}
  }
