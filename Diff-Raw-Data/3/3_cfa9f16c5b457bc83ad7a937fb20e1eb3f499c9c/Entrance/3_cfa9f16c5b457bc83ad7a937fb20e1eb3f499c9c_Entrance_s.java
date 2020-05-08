 package controllers;
 
 import org.mongojack.JacksonDBCollection;
 import org.mongojack.WriteResult;
 import play.mvc.*;
 import play.data.*;
 import views.html.*;
 
 import utils.DataUtil;
 import utils.EncryptionUtil;
 import utils.StringUtil;
 
 import models.*;
 
 import static play.data.Form.form;
 
 public class Entrance extends MasterController {
 
     /**
      * Defines a form wrapping the User class.
      */
     final static Form<User> loginForm = form(User.class);
     final static Form<User> registerForm = form(User.class);
     final static Form<User> securityForm = form(User.class);
 
     public static Result index() {
         if(!DataUtil.isDatabase()) {
             flash("error", "Our database is currently down. Please contact a system administrator.");
             return ok( index.render(loginForm, registerForm));
         }
        if(getLoggedInUser() != null) {
            return redirect("/landing");
        }
 
         return ok( index.render(loginForm, registerForm));
     }
 
     public static Result playing() {
         return ok( playing.render());
     }
 
     public static Result gog() {
         return ok( gog.render());
     }
 
     public static Result resources() {
         return ok( resources.render());
     }
 
     public static Result login() {
         if(!DataUtil.isDatabase()) {
             flash("error", "Our database is currently down. Please contact a system administrator.");
             return ok( index.render(loginForm, registerForm));
         }
         Form<User> filledForm = loginForm.bindFromRequest();
         String userName = filledForm.data().get("username");
 
         if(userName == null || userName.isEmpty()) {
             flash("error", "Please enter a username");
             return badRequest( index.render(filledForm, registerForm));
         }
 
         if (User.isUsernameTaken(userName)) {
             if (User.getDecryptedPasswordForUser(userName).equals(filledForm.data().get("password"))) {
                 User user = User.findUserByName(userName);
                 Http.Context.current().session().put("user", user.getId());
                 return redirect("/landing");
             }
         }
         flash("error", "Your username or password is incorrect");
         return badRequest( index.render(filledForm, registerForm));
     }
 
     public static Result logout() {
         Http.Context.current().session().remove("user");
         Http.Context.current().session().remove("passport");
         flash("info", "You are now logged out");
         return ok( index.render(loginForm, registerForm) );
     }
 
     public static Result register() {
         if(!DataUtil.isDatabase()) {
             flash("error", "Our database is currently down. Please contact a system administrator.");
             return ok( index.render(loginForm, registerForm));
         }
         Form<User> filledForm = registerForm.bindFromRequest();
         User newUser;
 
         if (User.isUsernameTaken(filledForm.data().get("username"))) {
             flash("error", "That username is already taken");
             return badRequest( index.render(loginForm, filledForm));
         }
         if (filledForm.data().get("username") == null || filledForm.data().get("username").isEmpty()) {
             flash("error", "Please enter a username");
             return badRequest( index.render(loginForm, filledForm));
         }
         if (!filledForm.data().get("repeatPassword").equals(filledForm.data().get("password")) || StringUtil.isEmpty(filledForm.data().get("repeatPassword")) || StringUtil.isEmpty(filledForm.data().get("password"))) {
             flash("error", "Your passwords do not match");
             return badRequest( index.render(loginForm, filledForm));
         }
         try {
             JacksonDBCollection<User, String> collection = DataUtil.getCollection("users", User.class);
             EncryptionUtil encryptionUtil = new EncryptionUtil();
             newUser = new User(filledForm.data().get("username"), encryptionUtil.encrypt(filledForm.data().get("password")));
 
             WriteResult<User, String> result = collection.insert(newUser);
 
             Http.Context.current().session().put("user", result.getSavedId());
 
         } catch (Exception e) {
             flash("error", "Unexpected error: " + e.toString());
             return internalServerError( index.render(loginForm, filledForm));
         }
 
         return ok( security.render(securityForm, newUser.getId()));
     }
 
 
     /**
      * Handle security questions.
      */
     public static Result security() {
         Form<User> filledForm = securityForm.bindFromRequest();
         User user = getLoggedInUser();
 
         try {
 
             JacksonDBCollection<User, String> collection = DataUtil.getCollection("users", User.class);
 
             user.securityAnswerOne = filledForm.data().get("securityAnswerOne");
             user.securityAnswerTwo = filledForm.data().get("securityAnswerTwo");
             user.securityAnswerThree = filledForm.data().get("securityAnswerThree");
             user.securityQuestionOne = filledForm.data().get("securityQuestionOne");
             user.securityQuestionTwo = filledForm.data().get("securityQuestionTwo");
             user.securityQuestionThree = filledForm.data().get("securityQuestionThree");
 
             collection.save(user);
 
         } catch (Exception e) {
             e.printStackTrace();
             flash("error", "Unexpected error: " + e.toString());
             return internalServerError( index.render(loginForm, filledForm));
         }
 
         return redirect(routes.SurveyController.index());
 
     }
 
 }
