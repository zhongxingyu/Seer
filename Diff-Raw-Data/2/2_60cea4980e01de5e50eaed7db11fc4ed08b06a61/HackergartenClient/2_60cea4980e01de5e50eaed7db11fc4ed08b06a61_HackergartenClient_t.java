 package net.hackergarten.android.app.client;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 
 import android.content.ContentValues;
 import android.content.Entity;
 import android.util.Log;
 
 import net.hackergarten.android.app.model.Event;
 import net.hackergarten.android.app.model.User;
 
 /**
  * Handles all the communication to the Hackergarten server. All method calls
  * start new threads for the Http communication.
  * 
  * @author asocaciu
  * 
  */
 public class HackergartenClient {
 
 	private static final String BASE_URL = "http://hackergarten-register.appspot.com";
 	
 //	private static final String BASE_URL = "http://192.168.3.136:8888";
 	
 	
 	private static final String TAG = "HackergartenClient";
 
 	private interface ResponseProcessor<T> {
 		T processResponse(HttpResponse response) throws IllegalStateException,
 				IOException, JSONException;
 	}
 
 	private HttpClient httpClient;
 
 	public HackergartenClient() {
 		httpClient = new DefaultHttpClient();
 	}
 
 	/**
 	 * Register a new User
 	 * 
 	 * @param user
 	 *            filled user object
 	 * @param callback
 	 */
 	public void registerUser(User user, AsyncCallback<Void> callback) {
 		Log.d(TAG, "registerUser " + user);
 		HttpPost post = new HttpPost(BASE_URL + "/user/");
 		post.setHeader("Content-Type", "text/plain");//GAE workaround
 		HttpMapper.map(user, post);
 		requestAsynchronously(callback, post, null);
 	}
 
 	/**
 	 * Initiate a new Hackergarten event
 	 * 
 	 * @param event
 	 * @param callback
 	 */
 	public void addEvent(Event event, AsyncCallback<Void> callback) {
 		Log.d(TAG, "addEvent " + event);
 		HttpPost post = new HttpPost(BASE_URL + "/event/");
 		post.setHeader("Content-Type", "text/plain");//GAE workaround
 		HttpMapper.map(event, post);
 		requestAsynchronously(callback, post, null);
 	}
 
 	/**
 	 * List upcoming events based on current time
 	 * 
 	 * @param callback
 	 */
 	public void listUpcomingEvents(AsyncCallback<List<Event>> callback) {
 		HttpGet get = new HttpGet(BASE_URL + "/event/");
 		long utcTimeMillis = DateUtils.getUTCTimeMillis();
 		get.getParams().setParameter(HttpMapper.TIME,
 				utcTimeMillis);
 		Log.d(TAG, "listUpcomingEvents " + utcTimeMillis);
 		requestAsynchronously(callback, get,
 				new ResponseProcessor<List<Event>>() {
 					public List<Event> processResponse(HttpResponse response)
 							throws IllegalStateException, IOException,
 							JSONException {
 						return HttpMapper.mapToEventList(response);
 					}
 				});
 	}
 
 	/**
 	 * Check in a specific user to a specific event
 	 * 
 	 * @param userEmail
 	 * @param eventId
 	 * @param callback
 	 */
 	public void checkInUser(String userEmail, String eventId,
 			AsyncCallback<Void> callback) {
 		Log.d(TAG, "checkInUser " + userEmail + ", " + eventId);
 		HttpPost post = new HttpPost(BASE_URL + "/checkin/");
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		nameValuePairs.add(new BasicNameValuePair(HttpMapper.EVENT_ID, eventId));
 		nameValuePairs.add(new BasicNameValuePair(HttpMapper.EMAIL, userEmail)); 
 		
 		post.setHeader("Content-Type", "text/plain");//GAE workaround
 		try {
 			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 		requestAsynchronously(callback, post, null);
 	}
 
 	/**
 	 * List the top x users ordered by number of checkins descending
 	 * 
 	 * @param callback
 	 */
 	public void listHallOfFame(AsyncCallback<List<User>> callback) {
 		Log.d(TAG, "listHallOfFame");
 		HttpGet get = new HttpGet(BASE_URL + "/user/");
 		requestAsynchronously(callback, get,
 				new ResponseProcessor<List<User>>() {
 					public List<User> processResponse(HttpResponse response)
 							throws IllegalStateException, IOException,
 							JSONException {
 						return HttpMapper.mapToUserList(response);
 					}
 				});
 	}
 
 	/**
 	 * Release all connections
 	 */
 	public void close() {
 		Log.d(TAG, "close");
 		httpClient.getConnectionManager().shutdown();
 	}
 
 	private <V> void requestAsynchronously(final AsyncCallback<V> callback,
 			final HttpUriRequest request,
 			final ResponseProcessor<V> responseProcessor) {
 		Thread thread = new Thread() {
 			public void run() {
 				try {
 					Log.d(TAG, "requesting asynchronously " + request.getURI());
 					HttpResponse response = httpClient.execute(request);
 					int statusCode = response.getStatusLine().getStatusCode();
 					Log.d(TAG, "got response code " + statusCode);
 					if (statusCode == 200) {
 						V result = null;
 						if (responseProcessor != null) {
 							Log.d(TAG, "processing response");
 							result = responseProcessor
 									.processResponse(response);
 						}
 						Log.d(TAG, "onSuccess");
 						callback.onSuccess(result);
 					} else {
 						Log.d(TAG, "onFailure");
						callback.onFailure(new Throwable("Unexpected status code: " + statusCode));
 					}
 					if (response.getEntity() != null) {
 						response.getEntity().consumeContent();
 					}
 				} catch (ClientProtocolException e) {
 					callback.onFailure(e);
 				} catch (IOException e) {
 					callback.onFailure(e);
 				} catch (IllegalStateException e) {
 					callback.onFailure(e);
 				} catch (JSONException e) {
 					callback.onFailure(e);
 				}
 			}
 		};
 		thread.start();
 	}
 
 }
 
