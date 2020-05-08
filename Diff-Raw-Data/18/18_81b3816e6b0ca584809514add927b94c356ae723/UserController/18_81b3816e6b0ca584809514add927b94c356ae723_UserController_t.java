 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import views.html.*;
 
 import models.*;
 
 /**
  * Manage projects related operations.
 * Controller of user
  */
 @Security.Authenticated(Secured.class)
 public class UserController extends Controller {
 
 	static Form<User> userForm = form(User.class);
 

 	public static Result userManager() {
 		return ok(views.html.adminUser.render(User.getAllUsers()
 											, User.findByUsername(request().username())));
 	}
 
 	public static Result deleteUser(Long id) {
 		User.delete(id);
 		return redirect(routes.UserController.userManager());
 	}
 
 	public static Result newUser(){
 		Form<User> filledForm = userForm.bindFromRequest();
 		
 		User.register(filledForm.get().username,filledForm.get().password
 					,filledForm.get().password);
 		return redirect(routes.UserController.userManager());
 	}
 
 	public static Result getUser(Long id){
 		return ok(views.html.adminUserEdit.render(User.getAllUsers()
 				, User.getUser(id) ));
 	}
 
 	public static Result editUser(Long id){
 		
 		Form<User> filledForm = userForm.bindFromRequest();
 
 		User.editUser(id,filledForm.get());
 
 		return ok(views.html.adminUser.render(User.getAllUsers()
 				, User.getUser(id) ));
 		
 	}	
 }
