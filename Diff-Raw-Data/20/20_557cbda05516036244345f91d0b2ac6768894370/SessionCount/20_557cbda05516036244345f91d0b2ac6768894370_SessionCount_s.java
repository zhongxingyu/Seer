 package home;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import listeners.SessionCountListener;
 
 /**
  * Servlet implementation class SessionCount
  */
 public class SessionCount extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Create session to register session listener
		HttpSession session = request.getSession();
		
 		response.getWriter().println("count: " + SessionCountListener.getTotalActiveSessions());
 	}
 }
