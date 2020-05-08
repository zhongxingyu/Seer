 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 @SuppressWarnings("serial")
 public class ProjectControlServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		
 		if ( user != null) {
 			resp.sendRedirect("/LogIn.jsp");
 		} else {
 			resp.sendRedirect("/TeamsTask.jsp");
 		}
 	}
 }
