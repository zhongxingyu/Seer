 package test_suite_databank;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import generic.WithApplication;
 import models.user.Independent;
 import models.user.User;
 import models.user.UserID;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import play.test.FakeApplication;
 import controllers.user.Type;
 import static play.test.Helpers.*;
 
 public class UserDatabaseTest{
 	
 	
 	
	@BeforeClass
 	public static void startApp(){
 		
 //		start(fakeApplication(inMemoryDatabase()));
 	    Map<String, String> settings = new HashMap<String, String>();
 		settings.put("db.default.driver", "org.h2.Driver");
 	    settings.put("db.default.user", "sa");
 	    settings.put("db.default.password", "");
 	    settings.put("db.default.url", "jdbc:h2:mem:play");
 	    
 		start(fakeApplication(settings));
 	}
 	
 	@Test
 	public void test() {
 //		User user = new Independent(new UserID("ind"), Type.INDEPENDENT, "Bertrand Russell");
 //		user.save();
 	}
 
 }
