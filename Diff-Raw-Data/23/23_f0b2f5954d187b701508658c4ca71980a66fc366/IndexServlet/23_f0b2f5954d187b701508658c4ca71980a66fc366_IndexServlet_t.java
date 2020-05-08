 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
 import com.eakjb.EakjbData.Logging.*;
 import com.eakjb.EakjbData.IDataStructure;
 
 public class IndexServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Login servlet running...",ErrorLevel.INFO);
 	    log.log("Login servlet is not complete yet.",ErrorLevel.WARNING);
 	    boolean loggedIn = false;
 	    try {
 	    	loggedIn  = (Boolean) req.getSession(true).getAttribute("loggedIn");
 	    } catch (Exception e) {
 	    	log.log("No loggedin session data for user.",ErrorLevel.WARNING);
 	    }
 	    if (loggedIn==true) {
 	    	log.log("User is logged in.");
	    	String redirect="";
 	    	String accountType = ((IDataStructure) req.getSession(true).getAttribute("user")).get("type").getTextValue();
 	    	if (accountType!=null && accountType.equals("teacher")) {
	    		redirect = "teacher/teacher_index.jsp";
 	    	} else if (accountType!=null && accountType.equals("student")) {
	    		redirect = "student/student_index.jsp";
	    	} else {
	    		throw new ServletException("Corrupt User!");
 	    	}
 		    log.log("Index dispatched to: " + redirect);
 	    	RequestDispatcher view = req.getRequestDispatcher(redirect);
 		    view.forward(req, res);
 	    } else {
 	    	res.sendRedirect("login.do");
 	    }
 	    	    
 	}
 }
