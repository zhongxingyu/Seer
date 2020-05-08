 package org.osiam.client;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.resources.scim.SCIMSearchResult;
 import org.osiam.resources.scim.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 public class ScimExtensionSearchIT extends AbstractIntegrationTestBase {
 
     @Test
     @DatabaseSetup("/database_seeds/ScimExtensionSearchIT/user_by_extension.xml")
     public void search_for_user_by_string_extension_field_with_query_string_works() {
         String query = encodeExpected("extension.gender eq female");
 
         SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);
 
         assertThat(result.getTotalResults(), is(equalTo(3L)));
         User transmittedUser = result.getResources().get(0);
         assertThat(transmittedUser.getUserName(), is(equalTo("adavies")));
     }
 
     @Test
     @DatabaseSetup("/database_seeds/ScimExtensionSearchIT/user_by_extension.xml")
     public void search_for_user_by_integer_extension_field_with_query_string_works() {
         String query = encodeExpected("extension.age lt 30");
 
         SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);
 
         assertThat(result.getTotalResults(), is(equalTo(3L)));
         User transmittedUser = result.getResources().get(0);
         Assert.assertThat(transmittedUser.getUserName(), is(equalTo("adavies")));
     }
 
     @Test
     @DatabaseSetup("/database_seeds/ScimExtensionSearchIT/user_by_extension.xml")
     public void search_for_user_by_decimal_extension_field_with_query_string_works() {
         String query = encodeExpected("extension.weight lt 80.5");
 
         SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);
 
         assertThat(result.getTotalResults(), is(equalTo(3L)));
         User transmittedUser = result.getResources().get(0);
         Assert.assertThat(transmittedUser.getUserName(), is(equalTo("adavies")));
     }
 
     @Test
     @DatabaseSetup("/database_seeds/ScimExtensionSearchIT/user_by_extension.xml")
     public void search_for_user_by_boolean_extension_field_with_query_string_works() {
         String query = encodeExpected("extension.newsletter eq true");
 
         SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);
 
         assertThat(result.getTotalResults(), is(equalTo(3L)));
         User transmittedUser = result.getResources().get(0);
         Assert.assertThat(transmittedUser.getUserName(), is(equalTo("adavies")));
     }
 
 
 }
