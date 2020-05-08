 package controllers;
 
 import java.text.DecimalFormat;
 import java.util.Arrays;
 
 import models.Vote;
 
 /*import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;*/
 import api.Request.GetPollRequest;
 import api.Response.GetPollResponse;
 import api.Request.GetResultsRequest;
import api.Request.GetResultsResponse;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonObject;
 
 import play.mvc.Controller;
 /*import Utility.RestClient;*/
 
 public class JoinPoll extends Controller {
 	public static void index(String id) throws JsonParseException {
 		if (request.url.contains("joinpoll")) {
 			redirect("/" + id);
 		}
 		try {
 			/*JSONObject questionJSON = new JSONObject(RestClient.getInstance().getQuestion(id));*/
 			/* ?? RestClient.getInstance().getQuestion here with 'id', in ManagePoll with 'token' ?? */
 			/*JSONObject questionJSON = new JSONObject(APIClient.getInstance().send(new api.Request.getQuestion(id)));
 
 			String token = questionJSON.getString("token");
 
 			String questionID = questionJSON.getString("questionID");
 			String question = questionJSON.getString("question");
 			JSONArray answersArray = questionJSON.getJSONArray("answers");
 			// String multipleAllowed =
 			// questionJSON.getString("multipleAllowed");
 			String duration = questionJSON.getString("duration");*/
 			
 			GetPollResponse response = (GetPollResponse) APIClient.getInstance().send(new GetPollRequest(id));
 
 			String token = response.token;
 			long pollid = response.id;
 			String question = response.question;
 			JsonArray answersArray = response.answersArray;
 			String duration = response.duration;
 			
 
 			render(id, token, pollid, question, answersArray, duration);
 		} catch (JsonParseException e) {
 			nopoll(id);
 		}
 	}
 
 	public static void submit(String token, String questionID, String answer)
 			throws JsonParseException {
 
 		validation.required(token);
 		validation.match(token, "^\\d+$");
 		validation.required(questionID);
 		validation.match(questionID, "^\\d+$");
 		validation.required(answer);
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			index(token);
 			return;
 		}
 
 		Vote v = new Vote();
 		v.token = Integer.parseInt(token);
 		v.questionID = Integer.parseInt(questionID);
 		v.answers = new String[] { answer };
 		v.rensponderID = request.remoteAddress + session.getId();
 
 		/*RestClient.getInstance().vote(v);*/
 		APIClient.getInstance().send(new api.Request.vote(v));
 
 		success(token);
 	}
 
 	public static void success(String token) {
 		String question = null;
 		JsonArray answersArray = null;
 		String duration = null;
 
 		try {
 			/*JSONObject questionJSON = new JSONObject(RestClient.getInstance().getQuestion(token));*/
 			/*JSONObject questionJSON = new JSONObject(APIClient.getInstance().send(new api.Request.getQuestion(token)));
 			
 			question = questionJSON.getString("question");
 			answersArray = questionJSON.getJSONArray("answers");
 			duration = questionJSON.getString("duration");*/
 			
 			GetPollResponse response = (GetPollResponse) APIClient.getInstance().send(new GetPollRequest(token));
 			
 			question = response.question;
 			answersArray = response.answersArray;
 			duration = response.duration;
 			
 		} catch (Exception e) {
 			try {
 				// Most like the poll has been inactivated, so we need the
 				// results instead
 				/* JSONObject resultJSON = new JSONObject(RestClient.getInstance().getResults(token, null)); */
 				/*JSONObject resultJSON = new JSONObject(APIClient.getInstance().send(new api.Request.getResults(token, null)));
 
 				question = resultJSON.getString("question");
 				answersArray = resultJSON.getJSONArray("answers");*/
 				GetResultsResponse response = (GetResultsResponse) APIClient.getInstance().send(new GetResultsRequest(token, null));
 
 				question = response.question;
 				answersArray = response.answersArray;
 				
 				
 				duration = "0";
 			} catch (Exception e2) {
 			}
 		}
 		
 		String durationString = "00:00";
 		// Parse the duration and turn it into minutes and seconds
 		int dur = Integer.parseInt(duration);
 		int m = (int) Math.floor(dur / 60);
 		int s = dur - m * 60;
 
 		// Add leading zeros and make the string.
 		char[] zeros = new char[2];
 		Arrays.fill(zeros, '0');
 		DecimalFormat df = new DecimalFormat(String.valueOf(zeros));
 
 		durationString = df.format(m) + ":" + df.format(s);
 
 		render(token, question, answersArray, duration, durationString);
 	}
 
 	public static void nopoll(String token) {
 		render(token);
 	}
 }
