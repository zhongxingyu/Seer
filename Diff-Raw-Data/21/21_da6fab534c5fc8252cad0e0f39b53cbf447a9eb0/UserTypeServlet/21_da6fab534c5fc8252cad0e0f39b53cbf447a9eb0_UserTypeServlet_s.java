 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
 import com.grademaster.auth.Authenticator;
import com.grademaster.logging.ErrorLevel;
import com.grademaster.logging.Logger;
 
 public class UserTypeServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("User type lookup Servlet Started.",ErrorLevel.INFO);
 		
	    Authenticator auth = new Authenticator();
 	    res.getWriter().print(auth.typeUser(req.getParameter("uname")));
 	}
 }
