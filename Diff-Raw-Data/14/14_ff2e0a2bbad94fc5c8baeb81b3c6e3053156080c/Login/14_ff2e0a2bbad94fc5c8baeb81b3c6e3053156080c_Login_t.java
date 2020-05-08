 package controllers;
 
 import java.util.List;
 
 import org.hibernate.loader.custom.Return;
 
 import models.User;
 import play.data.validation.Valid;
 import play.mvc.*;
 import play.mvc.Http.Cookie;
 import play.mvc.results.Result;
 
 public class Login extends Controller {
 
     static User user;
     static String currentUser(){
     	return user.firstName;
     }
 
     @Before
     static void addUser() {
         User user = connected();
         if (user != null) {
             renderArgs.put("user", user);
         }
     }
 
     static User connected() {
         if (renderArgs.get("user") != null) {
             return renderArgs.get("user", User.class);
         }
         String mail = session.get("user");
         if (mail != null) {
             return User.find("mail", mail).first();
         }
         return null;
     }
 
     public static void index() {
         if (connected() != null) {
 
             index();
 
         }
 
         if (loggedIn()) {
         }
 
         render();
     }
 
     public static void register() {
         render();
     }
 
     public static void saveUser(@Valid User user, String verifyPassword) {
         validation.required(verifyPassword);
         validation.equals(verifyPassword, user.password).message("Your password doesn't match");
         if (validation.hasErrors()) {
             render("@register", user, verifyPassword);
         }
         user.create();
         session.put("user", user.mail);
         response.setCookie("FriendsBoutiqueCookie", user.mail, "30d");
         flash.success("Welcome, " + user.firstName);
 
         Account.index();
 
     }
 
     public static boolean loggedIn() {
         if (Http.Request.current().cookies.get("FriendsBoutiqueCookie") != null) {
             String mail = Http.Request.current().cookies.get("FriendsBoutiqueCookie").value;
             user = User.find("byMail", mail).first();
             List<User> users = User.findAll();
             if (users.contains(user)) {
                 session.put("user", mail);
                 flash.success("Welcome back, " + user.firstName);
                 Account.index();
                 return true;
             } else {
                 return false;
             }
         }
         return false;
     }
 
     public static void login(String mail, String password) {
 
 
         User user = User.find("byMailAndPassword", mail, password).first();
         if (user != null) {
             session.put("user", user.mail);
             session.put("firstName", user.firstName);
             response.setCookie("FriendsBoutiqueCookie", mail, "30d");
             flash.success("Welcome, " + user.firstName);
 
             Account.index();
 
         }
 
         // Oops
         flash.put("mail", mail);
         flash.error("Login failed");
         index();
     }
 
     public static void logout() {
         session.clear();
         response.removeCookie("FriendsBoutiqueCookie");
         index();
     }
 
     public static void settings() {
         renderTemplate("Login/settings.html");
     }
 
     public static void saveSettings(String password, String verifyPassword) {
         User connected = connected();
         connected.password = password;
         validation.valid(connected);
         validation.required(verifyPassword);
         validation.equals(verifyPassword, password).message("Your password doesn't match");
         if (validation.hasErrors()) {
             render("@settings", connected, verifyPassword);
         }
         connected.save();
         flash.success("Password was updated");
         Account.index();
     }
 }
