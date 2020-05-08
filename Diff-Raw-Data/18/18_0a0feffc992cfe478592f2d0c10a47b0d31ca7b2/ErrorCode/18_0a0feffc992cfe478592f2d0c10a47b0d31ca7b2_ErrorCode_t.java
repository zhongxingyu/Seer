 package ru.goodroads.data;
 
 // XXX: this class not fully needs
 public enum ErrorCode {
 	
 	OK(0, "Success"),
 	
 	AUTH_EXPECTED_PARAM(60, "Login and/or secret not setted"),
 	AUTH_FAILED(61, "Login/secret pair failed"),
 	AUTH_NOT_FOUND(62, "Login not found"),
 	
 	REGISTER_EXPECTED_PARAM(71, "Login and/or secret and/or email not setted"),
 	REGISTER_USER_EXISTS(72, "User with its login exists"),
 	
 	// XXX: Client side error code. Request successful, 
 	// but returned data semantic is not valid
 	CLIENT_SIDE_ERROR(0xaabbccdd, "Client side error");
 
 	private final int code;
 	private final String description;
 
 	private ErrorCode(int code, String description) {
 		this.code = code;
 		this.description = description;
 	}

 	public int getCode() {
 		return code;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 	
 	public static ErrorCode fromCode(int code) {
 		for (ErrorCode ec : ErrorCode.values()) {
 	        if (code == ec.code) {
 	          return ec;
 	        }
 		}
 		return null;
 	}
 	
 	@Override
 	public String toString() {
 		return code + ": " + description;
 	}

	public String getLocalizedDescription() {
		return "Как дела?";
	}
 }
