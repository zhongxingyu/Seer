 package controllers;
 
 import play.*;
 import play.libs.WS;
 import play.mvc.*;
 
 import java.util.*;
 
 import com.google.gson.JsonObject;
 
 import models.*;
 
 public class Application extends Controller {
     
     @Before(unless="index")
     static void auth() {
         JsonObject me = null;
         String access_token=params.get("access_token");
         if (access_token != null) {
             me = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(access_token)).get().getJson().getAsJsonObject();
             if(me.get("error")==null) {
                User user=User.find("byFBid", me.get("id").getAsInt()).first();
                 if(user==null) {
                     user=new User(me.get("id").getAsInt(),me.get("name").getAsString(),me.get("first_name").getAsString(),me.get("last_name").getAsString());
                     user.create();
                 }
                 session.put("uuid", user.id);
             }
             else {
                 renderText(me);
             }
         }
         else {
             renderText("Need access_token");
         }
     }
     
     public static void index() {
         render();
     }
     
     public static void user() {
         User user=User.findById(Long.parseLong(session.get("uuid")));
         renderJSON(user);
     }
     
     
 
 }
