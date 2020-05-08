 package controllers;
 
 import play.mvc.Controller;
 import play.data.validation.Required;
 import models.*;
 
 public class UserController extends Controller {
     
     public static void login(String userNameOrEmail,String password){
         User user = User.validateUser(userNameOrEmail,password);
         if(user != null){
            session.put("user", user);
             flash.success("Welcome, ", user.userName);
            Application.deskScreen(user);
             //render("@Application.deskScreen",user);
         }else{
             flash.error("Login Failed");
             Application.index();
         }
     }
 }
