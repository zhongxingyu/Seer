 package controller;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /*
  * @author Aishwarya Srinivasan
 */
 
 /*
  * Information class that contains all the features of ManagingProjects
  */
 
 public class ManageProject extends HttpServlet {
 	
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 	//Renders manageProject.jsp
 	RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/JSP/manageProject.jsp");
 	//dispatcher.forward(request, response);
 	dispatcher.forward(request, response);
 	}
 	
 
 }
