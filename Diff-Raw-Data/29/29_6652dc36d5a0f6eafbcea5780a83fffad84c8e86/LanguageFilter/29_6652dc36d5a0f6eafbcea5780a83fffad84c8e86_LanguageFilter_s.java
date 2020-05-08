 package controllers;
 
 import play.i18n.Messages;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: sheldon
  * Date: 3/21/12
  * Time: 6:30 PM
  * To change this template use File | Settings | File Templates.
  */
 public class LanguageFilter extends Controller {
     @Before
     public static void checkLanguage() {
         if (session.get("language") == null) {
             // browser settings may have multiple languages. for example, "zh-CN", "zh", "en-US", "en"
             List<String> languages = request.acceptLanguage();
             if (languages != null && !languages.isEmpty()) {
                 // pick the first one that is supported
                 for (String language : languages) {
                     if (Messages.locales.containsKey(language)) {
                         session.put("language", language);
                         break;
                     }
                 }
                session.put("languageInitRequired", true);
             }
        } else {
            session.put("languageInitRequired", false);
         }
     }
 }
