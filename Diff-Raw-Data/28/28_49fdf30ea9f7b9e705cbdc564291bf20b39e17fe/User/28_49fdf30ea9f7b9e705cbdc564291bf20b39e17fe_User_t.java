 package se.timelog;
 
 import java.io.IOException;
import java.util.ArrayList;

 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import se.timelog.UserModel;
 import se.timelog.rmi.MockupRMI;
 
 @WebServlet("/User")
 public class User extends HttpServlet{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/views/admin_create_user.jsp").forward(request, response);		
		
	}	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String firstname, lastname, email, password;
		
 		firstname = request.getParameter("firstname");
 		lastname = request.getParameter("lastname");
		email = request.getParameter("email");
 		password = request.getParameter("password");
 		
		
 		UserModel usermodel = new UserModel();
 		usermodel.setFirstName(firstname);
 		usermodel.setLastName(lastname);
 		usermodel.setEmail(email);
 		usermodel.setPassword(password);
 		
 		MockupRMI mockupRMI = new MockupRMI();
		ArrayList<String> errorlist  = mockupRMI.userCreate(usermodel);
		if (errorlist.isEmpty()){
			request.getRequestDispatcher("/WEB-INF/views/success.jsp").forward(request, response);	
		}else{
			request.getRequestDispatcher("/WEB-INF/views/admin_create_user.jsp").forward(request, response);	
		}
 		
	}
 
 }
