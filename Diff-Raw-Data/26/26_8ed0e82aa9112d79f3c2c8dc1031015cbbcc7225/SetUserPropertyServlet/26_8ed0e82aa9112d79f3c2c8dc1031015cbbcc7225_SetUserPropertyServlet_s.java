 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
import com.grademaster.data.objects.User;
import com.grademaster.logging.ErrorLevel;
import com.grademaster.logging.Logger;
 
 public class SetUserPropertyServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Set User Property Servlet Started.",ErrorLevel.INFO);
 	    log.log("Property editor servlet not finished.",ErrorLevel.WARNING);
 		
 	    boolean loggedIn = (Boolean) req.getSession().getAttribute("loggedIn");
 	    if (loggedIn) {
	    	User user = (User) req.getSession().getAttribute("user");
	    	user.setShowWelcomeMessage(Boolean.parseBoolean(req.getParameter("value")));
	    	Globals.saveUsers();
 	    }
 	    if (req.getParameter("redirect")==null || req.getParameter("redirect")=="") {
 	    	res.sendRedirect("index.do");
 	    } else {
 	    	res.sendRedirect(req.getParameter("redirect"));
 	    }
 	}
 }
