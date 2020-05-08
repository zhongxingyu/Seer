 package quizsite.controllers;
 
 import java.io.IOException;
 import java.security.*;
 import java.sql.SQLException;
 import java.util.*;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import quizsite.models.User;
 
 
 /**
  * Servlet implementation class RegisterUserController
  */
 @WebServlet({"/RegisterNewUserController", "/register"})
 public class RegisterUserController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public RegisterUserController() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//Get take in the userName and password
 		//Check if the user exists in the database yet
 			//Redirect them otherwise ----->>
 		//Salt the password and hash
 		//Create a user in the database
 		//Set the servlet context
 		// send the user to the homepage ----->>
 			
 		//Get parameters from the request
 		String userName = request.getParameter("userName");
 		String email = request.getParameter("email");
 		String password = request.getParameter("password");
 		String passwordConfirm = request.getParameter("passwordConfirm");
 		
 		//if (request.getAttribute("failuireMessage") == NULL don't print out anything, otherwise print out the message
 	
 		//Check for empty case
 		if(userName.equals("")) {
 			request.setAttribute("failureMessage", "Can not leave user name blank. Please enter a valid user name");
 			RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_USER_VIEW);
 			dispatch.forward(request, response);
 			return;
 		//Check for empty password	
 		} else if(password.equals("")) {
 			request.setAttribute("failureMessage", "Can not leave password blank");
 			RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_USER_VIEW);
 			dispatch.forward(request, response);
 			return;
 		//Check for valid email
 		} else if(!isValidEmail(email)) {
 			request.setAttribute("failureMessage", "Invalid email address entered. Please enter a valid email address");
 			RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_USER_VIEW);
 			dispatch.forward(request, response);
 			return;
 		} else {
 		
 		//Catch SQLExceptions
 			try {
 				
 				//Existing account case
 				if (User.userExists(userName)) {
 					request.setAttribute("failureMessage", "User name is already taken. Please try another user name.");
 					RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_USER_VIEW);
 					dispatch.forward(request, response);
 					return;
 			
 				//Password not the same
 				} else if (!password.equals(passwordConfirm)) {
 					request.setAttribute("failureMessage", "Password and password confirmation do not match. Please try registering again");
 					RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_USER_VIEW);
 					dispatch.forward(request, response);
 					return;
 					
 				//Register user and set the userId to the session, then send to main view
 				} else {
 					//Add user and then set the USER_SESSION_KEY to the userId
					Integer userId = User.registerNewUser(userName, email, password);
 					if(userId == -1) throw new SQLException();
 					//If there is an error adding the user, throw an SQL exception
 					HttpSession session = request.getSession();
 					session.setAttribute(Util.USER_SESSION_KEY, userId);
 					//Send to the main view
 					RequestDispatcher dispatch = request.getRequestDispatcher(Util.HOME_VIEW);
 					dispatch.forward(request, response);
 					return;
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				System.err.println("SQL Error while autheticating user registration: ");
 				e.printStackTrace();
 				request.setAttribute("failureMessage", "There was an error making the registration. Please try registering again.");
 				RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_USER_VIEW);
 				dispatch.forward(request, response);
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Checks if the inputed email is valid
 	 */
 	private boolean isValidEmail(String email) {
 		int atIndex = email.indexOf('@');
 		if (atIndex == -1) return false;
 		int dotIndex = email.indexOf('.', atIndex);
 		if(dotIndex == -1) return false;
 		int spaceIndex = email.indexOf(' ');
 		if(spaceIndex != -1) return false;
 		return true;
 	}
 
 }
