 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.util.List;
 
 import models.TripMatch;
 import models.TripOffer;
 import models.TripRequest;
 import models.User;
 import play.data.Form;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 import flexjson.JSONSerializer;
 
 public class UserController extends Controller {
 
     public static Result getUsers() {
 	List<User> users = User.find.all();
 	return ok(toJson(users));
     }
 
     public static Result getUser(Integer id) {
 	User user = User.find.byId(id);
 	if (user != null) {
 	    return ok(toJson(user));
 	} else {
 	    return notFound();
 	}
     }
 
     @BodyParser.Of(play.mvc.BodyParser.Json.class)
     public static Result newUser() {
 	Form<User> userForm = form(User.class).bindFromRequest();
 	
 	if (userForm.hasErrors()) {
 	    return badRequest();
 	}
 
 	User newUser = userForm.get();
 	newUser.setId(0);
 	newUser.save();
 	response().setContentType("application/json");
 	return status(201, toJson(newUser));
     }
 
     @BodyParser.Of(play.mvc.BodyParser.Json.class)
     public static Result updateUser(Integer id) {
 	User userToEdit = User.find.byId(id);
 
 	if (userToEdit == null) {
 	    return notFound();
 	}
 
 	Form<User> userForm = form(User.class).bindFromRequest();
 
 	if (userForm.hasErrors()) {
 	    return badRequest();
 	}
 
 	User editedUser = userForm.get();
 	editedUser.setId(userToEdit.getId());
 	editedUser.update();
 
 	return status(200, toJson(editedUser));
     }
 
     public static Result deleteUser(Integer id) {
 	User u = User.find.byId(id);
 	if (u != null) {
 	    u.delete();
 	    return ok();
 	} else {
 	    return notFound();
 	}
     }
 
     public static Result getRequestsByUser(Integer id) {
 	List<TripRequest> requests = TripRequest.find.join("user").where().eq("user.id", id).findList();
 
 	JSONSerializer serializer = new JSONSerializer().exclude("*.class").include("*");
 
 	response().setContentType("application/json");
 	return ok(serializer.serialize(requests));
     }
 
     public static Result getOffersByUser(Integer id) {
 
 	List<TripOffer> offers = TripOffer.find.join("user").where().eq("user.id", id).findList();
 
 	JSONSerializer serializer = new JSONSerializer().exclude("class").include("*");
 
 	response().setContentType("application/json");
 	return ok(serializer.serialize(offers));
     }
 
     public static Result getMatchesByUser(Integer id) {
 	List<TripMatch> matches = TripMatch.find.join("tripRequest").where().eq("tripRequest.user.id", id).findList();
 	
 	JSONSerializer serializer = new JSONSerializer().exclude("class").include("*");
 
 	response().setContentType("application/json");
 	return ok(serializer.serialize(matches));
     }
     
    public static Result login()
     {
 	return TODO;
     }
 }
