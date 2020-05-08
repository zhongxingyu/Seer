 package functional;
 
 import models.User;
 import models.helpers.UserConnectionHelper;
 import oauth2.functional.AccessTokenTest;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.Logger;
 import play.cache.Cache;
 import play.mvc.Http;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 public class UsersTest extends FunctionalTest {
 	
 	private Http.Request request;
 	private Http.Response response;
 
 	private static final String BASE_CONTROLLER_PATH = "/users";
 	private String baseQuery = "?oauth_token=";
 	
 	User user1;
 	User user2;
 	
 	@Before
 	public void setUp() {
 		request = new Http.Request();
 		request.headers.put("accept", new Http.Header("Accept", "application/json"));
 		
 		Fixtures.load("data.yml");
 		
 		user1 = User.find("byEmail", "bob@gmail.com").first();
 		baseQuery += user1.accessToken + "&";
 		
 		user2 = User.find("byEmail", "bob2@gmail.com").first();
 		UserConnectionHelper.createUserConnection(user1, user2);
 	}
 	
 	@After
 	public void tearDown() {
 		Fixtures.deleteAll();
 		Cache.clear();
 	}
 	
 	@After
 	public void log() {
 		if (response != null) {
 			Logger.debug("Response: \n\n" + response.out.toString());
 		}
 	}
 
 	@Test
 	public void testIndexPage() {
 		response = GET(BASE_CONTROLLER_PATH + baseQuery);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 	}
 
 	@Test
 	public void testCreate() {
 		baseQuery = "";
 		
 		response = POST(BASE_CONTROLLER_PATH
 					+ "?user.email=axj7@aber.ac.uk"
 					+ "&user.password=password"
 					+ "&user.firstName=alex"
 					+ "&user.lastName=hello"
 					+ "&user.serviceName=alex"
 					+ "&user.telephone=123");
 		
 		assertStatus(201, response);
 	}
 	
 	@Test
 	public void testCreateErrorExistingEmail() {
 		testCreate();
 		
 		response = POST(BASE_CONTROLLER_PATH
 				+ "?user.email=axj7@aber.ac.uk"
 				+ "&user.password=password"
 				+ "&user.firstName=alex"
 				+ "&user.lastName=hello"
 				+ "&user.serviceName=alex"
 				+ "&user.telephone=123");
 		
 		assertStatus(400, response);
 		assertContentEquals("Email already exists", response);
 	}
 	
 	@Test
 	public void testCreateErrorExistingServiceName() {
 		testCreate();
 		
 		response = POST(BASE_CONTROLLER_PATH
 				+ "?user.email=axj77@aber.ac.uk"
 				+ "&user.password=password"
 				+ "&user.firstName=alex"
 				+ "&user.lastName=hello"
 				+ "&user.serviceName=alex"
 				+ "&user.telephone=123");
 		
 		assertStatus(400, response);
 		assertContentEquals("ServiceName already exists", response);
 	}
 	
 	@Test
 	public void testShowAuthUser() {
 		response = GET(BASE_CONTROLLER_PATH + "/" + user1.id + baseQuery);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 		
 		// Contains the connections to other users
 		assertTrue(response.out.toString().contains("connections"));
 	}
 	
 	@Test
 	public void testShowUser() {
 		response = GET(BASE_CONTROLLER_PATH + "/" + user2.id + baseQuery);
 		assertIsOk(response);
 		assertContentType("application/json", response);
 		
 		// Does not contain the connections to other users
 		assertFalse(response.out.toString().contains("connections"));
 	}
 	
 	@Test
 	public void testShowNonUser() {
 		response = GET(request, BASE_CONTROLLER_PATH + "/" + "9999999999" + baseQuery);
 		assertIsNotFound(response);
 		assertContentType("application/json", response);
 	}
 	
 	@Test
 	public void testUpdateAuthUser() {
 		String newPassword = "newpassword";
 		String data = "user.email="
 						+ user1.email
 						+ "&user.password=" + newPassword
 						+ "&user.firstName=alex";
 		
 		response = PUT(request, BASE_CONTROLLER_PATH + "/" + user1.id + baseQuery + data, "application/x-www-form-urlencoded", "");
 
 		assertStatus(200, response);
 		assertTrue("FirstName has not been updated", response.out.toString().contains("\"firstName\":\"alex\""));
 		
 		// Verify that the old password can no longer be used to authenticate the user
 		AccessTokenTest accessTokenTest = new AccessTokenTest();
 		accessTokenTest.requestAccessToken();
 		assertStatus(400, accessTokenTest.response);
 	}
 	
 	@Test
 	public void testUpdateNonAuthUser() {
 		String data = "user.email=" + user1.email
 						+ "&user.password=password"
 						+ "&user.firstName=alex"
 						+ "&user.lastName=hello"
 						+ "&user.serviceName=alex"
 						+ "&user.telephone=123";
 		
 		response = PUT(request, BASE_CONTROLLER_PATH + "/" + user2.id + baseQuery + data, "application/x-www-form-urlencoded", "");
 		
 		assertStatus(400, response);
 	}
 	
 	//TODO: updates for fields where there are clashes
 	
 	@Test
 	public void testUpdateClashes() {
 		String data = "user.email=" + user2.email;
 		response = PUT(request, BASE_CONTROLLER_PATH + "/" + user1.id + baseQuery + data, "application/x-www-form-urlencoded", "");
 		
 		assertStatus(400, response);
 		assertContentEquals("Email already exists", response);
 	}
 }
