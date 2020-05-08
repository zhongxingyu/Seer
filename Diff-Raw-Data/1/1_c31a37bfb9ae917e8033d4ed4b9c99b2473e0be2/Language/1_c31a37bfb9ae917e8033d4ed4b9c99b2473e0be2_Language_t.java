 package controllers;
 
 import views.html.language;
 import models.User;
 import play.mvc.*;
 import play.i18n.Lang;
 import play.i18n.Messages;
 
 public class Language extends Controller {
 
 	// Retrieve localized message defined in a conf/messages* file.
 	public static String get(String key, java.lang.Object... args) {
 		return Messages.get(getLang(), key, args);
 	}
 
 	// TODO: Add protection from Cross-Site Request Forgeries
	// TODO: Perhaps set language in user profile if logged in
 	public static Result chooseLanguage() {
 		Lang lang = getLangFromQueryString();
 		if(lang != null)
 			changeLang(lang);
 		return ok(language.render());
 	}
 
 	public static void changeLang(Lang lang) {
 		response().setCookie("lang", lang.code());
 	}
 
 	public static Lang getLang() {
 		Lang lang;
 
 		lang = getLangFromQueryString();
 		if(lang == null)
 			lang = getLangFromCookies();
 		if(lang == null)
 			lang = lang();
 		return lang;
 	}
 
 	private static Lang getLangFromQueryString() {
 		String code;
 		Lang lang = null;
 		String reqlang[] = request().queryString().get("lang");
 		if(reqlang == null || reqlang.length == 0)
 			return null;
 		code = reqlang[0];
 		try {
 			lang = Lang.forCode(code);
 		} catch(Exception e) {
 			lang = null;
 		}
 		return lang;
 	}
 
 	private static Lang getLangFromCookies() {
 		String code;
 		Lang lang = null;
 		play.mvc.Http.Cookie cookie = request().cookies().get("lang");
 		if(cookie == null)
 			return null;
 		code = cookie.value();
 		try {
 			lang = Lang.forCode(code);
 		} catch(Exception e) {
 			lang = null;
 		}
 		return lang;
 	}
 
 }
