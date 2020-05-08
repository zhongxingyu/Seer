 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import views.html.*;
 
 import models.*;
 
 /**
<<<<<<< HEAD
  * Manage projects related operations.
=======
 * Controller of user 
>>>>>>> f28048d5f5b772f31facb37f886670087588f3ba
  */
 @Security.Authenticated(Secured.class)
 public class UserController extends Controller {
 
<<<<<<< HEAD
 	static Form<User> userForm = form(User.class);
 
=======
>>>>>>> f28048d5f5b772f31facb37f886670087588f3ba
 	public static Result userManager() {
 		return ok(views.html.adminUser.render(User.getAllUsers()
 											, User.findByUsername(request().username())));
 	}
 
 	public static Result deleteUser(Long id) {
 		User.delete(id);
 		return redirect(routes.UserController.userManager());
 	}
<<<<<<< HEAD
 
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

=======
>>>>>>> f28048d5f5b772f31facb37f886670087588f3ba
 }
