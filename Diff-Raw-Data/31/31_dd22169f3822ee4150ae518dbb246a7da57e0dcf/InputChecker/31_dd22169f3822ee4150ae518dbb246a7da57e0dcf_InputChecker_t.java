 package controllers.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * InputChecker is used to check different kinds of inputstrings
  * like E-Mail or Name. You can simply add a new checker by adding a method public
  * boolean myChecker(String input){...}
  * @author Thomas Mortier
  */
 public class InputChecker {
 
     private static InputChecker _instance = null;
 
     /* below you can define patterns */
     //BEGIN
     private static final String EMAIL_PATTERN =
         "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
         + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
     //END
     private InputChecker(){
         /* default */
     }
 
     /**
      * @return instance of InputChecker
      */
     public static InputChecker getInstance() {
         if(_instance==null)
             _instance = new InputChecker();
         return _instance;
     }
 
     /**
      * Checks if a given string matches the general email string
      * 
     * @param String email
      * @return true if correct email
      */
     public boolean isCorrectEmail(String email) {
         Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
         Matcher matcher = pattern.matcher(email);
         return matcher.find();
     }
 }
