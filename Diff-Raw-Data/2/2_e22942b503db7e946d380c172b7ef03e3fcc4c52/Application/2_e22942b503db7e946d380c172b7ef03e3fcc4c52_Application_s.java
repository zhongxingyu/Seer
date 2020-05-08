 package controllers;
 
 
 import play.*;
 import play.mvc.*;
 
 
 
 public class Application extends Controller {
 
 
 
     private static void checkMobile() {
         String user_agent = request.headers.get("user-agent").value();
         String[] keyWords = {"iPad", "iPhone", "Android", "BlackBerry"};
         String webkit = "WebKit";
         boolean isWebkit = user_agent.contains(webkit) || user_agent.contains(webkit) || user_agent.contains(webkit);
 
         for (String keyword : keyWords) {
             if (user_agent.contains(keyword) ||
                     user_agent.contains(keyword.toLowerCase()) ||
                     user_agent.contains(keyword.toUpperCase()))
             {
                 if (isWebkit)
                     redirect("http://m.webube.com");
             }
         }
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
 
     public static void mobileApp() {
         renderTemplate("Application/eagle-technology.html");
     }
 
 }
