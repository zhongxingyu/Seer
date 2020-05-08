 package com.capstonecontrol.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 @SuppressWarnings("serial")
 public class AlertDispatcher extends HttpServlet {
 	private static final Logger log = Logger.getLogger(AlertDispatcher.class
 			.getName());
 
 	static String recipient;
 	static String user;
 	static String warning;
 	
 
 	public static final String PARAM_REGISTRATION_ID = "registration_id";
 
 	public static final String PARAM_DELAY_WHILE_IDLE = "delay_while_idle";
 
 	public static final String PARAM_COLLAPSE_KEY = "collapse_key";
 
 	private static final String UTF8 = "UTF-8";
 
 	String moduleName;
 	String moduleType;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		
 		
 		////Put it in Alerts Table
 		warning = req.getParameter("warning");
 		user = req.getParameter("user");
 		moduleName = req.getParameter("moduleName");
 		moduleType = req.getParameter("moduleType");
 		Date date = new Date();
 		Entity module = new Entity("Alerts");
 		module.setProperty("Warning", warning);
 		module.setProperty("user", user);
 		module.setProperty("moduleName", moduleName);
 		module.setProperty("date", date);
 		module.setProperty("ACK", false);
 
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		datastore.put(module);
 		
 		///put it in events table
 		Key moduleKey = KeyFactory.createKey("user", user);     
         Entity actionmodule = new Entity("moduleEvent", moduleKey);
         actionmodule.setProperty("user", user);
         actionmodule.setProperty("date", date);
         actionmodule.setProperty("moduleName", moduleName);
         actionmodule.setProperty("moduleType",moduleType);
         actionmodule.setProperty("action", "Alert");
        actionmodule.setProperty("value", 0);
 
         datastore =
                 DatastoreServiceFactory.getDatastoreService();
         datastore.put(actionmodule);
 
 		// /query for all user phone numbers
 
 		// The Query interface assembles a query
 		// filtered by user and moduleName
 		Query q = new Query("PhoneNumbers");
 		// filter for events on that day
 		q.addFilter("user", FilterOperator.EQUAL, user);
 		q.addSort("date", SortDirection.DESCENDING);
 
 		PreparedQuery pq = datastore.prepare(q);
 
 		// loop through and send out texts
 		for (Entity result : pq.asIterable()) {
 			String theNumber = (String) result.getProperty("phoneNumber");
 
 			String data = URLEncoder.encode("number", "UTF-8") + "="
 					+ URLEncoder.encode(theNumber, "UTF-8");
 			data += "&" + URLEncoder.encode("moduleName", "UTF-8") + "="
 					+ URLEncoder.encode(moduleName, "UTF-8");
 			data += "&" + URLEncoder.encode("alert", "UTF-8") + "="
 					+ URLEncoder.encode(warning, "UTF-8");
 			// Send data
 			URL url = new URL("http://23.21.229.136/DispatchText.php");
 			URLConnection conn = url.openConnection();
 			conn.setDoOutput(true);
 			OutputStreamWriter wr = new OutputStreamWriter(
 					conn.getOutputStream());
 			wr.write(data);
 			wr.flush();
 			// Get the response
 			BufferedReader rd = new BufferedReader(new InputStreamReader(
 					conn.getInputStream()));
 			wr.close();
 			rd.close();
 
 		}
 
 		// now send C2DM if the user has a registered android device
 		//first find the users auth_token and registrationId if they exist
 		String auth_token = "", registration_Id = "";
 		//grab auth_token first
 		Query query = new Query("C2DMConfig");
 		PreparedQuery authToken = datastore.prepare(query);
 		for (Entity result : authToken.asIterable()) {
 			auth_token = (String) result.getProperty("authToken");
 		}
 		//now grab registrationID
 		String name="";
 		Query query2 = new Query("DeviceInfo");
 		PreparedQuery registrationId = datastore.prepare(query2);
 		for (Entity result : registrationId.asIterable()) {
 			name = (result.getKey().getName());
 			if (!user.contains("@")){
 				//append @gmail.com
 				user = user + "@gmail.com";
 			}
 			boolean checkContainsName =name.contains(user);
 			if (checkContainsName){
 				registration_Id = (String) result.getProperty("deviceRegistrationID");
 			}
 		}
 		try {
 			sendMessage(auth_token, "0", registration_Id, warning + moduleName);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 
 	
 	private void sendMessage(String authToken, String collapseKey, String registrationId, String message)
 		    throws Exception
 		    {
 		        log.info("sending message...");
 		        
 		        URL url = new URL("https://android.apis.google.com/c2dm/send");
 		        HttpURLConnection request = (HttpURLConnection) url.openConnection();
 		        request.setDoOutput(true);
 		        request.setDoInput(true);
 
 		        StringBuilder buf = new StringBuilder();
 		        buf.append("registration_id").append("=").append((URLEncoder.encode(registrationId, "UTF-8")));
 		        buf.append("&collapse_key").append("=").append((URLEncoder.encode(collapseKey, "UTF-8")));
 		        buf.append("&data.message").append("=").append((URLEncoder.encode(message, "UTF-8")));
 		        
 		        request.setRequestMethod("POST");
 		        request.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 		        request.setRequestProperty("Content-Length", buf.toString().getBytes().length+"");
 		        request.setRequestProperty("Authorization", "GoogleLogin auth=" + authToken);
 		        
 		        OutputStreamWriter post = new OutputStreamWriter(request.getOutputStream());
 		        post.write(buf.toString());
 		        post.flush();
 		        
 		        BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
 		        buf = new StringBuilder();
 		        String inputLine;
 		        while ((inputLine = in.readLine()) != null) {
 		            buf.append(inputLine);
 		        }
 		        post.close();
 		        in.close();
 
 		        log.info("response from C2DM server:\n" + buf.toString());
 		        
 		        int code = request.getResponseCode();
 		        log.info("response code: " + request.getResponseCode());
 		        log.info("response message: " + request.getResponseMessage());
 		        if (code == 200) {
 		            //TODO: check for an error and if so, handle
 		            
 		        } else if (code == 503) {
 		            //TODO: check for Retry-After header; use exponential backoff and try again
 		            
 		        } else if (code == 401) {
 		            //TODO: get a new auth token
 		        }
 		    }
 
 }
