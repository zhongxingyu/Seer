 package business.user.test;
 
 import static org.junit.Assert.*;
 
 import models.User;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import business.user.UserHandler;
 
 public class UserHandlerTest {
 
 	private UserHandler userHandler;
 	private User user1, user2, user3;
 	
 	@Before
 	public void setUp() throws Exception {
 		userHandler = new UserHandler();
		user1= new User("identifier1", "firstName1", "lastName1", "email1", "location1");
		user2= new User("identifier2", "firstName2", "lastName2", "email2", "location2");
		user3= new User("identifier3", "firstName3", "lastName3", "email3", "location3");
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
		assertEquals(user2, userHandler.getUserByIdentifier(user2.getIdentifier()));
 	}
 	
 	@Test
 	public void testGetUserByIdentifierWhenIdentifierIsntFound(){
 		userHandler.addUser(user1);
 		userHandler.addUser(user2);
 		userHandler.addUser(user3);
 		assertEquals(null, userHandler.getUserByIdentifier("thisStringIsNotAnIdentifier"));
 	}
 
 }
