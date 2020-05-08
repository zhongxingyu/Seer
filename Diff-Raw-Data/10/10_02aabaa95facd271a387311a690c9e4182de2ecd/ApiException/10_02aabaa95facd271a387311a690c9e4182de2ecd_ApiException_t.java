 package fr.utc.assos.payutc.api;
 
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class ApiException extends Exception {
 	/**
 	 * Je sais pas c'est quoi, eclipse l'a généré tout seul
 	 */
 	private static final long serialVersionUID = -7241957979809837995L;
 	public String type;
 	public String code;
 	public String message;
 	public static final String DEFAULT_MSG	= "No msg. available.";
 
 	public ApiException() {
 		type = "UnitializedException";
 		code = "42";
 		message = DEFAULT_MSG;
 	}
 	
 	ApiException(String err_type, String err_code, String err_msg) {
 		type = err_type;
 		if (type == null || type.equals("")) {
 			type = "UntypedError";
 		}
 		code=err_code;
 		if (err_msg==null) err_msg = DEFAULT_MSG;
 		message=err_msg;
 	}
 	
 	@Override
 	public String getMessage() {
		if(!message.isEmpty()) {
			return message;
		}
		
		String msg = "Erreur " + type;
		if(!code.equals("0")) {
			msg += " (" + code + ")";
		}
		return msg;
 	}
 }
