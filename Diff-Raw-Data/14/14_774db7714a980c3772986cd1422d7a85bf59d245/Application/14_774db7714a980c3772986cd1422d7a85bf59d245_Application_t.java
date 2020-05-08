 package controllers;
 
 import java.util.Map;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.html.Index;
 import views.html.Login;
 import views.html.NewContact;
 import views.formdata.ContactFormData;
 import views.formdata.LoginFormData;
 import views.formdata.TelephoneTypes;
 import models.ContactDB;
 import models.UserInfoDB;
 import models.UserInfo;
 
 /**
  * Implements the controllers for this application.
  */
 public class Application extends Controller {
 
   /**
    * Returns the home page. 
    * @return The resulting home page. 
    */
   @Security.Authenticated(Secured.class)
   public static Result index() {
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     Boolean isLoggedIn = (userInfo != null);
     if (isLoggedIn && userInfo.getEmail() != null) {
       String user = userInfo.getEmail();
       return ok(Index.render("Home", isLoggedIn, userInfo, ContactDB.getContacts(user)));
     }
     else { return redirect(routes.Application.logout()); }
   }
   
   /**
    * Returns newcontact page.
    * @param id The id to get or create a new instance if id is zero
    * @return the NewContact page.
    */
   @Security.Authenticated(Secured.class)  
   public static Result newContact(long id) {
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     Boolean isLoggedIn = (userInfo != null);
     String user = userInfo.getEmail();
     ContactFormData data = (id == 0) ? new ContactFormData() : new ContactFormData(ContactDB.getContact(user, id));
     Form<ContactFormData> formData = Form.form(ContactFormData.class).fill(data);
     Map<String, Boolean> telephoneTypeMap = TelephoneTypes.getTypes(data.telephoneType);
     if (isLoggedIn && userInfo.getEmail() != null) {
       return ok(NewContact.render("New", isLoggedIn, userInfo, formData, telephoneTypeMap));
     }
     else { return redirect(routes.Application.logout()); }
   }
   
   /**
    * Handles post of form data.
    * @return the NewContact page.
    */
   @Security.Authenticated(Secured.class)  
   public static Result postContact() {
     Form<ContactFormData> formData = Form.form(ContactFormData.class).bindFromRequest();
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     Boolean isLoggedIn = (userInfo != null);
     String user = userInfo.getEmail();
     if (formData.hasErrors()) {
       Map<String, Boolean> telephoneTypeMap = TelephoneTypes.getTypes();
       return badRequest(NewContact.render("New", isLoggedIn, userInfo, formData, telephoneTypeMap));
     }
     else {
       ContactFormData data = formData.get();
       ContactDB.addContact(user, data);
       Map<String, Boolean> telephoneTypeMap = TelephoneTypes.getTypes();      
      return ok(Index.render("Home", isLoggedIn, userInfo, ContactDB.getContacts(user)));
     }    
   }
 
   /**
    * The login page.
    * @return Login page.
    */
   public static Result login() {
     Form<LoginFormData> formData = Form.form(LoginFormData.class);
     return ok(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData));
   }
 
   /**
    * Processes a login form submission from an unauthenticated user. 
    * First we bind the HTTP POST data to an instance of LoginFormData.
    * The binding process will invoke the LoginFormData.validate() method.
    * If errors are found, re-render the page, displaying the error data. 
    * If errors not found, render the page with the good data. 
    * @return The index page with the results of validation. 
    */
   public static Result postLogin() {
 
     // Get the submitted form data from the request object, and run validation.
     Form<LoginFormData> formData = Form.form(LoginFormData.class).bindFromRequest();
 
     if (formData.hasErrors()) {
       flash("error", "Login credentials not valid.");
       return badRequest(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData));
     }
     else {
       // email/password OK, so now we set the session variable and only go to authenticated pages.
       session().clear();
       session("email", formData.get().email);
       return redirect(routes.Application.index());
     }
   }
   
   /**
    * Logs out (only for authenticated users) and returns them to the Index page. 
    * @return A redirect to the Index page. 
    */
   @Security.Authenticated(Secured.class)
   public static Result logout() {
     session().clear();
     return redirect(routes.Application.index());
   }
 
 }
 
