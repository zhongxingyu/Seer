 package controllers;
 
 import play.*;
 import play.data.validation.Required;
 import play.data.validation.Validation.ValidationResult;
 import play.db.jpa.GenericModel.JPAQuery;
 import play.db.jpa.JPABase;
 import play.libs.Mail;
 import play.mvc.*;
 
 import groovy.ui.text.FindReplaceUtility;
 
 import java.util.*;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import models.*;
 
 
 public class Application extends Controller {
 
     public static void index() {
     	if (Security.isConnected()) {
     		redirect("Activities.listAll");
     	} 
     	redirect("Secure.login");
     }
     
     public static void signup() {
     	render();
     }
 
     public static void createUser(@Required(message="Ange namn") String user_name,
     		@Required(message="Ange email") String user_email,
     		@Required(message="Ange lösenord") String user_password,
     		@Required(message="Bekräfta lösenord") String user_password_confirmation
     		) {
     	
     	
     	ValidationResult validationResult = validation.email(user_email);
     	validationResult.message("Ogiltig Emailadress");
     	validationResult = validation.equals(user_password, user_password_confirmation);
     	validationResult.message("Lösenord och bekräfta lösenord ska vara samma");
     	
     	User user = User.findUserByUsername(user_email);
     	if (user != null) {
     		validation.addError(user_email, "Det finns redan en användera med email-adress %s", user_email);
     	} 
 
 		if (validation.hasErrors()) {
 			render("Application/signup.html");
 		}
     	
     	
     	user = new User(user_name, user_email, "1234567", user_password);
     	user.save();
     	
     	flash.success("Din användare är nu skapad. Nu kan du logga in");
     	//Send to login
     	redirect("Secure.login");
     }
     
     public static void createUserFromActivity(String name, String email) {
     	//Simple validation
     	ValidationResult valRes = validation.email(email);
     	if (!valRes.ok || email == null || email.equals("")) {
     		error("Ogiltig Emailadress");
     		return;
     	}
     	if (name.equals("")) {
     		error("Ange ett namn");
     		return;
     	}
     	
     	//Check if user exist
     	User user = User.findUserByUsername(email);
     	if (user == null) {
     		//Create user
     		user = new User(name,email, "1234567", email);
     		user.save();
     		informUserByEmail(email, name);
     	} else {
     		List<UserFriend> myFriends = UserFriend.find("byUserId", Security.connected()).fetch();
     		for (UserFriend userFriend : myFriends) {
 				if (userFriend.friendId.equals(email)) {
 					error("Användaren med email " + userFriend.friendId + " ("+ User.findUserByUsername(userFriend.friendId).name +  ") finns redan i listan");
 					return;
 				}
 			}
     		if (email.equals(Security.connected())) {
     			error("Du kan inte lägga till dig själv i listan. Du är redan inkluderad i aktiviteten");
     			return;
     		}
     	}
     	UserFriend first = new UserFriend(Security.connected(), email);
     	UserFriend second = new UserFriend(email, Security.connected());
     	first.save();
     	second.save();
     	
     }
     
     private static void informUserByEmail(String emailAddress, String name) {
 		User currentUser = User.findUserByUsername(Security.connected());
     	try {
 			SimpleEmail email = new SimpleEmail();
 			email.setFrom("info@klarlistan.nu");
 			email.addTo(emailAddress);
 			email.setSubject("Nu finns du som användare på klarlistan");
 			email.setMsg("Hej, " + name + "\n\n" + currentUser.name + " har skapat en användare åt dig på klarlistan.\n\n" +
 					"För att logga in använder du följande uppgifter:\n\n" + 
 					"Användarnamn: " + emailAddress + "\n" +
 					"Lösenord: " + emailAddress + "\n\n" +
 					"Hälsningar, Klarlistan"
 					);
 			Mail.send(email);
 		} catch (EmailException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void updateUser(String user_name, String user_email, String user_password, String user_password_confirmation) {
     	flash.clear();
     	validation.clear();
     	
     	if (!user_password.equals(user_password_confirmation)) {
     		validation.addError(user_password_confirmation, "Lösenorden överensstämmer inte");
     	}
     	if ("".equals(user_name)) {
     		validation.addError(user_name, "Namnet måste innehålla minst ett tecken");
     	}
 
     	User user = User.findUserByUsername(user_email);
     	if (validation.hasErrors()) {
 			render("Users/showAccountInfo.html", user);
 			return;
 		}
     	
     	user.name = user_name;
     	user.password = user_password;
     	user.save();
     	
     	flash.success("Dina uppgifter är nu uppdaterade");
     	
     	render("Users/showAccountInfo.html", user);
     }
     
 }
