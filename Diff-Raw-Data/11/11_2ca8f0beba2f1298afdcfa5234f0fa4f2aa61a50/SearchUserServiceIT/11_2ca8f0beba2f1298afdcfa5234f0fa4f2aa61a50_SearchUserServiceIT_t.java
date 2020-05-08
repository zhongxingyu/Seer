 package org.osiam.client;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseOperation;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.query.Query;
 import org.osiam.client.query.SortOrder;
 import org.osiam.client.query.metamodel.User_;
 import org.osiam.resources.scim.SCIMSearchResult;
 import org.osiam.resources.scim.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
 public class SearchUserServiceIT extends AbstractIntegrationTestBase {
 
     private static final int ITEMS_PER_PAGE = 3;
     private static final int STARTINDEX_SECOND_PAGE = 4;
     private SCIMSearchResult<User> queryResult;
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_username.xml")
     public void search_for_user_by_username_with_query_string_works() {
         String userName = "bjensen";
         String query = encodeExpected("userName eq " + userName);
 
         SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);
 
         assertThat(result.getTotalResults(), is(equalTo(1L)));
         User transmittedUser = result.getResources().get(0);
         assertThat(transmittedUser.getUserName(), is(equalTo(userName)));
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_username.xml")
     public void search_for_user_by_nonexistent_username_with_query_string_fails() {
         String query = encodeExpected("userName eq " + INVALID_STRING);
         SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);
         assertThat(result.getTotalResults(), is(equalTo(0L)));
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_username.xml")
     public void search_for_all_users_ordered_by_user_name_with_query_builder_works() throws UnsupportedEncodingException {
         Query.Builder queryBuilder = new Query.Builder(User.class);
         queryBuilder.setSortBy(User_.userName).setSortOrder(SortOrder.ASCENDING);
         queryResult = oConnector.searchUsers(queryBuilder.build(), accessToken);
 
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
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_email.xml")
     public void search_for_user_by_emails_value_with_query_string_works() {
         String email = "bjensen@example.com";
         String query = encodeExpected("emails.value eq " + email);
 
         SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);
 
         assertThat(result.getTotalResults(), is(equalTo(1L)));
         User transmittedUser = result.getResources().get(0);
         assertThat(transmittedUser.getEmails().get(0).getValue(), is(equalTo(email)));
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_last_modified.xml")
     public void search_for_all_users_ordered_by_last_modified_with_query_builder_works() throws UnsupportedEncodingException {
         Query.Builder queryBuilder = new Query.Builder(User.class);
         queryBuilder.setSortBy(User_.Meta.lastModified).setSortOrder(SortOrder.ASCENDING);
         SCIMSearchResult<User> result = oConnector.searchUsers(queryBuilder.build(), accessToken);
 
         ArrayList<String> sortedUserNames = new ArrayList<>();
         sortedUserNames.add("marissa");
         sortedUserNames.add("adavies");
         sortedUserNames.add("bjensen");
         sortedUserNames.add("cmiller");
         sortedUserNames.add("dcooper");
         sortedUserNames.add("epalmer");
 
         assertThat(result.getTotalResults(), is(equalTo((long) sortedUserNames.size())));
 
         int count = 0;
         for (User currentUser : result.getResources()) {
             Assert.assertThat(currentUser.getUserName(), is(equalTo(sortedUserNames.get(count++))));
         }
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
     public void search_for_user_with_multiple_fields() throws UnsupportedEncodingException {
         Query.Filter filter = new Query.Filter(User.class, User_.title.equalTo("Dr."))
                 .and(User_.nickName.equalTo("Barbara")).and(User_.displayName.equalTo("BarbaraJ."));
         Query query = new Query.Builder(User.class).setFilter(filter).build();
         whenSearchedIsDoneByQuery(query);
         assertThatQueryResultContainsOnlyValidUser();
     }
 
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
     public void search_for_3_users_by_username_using_and() {
         String user01 = "cmiller";
         String user02 = "hsimpson";
         String user03 = "kmorris";
         String searchString = encodeExpected("userName eq " + user01 + " and userName eq " + user02 + "and userName eq " + user03);
         whenSearchIsDoneByString(searchString);
         assertThatQueryResultDoesNotContainValidUsers();
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
     public void search_for_3_users_by_username_using_or() {
         String user01 = "cmiller";
         String user02 = "hsimpson";
         String user03 = "kmorris";
         String searchString = encodeExpected("userName eq " + user01 + " or userName eq " + user02 + " or userName eq " + user03);
         whenSearchIsDoneByString(searchString);
         assertThatQueryResultContainsUser(user01);
         assertThatQueryResultContainsUser(user02);
         assertThatQueryResultContainsUser(user03);
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
     public void search_with_braces() throws Exception {
         Query.Filter innerFilter = new Query.Filter(User.class, User_.userName.equalTo("marissa"))
                 .or(User_.userName.equalTo("hsimpson"));
 
         DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeParser();
         //DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
 
         DateTime date = dateTimeFormatter.parseDateTime("2000-05-23T13:12:45.672Z");
         Query.Filter mainFilter = new Query.Filter(User.class, User_.Meta.created.greaterThan(date))
                 .and(innerFilter);
         Query.Builder queryBuilder = new Query.Builder(User.class);
         queryBuilder.setFilter(mainFilter);
 
         queryResult = oConnector.searchUsers(queryBuilder.build(), accessToken);
         assertEquals(2, queryResult.getTotalResults());
         assertThatQueryResultContainsUser("marissa");
         assertThatQueryResultContainsUser("hsimpson");
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
     public void nextPage_scrolls_forward() throws UnsupportedEncodingException {
         Query.Builder builder = new Query.Builder(User.class).setCountPerPage(ITEMS_PER_PAGE);
         Query query = builder.build().nextPage();
         whenSearchedIsDoneByQuery(query);
         assertEquals(STARTINDEX_SECOND_PAGE, queryResult.getStartIndex());
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
     public void prevPage_scrolls_backward() throws UnsupportedEncodingException {
         // since OSIAMs default startIndex is wrongly '0' using ITEMS_PER_PAGE works here.
         Query.Builder builder = new Query.Builder(User.class).setCountPerPage(ITEMS_PER_PAGE).setStartIndex(STARTINDEX_SECOND_PAGE);
         Query query = builder.build().previousPage();
         whenSearchedIsDoneByQuery(query);
         assertEquals(1, queryResult.getStartIndex());
     }
 
     @Test
     @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
     public void get_all_user_if_over_hundert_user_exists() {
         create100NewUser();
         List<User> allUsers = oConnector.getAllUsers(accessToken);
         assertEquals(111, allUsers.size());
     }
 
     private void create100NewUser() {
         for (int count = 0; count < 100; count++) {
             User user = new User.Builder("user" + count).build();
             oConnector.createUser(user, accessToken);
         }
     }
 
     private void assertThatQueryResultContainsUser(String userName) {
         assertTrue(queryResult != null);
         for (User actUser : queryResult.getResources()) {
             if (actUser.getUserName().equals(userName)) {
                 return; // OK
             }
         }
         fail("User " + userName + " could not be found.");
     }
 
     private void assertThatQueryResultContainsOnlyValidUser() {
         assertTrue(queryResult != null);
         assertEquals(1, queryResult.getTotalResults());
         assertThatQueryResultContainsValidUser();
     }
 
     private void assertThatQueryResultDoesNotContainValidUsers() {
         assertTrue(queryResult != null);
         assertEquals(0, queryResult.getTotalResults());
     }
 
     private void whenSearchIsDoneByString(String queryString) {
         queryResult = oConnector.searchUsers("filter=" + queryString, accessToken);
     }
 
     private void whenSearchedIsDoneByQuery(Query query) {
         queryResult = oConnector.searchUsers(query, accessToken);
     }
 
     private void assertThatQueryResultContainsValidUser() {
         assertTrue(queryResult != null);
         for (User actUser : queryResult.getResources()) {
             if (actUser.getId().equals(VALID_USER_ID)) {
                 return; // OK
             }
         }
         fail("Valid user could not be found.");
     }
 
 }
