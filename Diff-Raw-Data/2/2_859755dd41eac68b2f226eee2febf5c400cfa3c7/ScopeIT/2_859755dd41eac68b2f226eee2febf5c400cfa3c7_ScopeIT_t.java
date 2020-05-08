 package de.osiam.client;
 
 import java.util.UUID;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.connector.OsiamConnector;
 import org.osiam.client.exception.ForbiddenException;
 import org.osiam.client.oauth.AccessToken;
 import org.osiam.client.oauth.GrantType;
 import org.osiam.client.oauth.Scope;
 import org.osiam.client.query.Query;
 import org.osiam.client.update.UpdateGroup;
 import org.osiam.client.update.UpdateUser;
 import org.osiam.resources.scim.Group;
 import org.osiam.resources.scim.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseSetup("/database_seed.xml")
 public class ScopeIT {
 
     static final private UUID VALID_USER_UUID = UUID.fromString("834b410a-943b-4c80-817a-4465aed037bc");
     static final private UUID VALID_GROUP_UUID = UUID.fromString("69e1a5dc-89be-4343-976c-b5541af249f4");
     private String endpointAddress = "http://localhost:8180/osiam-server";
     private String clientId = "example-client";
     private String clientSecret = "secret";
     private OsiamConnector oConnector;
     private AccessToken accessToken;
     private OsiamConnector.Builder oConBuilder;
     
     @Before
     public void setUp() throws Exception {
 
         oConBuilder = new OsiamConnector.Builder(endpointAddress).
                 setClientId(clientId).
                 setClientSecret(clientSecret).
                 setGrantType(GrantType.PASSWORD).
                 setUserName("marissa").
                 setPassword("koala")
                 ;
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_retrieve_user_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	
     	oConBuilder.setScope(Scope.DELETE,  Scope.GET, Scope.PATCH);
     	oConBuilder.setScope(Scope.DELETE);
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getUser(VALID_USER_UUID, accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_retrieve_group_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getGroup(VALID_GROUP_UUID, accessToken);
     }
 
     @Test (expected = ForbiddenException.class)
     public void try_to_retrieve_all_users_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getAllUsers(accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_retrieve_all_groups_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getAllGroups(accessToken);
     }
     
     @Test(expected = ForbiddenException.class)
     public void try_to_create_user_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	User user = new User.Builder("test").build();
     	oConnector.createUser(user, accessToken);
     }
     
     @Test(expected = ForbiddenException.class)
     public void try_to_create_group_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	Group group = new Group.Builder().setDisplayName("test").build();
     	oConnector.createGroup(group, accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_get_actual_user_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getMe(accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_update_user_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	UpdateUser updateUser = new UpdateUser.Builder().setActive(false).build();
     	oConnector.updateUser(VALID_USER_UUID, updateUser, accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_update_group_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	UpdateGroup updateGroup = new UpdateGroup.Builder().updateDisplayName("irrelevant").build();
     	oConnector.updateGroup(VALID_GROUP_UUID, updateGroup, accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_search_for_user_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	Query query = new Query.Builder(User.class).setStartIndex(0).build();
     	oConnector.searchUsers(query, accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_search_for_group_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	Query query = new Query.Builder(Group.class).setStartIndex(0).build();
     	oConnector.searchUsers(query, accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_delete_user_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.deleteUser(VALID_USER_UUID, accessToken);
     }
     
     @Test (expected = ForbiddenException.class)
     public void try_to_delete_group_raises_exception(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.deleteGroup(VALID_GROUP_UUID, accessToken);
     }
     
     @Test 
     public void try_to_retrieve_user(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	
     	oConBuilder.setScope(Scope.DELETE,  Scope.GET, Scope.PATCH);
     	oConBuilder.setScope(Scope.DELETE);
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getUser(VALID_USER_UUID, accessToken);
     }
     
     @Test 
     public void try_to_retrieve_group(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getGroup(VALID_GROUP_UUID, accessToken);
     }
 
     @Test 
     public void try_to_retrieve_all_users(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getAllUsers(accessToken);
     }
     
     @Test 
     public void try_to_retrieve_all_groups(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getAllGroups(accessToken);
     }
     
     @Test
     public void try_to_create_user(){
     	oConnector = oConBuilder.setScope(Scope.POST).build();
     	accessToken = oConnector.retrieveAccessToken();
     	User user = new User.Builder("test").build();
     	oConnector.createUser(user, accessToken);
     }
     
     @Test
     public void try_to_create_group(){
     	oConnector = oConBuilder.setScope(Scope.POST).build();
     	accessToken = oConnector.retrieveAccessToken();
     	Group group = new Group.Builder().setDisplayName("test").build();
     	oConnector.createGroup(group, accessToken);
     }
     
     @Test 
     public void try_to_get_actual_user(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getMe(accessToken);
     }
     
     @Test
     public void try_to_update_user(){
     	oConnector = oConBuilder.setScope(Scope.PATCH).build();
     	accessToken = oConnector.retrieveAccessToken();
     	UpdateUser updateUser = new UpdateUser.Builder().updateUserName("newName").setActive(false).build();
     	oConnector.updateUser(VALID_USER_UUID, updateUser, accessToken);
     }
     
     @Test
     public void try_to_update_group(){
     	oConnector = oConBuilder.setScope(Scope.PATCH).build();
     	accessToken = oConnector.retrieveAccessToken();
     	UpdateGroup updateGroup = new UpdateGroup.Builder().updateDisplayName("irrelevant").build();
     	oConnector.updateGroup(VALID_GROUP_UUID, updateGroup, accessToken);
     }
     
     @Test
     public void try_to_search_for_user(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	Query query = new Query.Builder(User.class).setStartIndex(0).build();
     	oConnector.searchUsers(query, accessToken);
     }
     
     @Test 
     public void try_to_search_for_group(){
     	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	Query query = new Query.Builder(Group.class).setStartIndex(0).build();
     	oConnector.searchUsers(query, accessToken);
     }
     
     @Test 
     public void try_to_delete_user(){
     	oConnector = oConBuilder.setScope(Scope.DELETE, Scope.POST).build();
     	accessToken = oConnector.retrieveAccessToken();
     	UUID newUserId = createTestUser();
     	oConnector.deleteUser(newUserId, accessToken);
     }
     
     @Test 
     public void try_to_delete_group(){
     	oConnector = oConBuilder.setScope(Scope.DELETE).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.deleteGroup(VALID_GROUP_UUID, accessToken);
     }
     
     @Test 
     public void try_to_get_actual_user_with_string_scope(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
     	accessToken = oConnector.retrieveAccessToken();
     	oConnector.getMe(accessToken);
     }
     
     private UUID createTestUser(){
     	User user = new User.Builder("testUSer0065").build();
     	return UUID.fromString(oConnector.createUser(user, accessToken).getId());
     }
 }
