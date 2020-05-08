 package servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import database.Database;
 import entities.User;
 
 /**
  * Servlet implementation class Login
  */
 @WebServlet("/Login")
 public class Login extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public Login() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String username = request.getParameter("username");
 		String password = request.getParameter("pass");
 		User user = Database.getInstance().signIn(username, password);
 		if (user !=null){
 			HttpSession session = request.getSession();
 			session.setAttribute("user", user);	
 			response.sendRedirect("index.jsp");
 		}else{
			response.sendRedirect("index.jsp?message=2");
 		}
 	}
 
 }
