 package model;
 
 public enum StatusCodes {
 	OK_CONFIGURER_READY(0, "Configurer ready"),
 	OK_COMMANDS_LISTED(0, "Commands listed"),
 	OK_LOGGED_OUT(0, "Logged out successfully"), 
 	OK_FILE_UPDATED(0, "File successfully updated"),
 	OK_PASSWORD_ACCEPTED(0, "Password accepted"),
 	OK_GOOD_BYE(0, "Good bye!"),
 	OK_FILE_PRINTED(0, "File printed"),
 	OK_STATISTICS_READY(0, "Statistics service ready"),
 	OK_SERVER_STATS_DISPLAYED(0, "Server stats displayed"),
 	OK_USER_STATS_DISPLAYED(0, "User stats displayed"),
 	OK_ALL_USER_STATS_DISPLAYED(0, "All users stats displayed"),
 	OK_SUCCESS(0, "Executed Succesfuly"),
 	
 	ERR_UNRECOGNIZED_COMMAND(100, "Unrecognized command"),
 	ERR_INVALID_PARAMETERS(105, "Invalid parameters"),
 	ERR_INVALID_PARAMETERS_ARGUMENTS(101, "Invalid parameters: missing arguments"),
 	ERR_INVALID_PARAMETERS_FILE(102, "Invalid parameters: file does not exists"),
 	ERR_INVALID_PARAMETERS_USER(103, "Invalid parameters: unrecognized user"),
 	ERR_INVALID_PARAMETERS_NUMBER(104, "Invalid parameters: not a number"),
 	ERR_TOO_MANY_ATTEMPTS(110, "Too many unsuccessful attempts. Bye!"),
 	ERR_INVALID_PASSWORD(111, "Invalid password"),
 	ERR_INTERNAL_SERVER_ERROR(666, "Internal server error!");
 	 
    public final int code;
    public final String message;
  
    StatusCodes(int code, String message) {
       this.code = code;
       this.message = message;
    }
 
    public int getCode() {
 	   return code;
    }
  
    public String getMessage() {
       return message;
    }
 	   
 }
