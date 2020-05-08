 package Salsalytics;
 
 import java.util.Map;
 
 /**
  * <p>EventSender is the public interface for the Salsalytics wrapper. This
  * class is thread safe singleton that can be safely used from anywhere 
  * in the users Android application.  This class has two methods one to 
  * set the Server in which to transfur data too and one to send data. </p>
  * 
  * ***IMPORTANT*** In order to send data, the URL must be set.
  * 
  * @author Brandon Page, brpage@calpoly.edu
  */
 public class EventSender {
         private static volatile EventSender INSTANCE;
         private static Event event;
         private static AsyncTaskSender ats;
        private static String urlName;
 
         private EventSender() {
         	event = new Event();
         	ats = new AsyncTaskSender(event);
         }
 
         private static EventSender getInstance() {
         	if (INSTANCE == null) {
         		INSTANCE = new EventSender();
         	}              
        	event = new Event();
         	ats = new AsyncTaskSender(event);
               
         	return INSTANCE;
         }
 
         /**
          * This method sets the URL of the receiving page of Server 
          * to send data too.
          * 
          * @param URL a String representing the URL
          */
         public static void setURL(String URL) {
         	getInstance();
        	urlName = URL;
        	//event.setServer(URL);
         }
 
         /**
          * <p>This method sends and Event to the server.</p>
          * 
          * ***REMEMBER*** In order to send data, the URL must be set.
          * 
          * @param title a String representing the Title of the Event.
          * @param attributes a Map<String, String> containing the key-value 
          * pair attributes associated with the Event.   
          */
         public static void sendData(String title, Map<String, String> attributes) {
         	getInstance();
        	event.setServer(urlName);
         	event.addData(title, attributes);
         	ats.execute(event.getServer());
         }
 }
