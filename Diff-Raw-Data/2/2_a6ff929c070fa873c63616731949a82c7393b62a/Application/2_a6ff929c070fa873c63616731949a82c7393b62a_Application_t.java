 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.Date;
 
 import controllers.Projects.ProjectForm;
 import models.Project;
 import models.User;
 import play.*;
 import play.data.Form;
 import play.mvc.*;
 import views.*;
 import views.html.*;
 import views.html.helper.ProjectDisplay;
 
 public class Application extends Controller {
 
 	public static class Login {
 
 		public String username;
 		public String password;
 
 		public String validate() {
 			if (User.authenticate(username, password) == null) {
 
 				return "Invalid user or password!";
 			}
 			return null;
 		}
 
 	}
 
 	public static class Register {
 		public String username;
 		public String email;
 		public String password;
 		public String rePassword;
 
 		public String validate() {
 			if (!username.matches("^[a-zA-Z0-9]{4,20}$")) {
 				return "Invalid username. Must be 4 to 20 length and might use only alphanumeric characters";
 			}
 			if (password.compareTo(rePassword) != 0) {
 				return "Passwords don't match";
 			}
 			if (!password.matches(".{4,20}")) {
 				return "Invalid password. Must be 4 to 25 length";
 			}
 			if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,3})$")) {
 				return "Invalid email";
 			}
 			User user = User.Register(username, password, email);
 			if (user == null) {
 				return "login or email exist";
 			}
 			Project project = new Project("Home", user);
 			project.save();
 			return null;
 		}
 	}
 
 	public static class UpdateUser {
 		public String email;
 		public String password;
 		public String rePassword;
 		public String oldPassword;
 
 		public String validate() {
 			System.out.println("|" + password + "|");
 			User user = Secured.getUser();
 			if (User.passwordHash(oldPassword).compareTo(user.password) != 0)
 				return "Wrong password";
 			if (password.compareTo(rePassword) != 0) {
 				return "Passwords don't match";
 			}
 			if (!password.isEmpty() && !password.matches(".{4,20}")) {
 				return "Invalid password. Must be 4 to 25 length";
 			}
 			if (!email.matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,3})$")) {
 				return "Invalid email";
 			}
 			if (password.compareTo(rePassword) != 0) {
 				return "Passwords are not same";
 			}
 
 			if (User.passwordHash(password).compareTo(oldPassword) == 0) {
 				return "Can't set the same password";
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * Login page.
 	 */
 	public static Result login() {
 		// return ok("fdsf");
 		return ok(login.render(form(Login.class)));
 	}
 
 	/**
 	 * Handle login form submission.
 	 */
 	public static Result authenticate() {
 		Form<Login> loginForm = form(Login.class).bindFromRequest();
 		if (loginForm.hasErrors()) {
 			return badRequest(login.render(loginForm));
 		} else {
 			session("username", loginForm.get().username);
			return redirect(routes.Application.dashboard()+"#message/success/Welcome "+loginForm.get().username+".");
 		}
 
 	}
 
 	/**
 	 * Handle register form submission.
 	 * 
 	 * @return
 	 */
 	public static Result registerPost() {
 		Form<Register> registerForm = form(Register.class).bindFromRequest();
 		if (registerForm.hasErrors()) {
 			return badRequest(register.render(registerForm));
 		} else {
 			return redirect(routes.Application.login());
 		}
 	}
 
 	/**
 	 * Login page.
 	 */
 	public static Result register() {
 		// return ok("fdsf");
 		return ok(register.render(form(Register.class)));
 	}
 
 	public static Result index() {
 		return ok(index.render());
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result dashboard() {
 		return dashboardDisplay(null, "all");
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result dashboardWeek() {
 		return dashboardDisplay(null, "week");
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result updateUser() {
 		Form<UpdateUser> userUpdateForm = form(UpdateUser.class).bindFromRequest();
 		if (userUpdateForm.hasErrors()) {
 			return forbidden(userUpdateForm.globalError().message());
 		}
 		User user = Secured.getUser();
 		user.updated = new Date();
 		user.mail = userUpdateForm.get().email;
 		if (!userUpdateForm.get().password.isEmpty()) {
 			user.password = User.passwordHash(userUpdateForm.get().password);
 		}
 		user.save();
 		return ok();
 	}
 
 	/**
 	 * Logout and clean the session.
 	 */
 	@Security.Authenticated(Secured.class)
 	public static Result logout() {
 		session().clear();
 		return redirect("/");
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result dashboardProject(Integer project_id) {
 		return dashboardDisplay(project_id, null);
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result dashboardDisplay(Integer project_id, String global) {
 		User logged_user = User.findByUsername(request().username());
 
 		return ok(dashboard.render(logged_user, Projects.getUserProjects(), project_id, global));
 	}
 }
