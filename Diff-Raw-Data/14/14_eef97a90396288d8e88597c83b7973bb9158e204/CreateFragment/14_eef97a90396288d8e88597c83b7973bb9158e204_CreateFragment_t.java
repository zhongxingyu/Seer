 package samurai;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

 @SuppressWarnings("serial")
 public class CreateFragment extends HttpServlet {
 	private static final Logger log = Logger.getLogger(CreateFragment.class.getName());
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String user = (String) req.getSession().getAttribute("user");
 		if (user != null) {
 			String fragmentName = req.getParameter("newFragment");
 			if ( Utility.notNullOrEmpty(fragmentName) ){			
 				
 				Entity fragment = new Entity("Fragment");
 				fragment.setProperty("fragmentName", fragmentName);
 				
 				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 				datastore.put(fragment);
 				
 				resp.sendRedirect("/samurai.jsp");	
 			} else {
 				log.log(Level.INFO, "Attempt at creating null fragment");
 				resp.sendError(400, "Invalid fragment name supplied");
 			}	
 		} else {
 			// Only allow a logged in user to create new fragments.
 			resp.sendRedirect("/samurai.jsp?fragment=no");
 		}
 	}
 }
