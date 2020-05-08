 package controllers;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import models.data.Link;
 import models.dbentities.UserModel;
 import models.user.AuthenticationManager;
 import models.user.User;
 import models.user.UserType;
 import play.Play;
 import play.api.libs.Crypto;
 import play.api.templates.Html;
 import play.api.templates.Template2;
 import play.data.Form;
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.mvc.Result;
 import play.mvc.Results;
 import views.html.landingPages.AdminLandingPage;
 import views.html.landingPages.IndependentPupilLandingPage;
 import views.html.landingPages.OrganizerLandingPage;
 import views.html.landingPages.PupilLandingPage;
 import views.html.login.error;
 import views.html.login.register;
 import views.html.login.registerLandingPage;
 
 import com.avaje.ebean.Ebean;
 
 /**
  * This class receives all GET requests and based on there session identifier (cookie)
  * and current role in the system they will be served a different view.
  * @author Sander Demeester, Ruben Taelman
  */
 public class UserController extends EController{
 
     /**
      * This hashmap embodies the mapping from a Type to a view.
      * Each view is responsible for getting all information from the DataModel and make a
      * beautiful view for the user :)
      */
     private static HashMap<UserType, Class<?>> LANDINGPAGES = new HashMap<UserType, Class<?>>();
     static {
         LANDINGPAGES.put(UserType.ADMINISTRATOR, AdminLandingPage.class);
         LANDINGPAGES.put(UserType.INDEPENDENT, IndependentPupilLandingPage.class);
         LANDINGPAGES.put(UserType.INDEPENDENT, IndependentPupilLandingPage.class);
         LANDINGPAGES.put(UserType.ORGANIZER, OrganizerLandingPage.class);
         LANDINGPAGES.put(UserType.PUPIL,PupilLandingPage.class);
     };
 
     /**
      * This methode gets requested when the user clicks on "signup".
      * @author Sander Demeester
      * @return Result page.
      */
     public static Result signup(){
         List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link("Sign Up", "/signup"));
         return ok(register.render("Registration",
                 breadcrumbs,
                 form(Register.class)
                 ));
     }
 
     /**
      * this methode is called when the user submits his/here register information.
      * @author Sander Demeester
      * @return Result page
      */
     public static Result register(){
         // Bind play form request.
         Form<Register> registerForm = form(Register.class).bindFromRequest();
 
         // Check if the email adres is uniqe.
         if(!registerForm.get().email.isEmpty()){
 
             if(Ebean.find(UserModel.class).where().eq(
                     "email",registerForm.get().email).findUnique() != null){
                 return badRequest(error.render("Error",new ArrayList<Link>(),form(Register.class),"There is already a user with the selected email address"));
             }
         }
 
         // If the form contains error's (specified by "@"-annotation in the class "Register" then this will be true.
         if(registerForm.hasErrors()){
             return badRequest(error.render("Error", new ArrayList<Link>(), form(Register.class), "Invalid request"));
         }
 
         // Delegate create user to Authentication Manager.
         String bebrasID = null;
         try {
             bebrasID = AuthenticationManager.getInstance().createUser(registerForm);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return ok(registerLandingPage.render("Succes", new ArrayList<Link>(), bebrasID));
     }
 
     /**
      * This methode is called when the users clicks on "login".
      * @author Sander Demeester, Ruben Taelman
      * @return returns the users cookie.
      */
     public static Result validate_login(String id, String password) throws Exception{
         // We do the same check here, if the input forms are empty return a error message.
         if(id == "" || password == "") {
             return badRequest("Please enter an ID and password.");
         } else if(AuthenticationManager.getInstance().validate_credentials(id, password)){
             String cookie = "";
             try {
                 //generate random id to auth user.
                 cookie = Integer.toString(Math.abs(SecureRandom.getInstance("SHA1PRNG").nextInt(100)));
 
                 //set the cookie. There really is no need for Crypto.sign because a cookie should be random value that has no meaning
                 cookie = Crypto.sign(cookie);
                 response().setCookie(AuthenticationManager.COOKIENAME, cookie);
 
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             }
             return ok(cookie);
         } else {
             return badRequest("Invalid ID and password.");
         }
     }
 
     /**
      * Logout current user
      * @author Sander Demeester
      * @return Result
      */
     public static Result logout(){
         AuthenticationManager.getInstance().logout();
         return Results.redirect(routes.Application.index());
     }
 
     /**
      * @author Sander Demeester
      * @return Returns a scala template based on the type of user that is requesting the page.
      **/
     @SuppressWarnings("unchecked")
     public static Result landingPage() throws Exception{
         List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link("Dashboard", "/home"));
 
         UserType type = AuthenticationManager.getInstance().getUser().getType();
         if(type.equals(UserType.ANON)) {
             return Results.redirect(routes.Application.index());
         } else {
             Class<?> object = Play.application().classloader().loadClass("views.html.landingPages." + LANDINGPAGES.get(type).getSimpleName() + "$");
             Template2<User,List<Link>, Html> viewTemplate = (Template2<User,List<Link>, Html>)object.getField("MODULE$").get(null);
             return ok(viewTemplate.render(AuthenticationManager.getInstance().getUser(), breadcrumbs));
         }
     }
 
     /**
      * Inline class that contains public fields for play forms.
      * @author Sander Demeester
      */
     public static class Register{
         @Required
         public String fname;
         @Required
         public String lname;
         public String email;
         @Required
         @Formats.DateTime(pattern = "yyyy/dd/mm")
         public String bday;
         @Required
         public String password;
         @Required
         public String controle_passwd;
         @Required
         public String gender;
         @Required
         public String prefLanguage;
     }
     /**
      * Inline class that contains public fields for play forms.
      * @author Sander Demeester
      */
     public static class Login{
         public String id;
         public String password;
     }
 
 }
