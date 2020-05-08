 package craig_proj;
 
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class DashBoardServlet
  */
 @WebServlet("/Dashboard")
 public class DashBoardServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     public DashBoardServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doBoth(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doBoth(request, response);
 	}
 
 	private void doBoth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		
 		//response.sendRedirect("/WEB-INF/dashboard.jsp"); //HEADER isn't set using this
 		RequestDispatcher rd = request.getRequestDispatcher("WEB-INF/dashboard.jsp");
 		rd.forward(request, response);
		//session.invalidate();
 	}
 }
