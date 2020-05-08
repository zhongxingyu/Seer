 package servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import sql.DB;
 import frontend.Quiz;
 import frontend.User;
 
 /**
  * Servlet implementation class UserViewServlet
  */
@WebServlet("/UserViewServlet")
 public class ProfileViewServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ProfileViewServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ServletContext context = request.getServletContext();
 		DB db = (DB) context.getAttribute("db");
 
 		String userId = ((String) request.getParameter("user"));
 		
 		// get quiz and store in request
 		User profile = db.getUser(userId);
 		request.setAttribute("profile", profile);
 		
 		request.getRequestDispatcher("/profile_view.jsp").forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 }
