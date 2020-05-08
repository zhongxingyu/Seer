 package com.grademaster.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
import com.eakjb.EakjbData.IDataStructure;
import com.eakjb.EakjbData.DataObjects.TextDataObject;
import com.eakjb.EakjbData.Logging.*;
 
 public class SetUserPropertyServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Set User Property Servlet Started.",ErrorLevel.INFO);
 	    log.log("Property editor servlet not finished.",ErrorLevel.WARNING);
 		
 	    boolean loggedIn = (Boolean) req.getSession().getAttribute("loggedIn");
 	    if (loggedIn) {
	    	IDataStructure user = (IDataStructure) req.getSession().getAttribute("user");
	    	user.set("showWelcome", new TextDataObject("showWelcome",String.valueOf(Boolean.parseBoolean(req.getParameter("value")))));
	    	try {
				Globals.getInterface("Users").dumpData();
			} catch (Exception e) {
				log.log("Problem saving users:",ErrorLevel.ERROR);
				log.log(e);
			}
 	    }
 	    if (req.getParameter("redirect")==null || req.getParameter("redirect")=="") {
 	    	res.sendRedirect("index.do");
 	    } else {
 	    	res.sendRedirect(req.getParameter("redirect"));
 	    }
 	}
 }
