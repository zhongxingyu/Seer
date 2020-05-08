 package gov.usgs.cida.watersmart.ldap;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public enum LoginMessage {
     BAD_PASS(0, "Incorrect username or password"),
     BAD_GROUP(1, "User is authorized to view this page"),
     NOT_HTTPS(2, "You have been redirected to the secure login page"),
    LOGOUT(3, "You have succesfully logged out"),
     ERROR(-1, "How did you get here?");
     
     int code;
     String msg;
     
     LoginMessage(int code, String msg) {
         this.code = code;
         this.msg = msg;
     }
     
     public int getCode() {
         return code;
     }
     
     public static String getMessage(int code) {
         switch (code) {
             case 0: return BAD_PASS.msg;
             case 1: return BAD_GROUP.msg;
             case 2: return NOT_HTTPS.msg;
             case 3: return LOGOUT.msg;
             default: return ERROR.msg;
         }
     }
     
     @Override
     public String toString() {
         return "" + this.code;
     }
 }
