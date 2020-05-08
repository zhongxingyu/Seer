 package Servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.Quiz;
 import model.QuizAttempts;
 
 import Accounts.Account;
 import Accounts.AccountManager;
 
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
     	AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
     	Account user = (Account) request.getSession().getAttribute("account");
     	int id = Integer.parseInt(request.getParameter("id"));
     	Quiz q;
 		try {
			q = new Quiz(id, am.getCon());
 			String qName = q.getQuizName();
 	    	//Account a = q.getCreator();
 	    	//String author = a.getName();
 	    	PrintWriter out = response.getWriter();
 	    	out.println("<h1>"+qName+"</h1>");
 	    	//out.println("by <a href = \"ProfileServlet?user="+author+"\">"+author+"</a>");
 	    	out.println("<br>");
 	    	out.println("<h3>Top Scorers</h3>");
 	    	out.println("<ol>");
 	    	for (QuizAttempts qa : am.getHistory(0, id)) {
 	    		out.println("<li>Hello</li>");
 	    	}
 	    	out.println("</ol><br>");
 	    	out.println("<h3>My Scores</h3>");
 	    	out.println("<ol>");
 	    	for (QuizAttempts qa : am.getHistory(user.getId(), id)) {
 	    		out.println("<li>Hello</li>");
 	    	}
 	    	out.println("</ol>");
 	    	out.println("<li><a href= \"SinglePageQuizServlet?id="+id+"\">Single Page</a></li>");
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
