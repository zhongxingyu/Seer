 package com.dierkers.schedule.api;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.dierkers.schedule.ScheduleServer;
 import com.dierkers.schedule.action.ActionType;
 import com.dierkers.schedule.tools.http.URLConn;
 
 public class ScheduleAPIAdd extends HttpServlet {
 
 	private static final long serialVersionUID = -3850013250150845300L;
 	private ScheduleServer ss;
 
 	public ScheduleAPIAdd(ScheduleServer ss) {
 		this.ss = ss;
 	}
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
 		// Check owner
 		String accessToken = req.getParameter("access_token");
 
 		if (accessToken == null || accessToken.trim().equals("") || accessToken.contains("&")) {
 			resp.getWriter().println("Access token needed");
 			return;
 		}
 
 		String owner = null;
 		try {
 			String fbMe = URLConn.getPage("https://graph.facebook.com/me/?fields=id&access_token=" + accessToken);
 
 			JSONObject fbObj = new JSONObject(fbMe);
 			owner = fbObj.getString("id");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (owner == null || owner.trim().equals("") || !owner.equals("1105870286")) {
 			resp.getWriter().println("Invalid access token");
 			return;
 		}
 
 		String typeString = req.getParameter("type");
 
 		if (typeString == null) {
 			resp.getWriter().println("Invalid type");
 			return;
 		}
 
 		int type = Integer.valueOf(typeString);
 
 		ArrayList<String> parametersToCopy = new ArrayList<String>();
 
 		switch (type) {
 		case ActionType.ERROR_PRINT:
 			parametersToCopy.add("msg");
 			break;
 		case ActionType.FACEBOOK_MESSAGE:
 			parametersToCopy.add("to");
 			parametersToCopy.add("msg");
 			break;
 		case ActionType.SMS:
 		case ActionType.CALL:
 			parametersToCopy.add("to");
 			parametersToCopy.add("msg");
 			break;
 		case ActionType.MAIL:
 			parametersToCopy.add("to");
 			parametersToCopy.add("subj");
 			parametersToCopy.add("msg");
 			break;
 		case ActionType.CLASS_CHECKER:
 			parametersToCopy.add("username");
 			parametersToCopy.add("password");
 			parametersToCopy.add("to");
 			break;
 		default:
 			resp.getWriter().println("Invalid type in switch. Add to ScheduleAPIAdd, or if it's there, remember to put a break statement.");
 			return;
 		}
 
 		JSONObject obj = new JSONObject();
 
 		for (String param : parametersToCopy) {
 			try {
 				obj.put(param, req.getParameter(param));
 			} catch (JSONException e) {
 				System.err.println("JSON exception copying parameters");
 				e.printStackTrace();
 			}
 		}
 
 		int time;
 		String timeString;
 		if ((timeString = req.getParameter("time")) != null) {
 			time = Integer.parseInt(timeString);
 		} else {
 			time = 1351913467;
 			System.err.println("No time specified, using default time which means execution *now*");
 		}
 
 		// Need to fix the SQL bug here.
 
 		ss.db().update("INSERT INTO schedules (id, type, owner, time, data, processed) VALUES ('" + randomUUID()
				+ "', " + type + ",'" + owner + "', " + time + ", '" + obj.toString().replace("'", "\'") + "', 'f')");
 
 	}
 
 	public static String randomUUID() {
 		return UUID.randomUUID().toString();
 	}
 
 }
