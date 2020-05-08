 package com.cromiumapps.uwhub;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.content.Context;
 
 public class EventsData {
 
 	//private
 	Context context;
 	
 	String id = null;
 	String date = null;
 	String name = null;
 	String links = null;
 	String description = null;
 	
 	SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
 	
 	public EventsData (String inputID, String inputDate, String inputName, String inputLinks, String inputDescription) {
 	    id = inputID;
 	    date = inputDate;
 	    name = inputName;
 	    links = inputLinks;
 	    description = inputDescription;
 	}
 	
 	public String getId() {
 		return id;
 	}
 	
 	public String getDate() {
 	    
 	    try 
 	    {
 	        Date parsedDate = (Date) format.parse(date);
 	        System.out.println(parsedDate);
 	        SimpleDateFormat day = new SimpleDateFormat("EEEE, MMMMM dd, yyyy");
 	        date = day.format(parsedDate).toString();
 	    } 
 	    catch (ParseException e) 
 	    {
 	        e.printStackTrace();
 	    }
 	    
 		return date;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getLinks() {
 		return decode(links);
 	}
 	
 	public String getDescription() {
 		return decode(description);
 	}
 	
 	private String decode(String string) {
 	    
 	    string = string.replace("&quot;", "\"");
 	    string = string.replace("&apos;", "'");
 	    string = string.replace("&amp;", "&");
 	    string = string.replace("&lt;", "<");
 	    string = string.replace("&gt;", ">");
 	    string = string.replace("{\"result\":\"", "");
 	    string = string.replace("\"}", "");
	    
 	    string = string.replaceAll("\\[Offered.*\\]", "");
 	    string = string.replaceAll("\\[Also offered.*\\]", "");
 	    string = string.replaceAll("\\&eacute\\;", "é");
 	    
 	    return string;
 	}
 
 }
