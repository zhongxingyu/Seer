 package controllers;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static play.mvc.Http.Status.BAD_REQUEST;
 import static play.mvc.Http.Status.SEE_OTHER;
 import static play.mvc.Http.Status.OK;
 import static play.test.Helpers.GET;
 import static play.test.Helpers.POST;
 import static play.test.Helpers.callAction;
 import static play.test.Helpers.contentAsString;
 import static play.test.Helpers.fakeRequest;
 import static play.test.Helpers.route;
 import static play.test.Helpers.status;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import models.User;
 
 import org.junit.Test;
 
 import play.mvc.Result;
 
 public class UsersTest extends BaseControllerTest{
 	
 	
 	
 	@Test
 	public void testAuthorize() {
 		Result authAction = callAction(controllers.routes.ref.Users.authorize());
 		assertThat(status(authAction)).isEqualTo(BAD_REQUEST); // form is not filled so u have a bad request
 		assertThat(contentAsString(authAction)).contains("user.login.invalidLogin");
 	}
 	@Test
     public void testChangePassRoute() {
         Result result = route(fakeRequest(GET, "/change_password"));
         assertThat(result).isNotNull();
     }
 	
 	@Test
 	public void testChangePassword() {
		User oldUser = new User();
		oldUser.id = 3L;
		oldUser.password = "Not izmail";
		oldUser.save();
 		Result changeAction = callAction(controllers.routes.ref.Users.changePassword(),
				fakeRequest().withSession("userId", "3"));
 		//assertThat(contentAsString(changeAction)).contains("[Смените пароль]");
 		assertThat(status(changeAction)).isEqualTo(OK); 
 	}
 	
 	@Test
 	public void testUpdateRoute() {
 		Result result = route(fakeRequest(POST, "/update"));
 		assertThat(result).isNotNull();
 	}
 	
 	@Test
 	public void testUpdateWithForm() {
 		
 		User oldUser = new User();
 		oldUser.id = 1L;
 		oldUser.password = "Not izmail";
 		oldUser.save();
 		
 		Map <String, String> data = new HashMap<String, String>();
 		data.put("password", "izmail");
 		data.put("repeat", "izmail");
 		Result result = callAction(
                 controllers.routes.ref.Users.update(),
                 fakeRequest().withFormUrlEncodedBody(data).withSession("userId", "1")
             );
 		assertThat(status(result)).isEqualTo(SEE_OTHER);
 		assertThat(User.find.byId(1L).getPassword()).isEqualTo("izmail");
 	}
 	
 }
