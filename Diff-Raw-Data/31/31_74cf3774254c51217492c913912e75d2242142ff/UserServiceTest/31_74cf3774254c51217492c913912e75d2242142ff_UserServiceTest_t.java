 package jogoanatomia.services;
 
 import static org.junit.Assert.*;
 
 import jogoanatomia.entidades.User;
 import org.junit.Test;
 
 public class UserServiceTest {
    private final String VALID_USER = "test";
     private final String VALID_PASSWORD = "1234";
 
     private UserService service = new UserServiceImpl();
 
    @Test
     public void loginTest() {
         User user = service.login(VALID_USER, VALID_PASSWORD);
 
         assertNotNull(user);
         assertNotNull(user.getId());
         assertEquals(VALID_USER, user.getLogin());
         assertEquals(VALID_PASSWORD, user.getPassword());
    }
 
     @Test
     public void loginTest_WhenItsAnInvalidLogin() {
         User user = service.login("not_found", "neither");
 
         assertNull(user);
     }
 }
