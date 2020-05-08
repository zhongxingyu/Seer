 package controllers;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import business.user.UserHandler;

 import models.User;
 
 public class CreateNewUserServlet extends HttpServlet {
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		
 		String lastName = request.getParameter("lastName");
 		String firstName = request.getParameter("firstName");
 		String email = request.getParameter("email");
 		String country = request.getParameter("country");
 		String city = request.getParameter("city");
 		String identifier = request.getParameter("identifier");
 		
		User user = new User(identifier, firstName, lastName, email, city);
		UserHandler userHandler = new UserHandler();
		userHandler.addUser(user);
 		
 		System.out.println(lastName + " " + firstName + " " + email + " " + country);
 		
		String url = "/login.jsp";
 		
 		RequestDispatcher dispatcher = getServletContext()
 				.getRequestDispatcher(url);
 		dispatcher.forward(request, response);
 		
 	}
 
 }
