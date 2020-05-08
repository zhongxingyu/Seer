 package Servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import Accounts.Account;
 import Accounts.AccountManager;
 
 import sun.font.TrueTypeFont;
 
 import model.FillInTheBlank;
 import model.Matching;
 import model.MultipleAnswer;
 import model.MultipleChoice;
 import model.MultipleChoiceMultipleAnswer;
 import model.PictureResponse;
 import model.Question;
 import model.QuestionResponse;
 import model.Quiz;
 
 
 /**
  * Servlet implementation class AddQuestion
  */
 @WebServlet("/AddQuestion")
 public class AddQuestion extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private Quiz quiz;
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public AddQuestion() {
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
 		HttpSession session = request.getSession(true);
 		//public Quiz(ArrayList<Question> q, boolean random, boolean onePage, boolean immediateCorrect, boolean practice, int userID, String quizName, String description, String category){
 		Quiz quiz;
 		if (session.getAttribute("Quiz")==null) {
 			
 			boolean random = (String)request.getParameter("random")!=null ? true : false ;
 			boolean onePage = (String)request.getParameter("multipage")!=null ? true : false ;
 			boolean ic = (String)request.getParameter("immediateCorrection")!=null ? true : false ;
 			String nameString = (String)request.getParameter("quizName");
			String categoryString = (String)request.getParameter("quizCategory");
			String descriptionString = (String)request.getParameter("quizDescription");
 			Account account =  ((Account)session.getAttribute("account"));
 			ServletContext sc = request.getServletContext();
 			AccountManager am = (AccountManager) sc.getAttribute("accounts");
 			quiz = new Quiz( am.getCon(), new ArrayList<Question>(), random, onePage, ic, false, account, nameString, categoryString, descriptionString);
 			session.setAttribute("Quiz", quiz);
 			
 		}
 		else{
 			quiz = (Quiz) session.getAttribute("Quiz");
 		}
 		
 		String question = new String();
 		try {
 			response.setContentType("text/html");
 			PrintWriter out = response.getWriter();
 			out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
 			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
 					      + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
 			out.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
 			out.println("<head>");
 			out.println("<title>"+quiz.getQuizName()+"</title>");
 			out.println("</head>");
 			out.println("<body>");
 			out.println("<h1>"+quiz.getQuizName()+"</h1>");
 			out.println("<h3>Input Question "+(quiz.getQuestions().size()+1)+"</h3>");
 			String type = request.getParameter("questionType");
 			
 			switch(Integer.parseInt(type)) {
 			case 1: 
 				question = QuestionResponse.getHTMLInputString();
 				
 				break;
 			
 			case 2:
 				question = FillInTheBlank.getHTMLInputString();
 				
 				break;
 				
 			case 3:
 				question = MultipleChoice.getHTMLInputString();
 				
 				break;
 				
 			case 4:
 				question = PictureResponse.getHTMLInputString();
 				break;
 				
 			case 5:
 				question = MultipleAnswer.getHTMLInputString();
 				break;
 				
 			case 6:
 				question = MultipleChoiceMultipleAnswer.getHTMLInputString();
 				break;
 				
 			case 7:
 				question = Matching.getHTMLInputString();
 				break;
 			case 8:
 				question = MultipleChoice.getRandomHTMLInputString();
 				break;
 		}
 			
 			if (type.equals("8")){
 				type = "3"; // map it back to multiple choice
 			}
 			out.println("<form action=\"SubmitQuestionAndUpdateQuizCreationServlet\" method=\"post\">");
 			out.println("<input name=\"questionType\" type=\"hidden\" value=\"" +type+"\"/>");
 			
 			out.println("<br />Quiz Name: "+quiz.getQuizName()+"");
 			out.println("<br />Quiz Description: " +quiz.getDescription()+"");
 			out.println("<br />Quiz Category: "  +quiz.getCategory()+"<br />");
 			out.println("<br />Quiz Creator: "  +quiz.getCreator().getName()+"<br />");
 			
 			out.println(question);
 			
 			out.println("<br /><input type=\"submit\" value=\"Submit the question\"/>");
 			out.println("</form>");
 			
 			out.println("</body>");
 			out.println("</html>");
 			
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 }
 
 
