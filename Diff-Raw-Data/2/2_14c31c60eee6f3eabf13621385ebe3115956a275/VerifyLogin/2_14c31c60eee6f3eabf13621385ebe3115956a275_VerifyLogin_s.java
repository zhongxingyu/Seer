 package groupone;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
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
 		HttpSession session = request.getSession(true);
 		String email = request.getParameter("emailAddress").toString();
 		String pass = request.getParameter("password").toString();
 
 		if (DBOperation.isValidLogin(email, pass)) {
 			Account account = DBOperation.getAccount(email, pass);
 			
 			request.setAttribute("account", account);
 			session.setAttribute("accountId", account.getId());
 			session.setAttribute("userEmail", email);
 			session.setAttribute("account", account);
 			
 			if (DBOperation.isVendor(email)	) {
				request.getRequestDispatcher("/vendor.jsp").forward(request, response);
 			}
 			else {
 				request.getRequestDispatcher("/page_home.jsp").forward(request, response);
 				//request.getRequestDispatcher("/homeProposal.jsp").forward(request, response);
 			}
 			
 		} else {
 			request.setAttribute("errorMsg", "Incorrect email/password");
 			request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
 	}
 
 }
