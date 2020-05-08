 package Servlets;
 
 import helpers.HTMLHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import Accounts.*;
 
 import model.*;
 
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
 					ServletContext sc = request.getServletContext();
 					AccountManager am = (AccountManager) sc.getAttribute("accounts");
 					quiz = new Quiz(Integer.parseInt(quizID), am.getCon());
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
 			Integer oldscore = 0;
 			ArrayList<Boolean> booleans = new ArrayList<Boolean>();
 			// Solve the exam
 			for (Question q : quiz.getQuestions()) {
 				ArrayList<String> answersArrayList = new ArrayList<String>();
 				if(q.getType()!=5 || q.getType()!=6 || q.getType()!=7){
 					String paramterString = Integer.toString(q.getType())+"_"+Integer.toString(q.getqID());
 					String string = (String) request.getParameter(paramterString);
 					answersArrayList.add(string);
 				}
 				if ( q.getType()==5) {
 					answersArrayList = new ArrayList<String>();
 					for(int i = 0; i < q.getNumAnswers(); i++) {
 						String param = Integer.toString(q.getqID())+"_"+Integer.toString(i);
 						answersArrayList.add((String)request.getParameter(param));
 					}
 				}
 				if (q.getType()==6) {
 					answersArrayList = new ArrayList<String>();
 					for(int i = 0; i < q.getNumAnswers(); i++) {
 						String p = "6_" + Integer.toString(q.getqID()) + "_" + i;
 						if(request.getParameter(p) != null) {
 							answersArrayList.add((String)request.getParameter(p));
 						}
 					}
 				}
 				if ( q.getType()==7) {
 					String paramterString = "thedata"+Integer.toString(q.getqID());
 					String string = (String) request.getParameter(paramterString);
 					System.out.println(string);
 					
 					// parse the results into format 2 3 1 5 4
 					string.replaceAll("&", "");
 					String splitString =q.getType()+ "_" + q.getqID()+"[]=";
 					String[] stringArray = string.split(Pattern.quote(splitString));
 
 					System.out.println("Printing answer integers for matching:");
 					answersArrayList= new ArrayList<String>();
 					for (String s : stringArray) {
 
 						try  
 						  {  
 							String temp = s.replaceAll("[^0-9]+","");
 							
 						    int i = Integer.parseInt(temp);  
 						    System.out.println(temp);
 							
 						    answersArrayList.add(Integer.toString(i));
 						    
 						  }  
 						  catch(NumberFormatException nfe)  
 						  {  
 
 						  }
 					}
 				}
 					
 				score+=q.solve(answersArrayList);
				if (oldscore!=score) {
 					booleans.add(true);
 				}else{
 					booleans.add(false);
 				}
 				oldscore = score;
 				q.setUserAnswers(answersArrayList);	
 			}
 			score = (int)(((double)score/quiz.totalScore()) * 100);
 
 			String timer = (String) request.getParameter("startTime");
 			int time = (int)(-Long.parseLong(timer) + (long)System.currentTimeMillis());
 			Account acct = (Account) request.getSession().getAttribute("account");
 			QuizAttempts qa;
 			if (acct != null) {
 				qa = new QuizAttempts(acct.getId(), (Integer) Integer.parseInt(quizID), score, (java.util.Date) new Date(), (int)time);
 				if(!quiz.isPracticeMode()) {
 					//store and update acheivements
 					AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
 					try {
 						qa.pushAttemptToDB((Connection)request.getServletContext().getAttribute("connect"));
 						quiz.addToHistory(qa);
 						int quizesDone = am.quizesTaken(acct);
 						am.updateAchievements(acct, quizesDone);
 						
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			} else { //not logged in, don't save to quiz history
 				qa = new QuizAttempts(-1, (Integer) Integer.parseInt(quizID), score, (java.util.Date) new Date(), (int)time);
 			}
 			if (session.getAttribute("qa") != null) {
 				session.removeAttribute("qa");
 			}
 
 			session.setAttribute("qa", qa);
 			session.setAttribute("booleans", booleans);
 			response.setContentType("text/html");
 			PrintWriter out = response.getWriter();
 			out.println("<head>");
 			out.println(HTMLHelper.printCSSLink());
 			out.println("</head>");
 			out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
 			out.println(HTMLHelper.contentStart());
 			out.println("<form action=\"QuizResultsServlet\" method=\"post\">");
 			out.println("<br /><input type=\"submit\" value=\"Exam is Scored...Click to see results!\"/>");
 			out.println("</form>");
 			out.println(HTMLHelper.contentEnd());
 			
 			//RequestDispatcher rd = request.getRequestDispatcher("QuizResultsServlet");
 			//rd.forward(request, response);
 	}
 }
 
 
