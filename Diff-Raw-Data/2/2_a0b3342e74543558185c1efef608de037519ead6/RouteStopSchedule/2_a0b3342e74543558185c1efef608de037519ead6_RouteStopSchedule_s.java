 package com.appirio.mobile.aau.nativemap;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.text.format.DateFormat;

 public class RouteStopSchedule implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private String color;
 	private String name;
 	private List<String> schedule;
 
 	public RouteStopSchedule(JSONObject object) throws AMException {
 		try {
 			if(object.has("color")) {
 				this.color = object.getString("color");
 			}
 			
 			if(object.has("name")) {
 				this.name = object.getString("name");
 			}
 			
 			schedule = new ArrayList<String>();
 			
 			if(object.has("schedule")) {
 				for(int i = 0; i < object.getJSONArray("schedule").length(); i++) {
 					schedule.add(object.getJSONArray("schedule").getString(i));
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			
 			throw new AMException(e);
 		}
 	}
 
 	public String getColor() {
 		return color;
 	}
 
 	public void setColor(String color) {
 		this.color = color;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<String> getSchedule() {
 		return schedule;
 	}
 
 	public void setSchedule(List<String> schedule) {
 		this.schedule = schedule;
 	}
 	
 	public String getNextBusETA() {
 		try {
 			int nextStopIndex = getNextStopIndex();
 			SimpleDateFormat format = new SimpleDateFormat("h:mm a");
 			
 			if(nextStopIndex == -1) {
 				return null;
 			} else {
 				Date nextStop = format.parse(schedule.get(nextStopIndex));
 				
 				Calendar now = Calendar.getInstance();
 				
 				now.set(1970, 0, 1);
 
 				return ((nextStop.getTime() - now.getTimeInMillis()) / (1000 * 60)) + " minutes";
 			}
 		} catch (ParseException e) {
 			e.printStackTrace();
 			
 			return null;
 		}
 	}
 	
 	public String getTodaysNextStops() {
 		StringBuilder result = new StringBuilder();
 		String separator = "";
 		
 		int nextStopIndex = getNextStopIndex();
 		
 		if(nextStopIndex != -1) { 
 			for(int i = nextStopIndex; i < schedule.size() && (i < nextStopIndex + 4); i++) {
 				String time = schedule.get(i);
 				
 				result.append(separator);
 				result.append(time);
 				
 				separator = ", ";
 			}
 		} else {
 			return null;
 		}
 		
 		return result.toString();
 	}
 	
 	private int getNextStopIndex() {
 		try {
 			SimpleDateFormat format = new SimpleDateFormat("h:mm a");
 			Calendar now = Calendar.getInstance();
 			
 			now.set(1970, 0, 1);
 			
 			for(int i = 0; i < schedule.size(); i++) {
 				Date date = format.parse(schedule.get(i));
 				
 				
 				if(date.after(now.getTime())) {
 					return i;
 				}
 			}
 			
 			return -1;
 		} catch (ParseException e) {
 			e.printStackTrace();
 			
 			return -1;
 		}
 	}
 	
 }
