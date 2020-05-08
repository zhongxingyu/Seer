 package net.lmxm.suafe.api;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import static net.lmxm.suafe.api.CustomMatchers.containsSameInstance;
 import static net.lmxm.suafe.api.CustomMatchers.emptySet;
 import static net.lmxm.suafe.api.CustomMatchers.immutableSet;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 public final class DocumentTest {
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Test
     public void testAddUserToUserGroup() {
         final Document document = new Document();
 
         // Setup
         final User user = document.createUser("userName", null);
         final UserGroup userGroup = document.createUserGroup("userGroupName");
         assertThat(userGroup.getUsers(), is(emptySet()));
         assertThat(user.getUserGroups(), is(emptySet()));
 
         // Test
         document.addUserToUserGroup(user.getName(), userGroup.getName());
         assertThat(userGroup.getUsers(), is(not(emptySet())));
         assertThat(userGroup.getUsers(), is(containsSameInstance(user)));
         assertThat(user.getUserGroups(), is(not(emptySet())));
         assertThat(user.getUserGroups(), is(containsSameInstance(userGroup)));
     }
 
     @Test
     public void testCloneRepository() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findRepositoryByName("repositoryName"), is(nullValue()));
         assertThat(document.findRepositoryByName("cloneRepositoryName"), is(nullValue()));
         document.createRepository("repositoryName");
         assertThat(document.findRepositoryByName("repositoryName"), is(notNullValue()));
         assertThat(document.findRepositoryByName("cloneRepositoryName"), is(nullValue()));
 
         // Test
         document.cloneRepository("repositoryName", "cloneRepositoryName");
         assertThat(document.findRepositoryByName("repositoryName"), is(notNullValue()));
         assertThat(document.findRepositoryByName("cloneRepositoryName"), is(notNullValue()));
 
         thrown.expect(EntityAlreadyExistsException.class);
         document.cloneRepository("cloneRepositoryName", "repositoryName");
     }
 
     @Test
     public void testCloneUser() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserByName("userName"), is(nullValue()));
         assertThat(document.findUserByName("cloneUserName"), is(nullValue()));
         document.createUser("userName", "userAlias");
         assertThat(document.findUserByName("userName"), is(notNullValue()));
         assertThat(document.findUserByName("userName").getAlias(), is(equalTo("userAlias")));
         assertThat(document.findUserByName("cloneUserName"), is(nullValue()));
 
         // Test
         document.cloneUser("userName", "cloneUserName", "cloneUserAlias");
         assertThat(document.findUserByName("userName"), is(notNullValue()));
         assertThat(document.findUserByName("userName").getAlias(), is(equalTo("userAlias")));
         assertThat(document.findUserByName("cloneUserName"), is(notNullValue()));
         assertThat(document.findUserByName("cloneUserName").getAlias(), is(equalTo("cloneUserAlias")));
 
         thrown.expect(EntityAlreadyExistsException.class);
         document.cloneUser("cloneUserName", "userName", null);
     }
 
     @Test
     public void testCloneUser_Groups() {
         final Document document = new Document();
 
         // Setup
         final User user = document.createUser("userName", "userAlias");
         final UserGroup userGroup = document.createUserGroup("userGroupName");
         document.addUserToUserGroup("userName", "userGroupName");
         assertThat(userGroup.getUsers(), is(not(emptySet())));
         assertThat(userGroup.getUsers().iterator().next(), is(sameInstance(user)));
         assertThat(user.getUserGroups(), is(not(emptySet())));
         assertThat(user.getUserGroups().iterator().next(), is(sameInstance(userGroup)));
 
         // Test
         final User cloneUser = document.cloneUser("userName", "cloneUserName", "cloneUserAlias");
         assertThat(userGroup.getUsers(), is(not(emptySet())));
         assertThat(userGroup.getUsers(), is(containsSameInstance(cloneUser)));
         assertThat(cloneUser.getUserGroups(), is(not(emptySet())));
         assertThat(cloneUser.getUserGroups(), is(containsSameInstance(userGroup)));
     }
 
     @Test
     public void testCloneUserGroup() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserGroupByName("userGroupName"), is(nullValue()));
         assertThat(document.findUserGroupByName("cloneUserGroupName"), is(nullValue()));
         document.createUserGroup("userGroupName");
         assertThat(document.findUserGroupByName("userGroupName"), is(notNullValue()));
         assertThat(document.findUserGroupByName("cloneUserGroupName"), is(nullValue()));
 
         // Test
         document.cloneUserGroup("userGroupName", "cloneUserGroupName");
         assertThat(document.findUserGroupByName("userGroupName"), is(notNullValue()));
         assertThat(document.findUserGroupByName("cloneUserGroupName"), is(notNullValue()));
 
         thrown.expect(EntityAlreadyExistsException.class);
         document.cloneUserGroup("cloneUserGroupName", "userGroupName");
     }
 
     @Test
     public void testCloneUserGroup_Users() {
         final Document document = new Document();
 
         // Setup
         final User user = document.createUser("userName", "userAlias");
         final UserGroup userGroup = document.createUserGroup("userGroupName");
         document.addUserToUserGroup("userName", "userGroupName");
         assertThat(userGroup.getUsers(), is(not(emptySet())));
         assertThat(userGroup.getUsers().iterator().next(), is(sameInstance(user)));
         assertThat(user.getUserGroups(), is(not(emptySet())));
         assertThat(user.getUserGroups().iterator().next(), is(sameInstance(userGroup)));
 
         // Test
         final UserGroup cloneUserGroup = document.cloneUserGroup("userGroupName", "cloneUserGroupName");
         assertThat(cloneUserGroup.getUsers(), is(not(emptySet())));
         assertThat(cloneUserGroup.getUsers(), is(containsSameInstance(user)));
         assertThat(user.getUserGroups(), is(not(emptySet())));
         assertThat(user.getUserGroups(), is(containsSameInstance(cloneUserGroup)));
     }
 
     @Test
     public void testCreateRepository() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findRepositoryByName("repositoryName"), is(nullValue()));
 
         // Test
         document.createRepository("repositoryName");
         assertThat(document.findRepositoryByName("repositoryName"), is(notNullValue()));
 
         thrown.expect(EntityAlreadyExistsException.class);
         document.createRepository("repositoryName");
     }
 
     @Test
     public void testCreateUser() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserByName("userName"), is(nullValue()));
         document.createUser("userName", null);
         assertThat(document.findUserByName("userName"), is(notNullValue()));
 
         // Test
         thrown.expect(EntityAlreadyExistsException.class);
         document.createUser("userName", null);
 
         assertThat(document.findUserByAlias("userAlias"), is(nullValue()));
         document.createUser("userName1", "userAlias");
         assertThat(document.findUserByAlias("userAlias"), is(notNullValue()));
 
         thrown.expect(EntityAlreadyExistsException.class);
         document.createUser("userName2", "userAlias");
     }
 
     @Test
     public void testDeleteRepository() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findRepositoryByName("repositoryName"), is(nullValue()));
         document.createRepository("repositoryName");
         assertThat(document.findRepositoryByName("repositoryName"), is(notNullValue()));
 
         // Test
         document.deleteRepository("repositoryName");
         assertThat(document.findRepositoryByName("repositoryName"), is(nullValue()));
 
         thrown.expect(EntityDoesNotExistException.class);
         document.deleteRepository("repositoryName");
     }
 
     @Test
     public void testDeleteUser() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserByName("userName"), is(nullValue()));
         document.createUser("userName", null);
         assertThat(document.findUserByName("userName"), is(notNullValue()));
 
         // Test
         document.deleteUser("userName");
         assertThat(document.findUserByName("userName"), is(nullValue()));
 
         thrown.expect(EntityDoesNotExistException.class);
         document.deleteUser("userName");
     }
 
     @Test
     public void testDeleteUser_Groups() {
         final Document document = new Document();
 
         // Setup
         final UserGroup userGroup = document.createUserGroup("userGroupName");
         final User user = document.createUser("userName", null);
         document.addUserToUserGroup("userName", "userGroupName");
         assertThat(userGroup.getUsers(), is(containsSameInstance(user)));
         assertThat(user.getUserGroups(), is(containsSameInstance(userGroup)));
 
         // Test
         document.deleteUser("userName");
         assertThat(userGroup.getUsers(), is(emptySet()));
     }
 
     @Test
     public void testDeleteUserGroup() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserGroupByName("userGroupName"), is(nullValue()));
         document.createUserGroup("userGroupName");
         assertThat(document.findUserGroupByName("userGroupName"), is(notNullValue()));
 
         // Test
         document.deleteUserGroup("userGroupName");
         assertThat(document.findUserGroupByName("userGroupName"), is(nullValue()));
 
         thrown.expect(EntityDoesNotExistException.class);
         document.deleteUserGroup("userGroupName");
     }
 
     @Test
     public void testDeleteUserGroup_Users() {
         final Document document = new Document();
 
         // Setup
         final UserGroup userGroup = document.createUserGroup("userGroupName");
         final User user = document.createUser("userName", null);
         document.addUserToUserGroup("userName", "userGroupName");
         assertThat(userGroup.getUsers(), is(containsSameInstance(user)));
         assertThat(user.getUserGroups(), is(containsSameInstance(userGroup)));
 
         // Test
         document.deleteUserGroup("userGroupName");
        assertThat(user.getUserGroups(), is(emptySet()));
     }
 
     @Test
     public void testFindRepositoryByName() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findRepositoryByName("repositoryName"), is(nullValue()));
         document.createRepository("repositoryName");
 
         // Test
         assertThat(document.findRepositoryByName("repositoryName"), is(notNullValue()));
         assertThat(document.findRepositoryByName("repositoryName").getName(), is(equalTo("repositoryName")));
     }
 
     @Test
     public void testFindUserByAlias() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserByAlias("userAlias"), is(nullValue()));
         document.createUser("userName", "userAlias");
 
         // Test
         assertThat(document.findUserByAlias("userAlias"), is(notNullValue()));
         assertThat(document.findUserByAlias("userAlias").getAlias(), is(equalTo("userAlias")));
     }
 
     @Test
     public void testFindUserByName() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserByName("userName"), is(nullValue()));
         document.createUser("userName", null);
 
         // Test
         assertThat(document.findUserByName("userName"), is(notNullValue()));
         assertThat(document.findUserByName("userName").getName(), is(equalTo("userName")));
     }
 
     @Test
     public void testFindUserGroupByName() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserGroupByName("userGroupName"), is(nullValue()));
         document.createUserGroup("userGroupName");
 
         // Test
         assertThat(document.findUserGroupByName("userGroupName"), is(notNullValue()));
         assertThat(document.findUserGroupByName("userGroupName").getName(), is(equalTo("userGroupName")));
     }
 
     @Test
     public void testGetRepositories() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.getRepositories(), is(notNullValue()));
         assertThat(document.getRepositories(), is(emptySet()));
         document.createRepository("repositoryName");
 
         // Test
         assertThat(document.getRepositories(), is(notNullValue()));
         assertThat(document.getRepositories(), is(not(emptySet())));
         assertThat(document.getRepositories(), is(immutableSet()));
     }
 
     @Test
     public void testGetUsers() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.getUsers(), is(notNullValue()));
         assertThat(document.getUsers(), is(emptySet()));
         document.createUser("userName", "userAlias");
 
         // Test
         assertThat(document.getUsers(), is(notNullValue()));
         assertThat(document.getUsers(), is(not(emptySet())));
         assertThat(document.getUsers(), is(immutableSet()));
     }
 
     @Test
     public void testGetUserGroups() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.getUserGroups(), is(notNullValue()));
         assertThat(document.getUserGroups(), is(emptySet()));
         document.createUserGroup("userGroupName");
 
         // Test
         assertThat(document.getUserGroups(), is(notNullValue()));
         assertThat(document.getUserGroups(), is(not(emptySet())));
         assertThat(document.getUserGroups(), is(immutableSet()));
     }
 
     @Test
     public void testRenameRepository() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findRepositoryByName("repositoryName"), is(nullValue()));
         assertThat(document.findRepositoryByName("newRepositoryName"), is(nullValue()));
         document.createRepository("repositoryName");
         assertThat(document.findRepositoryByName("repositoryName"), is(notNullValue()));
         assertThat(document.findRepositoryByName("repositoryName").getName(), is(equalTo("repositoryName")));
         assertThat(document.findRepositoryByName("newRepositoryName"), is(nullValue()));
 
         // Test
         document.renameRepository("repositoryName", "newRepositoryName");
         assertThat(document.findRepositoryByName("newRepositoryName"), is(notNullValue()));
         assertThat(document.findRepositoryByName("newRepositoryName").getName(), is(equalTo("newRepositoryName")));
         assertThat(document.findRepositoryByName("repositoryName"), is(nullValue()));
     }
 
     @Test
     public void testRenameUserAlias() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserByAlias("userAlias"), is(nullValue()));
         assertThat(document.findUserByAlias("newUserAlias"), is(nullValue()));
         document.createUser("userName", "userAlias");
         assertThat(document.findUserByAlias("userAlias"), is(notNullValue()));
         assertThat(document.findUserByAlias("userAlias").getAlias(), is(equalTo("userAlias")));
         assertThat(document.findUserByAlias("newUserAlias"), is(nullValue()));
 
         // Test
         document.renameUser("userName", "userName", "newUserAlias");
         assertThat(document.findUserByAlias("newUserAlias"), is(notNullValue()));
         assertThat(document.findUserByAlias("newUserAlias").getAlias(), is(equalTo("newUserAlias")));
         assertThat(document.findUserByAlias("userAlias"), is(nullValue()));
     }
 
     @Test
     public void testRenameUserName() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserByName("userName"), is(nullValue()));
         assertThat(document.findUserByName("newUserName"), is(nullValue()));
         document.createUser("userName", "userAlias");
         assertThat(document.findUserByName("userName"), is(notNullValue()));
         assertThat(document.findUserByName("userName").getName(), is(equalTo("userName")));
         assertThat(document.findUserByName("newUserName"), is(nullValue()));
 
         // Test
         document.renameUser("userName", "newUserName", "userAlias");
         assertThat(document.findUserByName("newUserName"), is(notNullValue()));
         assertThat(document.findUserByName("newUserName").getName(), is(equalTo("newUserName")));
         assertThat(document.findUserByName("userName"), is(nullValue()));
     }
 
     @Test
     public void testRenameUserGroup() {
         final Document document = new Document();
 
         // Setup
         assertThat(document.findUserGroupByName("userGroupName"), is(nullValue()));
         assertThat(document.findUserGroupByName("newUserGroupName"), is(nullValue()));
         document.createUserGroup("userGroupName");
         assertThat(document.findUserGroupByName("userGroupName"), is(notNullValue()));
         assertThat(document.findUserGroupByName("userGroupName").getName(), is(equalTo("userGroupName")));
         assertThat(document.findUserGroupByName("newUserGroupName"), is(nullValue()));
 
         // Test
         document.renameUserGroup("userGroupName", "newUserGroupName");
         assertThat(document.findUserGroupByName("newUserGroupName"), is(notNullValue()));
         assertThat(document.findUserGroupByName("newUserGroupName").getName(), is(equalTo("newUserGroupName")));
         assertThat(document.findUserGroupByName("userGroupName"), is(nullValue()));
     }
 }
