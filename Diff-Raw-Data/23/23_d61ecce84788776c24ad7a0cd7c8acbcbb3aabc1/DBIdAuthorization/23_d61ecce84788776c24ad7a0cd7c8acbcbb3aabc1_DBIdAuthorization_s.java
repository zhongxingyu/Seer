 package authorization.impl.dbid;
 
 import java.util.HashMap;
 
 import aQute.bnd.annotation.component.Component;
 import authorization.api.AuthorizationToken;
 import authorization.api.IAuthorization;
 
 /**
  * This class is the implementation of the enum type DB_ID in {@link IAuthorization}.
  *
  */
 @Component
 public class DBIdAuthorization implements IAuthorization {
 
 	private HashMap<String, Boolean> userAccess;
 	
 	public DBIdAuthorization() {
 		userAccess = new HashMap<String, Boolean>();
 		//testdata
 		userAccess.put("test", true);
 		userAccess.put("hei", false);
 	}
 	
 	@Override
 	public Type getType() {
 		return IAuthorization.Type.DB_ID;
 	}
 
 	@Override
 	public boolean authorize(AuthorizationToken token) {
		if (token.getType().equals(getType().toString())) {
 			if (userExists(token.getId()) && userAccess.get(token.getId())) {
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
 		if (userAccess.get(id) != null) {
 			return true;
 		}
 		System.out.println("User "+id+" does not exist.");
 		return false;
 	}
 }
