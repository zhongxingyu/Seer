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
 
 /**
  * Servlet implementation class IchiyazukeServlet
  */
 public class IchiyazukeServlet extends HttpServlet {
 	
 	//Loggerを作成する。
 	// static protected Logger logger = Logger.getLogger(IchiyazukeServlet.class);
 
 	
 	private static final long serialVersionUID = 2319188394791324751L;
 
 	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
 
 		request.setCharacterEncoding("UTF-8");
 		response.setCharacterEncoding("application/json; charset=UTF-8");
 
 		String requestStr = request.getPathInfo();
 
 		response.setContentType("text/html; charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		/*
 		logger.debug("It's debug log");
 		logger.info("It's info log");
 		logger.warn("It's warn log");
 		logger.error("It's error log");
 		logger.fatal("It's fatal log");
 		*/
 
 		if ("/select_question_id".equals(requestStr)) {
 			try {
 				int grade      = Integer.parseInt(request.getParameter("grade"));
 				int category   = Integer.parseInt(request.getParameter("category"));
 				int level      = Integer.parseInt(request.getParameter("level"));
 				int personalId = Integer.parseInt(request.getParameter("personalId"));
 
 				QuestionAction questionAction = new QuestionAction(grade, level, category, personalId);
 
 				ArrayList<Integer> idList = questionAction.getQuestionNumbers();
 
 		        if (idList.size() < 9){
 		        	throw new Exception();
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
 		} else if ("/select_question_by_id".equals(requestStr)) {
 			try {
 				int questionId = Integer.parseInt(request.getParameter("questionId"));
 				QuestionAction questionAction = new QuestionAction(questionId);
 				HashMap<String,HashMap<Integer, HashMap<String, String>>> questionHashMap = questionAction.getQuestionById();
 				
 				String test_choice1 = "";
 				String test_choice2 = "";
 				String test_choice3 = "";
 				String test_choice4 = "";
 				String test_answer = "";
 				String test_contents = "";
 				String test_title = "";
 
 				Object[] objKey = questionHashMap.keySet().toArray();
 
 				for (int i = 0; i < objKey.length; i++) {
 					HashMap<Integer, HashMap<String, String>> subHashMap = questionHashMap
 							.get(objKey[i]);
 
 					if (objKey[i].toString().equals("choice1")) {
 
 						Object[] objKey_sub = subHashMap.keySet().toArray();
 
 						for (int j = 0; j < objKey_sub.length; j++) {
 							HashMap<String, String> subsubHashMap = subHashMap
 									.get(objKey_sub[j]);
 
 							Object[] objKey_subsub = subsubHashMap.keySet()
 									.toArray();
 							for (int k = 0; k < objKey_subsub.length; k++) {
 								if (objKey_subsub[k].equals("math")) {
									test_choice1 += "<img src=\"https://chart.googleapis.com/chart?cht=tx&chl=\""
 											+ URLEncoder
 													.encode(subsubHashMap
 															.get(objKey_subsub[k]),
 															"UTF-8") + "\"/>";
 								} else {
 									test_choice1 += subsubHashMap
 											.get(objKey_subsub[k]);
 								}
 							}
 						}
 					} else if (objKey[i].toString().equals("choice2")) {
 						Object[] objKey_sub = subHashMap.keySet().toArray();
 
 						for (int j = 0; j < objKey_sub.length; j++) {
 							HashMap<String, String> subsubHashMap = subHashMap
 									.get(objKey_sub[j]);
 
 							Object[] objKey_subsub = subsubHashMap.keySet()
 									.toArray();
 							for (int k = 0; k < objKey_subsub.length; k++) {
 								if (objKey_subsub[k].equals("math")) {
 									test_choice2 += "<img src=\"https://chart.googleapis.com/chart?cht=tx&chl="
 											+ URLEncoder
 													.encode(subsubHashMap
 															.get(objKey_subsub[k]),
 															"UTF-8") + "\"/>";
 								} else {
 									test_choice2 += subsubHashMap
 											.get(objKey_subsub[k]);
 								}
 							}
 						}
 					} else if (objKey[i].toString().equals("choice3")) {
 						Object[] objKey_sub = subHashMap.keySet().toArray();
 
 						for (int j = 0; j < objKey_sub.length; j++) {
 							HashMap<String, String> subsubHashMap = subHashMap
 									.get(objKey_sub[j]);
 
 							Object[] objKey_subsub = subsubHashMap.keySet()
 									.toArray();
 							for (int k = 0; k < objKey_subsub.length; k++) {
 								if (objKey_subsub[k].equals("math")) {
 									test_choice3 += "<img src=\"https://chart.googleapis.com/chart?cht=tx&chl="
 											+ URLEncoder
 													.encode(subsubHashMap
 															.get(objKey_subsub[k]),
 															"UTF-8") + "\"/>";
 								} else {
 									test_choice3 += subsubHashMap
 											.get(objKey_subsub[k]);
 								}
 							}
 						}
 					} else if (objKey[i].toString().equals("choice4")) {
 						Object[] objKey_sub = subHashMap.keySet().toArray();
 
 						for (int j = 0; j < objKey_sub.length; j++) {
 							HashMap<String, String> subsubHashMap = subHashMap
 									.get(objKey_sub[j]);
 
 							Object[] objKey_subsub = subsubHashMap.keySet()
 									.toArray();
 							for (int k = 0; k < objKey_subsub.length; k++) {
 								if (objKey_subsub[k].equals("math")) {
 									test_choice4 += "<img src=\"https://chart.googleapis.com/chart?cht=tx&chl="
 											+ URLEncoder
 													.encode(subsubHashMap
 															.get(objKey_subsub[k]),
 															"UTF-8") + "\"/>";
 								} else {
 									test_choice4 += subsubHashMap
 											.get(objKey_subsub[k]);
 								}
 							}
 						}
 					} else if (objKey[i].toString().equals("answer")) {
 						Object[] objKey_sub = subHashMap.keySet().toArray();
 
 						for (int j = 0; j < objKey_sub.length; j++) {
 							HashMap<String, String> subsubHashMap = subHashMap
 									.get(objKey_sub[j]);
 
 							Object[] objKey_subsub = subsubHashMap.keySet()
 									.toArray();
 							for (int k = 0; k < objKey_subsub.length; k++) {
 								if (objKey_subsub[k].equals("math")) {
 									test_answer += "<img src=\"https://chart.googleapis.com/chart?cht=tx&chl="
 											+ URLEncoder
 													.encode(subsubHashMap
 															.get(objKey_subsub[k]),
 															"UTF-8") + "\"/>";
 								} else {
 									test_answer += subsubHashMap
 											.get(objKey_subsub[k]);
 								}
 							}
 						}
 					} else if (objKey[i].toString().equals("title")) {
 						Object[] objKey_sub = subHashMap.keySet().toArray();
 
 						for (int j = 0; j < objKey_sub.length; j++) {
 							HashMap<String, String> subsubHashMap = subHashMap
 									.get(objKey_sub[j]);
 
 							Object[] objKey_subsub = subsubHashMap.keySet()
 									.toArray();
 							for (int k = 0; k < objKey_subsub.length; k++) {
 								if (objKey_subsub[k].equals("math")) {
 									test_title += "<img src=\"https://chart.googleapis.com/chart?cht=tx&chl="
 											+ URLEncoder
 													.encode(subsubHashMap
 															.get(objKey_subsub[k]),
 															"UTF-8") + "\"/>";
 								} else {
 									test_title += subsubHashMap
 											.get(objKey_subsub[k]);
 								}
 							}
 						}
 					} else if (objKey[i].toString().equals("contents")) {
 						Object[] objKey_sub = subHashMap.keySet().toArray();
 
 						for (int j = 0; j < objKey_sub.length; j++) {
 							HashMap<String, String> subsubHashMap = subHashMap
 									.get(objKey_sub[j]);
 
 							Object[] objKey_subsub = subsubHashMap.keySet()
 									.toArray();
 							for (int k = 0; k < objKey_subsub.length; k++) {
 								if (objKey_subsub[k].equals("math")) {
 									test_contents += "<img src=\"https://chart.googleapis.com/chart?cht=tx&chl="
 											+ URLEncoder
 													.encode(subsubHashMap
 															.get(objKey_subsub[k]),
 															"UTF-8") + "\"/>";
 								} else {
 									test_contents += subsubHashMap
 											.get(objKey_subsub[k]);
 								}
 							}
 						}
 					}
 
 				}
 
 				request.setAttribute("test_choice1", test_choice1);
 				request.setAttribute("test_choice2", test_choice2);
 				request.setAttribute("test_choice3", test_choice3);
 				request.setAttribute("test_choice4", test_choice4);
 				request.setAttribute("test_answer", test_answer);
 				request.setAttribute("test_title", test_title);
 				request.setAttribute("test_contents", test_contents);
 				
 				RequestDispatcher rd = request.getRequestDispatcher("/question");
 				rd.forward(request, response);
 				questionHashMap.remove(0);
 			} catch (Exception e){
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
 			out.println("falsea");
 		}
 		out.close();
 	}
 }
