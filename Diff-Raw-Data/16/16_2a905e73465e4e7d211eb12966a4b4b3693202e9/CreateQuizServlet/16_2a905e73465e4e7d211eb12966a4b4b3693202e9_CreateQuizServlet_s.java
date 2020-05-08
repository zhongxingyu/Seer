 package servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import models.Question;
 import models.QuestionResponseQuestion;
 import models.Quiz;
 import models.QuizFactory;
 import models.User;
 
 /**
  * Servlet implementation class CreateQuizServlet
  */
 @WebServlet("/CreateQuizServlet")
 public class CreateQuizServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CreateQuizServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		RequestDispatcher dispatch = request.getRequestDispatcher("CreateQuiz.jsp");
 		dispatch.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String name = request.getParameter("name");
 		String description = request.getParameter("description");
 		boolean ordered = Boolean.parseBoolean(request.getParameter("ordered"));
 		boolean multi_page = Boolean.parseBoolean(request.getParameter("multi_page"));
 		User user = (User) request.getSession().getAttribute("user");
 		
 		int numQuestions = Integer.parseInt(request.getParameter("num_questions"));
 		ArrayList<Question> questions = new ArrayList<Question>();
 		for(int i = 0; i < numQuestions; i++) {
 			int type = Integer.parseInt(request.getParameter("" + i + "_type"));
 			Question currQuestion;
 			switch (type) {
 			case 0:
 				currQuestion = parseQuestionResponseQuestion(request, i);
 				break;
 			default:
 				currQuestion = null;
 			}
 			if (currQuestion != null) {
 				questions.add(currQuestion);
 			}
 		}
 		
		Quiz quiz = new Quiz(name, description, user.getId(), ordered, questions, multi_page);
 		QuizFactory factory = QuizFactory.sharedInstance();
 		factory.insertQuiz(quiz);
 	}
 	
 	private QuestionResponseQuestion parseQuestionResponseQuestion(HttpServletRequest request, int index) {
 		ArrayList<String> answers = new ArrayList<String>();
 		String questionString = request.getParameter("" + index + "_question");
 		int numAnswers = Integer.parseInt(request.getParameter("" + index + "_num_answers"));
 		for(int i = 0; i < numAnswers; i++) {
 			String answer = request.getParameter("" + index + "_answer_" + i);
 			if (answer.length() > 0) {
 				answers.add(answer);
 			}
 		}
 		
 		QuestionResponseQuestion question = new QuestionResponseQuestion(questionString, answers);
 		return question;
 	}
 
 }
