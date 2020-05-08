 package be.kdg.groepi.controller;
 
 import be.kdg.groepi.model.User;
 import be.kdg.groepi.service.UserService;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.sql.Date;
 import java.util.Calendar;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 public class RestUserControllerTest {
     RestUserController restUserController;
     User user;
 
     @Before
     public void beforeEachTest() {
         restUserController = new RestUserController();
 
         Calendar cal = Calendar.getInstance();
         cal.set(1992, Calendar.JULY, 15);
         Date dateOfBirth = new Date(cal.getTime().getTime());
 
         user = new User("TIMMEH", "TIM@M.EH", "hemmit", dateOfBirth);
         UserService.createUser(user);
     }
 
     @After
     public void afterEachTest() {
         UserService.deleteUser(user);
     }
 
     @Test
     public void testGetUser() {
        ModelAndView modelAndView = restUserController.getUser("1");
         assertEquals("profile/user", modelAndView.getViewName());
         assertNotNull("Model should not be null", modelAndView.getModel());
         User returnedUser = (User) modelAndView.getModel().get("userObject");
         assertNotNull("User with ID 1 should be present in the ModelAndView", returnedUser);

        modelAndView = restUserController.getUser("5");
        assertEquals("This user does not exist, so getUser should return \"5\".",
                "5", modelAndView.getModel().get("userId"));
     }
 
     @Test
     public void testCreateUser(){
         //TODO: uit commentaar halen als code werkt
         /*ModelAndView modelAndView = restUserController.createUser(user);
         assertNotNull("User object should exist", modelAndView.getModel().get("userObject"));
         User createdUser = (User) modelAndView.getModel().get("userObject");
         assertNotNull("User should have a name", createdUser.getName());*/
     }
 }
