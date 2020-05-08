 package mcgill.game;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class UserTest {
 
	private static String USER = "test-user";
	private static String PASS = "test-pass";
	
 	private Database db;
 	
 	@Before
 	public void setUp() {
 		this.db = new Database(Config.REDIS_HOST, Config.REDIS_PORT);
 	}
 	
 	@Test
 	public void testNewUser() throws Exception {
		User user = User.createUser(USER, PASS, this.db);
     	
    	assertEquals(USER, user.getUsername());
    	assertNotSame(PASS, user.getPasswordHash());
     	
     	this.db.delUser(user.getUsername());
 	}
 	
 	@Test
 	public void testVerifyUser() throws Exception {
		User test = User.createUser(USER, PASS, this.db);
 		
		User user = User.verifyUser(USER, PASS, this.db);
		assertEquals(USER, user.getUsername());
    	assertNotSame(PASS, user.getPasswordHash());
 		
		User wrong_pass = User.verifyUser(USER, "1243", this.db);
     	assertNull(wrong_pass);
     	
    	User wrong_name = User.verifyUser("alex2", PASS, this.db);
     	assertNull(wrong_name);
     	
     	this.db.delUser(test.getUsername());
 	}
 
 }
