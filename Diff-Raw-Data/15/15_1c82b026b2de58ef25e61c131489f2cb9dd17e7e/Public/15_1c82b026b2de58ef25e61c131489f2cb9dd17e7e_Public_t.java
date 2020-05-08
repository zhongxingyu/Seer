 package controllers;
 
 import actions.GlobalContextParams;
 import models.User;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.With;
 
 /**
  * <p>
  * Provides public actions without security checks.
  * </p>
  *
  */

public class Public extends Application {
 
     public static Result changeLanguage(String code) {
         changeLang(code);
         User loggerUser = GlobalContextParams.loggedUser();
         return redirect(routes.Application.index());
     }
 }
