 package api;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 
 import deadlineserver.Utils;
 import deadlineserver.models.DUser;
 import deadlineserver.models.Subscription;
 
 @SuppressWarnings("serial")
 public class RemoveSubscription extends HttpServlet {
 
 	private static final Logger log = Logger.getLogger(RemoveSubscription.class.getName());
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		Utils.registerObjectifyClasses();
 		Objectify ofy = ObjectifyService.begin();
 		log.setLevel(Level.ALL);
 		
 		//Only for mobile authentication
 		String regId = (String)req.getParameter("regId");
 		String subscriptionId= (String)req.getParameter("id");
 		
 		UserService userService = UserServiceFactory.getUserService();
		DUser oldUser;
 		
 		resp.setContentType("application/json");
 		
 		if(regId==null)
 		{
 			User user = userService.getCurrentUser();
 
 		    if(user==null) {
 		    	resp.getWriter().println("{\"success\":false, \"message\":\"You " + 
 		    "need to be logged-in to modify subscriptions.\"}");
 		    	return;
 		    }
		    
		    oldUser = ofy.query(DUser.class).filter("userId",user.getEmail()).get();
 		    
 		    if(oldUser==null) {
 		    	resp.getWriter().println("{\"success\":false, \"message\":\"Internal" + 
 		    " error - user not found in database.\"}");
 		    	return;
 		    }
 		}
 		else
 		{
 			oldUser = ofy.query(DUser.class).filter("regId",regId).get();
 			if(oldUser==null) {
 		    	resp.getWriter().println("{\"success\":false, \"message\":\"regId not" + 
 			" found in database.\"}");
 		    	return;
 		    }
 		}
 
 		Subscription oldSub = null;
 		Key<Subscription> oldSubKey = new Key<Subscription>(Subscription.class, subscriptionId);
 		if(subscriptionId!=null)
 		{
 			try
 			{
 				oldSub = ofy.get(oldSubKey);
 			}
 			catch(Exception e)
 			{
 				//TODO: Add log
 			}
 		}
 
 		if(oldSub==null)
 		{
 			resp.getWriter().println("{\"success\":false, \"message\":\"Subscription " + 
 		"not found in database\"}");
 			log.info("Subscription " + subscriptionId + " does not exist");
 			return;
 		}
 		
 		if(oldUser.subscriptions.contains(oldSubKey))
 		{
 			int index = oldUser.subscriptions.indexOf(oldSubKey);
 
 			oldUser.subscriptions.remove(index);
 			ofy.put(oldUser);
 			oldSub.numSubscribers = oldSub.numSubscribers - 1;
     		ofy.put(oldSub);
 
 			resp.getWriter().println("{\"success\":true, \"message\":\"Unsubscribed " + 
 		"successfully.\"}");
 		    return;
 		}
 		else
 		{
 			resp.getWriter().println("{\"success\":false, \"message\":\"You are not  " + 
 					"subscribed to this course.\"}");
 		    return;
 		}
 	}
 }
