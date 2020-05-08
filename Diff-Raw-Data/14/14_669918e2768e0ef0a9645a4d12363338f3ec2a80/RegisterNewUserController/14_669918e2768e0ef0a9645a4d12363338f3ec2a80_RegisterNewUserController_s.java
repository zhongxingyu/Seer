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
 @WebServlet("/RegisterUserController")
 public class RegisterNewUserController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public RegisterNewUserController() {
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
 		String password = request.getParameter("password");
 		String passwordConfirm = request.getParameter("passwordConfirm");
 		
 		
 		//TODO: NOTE for RegisterUserView.jsp
 		//if (request.getAttribute("failuireMessage") == NULL don't print out anything, otherwise print out the message
 		
 		//Check for empty case
 		if(userName == "") {
 			request.setAttribute("failureMessage", "Can not leave user name blank. Please enter a valid user name");
 			RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_NEW_USER_VIEW);
 			dispatch.forward(request, response);
 			return;
 		} else {
 	
 		//Catch SQLExceptions
 			try {
 				
 				//Existing account case
 				if (User.userExists(userName)) {
 					request.setAttribute("failureMessage", "User name is already taken. Please try another user name.");
 					RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_NEW_USER_VIEW);
 					dispatch.forward(request, response);
 					return;
 			
 				//Password not the same
 				} else if (!password.equals(passwordConfirm)) {
 					request.setAttribute("failureMessage", "Password and password confirmation do not match. Please try registering again");
 					RequestDispatcher dispatch = request.getRequestDispatcher(Util.REGISTER_NEW_USER_VIEW);
 					dispatch.forward(request, response);
 					return;
 				
 				//Salt, has, and a new account to accounts
 				} else {
 					//Add user and then set the USER_SESSION_KEY to the userId
 					int userId = registerNewUser(userName, password);
 					HttpSession session = request.getSession();
 					session.setAttribute(Util.USER_SESSION_KEY, userId);
 					//Send to the main view
 					RequestDispatcher dispatch = request.getRequestDispatcher(Util.MAIN_VIEW);
 					dispatch.forward(request, response);
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				System.err.println("SQL Error while autheticating user registration: ");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Registers a new user in the database (taking care of salting and hashing passwords
 	 * and returns the ID of the user (that can be stored later to identify them in the session.
 	 */
 	private int registerNewUser(String userName, String password) {
 		String salt = Util.generateSalt();
 		String saltedHash = Util.makeSaltedHash(password, salt);
 		
 		
		return;
		
 	}
x
 
 }
