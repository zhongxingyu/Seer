 package newsrack.web;
 
 import java.util.Map;
 
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import newsrack.GlobalConstants;
 import newsrack.user.User;
 import newsrack.util.PasswordService;
 import newsrack.util.MailUtils;
 
 public class PasswordResetAction extends BaseAction
 {
    private static final Log _log = LogFactory.getLog(PasswordResetAction.class); /* Logger for this action class */
 
 	private User _user;
 
 	public User getUser() { return _user; }
 
 	public String sendPasswordResetKey()
 	{
       String uid = getParam("uid");
 		if (uid == null) {
 			addFieldError("username", getText("error.missing.username"));
 			_log.error("User name not provided!");
 			return Action.INPUT;
 		}
 		User u = User.getUser(uid);
 		if (u == null) {
 			addFieldError("username", getText("error.invalid.uid", new String[]{uid}));
 			return Action.INPUT;
 		}
 
 		String serverUrl = GlobalConstants.getServerURL();
 		String resetKey  = PasswordService.getPasswordResetKey(uid);
 		String resetUrl  = serverUrl + "/password/reset-form?uid=" + uid + "&key=" + resetKey;
 		String message   = "Please click on the link below to reset your password for your account on " + serverUrl + ". \n\n " + resetUrl + "\n\n If you got this email in error, please send an email to " + GlobalConstants.getProperty("email.admin.emailid") + "\n"; 
 		try {
 			MailUtils.sendEmail(u.getEmail(), "Password reset request on " + serverUrl, message);
 			return Action.SUCCESS;
 		}
 		catch (javax.mail.MessagingException e) {
 			addActionError(getText("internal.app.error"));
 			_log.error("Error sending reset password email for user: " + uid, e);
 			return "internal.app.error";
 		}
 	}
 
	public String checkResetPasswordKey()
 	{
       String uid = getParam("uid");
       User u = User.getUser(uid);
 		if (u == null) {
 			addActionError(getText("error.invalid.uid", new String[]{uid}));
 			return Action.ERROR;
 		}
 
       String key = getParam("key");
 		if ((key == null) || !PasswordService.isAValidPasswordResetKey(uid, key)) {
 			addActionError(getText("error.invalid.key"));
         _session.put(GlobalConstants.USER_KEY, u);
 			return Action.ERROR;
 		}
 
 			// All is well!
 		return Action.SUCCESS;
 	}
 
 	public String resetPassword()
 	{
 		_user = getSessionUser();
 
 			// Reset the password
 		try {
			_user.resetPassword(getParam("password"));
 			PasswordService.invalidatePasswordResetKey(_user.getUid());
 			return Action.SUCCESS;
 		}
 		catch (Exception e) {
 			addActionError(getText("internal.app.error"));
 			_log.error("Exception resetting password!", e);
 			return "internal.app.error";
 		}
 	}
 }
