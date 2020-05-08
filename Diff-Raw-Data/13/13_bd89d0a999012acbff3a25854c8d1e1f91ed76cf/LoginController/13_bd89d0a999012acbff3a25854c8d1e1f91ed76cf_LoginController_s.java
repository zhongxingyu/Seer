 package ca.awesome;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.sql.*;
 
 
 public class LoginController {
 	public static String USERNAME_FIELD = "USERNAME";
 	public static String PASSWORD_FIELD = "PASSWORD";
 	public static String PASSWORD_CONF_FIELD = "PASSWORD_CONF";
 
 	// view-accessible fields
 	public User user;
 	public String error;
 	
 	private ServletContext context;
 	private HttpServletRequest request;
 	private HttpServletResponse response;
 	private HttpSession session;
 
 	public LoginController(ServletContext context, HttpServletRequest request,
 				HttpServletResponse response, HttpSession session) {
 		this.context = context;
 		this.request = request;
 		this.response = response;
 		this.session = session;
 
 		this.user = (User)session.getAttribute("user");
 		this.error = null;
 	}
 
 	public boolean userIsLoggedIn() {
 		return this.user == null;
 	}
 		
 	public boolean attemptLogin() {
 		String userName = request.getParameter(USERNAME_FIELD).trim();
 		String password = request.getParameter(PASSWORD_FIELD).trim();
 
 		DatabaseConnection connection = getDatabaseConnection(context);
		user = User.findUserByName(userName, connection);
		if (user == null) {
			return false;
 		}
 
		session.setAttribute("user", user);
		return user.getPassword().equals(password);
 	}
 
 	public void requireLogin() {
 		if (user == null) {
 			try {
 				response.sendRedirect("login.jsp");
 			} catch (IOException exception) {
 				throw new RuntimeException("failed to redirect to login.jsp",
 							exception);
 			}
 		}
 	}
 
 	public void logout() {
 		session.setAttribute("user", null);
 		user = null;
 	}
 
 	public boolean attemptChangePassword() {
 		String password = request.getParameter(PASSWORD_FIELD).trim();
 		String passwordConf = request.getParameter(PASSWORD_CONF_FIELD).trim();
 
 		if (!password.equals(passwordConf)) {
 			error = "password fields did not match";
 			return false;
 		}
 
 		DatabaseConnection connection = getDatabaseConnection(context);
 		User.updateUserPassword(user, password, connection);
 		return true;
 	}
 
 	public boolean hasError() {
 		return error != null;
 	}
 
 	public boolean requestIsPost() {
 		return "POST".equalsIgnoreCase(request.getMethod());
 	}
 
 	private DatabaseConnection getDatabaseConnection(ServletContext context) {
 		DatabaseConnection connection = new DatabaseConnection();
 		if (!connection.connect(context)) {
 			connection.close();
 			throw new RuntimeException("Failed to connect to database");
 		}
 		return connection;
 	}
 };
