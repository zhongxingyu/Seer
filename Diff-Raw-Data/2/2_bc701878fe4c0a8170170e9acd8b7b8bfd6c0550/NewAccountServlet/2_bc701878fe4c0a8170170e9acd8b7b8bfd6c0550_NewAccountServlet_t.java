 package servlets;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import quiz.*;
 import user.Activity;
 import user.PasswordHash;
 import user.User;
 
 import java.util.*;
 
 /**
  * Servlet implementation class NewAccountServlet
  */
 @WebServlet("/NewAccountServlet")
 public class NewAccountServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public NewAccountServlet() {
         super();
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
 		Connection conn = (Connection) getServletContext().getAttribute("database");
 		
 		String usr = request.getParameter("username");
 		String pass = request.getParameter("password");
 		String hash = PasswordHash.generationMode(pass);
 		
 		RequestDispatcher dispatch;
 		
 		try {
 			Statement stmt = conn.createStatement();
 			String query = "SELECT * FROM User WHERE username='" + usr + "';";
 			ResultSet rs = stmt.executeQuery(query);
 			
 			
 			if (rs.last()) {
 				dispatch = request.getRequestDispatcher("account_exists.jsp");
 			} else {
 				
 				User user = new User(usr, hash);
 				user.saveToDataBase(conn);
 				
 				request.getSession().setAttribute("user", user);
 				request.setAttribute("unreadMsg", 0);
 				HomePageQueries.getPopQuizzes(request, conn);
 				HomePageQueries.getRecentQuizzes(request, conn);
 				
 				request.setAttribute("authored", new ArrayList<Quiz>());
 				request.setAttribute("userRecent", new ArrayList<QuizResult>());
 				request.setAttribute("activities", new ArrayList<Activity>());
 				
				dispatch = request.getRequestDispatcher("index.jsp");
 			}
 			dispatch.forward(request, response);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
