 package edu.colorado.csci3308.inventory.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.colorado.csci3308.inventory.Server;
 import edu.colorado.csci3308.inventory.ServerDB;
 import edu.colorado.csci3308.inventory.ServerList;
 
 /**
  * Servlet implementation class ScanServlet
  */
 @WebServlet("/ShutdownServlet")
 public class ShutdownServlet extends HttpServlet implements Servlet{
 	private static final long serialVersionUID = 1L;
 
 	
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			System.out.println("SHUTDOWN SERVLET");
			
 			response.sendRedirect("ServerList.jsp");
 	}
 
 
 }
