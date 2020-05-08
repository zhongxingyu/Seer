 package ro.gagarin;
 
 import org.junit.Test;
 
 /**
 * Unit test for session.
  */
 public class AuthServiceTest {
 
 	@Test
 	public void testSessionCreateLoginLogout() throws SessionNotFoundException_Exception,
			ItemNotFoundException_Exception {
 		AuthenticationService service = new AuthenticationService();
 		Authentication api = service.getAuthenticationPort();
 		String session = api.createSession(null, null);
 		api.login(session, "admin", "password", null);
 		api.logout(session);
 	}

 }
