 package org.oregami.service;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.oregami.data.UserDao;
 import org.oregami.dropwizard.DropwizardJunitRunner;
 import org.oregami.dropwizard.DropwizardTestConfig;
 import org.oregami.dropwizard.OregamiService;
 import org.oregami.entities.user.User;
 import org.oregami.util.MailHelper;
 
 @RunWith(DropwizardJunitRunner.class)
 @DropwizardTestConfig(serviceClass=OregamiService.class, yamlFile = "/oregami.yml")
 public class TestUserService {
 
     private UserServiceImpl userService;
 
     @Before
     public void setup() {
         userService = new UserServiceImpl();
         UserDao userRepository = Mockito.mock(UserDao.class);
         userService.setUserDao(userRepository);
     }
 
     @After
     public void tearDown() {
         userService = null;
     }
 
     @Test
     public void testRegisterInvalidUser() {
         User user = new User();
 
         ServiceResult<User> result = userService.register(user);
         Assert.assertEquals(false, result.wasSuccessful());
         Assert.assertEquals(true, result.hasErrors());
         Assert.assertTrue(result.containsError(new ServiceError(new ServiceErrorContext("user.email"), ServiceErrorMessage.USER_EMAIL_EMPTY)));
         Assert.assertTrue(result.containsError(new ServiceError(new ServiceErrorContext("user.password"), ServiceErrorMessage.USER_PASSWORD_EMPTY)));
         Assert.assertTrue(result.containsError(new ServiceError(new ServiceErrorContext("user.username"), ServiceErrorMessage.USER_USERNAME_EMPTY)));
        Assert.assertTrue(result.containsError(new ServiceError(new ServiceErrorContext("user.username"), ServiceErrorMessage.USER_USERNAME_TOO_SHORT)));
        Assert.assertEquals(4,  result.getErrors().size());
         
     }
 
     @Test
     public void testRegisterUser() {
         User user = new User();
         user.setEmail("email@example.com");
         user.setUsername("username@example.com");
         user.setPassword("password");
 
         userService.setMailhelper(Mockito.mock(MailHelper.class));
         
         ServiceResult<User> result = userService.register(user);
         Assert.assertEquals(true, result.wasSuccessful());
         Assert.assertEquals(false, result.hasErrors());
 
         //        Assert.assertNotNull(user.getRegistrationTime());
     }
     
     
 
 }
