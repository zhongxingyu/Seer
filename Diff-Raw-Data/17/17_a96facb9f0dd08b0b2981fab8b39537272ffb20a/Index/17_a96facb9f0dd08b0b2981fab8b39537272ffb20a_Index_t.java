 
 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 
 import models.*;
 import views.html.*;
 
 /**
  * Return HTTP Server index.
  */
 @Security.Authenticated(Secured.class)
 public class Index extends Controller {
 	/**
 	 * Returns index page for HTTP.
 	 * 
 	 * @return - Rendered index page as response.
 	 */
 	public static Result index() {
 		String username = Http.Context.current().request().username();
 		User user = User.findByUsername(username);
 		//Todo: Döp om applicantView till typ UserIndexView.
 		//Todo: Skicka olika vyer beroende på användartyp
 		switch(user.role) {
 			case Applicant:
 				return ok(applicantView.render(user, form(UserController.CompetenceProfileForm.class)));
 			case Recruiter:
				return ok(recruiterView.render(user, form(UserController.CompetenceProfileForm.class)));
 			default:
 				return internalServerError("Invalid role");
 		}
 	}
 }
 
