 package controllers;
 
 import java.util.Date;
 
 import models.BMIHistory;
 import models.User;
 import play.mvc.Controller;
 
//@With(Secure.class)
 public class BMIHistoryController extends Controller {
 
 	public static void bmihistoryadd(String hiddenWeight, String hiddenHeight,
 			String hiddenBMI) {
 		// String email = Security.connected();
 		User signedUser = User.convertToUser(Security.session.get("user"));
 		String email = null;
 		if (signedUser != null) {
 			email = signedUser.email;
 			signedUser = User.find("byEmail", signedUser.email).first();
 			BMIHistory history = new BMIHistory(signedUser, new Date(),
 					Float.parseFloat(hiddenWeight),
 					Float.parseFloat(hiddenHeight), Float.parseFloat(hiddenBMI));
 			history.save();
 			System.out.println(signedUser.BMIHistoryList.size());
 			LogMaker.log("BMI_Activity", signedUser, "has added a BMI");
 
 			renderTemplate("Application/bmi_history.html",
 					signedUser);
 		} else {
 			renderTemplate("Application/index.html");
 		}
 	}
 
 	public static void bmihistory() {
 		// String email = Security.connected();
 		User signedUser = User.convertToUser(Security.session.get("user"));
 		String email = null;
 		if (signedUser != null)
 			email = signedUser.email;
 
 		User user = (User) User.find("byEmail", email).first();
 		signedUser = user;
 		if (user != null)
 		{		
 			System.out.println(signedUser.email);
 			renderTemplate("Application/bmi_history.html", signedUser);
 		}
 		else
 		{
 			renderTemplate("Application/bmi_history.html", signedUser);
 		}
 	}
 
 }
