 package controllers;
 
 import exceptions.FormValidationException;
 import exceptions.QueueException;
 import models.*;
 import models.norpneu.Company;
 import play.Logger;
 import play.Routes;
 import play.data.Form;
 import play.i18n.Messages;
 import play.mvc.Result;
 import util.EnhancedController;
 import util.NotificationManager;
 import util.PlayUtils;
 import views.html.*;
 
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class Application extends EnhancedController {
     private static final String INIT_PASSWORD = "init";
     private static final String RESET_PASSWORD = "reset";
 
     public static Result login() {
         return ok(login.render(form(Login.class)));
     }
 
     public static Result logout() {
         session().clear();
         //flash("success", "You've been logged out");
         return redirect(routes.Application.login());
     }
 
     public static Result authenticate() {
 
         Form<Login> loginForm = form(Login.class).bindFromRequest();
         if (loginForm.hasErrors()) {
             return badRequest(login.render(loginForm));
         } else {
             session("email", loginForm.get().email);
             // Generate a unique ID
             String uuid = session("uuid");
             if (uuid == null) {
                 uuid = java.util.UUID.randomUUID().toString();
                 session("uuid", uuid);
             }
 
 
             // Set the cache
             User sessionUserObject = (User) play.cache.Cache.get(uuid + "user");
             sessionUserObject = User.findByEmail(session("email"));
             play.cache.Cache.set(uuid + "user", sessionUserObject);
             return redirect(routes.Dashboard.index());
         }
     }
 
     public static Result registerForward() {
         return ok(register.render(form(RegisterUser.class)));
     }
 
     public static Result registerAction() {
         Form<RegisterUser> userForm = form(RegisterUser.class).bindFromRequest();
         if (userForm.hasErrors()) {
             return badRequest(register.render(userForm));
         }
         RegisterUser registerUser = userForm.get();
         try {
             if (User.findByEmail(userForm.get().email) != null) {
                 addGlobalError(userForm, Messages.get("registration.duplicate.email", registerUser.email));
                 return badRequest(register.render(userForm));
             }
 
             Company company = null;
             if ((company = Company.findByCompanyName(safePullModel(userForm).companyName)) == null) {
                 company = new Company();
                 company.name = safePullModel(userForm).companyName;
                 company.phoneNumber = safePullModel(userForm).phoneNumber;
                 company.street = safePullModel(userForm).street;
                 company.city = safePullModel(userForm).city;
                 company.postalCode = safePullModel(userForm).postalCode;
                 company.save();
                 company.refresh();
             }
 
             User user = new User();
             user.email = safePullModel(userForm).email;
             user.name = safePullModel(userForm).name;
             user.phoneNumber = safePullModel(userForm).phoneNumber;
             user.company = company;
             user.save();
         } catch (FormValidationException e) {
             return badRequest(register.render(userForm));
         }
         // Add notification to queue
         try {
             NotificationManager.queueNotification(userForm.get().email,
                     Messages.get("registration.pending.approval.email.subject"),
                     Messages.get("registration.pending.approval.email.body"));
             flash("success", Messages.get("registration.pending.approval", userForm.get().email));
         } catch (QueueException e) {
             flash("success", Messages.get("registration.pending.approval.error", userForm.get().email));
             Logger.error("Error sending email for:" + userForm.get().email, e);
         }
         return redirect(routes.Application.login());
     }
 
     public static Result aboutForward() {
         return ok(about.render());
     }
 
     public static Result resetPasswordForward(String email, String token) {
         User user = User.find.where()
                 .eq("email", email)
                 .eq("resetToken", token)
                 .ge("resetTokenExpirationDate", new Timestamp(System.currentTimeMillis()))
                 .findUnique();
 
         if (user == null)
             flash("error", "Registo não encontrado");
 
         return ok(changepassword.render(email, token, RESET_PASSWORD, form(ChangePassword.class)));
     }
 
     public static Result recoverPasswordForward() {
         return ok(recoverpassword.render(form(RecoverPassword.class)));
     }
 
     /**
      * Generate token to recover user and send email notification
      *
      * @return
      */
     public static Result recoverPasswordAction() {
         Form<RecoverPassword> recoverPasswordForm = form(RecoverPassword.class).bindFromRequest();
         if (recoverPasswordForm.hasErrors()) {
             return badRequest(recoverpassword.render(recoverPasswordForm));
         } else {
             // less then optimized way of getting the user and tag password for recovery
             if (User.tagPasswordForRecovery(recoverPasswordForm.get().email)) {
                 User u = User.find.where().eq("email", recoverPasswordForm.get().email).eq("active", true).findUnique();
                 try {
                     NotificationManager.queueNotification(u.email,
                             Messages.get("login.reset.password.email.subject"),
                             Messages.get("login.reset.password.email.body",
                                     PlayUtils.getApplicationConfig("host"), u.email, u.resetToken));
                     flash("success", Messages.get("login.reset.password.success", u.email));
                 } catch (QueueException e) {
                     Logger.error("Error sending email for:" + u.email, e);
                     addGlobalError(recoverPasswordForm, "Foi impossível enviar o email de momento. P.f. tente mais tarde");
                     return badRequest(recoverpassword.render(recoverPasswordForm));
                 }
             }
         }
         return ok(login.render(form(Login.class)));
     }
 
     public static Result changePasswordForward(String email, String hash) {
         User user = User.find.where()
                 .eq("email", email)
                 .eq("password", hash)
                 .findUnique();
 
         if (user == null)
             flash("error", "Registo não encontrado");
 
         return ok(changepassword.render(email, hash, INIT_PASSWORD, form(ChangePassword.class)));
     }
 
     public static Result changePasswordAction() {
         Form<ChangePassword> form = form(ChangePassword.class).bindFromRequest();
         ChangePassword changePasswordModel = null;
         try {
             changePasswordModel = safePullModel(form);
             if (changePasswordModel.email != null) {
                 if (!changePasswordModel.password.equals(changePasswordModel.confirmPassword)) {
                     addGlobalError(form, "A palavra passe e confirmação não são iguais");
                     return badRequest(changepassword.render(changePasswordModel.email, changePasswordModel.token, changePasswordModel.action, form));
                 }
 
                 User user;
                 if (changePasswordModel.action.equals(INIT_PASSWORD)) {
                     user = User.find.where()
                             .eq("email", changePasswordModel.email)
                             .eq("password", changePasswordModel.token)
                             .findUnique();
                 } else {
                     user = User.find.where()
                             .eq("email", changePasswordModel.email)
                             .eq("resetToken", changePasswordModel.token)
                             .gt("resetTokenExpirationDate", new Timestamp(System.currentTimeMillis()))
                             .findUnique();
                 }
 
                 if (user == null) {
                     addGlobalError(form, "Utilizador não foi encontrado ou passou demasiado tempo desde o envio do email até à tentativa de alteração. " +
                             "P.f. requira nova recuperação de palavra passe");
                     return badRequest(changepassword.render(changePasswordModel.email, changePasswordModel.token, changePasswordModel.action, form));
                 }
                 user.password = User.obfuscatePassword(changePasswordModel.password);
                 user.resetToken = null;
                 user.resetTokenExpirationDate = null;
                 //TODO: handle persistence exceptions - should be done in enhanced controller
                 user.update();
             }
         } catch (FormValidationException e) {
             //TODO: how can I get the email if form is not valid?!?!
             return badRequest(changepassword.render("", changePasswordModel.token, changePasswordModel.action, form));
         } catch (NoSuchAlgorithmException e) {
             Logger.error("Error in  obfuscating password.", e);
             addGlobalError(form, "Erro de incriptação da password. Por favor tente mais tarde");
             return badRequest(changepassword.render(changePasswordModel.email, changePasswordModel.token, changePasswordModel.action, form));
         } catch (UnsupportedEncodingException e) {
             Logger.error("Error in  obfuscating password.", e);
             addGlobalError(form, "Erro de incriptação da password. Por favor tente mais tarde");
             return badRequest(changepassword.render(changePasswordModel.email, changePasswordModel.token, changePasswordModel.action, form));
         }
         flash("success", "Palavra passe alterada com sucesso. Por favor efectue agora o seu login");
         return redirect(routes.Application.login());
     }
 
     public static Result javascriptRoutes() {
         response().setContentType("text/javascript");
         return ok(
                 Routes.javascriptRouter("jsRoutes"
                 ));
     }
 }
