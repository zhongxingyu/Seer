 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class CreateAccount
  */
 @WebServlet("/CreateAccount")
 public class CreateAccount extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CreateAccount() {
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
 		String firstName = request.getParameter("firstname").toString();
 		String lastName = request.getParameter("lastname").toString();
 		String email = request.getParameter("reg_email__").toString();
 		String email2 = request.getParameter("reg_email_confirmation__").toString();
 		String pass = request.getParameter("reg_passwd__").toString();
 
 		if (DBOperation.createAccount(firstName, lastName, email, pass, "C")) {
 			request.getRequestDispatcher("/page_home.jsp").forward(request, response);
 		} else {
 			// Something is wrong. Go back to index.
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
         
 	}
 
 }
