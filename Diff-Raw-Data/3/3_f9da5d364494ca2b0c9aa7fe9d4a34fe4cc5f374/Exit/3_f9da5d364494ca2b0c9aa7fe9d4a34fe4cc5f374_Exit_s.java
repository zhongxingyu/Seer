 package swsec;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class Exit extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     public Exit() {
         super();
     }
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(false);
		session.invalidate();
 		response.sendRedirect("index.jsp");
 	}
 
 }
