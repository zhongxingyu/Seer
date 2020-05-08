 package com.lordMap.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.labs.repackaged.org.json.JSONArray;
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 import com.lordMap.datastore.DataStore;
 import com.lordMap.models.Land;
 
 public class GetSurroundingServlet extends HttpServlet {
 	/**
 	 * userId=?lat=?lng=?
 	 */
 	private static final long serialVersionUID = 9049311948468914720L;
 	
 	private String userId;
 	private double lat;
 	private double lng;
 	private ArrayList<Land> lands = new ArrayList<Land>();
 	private ArrayList<String> friends = new ArrayList<String>();
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		resp.setContentType("application/json");
 		PrintWriter out = resp.getWriter();
 		DataStore ds = new DataStore();
 		parseReq(req);
 		lands = ds.findLands(userId, lat, lng);
 		friends = ds.showFriends(userId);
 		Collections.sort(friends);
 		String[] rel = new String[lands.size()];
 		getRel(rel);
 		JSONObject results = new JSONObject();
 		JSONArray arr = new JSONArray();
 		int count = 0;
 		for (Land l : lands) {
 			JSONObject r = new JSONObject();
 			try {
 				r.put("id", l.getId());
 				r.put("owner", l.getOwner());
 				r.put("price", l.getPrice());
 				r.put("defence", l.getDefence());
 				r.put("lat0", l.getLats()[0]);
 				r.put("long0", l.getLongs()[0]);
 				r.put("lat1", l.getLats()[1]);
 				r.put("long1", l.getLongs()[1]);
 				r.put("rel", rel[count]);
				r.put("name", l.getName());
				r.put("msg", l.getMsg());
 				count++;
 				arr.put(r);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		try {
 			results.put("results", arr);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		out.print(results);
 		out.flush();
 		out.close();
 	}
 		
 	private void parseReq(HttpServletRequest req) {
 		userId = req.getParameter("userId");
 		String tmp = req.getParameter("lat");		
 		lat = Double.parseDouble(tmp);
 		tmp = req.getParameter("lng");		
 		lng = Double.parseDouble(tmp);
 	}
 	
 	private void getRel(String[] rel) {
 		int count = 0;
 		for (Land land: lands) {
 			if (land.getOwner().equals(userId)) {
 				rel[count] = "own";
 				count++;
 				continue;
 			}
 			if (isFriend(land.getOwner())) {
 				rel[count] = "friend";
 				count++;
 				continue;
 			}
 			rel[count] = "others";
 			count++;
 		}
 	}
 	
 	private boolean isFriend(String userId1) {
 		if (Collections.binarySearch(friends, userId1) >= 0)
 			return true;
 		
 		return false;
 	}
 }
