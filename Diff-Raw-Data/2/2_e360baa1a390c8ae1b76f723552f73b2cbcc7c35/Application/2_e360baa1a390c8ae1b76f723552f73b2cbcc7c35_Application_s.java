 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.ArrayList;
 
 
 public class Application extends Controller {
 
     private static void checkMobile() {
         String user_agent = request.headers.get("user-agent").value();
         String[] keyWords = {"iPad", "iPhone", "Android", "BlackBerry"};
         int mobileAgent = -1;
 
         for (int i = 0; i < keyWords.length && mobileAgent < 0; i++)
             mobileAgent = user_agent.indexOf(keyWords[i]);
 
         if (mobileAgent > 0)
            redirect("/public/eagle-technology.html");
     }
 
 
 
     public static void index(String host) {
 
         Logger.info(host);
 
         if ("m.webube.com".equals(host))
             renderTemplate("Application/eagle-technology.html");
         else {
             Application.checkMobile();
             render();
         }
     }
 
     public static void webApp() {
         render();
     }
 
     public static void webSite() {
         render();
     }
 
     public static void company() {
         render();
     }
 
     public static void reference() {
         render();
     }
 
     public static void contact() {
         render();
     }
 
     public static void phoenixProject() {
         render();
     }
 
     public static void appFacebook() {
         render();
     }
 
     public static void cloud() {
         render();
     }
 
 }
