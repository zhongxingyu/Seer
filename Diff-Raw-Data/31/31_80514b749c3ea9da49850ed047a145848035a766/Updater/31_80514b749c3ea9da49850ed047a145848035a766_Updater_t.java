 package org.akk.akktuell.Model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.net.Uri;
 import android.util.Log;
 
 public class Updater implements Runnable{
 	
 	private static String AKK_API_ADRESS="";
 	private static String AKK_API_EVENT_NAME="";
 	private static String AKK_API_EVENT_PICTURE_URI="";
 	private static String AKK_API_EVENT_DESCRIPTION="";
 	private static String AKK_API_EVENT_BEGIN_TIME="";
 	
 	private LinkedList<AkkEvent> events;
 	
 	private GregorianCalendar lastTimeUpdated;
 	
 	
 	public Updater(LinkedList<AkkEvent> events, GregorianCalendar lastUpdated) {
 		if (events == null) {
 			//no events in database
 			this.events = new LinkedList<AkkEvent>();
 		} else {
 			this.events = events;
 		}
 		
 		if (lastUpdated == null) {
 			//not yet updated
 			lastTimeUpdated = new GregorianCalendar();
 			lastTimeUpdated.set(0, 1, 1);
 			//this should be before today;)
 		}else {
 			lastTimeUpdated = lastUpdated;
 		}
 	}
 	
 	private void updateEventList() {
 		JSONObject request = new JSONObject();
 		try {
 			request.accumulate("jsonrpc", "2.0");
 			request.accumulate("method", "list");
 		} catch (JSONException e) {
 			Log.d("InfoManager: Updater", "unable to create JSON request");
 			e.printStackTrace();
 		}
 		JSONArray akkEvents = postDataAndGetResponse(request);
 		if (events == null) {
 			Log.d("Updater", "Eventlist update failed");
 			return;
 		}
 		
 		for (int i = 0; ; i++) {
 			try {
 				JSONObject currentObject = akkEvents.getJSONObject(i);
 				String eventName = currentObject.getString(AKK_API_EVENT_NAME);
 				String eventDescription = currentObject.getString(AKK_API_EVENT_DESCRIPTION);
 				//GregorianCalendar eventBeginTime = currentObject.get(AKK_API_EVENT_BEGIN_TIME);
 				//Uri eventPictureUri = new Uri(currentObject.getString(AKK_API_EVENT_PICTURE_URI));
 				//AkkEvent event = new AkkEvent(eventName, eventDescription, eventBeginTime, eventPictureUri);
 				AkkEvent event = new AkkEvent(eventName, eventDescription, null, null);
 				
 				if (!this.events.contains(event)) {
 					this.events.add(event);
 				} else {
 					//let's see if this event got updated
 					for (AkkEvent currentEvent: events) {
 						if (currentEvent.equals(event)) {
 							if (!currentEvent.wasUpdated(event)) {
 								events.remove(currentEvent);
 								events.add(event);
 								break;
 							}
 						}
 					}
 				}
 				
 			} catch (JSONException e) {
 				// this is not a JSONObject or end of array
 				break;
 			}
 		}
 	}
 	
 	private JSONArray postDataAndGetResponse(JSONObject obj) {
 	    // Create a new HttpClient and Post Header
 	    HttpClient httpclient = new DefaultHttpClient();
 	    HttpParams myParams = new BasicHttpParams();
 	    HttpConnectionParams.setConnectionTimeout(myParams, 10000);
 	    HttpConnectionParams.setSoTimeout(myParams, 10000);
 	    HttpResponse response = null;
 
 	    try {
 
 	        HttpPost httppost = new HttpPost();
 	        httppost.setHeader("Content-type", "application/json");
 
 	        StringEntity se = new StringEntity(obj.toString()); 
 	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 	        httppost.setEntity(se); 
 
 	        response = httpclient.execute(httppost);
 
 	        BufferedReader reader;
 	        reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
 			StringBuilder builder = new StringBuilder();
 		    for (String line = null; (line = reader.readLine()) != null;) {
 		        builder.append(line).append("\n");
 		    }
 		    JSONTokener tokener = new JSONTokener(builder.toString());
 		    JSONArray finalResult = new JSONArray(tokener);
 		    return finalResult;
 
 	    } catch (ClientProtocolException e) {
 	    	Log.d("Updater", "Got ClientProtocolException");
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	Log.d("Updater", "Got IOException");
 	    	e.printStackTrace();
 	    } catch (JSONException e) {
 	    	Log.d("Updater", "Got JSONException");
 			e.printStackTrace();
 		}
 
 	    return null;
 	   
 	}
 
 	public boolean updateNeeded() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	private List<AkkEvent> getEventsFromServer() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void run() {
 		this.getEventsFromServer();
 	}
 
 
 
 }
