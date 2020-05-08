 package models.userTests;
 
import javax.persistence.PersistenceException;
 import models.User;
 import models.enums.Country;
 import org.junit.*;
 import play.test.*;
 import services.UserService;
 
 public class BasicUserTests extends UnitTest {
 
 	@Before
 	public void preparation() {
 		Fixtures.deleteDatabase();
 	}
 
 	@Test
 	public void userRegister() {
 		User user = createTestUser();
 		User userToTest = UserService.register(user);
 		assertNotNull(userToTest);
 		assertEquals(1, User.count());
 
 		user.delete();
 		assertEquals(User.count(), 0);
 	}
 
	@Test(expected = PersistenceException.class)
 	public void multipleUserRegistered() {
 		for (int i = 0; i < 5; i++) {
 			User user = createTestUser();
 			UserService.register(user);
 		}
 		assertEquals(1, User.count());
 	}
 
 	@Test
 	public void connect() {
 		User user = createTestUser();
 		user.save();
 
 		User userUnderTest = UserService.connect("Username", "PassworT");
 		assertNull(userUnderTest);
 
 		userUnderTest = UserService.connect("Username", "Password");
 		assertNotNull(userUnderTest);
 	}
 
 	private User createTestUser() {
 		User user = new User();
 		user.username = "Username";
 		user.password = "Password";
 		user.country = Country.RU;
 		user.email = "email@email.com";
 		return user;
 	}
 
 
 }
