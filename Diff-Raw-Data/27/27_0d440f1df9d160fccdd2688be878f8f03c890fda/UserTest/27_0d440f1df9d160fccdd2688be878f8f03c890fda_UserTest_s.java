 package mcgill.game;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class UserTest {
 
 	private Database db;
 	
 	@Before
 	public void setUp() {
 		this.db = new Database(Config.REDIS_HOST, Config.REDIS_PORT);
 	}
 	
 	@Test
 	public void testNewUser() throws Exception {
		User user = User.createUser("alex", "1234", this.db);
     	
    	assertEquals("alex", user.getUsername());
    	assertNotSame("1234", user.getPasswordHash());
     	
     	this.db.delUser(user.getUsername());
 	}
 	
 	@Test
 	public void testVerifyUser() throws Exception {
		User test = User.createUser("alex", "1234", this.db);
 		
		User user = User.verifyUser("alex", "1234", this.db);
		assertEquals("alex", user.getUsername());
    	assertNotSame("1234", user.getPasswordHash());
 		
		User wrong_pass = User.verifyUser("alex", "1243", this.db);
     	assertNull(wrong_pass);
     	
    	User wrong_name = User.verifyUser("alex2", "1234", this.db);
     	assertNull(wrong_name);
     	
     	this.db.delUser(test.getUsername());
 	}
 
 }
