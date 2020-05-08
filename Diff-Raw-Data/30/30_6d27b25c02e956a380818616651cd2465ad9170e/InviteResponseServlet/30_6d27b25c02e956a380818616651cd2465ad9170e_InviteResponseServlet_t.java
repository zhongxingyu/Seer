 package com.foodcirclesserver.circles;
 
import java.io.IOException;

 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.foodcirclesserver.user.UserManager;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class InviteResponseServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 2101971148915359984L;
 
 	//post?
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String keyIDString = req.getParameter("key");
 		Long keyID = Long.parseLong(keyIDString);
 
 		String acceptedString = req.getParameter("accepted");
 		Boolean accepted = Boolean.parseBoolean(acceptedString);
 
		String tokenHash = (req.getParameter(UserManager.TOKEN_HASH));

		if (keyID == null || accepted == null || tokenHash == null)
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
 
 		Key key = KeyFactory.createKey(InviteManager.TYPE, keyID);
 
 		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 		Entity invite;
 		try {
 			invite = ds.get(key);
 			String receiverID = (String) invite.getProperty(InviteManager.RECEIVER_ID);
 
 			if (UserManager.validateUser(tokenHash, receiverID, ds)) {
 
 				//mark received so user does not see again
 				InviteManager.markReceived(keyID, ds);
 
 				if (accepted) {
 					Long circleID = (Long) invite.getProperty(InviteManager.CIRCLE_ID);
 					CircleManager.addUserToCircle(receiverID, circleID, ds);
 
 					//TODO: notification for sender that friend accepted?
 				}

				resp.setStatus(HttpServletResponse.SC_ACCEPTED);
			} else {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
 			}
 		} catch (EntityNotFoundException e) {
 			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 	}
 
 }
