 package edu.colorado.csci3308.inventory;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class GoToBoxServlet
  */
 @WebServlet("/GoToBoxServlet")
 public class GoToBoxServlet extends HttpServlet implements Servlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//This should take the box number from the BoxList page and send it as info to the Box.jsp page
 		ServerDB.setPath(request.getServletContext().getRealPath("WEB-INF/db/servers.db")); //get path to the database
 		String forward="Box.jsp"; //This is the page that we are going to go to.
 		
		Object i = request.getSession(true).getAttribute("BoxID");
 		if(i != null){
			Server s = ServerDB.getServerById((Integer)i);
 			if(s!= null)
 				request.getSession().setAttribute("BoxID", s.getServerId());
 			else
				request.getSession().setAttribute("BoxID", ((Integer)i).intValue());
 		}else{
 				request.getSession().setAttribute("BoxID", -1);
 		}	
 					
 		RequestDispatcher view = request.getRequestDispatcher(forward); //Dispatches to new JSP
 		//sets the BoxID field in the Box JSP
 		view.forward(request, response);
 	}
 
 }
