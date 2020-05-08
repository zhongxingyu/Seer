 package com.tritowntim.simple;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class RedirectorServlet extends HttpServlet { 
 	
 	private static final long serialVersionUID = 1L;
 
 	private String p(String text) {
 		return "<p>" + text + "</p>";
 	}
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		
		response.sendRedirect("/redirected.html");
 		
 	}
 	
 }
