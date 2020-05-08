 package authorization.impl.dbpasscode;
 
 import java.util.HashMap;
 
 import componenttypes.api.ComponentTypes;
 import aQute.bnd.annotation.component.Component;
 import authorization.api.AuthorizationToken;
 import authorization.api.IAuthorization;
 
 /**
  * This class is the implementation of the enum type DB_PASSCODE in {@link IAuthorization}.
  *
  */
 @Component
 public class DBPasscodeAuthorization implements IAuthorization {
 
 	private HashMap<String, String> passcodeAssociations; // controller, passcode
 	
	public DBPasscodeAuthorization() {
		this.passcodeAssociations = new HashMap<String, String>();
	}
	
 	@Override
 	public ComponentTypes.Authorization getType() {
 		return ComponentTypes.Authorization.DB_PASSCODE;
 	}
 
 	@Override
 	public boolean authorize(AuthorizationToken token) {
 		if (token.getType().equals(getType())) {
 			if (token.getValue().equals(passcodeAssociations.get(token.getController()))) {
 				return true;			
 			}
 			System.out.println("Access denied.");
 		}
 		else {
 			System.out.println("Invalid AuthorizationToken.");
 		}
 		return false;
 	}
 
 	@Override
 	public void addAuthorizedValue(AuthorizationToken token) {
 		passcodeAssociations.put(token.getController(), token.getValue());
 		
 	}
 
 	@Override
 	public void removeAuthorizedValue(AuthorizationToken token) {
 		passcodeAssociations.remove(token.getController());
 	}
 }
