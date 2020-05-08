 package servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.apache.log4j.Logger;

 import action.QuestionAction;
 import action.PersonalAction;
 import action.UserAction;
 
 import net.arnx.jsonic.JSON;
 
 public class IchiyazukeServlet extends HttpServlet {
 	private static final long serialVersionUID = 2319188394791324751L;
 
 	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
 		request.setCharacterEncoding("UTF-8");
 		response.setCharacterEncoding("application/json; charset=UTF-8");
 		response.setContentType("text/html; charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		String requestStr = request.getPathInfo();
		System.out.println(requestStr);
 
 		if ("/select_question_id".equals(requestStr)) {
 			try {
 				int grade      = Integer.parseInt(request.getParameter("grade"));
 				int category   = Integer.parseInt(request.getParameter("category"));
 				int level      = Integer.parseInt(request.getParameter("level"));
 				int personalId = Integer.parseInt(request.getParameter("personalId"));
 
 				QuestionAction questionAction = new QuestionAction(grade, level, category, personalId);
 				ArrayList<Integer> idList = questionAction.getQuestionIds();
 
 				if (idList.size() < 9){
 					throw new Exception("問題IDを9個以上取得できませんでした.");
 				}
 
 				String tmp = JSON.encode(idList);
 				out.println(tmp);
 				idList.remove(0);
 
 			} catch (IndexOutOfBoundsException e){
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 				out.println("false");
 			}
 
 		// テスト@高田
 		} else if ("/select_question_by_id".equals(requestStr)) {
 			try {
 				int questionId = Integer.parseInt(request.getParameter("questionId"));
 				QuestionAction questionAction = new QuestionAction(questionId);
 				HashMap<String, String> qHashMap = questionAction.getQuestionById2();
 
 				request.setAttribute("questionId", questionId);
 				request.setAttribute("test_title", qHashMap.get("title"));
 				request.setAttribute("test_choice1", qHashMap.get("choice1"));
 				request.setAttribute("test_choice2", qHashMap.get("choice2"));
 				request.setAttribute("test_choice3", qHashMap.get("choice3"));
 				request.setAttribute("test_choice4", qHashMap.get("choice4"));
 				request.setAttribute("test_contents", qHashMap.get("contents"));
 
 				RequestDispatcher rd = request.getRequestDispatcher("/question");
 				rd.forward(request, response);
 				qHashMap.remove(0);
 			} catch (Exception e){
 				e.printStackTrace();
 				out.println("false");
 			}
 
 
 		}else if ("/answer_question".equals(requestStr)) {
 			try {
 				int questionId = Integer.parseInt(request.getParameter("questionId"));
 				int answer     = Integer.parseInt(request.getParameter("answer"));
 				QuestionAction questionAction = new QuestionAction(questionId);
 				HashMap<String, String> qHashMap = questionAction.getAnswerById(answer);
 
 				request.setAttribute("questionId",    questionId);
 				request.setAttribute("title",         qHashMap.get("title"));
 				request.setAttribute("choice",        qHashMap.get("choice"));
 				request.setAttribute("correctAnswer", qHashMap.get("correctAnswer"));
 				request.setAttribute("judgment",      qHashMap.get("judgment"));
 				request.setAttribute("explanation",   qHashMap.get("explanation"));
 
 				RequestDispatcher rd = request.getRequestDispatcher("/answer");
 				rd.forward(request, response);
 				qHashMap.remove(0);
 
 			} catch (IndexOutOfBoundsException e){
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 				out.println("false");
 			}
 		} else if ("/update_infomation".equals(requestStr)) {
 			try {
 				int personalId = Integer.parseInt(request.getParameter("personalId"));
 				int questionId = Integer.parseInt(request.getParameter("questionId"));
 				int result     = Integer.parseInt(request.getParameter("result"));
 
 				PersonalAction personalAction = new PersonalAction(personalId, questionId,result);
 				out.println(personalAction.updatePersonalData());
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				out.println("false");
 			}
 		} else if ("/login".equals(requestStr)) {
 			try {
 				String username = request.getParameter("username");
 				String passward = request.getParameter("passward");
 
 				UserAction userAction = new UserAction(username, passward);
 				out.println(userAction.login());
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				out.println("false");
 			}
 		} else if ("/signup".equals(requestStr)) {
 			try {
 				String username = request.getParameter("username");
 				String passward = request.getParameter("passward");
 
 				UserAction userAction = new UserAction(username, passward);
 				out.println(userAction.signUp());
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				out.println("false");
 			}
 		} else {
 			out.println("false");
 		}
 		out.close();
 	}
 }
