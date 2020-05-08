 package festivalv2.services;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import festivalv2.LoginInfo;
 import festivalv2.NotLoggedInException;
 
 public class LoginServiceImpl  implements
     LoginService {
 
 	private static final long serialVersionUID = 1L;
	//private static String[] ValidEmails = { "a", "james.kingston@gmail.com", "codsallartsfestival@googlemail.com", "pandab58@googlemail.com", "peter.birkert@googlemail.com", "flissk@gmail.com" };
 	static UserService userService = UserServiceFactory.getUserService();
 	
 	public LoginInfo login(String requestUri) {
 		
 		User user = getUser();
 		LoginInfo loginInfo = new LoginInfo();
 
 		if (user != null) {
 			loginInfo.setLoggedIn(true);
 			loginInfo.setEmailAddress(user.getEmail());
 			loginInfo.setNickname(user.getNickname());
 			loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));
 		} else {
 			loginInfo.setLoggedIn(false);
 			loginInfo.setLoginUrl(userService.createLoginURL(requestUri));
 		}
 		return loginInfo;
 	}
 
 	public static boolean isValidEmail(String email){
		return true;
		//or (String valid: ValidEmails){
		//	if (valid.equals(email)){
		//		return true;
		//	}
		//}
		//return false;
 	}
 	
 	private static User getUser() {
 		
 		User user = userService.getCurrentUser();
		if (user != null && userService.isUserAdmin()){
 			return user;
 		}
 		return null;
 	}
 	
 	public static void checkLoggedIn() throws NotLoggedInException {
 	
 		if (userService.getCurrentUser() == null){
 			throw new NotLoggedInException("Not a valid user");
 		}
 		if (getUser() == null) {
 			throw new NotLoggedInException("Not logged in.");
 		}
 	}	
 	
 }
