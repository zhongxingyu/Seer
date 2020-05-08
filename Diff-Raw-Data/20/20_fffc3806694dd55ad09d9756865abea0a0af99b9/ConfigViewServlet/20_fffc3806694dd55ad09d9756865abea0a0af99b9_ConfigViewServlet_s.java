 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
import com.grademaster.data.objects.Config;
import com.grademaster.logging.ErrorLevel;
import com.grademaster.logging.Logger;
 
 public class ConfigViewServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Config View Servlet Started.",ErrorLevel.INFO);
 	    
		Globals.updateConfig();
	    Config config = Globals.getConfig();
 	    
 	    req.setAttribute("config", config);
 	    
 	    RequestDispatcher view = req.getRequestDispatcher("ViewConfig.jsp");
 	    view.forward(req, res);
 	}
 }
