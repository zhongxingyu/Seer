 package controllers;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.about;
 import views.html.about_en;
 
 public class Application extends Controller {
 
 	public static final String FULLSCREEN_MAP_ID = "_USER_ID_";
 
 	public static Result index() {
 		return redirect(routes.Application.map());
 	}
 
 	public static Result map() {
 		return ok(views.html.map.render(null, false, false));
 	}
 
 	public static Result focusedMap(final String userId) {
 		return ok(views.html.map.render(userId, false, false));
 	}
 
 	public static Result fullscreenMap() {
 		return ok(views.html.map.render(FULLSCREEN_MAP_ID, true, false));
 	}
 
 	public static Result about(String lang) {
		if (lang.contains("en")) {
 			return ok(about_en.render(false));
 		} else {
 			return ok(about.render(false));			
 		}
 	}
 
 	public static Result mobileAbout(String lang) {
		if (lang.contains("en")) {
 			return ok(about_en.render(true));
 		} else {
 			return ok(about.render(true));			
 		}
 	}
 }
