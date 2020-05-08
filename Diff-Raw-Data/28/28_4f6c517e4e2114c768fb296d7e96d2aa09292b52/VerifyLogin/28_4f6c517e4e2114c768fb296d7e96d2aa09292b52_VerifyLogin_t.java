 package groupone;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class HomeServlet
  */
 @WebServlet("/VerifyLogin")
 public class VerifyLogin extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public VerifyLogin() {
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
 		String email = request.getParameter("emailAddress").toString();
 		String pass = request.getParameter("password").toString();
 
 		if (DBOperation.isValidLogin(email, pass)) {
 			Account account = DBOperation.getAccount(email, pass);
 			
 			request.setAttribute("account", account);
 			request.setAttribute("accountId", account.getId());
 			System.out.println("AccountId = " + request.getAttribute("accountId"));
 			if (DBOperation.isVendor(email)) {
				request.setAttribute("email", email);
 				request.getRequestDispatcher("/vendor.jsp").forward(request, response);
 			}
 			else {
				request.setAttribute("email", email);
 				request.getRequestDispatcher("/page_home.jsp").forward(request, response);
 			}
 			
 		} else {
 			// Something is wrong. Go back to index.
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
 	}
 
 }
