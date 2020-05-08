 package quiz;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import user.*;
 import database.*;
 
 /**
  * Servlet implementation class AddRating
  */
 public class AddRating extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddRating() {
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
 		int quizID = Integer.valueOf(request.getParameter("quizID"));
 		String review = request.getParameter("review");
		int rating = Integer.valueOf(request.getParameter("rating"));
		Rating rate = new Rating(((User)request.getSession().getAttribute("User")).getUserID(), quizID, rating, review);
 		
 		//Need to check if user has already rated this quiz and handle that
 		//Right now the sql insert errors out if they've already rated it
 		
 		RatingBank ratingBank = (RatingBank)getServletContext().getAttribute("RatingBank");
 		ratingBank.addRating(rate);
 		RequestDispatcher dispatch = request.getRequestDispatcher("QuizSummary.jsp?id=" + quizID);
 		dispatch.forward(request, response);
 	}
 
 }
