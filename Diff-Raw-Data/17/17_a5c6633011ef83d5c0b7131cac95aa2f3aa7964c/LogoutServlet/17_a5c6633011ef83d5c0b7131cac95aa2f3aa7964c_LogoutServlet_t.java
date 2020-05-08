 package com.supinfo.supcommerce.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @WebServlet(urlPatterns = { "/logout" })
 public class LogoutServlet extends HttpServlet {
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 	
 		req.logout();
		req.getSession().invalidate();
 		resp.sendRedirect(getServletContext().getContextPath());
 	}
 
 }
