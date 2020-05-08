 package edu.colorado.csci3308.inventory.servlet;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.colorado.csci3308.inventory.Server;
 import edu.colorado.csci3308.inventory.ServerDB;
 
 /**
  * Servlet implementation class MoveServerServlet
  */
 @WebServlet("/MoveServerServlet")
 public class MoveServerServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ServerDB.setPath(request.getServletContext().getRealPath("WEB-INF/db/servers.db"));
 		Server s = ServerDB.getServerById(Integer.valueOf(request.getParameter("BoxID")));
 		s.setLocationId(Integer.valueOf(request.getParameter("Location_id")));
		s.setRackId(Integer.valueOf(request.getParameter("Rack_id")));
 		
 		ServerDB.updateServer(s);
 		response.sendRedirect("ServerList.jsp");
 	}
 
 }
