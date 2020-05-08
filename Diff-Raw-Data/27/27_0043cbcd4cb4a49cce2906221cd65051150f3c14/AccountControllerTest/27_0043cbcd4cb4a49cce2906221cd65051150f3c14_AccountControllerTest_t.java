 package example.springmvc.controller;
 
 import static org.junit.Assert.*;
 
 import javax.servlet.http.Cookie;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.web.ModelAndViewAssert;
 import org.springframework.web.servlet.ModelAndView;
 
 import example.springmvc.data.Session;
 import example.springmvc.data.User;
 import example.springmvc.data.UserRegistrationData;
 import example.springmvc.data.impl.SessionStorageDummyImpl;
 import example.springmvc.data.impl.UserStorageDummyImpl;
 
 public class AccountControllerTest {
 
 	AccountController controller;
 
 	@Before
 	public void setUp() {
 		controller = new AccountController();
 		controller.setUserStorage(new UserStorageDummyImpl());
 		controller.setSessionStorage(new SessionStorageDummyImpl());
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void testDoSignupSuccessful() throws Exception {
 		UserRegistrationData userAccountData = new UserRegistrationData(
 				"admin", "system", "system");
 		final ModelAndView mav = this.controller.doSignup(userAccountData);
 		
 		User user = this.controller.getUserStorage().byId("admin");
 		
 		ModelAndViewAssert.assertViewName(mav, "redirect:/");
 		assertNotNull(user);
 		assertEquals("admin", user.getId());
 		assertEquals("system", user.getPassword());
 	}
 	
 	@Test
 	public void testDoSignup_userAlreadyExists() throws Exception {
 		this.createStandardTestUser();
		UserRegistrationData userRegistrationData = new UserRegistrationData("admin", "system", "system");
		final ModelAndView mav = controller.doSignup(userRegistrationData);
 		
 		ModelAndViewAssert.assertViewName(mav, "signup");
 		ModelAndViewAssert.assertModelAttributeValue(mav, "userId_error", "admin");
		ModelAndViewAssert.assertModelAttributeValue(mav, "userRegistrationData", userRegistrationData);
 	}
 	
 	@Test
 	public void testDoSignup_passwordsNotEqual() throws Exception {
		UserRegistrationData userRegistrationData = new UserRegistrationData("admin", "system", "metsys");
		final ModelAndView mav = controller.doSignup(userRegistrationData);
 		
 		ModelAndViewAssert.assertViewName(mav, "signup");
 		ModelAndViewAssert.assertModelAttributeValue(mav, "password_error", "not_equal");
		ModelAndViewAssert.assertModelAttributeValue(mav, "userRegistrationData", userRegistrationData);
 	}
 
 	@Test
 	public void testGetLogin() throws Exception {
 		final ModelAndView mav = controller.getLoginPage();
 		ModelAndViewAssert.assertViewName(mav, "login");
 	}
 
 	@Test
 	public void testDoLogin_successful() throws Exception {
 		this.createStandardTestUser();
 		
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		
 		final ModelAndView mav = controller.doLogin("admin", "system", response);
 		
 		ModelAndViewAssert.assertViewName(mav, "redirect:/");
 		ModelAndViewAssert.assertModelAttributeValue(mav, "userId", "admin");
 		Cookie cookie = response.getCookie("sessionId");
 		assertNotNull(cookie);
 		assertNotNull(cookie.getValue());
 		Session session = this.controller.getSessionStorage().getById(cookie.getValue());
 		assertNotNull(session);
 		assertEquals("admin", session.getUserId());
 	}
 	
 	@Test
 	public void testDoLogin_failed_unknownUser() throws Exception {
 		this.createStandardTestUser();
 		
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		
 		final ModelAndView mav = controller.doLogin("user", "system", response);
 		
 		ModelAndViewAssert.assertViewName(mav, "login");
 		ModelAndViewAssert.assertModelAttributeValue(mav, "login_error", true);
 		Cookie cookie = response.getCookie("sessionId");
 		assertNull(cookie);
 	}
 	
 	@Test
 	public void testDoLogin_failed_wrongPassword() throws Exception {
 		this.createStandardTestUser();
 		
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		
 		final ModelAndView mav = controller.doLogin("admin", "12345", response);
 		
 		ModelAndViewAssert.assertViewName(mav, "login");
 		ModelAndViewAssert.assertModelAttributeValue(mav, "login_error", true);
 		Cookie cookie = response.getCookie("sessionId");
 		assertNull(cookie);
 	}
 	
 	@Test
 	public void testLogout_successful() {
 		this.createStandardTestUser();
 		Session session = this.controller.getSessionStorage().create("admin");
 		
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		
 		final String viewName = controller.doLogout(session.getId(), response);
 		assertEquals("redirect:/", viewName);
 		assertNull(this.controller.getSessionStorage().getById(session.getId()));
 		Cookie cookie = response.getCookie("sessionId");
 		assertNotNull(cookie);
 		assertEquals(0, cookie.getMaxAge());
 		assertEquals(null, cookie.getValue());
 	}
 	
 	private void createStandardTestUser() {
 		this.controller.getUserStorage().createNewUser(new UserRegistrationData("admin", "system", "system"));
 	}
 }
