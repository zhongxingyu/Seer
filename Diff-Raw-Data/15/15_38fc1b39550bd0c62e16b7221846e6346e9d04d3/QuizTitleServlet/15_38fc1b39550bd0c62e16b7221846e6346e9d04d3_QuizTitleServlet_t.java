 package Servlets;
 
 import helpers.HTMLHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import model.*;
 
 import Accounts.*;
 
 /**
  * Servlet implementation class AcctManagementServlet
  */
 @WebServlet("/QuizTitleServlet")
 public class QuizTitleServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
     
     /**
      * @see HttpServlet#HttpServlet()
      */
     public QuizTitleServlet() {
         super();
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	String qid;
     	HttpSession session = request.getSession(true);
 		if (request.getParameter("id") != null){
 			qid = request.getParameter("id");
 		}
 		else {
 			qid = (String) session.getAttribute("quizID");
 		}
 		
 		session.setAttribute("quizID", qid);
     	AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
     	Account user = (Account) request.getSession().getAttribute("account");
     	//Account user = am.getAccount("Sam I Am");
     	int id = Integer.parseInt(qid);
     	Quiz q;
 		try {
 			//if(session.getAttribute("quiz_"+qid) == null){
 				q = new Quiz((id), am.getCon());
 				session.setAttribute("quiz_"+qid, q);
 				
 			//}
 			//else{
 				//q = (Quiz) session.getAttribute("quiz_"+qid); this should never happen
 			//}
 			
 			
 			String qName = q.getQuizName();
 	    	Account a = q.getCreator();
 	    	String author = a.getName();
 	    	response.setContentType("text/html");
 	    	PrintWriter out = response.getWriter();
 	    	out.println("<head>");
 	    	out.println(HTMLHelper.printCSSLink());
 	    	out.println("</head>");
 	    	out.println("<body>");
 	    	out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
 	    	
 			//if(request.getSession().getAttribute("account") != null) out.println(HTMLHelper.printNewsFeed(am.getAnnouncements()));
 	    	
 	    	out.println(HTMLHelper.contentStart());
 	    	out.println("<h1>"+qName+"</h1>");
 	    	out.println("In category: " + q.getCategory() + "<br>");
 	    	out.println("by <a href = \"ProfileServlet?user="+author+"\">"+author+"</a><br>");
 	    	if (request.getSession().getAttribute("account") != null)out.println("<a href = \"newMessage.jsp?quiz="+q.getQuiz_id()+"\">Send to a friend</a><br>");
 	    	out.println(q.getDescription());
 	    	out.println("<br>");
 	    	if (user != null && user.isAdmin()) {
 	    		out.println("<form id=\"admin\" action=\"QuizCatalogServlet\" method=\"post\">");
 	    		out.println("<input type=\"hidden\" name=\"id\" value=\""+q.getQuiz_id()+"\">");
 	    		out.println("<a href=\"#\" onclick=\"document.getElementById(\'admin\').submit();\">Remove Quiz</a>");
 	    		out.println("</form>");
 	    	}
 	    	out.println(HTMLHelper.contentEnd());
 	    	out.println(HTMLHelper.contentStart());
 	    	if (user == null) {
 	    		out.println("NOTE: You are not logged in. Your quiz results will not be saved.");
 	    	}
 	    	out.println("<h3>Top Scorers</h3>");
 	    	out.println("<ol>");
 	    	//currently prints ALL the scores. Can switch to a for (0-4) but table needs to be sorted first.
 	    	//for (QuizAttempts qa : am.getHistory(0, id)) {
 	    	ArrayList<QuizAttempts> history = am.getHistory(0, id);
 	    	for (int i = 0; i < 5; i++) {
 	    		if (i >= history.size()) break;
 	    		QuizAttempts qa = history.get(i);
 	    		out.println(qa.printAttemt(am));
 	    	}
 	    	out.println("</ol>");
 	    	out.println("<a href = \"HistoryServlet?&quiz="+id+"\">More Results</a>");
 	    	out.println(HTMLHelper.contentEnd());
 	    	
 	    	if (user != null) {
 	    		out.println(HTMLHelper.contentStart());	
 	    		out.println("<h3>My Scores</h3>");
 	    		out.println("<ol>");
 	    		history = am.getHistory(user.getId(), id);
 		    	for (int i = 0; i < 5; i++) {
 		    		if (i >= history.size()) break;
 		    		QuizAttempts qa = history.get(i);
 	    			out.println("<li>score: "+qa.getScore()+"%; time: "+qa.getTime()/1000+" s</li>");
 		    	}
 	    	out.println("</ol>");
 	    	out.println("<a href = \"HistoryServlet?&user="+user.getId()+"&quiz="+id+"\">More Results</a>");
 	    	out.println(HTMLHelper.contentEnd());
 	    	}
 	    	
 	    	out.println(HTMLHelper.contentStart());
 	    	if(q.isOnePageMultiPage()){
 		    	out.println("<li><a href= \"MultiPageQuiz?id="+id+"\">Multi Page</a></li>");
 		    	out.println("<li><a href= \"MultiPageQuiz?id=p"+id+"\">Practice Mode</a></li>");
 			}else{
 		    	out.println("<li><a href= \"SinglePageQuizServlet?id="+id+"\">Single Page</a></li>");
 		    	out.println("<li><a href= \"SinglePageQuizServlet?id=p"+id+"\">Practice Mode</a></li>");
 			}
 	    	out.println(HTMLHelper.contentEnd());
 	    	
	    	if(user != null && (user.isAdmin() || user.getId() == q.getCreator().getId())) {
 	    		out.println(HTMLHelper.contentStart());
 	    		out.println("<a href = \"QuizEditServlet?id="+q.getQuiz_id()+"\">Edit Quiz</a>");
 	    		out.println(HTMLHelper.contentEnd());
 	    	}
 	    	out.println("</body>");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     }
 }
