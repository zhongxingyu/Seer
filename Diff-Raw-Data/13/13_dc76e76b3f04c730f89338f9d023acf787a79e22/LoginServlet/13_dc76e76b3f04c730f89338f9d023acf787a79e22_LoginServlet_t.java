 package site;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class LoginServlet
  */
 @WebServlet("/LoginServlet")
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public LoginServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		RequestDispatcher dispatch = request.getRequestDispatcher("success.jsp");
 		dispatch.forward(request, response);
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//ServletContext context = this.getServletContext();
 		//AccountManager manager = (AccountManager)context.getAttribute("manager");
 		
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		
 		HttpSession session = request.getSession();
 		AccountManager manager = (AccountManager)session.getAttribute("manager");
 		if(manager == null) {
 			manager = new AccountManager();
 			session.setAttribute("manager", manager);
 		}
 		
 		if(manager.isExistingAccount(username) && manager.isPasswordCorrect(username, password)) {
 			
 			User user = manager.getAccount(username);
 			if(user != null) {
 				session.setAttribute("user", user);
 				
				if(request == null || response == null) return;
 				RequestDispatcher dispatch = request.getRequestDispatcher("success.jsp");
 				dispatch.forward(request, response);
 				return;
 			}
 		}
 		RequestDispatcher dispatch = request.getRequestDispatcher("failure.jsp");
 		dispatch.forward(request, response);
 	}
 
 }
