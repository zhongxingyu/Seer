 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.CandidateUser;
 import models.CompanyUser;
 import models.UserApp;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.mainmenu;
 
 public class Application extends Controller {
 
 	public static Result index() {
 		String titleMsg = Messages.get("home.title");
 		return ok(views.html.index.render(titleMsg, mainmenu.render()));
 	}
 
 	public static Result newCompUser() {
 		Form<CompanyUser> userForm = form(CompanyUser.class);
 		return ok(views.html.newCompanyUser.render(userForm, mainmenu.render()));
 	}
 
 	public static Result saveCompUser() {
 		Form<CompanyUser> userAddForm = form(CompanyUser.class)
 				.bindFromRequest();
 		if (userAddForm.hasErrors()) {
 			flash("error", "Please correct errors above.");
 			return badRequest(views.html.newCompanyUser.render(userAddForm,
 					mainmenu.render()));
 		} else if (UserApp.findByEmail(userAddForm.get().getEmail()) != null) {
 			flash("error", "This e-mail already exists!");
 			return badRequest(views.html.newCompanyUser.render(userAddForm,
 					mainmenu.render()));
 
 		} else {
 			UserApp user = UserApp.makeInstance(userAddForm.get());
			user.save();
 			flash("success", "Company account instance created/edited: " + user);
 			System.out.println("userAddForm.get().name: "
 					+ userAddForm.get().name);
 			flash("success", "Company " + userAddForm.get().name
 					+ " has been created");
 
 			return redirect(routes.Application.index());
 		}
 	}
 
 	public static Result newUser() {
 		Form<CandidateUser> userForm = form(CandidateUser.class);
 		return ok(views.html.newUser.render(userForm, mainmenu.render()));
 	}
 
 	public static Result saveUser() {
 		Form<CandidateUser> userAddForm = form(CandidateUser.class)
 				.bindFromRequest();
 		if (userAddForm.hasErrors()) {
 			flash("error", "Please correct errors above.");
 			return badRequest(views.html.newUser.render(userAddForm,
 					mainmenu.render()));
 		} else if (UserApp.findByEmail(userAddForm.get().getEmail()) != null) {
 			flash("error", "This e-mail already exists!");
 			return badRequest(views.html.newUser.render(userAddForm,
 					mainmenu.render()));
 
 		} else {
 			UserApp user = UserApp.makeInstance(userAddForm.get());
			user.save();
 			flash("success", "Candidate instance created/edited: " + user);
 			System.out.println("userAddForm.get().name: "
 					+ userAddForm.get().name);
 			flash("success", "Candidate " + userAddForm.get().name
 					+ " has been created");
 
 			return redirect(routes.Application.index());
 		}
 	}
 
 	/**
 	 * Authentication methods
 	 * 
 	 */
 	public static class Login {
 		public String email;
 		public String password;
 
 		public List<ValidationError> validate() {
 			List<ValidationError> errors = new ArrayList<ValidationError>();
 			if (UserApp.authenticate(email, password) == null) {
 				errors.add(new ValidationError("email",
 						"Invalid user or password."));
 			} else {
 				if (UserApp.findByEmail(email) != null) {
 					errors.add(new ValidationError("email",
 							"This e-mail is already registered."));
 				}
 			}
 			return errors.isEmpty() ? null : errors;
 		}
 	}
 
 	/**
 	 * Login page.
 	 */
 	public static Result login() {
 		return ok(views.html.login.render(form(Login.class), mainmenu.render()));
 	}
 
 	/**
 	 * Handle login form submission.
 	 */
 	public static Result authenticate() {
 		Form<Login> loginForm = form(Login.class).bindFromRequest();
 		if (loginForm.hasErrors()) {
 			return badRequest(views.html.login.render(loginForm,
 					mainmenu.render()));
 		} else {
 			session("email", loginForm.get().email);
 			UserApp user = UserApp.authenticate(loginForm.get().email,
 					loginForm.get().password);
 			if (user != null) {
 				if (user.isCandidate()) {
 					return redirect(routes.Company.index(loginForm.get().email));
 				} else if (user.isCompany()) {
 					return redirect(routes.Company.index(loginForm.get().email));
 				} else {
 					return redirect(routes.Application.index());
 				}
 			} else {
 				return badRequest(views.html.login.render(loginForm,
 						mainmenu.render()));
 			}
 		}
 	}
 
 	/**
 	 * Logout and clean the session.
 	 */
 	public static Result logout() {
 		session().clear();
 		flash("success", "You've been logged out");
 		return redirect(routes.Application.login());
 	}
 }
