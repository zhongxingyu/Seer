 package Servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import model.Question;
 import model.Quiz;
 
 import model.QuizAttempts;
 /**
  * Servlet implementation class SolveServlet
  */
 @WebServlet("/SolveServlet")
 public class SolveServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private Quiz quiz;
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public SolveServlet() {
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
 			String quizID = (String) session.getAttribute("quizID");
 			
 			//means the cart hasnt been initialized
 			if(session.getAttribute("quiz_"+quizID) == null){
 
 				try {
 					quiz = new Quiz(Integer.parseInt(quizID));
 				} catch (NumberFormatException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				session.setAttribute("quiz_"+quizID, quiz);
 			}
 			else{
 				quiz = (Quiz) session.getAttribute("quiz_"+quizID);
 			}
 			Integer score = 0;
 			// Solve the exam
 			for (Question q : quiz.getQuestions()) {
 				if(q.getType()!=5 || q.getType()!=6){
 					ArrayList<String> answersArrayList = new ArrayList<String>();
 					String paramterString = Integer.toString(q.getType())+"_"+Integer.toString(q.getqID());
 					String string = (String) request.getParameter(paramterString);
 					System.out.println(string);
 					answersArrayList.add(string);
 					score+=q.solve(answersArrayList);
 				}
 			}
 			
 			String timer = (String) request.getParameter("startTime");
 			int time = (int)(-Long.parseLong(timer) + (long)System.currentTimeMillis());
 			
 			QuizAttempts qa = new QuizAttempts(new Integer(1), (Integer) Integer.parseInt(quizID), score, (java.util.Date) new Date(), (int)time);
 			
 			if (session.getAttribute("qa") != null) {
 				session.removeAttribute("qa");
 			}
 			
 			session.setAttribute("qa", qa);
 			response.setContentType("text/html");
 			PrintWriter out = response.getWriter();
 			
 			out.println("<form action=\"QuizResultsServlet\" method=\"post\">");
			out.println("<br /><input type=\"submit\" value=\"Exam is Scores.. See results!\"/>");
 			out.println("</form>");
 			
 			//RequestDispatcher rd = request.getRequestDispatcher("QuizResultsServlet");
 			//rd.forward(request, response);
 	}
 }
 
 
