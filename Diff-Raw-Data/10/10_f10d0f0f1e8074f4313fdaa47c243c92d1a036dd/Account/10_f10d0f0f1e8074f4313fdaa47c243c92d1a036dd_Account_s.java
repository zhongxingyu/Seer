 package controllers;
 
 import play.Logger;
 import play.mvc.Controller;
 
 public class Account extends Controller {
 
     public static void show(String id) {
         if (id == null || id.trim().length() == 0) {
             render();
         } else {
            renderTemplate("/app/views/Account/" + id);
         }
     }
     
     public static void process(String id) {
         Logger.info("processing login information");
         if (id == null || id.trim().length() == 0) {
             render();
         } else {
             String username = params.get("username");
             String password = params.get("password");
 
             if (username == null || password == null) {
                renderTemplate("/app/views/Account/login.html");
             } else {
                 Logger.info("processing login for username " + username);
                 session.put("username", username);
                 processLogin();
             }
         }
     }
     
     private static void processLogin() {
        
     }
     
 }
