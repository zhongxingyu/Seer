 package org.test.bugtracker.impl.bo;
 
import static junit.framework.Assert.*;
 
 import org.hibernate.PropertyValueException;
 import org.hibernate.exception.ConstraintViolationException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.test.bugtracker.bo.UserBO;
 import org.test.bugtracker.impl.model.UserImpl;
 import org.test.bugtracker.model.User;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations = { "classpath:spring/context.xml" })
 public class UserImplTest extends AbstractTestNGSpringContextTests {
     private static final String LOGIN = "login";
     private static final String PASS = "pass";    
     @Autowired
     private ApplicationContext context;
     @Autowired
     private UserBO userBO;
 
     @Test(priority = 0)
     public void createNewUser() {
         User user = new UserImpl();
         user.setLogin(LOGIN);
         user.setPass(PASS);
         userBO.save(user);
         assertTrue(user.getId() > 0);
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUser")
     public void findUserByLogin() {
         User user = userBO.findByLogin(LOGIN);
         assertNotNull(user);
         assertEquals(LOGIN, user.getLogin());
         assertEquals(PASS, user.getPass());
 
         user = userBO.findById(user.getId());
         assertNotNull(user);
         assertEquals(LOGIN, user.getLogin());
         assertEquals(PASS, user.getPass());
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUser",
             expectedExceptions = { ConstraintViolationException.class })
     public void createUserWithSomeId() {
         User user = new UserImpl();
         user.setId(1L);
         user.setLogin(LOGIN);
         user.setPass(PASS);
         userBO.save(user);
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUser",
             expectedExceptions = { PropertyValueException.class })
     public void createUserWithoutLogin() {
         User user = new UserImpl();
         user.setPass(PASS);
         userBO.save(user);
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUser",
             expectedExceptions = { PropertyValueException.class })
     public void createUserWithoutPass() {
         User user = new UserImpl();
         user.setLogin(LOGIN);
         userBO.save(user);
     }
     
     @Test(priority = 1, dependsOnMethods = "createNewUser",
             expectedExceptions = { ConstraintViolationException.class })
     public void createUserWithSomeLogin() {
         User user = new UserImpl();
         user.setLogin(LOGIN);
         user.setPass(PASS);
         userBO.save(user);
     }
 
     @Test(priority = 2, dependsOnMethods = "createNewUser")
     public void updateUserLoginAndPass() {
         String newLogin = LOGIN + "2";
         String newPass = PASS + "2";
         User user = userBO.findByLogin(LOGIN);
         long id = user.getId();
         assertNotNull(user);
         user.setLogin(newLogin);
         user.setPass(newPass);
         userBO.update(user);
         User user2 = userBO.findByLogin(newLogin);
         assertNotNull(user2);
         assertEquals(newLogin, user2.getLogin());
         assertEquals(newPass, user2.getPass());
         assertTrue(id == user2.getId());
         User user3 = userBO.findByLogin(newLogin);
         assertNotNull(user3);
         user3.setLogin(LOGIN);
         user3.setPass(PASS);
         userBO.update(user3);
         User user4 = userBO.findByLogin(LOGIN);
         assertTrue(id == user4.getId());
         assertEquals(LOGIN, user4.getLogin());
         assertEquals(PASS, user4.getPass());
         
     }
 
     @Test(priority = 3, dependsOnMethods = "updateUserLoginAndPass")
     public void deleteUser() {
         User user = userBO.findByLogin(LOGIN);
         userBO.delete(user);
         User user2 = userBO.findByLogin(LOGIN);
         assertNull(user2);
     }
 }
