 package Servlets;
 
 import helpers.HTMLHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.TreeMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import Accounts.*;
 
 /**
  * Servlet implementation class AcctManagementServlet
  */
 @WebServlet("/MailManagementServlet")
 public class MailManagementServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
     
     /**
      * @see HttpServlet#HttpServlet()
      */
     public MailManagementServlet() {
         super();
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	MailManager mm = (MailManager) request.getServletContext().getAttribute("mail");
     	String user = request.getParameter("user");
     	response.setContentType("text/html");
     	PrintWriter out = response.getWriter();
     	out.println("<head>");
     	out.println(HTMLHelper.printCSSLink());
     	out.println("</head>");
     	out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
     	out.println("<body>");
     	
     	AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
 		if(request.getSession().getAttribute("account") != null) out.println(HTMLHelper.printNewsFeed(am.getAnnouncements()));
 		
     	out.println(HTMLHelper.contentStart());
     	
     	
     	
     	
     	if (request.getParameter("index").equals("inbox")) {
     		TreeMap<Integer, Message> inbox = mm.listInbox(user);
     		out.println("<table border=\"0\">");
     		out.println("<tr><th><b>Subject</b></th><th><b>Sender</b></th><th><b>Date</b></th></tr>");
     		for (int i : inbox.keySet()) {
     			String boldin = "";
     			String boldout = "";
     			if (inbox.get(i).unread) {
     				boldin = "<b>";
     				boldout = "</b>";
     			}
     			out.println("<tr>");
     			out.println("<td>"+boldin+"<a href = \"MailManagementServlet?&index="+i+"&user="+user+"\">");
     			out.println(inbox.get(i).getSubject()) ;
     			out.println("</a>"+boldout+"</td>");
     			out.println("<td><a href = \"ProfileServlet?user="+inbox.get(i).getSender()+"\">");
     			out.println(inbox.get(i).getSender()) ;
     			out.println("</a></td>");
     			out.println("<td>");
     			out.println(new java.util.Date(inbox.get(i).getTimestamp())) ;
     			out.println("</td>");
     		}
     		out.println("</table>");
     	} else if (request.getParameter("index").equals("outbox")) {
     		TreeMap<Integer, Message> outbox = mm.listOutbox(user);
     		//out.println("<ul>");
     		out.println("<table border=\"0\">");
     		out.println("<tr><th><b>Subject</b></th><th><b>Recipient</b></th><th><b>Date</b></th></tr>");
     		for (int i : outbox.keySet()) {
     			out.println("<tr>");
     			out.println("<td><a href = \"MailManagementServlet?&index="+i+"&user="+user+"\">");
     			out.println(outbox.get(i).getSubject()) ;
     			out.println("</a></td>");
     			out.println("<td><a href = \"ProfileServlet?user="+outbox.get(i).getRecipient()+"\">");
     			out.println(outbox.get(i).getRecipient()) ;
     			out.println("</a></td>");
     			out.println("<td>");
     			out.println(new java.util.Date(outbox.get(i).getTimestamp())) ;
     			out.println("</td>");
     		}
     		out.println("</table>");
     	} else {//print specific message
    		int x = 37;
     		try {
     			x = Integer.parseInt(request.getParameter("index"));
     		} catch (NumberFormatException e) {
     			System.out.println("mail fail");
     		}
     		System.out.println(x);
     		Message m = mm.recieveMessage(x);
     		out.println("<b>Subject:</b> " + m.getSubject() + "<br>");
     		out.println("<b>From:</b> " + m.getSender() + " @ " + new java.util.Date(m.getTimestamp())+ "<hr>");
     		if (m.getChallengeName() != null) {
     			out.println(m.getSender() + " has challenged you to take: <a href = QuizTitleServlet?id="+m.getChallengeID()+">"+m.getChallengeName()+"</a><br><br>");
     		}
     		out.println(m.getBody());
     		
     	}
     	out.println(HTMLHelper.contentEnd() + "</body>");
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	String sender = request.getParameter("sender");
     	String recipient = request.getParameter("recipient");
     	AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
     	if (!am.accountExists(recipient)) {
     		String errorURL = "/newMessage.jsp?&to=error&subject="+request.getParameter("subject")+"&quiz=" + request.getParameter("quiz")+"&body="+request.getParameter("body");
     		request.getRequestDispatcher(errorURL).forward(request, response);
     	} else {
     	String subject = request.getParameter("subject");
     	String body = request.getParameter("body");
     	int challenge = -1;
     	try {
     		challenge = Integer.parseInt(request.getParameter("challenge"));
     	} catch (NumberFormatException e) {
 	
     	}
     	Message m = new Message(sender, recipient, subject, body, 0, challenge, null, true);
     	MailManager mm = (MailManager) request.getServletContext().getAttribute("mail");
     	mm.sendMessage(m);
     	request.getRequestDispatcher("/UserHome.jsp").forward(request, response);
     	}
     }
 }
