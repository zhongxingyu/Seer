 package quiz;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import database.QuizBank;
 
 /**
  * Servlet implementation class CreateQuestionServlet
  */
 public class CreateQuestionServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CreateQuestionServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		// request for new question results in forward to appropriate page
 		// in this case, we came from CreateQuestion.html
 		if (request.getParameter("new") != null) {
 			RequestDispatcher rd;
 			int type = Integer.parseInt(request.getParameter("type"));
 			switch (type) {
 			case 0:
 				rd = request.getRequestDispatcher("CreateQuestionResponse.html");
 				break;
 			case 1:
 				rd = request.getRequestDispatcher("CreateFillBlank.html");
 				break;
 			case 2:
 				rd = request.getRequestDispatcher("CreatePictureResponse.html");
 				break;
 			case 3:
 				rd = request.getRequestDispatcher("CreateMultipleChoice.html");
 				break;
 			default:
				rd = request.getRequestDispatcher("CreateQuestion.html"); // default return to the create question page
 				break;
 			}
 			
 			
 			rd.forward(request, response);
 			return;
 		}
 		
 		// request to finish creating quiz results in forward to quiz summary
 		if (request.getParameter("finish") != null) {
 			// get the quiz object
 			HttpSession session = request.getSession();
 			Quiz quiz = (Quiz) session.getAttribute("Quiz");
 			
 			// forward to quiz summary page
 			RequestDispatcher rd;
 			//request.setAttribute("id", quiz.getQuizID());
 			rd = request.getRequestDispatcher("QuizSummary.jsp?id=" + quiz.getQuizID());
 			rd.forward(request, response);
 			return;
 		}
 		
 		// request to submit a new question
 		
 		// get access to java banks
 		ServletContext application = this.getServletContext();
 		QuizBank quizBank = (QuizBank)application.getAttribute("QuizBank");
 		QuestionBank questionBank = (QuestionBank) application.getAttribute("QuestionBank");
 		HttpSession session = request.getSession();
 		Quiz quiz = (Quiz) session.getAttribute("Quiz");
 		
 		// submit request for a new quiz if one hasn't been sent yet
 		if (quiz.getQuizID() == 0) {
 			try {
 				// create new quiz
 				int id = quizBank.addQuiz(quiz);
 				// find the quiz in the database
 				quiz = quizBank.getQuiz(id);
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return;
 			}			
 		}
 		
 		// get common parameters
 		Question question; // container to hold the question
 		int questionID = 0; // auto generated
 		int quizID = quiz.getQuizID();
 		int questionNumber = (Integer) session.getAttribute("QuestionNumber");
 		
 		// create an object to put in the question container
 		if (request.getParameter("questionresponse") != null) {
 			String questiontext = request.getParameter("question");
 			
 			// parse answers from input field
 			String answerlist = request.getParameter("answer");
 			Set<String> answerSet = Question.parseMultipleAnswers(answerlist);
 			
 			// form a new QuestionResponse object based on parameters
 			question = new QuestionResponse(questionID, quizID, questionNumber, questiontext, answerSet);
 			
 		}
 		else if (request.getParameter("fillintheblank") != null) {
 			// parse two parts of the question
 			String questiontext1 = request.getParameter("question1");
 			String questiontext2 = request.getParameter("question2");
 			
 			// parse answers from input field
 			String answerlist = request.getParameter("answer");
 			Set<String> answerSet = Question.parseMultipleAnswers(answerlist);
 			
 			// form a new FillInTheBlank object based on parameters
 			question = new FillInTheBlank(questionID, quizID, questionNumber, questiontext1, questiontext2, answerSet);
 		}
 		else if (request.getParameter("pictureresponse") != null) {
 			String questiontext = request.getParameter("question");
 			String imageUrl = request.getParameter("image");
 			
 			// parse answers from input field
 			String answerlist = request.getParameter("answer");
 			Set<String> answerSet = Question.parseMultipleAnswers(answerlist);
 			
 			// form a new PictureResponse object based on parameters
 			question = new PictureResponse(questionID, quizID, questionNumber, imageUrl, questiontext, answerSet);
 		}
 		else if (request.getParameter("multiplechoice") != null) {
 			String questiontext = request.getParameter("question");
 			
 			// parse choices from input field
 			List<String> choices = new ArrayList<String>();
 			for (int i=1; i <= MultipleChoice.MAX_CHOICES; i++) {
 				String choice = request.getParameter("choice" + i);
 				if (choice.length() != 0)
 					choices.add(choice);
 			}
 			
 			// parse the correct choice from the radio buttons
 			String answer = request.getParameter(request.getParameter("answer"));
 			Set<String> answerSet = new HashSet<String>();
 			answerSet.add(answer);
 			
 			// form a new MultipleChoice object based on parameters
 			question = new MultipleChoice(questionID, quizID, questionNumber, questiontext, choices, answerSet);
 		}
 		else {
 			System.err.println("Error: cannot understand request.");
 			return;
 		}
 		
 		// submit request for a new question for this quiz
 		try {
 			// this method will update the question id
 			questionBank.addQuestion(question);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		// update the next question number
 		session.setAttribute("QuestionNumber", questionNumber +1);
 		
 		// add the question to the quiz in the session for the purpose of computing max score
 		quiz.addQuestion(question.getQuestionID());
 		session.setAttribute("Quiz", quiz); // update session object
 		
 		// update the max score
 		// here we use the number of questions as the max score. In the future, this mechanism will need
 		// to be changed if a question can be worth more than a single point.
 		int score = quiz.getQuestions().size();
 		quizBank.updateScore(quiz.getQuizID(), score);
 		
 		// forward to the create a new question page
		RequestDispatcher rd = request.getRequestDispatcher("CreateQuestion.html");
 		rd.forward(request, response);
 	}
 
 }
