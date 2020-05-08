 package com.mistypanda.ultimatescheduler.DBAccess;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.net.ParseException;
 import com.mistypanda.ultimatescheduler.Event;
 
 
 /**
  * This class is used as the main work horse between the app and the external
  * database. It will start up a background thread and will access the database 
  * accordingly. In order to access the information pulled from the database the 
  * wrapper classes need to call methods of the DBHelper.
  * @author Senai Mesfin
  *
  */
 public class DBHelper {
 	
 	
 	/** This method accesses the main external database and retrieves an event by ID.
 	 * @param eventID - The int ID for a specific event.
 	 * @return Returns the newly created event.
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 * @throws TimeoutException
 	 */
 	public static Event getEventByEventID(int eventID) throws InterruptedException, ExecutionException, TimeoutException{
 		Event event = null;
 		FetchTask task = new FetchTask();
 		
 		// FIGURE OUT HOW TO ACCESS STRING RESOURCE
 		//task.execute(R.string.get_event);
 		
 		// Start background thread to access the database through php
 		task.execute("http://www.users.csbsju.edu/~symesfin/mistypanda/getEvent.php?id="+eventID);
 		task.get(2000, TimeUnit.MILLISECONDS);
 		String result = task.getData();
 		
 		// Parse result into manageable variables and create new event
 		try{
 			JSONArray dataArray = new JSONArray(result);
 			JSONObject json_data=null;
 			
 			for(int i=0; i<dataArray.length(); i++){
 				json_data = dataArray.getJSONObject(i);
 				
 				int eID = json_data.getInt("eID");
 				String eventName = json_data.getString("eName");
 				String location = json_data.getString("eLocation");
 				String host = json_data.getString("eHost");
 				DateTime startDate = parseDate(json_data.getString("eStartDate"));
 				DateTime endDate = parseDate(json_data.getString("eEndDate"));
 				String info = json_data.getString("eInfo");
 				int version = json_data.getInt("eVersion");
 				
 				event = new Event(eID, eventName, location, host, startDate, endDate, info, version);
 			}
 		}
 		catch(JSONException je){
 			System.out.println(je.toString());
 		}
 		catch(ParseException pe){
 			System.out.println("Date string is not in the correct format: " + pe.toString());
 		}
 
 		return event;
 	}
 	
 	public static List<Event> getAllEvents() throws InterruptedException, ExecutionException, TimeoutException{
 		List<Event> eventList = new ArrayList<Event>();
 		FetchTask task = new FetchTask();
 		Event event = null;
 		
 		// Start background thread to access the database through php
 		task.execute("http://www.users.csbsju.edu/~symesfin/mistypanda/getAllEvents.php");
 		task.get(3000, TimeUnit.MILLISECONDS);
 		String result = task.getData();
 		
 		// Parse results into manageable variables and events. Then add them to event list.
 		try{
 			JSONArray dataArray = new JSONArray(result);
 			JSONObject json_data = null;
 			
 			for(int i=0; i<dataArray.length(); i++){
 				json_data = dataArray.getJSONObject(i);
 				
 				int eID = json_data.getInt("eID");
 				String eventName = json_data.getString("eName");
 				String location = json_data.getString("eLocation");
 				String host = json_data.getString("eHost");
 				DateTime startDate = parseDate(json_data.getString("eStartDate"));
 				DateTime endDate = parseDate(json_data.getString("eEndDate"));
 				String info = json_data.getString("eInfo");
 				int version = json_data.getInt("eVersion");
 				
 				event = new Event(eID, eventName, location, host, startDate, endDate, info, version);
				eventList.add(event);
 			}
 		}
 		catch(JSONException je){
 			System.out.println(je.toString());
 		}
 		catch(ParseException pe){
 			System.out.println("Date string is not in the correct format: " + pe.toString());
 		}
 		
 		// Return the list of all events in the external database.
 		return eventList;
 	}
 
 	
 	/** Parses a string in the format of a MySQL DATETIME and turns it into a DateTime.
 	 * @param stringDate - The date of a MySQL type DATETIME in string format.
 	 * @return The date in the form of DateTime.
 	 */
 	private static DateTime parseDate(String stringDate) {
 		DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
 		DateTime date = format.parseDateTime(stringDate);
 		return date;
 	}
 
 }
