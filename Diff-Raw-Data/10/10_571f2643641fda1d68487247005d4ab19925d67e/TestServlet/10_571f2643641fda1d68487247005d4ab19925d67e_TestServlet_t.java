 package se.chalmers.chessfeudserver;
 
 import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 /**
  * Servlet implementation class TestServlet
  */
 @WebServlet("/TestServlet")
 public class TestServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Default constructor. 
      */
     public TestServlet() {
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/xml");
 		final ServletOutputStream out = response.getOutputStream();
 		try {
 			final String userName = (String) request.getParameter("userName");
 			out.println("<login><status>SUCSESS</status></login>");
 		} catch(Exception e) {
 			out.println("<login><status>ERROR</login></status>");
 		}
 		out.flush();
 		out.close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
		this.doGet(request, response);
 
 	}
 
 }
