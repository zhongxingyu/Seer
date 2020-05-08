 package servlets;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import frontend.*;
 
 import sql.DB;
 
 /**
  * Servlet implementation class HomeServlet
  */
 @WebServlet("/HomeServlet")
 public class HomeServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final int NUM_QUIZZES = 5;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public HomeServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		System.out.println("hitting home servlet get"); 
 		if(request.getSession().getAttribute("user") == null)
 			request.getRequestDispatcher("/login.html").forward(request, response);
 		else{
 			ServletContext context = request.getServletContext();
 			DB db = (DB) context.getAttribute("db");
 			User currentUser = (User)request.getSession().getAttribute("user");
 			History history = currentUser.getHistory();
 			ArrayList<Quiz> recentlyCreated = currentUser.getCreatedQuizzes();
 			ArrayList<Quiz> popularQuizzes = db.getPopularQuizzes(NUM_QUIZZES);
 			ArrayList<Quiz> recentQuizzes = db.getRecentQuizzes(NUM_QUIZZES);
 			ArrayList<Message> notes = currentUser.getNotes();
 			ArrayList<Challenge> challenges = currentUser.getChallenges();
 			ArrayList<FriendRequest> friendRequests = currentUser.getFriendRequests();
 			ArrayList<Achievement> achievements = currentUser.getAchievements();
 			request.setAttribute("challenges", challenges);
 			request.setAttribute("friendRequests", friendRequests);
 			request.setAttribute("achievements", achievements);
 			request.setAttribute("recentlyCreated", recentlyCreated);
 			request.setAttribute("popularQuizzes", popularQuizzes);
 			request.setAttribute("recentQuizzes", recentQuizzes);
 			request.setAttribute("history", history);
 			request.setAttribute("achievements", achievements);
 			request.getRequestDispatcher("/home.jsp").forward(request, response);
 		}
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		System.out.println("hitting home servlet post");
 		doGet(request, response);
 	}
 	
 	
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 }
