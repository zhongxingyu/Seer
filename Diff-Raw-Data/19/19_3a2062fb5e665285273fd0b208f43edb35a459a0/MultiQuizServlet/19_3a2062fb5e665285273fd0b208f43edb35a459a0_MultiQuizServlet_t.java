 package servlets;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import sql.DB;
 import frontend.Question;
 import frontend.Quiz;
 import frontend.Result;
 import frontend.User;
 
 /**
  * Servlet implementation class MultiQuizServlet
  */
 @WebServlet("/MultiQuizServlet")
 public class MultiQuizServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public MultiQuizServlet() {
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
 		
 		HttpSession session = request.getSession();
 		
 		// check if a multi page quiz is already under way
 		Quiz quiz = (Quiz) session.getAttribute("multiQuiz");
 		if(quiz == null){
 			// start a new quiz
 			quiz = db.getQuiz(request.getParameter("quizID"));
 			session.setAttribute("multiQuiz", quiz);
 			session.setAttribute("multiQuizScore", new Integer(0));
 		}
 		
 		// otherwise grade last question
 		else {
			// get question number
			int questionNum = 0;
			try {
			    questionNum =  new Integer((String)request.getParameter("questionNum"));
			  } catch (NumberFormatException e) {
			    // something went wrong
				// reset quiz status and send home
				session.setAttribute("multiQuiz", null);
				 request.getRequestDispatcher("take_multi_page_quiz.jsp").forward(request, response);
			  }
			
 			String answer = request.getParameter(new Integer(questionNum).toString());
 			Question q = quiz.getQuestionNum(questionNum);			
 			if(q.isCorrect(answer)) {
 				Integer oldScore = (Integer) session.getAttribute("multiQuizScore");
 				session.setAttribute("multiQuizScore", oldScore + 1);
 			}
 		}
 		
 		// get next question
 		Question q = quiz.nextQuestion();
 		request.setAttribute("nextQuestion", q);
 		
 		
 		// if question is null, the quiz is over. show results
 		if(q == null){
 			//TODO: calculate time used
 			
 			// make new result
 			User user = (User)request.getSession().getAttribute("user");
 			int score = (Integer)session.getAttribute("multiQuizScore");
 			Result r = new Result(quiz.getQuizId(), user.getID(), 1000, quiz.getNumQuestions(), score, new Date().toString());
 			user.addResult(r);
			
			// reset quiz status
			session.setAttribute("multiQuiz", null);
 
 			// send to quiz result page
 			request.getRequestDispatcher("QuizResultServlet?quiz=" + quiz.getQuizId()).forward(request, response);
 		}
 		
 		// otherwise forward to next question
 		else {
 			request.setAttribute("question", q);
			request.getRequestDispatcher("take_multi_page_quiz.jsp").forward(request, response);
 		}
 		
 		
 		
 	}
 
 }
