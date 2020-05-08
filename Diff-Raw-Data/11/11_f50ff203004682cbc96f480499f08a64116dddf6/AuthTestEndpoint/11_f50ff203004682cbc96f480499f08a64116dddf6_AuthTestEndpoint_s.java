 package edu.gatech.oad.rocket.findmythings.server;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 
 import edu.gatech.oad.rocket.findmythings.server.util.HTTP;
 import edu.gatech.oad.rocket.findmythings.server.util.Parameters;
 
 public class AuthTestEndpoint extends BaseServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6058248835620387583L;
 
 	public AuthTestEndpoint() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		Subject subject = SecurityUtils.getSubject();
 		if (subject != null && subject.isAuthenticated()) {
 			String message = "Howdy, " + subject.getPrincipal() + "!";
 			HTTP.writeAsJSON(response, HTTP.Status.OK, Parameters.STATUS, message);
 		} else {
 			HTTP.writeAsJSON(response, HTTP.Status.OK, Parameters.STATUS, "You're not logged in.");
 		}
 	}
 
 }
