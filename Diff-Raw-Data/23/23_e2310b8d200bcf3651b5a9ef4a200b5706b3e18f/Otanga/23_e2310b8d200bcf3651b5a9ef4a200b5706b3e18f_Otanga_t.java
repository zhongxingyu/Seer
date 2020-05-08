package otanga;
 
 import com.google.appengine.api.users.*;
 // import com.google.appengine.api.datastore.Entity; // temporary
 
 public final class Otanga {
 	private final static UserService userService = UserServiceFactory.getUserService();
 
 	private Otanga() {
 	}
 
 	private static User getUser() {
 		return userService.getCurrentUser();
 	}
 
 	public static String getLoginUrl(String uri) {
 		return userService.createLoginURL(uri);
 	}
 
 	public static String getLogoutUrl(String uri) {
 		return userService.createLogoutURL(uri);
 	}
 
 	public static String getUserName() {
 		return getUser().getNickname();
 	}
 
 	public static boolean isUserLoggedIn() {
 		return (getUser() != null);
 	}
 
 	public static boolean isProfileStored() {
 		
 		// uncomment this line to delete the user (if it exists)
 		// Storage.deleteUserProfile(getUser().getUserId());
 		
 		return Storage.userProfileExists(getUser().getUserId());
 		
 		// return Storage.getUser(getUser().getUserId()) != null;
 	}
 
 	public static String testStoreAndUpdateProfile() {
 		Profile profile = Storage.createUserProfile(getUser().getUserId());
 		if (profile != null) {
 			// creation was successful; we can now try to update
 			
 			String nickname = getUser().getNickname();
 			int indx = nickname.indexOf("@");
 		    if (indx > 0)
 		    	nickname = nickname.substring(0, indx);
 			
 			profile.setProperty("nickname", nickname + "Nickname");
 			Storage.updateUserProfile(profile);
 			
 			// get the profile again to test if the changes 
 			// were reflected in the underlying datastore
 			profile = Storage.getUserProfile(getUser().getUserId());
 			if (profile != null && profile.hasProperty("nickname"))
 				return (String)profile.getProperty("nickname");
 			else
 				return "(error)";
 		}
 		
 		/*
 		long id = Storage.storeUser(getUser().getUserId());
 		if (id >= 0) {
 			if (Storage.updateUser(getUser().getUserId(), "TheNickname")) {
 				Entity entity = Storage.getUser(getUser().getUserId());
 				return (String) entity.getProperty("nickname");
 			}
 		}
 		*/
 		return "(error)";
 	}
 
 	public static String getStoredProfile() {
 		Profile profile = Storage.getUserProfile(getUser().getUserId());
 		return (profile != null) ? (String)profile.getProperty("nickname") : "(error)";
 		
 		/*
 		Entity entity = Storage.getUser(getUser().getUserId());
 		if (entity != null)
 			return (String) entity.getProperty("nickname");
 		else
 			return "!error!";
 		*/
 	}
 
 }
