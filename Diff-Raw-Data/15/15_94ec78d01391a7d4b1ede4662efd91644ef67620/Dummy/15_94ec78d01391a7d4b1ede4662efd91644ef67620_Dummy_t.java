 package controllers;
 
 import models.*;
 
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
 	
	public static void newAccount(@Required(message = "Please enter a valid email address") String email,
			@Required(message = "Please enter a password") String password,
			@Required(message = "Please enter your first name") String firstName,
			@Required(message = "Please enter your last name") String lastName,
			@Required(message = "Please enter your address")  String address,
			@Required(message = "Please enter your phone number") String phoneNumber,
			@Required String sex,
			String code){
 		
     	//Check that the email isnt registered already
     	if(User.find("byUsername", email).fetch().size() > 0){
     		//Email already registered.
    		validation.addError("email", "Email already registered", "email");
     		
     	}
     	
     	if (validation.hasErrors()) {
             render("Dummy/createAccount.html", email, firstName, lastName, address, phoneNumber, code);
         }
     	
     	//If the code is correct, create user as physician
     	if(code.equals("physician")){
     		new Physician(email, password, firstName, lastName).save();
     	} else {
     		//Else, create them as a patient
     		new Patient(email, password, firstName, lastName, address, phoneNumber, sex.charAt(0)).save();
     	}
     	
     	//Authenticate them automatically
     	session.put("username", email);
     	PageController.welcome();
 	}
 	
 }
