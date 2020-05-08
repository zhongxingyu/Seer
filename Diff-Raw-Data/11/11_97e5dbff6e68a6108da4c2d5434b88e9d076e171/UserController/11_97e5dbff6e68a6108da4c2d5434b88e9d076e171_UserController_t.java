 package controllers;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Map;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import play.data.format.*;
 
 import java.util.Locale;
 import java.text.ParseException;
 
 import models.*;
 import views.html.*;
 
 public class UserController extends Controller {
 
 	public static class CompetenceProfileForm {
 		public Competence competence;
 		public float yearsOfExperience;
 		/**
 		 * Validate whether the competence exists in database.
 		 * 
 		 * @return - null on success, else error message.
 		 */
 		public String validate() {
 			return null;
 		}
 	}
 
 	/**
 	 * Authentication
 	 */
 	public static class Login {
 		public String username;
 		public String password;
 
 		/**
 		 * Validate user name  and password.
 		 * 
 		 * @return - null on success, else error message.
 		 */
 		public String validate() {
 			if (User.authenticate(username, password) == null) {
 				return "Invalid user or password";
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * Renders the login view
 	 */
 	public static Result login() {
 		return ok(login.render(form(Login.class)));
 	}
 
 	/**
 	 * Authenticates user and creates a session cookie.
 	 */
 	public static Result authenticate() {
 		Form<Login> loginForm = form(Login.class).bindFromRequest();
 		if (loginForm.hasErrors()) {
 			return badRequest(login.render(loginForm));
 		} else {
 			session("name", loginForm.get().username);
 			return redirect(routes.Index.index());
 		}
 	}
 
 	/**
 	 * Logs out and clears cookie
 	 */
 	public static Result logout() {
 		session().clear();
 		flash("success", "You've been logged out");
 		return redirect(routes.UserController.login());
 	}
 
 	public static Result register() {
 		return ok(register.render(form(User.class)));
 	}
 
 	/**
 	 *	Creates and persists a new user.
 	 */
 	public static Result newUser() {
 		Form<User> userForm = form(User.class).bindFromRequest();
 		if(userForm.hasErrors()) {
 			
 			System.out.println("Error :" + userForm.errorsAsJson());
 			
 			return badRequest(register.render(userForm));
 		} else {
 			userForm.get().save();
 			return redirect(routes.Index.index());
 		}
 	}
 	
 	/**
 	 * Is username free?
 	 */
 	public static Result usernameAvailable(String username) {
 		if (User.usernameAvailable(username))
 			return ok("true");
 		
 		return ok("false");
 	}
 	
 	/**
 	 * Setup rotes for javascript calls.
 	 * 
 	 * @return
 	 */
 	public static Result javascriptRoutes() {
 		response().setContentType("text/javascript");
 		return ok(
 				Routes.javascriptRouter("jsRoutes",
 						// Routes
 						controllers.routes.javascript.UserController.usernameAvailable())
 		);
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result getCompetenceView() {
 		String username = Http.Context.current().request().username();
 		User user = User.findByUsername(username);
 		return ok(competenceView.render(user, form(CompetenceProfileForm.class)));
 	}
 
 	@Security.Authenticated(Secured.class)
 	public static Result addCompetence() {
 
 		String username = Http.Context.current().request().username();
 		User user = User.findByUsername(username);
 
 		//Adds a custom formatter to convert from string to Competence
 		Formatters.register(Competence.class, new Formatters.SimpleFormatter<Competence>() {
 			@Override
 			public Competence parse(String input, Locale arg1) throws ParseException {
 				System.out.println("Parsing!");
 				Competence comp = Competence.findById(Integer.parseInt(input));
 				return comp;
 			}
 			
 			@Override
 			public String print(Competence comp, Locale arg1) {
 				System.out.println("Printing!");
 				return Integer.toString(comp.competence_id);
 			}
 		});
 
 		Form<CompetenceProfileForm> compForm = form(CompetenceProfileForm.class).bindFromRequest();
 
 		if (compForm.hasErrors()) {
 			System.out.println("Compform has errors");
 			return badRequest(competenceView.render(user, form(CompetenceProfileForm.class)));
 		} else {
 			System.out.println("Compform was a success");
 			CompetenceProfile p = new CompetenceProfile();
 			p.person = user;
 			p.competence = compForm.get().competence;
 			p.years_of_experience = compForm.get().yearsOfExperience;
 			System.out.println(compForm.get().competence + ", " + compForm.get().yearsOfExperience);
 			user.competenceProfileList.add(p);
			user.update();
 			return redirect(routes.Index.index());
 		}
 	}
 }
