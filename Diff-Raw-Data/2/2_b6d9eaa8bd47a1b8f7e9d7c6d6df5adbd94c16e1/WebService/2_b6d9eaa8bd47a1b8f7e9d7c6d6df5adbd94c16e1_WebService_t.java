 package com.madp.meetme.webapi;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.madp.meetme.common.entities.Meeting;
 import com.madp.meetme.common.entities.User;
 
 /**
  * API for communication with MeetMe server
  * 
  * @author esauali 2012-02-16 initial implementation
  * @author esauali 2012-02-28 fixed server URL, javadocs
  *
  */
 public class WebService {
 	private static final String TAG = "WebService";
 	private static final String URL_BASE = "http:///MeetMeServer/rest/";	
 	private static final String URL_POST_MEETING = URL_BASE + "meeting";
 	private static final String URL_GET_MEETING_ALL =  URL_BASE + "meeting/all/%d/%d";
 	private static final String URL_GET_MEETING_USER =  URL_BASE + "meeting/all/%d/%d/%s";
 	private static final String URL_GET_MEETING = URL_BASE + "meeting/%d";		
 	private static final String URL_PUT_USER = URL_BASE + "user";	
 	private Gson gson;
 	private LoggerInterface log;
 	
 	public WebService(LoggerInterface log){		
 		this.gson = new Gson();
 		this.log = log;
 	}
 	
 	/**
	 * Put new meetings to server
 	 * @param meeting
 	 * @return
 	 */
 	public String postMeeting(Meeting meeting) {
 		log.d(TAG, "postMeeting");		
 		return this.post(URL_POST_MEETING, gson.toJson(meeting));
 	}
 	
 	/**
 	 * Fetch all meeting ordered by date
 	 * @param limit_from
 	 * @param limit_to
 	 * @return
 	 */
 	public List<Meeting> getMeetings(int limit_from, int limit_to) {
 		String response = this.get(String.format(URL_GET_MEETING_ALL, limit_from, limit_to), null);		
 		return gson.fromJson(response, new TypeToken<List<Meeting>>(){}.getType());
 	}
 	
 	/**
 	 * Fetch all user meetings (owner or participant) ordered by date
 	 * @param user
 	 * @param limit_from
 	 * @param limit_to
 	 * @return
 	 */
 	public List<Meeting> getUserMeetings(User user,int limit_from, int limit_to) {
 		String response = this.get(String.format(URL_GET_MEETING_USER, limit_from, limit_to, user.getEmail()), null);		
 		return gson.fromJson(response, new TypeToken<List<Meeting>>(){}.getType());
 	}	
 	
 	/**
 	 * Get meetings by id
 	 * @param id
 	 * @return
 	 */
 	public Meeting getMeeting(int id){
 		String response = this.get(String.format(URL_GET_MEETING, id), null);
 		return gson.fromJson(response, Meeting.class);
 	}
 	
 	/**
 	 * Update user location
 	 * @param user - user object must have only email set
 	 * @return
 	 */
 	public boolean updateUser(User user){
 		return this.put(URL_PUT_USER, gson.toJson(user));
 	}
 	
 	private String post(String url, String jsonString){
 		log.d(TAG, "POST "+url+" Data:"+jsonString);
 		HttpPost postMethod = new HttpPost(url);		
 		
 		try {
 			postMethod.setEntity(new StringEntity(jsonString));
 		} catch (UnsupportedEncodingException e) {
 			log.e(TAG, "Could not add entity",e);
 		}		
 		postMethod.setHeader("Content-type", "application/json");
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		String response = null;
 		try {
 			response = httpClient.execute(postMethod, new BasicResponseHandler());			
 		} catch (ClientProtocolException e) {
 			log.e(TAG, "Error in request",e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.e(TAG, "Error in request",e);
 			e.printStackTrace();
 		}		
 		
 		log.d(TAG,"Received response:"+response);
 		if (response == null || response.length() < 1){
 			return null;
 		}	
 		return response;
 	}
 	
 	private String get(String url, String params){
 		log.d(TAG, "GET "+url+" Params:"+params);
 		HttpGet getMethod = new HttpGet(url);		
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		String response = null;
 		try {
 			response = httpClient.execute(getMethod, new BasicResponseHandler());			
 		} catch (ClientProtocolException e) {
 			log.e(TAG, "Error in request",e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.e(TAG, "Error in request",e);
 			e.printStackTrace();
 		}		
 		log.d(TAG,"Received response:"+response);
 		if (response == null || response.length() < 1){
 			return null;
 		}	
 		return response;
 	}
 	
 	private boolean put(String url, String jsonString){
 		log.d(TAG, "POST "+url+" Data:"+jsonString);
 		HttpPut putMethod = new HttpPut(url);		
 		
 		try {
 			putMethod.setEntity(new StringEntity(jsonString));
 		} catch (UnsupportedEncodingException e) {
 			log.e(TAG, "Could not add entity",e);
 		}		
 		putMethod.setHeader("Content-type", "application/json");
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		HttpResponse response = null;		
 		try {			
 			response = httpClient.execute(putMethod);			
 		} catch (ClientProtocolException e) {
 			log.e(TAG, "Error in request",e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.e(TAG, "Error in request",e);
 			e.printStackTrace();
 		} 
 		
 		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
 			log.e(TAG, "Error while updating user location");
 			return false;
 		}
 		
 		return true;
 	}
 }
