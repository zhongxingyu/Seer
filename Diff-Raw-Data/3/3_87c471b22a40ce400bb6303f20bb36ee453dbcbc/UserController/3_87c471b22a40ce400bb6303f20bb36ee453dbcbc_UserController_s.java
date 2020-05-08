 package controllers;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import models.Service;
 import models.User;
 
 import com.sun.istack.internal.FinalArrayList;
 
 import controllers.securesocial.SecureSocial;
 import controllers.securesocial.UsernamePasswordController;
 import play.data.validation.Equals;
 import play.data.validation.Required;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import securesocial.provider.AuthenticationMethod;
 import securesocial.provider.ProviderType;
 import securesocial.provider.SocialUser;
 import securesocial.provider.UserId;
 import securesocial.provider.UserService;
 import securesocial.utils.SecureSocialPasswordHasher;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: TE162141
  * Date: 10/25/12
  * Time: 9:07 PM
  * To change this template use File | Settings | File Templates.
  */
 public class UserController extends Controller {
 
 	private static final String emailAlreadyRegistered = "emailAlreadyRegistered";
 	
     public static void registerUser(@Required String fname, 
     		@Required String lname,
     		@Required String email,
     		@Required String password,
     		@Required @Equals(message = "securesocial.passwordsMustMatch", value = "password") String password2,
     		@Required String city,
     		@Required String gender,
     		@Required String dob,
     		@Required String country,
     		String phone
     		
     		)
     {
     	
     	/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
     	try {
 			Date dobDate = sdf.parse(dob);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
     	
     	
     	
     	UserId id = new UserId();
 		id.id = email;
 		id.provider = ProviderType.userpass;
 		
 		if ( UserService.find(id) != null ) {
 		validation.addError(emailAlreadyRegistered, Messages.get(emailAlreadyRegistered));
 		}
 		
     	
     	
     	SocialUser user = new SocialUser();
 		user.id = id;
 		user.firstName = fname;
 		user.lastName=lname;
 		user.email = email;
 		user.password = SecureSocialPasswordHasher.passwordHash(password);
 		// the user will remain inactive until the email verification is done.
 		user.isEmailVerified = true;
 		user.authMethod = AuthenticationMethod.USER_PASSWORD;
 		
 		user.city=city;
 		user.phone=phone;
 		user.gender=gender;
     	
     	System.out.println("Inside Custom user controller");
 		//UsernamePasswordController.createAccount(fname, lname, email, phone, password, password2, city, gender, null);
 		
 		User newUser = new User();
 		newUser.userid = id.id;
 		newUser.details = user;
 		
 		newUser.save(); 
 		
 		System.out.println("User Account created : "+newUser.userid);
 		
     	
 		//render("/public/html/sign-up.html");
 
         System.out.println("user registered : "+id.id);
 
     }
 
     public static void logOutUser()
     {
     	SecureSocial.logout();
     	
     }
 
 
     public static void showUserProfile()
     {
        render();
     }
 
     public static void userProfile()
     {
 
       SocialUser currentUser =    SecureSocial.getCurrentUser();
 
         List<Service> userServices = Service.q().filter("createdBy",currentUser.email).asList();
 
 
         render(userServices);
     }
 
     public static void editProfile()
     {
         render();
     }
 
 
 
 }
