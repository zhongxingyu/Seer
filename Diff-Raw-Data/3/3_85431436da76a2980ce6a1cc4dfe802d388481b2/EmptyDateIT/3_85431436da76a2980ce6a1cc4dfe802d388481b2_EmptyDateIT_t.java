 package org.osiam.client;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.resources.scim.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseOperation;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class })
 @DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
 @DatabaseSetup(value = "/database_seeds/SearchByExtensionIT/extensions.xml")
 public class EmptyDateIT extends AbstractIntegrationTestBase {
     
     @Test
     public void replace_user_with_empty_date_extension_fail() {
         User user = oConnector.getUser("df7d06b2-b6ee-42b1-8c1b-4bd1176cc8d4", accessToken);
         user.getExtension("extension").addOrUpdateField("birthday", "");
         
         User replacedUser = oConnector.replaceUser(user, accessToken);
         
         assertThat(replacedUser.getExtension("extension").isFieldPresent("birthday"), is(equalTo(false)));
     }
 
 }
