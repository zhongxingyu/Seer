 package controllers.user;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Expr;
 import controllers.EController;
 import controllers.util.InputChecker;
 import controllers.util.PasswordHasher;
 import controllers.util.PasswordHasher.SaltAndPassword;
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.UserModel;
 import models.user.AuthenticationManager;
 import models.user.Gender;
 import models.user.Role;
 import play.data.DynamicForm;
 import play.mvc.Content;
 import play.mvc.Result;
 import play.mvc.Results;
 import views.html.commons.noaccess;
 import views.html.user.settings;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Controller for the personal settings.
  * @author Thomas Mortier
  * 
  */
 public class PersonalPageController extends EController {
 
 	/**
 	 * @return True if the user has the rights to edit personal information
 	 */
     public static boolean isAuthorized() {
         return AuthenticationManager.getInstance().getUser().hasRole(Role.SETTINGS);
     }
 
     /**
      * Method which takes care of rendering the correct page for the personal
      * information editor.
      * 
      * @param tab for a certain tab (information/edit information/password change)
      */
     public static Result show(int tab) {
         ArrayList<Link> breadcrumbs = new ArrayList<Link>();
         Content showpage;
         breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
         breadcrumbs.add(new Link(EMessages.get("user.pip.settings"), "/settings"));
         if (isAuthorized()) {
             showpage = settings.render(EMessages.get("user.pip.personalinfo"), breadcrumbs, tab);
         } else {
             showpage = noaccess.render(breadcrumbs);
         }
         return ok(showpage);
     }
 
     /**
      * Change information of user. 
      */
     public static Result changeInformation() {
         boolean error = false;
         Date bd = new Date();
         UserModel userModel = Ebean.find(UserModel.class).where().eq(
                 "id", AuthenticationManager.getInstance().getUser().getID()).findUnique();
 
         // get new given information about user and save in data
         DynamicForm editInfo = form().bindFromRequest();
 
         // name
         if (!editInfo.get("fname").equals("")) {
             userModel.name = editInfo.get("fname");
             AuthenticationManager.getInstance().getUser().data.name = editInfo.get("fname");
         } else {
             flash("error", EMessages.get(EMessages.get("error.no_input_fname")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
         }
 
         // email
         if (!InputChecker.getInstance().isCorrectEmail(editInfo.get("email")) || Ebean.find(UserModel.class).where().and(Expr.eq(
                   "email",editInfo.get("email")),Expr.ne("id",AuthenticationManager.getInstance().getUser().getID())).findUnique() != null) {
             if(editInfo.get("email").isEmpty()) {
                 userModel.email = null;
                 AuthenticationManager.getInstance().getUser().data.email = null;
             } else {
                 flash("error", EMessages.get(EMessages.get("register.same_email")));
                 return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
             }
         } else {
             userModel.email = editInfo.get("email");
             AuthenticationManager.getInstance().getUser().data.email = editInfo.get("email");
         }
 
         // bday
         if (!editInfo.get("bday").equals("")) {
             try {
                 bd = new SimpleDateFormat("MM/dd/yyyy").parse(editInfo.get("bday"));
                 Date currentDate = new Date();
                 if (bd.after(currentDate)) {
                     flash("error", EMessages.get(EMessages.get("error.wrong_date_time")));
                     return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
                 }
             } catch (ParseException e) {
                 flash("error", EMessages.get(EMessages.get("error.date")));
                 return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
             }
             userModel.birthdate = bd;
             AuthenticationManager.getInstance().getUser().data.birthdate = bd;
         } else {
             flash("error", EMessages.get(EMessages.get("error.date")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
         }
 
         // gender
         AuthenticationManager.getInstance().getUser().data.preflanguage = editInfo.get("prefLanguage");
         Gender gen = Gender.Female;
         if (editInfo.get("gender").equals("Male")) {
             gen = Gender.Male;
         } else if (editInfo.get("gender").equals("Other")) {
             gen = Gender.Other;
         }
         AuthenticationManager.getInstance().getUser().data.gender = gen;
 
         // language
         userModel.preflanguage = editInfo.get("prefLanguage");
         EMessages.setLang(editInfo.get("prefLanguage"));
         userModel.gender = gen;
 
         // save new information in db
         Ebean.save(userModel);
 
         // success
         flash("success", EMessages.get(EMessages.get("info.successedit")));
         return Results.redirect(controllers.user.routes.PersonalPageController.show(1));
     }
 
     /**
      * Returns a date in a better readable string.
      * @param dt The date we want to read.
      * @return String which represents the date
      */
     public static String dateToString(Date dt) {
         Calendar cal = new GregorianCalendar();
         String newdate = new String();
         cal.setTime(dt);
         newdate = newdate + Integer.toString(cal.get(Calendar.MONTH) + 1) + "/" + Integer.toString(cal.get(Calendar.DATE))
                 + "/" + Integer.toString(cal.get(Calendar.YEAR));
         return newdate;
     }
 
     /**
      * This method is called when the user clicks on 'Change password'.
     *
      * @return page after password is changed
      * @throws Exception
      */
     public static Result changePassword() throws Exception {
         ArrayList<Link> breadcrumbs = new ArrayList<>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("edit_pwd.edit_pwd"), "/passwedit"));
 
         DynamicForm editPass = form().bindFromRequest();
         UserModel userModel = Ebean.find(UserModel.class).where().eq(
                 "id", AuthenticationManager.getInstance().getUser().getID()).findUnique();
 
         if(!AuthenticationManager.getInstance().hasCurrentUserSamePassw(editPass.get("current_pwd"))) {
             flash("error", EMessages.get(EMessages.get("error.invalid_password")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(2));
         }
 
         if (userModel == null || !editPass.get("n_password").equals(editPass.get("controle_password"))) {
             flash("error", EMessages.get(EMessages.get("register.password_mismatch")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(2));
         }
 
         if (editPass.hasErrors()) {
             flash("error", EMessages.get(EMessages.get("forms.error")));
             return Results.redirect(controllers.user.routes.PersonalPageController.show(2));
         }
 
         PasswordHasher.SaltAndPassword sp = PasswordHasher.generateSP(editPass.get("n_password").toCharArray());
         String passwordHEX = sp.password;
         String passwordSalt = sp.salt;
 
         userModel.password = passwordHEX;
         userModel.hash = passwordSalt;
 
         userModel.save();
 
         flash("success", EMessages.get("edit_pwd.success"));
         return Results.redirect(controllers.user.routes.PersonalPageController.show(2));
     }
 }
