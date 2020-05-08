 package newsrack.web;
 
 import java.util.Map;
 
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import newsrack.GlobalConstants;
 import newsrack.user.User;
 
 /**
  * class <code>AdminAction</code> presents an admin screen!
  */
 public class AdminAction extends BaseAction
 {
    private static final Log log = LogFactory.getLog(AdminAction.class); /* Logger for this action class */
 
    private User getAdmin()
    {
 		if (_user != null && !_user.isAdmin()) { // Check if this is indeed the admin
 			addActionError(getText("error.not.admin"));
 			log.error("User " + _user.getUid() + " not an admin!");
 			return null;
 		}
 
 		return _user;
 	}
 
 	public String loginAsAnotherUser()
 	{
 		User u = getAdmin();
 		if (u == null)
 			return Action.ERROR;
 
 		String username = getParam("username");
 		if (username == null) {
 			addActionError(getText("error.username.required"));
 			log.error("User name not provided!");
 			return Action.INPUT;
 		}
 
 		try {
 				// Save our logged-in user in the session,
 				// because we use it again later.
 			log.info("ADMIN: got username - " + username);
 			u = u.signInAsUser(username);
 			log.info("ADMIN: Signed in as user " + u.getUid());
 
 				// Record the new user info in the action class
 			_session.put(GlobalConstants.UID_KEY, u.getUid());
 			_session.put(GlobalConstants.USER_KEY, u);
 			_user = u;
 			return Action.SUCCESS;
 		}
 		catch (final Exception e) {
 			addActionError(getText("error.invalid.uid", new String[]{username}));
 			log.error("Bad username?", e);
 			return Action.INPUT;
 		}
 	}
 
 	public String makeReadOnly()
 	{
 		GlobalConstants.setProperty("readonly", "true");
 		GlobalConstants.setProperty("testing", "true");
 		return Action.SUCCESS;
 	}
 
 	public String makeReadWrite()
 	{
 		GlobalConstants.setProperty("readonly", "false");
 		GlobalConstants.setProperty("testing", "false");
 		return Action.SUCCESS;
 	}
 
 	public String clearCache()
 	{
 		GlobalConstants.getDBInterface().clearCache();
 		return Action.SUCCESS;
 	}
 
 	public String refreshCachingRules()
 	{
 		User u = getAdmin();
 		if (u == null)
 			return Action.ERROR;
 
 		newsrack.archiver.Feed.refreshCachedTextDisplayRules();
 		return Action.SUCCESS;
 	}
 
 	public String refreshGlobalProperties()
 	{
 		User u = getAdmin();
 		if (u == null)
 			return Action.ERROR;
 
       GlobalConstants.loadGlobalProperties();
 		return Action.SUCCESS;
 	}
 }
