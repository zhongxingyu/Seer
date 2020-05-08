 package newsrack.web;
 
 import java.util.Map;
 
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import newsrack.GlobalConstants;
 import newsrack.user.User;
 
 /**
  * class <code>UserAction</code> supports various user-screen specific tasks
  */
 public class UserAction extends BaseAction
 {
    private static final Log _log = LogFactory.getLog(UserAction.class); /* Logger for this action class */
 
 	public String changePassword()
 	{
 		User u = getSessionUser();
 
 		try {
 			u.changePassword(getParam("oldPassword"), getParam("newPassword"));
 			return Action.SUCCESS;
 		}
 		catch (InvalidPasswordException e) {
 			addActionError(getText("error.invalid.password"));
 			return Action.ERROR;
 		}
 		catch (Exception e) {
 			addActionError(getText("internal.app.error"));
 			_log.error("Exception changing password!", e);
 			return "internal.app.error";
 		}
 	}
 
 	public String logout()
 	{
 			// The ClearSessionInterceptor clears the session, so, nothing else to do here!
 		_session.remove(GlobalConstants.USER_KEY);
		return "home";
 	}
 }
