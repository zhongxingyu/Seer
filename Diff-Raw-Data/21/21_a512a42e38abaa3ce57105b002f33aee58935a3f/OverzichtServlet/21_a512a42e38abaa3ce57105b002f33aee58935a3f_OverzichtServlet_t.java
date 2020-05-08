 package view;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
import model.Student;

 import control.*;
 
 @SuppressWarnings("serial")
 public class OverzichtServlet extends HttpServlet
 {
 
	private ArrayList<Student> studenten;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
		System.out.println(studenten);
 		studenten = Controller.getAllStudenten();
 		try {
			System.out.println(studenten);
 			resp.sendRedirect("overzicht.jsp");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
