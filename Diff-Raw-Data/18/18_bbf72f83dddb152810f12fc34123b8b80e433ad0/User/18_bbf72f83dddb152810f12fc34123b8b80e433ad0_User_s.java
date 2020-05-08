 package test.unit;
 
 import static org.junit.Assert.*;
 import org.junit.*;
 
 /**
  * Unit test for {@link app.User}.
  */
 public class User {
 	app.User u;
 
 	@Before
 	public void setup() {

 	}
 
 	@After
 	public void teardown() {
		u = null;
 	}
 
	@Test (expected=IllegalArgumentException.class)
 	public void testUserCreation() {
		u = new app.User(null);
 	}
 
 	@Test
 	public void testEmails() {
		app.Email[] emails = new app.Email[] { new app.Email("one@two"),
												new app.Email("one@two"),
												new app.Email("one@two"),
												new app.Email("one@two"),
												new app.Email("one@two")};

		u = new app.User(emails);

		assertEquals(u.getEmail(), emails[0]);
 	}
 }
