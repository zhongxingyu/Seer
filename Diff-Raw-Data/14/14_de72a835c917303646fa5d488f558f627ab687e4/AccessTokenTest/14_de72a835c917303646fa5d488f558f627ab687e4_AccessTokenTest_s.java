 package oauth2.functional;
 
 import oauth2.OAuth2Constants;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.Logger;
 import play.cache.Cache;
 import play.mvc.Http.Response;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 
 public class AccessTokenTest extends FunctionalTest {
 	
 	public Response response;
 	public String accessToken;
 	
 	public void requestAccessToken() {
 		response = POST("/oauth2/?grant_type=password&client_id=bob@gmail.com&client_secret=password");
 		accessToken = response.out.toString();
 	}
 	
 	@Before
 	public void loadFixtures() {
 		Fixtures.load("data.yml");
 	}
 	
 	@After
 	public void tearDown() {
 		Fixtures.deleteAll();
 		Cache.clear();
 	}
 	
 	@Test
 	public void testResponseOK() {
 		requestAccessToken();
 		assertIsOk(response);
 	}
 	
 	@Test
 	public void testResponseKeyLength() {
 		requestAccessToken();
 		assertEquals(32, accessToken.length());
 	}
 	
 	@Test
 	public void testKeyIsRandom() {
 		requestAccessToken();
 		String key1 = accessToken;
 		requestAccessToken();
 		String key2 = accessToken;
 		assertNotSame(key1, key2);
 	}
 	
 	@Test
 	public void testUnknownGrantTypes() {	
 		response = POST("/oauth2/?grant_type=unknown&client_id=bob@gmail.com&client_secret=secret");
 		assertStatus(400, response);
 	}
 	
 	@Test
 	public void testValidationErrors() {
 		response = POST("/oauth2/?grant_type=password&client_id=bob@gmail.com");
 		assertStatus(400, response);
 	}
 	
 	@Test
     public void testBadRequest() {
         response = GET("/users/");
        assertStatus(400, response);
     }
 	
 	@Test
 	public void testAccessWithToken() {
 		requestAccessToken();
 		response = GET("/?"+OAuth2Constants.PARAM_OAUTH_TOKEN+"="+accessToken);
 		assertStatus(200, response);
 	}
 	
 	@Test
 	public void testDestroyToken() {
 		requestAccessToken();
 		response = DELETE("/oauth2/?"+OAuth2Constants.PARAM_OAUTH_TOKEN+"="+accessToken);
 		assertStatus(200, response);
 		
 		// Second time will show a bad request
 		response = DELETE("/oauth2/?"+OAuth2Constants.PARAM_OAUTH_TOKEN+"="+accessToken);
		assertStatus(400, response);
 	}
 	
 	@After
 	public void log() {
 		if (response != null) {
 			Logger.debug("Response Status: " + response.status.toString());
 			Logger.debug("Response: " + response.out.toString());
 		}
 	}
 
 }
