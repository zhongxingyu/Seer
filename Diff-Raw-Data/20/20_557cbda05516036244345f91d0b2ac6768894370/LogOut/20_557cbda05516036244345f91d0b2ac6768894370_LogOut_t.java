 package home;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class LogOut
  */
 public class LogOut extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// End the session
		logOut(request);
 		
 		response.getWriter().println("Your session is now ended!");
 	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// End the session
		logOut(request);
	}
	
	protected static void logOut(HttpServletRequest req){
		req.getSession().invalidate();
	}
 }
