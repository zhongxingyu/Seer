 package ca.awesome;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.sql.*;
 
 
 public class Controller {
 	// view-accessible fields
 	public User user = null;
 	public String error = null;
 	
 	protected ServletContext context;
 	protected HttpServletRequest request;
 	protected HttpServletResponse response;
 	protected HttpSession session;
 
 	public Controller(ServletContext context, HttpServletRequest request,
 				HttpServletResponse response, HttpSession session) {
 		this.context = context;
 		this.request = request;
 		this.response = response;
 		this.session = session;
 
 		this.user = (User)session.getAttribute("user");
 		this.error = null;
 
 		DatabaseConnection.initialize(context);
 	}
 
 	public boolean userIsLoggedIn() {
 		return this.user != null;
 	}
 		
 	/*
 	 * Checks whether a users is logged in,
 	 * if not, redirects to login page.
 	 * returns true if the user is logged in.
 	 */
 	public boolean requireLogin() {
 		if (user == null) {
 			try {
 				response.sendRedirect("login.jsp");
 			} catch (IOException exception) {
 				throw new RuntimeException("failed to redirect to login.jsp",
 							exception);
 			}
 			return false;
 		}
 		return true;
 	}
 
 	public boolean hasError() {
 		return error != null;
 	}
 
 	public boolean requestIsPost() {
 		return "POST".equalsIgnoreCase(request.getMethod());
 	}
 
 	protected boolean validateStringLength(String input, String name, int len) {
 		if (input.length() > len) {
 			if (error == null) {
 				error = "";
 			}
 
 			error += name + " cannot exceed " + len + " characters.";
 			return false;
 		}
 		return true;
 	}
 
 	protected DatabaseConnection getDatabaseConnection(ServletContext context) {
 		DatabaseConnection connection = new DatabaseConnection();
 		if (!connection.connect()) {
 			connection.close();
 			throw new RuntimeException("Failed to connect to database");
 		}
 		return connection;
 	}
 }
