 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.validation.*;
 import play.libs.*;
 import play.cache.*;
 
 import java.util.*;
 import java.security.*;
 
 import models.*;
 
 
 public class Application extends Controller {
 
 	//Rendering the index page
     public static void index() {
     	render();
     }
     
     //Rendering the registration page (and generating a unique ID for the captcha image)
     public static void register() {
     	String randomID = Codec.UUID();
         render(randomID);
     }
     
     //Rendering the Privacy Policy page
     public static void privacyPolicy()
     {
     	render();
     }
     
     //Registration of a new User
     public static void saveUser(@Valid User user, String verifyPassword, @IsTrue String policyAgreement, @Required String code, String randomID) {
         validation.required(verifyPassword);
         validation.equals(verifyPassword, user.password).message("Your password doesn't match");
         validation.equals(code, Cache.get(randomID)).message("Invalid Code, please type again!");
         if(validation.hasErrors()) {
            render("@register", user, verifyPassword);
         }
         user.create();
         session.put("user", user.nick_name);
         flash.success("Welcome, " + user.nick_name + "! Start renting Sports equipment right away!");
         index();
     }
     
     //Generation of captcha images using the play libraries
     public static void captcha(String id) {
         Images.Captcha captcha = Images.captcha();
         //#000000 (ie black) represents the color of the captcha text
         String code = captcha.getText("#000000");
         Cache.set(id, code, "10mn");
         renderBinary(captcha);
     }
     
     //Generating a hash value using a given method for some data
     public static String getHash(String data, String hashMethod)
     {
     	MessageDigest md;
 		try {
 			//Setting the Hashfunction to SHA-256 (MD5 and others are available as well)
 			md = MessageDigest.getInstance("SHA-256");
 			
 			//Setting the data --> can be done more than once
 			md.update(data.getBytes());
 			
 			//generating the Hash
 			byte[] byteData = md.digest();
 			
 			//generating a hex representation
 			StringBuffer sb = new StringBuffer();
 			
 	        for (int i = 0; i < byteData.length; i++) {
 	        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
 	        }
 	        
 	        return sb.toString();
 			
 		} 
 		catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			return "";
 		}
     }
 
 }
