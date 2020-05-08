 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class TeamsServlet extends HttpServlet{
 
 	private static final long serialVersionUID = -2505608701798341438L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService service = UserServiceFactory.getUserService();
 		User user = service.getCurrentUser();
 		if(user!= null){
 			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 			
 		}
 		else{
 			resp.sendRedirect("/projectcontrol");
 		}
 		
 	}
 
 
 
 }
