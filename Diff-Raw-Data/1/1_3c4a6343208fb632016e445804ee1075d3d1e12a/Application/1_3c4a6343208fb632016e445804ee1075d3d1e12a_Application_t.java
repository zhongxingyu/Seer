 package controllers;
 
 import models.User;
 import notifiers.Mails;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.Play;
 import play.data.validation.EmailCheck;
 import play.i18n.Messages;
 import play.libs.Codec;
 import play.libs.Crypto;
 
 public class Application extends BaseController {
 
 	public static void register(String email) {
 		Logger.info("[register] email: %s", email);

 		boolean success = false;
 		if (StringUtils.isBlank(email)) {
 			flash.error(Messages.get("email.err.required"));
 		} else if (!new EmailCheck().isSatisfied(null, email, null, null)) {
 			flash.error(Messages.get("email.err.invalid", email));
 		} else if (User.count("byEmail", email) > 0) {
 			flash.error(Messages.get("register.failure.email_exists"));
 		} else {
 			User user = new User(email);
 			if (User.count() == 0) {
 				// If there are no users, the first registered user will be set to administrator
 				user.isAdmin = true;
 			}
 			user.save();
 			Mails.welcome(user);
 			success = true;
 			flash.put("done", true);
 			flash.success(Messages.get("register.success"));
 		}
 		if (!success) {
 			params.flash();
 		}
 		redirect("secure.login");
 	}
 
 	public static void forgot(String email) {
 		Logger.info("[forgot] email: %s", email);
 		boolean success = false;
 		if (StringUtils.isBlank(email)) {
 			flash.error(Messages.get("email.err.required"));
 		} else if (!new EmailCheck().isSatisfied(null, email, null, null)) {
 			flash.error(Messages.get("email.err.invalid"));
 		} else {
 			User user = User.find("byEmail", email).first();
 			if (user == null) {
 				flash.error(Messages.get("forgot.failure.email_not_exist"));
 			} else {
 				user.generateNewAuthCode();
 				user.save();
 				Mails.forgot(user);
 				success = true;
 				flash.put("done", true);
 				flash.success("forgot.success");
 			}
 		}
 		if (!success) {
 			params.flash();
 		}
 		redirect("secure.login");
 	}
 
 	public static void login(String authCode) throws Throwable {
 		fakeLogin();
 		if (Security.authenticate(authCode, null)) {
 			String email = User.find("byAuthCode", authCode).<User> first().email;
 			session.put("username", email);
 			response.setCookie("rememberme", Crypto.sign(email) + "-" + email, "30d");
 			redirect("member.dashboard");
 		} else {
 			flash.error("登录链接已失效");
 			redirect("secure.login");
 		}
 	}
 
 	private static void fakeLogin() {
 		if (Play.mode.isProd() || Security.isConnected()) {
 			return;
 		}
 		User admin = User.find("byIsAdmin", true).first();
 		if (admin == null) {
 			throw new RuntimeException("fakeLogin failed, no active admin found!");
 		}
 		Logger.warn("[Application#fakeLogin] as user: %s", admin.email);
 		session.put("username", admin.email);
 		response.setCookie("rememberme", Crypto.sign(admin.email) + "-" + admin.email, "30d");
 	}
 
 }
