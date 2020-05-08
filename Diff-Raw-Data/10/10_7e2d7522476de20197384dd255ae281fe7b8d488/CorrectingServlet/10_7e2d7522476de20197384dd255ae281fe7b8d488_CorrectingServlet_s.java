 package org.sc.textcorrect.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URLDecoder;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.sc.textcorrect.RuleFile;
 
 public class CorrectingServlet extends HttpServlet {
 	
 	private RuleFile rules;
 	
 	public CorrectingServlet(RuleFile rf) { 
 		rules = rf;
 	}
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		String text = URLDecoder.decode(request.getParameter("text"), "UTF-8");
 		String corrected = rules.rewrite(text);
 		
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.setContentType("text");
 		PrintWriter pw = response.getWriter();
 		pw.println(corrected);
 	}
 }
