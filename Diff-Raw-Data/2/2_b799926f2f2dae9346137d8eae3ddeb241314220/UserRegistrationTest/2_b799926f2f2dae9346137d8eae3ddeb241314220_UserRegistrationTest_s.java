 package models.user;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import org.junit.Test;
 import org.junit.Assert;
 import static org.junit.Assert.assertNotNull;
 import static org.fest.assertions.Assertions.assertThat;
 
 import play.templates.Hash;
 import play.mvc.Result;
 import static play.test.Helpers.fakeRequest;
 import static play.test.Helpers.callAction;
 import static play.test.Helpers.contentAsString;
 import static play.test.Helpers.status;
 
 import com.avaje.ebean.Ebean;
 
 import models.dbentities.UserModel;
 import test.ContextTest;
 
 public class UserRegistrationTest extends ContextTest{
 
 
 	@Test
 	public void createAccountSucces(){
 
 		Map<String, String> map = new HashMap<String,String>();
 		map.put("name", "Jim Jones");
 		map.put("email","jimjones@localhost.com");
 		map.put("bday","13/05/1931");
 		map.put("gender","Male");
 		map.put("prefLanguage","en");
 		map.put("password","kaituma");
 		map.put("controle_passwd","kaituma");
 
 		Result result = callAction(
 				controllers.routes.ref.UserController.register(),fakeRequest().withFormUrlEncodedBody(map)
 				);
 
 		assertThat(status(result)).isEqualTo(200);
 		assertThat(contentAsString(result)).contains("Your Bebras ID is: jjones.");
 		assertThat(contentAsString(result)).contains("You may login with your ID and password.");
 
 		result = callAction(
 				controllers.routes.ref.UserController.register(),fakeRequest().withFormUrlEncodedBody(map)
 				);
 
 		assertThat(contentAsString(result)).contains("There is already a user with the selected email address");
 
 		assertNotNull(Ebean.find(UserModel.class).where().eq("id","jjones").where().eq("type", UserType.PUPIL_OR_INDEP.toString()).findUnique());
 
 
 		// ok.. He is not dutch.. 
 		Map<String, String> map2 = new HashMap<String,String>();
 		map2.put("name", "Wang\\ Xiaoyun");
 		map2.put("email","wangxiaoyn@localhost.com");
 		map2.put("bday","20/02/1966");
 		map2.put("gender","Male");
 		map2.put("prefLanguage","nl");
 		map2.put("password","genealogy");
 		map2.put("controle_passwd","genealogy");
 
 		result = callAction(
 				controllers.routes.ref.UserController.register(),fakeRequest().withFormUrlEncodedBody(map2)
 				);
 
 		assertThat(status(result)).isEqualTo(400);
 		assertThat(contentAsString(result)).contains("Input contains invalid symbols.");
 
 		Map<String, String> map3 = new HashMap<String,String>();
 		map3.put("name", "Robert Wilhelm");
 		map3.put("email","rober\\wilhelm@localhost.com");
 		map3.put("bday","31/03/1811");
 		map3.put("gender","Male");
 		map3.put("prefLanguage","nl");
 		map3.put("password","hydrojet");
 		map3.put("controle_passwd","hydrojet");
 
 		result = callAction(
 				controllers.routes.ref.UserController.register(),fakeRequest().withFormUrlEncodedBody(map3)
 				);
 		assertThat(status(result)).isEqualTo(400);
		assertThat(contentAsString(result)).contains("Invalid email addres.");
 
 
 		Map<String, String> map4 = new HashMap<String,String>();
 		map4.put("name", "Isaac Chauncey");
 		map4.put("email","isaacchauncey@localhost.com");
 		map4.put("bday","1779-02-20");
 		map4.put("gender","Male");
 		map4.put("prefLanguage","en");
 		map4.put("password","Chauncey");
 		map4.put("controle_passwd","Chauncey");
 
 		result = callAction(
 				controllers.routes.ref.UserController.register(),fakeRequest().withFormUrlEncodedBody(map4)
 				);
 		assertThat(status(result)).isEqualTo(400);
 		assertThat(contentAsString(result)).contains("Could not parse date.");
 
 		Map<String, String> map5 = new HashMap<String,String>();
 		map5.put("name", "Rosa-Parks");
 		map5.put("email","Rosa_Parks@localhost.com");
 		map5.put("bday","07/02/1993");
 		map5.put("gender","Female");
 		map5.put("prefLanguage","en");
 		map5.put("password","Montgomery");
 		map5.put("controle_passwd","Montgomery1");
 		result = callAction(
 				controllers.routes.ref.UserController.register(),fakeRequest().withFormUrlEncodedBody(map5)
 				);
 
 		assertThat(status(result)).isEqualTo(400);
 		assertThat(contentAsString(result)).contains("Password did not match.");
 
 
 		// Check to see if userModel is stored in database (should be zero).
 		Assert.assertNull(Ebean.find(UserModel.class).where().eq("name", "Rosa Parks").findUnique());
 
 
 	}
 }
