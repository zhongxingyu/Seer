 package com.globalresolvewws;
 
 /**
  * This handler populates a list of weather objects off of the service
  * it is based off of work found on stackoverflow
  * @author Caleb, (?)
  * 
  */
 
 
 import java.util.ArrayList;
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class ServiceHandler extends DefaultHandler {
 
 	// Create an arraylist with the weather information
 	public ArrayList<Weather> forecast = new ArrayList<Weather>();
 
 	// Make sure that the code in DefaultHandler's
 	//   constructor is called:
 	public ServiceHandler() {
 		super();
 	}
 
 	/*** Below are the three methods that we are extending ***/
 
	public void startDocument() {
		System.out.println("Start document");
    }

	public void endDocument() {
		System.out.println("End document");
	}

 	// This is where all the work is happening:
 	public void startElement (String uri, String name, String qName, Attributes atts) {
 		Weather entry = new Weather();
 		if (qName.compareTo("day_of_week") == 0) {
 			String day = atts.getValue(0);
 			entry.setTime(day);
 		}
 		if(qName.compareToIgnoreCase("low") == 0) {
 			int low = Integer.parseInt(atts.getValue(0));
 			entry.setMinTemp(low);
 		}
 		if(qName.compareToIgnoreCase("high") == 0) {
 			int high = Integer.parseInt(atts.getValue(0));
 			entry.setMaxTemp(high);
 		}
 		//if(qName.compareToIgnoreCase("condition") == 0) {
 		//	String condition = atts.getValue(0);
 		//	entry.set(condition);
 		//}
 		if(qName.compareToIgnoreCase("humidity") == 0) {
 			String humid = atts.getValue(0).split("[a-z]")[7].split(":")[1].split("%")[0].split(" ")[1];
 			int humidval = Integer.parseInt(humid);
 			entry.setHumidity(humidval);
 		}
 		forecast.add(entry);
 	}
 }
