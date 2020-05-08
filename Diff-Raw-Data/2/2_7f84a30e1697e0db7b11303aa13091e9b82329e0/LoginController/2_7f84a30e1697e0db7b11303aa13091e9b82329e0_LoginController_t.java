 package ca.awesome;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.sql.*;
 
 
 public class LoginController {
 	public static String USERNAME_FIELD = "USERNAME";
 	public static String PASSWORD_FIELD = "PASSWORD";
 	public static String PASSWORD_CONF_FIELD = "PASSWORD_CONF";
 	public static String FIRST_NAME = "FIRSTNAME";
 	public static String LAST_NAME = "LASTNAME";
 	public static String ADDRESS = "ADDRESS";
 	public static String EMAIL = "EMAIL";
 	public static String PHONE = "PHONE";
 
 	// view-accessible fields
 	public User user = null;
 	public String error = null;
 	
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
		return this.user != null;
 	}
 		
 	public boolean attemptLogin() {
 		String userName = request.getParameter(USERNAME_FIELD).trim();
 		String password = request.getParameter(PASSWORD_FIELD).trim();
 
 		DatabaseConnection connection = getDatabaseConnection(context);
 		User foundUser = User.findUserByName(userName, connection);
 
 		if (foundUser != null && foundUser.getPassword().equals(password)) {
 			user = foundUser;
 			user.loadPersonalInfo(getDatabaseConnection(context));
 			session.setAttribute("user", user);
 			return true;
 		}
 
 		return false;
 	}
 
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
 
 	public boolean attemptUpdateInfo() {
 		String firstName = request.getParameter(FIRST_NAME).trim();
 		String lastName = request.getParameter(LAST_NAME).trim();
 		String address = request.getParameter(ADDRESS).trim();
 		String email = request.getParameter(EMAIL).trim();
 		String phone = request.getParameter(PHONE).trim();
 
 		boolean valid = true;
 		// do validation
 		valid &= validateStringLength(firstName, "first name", 24);
 		valid &= validateStringLength(lastName, "last name", 24);
 		valid &= validateStringLength(address, "address", 128);
 		valid &= validateStringLength(email, "email", 128);
 		valid &= validateStringLength(phone, "phone", 10);
 
 		if (valid == false) {
 			return false;
 		}
 
 		return user.updatePersonalInfo(firstName, lastName, address, email, phone,
 			getDatabaseConnection(context));
 	}
 
 	public boolean hasError() {
 		return error != null;
 	}
 
 	public boolean requestIsPost() {
 		return "POST".equalsIgnoreCase(request.getMethod());
 	}
 
 	private boolean validateStringLength(String input, String name, int len) {
 		if (input.length() > len) {
 			if (error == null) {
 				error = "";
 			}
 
 			error += name + " cannot exceed " + len + " characters.";
 			return false;
 		}
 		return true;
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
