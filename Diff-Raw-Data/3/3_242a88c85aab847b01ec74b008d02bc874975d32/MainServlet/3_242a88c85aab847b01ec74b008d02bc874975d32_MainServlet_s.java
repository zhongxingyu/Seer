 package edu.columbia.e6998.cloudexchange.client;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import edu.columbia.e6998.cloudexchange.aws.AWSCodes;
 import edu.columbia.e6998.cloudexchange.toolkit.GenericToolkit;
 
 import com.google.appengine.repackaged.org.json.JSONArray;
 import com.google.appengine.repackaged.org.json.JSONException;
 import com.google.appengine.repackaged.org.json.JSONObject;
 
 @SuppressWarnings("serial")
 public class MainServlet extends HttpServlet {
 
 	/* our destination jsp to render stuff */
 	final String destination = "/views/main.jsp";
 	final int NUM_DAYS = 7;
 	
 
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		RequestDispatcher rd;
 		UserService userService = UserServiceFactory.getUserService();
 
 		/*verify there is a user*/
 		if (userService.getCurrentUser() != null) {
 
 			
 			/* set unique channel id for user */
 			ChannelService channelService = ChannelServiceFactory
 					.getChannelService();
 			String userId = userService.getCurrentUser().getUserId();
 			String token = channelService.createChannel(userId);
 
 			
 			/*prep initial contract data*/
 			String[] defaults = { AWSCodes.Region.US_EAST.toString(), 
 								  AWSCodes.Zone.US_EAST1A.toString(), 
 								  AWSCodes.OS.LINUX.toString(), 
 								  AWSCodes.InstanceType.MICRO.toString() };
 			
 			ArrayList<String> key_list = new ArrayList<String>(NUM_DAYS);
 			ArrayList<String> dates_list = new ArrayList<String>(NUM_DAYS);
 			ArrayList<String[][]> contracts_list = new ArrayList<String[][]>(NUM_DAYS);
 			
 			JSONArray contracts_jsondata = new JSONArray();
 			
 			Populate_Contract_Data(defaults, key_list, dates_list, 
 									contracts_list, contracts_jsondata);
 			
 			
 			/* pass vars to jsp (see destination address) */
 			req.setAttribute("token", token);
 			req.setAttribute("defaults", defaults);
 			req.setAttribute("keys", key_list);
 			req.setAttribute("dates", dates_list);
 			req.setAttribute("contracts", contracts_list);
 			
 			req.setAttribute("contracts_json", contracts_jsondata);
 			req.setAttribute("dates_json", new JSONArray(dates_list));
 
 
 			
 			rd = getServletContext().getRequestDispatcher(destination);
 
 			try {
 				rd.forward(req, resp);
 			} catch (ServletException e) {
 				e.printStackTrace();
 			}
 		} else {
 			/*no signed in user, something went wrong, 
 			 * user maybe directly tried to access main,
 			 * whatever redirect back to index*/
 			
 
 			resp.sendRedirect("/");
 		}
 
 	}
 	
 	
 	/* this is used when a user selects a new region/os/zone/instances
 	 * the client sends a POST request containing the selection
 	 * data is queried and sent over
 	 * then dynamically repopulated via javascript on the client end.
 	 */
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		GenericToolkit gt = new GenericToolkit();
 
 		String msg_type = req.getParameter("msg");
 
 		
 		/*get block*/
 		if(msg_type.equals("update")) {
 			String region = req.getParameter("data[region]");
 			String os = req.getParameter("data[os]");
 			String zone = req.getParameter("data[zone]");
 			String instance = req.getParameter("data[instance]");
 		
 			System.out.println("msg recvd:" + msg_type + 
 					"[" + region + "|" + os + "|" + zone + "|" + instance + "]");
 			
 			String[] request = {region, zone, os, instance };
 			
 			ArrayList<String> key_list = new ArrayList<String>(NUM_DAYS);
 			ArrayList<String> dates_list = new ArrayList<String>(NUM_DAYS);
 			ArrayList<String[][]> contracts_list = new ArrayList<String[][]>(NUM_DAYS);
 
 			JSONArray contracts_jsondata = new JSONArray();
 
 			Populate_Contract_Data(request, key_list, dates_list, 
 						contracts_list, contracts_jsondata);
 
 		
 			try {
 				JSONObject out = new JSONObject();
 				out.put("contract_data", contracts_jsondata);
 				out.put("dates_data", dates_list);
 				resp.getWriter().println(out.toString());
 				
 			} catch (JSONException e) {
 
 				e.printStackTrace();
 			}
 			
 
 			
 		}
 		
 		
 		
 	}
 	
 	
 	
 	private void Populate_Contract_Data(String[] defaults,
 										ArrayList<String> key_list,
 										ArrayList<String> dates_list,
 										ArrayList<String[][]> contracts_list,
 										JSONArray contracts_jsondata) {
 		
 		Calendar day = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd");
 
 		GenericToolkit gt = new GenericToolkit();
 
 		
 		for (int d = 0; d < NUM_DAYS; d++) {
 			
 			String key = gt.generateProfileKey( defaults[0], 
 												defaults[1], 
 												defaults[2], 
 												defaults[3], 
 										   	   	day.getTime());
 
 			
 			key_list.add(key);
 			//TODO: calls some toolkit function to get real array using key
 			//TODO: need to prune first day for time (this seems like model logic)
 			//TODO: i'm going to separate buyers/sellers, easier to render
 			String formatted_date = sdf.format(day.getTime());
 			
 			String[][] results = gt.getBidsOffers(key);
 			
 			dates_list.add(formatted_date);
 			contracts_list.add(results);
 
 			/*json populate*/
 			try {
 				JSONObject contract_hours = new JSONObject();
 				contract_hours.put(key, results);
 				contracts_jsondata.put(contract_hours);
 				
 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 			day.add(Calendar.DAY_OF_YEAR, 1);
 		}
 		
 	}
 	
 
 }
