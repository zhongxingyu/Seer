 package controllers;
 
 import models.User;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
     public static User activeUser;
 
     public static void index() {
         render();
     }
 }
