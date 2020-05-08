 package controllers;
 
 import java.util.List;
 
 import models.User;
 
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Http.Session;
 import play.mvc.Result;
 import views.html.docHead;
 import views.html.footer;
 import views.html.header;
 import views.html.index;
 import views.html.maps;
 import views.html.mapsHome;
 import views.html.registration;
 
 import com.feth.play.module.pa.PlayAuthenticate;
 import com.feth.play.module.pa.user.AuthUser;
 
 public class Application extends Controller {
   
     public static Result index() {
     	
     	AuthUser user = PlayAuthenticate.getUser(session());
     	User appUser = User.findByAuthUserIdentity(user);
     	
         return ok(index.render("Your new application is ready.",appUser));
     }
     
     public static Result oAuthDenied(final String providerKey) {
         flash("flash",
                 "You need to accept the OAuth connection in order to use this website!");
         return redirect(routes.Application.index());
     }
     
 	public static User getLocalUser(final Session session) {
 		final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
 		final User localUser = User.findByAuthUserIdentity(currentAuthUser);
 		return localUser;
 	}
 	
 	public static Result registeredUsers() {
 		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
 
 		List<User> all = User.find.all();
 		for (User u : all) {
 			ObjectNode newObject = Json.newObject();
 			newObject.put("recycledItems", u.recycledItems);
 			newObject.put("name", u.name);
 			newObject.put("facebookId",
					all.get(0).linkedAccounts.get(0).providerUserId);
 			arrayNode.add(newObject);
 			newObject.put("lat", u.lat);
 			newObject.put("lon", u.lon);
 		}
 		return ok(arrayNode);
 	}
 	
 
 	public static Result registration() {
 	    return ok(registration.render());
 	}
 
     public static Result header() {
     	return ok(header.render());
     }
 
     public static Result footer() {
         return ok(footer.render());
     }
 
     public static Result docHead() {
         return ok(docHead.render());
     }
     
     public static Result maps() {
         return ok(maps.render());
     }
     
     public static Result mapsHome() {
         return ok(mapsHome.render());
     }
 }
