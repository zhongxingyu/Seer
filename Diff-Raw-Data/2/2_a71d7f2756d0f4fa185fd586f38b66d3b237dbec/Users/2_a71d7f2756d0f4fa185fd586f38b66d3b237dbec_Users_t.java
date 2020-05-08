 package controllers;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.UUID;
 
 import models.Offer;
 import models.User;
 import play.mvc.Controller;
 import siena.Model;
 import siena.Query;
 
 import com.google.gson.Gson;
 
 public class Users extends Controller {
 
 	protected static User parseJSON(InputStream in) {
 		return new Gson().fromJson(new InputStreamReader(in), User.class);
 	}
 
 	public static void create() {
 		User user = parseJSON(request.body);
 		user.email = user.email.toLowerCase();
 		user.insert();
 		renderJSON(user);
 	}
 
 	static Query<User> all() {
 		return Model.all(User.class);
 	}
 
 	public static void login() {
 		User user = parseJSON(request.body);
 		User authenticatedUser = all().filter("email", user.email.toLowerCase()).filter("password", user.password).get();
 		authenticatedUser.token = UUID.randomUUID().toString();
 		authenticatedUser.update();
 		renderJSON("{\"token\" : \"" + authenticatedUser.token + "\"}");
 	}
 	
 	public static void offers() {
 		String token = request.headers.get("authorization").values.get(0);
 		User owner = all().filter("token", token.replaceAll("\"", "")).get();
		renderJSON(Model.all(Offer.class).filter("owner", owner).fetch());
 	}
 
 }
