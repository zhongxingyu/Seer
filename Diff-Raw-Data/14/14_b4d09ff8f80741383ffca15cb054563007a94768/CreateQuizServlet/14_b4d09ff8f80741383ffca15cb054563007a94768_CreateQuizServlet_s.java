 package servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import models.FillInQuestion;
 import models.MultipleChoiceQuestion;
 import models.PictureResponseQuestion;
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
 		User user = (User) request.getSession().getAttribute("user");
 		if (user == null || !user.isLoggedIn()) {
 			response.sendRedirect("LoginServlet");
 			return;
 		}
 		
 		RequestDispatcher dispatch = request.getRequestDispatcher("CreateQuiz.jsp");
 		dispatch.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String name = request.getParameter("name");
 		String description = request.getParameter("description");
 		boolean ordered = request.getParameter("ordered") != null;
 		boolean multi_page = request.getParameter("multi_page") != null;
 		boolean immediateCorrection = request.getParameter("immediate_correction") != null;
 		System.out.println(multi_page);
 		System.out.println(request.getParameter("multi_page"));
 		User user = (User) request.getSession().getAttribute("user");
 		if (user == null || !user.isLoggedIn()) {
 			response.sendRedirect("LoginServlet");
 			return;
 		}
 		
 		int numQuestions = Integer.parseInt(request.getParameter("num_questions"));
 		ArrayList<Question> questions = new ArrayList<Question>();
 		System.out.println("Num questions: " + numQuestions);
 		for(int i = 0; i < numQuestions; i++) {
 			int type = Integer.parseInt(request.getParameter("" + i + "_type"));
 			Question currQuestion;
 			switch (type) {
 			case 0:
 				currQuestion = parseQuestionResponseQuestion(request, i);
 				break;
 			case 1:
 				currQuestion = parseFillInQuestion(request, i);
 				break;
 			case 2:
 				currQuestion = parseMultipleChoiceQuestion(request, i);
 				break;
 			case 3:
 				currQuestion = parsePictureResponseQuestion(request, i);
 				break;
 			default:
 				currQuestion = null;
 			}
 			if (currQuestion != null) {
 				questions.add(currQuestion);
 			}
 		}
 		
 		Quiz quiz = new Quiz(name, description, user.getID(), ordered, questions, multi_page, immediateCorrection);
 		QuizFactory factory = QuizFactory.sharedInstance();
		factory.insertQuiz(quiz);
 	}
 	
 	private PictureResponseQuestion parsePictureResponseQuestion(HttpServletRequest request,
 			int index) {
 		ArrayList<String> answers = new ArrayList<String>();
 		String questionString = request.getParameter("" + index + "_question");
 		int numAnswers = Integer.parseInt(request.getParameter("" + index + "_num_answers"));
 		for(int i = 0; i < numAnswers; i++) {
 			String answer = request.getParameter("" + index + "_answer_" + i);
 			if (answer.length() > 0) {
 				answers.add(answer);
 			}
 		}
 		
 		PictureResponseQuestion question = new PictureResponseQuestion(questionString, answers);
 		return question;
 	}
 
 	private MultipleChoiceQuestion parseMultipleChoiceQuestion(HttpServletRequest request,
 			int index) {
 		ArrayList<String> answers = new ArrayList<String>();
 		String questionString = request.getParameter("" + index + "_question");
 		int numAnswers = Integer.parseInt(request.getParameter("" + index + "_num_answers"));
 		for(int i = 0; i < numAnswers; i++) {
 			String answer = request.getParameter("" + index + "_answer_" + i);
 			if (answer.length() > 0) {
 				answers.add(answer);
 			}
 		}
 		
 		int numChoices = Integer.parseInt(request.getParameter("" + index + "_num_choices"));
 		ArrayList<String> choices = new ArrayList<String>();
 		for(int i = 0; i < numChoices; i++) {
 			String choice = request.getParameter("" + index + "_choice_" + i);
 			if (choice.length() > 0) {
 				choices.add(choice);
 			}
 		}
 		
 		MultipleChoiceQuestion question = new MultipleChoiceQuestion(questionString, choices, answers);
 		return question;
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
 	
 	private FillInQuestion parseFillInQuestion(HttpServletRequest request, int index) {
 		ArrayList<String> answers = new ArrayList<String>();
 		String questionString = request.getParameter("" + index + "_question");
 		int numAnswers = Integer.parseInt(request.getParameter("" + index + "_num_answers"));
 		for(int i = 0; i < numAnswers; i++) {
 			String answer = request.getParameter("" + index + "_answer_" + i);
 			if (answer.length() > 0) {
 				answers.add(answer);
 			}
 		}
 		
 		FillInQuestion question = new FillInQuestion(questionString, answers);
 		return question;
 	}
 
 }
