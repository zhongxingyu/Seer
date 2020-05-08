 package de.osiam.client;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.OsiamUserService;
 import org.osiam.client.query.Query;
 import org.osiam.client.query.QueryResult;
 import org.osiam.client.query.SortOrder;
 import org.osiam.resources.scim.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import static org.junit.Assert.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseSetup("/database_seed.xml")
 public class SearchUserServiceIT extends AbstractIntegrationTestBase {
 
     private OsiamUserService service;
     private QueryResult<User> queryResult;
 
     @Before
     public void setUp() throws Exception {
         service = new OsiamUserService.Builder(endpointAddress).build();
     }
 
     @Test
     public void search_for_user_by_username() {
         String searchString = encodeExpected("userName eq bjensen");
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_emails_value() {
         String searchString = encodeExpected("emails.value eq bjensen@example.com");
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsOnlyValidUser();
     }
 
 
     @Test
     public void search_for_user_with_multiple_fields() throws UnsupportedEncodingException {
         Query query = new Query.Builder(User.class).filter("title").equalTo("Dr.")
                 .and("nickName").equalTo("Barbara")
                 .and("displayName").equalTo("BarbaraJ.").build();
         whenSingleUserIsSearchedByQueryBuilder(query);
         queryResultContainsOnlyValidUser();
     }
 
     @Test
     public void search_for_user_by_non_used_username() {
         String searchString = encodeExpected("userName eq " + INVALID_STRING);
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultDoesNotContainValidUsers();
     }
 
     @Test
     public void search_for_3_users_by_username_using_and() {
         String user01 = "cmiller";
         String user02 = "hsimpson";
         String user03 = "kmorris";
         String searchString = encodeExpected("userName eq " + user01 + " and userName eq " + user02 + "and userName eq " + user03);
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultDoesNotContainValidUsers();
     }
 
     @Test
     public void search_for_3_users_by_username_using_or() {
         String user01 = "cmiller";
         String user02 = "hsimpson";
         String user03 = "kmorris";
         String searchString = encodeExpected("userName eq " + user01 + " or userName eq " + user02 + " or userName eq " + user03);
         whenSingleUserIsSearchedByQueryString(searchString);
         queryResultContainsUser(user01);
         queryResultContainsUser(user02);
         queryResultContainsUser(user03);
     }
 
     @Test
     public void search_with_braces() throws UnsupportedEncodingException {
         Query.Builder innerQuery = new Query.Builder(User.class);
         innerQuery.filter("userName").equalTo("marissa").or("userName").equalTo("hsimpson");
 
         Query.Builder queryBuilder = new Query.Builder(User.class);
         queryBuilder.filter("meta.created").greaterThan("2000-05-23T13:12:45.672").and(innerQuery);
 
         queryResult = service.searchUsers(queryBuilder.build(), accessToken);
        assertEquals(expectedNumberOfMembers(2), queryResult.getTotalResults());
        queryResultContainsUser("marissa");
         queryResultContainsUser("hsimpson");
     }
 
     @Test
     public void sorted_search() throws UnsupportedEncodingException {
         Query.Builder queryBuilder = new Query.Builder(User.class);
         queryBuilder.sortBy("userName").withSortOrder(SortOrder.ASCENDING);
         queryResult = service.searchUsers(queryBuilder.build(), accessToken);
 
         ArrayList<String> sortedUserNames = new ArrayList<>();
         sortedUserNames.add("bjensen");
         sortedUserNames.add("jcambell");
         sortedUserNames.add("adavies");
         sortedUserNames.add("cmiller");
         sortedUserNames.add("dcooper");
         sortedUserNames.add("epalmer");
         sortedUserNames.add("gparker");
         sortedUserNames.add("hsimpson");
         sortedUserNames.add("kmorris");
         sortedUserNames.add("ewilley");
        sortedUserNames.add("marissa");
         Collections.sort(sortedUserNames);
 
         assertEquals(sortedUserNames.size(), queryResult.getTotalResults());
         int count = 0;
         for (User actUser : queryResult.getResources()) {
             assertEquals(sortedUserNames.get(count++), actUser.getUserName());
         }
     }
 
     private void queryResultContainsUser(String userName) {
         assertTrue(queryResult != null);
         for (User actUser : queryResult.getResources()) {
             if (actUser.getUserName().equals(userName)) {
                 return; // OK
             }
         }
         fail("User " + userName + " could not be found.");
     }
 
     private void queryResultContainsOnlyValidUser() {
         assertTrue(queryResult != null);
         assertEquals(1, queryResult.getTotalResults());
         queryResultContainsValidUser();
     }
 
     private void queryResultDoesNotContainValidUsers() {
         assertTrue(queryResult != null);
         assertEquals(0, queryResult.getTotalResults());
     }
 
     private void whenSingleUserIsSearchedByQueryString(String queryString) {
         queryResult = service.searchUsers("filter=" + queryString, accessToken);
     }
 
     private void queryResultContainsValidUser() {
         assertTrue(queryResult != null);
         for (User actUser : queryResult.getResources()) {
             if (actUser.getId().equals(VALID_USER_UUID)) {
                 return; // OK
             }
         }
         fail("Valid user could not be found.");
     }
 
     private void whenSingleUserIsSearchedByQueryBuilder(Query query) {
         queryResult = service.searchUsers(query, accessToken);
     }
 
 }
