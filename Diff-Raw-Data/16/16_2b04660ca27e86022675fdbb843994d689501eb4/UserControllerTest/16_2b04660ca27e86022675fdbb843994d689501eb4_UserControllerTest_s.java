 package com.forum.web.controller;
 
 import com.forum.domain.User;
 import com.forum.service.UserService;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.BindingResult;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class UserControllerTest {
 
     private UserService userService;
     private UserController userController;
 
     @Before
     public void setupUserController(){
         userService = mock(UserService.class);
         userController = new UserController(userService);
     }
 
     @Test
     public void shouldNavigateToJoinPage() {
         Map map = new HashMap();
         String activityModelAndView = userController.showRegistrationForm(map);
 
         //Then
         assertThat((User)map.get(UserController.USER), is(new User()));
         assertTrue(map.containsKey(UserController.COUNTRIES));
         assertThat(activityModelAndView, is(UserController.JOIN));
 
     }
 
     @Test
     public void shouldRejectInvalidUser(){
         BindingResult mockBindingResult = mock(BindingResult.class);
         when(mockBindingResult.hasErrors()).thenReturn(true);
         Map map = new HashMap();
 
         String activityModelAndView =userController.processRegistrationForm(new User(), mockBindingResult, map);
 
         assertTrue(map.containsKey(UserController.COUNTRIES));
         assertThat(activityModelAndView, is(UserController.JOIN));
     }
 
 
     @Test
     public void shouldShowProfile(){
         BindingResult mockBindingResult = mock(BindingResult.class);
         when(mockBindingResult.hasErrors()).thenReturn(false);
         Map map = new HashMap();
 
         String activityModelAndView =userController.processRegistrationForm(new User(), mockBindingResult, map);
 
         assertThat((User)map.get(UserController.USER), is(new User()));
         assertTrue(map.containsKey(UserController.COUNTRIES));
         assertThat(activityModelAndView, is(UserController.HOME_PAGE));
     }
 
     @Test
     public void shouldSaveUserProfile(){
         BindingResult mockBindingResult = mock(BindingResult.class);
         when(mockBindingResult.hasErrors()).thenReturn(false);
         Map map = new HashMap();
         when(userService.createUser((User)map.get("user"))).thenReturn(42);
         User user = new User("a  ","asdf ","asdf","asdf","asdf","asdf","asdf",0,false);
         userController.processRegistrationForm(user, mockBindingResult, map);
 
         verify(userService).createUser((User)map.get("user"));
     }
 
 
 }
