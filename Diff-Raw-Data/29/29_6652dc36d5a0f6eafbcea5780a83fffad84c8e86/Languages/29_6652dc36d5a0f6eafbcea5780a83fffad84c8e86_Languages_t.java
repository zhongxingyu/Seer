 package controllers;
 
 import com.google.gson.Gson;
 import play.i18n.Messages;
 import play.mvc.Controller;
 
import java.util.Map;
 import java.util.Properties;
 
 /**
  * Created by IntelliJ IDEA.
  * User: root
  * Date: 4/19/12
  * Time: 5:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Languages extends Controller {
    public static Map<String, String> localMessages;
     /**
      * this is invoked from a <script/> tag as a javascript file
      */
     public static void localize() {
        if (localMessages.get(session.get("language")) == null) {
             // init language by converting messages properties into json string
             Properties properties = Messages.all(session.get("language"));
             if (properties == null) {
                 properties = Messages.defaults;
             }
            localMessages.put(session.get("language"), new Gson().toJson(properties));
         }
         // set the right content-type instead of the default text/plain; browswer would warn if not set.
         response.contentType = "text/javascript";
        renderText("mytinytodo.lang.init(" + localMessages.get(session.get("language")) + ");");
     }
 }
