 package de.osiam.client;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.OsiamUserService;
 import org.osiam.client.exception.NoResultException;
 import org.osiam.client.exception.UnauthorizedException;
 import org.osiam.client.query.QueryBuilder;
 import org.osiam.client.query.QueryResult;
 import org.osiam.resources.scim.*;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import java.util.List;
 import java.util.UUID;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseSetup("/database_seed.xml")
 public class UserServiceIT extends AbstractIntegrationTestBase {
 
 
     private UUID validUUID = null;
 
     private OsiamUserService service;
     private User deserializedUser;
     private QueryResult<User> queryResult;
 
     @Before
     public void setUp() throws Exception {
 
         service = new OsiamUserService.Builder(endpointAddress).build();
 
     }
 
     @Test
     public void get_a_valid_user() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
         assertEquals(validUUID.toString(), deserializedUser.getId());
     }
 
 
 
     @Test
     public void name_is_deserialized_correctly() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
 
         Name name = deserializedUser.getName();
 
         assertEquals("Jensen", name.getFamilyName());
         assertEquals("Ms. Barbara J Jensen III", name.getFormatted());
         assertEquals("Barbara", name.getGivenName());
         assertEquals(null, name.getHonorificPrefix());
         assertEquals(null, name.getHonorificSuffix());
         assertEquals(null, name.getMiddleName());
     }
 
     @Test
     public void emails_are_deserialized_correctly() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
 
         List<MultiValuedAttribute> emails = deserializedUser.getEmails();
         assertEquals(1, emails.size());
         MultiValuedAttribute email = emails.get(0);
 
         assertEquals("bjensen@example.com", email.getValue().toString());
         assertEquals("work", email.getType());
     }
 
     @Test
     public void photos_are_deserialized_correctly() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
 
         List<MultiValuedAttribute> photos = deserializedUser.getPhotos();
         assertEquals(1, photos.size());
         MultiValuedAttribute photo = photos.get(0);
 
         assertEquals("http://example.com/barbara.jpg", photo.getValue().toString());
         assertEquals("photo", photo.getType());
     }
 
     @Test
     public void ims_are_deserialized_correctly() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
 
         List<MultiValuedAttribute> ims = deserializedUser.getIms();
         assertEquals(1, ims.size());
         MultiValuedAttribute im = ims.get(0);
 
         assertEquals("barbara", im.getValue().toString());
         assertEquals("xmpp", im.getType());
     }
 
     @Test
     public void external_id_is_deserialized_correctly() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
 
     }
 
     @Test
     public void ensure_basic_values_are_deserialized_correctly() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
 
         assertEquals("bjensen", deserializedUser.getExternalId());
         assertEquals("BarbaraJ.", deserializedUser.getDisplayName());
         assertEquals("de", deserializedUser.getLocale());
         assertEquals("Barbara", deserializedUser.getNickName());
         assertEquals("UTC", deserializedUser.getTimezone());
         assertEquals("Dr.", deserializedUser.getTitle());
         assertEquals("bjensen", deserializedUser.getUserName());
         assertEquals("user", deserializedUser.getUserType());
     }
 
     @Test
     public void password_is_not_transferred() throws Exception {
         givenAValidUserUUID();
         whenUserIsDeserialized();
         assertEquals(null, deserializedUser.getPassword());
     }
 
     @Test(expected = NoResultException.class)
     public void get_an_invalid_user_raises_exception() throws Exception {
         service.getUserByUUID(UUID.fromString(INVALID_UUID), accessToken);
     }
 
     @Test(expected = UnauthorizedException.class)
     public void provide_an_invalid_access_token_raises_exception() throws Exception {
         givenAValidUserUUID();
         given_an_invalid_access_token();
 
         whenUserIsDeserialized();
         fail();
     }
 
     @Test
     public void search_for_user_by_username(){
         String searchString = "userName eq bjensen";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_externalId(){
         String searchString = "externalId eq bjensen";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_name_formated(){
         String searchString = "name.formatted eq Ms. Barbara J Jensen III";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_name_familyName(){
         String searchString = "name.familyName eq Jensen";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_name_givenName(){
         String searchString = "name.givenName eq Barbara";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_displayName(){
         String searchString = "displayName eq BarbaraJ.";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_nickName(){
         String searchString = "nickName eq Barbara";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_locale(){
         String searchString = "locale eq de";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsValidUser();
     }
 
     @Test
     public void search_for_user_by_title(){
         String searchString = "title eq Dr.";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsValidUser();
     }
 
     @Test
     public void search_for_user_by_emails_value(){
         String searchString = "emails.value eq bjensen@example.com";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_compared_search_string(){
         String searchString = "title eq Dr. and nickName eq Barbara and displayName eq BarbaraJ.";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_compared_search_string_and_querybuilder(){
         QueryBuilder queryBuilder = new QueryBuilder(User.class);
         queryBuilder.query("title").equalTo("Dr.")
                 .and("nickName").equalTo("Barbara")
                 .and("displayName").equalTo("BarbaraJ.");
         whenSingleUserIsSearchedByQueryBuilder(queryBuilder);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_non_used_username(){
         String searchString = "userName eq noUserWithThisName";
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsNoValidUser();
     }
 
    @Test
    public void search_for_3_user_by_username_using_and(){
        String user01 = "cmiller";
        String user02 = "hsimpson";
        String user03 = "kmorris";
        String searchString = "userName eq " + user01 + " and userName eq " + user02 + "and userName eq " + user03;
        whenSingleUserIsSearchedByQueryString(searchString);
        queryResultContainsNoValidUser();
    }

    @Test
    public void search_for_3_user_by_username_using_or(){
        String user01 = "cmiller";
        String user02 = "hsimpson";
        String user03 = "kmorris";
        String searchString = "userName eq " + user01 + " or userName eq " + user02 + " or userName eq " + user03;
        whenSingleUserIsSearchedByQueryString(searchString);
        queryResultContainsUser(user01);
        queryResultContainsUser(user02);
        queryResultContainsUser(user03);
    }

    private void queryResultContainsUser(String userName){
        assertTrue(queryResult != null);
        for(User actUser : queryResult.getResources()){
            if(actUser.getUserName().equals(userName)){
                return; // OK
            }
        }
        fail("User " + userName + " could not be found.");
    }
     private void queryResultContainsOnlyValidUser(){
         assertTrue(queryResult != null);
         assertEquals(1, queryResult.getTotalResults());
         queryResultContainsValidUser();
     }
 
     private void queryResultContainsNoValidUser(){
         assertTrue(queryResult != null);
         assertEquals(0, queryResult.getTotalResults());
     }
 
     private void queryResultContainsValidUser(){
         assertTrue(queryResult != null);
         for(User actUser : queryResult.getResources()){
            if(actUser.getId().equals(VALID_USER_UUID)){
                return; // OK
            }
         }
         fail("Valid user could not be found.");
     }
 
     private void whenSingleUserIsSearchedByQueryString(String queryString) {
         queryResult = service.searchUsers(queryString, accessToken);
     }
 
     private void whenSingleUserIsSearchedByQueryBuilder(QueryBuilder queryBuilder) {
         queryResult = service.searchUsers(queryBuilder, accessToken);
     }
 
     private void whenUserIsDeserialized() {
         deserializedUser = service.getUserByUUID(validUUID, accessToken);
     }
 
     private void givenAValidUserUUID() throws Exception {
         validUUID = UUID.fromString(VALID_USER_UUID);
     }
 }
