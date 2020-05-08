 package de.enwida.web;
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.enwida.web.dao.interfaces.IGroupDao;
 import de.enwida.web.dao.interfaces.IRightDao;
 import de.enwida.web.dao.interfaces.IRoleDao;
 import de.enwida.web.model.Group;
 import de.enwida.web.model.Right;
 import de.enwida.web.model.Role;
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IUserService;
 import de.enwida.web.utils.Constants;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/root-context-test.xml")
 public class BasicUserManagement {
     
 	private static boolean isDbSchemaRecreated = false;
 	
 	@Autowired
 	private TestUtils testUtils;
 	
     @Autowired
     private IUserService userService;
 
 	@Autowired
 	private IGroupDao groupDao;
 
 	@Autowired
 	private IRoleDao roleDao;
 
 	@Autowired
 	private IRightDao rightDao;
 
 	@Autowired
 	private DataSource dataSource;
 	
 	@Before
 	public void cleanup() throws Exception {
 		final Connection connection = dataSource.getConnection();
 		
 		if (!isDbSchemaRecreated) {
 			TestUtils.recreateUsersSchema(connection);
 			isDbSchemaRecreated = true;
 		}
 
 		TestUtils.cleanupDatabase(connection);
 		connection.close();
 	}
 	
 	/***
 	 ***** Basic persistence tests
 	 ***/
 	
 	@Test
 	public void userIsSaved() throws Exception {
 		final User user = testUtils.saveTestUser("testuser");
 		final User testee = userService.fetchUser("testuser");
 		
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
 	public void userIsAssignedToAnonymousGroup() throws Exception {
 		final User user = testUtils.saveTestUser("testuser");
 		Assert.assertEquals(1, user.getGroups().size());
 		Assert.assertEquals(Constants.ANONYMOUS_GROUP, user.getGroups().iterator().next().getGroupName());
 	}
 	
 	@Ignore
 	@Test
 	public void cannotAddUserWithSameUsername() throws Exception {
 		final User user1 = new User("testuser1@pleasedontsendmailshere.com", "test", "secret", "test", "test", true);
 		user1.setCompanyName("enwida.de");
 		userService.saveUser(user1,false);
 
 		final User user2 = new User("testuser2@pleasedontsendmailshere.com", "test", "secret", "test", "test", true);
 		user2.setCompanyName("enwida.de");
 		try {
 			userService.saveUser(user2,false);
 		} catch (Exception e) {
 			// Expected
 			return;
 		}
 		throw new Exception("Should not be reachable; exception when adding user with same name expected");
 	}
  	public void cannotAddUserWithSameEmail() throws Exception {
 		final User user1 = new User("testuser@pleasedontsendmailshere.com", "test1", "secret", "test", "test", true);
 		user1.setCompanyName("enwida.de");
 		userService.saveUser(user1,false);
 
 		final User user2 = new User("testuser@pleasedontsendmailshere.com", "test2", "secret", "test", "test", true);
 		user2.setCompanyName("enwida.de");
 		try {
 			userService.saveUser(user2,false);
 		} catch (Exception e) {
 			// Expected
 			return;
 		}
 		throw new Exception("Should not be reachable; exception when adding user with same email expected");
 	}
 	
 	@Test
 	public void groupIsSaved() throws Exception {
 		final Group group = testUtils.saveTestGroup("testgroup");
 		final Group testee = userService.fetchGroup("testgroup");
 		
 		// Instance is fresh from database
 		Assert.assertFalse(group == testee);
 		
 		Assert.assertNotNull(testee);
 		Assert.assertEquals(group, testee);
 		Assert.assertEquals(group.getGroupID(), testee.getGroupID());
 		Assert.assertEquals(group.getGroupName(), testee.getGroupName());
 	}
 	
 	@Ignore
 	@Test
 	public void cannotAddGroupWithSameName() throws Exception {
 		testUtils.saveTestGroup("testgroup");
 		
 		try {
 			testUtils.saveTestGroup("testgroup");
 		} catch (Exception e) {
 			// Expected
 			return;
 		}
 		throw new Exception("Should not be reachable; exception when adding group with same name expected");
 	}
 	
 	@Test
 	public void roleIsSaved() throws Exception {
 		final Role role = testUtils.saveTestRole("testrole");
 		final Role testee = userService.fetchRole("testrole");
 		
 		// Instance if fresh from database
 		Assert.assertFalse(role == testee);
 		
 		Assert.assertNotNull(testee);
 		Assert.assertEquals(role, testee);
 		Assert.assertEquals(role.getRoleID(), testee.getRoleID());
 		Assert.assertEquals(role.getRoleName(), testee.getRoleName());
 	}
 
 	@Ignore
 	@Test
 	public void cannotAddRoleWithSameName() throws Exception {
 		testUtils.saveTestRole("testrole");
 		
 		try {
 			testUtils.saveTestRole("testrole");
 		} catch (Exception e) {
 			// Expected
 			return;
 		}
 		throw new Exception("Should not be reachable; exception when adding role with same name expected");
 	}
 	
 	@Test
 	public void rightIsSaved() throws Exception {
 		final Right right = testUtils.saveTestRight(211);
 		final Right testee = userService.fetchRight(right.getRightID());
 		
 		// Instance is fresh from database
 		Assert.assertFalse(right == testee);
 		
 		Assert.assertNotNull(testee);
 		Assert.assertEquals(right, testee);
 		Assert.assertEquals(right.getRightID(), testee.getRightID());
 		Assert.assertEquals(right.getAspect(), testee.getAspect());
 		Assert.assertEquals(right.getResolution(), testee.getResolution());
 		Assert.assertEquals(right.getProduct(), testee.getProduct());
 		Assert.assertEquals(right.getTimeRange(), testee.getTimeRange());
 		Assert.assertEquals(right.getTso(), testee.getTso());
 	}
 
 	/***
 	 ***** Users <-> Groups relationship tests
 	 ***/
 
 	@Test
 	public void addGroupsToUser() throws Exception {
 		final User user = testUtils.saveTestUser("testuser");
 		final Group group1 = testUtils.saveTestGroup("testgroup1");
 		final Group group2 = testUtils.saveTestGroup("testgroup2");
 		
 		Assert.assertEquals(1, user.getGroups().size());
 		
 		final Group freshGroup1 = userService.assignGroupToUser(user, group1);
 		final Group freshGroup2 = userService.assignGroupToUser(user, group2);
 		
 		// Groups got added to user object
 		Assert.assertEquals(3, user.getGroups().size());
 		Assert.assertTrue(user.getGroups().contains(group1));
 		Assert.assertTrue(user.getGroups().contains(group2));
 		
 		// User got added to fresh group objects
 		Assert.assertEquals(1, freshGroup1.getAssignedUsers().size());
 		Assert.assertTrue(freshGroup1.getAssignedUsers().contains(user));
 		Assert.assertEquals(1, freshGroup2.getAssignedUsers().size());
 		Assert.assertTrue(freshGroup2.getAssignedUsers().contains(user));
 		
 		// Note: User might NOT be to stale groups objects
 		
 		// Groups got added to freshly fetched user object
 		final User fetchedUser = userService.fetchUser("testuser");
 		Assert.assertEquals(3, fetchedUser.getGroups().size());
 		Assert.assertTrue(fetchedUser.getGroups().contains(group1));
 		Assert.assertTrue(fetchedUser.getGroups().contains(group2));
 		
 		// User got added to freshly fetched group objects
 		final Group fetchedGroup1 = userService.fetchGroup("testgroup1");
 		final Group fetchedGroup2 = userService.fetchGroup("testgroup2");
 
 		Assert.assertEquals(1, fetchedGroup1.getAssignedUsers().size());
 		Assert.assertTrue(fetchedGroup1.getAssignedUsers().contains(user));
 		Assert.assertEquals(1, fetchedGroup2.getAssignedUsers().size());
 		Assert.assertTrue(fetchedGroup2.getAssignedUsers().contains(user));
 	}
 	
 	@Test public void addGroupsToUserById() throws Exception {
 		final User user = testUtils.saveTestUser("testuser");
 		final Group group1 = testUtils.saveTestGroup("testgroup1");
 		final Group group2 = testUtils.saveTestGroup("testgroup2");
 		
 		Assert.assertEquals(1, user.getGroups().size());
 		
 		userService.assignGroupToUser(user.getUserId(), group1.getGroupID());
 		userService.assignGroupToUser(user.getUserId(), group2.getGroupID());
 		
 		// Note: Changes might NOT be reflected in stale objects
 		
 		// Changes are visible as soon as you fetch new objects
 		final User fetchedUser = userService.fetchUser("testuser");
 		final Group fetchedGroup1 = userService.fetchGroup("testgroup1");
 		final Group fetchedGroup2 = userService.fetchGroup("testgroup1");
 		
 		Assert.assertEquals(3, fetchedUser.getGroups().size());
 		Assert.assertEquals(1, fetchedGroup1.getAssignedUsers().size());
 		Assert.assertEquals(1, fetchedGroup2.getAssignedUsers().size());
 	}
 	
 	@Test
 	public void addGroupToSeveralUsers() throws Exception {
 		final User user1 = testUtils.saveTestUser("testuser1");
 		final User user2 = testUtils.saveTestUser("testuser2");
 		Group group = testUtils.saveTestGroup("testgroup");
 		
 		Assert.assertEquals(1, user1.getGroups().size());
 		Assert.assertEquals(1, user2.getGroups().size());
 		Assert.assertEquals(0, group.getAssignedUsers().size());
 		
 		group = userService.assignGroupToUser(user1, group);
 		Assert.assertEquals(2, user1.getGroups().size());
 		Assert.assertEquals(1, group.getAssignedUsers().size());
 
 		group = userService.assignGroupToUser(user2, group);
 		Assert.assertEquals(2, user2.getGroups().size());
 		Assert.assertEquals(2, group.getAssignedUsers().size());
 	}
 	
 	@Test
 	public void addGroupTwice() throws Exception {
 		final User user = testUtils.saveTestUser("testuser");
 		final Group group1 = testUtils.saveTestGroup("testgroup1");
 
 		Assert.assertEquals(1, user.getGroups().size());
 		
 		userService.assignGroupToUser(user, group1);
 
 		// Second add succeeds due to set semantics
 		final Group freshGroup1 = userService.assignGroupToUser(user, group1);
 		
 		Assert.assertEquals(2, user.getGroups().size());
 		Assert.assertEquals(1, freshGroup1.getAssignedUsers().size());
 	}
 	
 	@Test
 	public void addNonPersistedGroup() throws Exception {
 		final User user = testUtils.saveTestUser("testuser");
 
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
 		final User user = testUtils.saveTestUser("testuser");
 		final Group group1 = testUtils.saveTestGroup("testgroup1");
 		final Group group2 = testUtils.saveTestGroup("testgroup2");
 
 		Assert.assertEquals(1, user.getGroups().size());
 		
 		userService.assignGroupToUser(user, group1);
 		userService.assignGroupToUser(user, group2);
 
 		Assert.assertEquals(3, user.getGroups().size());
 		
 		final Group freshGroup1 = userService.revokeUserFromGroup(user, group1);
 		
 		// Group was removed from user object
 		Assert.assertEquals(2, user.getGroups().size());
 		Assert.assertTrue(user.getGroups().contains(group2));
 
 		// User is removed from fresh group object
 		Assert.assertTrue(freshGroup1.getAssignedUsers().isEmpty());
 
 		// Group is removed from freshly fetched user object
 		final User fetchedUser = userService.fetchUser("testuser");
 		Assert.assertEquals(2, fetchedUser.getGroups().size());
 		Assert.assertTrue(fetchedUser.getGroups().contains(group2));
 		
 		// User is removed from freshly fetched group object
 		final Group fetchedGroup = userService.fetchGroup("testgroup1");
 		Assert.assertTrue(fetchedGroup.getAssignedUsers().isEmpty());
 	}
 	
 	@Test
 	public void doNotAllowDirectModificationOfUserGroupsRelationship() throws Exception {
 		final User user = testUtils.saveTestUser("testuser");
 		final Group group = testUtils.saveTestGroup("testgroup");
 		
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
 	
 
 	/***
 	 ***** Groups <-> Roles relationship tests
 	 ***/
 
 	@Test
 	public void addRolesToGroup() throws Exception {
 		final Group group = testUtils.saveTestGroup("testgroup");
 		final Role role1 = testUtils.saveTestRole("testrole1");
 		final Role role2 = testUtils.saveTestRole("testrole2");
 		
 		Assert.assertTrue(group.getAssignedRoles().isEmpty());
 		
 		final Role freshRole1 = userService.assignRoleToGroup(role1, group);
 		final Role freshRole2 = userService.assignRoleToGroup(role2, group);
 		
 		// Roles were added to group object
 		Assert.assertEquals(2, group.getAssignedRoles().size());
 		Assert.assertTrue(group.getAssignedRoles().contains(role1));
 		Assert.assertTrue(group.getAssignedRoles().contains(role2));
 		
 		// Group was added to fresh role objects
 		Assert.assertEquals(1, freshRole1.getAssignedGroups().size());
 		Assert.assertTrue(freshRole1.getAssignedGroups().contains(group));
 		Assert.assertEquals(1, freshRole2.getAssignedGroups().size());
 		Assert.assertTrue(freshRole2.getAssignedGroups().contains(group));
 		
 		// Note: Group might NOT have been added to stale role objects
 		
 		// Roles were added to freshly fetched group object
 		final Group fetchedGroup = userService.fetchGroup("testgroup");
 		Assert.assertEquals(2, fetchedGroup.getAssignedRoles().size());
 		Assert.assertTrue(fetchedGroup.getAssignedRoles().contains(role1));
 		Assert.assertTrue(fetchedGroup.getAssignedRoles().contains(role2));
 		
 		// Group was added to freshly fetched role objects
 		final Role fetchedRole1 = userService.fetchRole("testrole1");
 		final Role fetchedRole2 = userService.fetchRole("testrole2");
 		
 		Assert.assertEquals(1, fetchedRole1.getAssignedGroups().size());
 		Assert.assertTrue(fetchedRole1.getAssignedGroups().contains(group));
 		Assert.assertEquals(1, fetchedRole2.getAssignedGroups().size());
 		Assert.assertTrue(freshRole2.getAssignedGroups().contains(group));
 	}
 
 	@Test public void addRolesToGroupById() throws Exception {
 		final Group group = testUtils.saveTestGroup("testgroup");
 		final Role role1 = testUtils.saveTestRole("testrole1");
 		final Role role2 = testUtils.saveTestRole("testrole2");
 		
 		Assert.assertTrue(group.getAssignedRoles().isEmpty());
 		
 		userService.assignRoleToGroup(role1.getRoleID(), group.getGroupID());
 		userService.assignRoleToGroup(role2.getRoleID(), group.getGroupID());
 		
 		// Note: Changes might NOT be reflected in stale objects
 		
 		// Changes are visible as soon as you fetch new objects
 		final Group fetchedGroup = userService.fetchGroup("testgroup");
 		final Role fetchedRole1 = userService.fetchRole("testrole1");
 		final Role fetchedRole2 = userService.fetchRole("testrole2");
 		
 		Assert.assertEquals(2, fetchedGroup.getAssignedRoles().size());
 		Assert.assertEquals(1, fetchedRole1.getAssignedGroups().size());
 		Assert.assertEquals(1, fetchedRole2.getAssignedGroups().size());
 	}
 	
 	@Test
 	public void addRoleTwice() throws Exception {
 		final Group group = testUtils.saveTestGroup("testgroup");
 		final Role role = testUtils.saveTestRole("testrole");
 		
 		userService.assignRoleToGroup(role, group);
 
 		// Second add succeeds due to set semantics
 		final Role freshRole = userService.assignRoleToGroup(role, group);
 		
 		Assert.assertEquals(1, group.getAssignedRoles().size());
 		Assert.assertEquals(1, freshRole.getAssignedGroups().size());
 	}
 	
 	@Test
 	public void addNonPersistedRole() throws Exception {
 		final Group group = new Group("testgroup");
 
 		// Role is not persisted 
 		final Role role = new Role("testrole");
 		
 		// Assigning it should cause an exception
 		try {
 			userService.assignRoleToGroup(role, group);
 			throw new Exception("Shouldn't be reachable; IllegalArgumentException expected");
 		} catch (IllegalArgumentException e) {
 			// Expected
 			Assert.assertTrue(e.getMessage().toLowerCase().contains("persisted"));
 		}
 	}
 	
 	@Test
 	public void revokeRolesFromUser() throws Exception {
 		final Group group = testUtils.saveTestGroup("testgroup");
 		final Role role1 = testUtils.saveTestRole("testrole1");
 		final Role role2 = testUtils.saveTestRole("testrole2");
 		
 		userService.assignRoleToGroup(role1, group);
 		userService.assignRoleToGroup(role2, group);
 		
 		final Role freshRole1 = userService.revokeRoleFromGroup(role1, group);
 		
 		// Role was removed from group object
 		Assert.assertEquals(1, group.getAssignedRoles().size());
 		Assert.assertTrue(group.getAssignedRoles().contains(role2));
 
 		// Group is removed from fresh role object
 		Assert.assertTrue(freshRole1.getAssignedGroups().isEmpty());
 
 		// Role is removed from freshly fetched group object
 		final Group fetchedGroup = userService.fetchGroup("testgroup");
 		Assert.assertEquals(1, fetchedGroup.getAssignedRoles().size());
 		Assert.assertTrue(fetchedGroup.getAssignedRoles().contains(role2));
 		
 		// User is removed from freshly fetched group object
 		final Role fetchedRole1 = userService.fetchRole("testrole1");
 		Assert.assertTrue(fetchedRole1.getAssignedGroups().isEmpty());
 	}
 	
 	@Test
 	public void doNotAllowDirectModificationOfGroupRoleRelationship() throws Exception {
 		final Group group = testUtils.saveTestGroup("testgroup");
 		final Role role = testUtils.saveTestRole("testrole");
 		
 		try {
 			group.getAssignedRoles().add(role);
 			throw new Exception("Shouldn't be reachable; UnsupportedOperationException expected");
 		} catch (UnsupportedOperationException e) {
 			// Expected
 		}
 		
 		try {
 			role.getAssignedGroups().add(group);
 			throw new Exception("Shouldn't be reachable; UnsupportedOperationException expected");
 		} catch (UnsupportedOperationException e) {
 			// Expected
 		}
 	}
 
 	/***
 	 ***** Role -> Rights relationship tests
 	 ***/
 
 	@Test
 	public void addNonPersistedRight() throws Exception {
 		final Role role = testUtils.saveTestRole("testrole");
 
 		// Right is not persisted 
 		final Right right = new Right();
 		
 		// Assigning it should cause an exception
 		try {
 			userService.assignRightToRole(right, role);
 			throw new Exception("Shouldn't be reachable; IllegalArgumentException expected");
 		} catch (IllegalArgumentException e) {
 			// Expected
 			Assert.assertTrue(e.getMessage().toLowerCase().contains("persisted"));
 		}
 	}
 	
 	@Test
 	@Transactional 
 	public void revokeRightFromRole() throws Exception {
 		final Role role = testUtils.saveTestRole("testrole");
 		final Right right1 = testUtils.saveTestRight(211);
 		final Right right2 = testUtils.saveTestRight(221);
 		
 		userService.assignRightToRole(right1, role);
 		userService.assignRightToRole(right2, role);
 		
 		final Role freshRole = userService.revokeRightFromRole(right1, role);
 		
 		// Role is removed from right
 		Assert.assertNull(right1.getRole());
 		Assert.assertEquals(role, right2.getRole());
 		
 		// Right is removed from fresh role
 		Assert.assertEquals(1, freshRole.getRights().size());
 		Assert.assertTrue(freshRole.getRights().contains(right2));
 		
 		// Role is removed from freshly fetched rights
 		final Right fetchedRight1 = userService.fetchRight(right1.getRightID());
 		final Right fetchedRight2 = userService.fetchRight(right2.getRightID());
 		Assert.assertNull(fetchedRight1.getRole());
 		Assert.assertEquals(role, fetchedRight2.getRole());
 		
 		// Right is removed from freshly fetched role
 		final Role fetchedRole = userService.fetchRole("testrole");
 		Assert.assertEquals(1, freshRole.getRights().size());
 		Assert.assertTrue(fetchedRole.getRights().contains(right2));
 	}
 
 	
 	@Test
 	@Transactional 
 	public void addRightsToRole() throws Exception {
 		final Role role = testUtils.saveTestRole("testrole");
 		final Right right1 = testUtils.saveTestRight(211);
 		final Right right2 = testUtils.saveTestRight(221);
 		
 		Assert.assertTrue(role.getRights().isEmpty());
 		
 		userService.assignRightToRole(right1, role);
 		final Role freshRole = userService.assignRightToRole(right2, role);
 		
 		// Role is assigned to right objects
 		Assert.assertEquals(role, right1.getRole());
 		Assert.assertEquals(role, right2.getRole());
 
 		// Rights were added to fresh role object
 		Assert.assertEquals(2, freshRole.getRights().size());
 		Assert.assertTrue(freshRole.getRights().contains(right1));
 		Assert.assertTrue(freshRole.getRights().contains(right2));
 		
 		// Role is assigned to freshly fetched right objects
 		final Right fetchedRight1 = userService.fetchRight(right1.getRightID());
 		final Right fetchedRight2 = userService.fetchRight(right2.getRightID());
 
 		// Rights were assigned to freshly fetched role object
 		final Role fetchedRole = userService.fetchRole("testrole");
 		Assert.assertEquals(2, fetchedRole.getRights().size());
 		Assert.assertTrue(fetchedRole.getRights().contains(right1));
 		Assert.assertTrue(fetchedRole.getRights().contains(right2));
 		
 		Assert.assertEquals(role, fetchedRight1.getRole());
 		Assert.assertEquals(role, fetchedRight2.getRole());
 	}
 	
 	@Test
 	public void assignRightTwice() throws Exception {
 		final Role role = testUtils.saveTestRole("testrole");
 		final Right right = testUtils.saveTestRight(211);
 		
 		userService.assignRightToRole(right, role);
 
 		// Second add succeeds due to set semantics
 		final Role freshRole = userService.assignRightToRole(right, role);
 
 		Assert.assertEquals(1, freshRole.getRights().size());
 		Assert.assertTrue(freshRole.getRights().contains(right));
 		Assert.assertEquals(role, right.getRole());
 	}
 	
 	@Test
 	@Transactional
 	public void transitiveClosure() throws Exception {
 		final User user1 = testUtils.saveTestUser("testuser1");
 		final User user2 = testUtils.saveTestUser("testuser2");
 		final Group group1 = testUtils.saveTestGroup("testgroup1");
 		final Group group2 = testUtils.saveTestGroup("testgroup2");
 		final Group group3 = testUtils.saveTestGroup("testgroup3");
 		final Role role1 = testUtils.saveTestRole("testrole1");
 		final Role role2 = testUtils.saveTestRole("testrole2");
 		final Role role3 = testUtils.saveTestRole("testrole3");
 		final Role role4 = testUtils.saveTestRole("testrole4");
 		final Right right1 = testUtils.saveTestRight(211);
 		final Right right2 = testUtils.saveTestRight(221);
 		final Right right3 = testUtils.saveTestRight(222);
 		final Right right4 = testUtils.saveTestRight(322);
 		final Right right5 = testUtils.saveTestRight(312);
 		final Right right6 = testUtils.saveTestRight(313);
 		
 		// Create the mappings
 		userService.assignGroupToUser(user1, group1);
 		userService.assignGroupToUser(user1, group2);
 		userService.assignGroupToUser(user2, group2);
 		userService.assignGroupToUser(user2, group3);
 		
 		userService.assignRoleToGroup(role1, group1);
 		userService.assignRoleToGroup(role2, group1);
 		userService.assignRoleToGroup(role3, group2);
 		userService.assignRoleToGroup(role4, group3);
 		
 		userService.assignRightToRole(right1, role1);
 		userService.assignRightToRole(right2, role1);
 		userService.assignRightToRole(right3, role2);
 		userService.assignRightToRole(right4, role2);
 		userService.assignRightToRole(right5, role3);
 		userService.assignRightToRole(right6, role4);
 
 		// Check user 1
 		final User freshUser1 = userService.fetchUser("testuser1");
 		Assert.assertEquals(3, freshUser1.getGroups().size());
 		Assert.assertTrue(freshUser1.getGroups().contains(group1));
 		Assert.assertTrue(freshUser1.getGroups().contains(group2));
 				
 		final List<Role> allRoles1 = new ArrayList<Role>();
 		for (final Group group : freshUser1.getGroups()) {
 			allRoles1.addAll(group.getAssignedRoles());
 		}
 		Assert.assertEquals(3, allRoles1.size());
 		Assert.assertEquals(3, freshUser1.getAllRoles().size());
 		
 		final List<Right> allRights1 = new ArrayList<Right>();
 		for (final Role role : allRoles1) {
 			allRights1.addAll(role.getRights());
 		}
 		Assert.assertEquals(5, allRights1.size());
 		Assert.assertEquals(5, freshUser1.getAllRights().size());
 
 		// Check user 2
 		final User freshUser2 = userService.fetchUser("testuser2");
 		Assert.assertEquals(3, freshUser2.getGroups().size());
 		Assert.assertTrue(freshUser2.getGroups().contains(group2));
 		Assert.assertTrue(freshUser2.getGroups().contains(group3));
 				
 		final List<Role> allRoles2 = new ArrayList<Role>();
 		for (final Group group : freshUser2.getGroups()) {
 			allRoles2.addAll(group.getAssignedRoles());
 		}
 		Assert.assertEquals(2, allRoles2.size());
 		Assert.assertEquals(2, freshUser2.getAllRoles().size());
 		
 		final List<Right> allRights2 = new ArrayList<Right>();
 		for (final Role role : allRoles2) {
 			allRights2.addAll(role.getRights());
 		}
 		Assert.assertEquals(2, allRights2.size());
 		Assert.assertEquals(2, freshUser2.getAllRights().size());
 		
 		// Check groups
 		final Group freshGroup1 = userService.fetchGroup("testgroup1");
 		Assert.assertEquals(4, freshGroup1.getAllRights().size());
 
 		final Group freshGroup2 = userService.fetchGroup("testgroup2");
 		Assert.assertEquals(1, freshGroup2.getAllRights().size());
 
 		final Group freshGroup3 = userService.fetchGroup("testgroup3");
 		Assert.assertEquals(1, freshGroup3.getAllRights().size());
 	}
 	
 	@Test
 	public void testAutoPass() throws Exception {      
         //Group 1 with autopass
 		Group group = new Group("enwida-test.de");
 		group.setAutoPass(true);
 		group = userService.saveGroup(group);
         
         //Group 2 with autopass
         Group group2 = new Group("enwida-test.de2");
         group2.setAutoPass(true);
         group2 = userService.saveGroup(group2);   
         
         //Group 3 without autopass
        Group group3 = new Group("enwida-test.de2");
         group3.setAutoPass(false);
         group3 = userService.saveGroup(group3);
 		
 	    final User user = new User("test@enwida-test.de", "testuser", "secret", "Test", "User", true);
 		user.setCompanyName("enwida-test.de");
 		userService.saveUser(user,false);
 		
		Assert.assertEquals(1, user.getGroups().size());
        userService.assignGroupToUser(user, group);
        Assert.assertEquals(2, user.getGroups().size());
         userService.assignGroupToUser(user, group2);
         Assert.assertEquals(3, user.getGroups().size());
         userService.assignGroupToUser(user, group3);
         Assert.assertEquals(4, user.getGroups().size());
 
	    final User testee = new User("test2@enwida-test.de", "testuser2", "secret", "Test", "User", true);
 		testee.setCompanyName("enwida-test.de");
 		userService.saveUser(testee,false);
 		
		Assert.assertEquals(3, testee.getGroups().size());
		//Check first group is assigned
        Assert.assertTrue(testee.getGroups().contains(group));
        //Check second group is assigned
        Assert.assertTrue(testee.getGroups().contains(group2));
        //Check anonymous is assigned
        for (final Group g : testee.getGroups()) {
            Assert.assertTrue(g.getGroupName().equals(Constants.ANONYMOUS_GROUP));
        }
        //Check 3rd group is NOT assigned
        Assert.assertTrue(!testee.getGroups().contains(group));
 	}
 
 
     @Test
     public void testRemoveOneUser() throws Exception {
         testUtils.saveTestUser("testuser1");
         User user=testUtils.saveTestUser("testuser2");
         userService.deleteUser(user.getUserId());
         //verify other user remains
         User testee=userService.fetchUser("testuser1");
         Assert.assertNotNull(testee);
     }
 
 
     @Test
     public void testEnableDisableAutoPass() throws Exception {
         final Group group = testUtils.saveTestGroup("testgroup");
         final Group testee=userService.findGroup(new Group("testgroup"));
         Assert.assertTrue(!testee.isAutoPass());
         userService.enableDisableAutoPass(group.getGroupID(),true);
         final Group testee2=userService.findGroup(new Group("testgroup"));
         Assert.assertTrue(testee2.isAutoPass());
     }
 
  }
