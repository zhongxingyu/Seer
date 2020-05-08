 package ge.edu.freeuni.restaurant.presentation;
 
 
 import ge.edu.freeuni.restaurant.logic.DBConnector;
 import ge.edu.freeuni.restaurant.logic.User;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class AccountCreatingServlet
  */
 @WebServlet("/AccountCreatingServlet")
 public class AccountCreatingServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AccountCreatingServlet() {
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
 		String	username = request.getParameter("username");
 		String	pass = request.getParameter("myPass");
 		String	info = request.getParameter("info");
 		String 	name = request.getParameter("name");
 		String	surname =request.getParameter("surname");
 		
 		
 		if(username.equals("") || pass.equals("") || info.equals("") || name.equals("") || surname.equals("") ){
 			RequestDispatcher dispatch = request.getRequestDispatcher("FillFields.html");
 			dispatch.forward(request, response);
 		}
 		else{
			User user = new User(username, pass, name, surname, info);
 			DBConnector db = DBConnector.getInstance();
 			if(true ){//db.registerNewUser(user)
 				RequestDispatcher dispatch = request.getRequestDispatcher("AccountCreated.jsp");
 				dispatch.forward(request, response);
 			}
 		}
 		
 	}
 
 }
