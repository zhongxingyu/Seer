 package controllers;
 
 import java.text.DecimalFormat;
 import java.util.Arrays;
 
 /*import org.json.JSONObject;*/
 
 import api.Request.CheckAdminkeyRequest;
 import api.Response.CheckAdminkeyResponse;
 import api.Response.GetPollResponse;
 import api.Request.GetPollRequest;
 
 
 /*import Utility.RestClient;*/
 
 import play.mvc.Controller;
 
 public class ManagePoll extends Controller {
 	public static void index(String token, String adminkey) {
 		validation.required(token);
 		validation.required(adminkey);
 		boolean redirect = true;
 		if (validation.hasErrors()) {
 			token = session.get("token");
 			adminkey = session.get("adminkey");
 			redirect = false;
 		}
 		
 		try {
 			/*boolean correct = RestClient.getInstance().checkAdminkey(token, adminkey);*/
 			/*JSONObject correctJSON = new JSONObject(APIClient.getInstance().send(new api.Request.CheckAdminkey(token, adminkey)));*/
 			
 			CheckAdminkeyResponse response = (CheckAdminkeyResponse) APIClient.getInstance().send(new CheckAdminkeyRequest(token, adminkey));
 			
 			/*boolean correct = correctJSON.getBoolean("correct");*/
 			boolean correct = response.bool;
 			if (correct) {
 				if (redirect) {
 					session.put("token", token);
 					session.put("adminkey", adminkey);
 					ManagePoll.index(null, null);
 				} else {
 					render(token, adminkey);
 				}
 			}
 		} catch (Exception e) {
 		}
 		Application.index();
 	}
 
 	public static void activate() {
 		String token = session.get("token");
 		String adminkey = session.get("adminkey");
 
 		String duration = "";
 		String durationString = "00:00";
 		// Get the duration from the server
 		String res = null;
 		try {
 			/*JSONObject questionJSON = new JSONObject(RestClient.getInstance().getQuestion(token));*/
 			
 			/* ?? RestClient.getInstance().getQuestion here with 'token', in JoinPoll with 'id ?? */
 			/*JSONObject questionJSON = new JSONObject(APIClient.getInstance().send(new api.Request.getQuestion(token)));
 
 			duration = questionJSON.getString("duration");*/
 			
 			GetPollResponse response = (GetPollResponse) APIClient.getInstance().send(new GetPollRequest(token));
 			duration = response.duration;
 			// Parse the duration and turn it into minutes and seconds
 			int dur = Integer.parseInt(duration);
 			int m = (int) Math.floor(dur / 60);
 			int s = dur - m * 60;
 
 			// Add leading zeros and make the string.
 			char[] zeros = new char[2];
 			Arrays.fill(zeros, '0');
 			DecimalFormat df = new DecimalFormat(String.valueOf(zeros));
 
 			durationString = df.format(m) + ":" + df.format(s);
 		} catch (Exception e) {
 			System.err.println("Error parsing the response from the service layer: '"+res+"'");
 			e.printStackTrace();
 		}
 
 		render(token, adminkey, duration, durationString);
 	}
 
 	public static void activateSubmit(String minutes, String seconds) {
 		String token = session.get("token");
 		String adminkey = session.get("adminkey");
 		
 		int s = 0;
 		int m = 0;
 
 		validation.required(minutes);
 		validation.required(seconds);
 		if (!validation.hasErrors()) {
 			try {
 				s = Integer.parseInt(seconds);
 				if (s < 0) {
 					s = 0;
 				}
 				
 				m = Integer.parseInt(minutes);
 				if (m < 0) {
 					m = 0;
 				}
 
 				int duration = s + m * 60;
 
 				/*RestClient.getInstance().activate(token, adminkey, duration);*/
				APIClient.getInstance().send(new api.Request.activatePoll(token, adminkey, duration));
 			} catch (Exception e) {
 			}
 		}
 
 		activate();
 	}
 
 	public static void edit() {
 		String token = session.get("token");
 		String adminkey = session.get("adminkey");
 		render(token, adminkey);
 	}
 
 	public static void statistics() {
 		String token = session.get("token");
 		String adminkey = session.get("adminkey");
 		render(token, adminkey);
 	}
 }
