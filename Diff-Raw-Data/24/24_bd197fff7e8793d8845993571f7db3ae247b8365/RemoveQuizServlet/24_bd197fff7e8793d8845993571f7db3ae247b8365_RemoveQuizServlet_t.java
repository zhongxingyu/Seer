 package servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import frontend.Quiz;
 
 import sql.DB;
 
 /**
  * Servlet implementation class RemoveQuizServlet
  */
 @WebServlet("/RemoveQuizServlet")
 public class RemoveQuizServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public RemoveQuizServlet() {
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
 		ServletContext context = request.getServletContext();
 		DB db = (DB) context.getAttribute("db");
 		
 		// remove quiz
 		HttpSession session = request.getSession();
		String quizId = (String) request.getParameter("target");
 		Quiz q = db.getQuiz(quizId);
 		db.clearQuizHistory(quizId);
 		db.removeQuiz(q);
 		
 		request.getRequestDispatcher("/remove_user.jsp").forward(request, response);
 	}
 
 }
