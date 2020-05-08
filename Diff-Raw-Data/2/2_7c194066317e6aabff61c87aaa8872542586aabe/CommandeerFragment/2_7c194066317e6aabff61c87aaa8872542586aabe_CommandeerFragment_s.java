 package samurai;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreFailureException;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @SuppressWarnings("serial")
 public class CommandeerFragment extends HttpServlet {
 	private static final Logger log = Logger.getLogger(CreateFragment.class.getName());
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String user = (String) req.getSession().getAttribute("user");
 		if (user != null) {
			String fragmentName = req.getParameter("newFragment");
 			if ( Utility.notNullOrEmpty(fragmentName) ){			
 				Key fragmentKey = KeyFactory.createKey("Fragment", fragmentName);
 				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 				try {
 					Entity fragment = datastore.get(fragmentKey);
 					// If we don't get an exception, the value was successfully retrieved. We expect this to be the standard case.
 					String holder = (String)fragment.getProperty("holder");
 					if (Utility.notNullOrEmpty(holder)) {
 						// User attempted to claim the fragment, likely at the same time as someone else. Redirect and inform them that someone else ninja'd the samurai ;).
 						resp.sendRedirect("/samurai.jsp?fragment=ninja");
 					} else {
 						Date claimDate = new Date();
 						fragment.setProperty("holder", user);
 						fragment.setProperty("claimDate", claimDate);
 						datastore.put(fragment);
 					}
 				} catch (EntityNotFoundException e) {
 					// User attempted to commandeer a fragment that doesn't exist. This should never actually happen.
 					log.log(Level.INFO, user + " attempted to claim " + fragmentName + " which doesn't exist.");
 					resp.sendRedirect("/samurai.jsp?fragment=invalidclaim");
 				} catch (IllegalArgumentException e) {
 					log.log(Level.INFO, user + " attempted to claim " + fragmentName + " which is invalid.");
 					resp.sendRedirect("/samurai.jsp?fragment=invalidclaim");
 				} catch (DatastoreFailureException e) {
 					log.log(Level.INFO, "Error accessing datastore with " + user);
 					resp.sendError(500, "Error accessing datastore");
 				}
 				resp.sendRedirect("/samurai.jsp");	
 			} else {
 				log.log(Level.INFO, user + " attempted to claim null fragment");
 				resp.sendRedirect("/samurai.jsp?fragment=invalidclaim");
 			}	
 		} else {
 			// Only allow a logged in user to seize fragments.
 			resp.sendRedirect("/samurai.jsp?fragment=no");
 		}
 	}
 }
