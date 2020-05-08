 package controllers;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.KeySpec;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 import play.api.templates.Html;	
 import play.api.templates.Template1;
 import org.apache.commons.codec.binary.Hex;
 import com.avaje.ebean.Ebean;
 import play.api.libs.Crypto;
 import play.data.Form;
 import play.data.validation.Constraints.Required;
 import play.mvc.Result;
 import scala.collection.mutable.HashMap;
 import models.data.Link;
 import models.user.AuthenticationManager;
 import models.user.Gender;
 import models.user.User;
 import models.user.UserType;
 import models.user.UserID;
 import models.dbentities.UserModel;
 import views.html.landingPages.AdminLandingPage;
 import views.html.landingPages.IndependentPupilLandingPage;
 import views.html.landingPages.OrganizerLandingPage;
 import views.html.landingPages.PupilLandingPage;
 import views.html.register;
 import views.html.registerLandingPage;
 import views.html.login;
 import views.html.loginLandingPage;
 import views.html.error;
 /**
  * This class receives all GET requests and based on there session identifier (cookie)
  * and current role in the system they will be served a different view.
  * @author Sander Demeester
  */
 public class UserController extends EController{
 
 	/**
 	 * This hashmap embodies the mapping from a Type to a view.
 	 * Each view is responsible for getting all information from the DataModel and make a
 	 * beautiful view for the user :)
 	 */
 	private static HashMap<UserType, Class<?>> landingPageHashmap = new HashMap<UserType, Class<?>>();
 	private static AuthenticationManager authenticatieManger = AuthenticationManager.getInstance();
 
 	private static final String COOKIENAME = "avank.auth";
 	
 	public UserController(){
 		landingPageHashmap.put(UserType.ADMINISTRATOR, AdminLandingPage.class);
 		landingPageHashmap.put(UserType.INDEPENDENT, IndependentPupilLandingPage.class);
 		landingPageHashmap.put(UserType.ORGANIZER, OrganizerLandingPage.class);
 		landingPageHashmap.put(UserType.PUPIL,PupilLandingPage.class);
 	}
 	/**
 	 * This methode gets requested when the user clicks on "signup".
 	 * @author Sander Demeester
 	 * @return Result page.
 	 */
 	public static Result signup(){
 		setCommonHeaders();
 		return ok(register.render("Registration", 
 				new ArrayList<Link>(),
 				form(Register.class)
 		));
 	}
 	
 	/**
 	 * this methode is called when the user submits his/here register information.
 	 * @author Sander Demeester
 	 * @return Result page
 	 */
 	public static Result register(){
 		setCommonHeaders();
 		Form<Register> registerForm = form(Register.class).bindFromRequest();
 		if(registerForm.hasErrors()){
 			return badRequest(error.render("Fout", new ArrayList<Link>(), form(Register.class), "Invalid request"));
 		}
 		SecureRandom random = null;
 		SecretKeyFactory secretFactory = null;
 		byte[] passwordByteString = null;
 		String passwordHEX = "";
 		String saltHEX = "";
 		Date birtyDay = new Date();
 		
 		
 		//Zijn de 2 eerste letters van uw voornaam en de 7 of MAX letters van uw achternaam.
 		String bebrasID = null; 
 		
 		try {
 			random = SecureRandom.getInstance("SHA1PRNG");
 		} catch (NoSuchAlgorithmException e) {}
 
 		byte[] salt = new byte[16]; //RSA PKCS5
 		
 		
 		random.nextBytes(salt);
 		
 		KeySpec PBKDF2 = new PBEKeySpec(registerForm.get().password.toCharArray(), salt, 1000, 160);
 
 		try{
 			secretFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
 		}catch(Exception e){}
 
 
 		try {
 			passwordByteString = secretFactory.generateSecret(PBKDF2).getEncoded();
 		} catch (InvalidKeySpecException e) {}
 		try{
 			saltHEX = new String(Hex.encodeHex(salt));
 			passwordHEX = new String(Hex.encodeHex(passwordByteString));
 			birtyDay = new SimpleDateFormat("yyyy/dd/mm").parse(registerForm.get().bday);
 		}catch(Exception e){}
 		
 		bebrasID = registerForm.get().fname.toLowerCase().substring(0,2);
 		bebrasID += registerForm.get().lname.toLowerCase().substring(0, registerForm.get().lname.length() < 7 ? registerForm.get().lname.length() : 7);
 		
 		String r = "Welkom ";
 		r += registerForm.get().fname + "!";
 		
 		if(!registerForm.get().email.isEmpty()){
 
 			if(Ebean.find(UserModel.class).where().eq(
 					"email",registerForm.get().email).findUnique() != null){
 				return ok(error.render("Fout",new ArrayList<Link>(),form(Register.class),"Er bestaat al een gebruiker met het gekozen email address"));
 			}
 		}
 
 		/*
 		 * There needs to be some more logic here for generating bebras ID's 
 		 * Save user object in database.
 		 */
 		new UserModel(new UserID(bebrasID), UserType.INDEPENDENT,
 				registerForm.get().fname + " " + registerForm.get().lname, 
 				birtyDay, 
 				new Date(), 
 				passwordHEX,
 				saltHEX, registerForm.get().email, 
 				Gender.Male, registerForm.get().prefLanguage).save();
 		
 		return ok(registerLandingPage.render("Succes", new ArrayList<Link>(), bebrasID));
 	}
 
 	public static Result login(){
 		setCommonHeaders();
 		Form<Login> loginForm = form(Login.class).bindFromRequest();
 		//We need to do this check, because a user can this URL without providing POST data.
 		if(loginForm.get().id == null && loginForm.get().password == null){
 			return ok(login.render("login", 
 					new ArrayList<Link>(),
 					form(Login.class)
 			));
 		}else{//POST data is available to us. Try to validate the user.
 			return validate_login();
 		}
 		
 	}
 	/**
 	 * This methode is called when the users clicks on "login", the purpose of this code is to validate the users login credentials.
 	 * @author Sander Demeester
 	 * @return returns the loginLandingPage succes, this landing page should redirect to /home
 	 */
 	public static Result validate_login(){
 		setCommonHeaders();
 		Form<Login> loginForm = form(Login.class).bindFromRequest();
 		//We do the same check here.
 		if(loginForm.get().id == null && loginForm.get().password == null){
 			return Application.index();
 		}else{ //POST data is available to us. Try to validate the user.
 			byte[] salt = null; //the users salt saved in the db.
 			byte[] passwordByteString = null; //the output from the PBKDF2 function.
 			String passwordHEX = null; // The password from the PBKDF2 output converted into a string.
 			String passwordDB = null; //the pasword as it is saved in the database.
 			
 			UserModel userModel = Ebean.find(UserModel.class).where().eq(
 					"id",loginForm.get().id).findUnique();
 			if(userModel == null){
 				return ok(loginLandingPage.render("Failed to login", new ArrayList<Link>(), "Error while logging in: email" + loginForm.get().id));
 			}
 			passwordDB = userModel.password;
 			SecretKeyFactory secretFactory = null;
 			try{
 			salt = Hex.decodeHex(userModel.hash.toCharArray());
 			}catch(Exception e){}
 			
 			KeySpec PBKDF2 = new PBEKeySpec(loginForm.get().password.toCharArray(), salt, 1000, 160);
 
 			try{
 				secretFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
 			}catch(Exception e){}
 
 
 			try {
 				passwordByteString = secretFactory.generateSecret(PBKDF2).getEncoded();
 			} catch (InvalidKeySpecException e) {}
 			try{
 				passwordHEX = new String(Hex.encodeHex(passwordByteString));
 			}catch(Exception e){}
 			
 			
 			if(passwordHEX.equals(passwordDB)){ 
 				//TODO: this should be users landing page based on type of account.
 				String cookie = "";
 				try {
 					//generate random id to auth user.
 					cookie = Integer.toString(Math.abs(SecureRandom.getInstance("SHA1PRNG").nextInt(100)));
 					
 					//set the cookie. There really is no need for Crypto.sign because a cookie should be random value that has no meaning
 					response().setCookie(COOKIENAME, Crypto.sign(cookie));
 					
 					//authenticate the user to the AuthenticationManager
 					AuthenticationManager.getInstance().login(userModel);
 				} catch (NoSuchAlgorithmException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return ok(loginLandingPage.render("Succes", new ArrayList<Link>(), "Welkom " + userModel.name));
 			}else{
 				return ok(loginLandingPage.render("Failed to login", new ArrayList<Link>(), passwordHEX + "|" + passwordDB + "|" +
 						new String(Hex.encodeHex(salt))));
 			}
 		}
 	}
 
 	public static Result logout(){
 		//TODO: Tell authenticationManager to log a user out.
 		setCommonHeaders();
 		return null;
 	}
 
 	/**
 	 * @author Sander Demeester
 	 * @return Returns a scala template based on the type of user that is requesting the page.
 	 **/
 	@SuppressWarnings("unchecked")
 	public static Result landingPage(){
 		UserType type = authenticatieManger.getUser().getType();
 		Template1<User, Html> landingPage = (Template1<User, Html>) landingPageHashmap.get(type);
 		
 		return ok(landingPage.render(authenticatieManger.getUser()));
 	}
 
 	/**
 	 * Inline class that contains public fields for play forms. 
 	 * @author Sander Demeester
 	 */
 	public static class Register{
 		@Required
 		public String fname;
 		@Required
 		public String lname;
 		public String email;
 		@Required
 		public String bday;
 		@Required
 		public String password;
 		@Required
 		public String controle_passwd;
 		@Required
 		public String gender;
 		@Required
 		public String prefLanguage;
 	}
 	/**
 	 * Inline class that contains public fields for play forms.
 	 * @author Sander Demeester
 	 */
 	public static class Login{
 		public String id;
 		public String password;
 	}
 
 }
