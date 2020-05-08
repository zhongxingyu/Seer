 package controller;
 
 /**
 * @author Siddhesh Jaiswal
 * @author Indrajit Kulkarni
 */
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import repository.ProjectDAO;
 
 /*
  * This servlet is used to change the status of the project that is either enable or disable it depending whathas been selected
  */
 @WebServlet(name= "ChangeProjectStatus", urlPatterns = { "/ChangeProjectStatus"} )
 public class ChangeProjectStatus extends HttpServlet{
 
 protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//
 		try {
 			processRequest(request, response);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	
	System.out.println("Enters changeProjectStaus servlet");	
 	try {
 		processRequest(request, response);
 	} catch (SQLException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 }
 
 private void processRequest(HttpServletRequest request,
 		HttpServletResponse response) throws SQLException, ServletException, IOException {
 	
 	
 	/*
 	 * doPOst
 	 * Check if the value of statusChangeTo by using request.getParam\
 	 * enable-> project.setStatus("enable");
 	 * else disable
 	 */
 	
 	Boolean projectstatus = Boolean.valueOf(request.getParameter("statusChangeTo"));
	System.out.println(request.getParameter("disableprojid"));
	int projectId = Integer.parseInt(request.getParameter("disableprojid"));
 	
 	
 	// if projectstatus true
 	if (projectstatus){		
 		ProjectDAO.enableProject(projectId);
 	
 	// if projectstatus false
 	}else{		
 		ProjectDAO.disableProject(projectId);
 }
 	
 	RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/JSP/home.jsp");
     dispatcher.forward(request, response);
 	
 }
 
 }
