 package be.kdg.groepi;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vincent
  * Date: 6/02/13
  * Time: 9:05
  * To change this template use File | Settings | File Templates.
  */
 
 import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
     @RequestMapping(value = "/")
     public String home() {
         System.out.println("HomeController: Passing through...");
         //UserService.createUser(); //TODO: HEY DAVE
         return "home";
     }
 
     @RequestMapping(value = "/login")
     public String login() {
         System.out.println("Login: Passing through...");
         return "login/showlogin";
     }
     @RequestMapping(value = "/topmenu")
     public String topmenu() {
         System.out.println("Topmenu: Passing through...");
         return "menu/topmenu";
     }
 
    @RequestMapping(value = "/error/{error}")
    public ModelAndView errorpage(@PathVariable("error") String errorid) {
        System.out.println("Error page");
        return new ModelAndView("error/displayerror", "errorid", errorid);
    }
     @RequestMapping(value = "/profile/register")
     public String register(){
         System.out.println("Register: Passing through...");
         return "profile/register";
     }
 
 }
