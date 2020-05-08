 package com.grademaster.servlets;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
import com.grademaster.logging.ErrorLevel;
import com.grademaster.logging.Logger;

import com.grademaster.data.objects.Assignment;
 
 public class CreateAssignmentServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Create Assignment servlet started.",ErrorLevel.INFO);
 	    
 		Assignment a = new Assignment(new Integer(Globals.getAssignments().getMaxIntId()+1).toString(), req.getParameter("sid"), req.getParameter("name"),req.getParameter("multi"),req.getParameter("pts"),new Date().toString(),req.getParameter("due"),req.getParameter("desc"));
 	
 		Globals.getAssignments().addObject(a);
 		
 		Globals.saveAssignments();
 		
 		if (req.getParameter("redirect")==null || req.getParameter("redirect")=="") {
 			res.sendRedirect("index.do");
 		} else {
 			res.sendRedirect(req.getParameter("redirect"));
 		}
 	}
 }
