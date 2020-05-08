 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.user.*;
 
 public class UserTest extends UnitTest {
 	User bob;
 
 	@Before
 	public void setup() {
 		Fixtures.deleteDatabase();
 
 		bob = new User("bob@gmail.com", "secret").save();
 	}
 
     @Test
     public void createAndRetrieveUser() {
     	User bob = User.find("byEmail", "bob@gmail.com").first();
 
     	assertNotNull(bob);
     	assertEquals("bob@gmail.com", bob.email);
     }
 
     @Test
     public void createAndRetrieveUserDetails() {
     	UserDetails details = new UserDetails(
     		"123 West Sesame Street",
     		"Melrose",
     		"Minnesota",
     		"55426").save();
 
     	bob.accountInformation = details;
     	assertNotNull(bob.accountInformation);
     	assertEquals(bob.accountInformation.address, "123 West Sesame Street");
     	assertEquals(bob.accountInformation.city, "Melrose");
     	assertEquals(bob.accountInformation.state, "Minnesota");
     	assertEquals(bob.accountInformation.zipCode, "55426");
     }
 
    @After
    public void takeDown() {
        bob = null;
    }
 }
