 package communication.api;
 
 import java.util.HashMap;
 import java.util.Set;
 
 /* MESSAGE FORMATS:
  * 
  * Type: ACCESS_REQ
  * To: MANAGER
  * From: AccessController
  * Fields: AUTH_TYPE, optional: TOKEN, ID, PASSCODE, VALUE
  * 
  * 
  * Type: ACCESS_RSP
  * To: sender of ACCESS_REQ (AccessController)
  * From: MANAGER
  * Fields: ACCES_RES
  * 
  * 
  * Type: OPEN (equivalent to grantAccess)
  * To: AccessPoint
  * From: MANAGER
  * Fields: none
  * 
  * 
  * Type: CLOSE (equivalent to revokeAccess)
  * To: relevant AccessPoint
  * From: MANAGER
  * Fields: none
  * 
  * 
  * Type: REGISTER
  * To: MANAGER
  * From: any component
  * Fields: COMPONENT_TYPE, COMPONENT_SUBTYPE, COMPONENT_ID, LOCATION, optional: PREFERRED_AUTH_TYPE, 
  * 			ALT_AUTH_TYPE, PREFERRED_CONTROLLER_TYPE, ALT_CONTROLLER_TYPE
  * 
  * 
  * Type: NEW_ID
  * To: sender of REGISTER
  * From: MANAGER
  * Fields: COMPONENT_ID
  * 
  * 
  * Type: ASSOCIATE
  * To: AccessPoint or AccessController
  * From: MANAGER
  * Fields: COMPONENT_SUBTYPE, COMPONENT_ID
  * 
  */
 
 /**
  * Simple message format with fields for type, to and from, in addition to various type/value data fields.
  * May be serialized and deserialized with the {@link Serializer} class.
  */
 public class Message {
 
 	private Type type;
 	private String to, from;
 	private HashMap<Field, String> data;
 	
 	// message recipient
 	public final static String MANAGER = "MANAGER";
 	
 	// message types
 	public enum Type { ACCESS_RSP, ACCESS_REQ, OPEN, CLOSE, REGISTER, ASSOCIATE, NEW_ID, };
 	
 	// message data fields
	public enum Field { ACCESS_RES, TOKEN, ID, PASSCODE, VALUE, LOCATION, AUTH_TYPE, COMPONENT_TYPE, COMPONENT_SUBTYPE, 
 							COMPONENT_ID, PREFERRED_AUTH_TYPE, ALT_AUTH_TYPE, PREFERRED_CONTROLLER_TYPE, ALT_CONTROLLER_TYPE, };
 	
 	// component types
 	public enum ComponentType { ACCESSPOINT, CONTROLLER, };
 	
 	public Message(Type type, String to, String from) {
 		this.type = type;
 		this.to = to;
 		this.from = from;
 		data = new HashMap<>();
 	}
 	
 	public void addData(Field key, String value) {
 		data.put(key, value);
 	}
 	
 	public String getData(Field key) {
 		return data.get(key);
 	}
 	
 	public Set<Field> getKeys() {
 		return data.keySet();
 	}
 	
 	public Type getType() {
 		return type;
 	}
 	
 	public String getTo(){
 		return to;
 	}
 	
 	public String getFrom(){
 		return from;
 	}
 	
 	public void setFrom(String from){
 		this.from = from;
 	}
 }
