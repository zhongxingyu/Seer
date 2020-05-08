 package authorization.impl.dbuserpass;
 
 import java.util.HashMap;
 
 import aQute.bnd.annotation.component.Component;
 import authorization.api.AuthorizationToken;
 import authorization.api.IAuthorization;
 
 /**
  * This class is the implementation of the enum type DB_USERNAME_PASSWORD in {@link IAuthorization}.
  *
  */
 @Component
 public class DBUserPassAuthorization implements IAuthorization {
 
 	private HashMap<String, String> users;
 	
 	public DBUserPassAuthorization() {
 		users = new HashMap<String, String>();
 		//testdata
 		users.put("test", "pass");
 		users.put("desarc", "hei");
 	}
 	
 	@Override
 	public Type getType() {
 		return IAuthorization.Type.DB_USERNAME_PASSWORD;
 	}
 
 	@Override
 	public boolean authorize(AuthorizationToken token) {
		if (token.getType().equals(getType())) {
 			if (userExists(token.getId()) && users.get(token.getId()).equals(token.getPasscode())) {
 				return true;			
 			}
 			System.out.println("Access denied.");
 		}
 		else {
 			System.out.println("Invalid AuthorizationToken.");
 		}
 		return false;
 	}
 	
 	private boolean userExists(String id) {
 		if (users.get(id) != null) {
 			return true;
 		}
 		System.out.println("User "+id+" does not exist.");
 		return false;
 	}
 }
