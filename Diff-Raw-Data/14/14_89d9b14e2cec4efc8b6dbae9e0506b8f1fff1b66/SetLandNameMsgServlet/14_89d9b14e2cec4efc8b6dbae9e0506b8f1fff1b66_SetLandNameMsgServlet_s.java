 package com.lordMap.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 import com.lordMap.datastore.DataStore;
 
 public class SetLandNameMsgServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8765350503960134892L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		resp.setContentType("application/json");
 		PrintWriter out = resp.getWriter();
 		JSONObject result = new JSONObject();
 
		String landId = req.getParameter("landId");
 		String name = req.getParameter("name");
 		String msg = req.getParameter("msg");
 		
 		DataStore ds = new DataStore();
 		ds.setLandNameMsg(landId, name, msg);
 		
 		try {
 			result.put("result", "yes");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		out.print(result);
 		out.flush();
 		out.close();
 		
 		
 	}
 }
