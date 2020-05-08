 package ee.itcollege.i377.praktikum1;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class CountVisit
  */
 @WebServlet("/count")
 public class CountVisit extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String SESSION_VISITS = "visits";
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession sess = request.getSession();
 		if (sess.isNew()) {
 			sess.setAttribute(SESSION_VISITS, 1);
 		}
 		else {
 			Integer visits = (Integer) sess.getAttribute(SESSION_VISITS);
 			visits += 1;
 			sess.setAttribute(SESSION_VISITS, visits);
 		}
 		
 		PrintWriter out = response.getWriter();
 		Integer visits = (Integer) request.getSession().getAttribute(SESSION_VISITS);
 		out.append("Külastuste arv: " + visits);
 	}
 
 }
