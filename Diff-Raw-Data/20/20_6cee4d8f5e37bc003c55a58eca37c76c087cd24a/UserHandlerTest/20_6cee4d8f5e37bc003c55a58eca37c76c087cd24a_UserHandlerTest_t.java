 package business.user.test;
 
 import static org.junit.Assert.*;
 
 import models.User;
 
 import org.junit.After;
 import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 
 import business.user.UserHandler;
 
 public class UserHandlerTest {
 
 	private UserHandler userHandler;
 	private User user1, user2, user3;
 	
	
 	@Before
 	public void setUp() throws Exception {
 		userHandler = new UserHandler();
		user1= new User("firstName1", "lastName1", "email1", "city1", "country1", "identifier1");
		user2= new User("firstName2", "lastName2", "email2", "city2", "country2", "identifier2");
		user3= new User("firstName3", "lastName3", "email3", "city3", "country3", "identifier3");
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		userHandler = null;
 		user1 = user2 = user3 = null;
 	}
 
 
 	@Test
 	public void testGetUserByIdentifier() {
 		userHandler.addUser(user1);
 		userHandler.addUser(user2);
 		userHandler.addUser(user3);
		assertEquals(user2, userHandler.getUserByIdentifier(user2.getIdentifierOpenID()));
 	}
 	
 	@Test
 	public void testGetUserByIdentifierWhenIdentifierIsntFound(){
 		userHandler.addUser(user1);
 		userHandler.addUser(user2);
 		userHandler.addUser(user3);
 		assertEquals(null, userHandler.getUserByIdentifier("thisStringIsNotAnIdentifier"));
 	}
 
 }
