 package mail;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import quiz.Score;
 
 import user.User;
 
 import database.FriendBank;
 import database.MailBank;
 import database.QuizBank;
 import database.ScoreBank;
 
 /**
  * Servlet implementation class HandleMail
  */
 public class HandleMail extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public HandleMail() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// unicode support
         if (request.getCharacterEncoding() == null) {
             request.setCharacterEncoding("UTF8");
         }
         
 		User user = ((User)request.getSession().getAttribute("User"));
 		String type = (String) request.getParameter("type");
 		String mailid = (String) request.getParameter("id");
 
 		//Get Mail Bank
 		ServletContext context = getServletContext();
 		MailBank mailBank = (MailBank)context.getAttribute("MailBank");
 		ScoreBank scoreBank = (ScoreBank)context.getAttribute("ScoreBank");
 		
 		Mail mail = mailBank.getMailByID(Integer.parseInt(mailid));
 		mailBank.setIsRead(mail.getMailId());
 		
 	    java.io.PrintWriter out = response.getWriter();
 
 	    //Begin assembling the HTML content
 	    out.println("<html><head>");
 
 	    out.println("<title>Mail for " +user.getUserName() +"</title></head><body>");
	    out.println("<h2>Mesage</h2>");
 
 		if(user.getUserID()==mail.getToId()) {
 			response.getWriter().println(mail.getContents());
 		}
 		else {
 			response.getWriter().println("You do not have access to this message.");
 		}
 	    out.println("</body></html>");
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// unicode support
         if (request.getCharacterEncoding() == null) {
             request.setCharacterEncoding("UTF8");
         }
         
 		User user = ((User)request.getSession().getAttribute("User"));
 		String type = (String) request.getParameter("type");
 		String mailid = (String) request.getParameter("mailid");
 		//System.out.println("Type: " + type);
 		//System.out.println("Mailid: " + mailid);
 
 		//Get Mail Bank
 		ServletContext context = getServletContext();
 		MailBank mailBank = (MailBank)context.getAttribute("MailBank");
 		ScoreBank scoreBank = (ScoreBank)context.getAttribute("ScoreBank");
 		
 		//If its delete request, then delete that mailid
 		if(type.equals("delete")) {
 			mailBank.deleteMail(Integer.parseInt(mailid));
 			RequestDispatcher dispatch = request.getRequestDispatcher("UserMailbox.jsp");
 			dispatch.forward(request, response);
 		}
 		else if(type.equals("acceptRequest")) {
 			int friendID = Integer.valueOf(request.getParameter("FriendID"));
 			FriendBank friendBank = (FriendBank)getServletContext().getAttribute("FriendBank");
 			user.AddFriend(friendID);
 			friendBank.AddFriends(friendID, user.getUserID());
 			mailBank.updateRequest(Integer.parseInt(mailid), MailBank.ACCEPTED);
 			mailBank.setIsRead(Integer.parseInt(mailid));
 			RequestDispatcher dispatch = request.getRequestDispatcher("UserMailbox.jsp");
 			dispatch.forward(request, response);
 		}
 		else if(type.equals("rejectRequest")) {
 			//System.out.println("rejected");
 			mailBank.updateRequest(Integer.parseInt(mailid), MailBank.REJECTED);
 			mailBank.setIsRead(Integer.parseInt(mailid));
 			RequestDispatcher dispatch = request.getRequestDispatcher("UserMailbox.jsp");
 			dispatch.forward(request, response);
 		}
 		else if(type.equals("sendRequest")) {
 			String message = request.getParameter("message");
 			int toUserID = Integer.parseInt(request.getParameter("FriendID"));
 			Mail mail = new Mail(0,message,user.getUserID(),toUserID,null,2,2,null,null);
 			mailBank.addMail(mail);
 			
 			RequestDispatcher dispatch = request.getRequestDispatcher("UserPage.jsp?id=" +toUserID);
 			dispatch.forward(request, response);
 		}
 		else if(type.equals("sendMsg")) {
 			String message = request.getParameter("message");
 			int toUserID = Integer.parseInt(request.getParameter("FriendID"));
 			Mail mail = new Mail(0,message,user.getUserID(),toUserID,null,0,null,null,null);
 			mailBank.addMail(mail);
 			
 			RequestDispatcher dispatch = request.getRequestDispatcher("UserPage.jsp?id=" +toUserID);
 			dispatch.forward(request, response);
 			
 			
 		}
 		else if(type.equals("sendChallenge")) {
 			String quizOption = request.getParameter("quizOption");
 			int toUserID = Integer.parseInt(request.getParameter("FriendID"));
 			String message = request.getParameter("message");
 			//System.out.println("quiz: " + quizOption + " toUserID: " + toUserID+ "\nmessage: " + message);
 			Integer scoreID = null;
 			
 			List<Score> scoreList = scoreBank.getUserQuizTopScores(user.getUserID(), Integer.parseInt(quizOption), 1, 0);
 			if(!scoreList.isEmpty()) { scoreID = scoreList.get(0).getScoreId(); }
 			
 			Mail mail = new Mail(0, message, user.getUserID(), toUserID, null, 1, null, Integer.parseInt(quizOption), scoreID);
 			mailBank.addMail(mail);
 			
 			RequestDispatcher dispatch = request.getRequestDispatcher("UserPage.jsp?id=" +toUserID);
 			dispatch.forward(request, response);
 		}
 	}
 
 }
