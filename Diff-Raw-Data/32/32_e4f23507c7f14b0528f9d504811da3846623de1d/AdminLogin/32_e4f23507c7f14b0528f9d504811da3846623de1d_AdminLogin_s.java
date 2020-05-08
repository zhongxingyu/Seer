 package what;
 
 import controllers.Localize;
 import play.data.validation.Constraints.Required;
 /**
  * Class to help with login form and validation
  * @author Lukas Ehnle
  *
  */
 public class AdminLogin {
 	@Required
 	public String username;
 	
 	@Required
 	public String password;
 	
 	/**
 	 * method to check if a login was correct
	 * @return returns null or an arror message
 	 */
 	public String validate() {
 		if (username.equals("test") && password.equals("secret")) return null;
 		return Localize.get("admin.wrongLogin");
 	}
 }
