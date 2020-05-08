 package groupone;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class CreateAccount
  */
 @WebServlet("/CreateCustomer")
 public class CreateCustomer extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CreateCustomer() {
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
 		
 		HttpSession session = request.getSession(true);
 		
 		String firstName = request.getParameter("c_firstname").toString();
 		String lastName = request.getParameter("c_lastname").toString();
 		String email = request.getParameter("c_reg_email__").toString();
 		String email2 = request.getParameter("c_reg_email_confirmation__").toString();
 		String pass = request.getParameter("c_reg_passwd__").toString();
		String encryptedPass = pass;
 		try {
			encryptedPass = AeSimpleSHA1.SHA1(pass);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (firstName.equals("") || lastName.equals("") || email.equals("") || email2.equals("") || pass.equals("")) {
 			request.setAttribute("customer_error", "Uh-Oh! Did you forget something?");
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
 		else if (!email.equalsIgnoreCase(email2)) {
 			request.setAttribute("customer_error", "Uh-Oh! Emails don't match!");
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
		else if (DBOperation.createAccount(firstName, lastName, email, encryptedPass, "C")) 
 		{
			Account account = DBOperation.getAccount(email, encryptedPass);
 			session.setAttribute("userType", "customer");
 			session.setAttribute("account", account);
 			request.getRequestDispatcher("/registration_confirmation.jsp").forward(request, response);
 		} else {
 			// Something is wrong. Go back to index.
 			request.setAttribute("customer_error", "Uh-Oh! Registration failed!");
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
         
 	}
 
 }
