 package authorization.api;
 
 /**
  * Container for data used by an authorization service
  *
  */
 public class AuthorizationToken {
 		
 	private String controller;
 	private IAuthorization.Type type;
 	private String id;
 	private String passcode;
 	private String value;
 
 	public AuthorizationToken(String controller, IAuthorization.Type type) {
 		this.controller = controller;
 		this.type = type;
 	}
 	
 	public AuthorizationToken(String controller, IAuthorization.Type type, String value) {
 		this.controller = controller;
 		this.type = type;
 		this.value = value;
 	}
 	
 	public AuthorizationToken(String controller, IAuthorization.Type type, String id, String passcode) {
 		this.controller = controller;
 		this.type = type;
 		this.id = id;
 		this.passcode = passcode;
 	}
 
 	public String getController() {
 		return this.controller;
 	}
 	
 	public IAuthorization.Type getType() {
 		return this.type;
 	}
 	
 	public String getId() {
 		return this.id;
 	}
 	
 	public String getPasscode() {
 		return this.passcode;
 	}
 	
 	public String getValue() {
 		return this.value;
 	}
 	
 	public static AuthorizationToken generateToken(String controller, String type, String id, String passcode) {
 		if (type.equals(IAuthorization.Type.NONE_FALSE.toString())) {
 			return new AuthorizationToken(controller, IAuthorization.Type.NONE_FALSE);
 		}
 		else if (type.equals(IAuthorization.Type.NONE_TRUE.toString())) {
 			return new AuthorizationToken(controller, IAuthorization.Type.NONE_TRUE);
 		}
 		else if (type.equals(IAuthorization.Type.TIMED.toString())) {
 			return new AuthorizationToken(controller, IAuthorization.Type.TIMED);
 		}
 		else if (type.equals(IAuthorization.Type.DB_PASSCODE.toString())) {
 			return new AuthorizationToken(controller, IAuthorization.Type.DB_PASSCODE, passcode);
 		}
 		else if (type.equals(IAuthorization.Type.DB_ID.toString())) {
 			return new AuthorizationToken(controller, IAuthorization.Type.DB_ID, id);
 		}
 		else if (type.equals(IAuthorization.Type.DB_USERNAME_PASSWORD.toString())) {
 			return new AuthorizationToken(controller, IAuthorization.Type.DB_USERNAME_PASSWORD, id, passcode);
 		}
 		return null;
 	}
 }
