 package controllers;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.UUID;
 
 import org.apache.commons.mail.EmailException;
 
 import models.User;
 import models.utils.AppException;
 import models.utils.Hash;
 import models.utils.Mail;
 import play.Configuration;
 import play.Logger;
 import play.data.Form;
 import play.data.validation.Constraints;
 import play.data.validation.Constraints.Required;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Results;
 import views.html.index;
 import views.html.created;
 import views.html.confirm;
 
 public class Application extends Controller {
 
 	public static Form<Application.Register> registerForm  = form(Application.Register.class);
 	
 	public static Result index() {
 		return Results.ok(index.render(registerForm));
 	}
 
 	public static Result authenticate() {
 		return Results.TODO;
 	}
 
 	public static Result signup() {
 		 Form<Application.Register> registerForm = form(Application.Register.class).bindFromRequest();
 
 		 if (registerForm.hasErrors()) {
 	            return badRequest(index.render(registerForm));
 	        }
 
 	        Application.Register register = registerForm.get();
 	        Result resultError = checkBeforeSave(registerForm, register.email);
 
 	        if (resultError != null) {
 	            return resultError;
 	        }
 
 	        try {
 	            User user = new User();
 	            user.email = register.email;
 	            user.password = Hash.createPassword(register.inputPassword);
 	            user.confirmationToken = UUID.randomUUID().toString();
 
 	            user.save();
 	            sendMailAskForConfirmation(user);
 
 	            return ok(created.render());
 	        } catch (EmailException e) {
 	            Logger.debug("Signup.save Cannot send email", e);
 	            flash("error", Messages.get("error.sending.email"));
 	        } catch (Exception e) {
 	            Logger.error("Signup.save error", e);
 	            flash("error", Messages.get("error.technical"));
 	        }
 	        return badRequest(index.render(registerForm));
 	}
 	
 	private static Result checkBeforeSave(Form<Application.Register> registerForm, String email) {
         // Check unique email
         if (User.findByEmail(email) != null) {
             flash("error", Messages.get("error.email.already.exist"));
             return badRequest(index.render(registerForm));
         }
 
         return null;
      }
 
 	
 	 public static class Register {
 
 		@Required
 		public String email;
 		@Required
 		public String inputPassword;
 		@Required
 		public String accept;
 
 	 }
 
 	  private static void sendMailAskForConfirmation(User user) throws EmailException, MalformedURLException {
 	        String subject = Messages.get("mail.confirm.subject");
 
	        URL url = getSignUpRequest(user);
	        
	        String message =url.toString();
 
 	        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.email);
 	        Mail.sendMail(envelop);
 	    }

	private static URL getSignUpRequest(User user) throws MalformedURLException {
		String urlString = "http://" + Configuration.root().getString("server.hostname");
		urlString += "/confirm/" + user.confirmationToken;
		URL url = new URL(urlString);
		return url;
	}
 	 
 	    public static Result confirm(String token) {
 	        User user = User.findByConfirmationToken(token);
 	        if (user == null) {
 	            flash("error", Messages.get("error.unknown.email"));
 	            return badRequest(confirm.render());
 	        }
 
 	        if (user.validated) {
 	            flash("error", Messages.get("error.account.already.validated"));
 	            return badRequest(confirm.render());
 	        }
 
 	        try {
 	            if (User.confirm(user)) {
 	                sendMailConfirmation(user);
 	                flash("success", Messages.get("account.successfully.validated"));
 	                return ok(confirm.render());
 	            } else {
 	                Logger.debug("Signup.confirm cannot confirm user");
 	                flash("error", Messages.get("error.confirm"));
 	                return badRequest(confirm.render());
 	            }
 	        } catch (AppException e) {
 	            Logger.error("Cannot signup", e);
 	            flash("error", Messages.get("error.technical"));
 	        } catch (EmailException e) {
 	            Logger.debug("Cannot send email", e);
 	            flash("error", Messages.get("error.sending.confirm.email"));
 	        }
 	        return badRequest(confirm.render());
 	    }
 	    
 	    private static void sendMailConfirmation(User user) throws EmailException {
 	        String subject = Messages.get("mail.welcome.subject");
 	        String message = Messages.get("mail.welcome.message");
 	        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.email);
 	        Mail.sendMail(envelop);
 	    }
 	  
 	  
 }
