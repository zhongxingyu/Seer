 package edu.byu.isys413.data.actions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONObject;
 
 import edu.byu.isys413.data.web.Action;
 
 /**
  * Logout enables customers to log out of the web system. 
  *
  */
 public class Logout implements Action {
 	
 	/** No-arg constructor per Dr. Albrecht's instruction in Action.java */
 	public Logout() {}
 
 	/* @see edu.byu.isys413.data.web.Action#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)*/
 	@Override
 	public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		
 		request.getSession().removeAttribute("cust");
 		request.getSession().invalidate();
 		
		if(request.getParameter("format").equals("json")) {
 			JSONObject json = new JSONObject();
 			json.put("status", "success");
 			request.setAttribute("json", json);
 			return "json.jsp";
 		} else {
 			return "index.jsp";
 		}
 	}
 	
 }
