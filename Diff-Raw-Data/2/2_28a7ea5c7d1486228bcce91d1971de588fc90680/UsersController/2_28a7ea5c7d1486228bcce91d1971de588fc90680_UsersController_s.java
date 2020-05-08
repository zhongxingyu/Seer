 package com.kittens.controller;
 
 import com.google.common.base.Strings;
 
 import com.kittens.database.Dataset;
 import com.kittens.database.User;
 import com.kittens.Utils;
 
 import java.io.IOException;
 import java.lang.String;
 import java.sql.SQLException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.ServletException;
 
 public class UsersController extends BaseController {
 
 	// the version of this object
 	public static final long serialVersionUID = 0L;
 
 	/**
 	 * Attaches the given message to the
 	 * session and redirects the user to
 	 * the application root (the login form).
 	 */
 	private void setErrorAndRedirect(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
 		request.getSession().setAttribute(Utils.ErrorCode.ERROR_MSG, message);
 		response.sendRedirect(Utils.APP_ROOT);
 	}
 	/**
 	 * Try to create a user from the parameters given.
 	 */
 	private User getUserFromRequest(HttpServletRequest request) {
 		String email    = request.getParameter("email");
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		if (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
 			// error
 			return null;
 		}
		return new User(username, email, password, /* is admin */ false);
 	}
 	/**
 	 * Returns the appropriate user from the given credentials.
 	 */
 	private User getUserFromRequestViaCredentials(HttpServletRequest request) throws SQLException {
 		String email    = request.getParameter("email");
 		String password = request.getParameter("password");
 		if (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(password)) {
 			return null;
 		}
 		return database.getUserWithCredentials(email, password);
 	}
 	/**
 	 * Register the user.
 	 */
 	private void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		final User user = getUserFromRequest(request);
 		if (user == null) {
 			// the form was not filled out properly
 			setErrorAndRedirect(request, response, Utils.ErrorCode.COMPLETE_FORM);
 			return;
 		}
 		try {
 			if (database.emailInDatabase(user.getEmail())) {
 				setErrorAndRedirect(request, response, Utils.ErrorCode.EMAIL_IN_USE);
 				return;
 			}
 			database.createUser(user);
 			// provide all new users with a sample dataset
 			database.addDataset(user, Dataset.newSampleDataset(user));
 		}
 		catch (SQLException sqle) {
 			sqle.printStackTrace();
 			return;
 		}
 		// allow access to the user
 		login(request, response, user);
 	}
 	/**
 	 * Handle logging in the user.
 	 */
 	private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		User user;
 		try {
 			// attempt to get the user
 			 user = getUserFromRequestViaCredentials(request);
 		}
 		catch (SQLException sqle) {
 			// problems
 			sqle.printStackTrace();
 			user = null;
 		}
 		if (user == null) {
 			// the user has given invalid
 			// credentials, redirect them
 			// back to the login form and
 			// alert them of their mistake
 			setErrorAndRedirect(request, response, Utils.ErrorCode.INVALID_CREDENTIALS);
 			return;
 		}
 		// user exists/is valid
 		// go ahead and log in
 		login(request, response, user);
 	}
 	/**
 	 * Handle logging in the user without any checks.
 	 */
 	private void login(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
 		// ask for the response to not be cached
 		Utils.pleaseDontCache(response);
 		// get the session
 		HttpSession session = request.getSession();
 		// attach the user to the session
 		session.setAttribute(Utils.CURRENT_SESSION_USER, user);
 		// show them their projects
 		response.sendRedirect("/projects");
 	}
 	/**
 	 * Handle logging out the user.
 	 */
 	private void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// no cache
 		Utils.pleaseDontCache(response);
 		// invalidate our current session
 		Utils.invalidateSession(request, response);
 	}
 	/**
 	 * Handle POST requests.
 	 */
 	@Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		final String requestUri = request.getRequestURI().substring(1);
 		if (database == null) {
 			// serious issues
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return;
 		}
 		else if (requestUri.endsWith("register")) { register(request, response); }
 		else if (requestUri.endsWith("login"))    { login(request, response); }
 		else if (requestUri.endsWith("logout"))   { logout(request, response); }
 	}
 	/**
 	 *
 	 */
 	@Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		final String requestUri = request.getRequestURI().substring(1);
 		if (requestUri.endsWith("logout")) {
 			logout(request, response);
 			return;
 		}
 		// don't respond to anything but logout
 		response.sendError(HttpServletResponse.SC_NOT_FOUND);
 	}
 
 }
