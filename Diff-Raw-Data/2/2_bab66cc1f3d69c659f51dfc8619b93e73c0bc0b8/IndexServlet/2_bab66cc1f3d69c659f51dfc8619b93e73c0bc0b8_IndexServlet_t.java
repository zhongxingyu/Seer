 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
 import com.grademaster.data.objects.User;
 import com.grademaster.logging.ErrorLevel;
 import com.grademaster.logging.Logger;
 
 public class IndexServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Login servlet running...",ErrorLevel.INFO);
 	    log.log("Login servlet is not complete yet.",ErrorLevel.WARNING);
 	    boolean loggedIn = (Boolean) req.getSession(true).getAttribute("loggedIn");
	    if (loggedIn==true && req.getAttribute("user")!=null) {
 	    	String redirect="index_misc.jsp";
 	    	String accountType = ((User) req.getSession(true).getAttribute("user")).getUserType();
 	    	if (accountType!=null && accountType=="teacher") {
 	    		redirect = "teacher_index.jsp";
 	    	} else if (accountType!=null && accountType=="student") {
 	    		redirect = "student_index.jsp";
 	    	}
 		    log.log("Index dispatched to: " + redirect);
 	    	RequestDispatcher view = req.getRequestDispatcher(redirect);
 		    view.forward(req, res);
 	    } else {
 	    	res.sendRedirect("login.do");
 	    }
 	    	    
 	}
 }
