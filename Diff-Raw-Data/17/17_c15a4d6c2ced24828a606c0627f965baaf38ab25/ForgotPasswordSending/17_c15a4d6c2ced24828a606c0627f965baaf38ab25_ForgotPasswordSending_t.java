 package com.epam.lab.intouch.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.controller.member.common.MemberController;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.web.util.AttributeForgotPasword;
 import com.epam.lab.intouch.web.util.EmailPropertie;
 
 @WebServlet("/forgotPassword")
 public class ForgotPasswordSending extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private String host;
 	private String port;
 	private String user;
 	private String pass;
 
 	public void init() {
 		// reads SMTP server setting from web.xml file
 		ServletContext context = getServletContext();
 		host = context.getInitParameter("host");
 		port = context.getInitParameter("port");
 		user = context.getInitParameter("user");
 		pass = context.getInitParameter("pass");
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		MemberController memberController = new MemberController();
 		String resultMessage  = "";
 		String password = AttributeForgotPasword.randomString();
 		// reads form fields
 		String recipient = request.getParameter("userMail");
 		StringBuilder content = new StringBuilder();
 		content.append("Your new password is: ").append(password);
 
 		// String content = "Your new password is: " + password;
 
 		try {
 
 			Member oldMember = memberController.getById(recipient);
 			Member newMember = memberController.getById(recipient);
 			newMember.setPassword(password);
 			memberController.updateProfile(oldMember, newMember);
 
 			EmailPropertie.sendEmail(host, port, user, pass, recipient, content.toString());
			resultMessage = "OK" ;
			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			resultMessage = "There were an error: " + ex.getMessage();
 		} finally {
			 response.getWriter().write(resultMessage);
 			// request.setAttribute("Message", resultMessage);
 			// getServletContext().getRequestDispatcher("/result.jsp").forward(request,
 			// response);
 		}
 	}
	
	
	
 
 }
