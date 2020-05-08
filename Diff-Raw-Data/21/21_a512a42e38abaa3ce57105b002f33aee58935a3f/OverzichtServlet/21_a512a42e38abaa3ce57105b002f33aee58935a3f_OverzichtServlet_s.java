 package view;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import control.*;
 
 @SuppressWarnings("serial")
 public class OverzichtServlet extends HttpServlet
 {
	private Object studenten;
 
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		studenten = Controller.getAllStudenten();
 		try {
 			resp.sendRedirect("overzicht.jsp");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
