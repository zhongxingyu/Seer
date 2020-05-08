 /**
  * SOEN 490
  * Capstone 2011
  * Test for UserProxy.
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package tests.domain.user;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 
 import domain.user.User;
 import domain.user.UserFactory;
import domain.user.UserIdentityMap;
 import domain.user.UserProxy;
 import domain.user.UserType;
 import junit.framework.TestCase;
 
 
 public class UserProxyTest extends TestCase {
 	
 	private final String email = "example@example.com";
 	private final String password = "password";
 	private final UserType userType = UserType.USER_NORMAL;
 	private final int version = 1;
 	
 	private User realUser = null;			// Create a real user object
 	private UserProxy userProxy = null;		// Create a proxy for the real object
 	
 	
 	public void testSetters() throws IOException, SQLException, NoSuchAlgorithmException {
 		realUser = UserFactory.createNew(email, password, userType);
		UserIdentityMap.getUniqueInstance().put(realUser.getUid(), realUser);
		
		// Get the UID from the user object since it's generated in it's constructor
		BigInteger uid = realUser.getUid();
 		
 		userProxy = new UserProxy(uid);
 		userProxy.setEmail(email);
 		userProxy.setPassword(password);
 		userProxy.setType(userType);
 		userProxy.setVersion(version);
 		
 		assertEquals(userProxy.getEmail(), email);
 		assertEquals(userProxy.getUid(), uid);
 		assertEquals(userProxy.getPassword(), password);
 		assertEquals(userProxy.getVersion(), version);
 		assertEquals(userProxy.getType(), userType);
 	}
 	
 	public void testGetters() throws IOException, SQLException, NoSuchAlgorithmException {
 		// First we create an object via the factory so it get's sent to the IdentityMap as well
 		realUser = UserFactory.createNew(email, password, userType);
		UserIdentityMap.getUniqueInstance().put(realUser.getUid(), realUser);
		
 		// Create a proxy for the real object
 		userProxy = new UserProxy(realUser.getUid());
 		
 		assertEquals(userProxy.getUid(), realUser.getUid());
 		
 		assertEquals(userProxy.getEmail(), realUser.getEmail());
 		assertEquals(userProxy.getPassword(), realUser.getPassword());
 		assertEquals(userProxy.getVersion(), realUser.getVersion());
 		assertEquals(userProxy.getType(), realUser.getType());
 	}
 	
 	public void testEquals() {
		userProxy = new UserProxy(new BigInteger("564646747777443"));
 		UserProxy userProxy1 = new UserProxy(new BigInteger("5555635465657")); // Different UserID specified here
 		assertEquals("Should return false when compared null", userProxy.equals(null),false);
 		assertEquals("Should return false when compared to a different object", userProxy.equals(userProxy1),false);
 		assertEquals("Should return true when compared to the same object", userProxy.equals(userProxy),true);
 	}
 	
 	
 	
 }
