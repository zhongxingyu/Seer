 package controllers.user;
 
 import com.avaje.ebean.Ebean;
 import controllers.*;
 import controllers.util.PasswordHasher;
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.ClassGroup;
 import models.dbentities.UserModel;
 import models.mail.EMail;
 import models.mail.ForgotPwdMail;
 import models.mail.StudentTeacherEmailReset;
 import play.data.Form;
 import play.data.validation.Constraints;
 import play.data.validation.Constraints.Required;
 import play.mvc.Result;
 import views.html.commons.noaccess;
 import views.html.forgotPwd;
 import views.html.login.resetPwd;
 
 import javax.mail.MessagingException;
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ResetPasswordController extends EController {
 
     private static SecureRandom secureRandom = new SecureRandom();
 
     /**
      * This method is called when a user hits the 'Forgot Password' button.
      *
      * @return forgot_pwd page
      */
     public static Result forgotPwd() {
         List<Link> breadcrumbs = new ArrayList<>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("forgot_pwd.forgot_pwd"), "/forgotPwd"));
         return ok(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
                 breadcrumbs,
                 form(ForgotPwd.class)
         ));
     }
 
     public static Result forgotPwdSendMail() throws InvalidKeySpecException, NoSuchAlgorithmException {
         List<Link> breadcrumbs = new ArrayList<>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("forgot_pwd.forgot_pwd"), "/forgotPwd"));
 
 
         Form<ForgotPwd> form = form(ForgotPwd.class).bindFromRequest();
 
         if (form.hasErrors()) {
             flash("error", EMessages.get(EMessages.get("forms.error")));
             return badRequest(forgotPwd.render((EMessages.get("forgot_pwd.forgot_pwd")), breadcrumbs, form));
         }
         System.out.println(form.get().id);
         String id = form.get().id;
         UserModel userModel = Ebean.find(UserModel.class).where().eq("id", id).findUnique();
 
         if (userModel == null) {
             flash("error", EMessages.get("error.text"));
             return badRequest(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
         }
 
         // There are two cases, the user has an email or the user does not has a email
 
         if (!userModel.email.isEmpty()) {
             // Case 1
 
             //check if provided email is the same as stored in the database associated with the ID
             if (!userModel.email.equals(form.get().email)) {
                 flash("error", EMessages.get("error.text"));
                 return badRequest(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
             }
             // Put reset token into database
             userModel.reset_token = new BigInteger(130, secureRandom).toString(32);
             Ebean.save(userModel);
 
             String baseUrl = request().host() + "/reset_password?token=" + userModel.reset_token;
             //TODO: delete
             System.out.println(baseUrl);
             // Prepare email
             EMail mail = new ForgotPwdMail(userModel.email, userModel.id, baseUrl);
             try {
                 mail.send();
                 flash("success", EMessages.get("forgot_pwd.mail"));
                 return ok(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
             } catch (MessagingException e) {
                 flash("error", EMessages.get("forgot_pwd.notsent"));
                 return badRequest(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
             }
         } else if (userModel.email.isEmpty() && userModel.classgroup > 0) {
             // Case 2
             Integer classGroupID = userModel.classgroup;
             ClassGroup g = Ebean.find(ClassGroup.class).where().eq("id", classGroupID).findUnique();
             String teacherEmail = g.getTeacher().getData().email;
             EMail mail = new StudentTeacherEmailReset(teacherEmail, userModel.id, "url");
             try {
                 mail.send();
                 flash("success", EMessages.get("forgot_pwd.mail"));
                 return ok(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
             } catch (MessagingException e) {
                 flash("error", EMessages.get("forgot_pwd.notsent"));
                 return badRequest(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
             }
         } else {
             flash("error", EMessages.get("error.text"));
             return badRequest(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
         }
     }
 
     /**
      * The method is called when the user clicks in the email on the provided link.
      * The purpose of this method is to generate a new time-based token to verify the
      * form validity and the provide a new view for the users to enter his new password.
      *
      * @param token the generated token <url>?token=TOKEN
      * @return if the provided token is valid, this method will return a view for the user to set his new password.
      */
     public static Result receivePasswordResetToken(String token) {
         //TODO: remove
         System.out.println(token);
 
         List<Link> breadcrumbs = new ArrayList<>();
         breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
         breadcrumbs.add(new Link(EMessages.get("app.signUp"), "/signup"));
 
         UserModel userModel = Ebean.find(UserModel.class).where().eq("reset_token", token).findUnique();
         if (userModel == null) {
             return ok(noaccess.render(breadcrumbs));
         } else {
             Form<ResetPasswordVerify> reset_form = form(ResetPasswordVerify.class);
 
             // old token that is being re-used?
             // generate new token to send back to the client to make sure that we don't get a random request.
             // it's import that time is included in this token.
             String secure_token = new BigInteger(130, secureRandom).toString(32);
 
             Long unixTime = System.currentTimeMillis() / 1000L;
             secure_token = secure_token + unixTime.toString();
 
             //TODO: remove
             System.out.println("secure token :" + secure_token);
 
             userModel.reset_token = secure_token;
 
             // Save  new token.
             userModel.save();
 
             return ok(resetPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
                     breadcrumbs,
                     reset_form,
                     secure_token
             ));
         }
     }
 
     /**
      * This method is called when the users filled in his new password.
      * The purpose of this method is to calculate the new hash value of the password and store it into the database
      *
      * @return
      * @throws Exception
      */
     public static Result resetPassword() throws Exception {
         List<Link> breadcrumbs = new ArrayList<>();
         breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
         breadcrumbs.add(new Link(EMessages.get("app.signUp"), "/signup"));
 
         Form<ResetPasswordVerify> form = form(ResetPasswordVerify.class).bindFromRequest();
         String id = form.get().id;
         String reset_token = form.get().reset_token;
         UserModel userModel = Ebean.find(UserModel.class).where().eq("id", id).findUnique();
 
         //TODO:
         // We perform some checks on the server side (view can be skipped).
         if (userModel == null || userModel.reset_token.isEmpty() || !form.get().password.equals(form.get().confirmPassword)) {
             return ok(noaccess.render(breadcrumbs));
         }
         String reset_token_database = userModel.reset_token;
 
 
         System.out.println("reset token client: " + reset_token);
         System.out.println("reset token server: " + reset_token_database);
 
         Long time_check = Long.parseLong(reset_token_database.substring(26, reset_token_database.length()));
         Long system_time_check = (System.currentTimeMillis() / 1000L);
 
         System.out.println("time check        : " + time_check);
         System.out.println("time check_server : " + system_time_check);
         System.out.println("-                 : " + (system_time_check - time_check));
 
         System.out.println(reset_token);
 
         // 1 min time to fill in new password
         if (reset_token.equals(reset_token_database) && (system_time_check - time_check) < 60) {
 
             PasswordHasher.SaltAndPassword sp = PasswordHasher.generateSP(form.get().password.toCharArray());
             String passwordHEX = sp.password;
             String saltHEX = sp.salt;
 
             userModel.password = passwordHEX;
             userModel.hash = saltHEX;
 
             userModel.reset_token = "";
 
             userModel.save();
 
             flash("success", EMessages.get("forgot_pwd.reset_success"));
             return ok(resetPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
                     breadcrumbs,
                     form,
                     reset_token
             ));
         } else {
             flash("error", EMessages.get("forgot_pwd.reset_fail"));
             return ok(resetPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
                     breadcrumbs,
                     form,
                     reset_token
             ));
         }
     }
 
     public static class ForgotPwd {
         public String id;
         public String email;
     }
 
     public static class ResetPasswordVerify {
         public String id;
         public String password;
         public String confirmPassword;
         public String reset_token;
     }
 }
