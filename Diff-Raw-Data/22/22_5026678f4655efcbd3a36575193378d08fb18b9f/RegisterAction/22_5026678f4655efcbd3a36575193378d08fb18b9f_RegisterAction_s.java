 package newsrack.web;
 
 import java.util.Map;
 
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import newsrack.GlobalConstants;
 import newsrack.user.User;
 
 public class RegisterAction extends BaseAction
 {
    private static final Log _log = LogFactory.getLog(RegisterAction.class); /* Logger for this action class */
 
 	private void validateUserId(String uid)
 	{
 		int    n  = uid.length();
 		char[] cs = uid.toCharArray();
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 				// White space cannot be part of a valid uid!
 			if (Character.isWhitespace(c))
 				addFieldError("username", "You chose '" + uid + "' as your username.  Character '" + c + "' at position " + i + " cannot be used in a user name!");
 				// No special characters allowed!
 			if (   ((i == 0) && !Character.isUnicodeIdentifierStart(c))
 				 || ((i > 0)  && !Character.isUnicodeIdentifierPart(c) && (c != '_'))
 				)
 			{
 				addFieldError("username", "You chose '" + uid + " as your username. Character '" + c + "' at position " + i + " cannot be used in a user name!  Start with a letter and use letters, numbers, or _ elsewhere!");
 			}
 		}
 	}
 
 	public void validate()
 	{
       String uid  = getParam("username");
 		if (uid == null || uid.trim().equals(""))
 			addFieldError("username", getText("error.username.required"));
		if (!User.userIdAvailable(uid))
 			addFieldError("username", getText("error.uid.unavailable", new String[]{uid}));
 
       String pass = getParam("password");
       String pass2 = getParam("passwordConfirm");
 		if (pass == null || pass.equals(""))
 			addFieldError("password", getText("error.password.required"));
 		if (pass2 == null || pass2.equals(""))
 			addFieldError("passwordConfirm", getText("error.password.required"));
 		if (!pass.equals(pass2))
 			addFieldError("passwordConfirm", getText("error.password.mismatch"));
 
			/* This is one way to handle robot registrations */
 		String hsv = getParam("humanSumValue");
 		String hsr = getParam("humanSumResponse");
      if ((hsr == null) || !Integer.valueOf(hsv).equals(Integer.valueOf(hsr)))
         addFieldError("humansum", "If you are a human trying to register, please enter " + hsv + " in the box asking for the sum.");
 
 		String emailid = getParam("emailid");
 		if (emailid == null || emailid.trim().equals(""))
 			addFieldError("password", getText("error.emailid.required"));
 	}
 
 	public String execute()
 	{
       String name     = getParam("name");
       String username = getParam("username");
       String password = getParam("password");
       String pc       = getParam("passwordConfirm");
       String emailid  = getParam("emailid");
 
 		try {
 			User u = User.registerUser(username, password, name, emailid);
 			assert(u != null);
 			return Action.SUCCESS;
 		}
 		catch (Exception e) {
 			addActionError(getText("internal.app.error"));
 			_log.error("Exception registering user!", e);
 			return "internal.app.error";
 		}
 	}
 }
