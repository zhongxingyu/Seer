 package controllers;
 
 import models.ContactDB;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.formdata.ContactFormData;
 import views.formdata.LoginFormData;
 import views.formdata.TelephoneTypes;
 import views.html.Index;
 import views.html.Login;
 import views.html.NewContact;
 import views.html.Profile;
 
 /**
  * Implements the controllers for this application.
  */
 public class Application extends Controller {
 
   /**
    * Returns home, a page that contains sample contacts.
    * 
    * @return The Home.
    */
   public static Result index() {
     return ok(Index.render(ContactDB.getContacts(), Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
   }
 
   /**
    * Returns newcontact, a page that simulates an add contact page.
    * 
    * @param id the id of the Contact
    * 
    * @return The NewContact.
    */
   public static Result newContact(long id) {
     ContactFormData data = (id == 0) ? new ContactFormData() : new ContactFormData(ContactDB.getContact(id));
     Form<ContactFormData> formData = Form.form(ContactFormData.class).fill(data);
    return ok(NewContact.render(formData, TelephoneTypes.getTypes(data.telephoneType), Secured.isLoggedIn(ctx()),
        Secured.getUserInfo(ctx())));
   }
 
   /**
    * Posts the contact info to the Play console and adds it to the contact database.
    * 
    * @return contact info to the play console.
    */
   public static Result postContact() {
     Form<ContactFormData> formData = Form.form(ContactFormData.class).bindFromRequest();
 
     if (formData.hasErrors()) {
       System.out.println("Errors Found");
      return badRequest(NewContact.render(formData, TelephoneTypes.getTypes(), Secured.isLoggedIn(ctx()),
          Secured.getUserInfo(ctx())));
     }
     else {
       ContactFormData data = formData.get();
       ContactDB.add(data);
       System.out.println("OK: " + data.firstName + " " + data.lastName + " " + data.telephone + " "
           + data.telephoneType);
      return ok(NewContact.render(formData, TelephoneTypes.getTypes(), Secured.isLoggedIn(ctx()),
          Secured.getUserInfo(ctx())));
     }
   }
 
   /**
    * Provides the Login page (only to unauthenticated users).
    * 
    * @return The Login page.
    */
   public static Result login() {
     Form<LoginFormData> formData = Form.form(LoginFormData.class);
     return ok(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData));
   }
 
   /**
    * Processes a login form submission from an unauthenticated user. First we bind the HTTP POST data to an instance of
    * LoginFormData. The binding process will invoke the LoginFormData.validate() method. If errors are found, re-render
    * the page, displaying the error data. If errors not found, render the page with the good data.
    * 
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
    * 
    * @return A redirect to the Index page.
    */
   @Security.Authenticated(Secured.class)
   public static Result logout() {
     session().clear();
     return redirect(routes.Application.index());
   }
 
   /**
    * Provides the Profile page (only to authenticated users).
    * 
    * @return The Profile page.
    */
   @Security.Authenticated(Secured.class)
   public static Result profile() {
     return ok(Profile.render("Profile", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
   }
 }
