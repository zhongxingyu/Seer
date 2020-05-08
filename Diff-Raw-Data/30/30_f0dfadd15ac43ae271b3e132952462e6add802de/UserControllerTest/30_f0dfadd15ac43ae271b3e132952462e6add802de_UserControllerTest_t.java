 package com.crowdgame.controller;
 
 import static org.junit.Assert.assertEquals;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 
 import com.crowdgame.model.GameUser;
 import com.crowdgame.model.Registration;
 import com.crowdgame.service.GameUserService;
 import com.crowdgame.util.RegistrationValidator;
 
 @RunWith(MockitoJUnitRunner.class)
 public class UserControllerTest {
 
 	@InjectMocks
 	private UserController controller = new UserController();
 	
 	@Mock
 	private RegistrationValidator validator;
 	
 	@Mock
 	private AuthenticationManager manager;
 	
 	@Mock
 	private GameUserService service;
 	
 	@Mock
 	private PasswordEncoder encoder;
 	
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 		
 		Mockito.when(encoder.encodePassword(Mockito.anyString(), Mockito.isNull())).thenReturn("password");
 	}
 	
 	@Test
 	public void testLoadLoginHandleRequestView() {
		String result = controller.loadLogin(null, null);
 		
 		assertEquals("login", result);
 	}
 	
 	@Test
 	public void testShowRegistrationHandleRequestView() {
 		Model model = Mockito.mock(Model.class);
 		
 		String result = controller.showRegistration(model);
 		
 		assertEquals("register", result);
 	}
 	
 	@Test
 	public void testShowRegistrationAddsEmptyRegistrationToModel() {
 		Model model = Mockito.mock(Model.class);
 		
 		controller.showRegistration(model);
 		
 		Mockito.verify(model).addAttribute(Mockito.any(Registration.class));
 	}
 	
 	@Test
 	public void testProcessRegistrationHandleRequestViewBindingErrors() {
 		Registration registration = new Registration();
 		BindingResult bindingResult = Mockito.mock(BindingResult.class);
 		Mockito.when(bindingResult.hasErrors()).thenReturn(true);
 		
 		String result = controller.processRegistration(registration, bindingResult, null);
 		
 		assertEquals("register", result);
 	}
 	
 	@Test
 	public void testProcessRegistrationHandleRequestViewCreationErrors() {
 		Registration registration = new Registration();
 		BindingResult bindingResult = Mockito.mock(BindingResult.class);
 		
 		String result = controller.processRegistration(registration, bindingResult, null);
 		
 		assertEquals("register", result);
 	}
 	
 	@Test
 	public void testProcessRegistrationHandleRequestViewSuccess() {
 		Registration registration = new Registration();
 		BindingResult bindingResult = Mockito.mock(BindingResult.class);
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		Mockito.when(service.getUser(Mockito.anyString())).thenReturn(new GameUser());
 		
 		String result = controller.processRegistration(registration, bindingResult, request);
 		
 		assertEquals("redirect:/game", result);
 	}
 	
 	@Test
 	public void testProcessRegistrationValidatesInput() {
 		Registration registration = new Registration();
 		BindingResult bindingResult = Mockito.mock(BindingResult.class);
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		
 		controller.processRegistration(registration, bindingResult, request);
 		
 		Mockito.verify(validator).validate(registration, bindingResult);
 	}
 	
 	@Test
 	public void testProcessRegistrationCallsUserService() {
 		Registration registration = new Registration();
 		BindingResult bindingResult = Mockito.mock(BindingResult.class);
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		
 		controller.processRegistration(registration, bindingResult, request);
 		
 		Mockito.verify(service).addGameUser(Mockito.any(GameUser.class));
 	}
 	
 	@Test
 	public void testProcessRegistrationHandlesLogin() {
 		Registration registration = new Registration();
 		BindingResult bindingResult = Mockito.mock(BindingResult.class);
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		Mockito.when(service.getUser(Mockito.anyString())).thenReturn(new GameUser());
 		
 		controller.processRegistration(registration, bindingResult, request);
 		
 		Mockito.verify(manager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
 	}
 	
 	@Test
 	public void testShowUserDataHandleRequestView() {
 		Model model = Mockito.mock(Model.class);
 		
 		String result = controller.showUserData(model);
 		
 		assertEquals("user", result);
 	}
 	
 	@Test
 	public void testShowUserDataAddsCurrentUserToModel() {
 		Model model = Mockito.mock(Model.class);
 		GameUser user = new GameUser();
 		Mockito.when(service.getCurrentUser()).thenReturn(user);
 		
 		controller.showUserData(model);
 		
 		Mockito.verify(model).addAttribute("user", user);
 	}
 
 }
