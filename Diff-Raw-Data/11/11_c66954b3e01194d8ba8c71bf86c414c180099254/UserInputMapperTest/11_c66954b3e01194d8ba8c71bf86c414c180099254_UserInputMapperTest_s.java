 /**
  * SOEN 490
  * Capstone 2011
  * Test for UserMapperTest.
  * Team members: 	
  * 			Sotirios Delimanolis
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
 import domain.user.mappers.UserInputMapper;
 import domain.user.mappers.UserOutputMapper;
 import domain.user.UserType;
 import junit.framework.TestCase;
 
 public class UserInputMapperTest extends TestCase {
 
 	private final String email = "unique@example.com";
 	private final String password = "password";
 	private final UserType userType = UserType.USER_NORMAL;
 	private final int version = 1;
 	private final BigInteger uid = new BigInteger("3785635465657");
 	
 
 	public void testFindUserCacheHit() throws IOException, SQLException, NoSuchAlgorithmException {
 		// Create a new user with the factory to make sure
 		// a) it is placed in the UserIdentityMap and
 		// b) persisted to the database
 		User user = UserFactory.createNew(email, password, userType);
 		
 		// Get a copy of the newly created object from the datastore
 		User userCopy = UserInputMapper.find(user.getUid());
 		
 		// Make sure the copy is equivalent to the original
 		assertEquals(user.equals(userCopy), true);
		
		// Remove the test record from the database, which also confirms it was persisted.
		assertEquals(UserOutputMapper.delete(user), 1);
 	}
 	
 	public void testFindUserCacheMiss() throws IOException {
 		// Create a new user without the factory to make sure
 		// a) it is NOT placed in the UserIdentityMap and
 		// b) Manually persist the User to the database using the UserOutputMapper
 		//    so it can be recovered after a cache miss in the UserIndentityMap
 		User user = new User(uid, email, password, userType, version);
 		
 		// User the mapper to persist the object
 		UserOutputMapper.insert(user);
 		
 		// Make sure a cache miss occurs
 		assertEquals(UserIdentityMap.getUniqueInstance().get(uid), null);
 		
 		// Retrieve the user object from the database
 		User userCopy = UserInputMapper.find(uid);
 				
 		// Make sure the copy is equivalent to the original
 		assertEquals(user.equals(userCopy), true);
 		
 		// Remove the test record from the database
 		assertEquals(UserOutputMapper.delete(user), 1);
 	}
 	
 	public void testFindByEmail() throws IOException, SQLException, NoSuchAlgorithmException {
 		// Create a new user with the factory to make sure
 		// a) it is placed in the UserIdentityMap and
 		// b) persisted to the database
 		User user = UserFactory.createNew(email, password, userType);
 		
 		// Get a copy of the newly created object from the datastore
 		User userCopy = UserInputMapper.findByEmail(user.getEmail());
 		
 		// Make sure the copy is equivalent to the original
 		assertEquals(user.equals(userCopy), true);
 		
 		// Remove the test record from the database, which also confirms it was persisted.
 		assertEquals(UserOutputMapper.delete(user), 1);
 	}
 	
 }
