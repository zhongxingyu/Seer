 package controllers;
 
 import com.shopservice.domain.ClientSettings;
 import play.cache.Cache;
 import play.mvc.Controller;
 import play.mvc.Result;
 import tyrex.services.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: user50
  * Date: 28.09.13
  * Time: 21:29
  * To change this template use File | Settings | File Templates.
  */
 public class LoginController extends Controller {
 
 
     public static Result authentication()
     {
         String password = request().body().asFormUrlEncoded().get("j_password")[0];
         String username = request().body().asFormUrlEncoded().get("j_username")[0];
 
         ClientSettings clientSettings = ClientSettings.getBySiteName(username);
 
        if (!clientSettings.password.equals(password))
             return redirect("assets/error.html");
 
         String key = UUID.create();
         Cache.set(key, clientSettings.id);
 
         response().setCookie("key", key);
 
         return redirect("assets/page.html?clientId="+clientSettings.id);
     }
 }
