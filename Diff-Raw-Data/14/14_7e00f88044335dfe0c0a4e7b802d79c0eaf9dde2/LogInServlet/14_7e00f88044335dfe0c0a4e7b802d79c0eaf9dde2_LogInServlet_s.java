 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class LogInServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
		
		
		resp.setContentType("text/plain");		
		resp.getWriter().println("You have been logged in");
 	}
 
 }
