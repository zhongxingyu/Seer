 package com.grademaster.servlets;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.grademaster.Globals;
 import com.eakjb.EakjbData.IDataStructure;
 import com.eakjb.EakjbData.IDataInterface;
 import com.eakjb.EakjbData.Logging.*;
 
 public class ConfigViewServlet extends HttpServlet {
 	private static final long serialVersionUID = -9088365072065846961L;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		Logger log = Globals.getLogger();
 		
 	    log.log("Config View Servlet Started.",ErrorLevel.INFO);
 	    
 		IDataStructure config=null;
 		
 		try {
 			HashMap<String,Object> p = Globals.getProps();
 			for (String key : p.keySet()) {
 				log.log(key+": "+p.get(key));
 			}
 			IDataInterface i = (IDataInterface) p.get("Config.interface");
			config = (IDataStructure) ((IDataStructure) i.getData()).get("config");
 		} catch (Exception e) {
 			log.log(e);
 		}
 	    
 	    req.setAttribute("config", config);
 	    
 	    RequestDispatcher view = req.getRequestDispatcher("ViewConfig.jsp");
 	    view.forward(req, res);
 	}
 }
