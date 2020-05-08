 package controllers;
 
 import model.domain.User;
 import model.dao.UserDAO;
 import model.services.WeddingCreationService;
 
 import play.data.DynamicForm;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.user.*;
 
 public class UserController extends Controller {
 
 	public static Form<User> userForm = Form.form(User.class);
 
 	public static Result user() {
 		return ok(signup.render(userForm));
 	}
 
 	public static Result getLogin() {
 		return ok(login.render(userForm));
 	}
 
 	public static Result login() {
 		DynamicForm params = new DynamicForm().bindFromRequest();
		String email = params.get("email");
 		String password = params.get("password");
		User user = UserDAO.getUserDAO().findOne("email", email);
 
 		if ((user != null) && user.password.equals(password)) {
 			WeddingAuthenticator.signin(session(), user);
 			return redirect( routes.DashboardController.show() );
 		} else {
 			Form<User> filledForm = userForm.bindFromRequest();
 			return badRequest( login.render(filledForm) );
 		}
 	}
 
 	public static Result logout() {
 		WeddingAuthenticator.signout(session());
 		return redirect( routes.UserController.getLogin() );
 	}
 
 	public static Result addUser(){
 
 		Form<User> filledForm = userForm.bindFromRequest();
 
 		if(filledForm.hasErrors()) {
 			return badRequest(signup.render(filledForm));
 		} else {
 			User user = filledForm.get();
 			user.wedding = new WeddingCreationService().createWedding(user.wedding);
 
 			User.create(user);
 			WeddingAuthenticator.signin(session(), user);
 
 			return redirect(routes.DashboardController.show());
 		}
 	}
 
 }
