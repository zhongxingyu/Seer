 package tests.domain.user;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.sql.SQLException;
 
 import domain.user.User;
 import domain.user.UserIdentityMap;
 import domain.user.mappers.UserInputMapper;
 import domain.user.mappers.UserOutputMapper;
 import domain.user.UserType;
 import exceptions.MapperException;
 import foundation.tdg.UserTDG;
 import junit.framework.TestCase;
 
 public class UserOutputMapperTest extends TestCase {
 
 	private final String email = "example@example.com";
 	private final String password = "password";
 	private final UserType userType = UserType.USER_NORMAL;
 	private final int version = 1;
 	private final BigInteger uid = new BigInteger("3325635465657");
 	private User user = null;
 	
 	public UserOutputMapperTest() {
 		user = new User(uid, email, password, userType, version);
 	}
 	public void testInsert() throws IOException, SQLException, MapperException {
 		// Use the UserOutputMapper to deconstruct the object and place it into the database
 		UserOutputMapper.insert(user);
 		
 		// Confirm that the object has in fact made it into the database by retrieving it
 		User userCopy = UserInputMapper.find(uid);
 		
 		// Ensure that the retrieved copy matches the one the OutputMapper stored
 		assertEquals(user.equals(userCopy), true);
 		
 		// Delete the newly created record from the database and the identity map
 		assertEquals(UserTDG.delete(uid, version),1);
		UserIdentityMap.remove(uid);
 	}
 	
 	public void testUpdate() throws IOException, SQLException, MapperException {
 		// Use the UserOutputMapper to deconstruct the object and place it into the database
 		UserOutputMapper.insert(user);
 		
 		// Change a property of the object and update the version
 		String newEmail = "test@example.com";
 		user.setEmail(newEmail);
 		
 		// update the user in the database
 		UserOutputMapper.update(user); 
 		
 		// retrieve the object from the database
 		User userCopy = UserInputMapper.find(uid);
 
 		// Make sure the retrieved object was updated
 		assertEquals(userCopy.getUid(), uid);
 		assertEquals(userCopy.getEmail(),newEmail);		// Email changes
		// assertEquals(userCopy.getPassword(), password); // Note the password is not returned by the UserInputMapper
 		assertEquals(userCopy.getType(), userType);
 		assertEquals(userCopy.getVersion(), 2);			// Version changes
 		
 		// Delete the newly created record from the database
 		assertEquals(UserTDG.delete(uid, 2),1);	
 	}
 
 	public void testDelete() throws IOException, SQLException {
 		// Use the UserOutputMapper to deconstruct the object and place it into the database
 		UserOutputMapper.insert(user);
 		
 		// Delete the user from the database and make sure the count of records changed equals 1.
 		assertEquals(UserOutputMapper.delete(user),1);
 	}
 }
