 package com.globalmesh.action.login;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class LogoutServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		HttpSession session = req.getSession();
 		session.removeAttribute("login");
 		session.removeAttribute("email");
 		session.removeAttribute("type");
		session.invalidate();
		
 		
 		resp.sendRedirect("/init.do");
 	}
 	
 }
