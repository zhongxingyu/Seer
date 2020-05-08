 package controllers;
 //test
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import models.Address;
 import models.Gender;
 import models.User;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
     public static void index(User signedUser) {
     	
     	
     	
     	System.out.println("INDEX");
     	if (signedUser==null || signedUser.email==null)
     	{
     		
     		//User user = Cache.get(session.getId() + "-user", User.class);
     		signedUser=User.convertToUser(Security.session.get("user"));
 
     	    if(signedUser == null) {
     	        // Cache miss
     	    	
     	    	System.out.println("No user");
         		renderTemplate("Application/index.html");
     	        
     	    }
 
     	    else
         	{
         		System.out.println("User signed in"+signedUser.email);
         		renderTemplate("Application/index.html", signedUser); 
         	}
     		
     	}
     	else
     	{
     		System.out.println("User signed in");
     		renderTemplate("Application/index.html", signedUser); 
     	}
     	
     	
     }
     
     public static void signup(){
     	
     	renderTemplate("Application/registration.html");
     } 
     
     public static void signupform(String txtFirstName, String txtLastName, String txtEmail, String txtPassword, String txtRepPassword, String selDay, String selMonth, String selYear, String txtWeight, String rdGender, String txtStreet, String txtNumber, String txtCity, String txtProvince, String txtCountry, String txtPhoneNumber, String txtMobile, String chkAgree){
     	Address address = new Address(txtStreet, txtNumber, txtCity, txtProvince, txtCountry);
     	
         Date dateStr;
         User user=null;
 		Calendar cal = Calendar.getInstance();
 		cal.set(Integer.parseInt(selDay), Integer.parseInt(selMonth), Integer.parseInt(selYear));
 		dateStr = cal.getTime();
 		
 		Gender engender;
 		if (rdGender.equals("male"))
 			engender = Gender.MALE;
 		else
 			engender = Gender.FEMALE;
 		System.out.println("First name: " + txtFirstName);
 
 		user = new User(txtEmail, txtPassword, txtFirstName, txtLastName, dateStr, engender, Double.parseDouble(txtWeight), address, txtPhoneNumber, txtMobile);
 		System.out.println("E-mail: " + user.email);
 		address.save();
 		user.save(); 
 		
 		LogMaker.log("UserActivity", user, "has just registred");
 		
 		//Cache.set(session.getId() + "-user", user, "30mn");
 		Security.session.put("user", user.convertToString());
     	index(user);
     }
     
     public static void signinform(String txtUsername, String txtPassword){
     	System.out.println("Sign in form" + txtUsername);
     	
     	User signedUser = (User) User.find("email = ? AND password = ?", txtUsername, txtPassword).first();
     	if (signedUser!=null)
     	{
    		System.out.println("User found");
     		//Security.session.put("user", signedUser.email);
     		Security.session.put("user", signedUser.convertToString());
     		System.out.println("Name:" + signedUser.firstName + " " + signedUser.lastName); 
     		
     		LogMaker.log("UserActivity", signedUser, "has just signed in");
     		
     		renderTemplate("Application/index.html", signedUser);   		
     	}
     	else
     	{
     		LogMaker.log("UserActivity", null, "failed sign in");
     		Boolean wrongCred=true;
     		renderTemplate("Application/index.html",wrongCred);
    		System.out.println("User not found");
     	}
     	
     }
     
     public static void signout(String email)
     {
 		User signedUser=User.convertToUser(Security.session.get("user"));
     	LogMaker.log("UserActivity", signedUser, "has signed  out");
     	Security.session.put("user", null);
     	try {
 			//Secure.logout();
 		} catch (Throwable e) {
 			// TODO Auto-generated catch block
 			LogMaker.log("UserActivity", signedUser, "failed signed out");
 			e.printStackTrace();
 		}
     	renderTemplate("Application/index.html");
     }
     
     public static void bmi_calculator() {
     	User signedUser=User.convertToUser(Security.session.get("user"));
     	render(signedUser);
     }
 
     public static void bmi_history() {
     	User signedUser=User.convertToUser(Security.session.get("user"));
     	render(signedUser);
     }
 
     public static void about_us() {
     	
     	User signedUser=User.convertToUser(Security.session.get("user"));
     	render(signedUser);
 	        
 	    
     }
 
     public static void equipments_for_you() {
     	User signedUser=User.convertToUser(Security.session.get("user"));
         render(signedUser);
     }
 
     public static void contact() {
     	User signedUser=User.convertToUser(Security.session.get("user"));
     	render(signedUser);
     }
 
 }
