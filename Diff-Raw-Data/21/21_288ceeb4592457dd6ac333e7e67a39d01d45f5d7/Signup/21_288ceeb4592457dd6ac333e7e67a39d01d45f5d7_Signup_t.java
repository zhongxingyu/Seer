 package controllers;
 
 import models.TokenAction;
 import models.TokenAction.Type;
 import models.User;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 import providers.MyUsernamePasswordAuthProvider;
 import providers.MyUsernamePasswordAuthProvider.MyIdentity;
 import providers.MyUsernamePasswordAuthUser;
 import views.html.account.*;
 import views.html.*;
 
 import java.util.Collection;
 import java.util.List;
 
 import static play.data.Form.form;
 
 public class Signup extends Controller {
 
 	public static class PasswordReset extends Account.PasswordChange {
 
 		public PasswordReset() {
 		}
 
 		public PasswordReset(final String token) {
 			this.token = token;
 		}
 
 		public String token;
 
 		public String getToken() {
 			return token;
 		}
 
 		public void setToken(String token) {
 			this.token = token;
 		}
 	}
 
 	private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);
 
 	public static Result unverified() {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		return notFound("Unverified User");
 	}
 
 	private static final Form<MyIdentity> FORGOT_PASSWORD_FORM = form(MyIdentity.class);
 
 	public static Result doForgotPassword() {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		final Form<MyIdentity> filledForm = FORGOT_PASSWORD_FORM.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			// User did not fill in his/her email
 			return badRequest(Application.getValidationErrorsHtml(filledForm.errors().values()));
 		} else {
 			// The email address given *BY AN UNKNWON PERSON* to the form - we
 			// should find out if we actually have a user with this email
 			// address and whether password login is enabled for him/her. Also
 			// only send if the email address of the user has been verified.
 			final String email = filledForm.get().email;
 
 			// We don't want to expose whether a given email address is signed
 			// up, so just say an email has been sent, even though it might not
 			// be true - that's protecting our user privacy.
             String msg = Messages.get("playauthenticate.reset_password.message.instructions_sent", email);
 			final User user = User.findByEmail(email);
 			if (user != null) {
 				// yep, we have a user with this email that is active - we do
 				// not know if the user owning that account has requested this reset, though.
 				final MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
 				// User exists
 				if (user.emailValidated) {
 					provider.sendPasswordResetMailing(user, ctx());
 					// In case you actually want to let (the unknown person) know whether a user was
 					// found/an email was sent, use,change the flash message
 				} else {
 					// We need to change the message here, otherwise the user
 					// does not understand whats going on - we should not verify
 					// with the password reset, as a "bad" user could then sign
 					// up with a fake email via OAuth and get it verified by an
 					// a unsuspecting user that clicks the link.
                     msg = Messages.get("playauthenticate.reset_password.message.email_not_verified");
 					// You might want to re-send the verification email here...
 					provider.sendVerifyEmailMailingAfterSignup(user, ctx());
 				}
 			}  else {
                 msg = "User not signed up";
             }
             return ok(msg);
 		}
 	}
 
 	/**
 	 * Returns a token object if valid, null if not
 	 * 
 	 * @param token
 	 * @param type
 	 */
 	private static TokenAction tokenIsValid(final String token, final Type type) {
 		TokenAction ret = null;
 		if (token != null && !token.trim().isEmpty()) {
 			final TokenAction ta = TokenAction.findByToken(token, type);
 			if (ta != null && ta.isValid())
 				ret = ta;
 		}
 		return ret;
 	}
 
 	public static Result resetPassword(final String token, String errors) {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
 		if (ta == null) {
             return redirect(routes.Application.tokenFail());
 		}
         return ok(password_reset.render(token, errors));
 	}
 
 	public static Result doResetPassword() {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM.bindFromRequest();
 		if (filledForm.hasErrors()) {
             Collection<List<ValidationError>> errors = filledForm.errors().values();
             String errorList = "<ul>";
             for (List<ValidationError> vel : errors)
                 for (ValidationError ve : vel)
                     errorList += "<li>" + ve.key() + ": " + ve.message() + "</li>";
             errorList += "</ul>";
             return redirect(routes.Signup.resetPassword(filledForm.data().get("token"), errorList));
 
 		} else {
 
 			final String token = filledForm.data().get("token");
 			final String newPassword = filledForm.data().get("password");
 			final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
 			if (ta == null)
 				return redirect(routes.Application.passwordResetFail("Invalid Token"));
 			final User u = ta.targetUser;
 			try {
 				// Pass true for the second parameter if you want to
 				// automatically create a password and the exception never to happen
				u.resetPassword(new MyUsernamePasswordAuthUser(newPassword), true);
 			} catch (final RuntimeException re) {
                 return redirect(routes.Application.passwordResetFail("Error trying to reset the password account"));
 			}
             return redirect(routes.Application.passwordResetSuccess());
 		}
 	}
 
 	public static Result oAuthDenied(final String getProviderKey) {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		return ok(oAuthDenied.render(getProviderKey));
 	}
 
 	public static Result exists() {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		return ok("User exists");
 	}
 
 	public static Result verify(final String token) {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		final TokenAction ta = tokenIsValid(token, Type.EMAIL_VERIFICATION);
 		if (ta == null)
             return redirect(routes.Application.tokenFail());
 		User.verify(ta.targetUser);
         return redirect(routes.Application.tokenSuccess());
 	}
 
 }
