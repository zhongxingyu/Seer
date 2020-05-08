 package controllers;
 
 import play.mvc.Controller;
 
 import play.data.validation.Required;
 
 public class Dummy extends Controller {
 
 	public static void index(){
 		if(!Security.isConnected())
 			render();
 		else
 			PageController.welcome();
 	}
 	
 	public static void createAccount(){
 		render();
 	}
 	
 	public static void newAccount(@Required String email, @Required String password,
 			@Required String firstName, @Required String lastName,
 			@Required String address, @Required String phoneNumber,
 			@Required String sex, String code){
 		
     	if (validation.hasErrors()) {
            render("createAccount.html");
         }
 		//TODO: Create the user, then log them in automagically
 		
 	}
 	
 }
