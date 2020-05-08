 /*
  * 		
  			DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                     Version 2, December 2004
 
  		Copyright (C) 2013 Raveesh Bhalla <me@raveesh.cot>
 
  	Everyone is permitted to copy and distribute verbatim or modified
  	copies of this license document, and changing it is allowed as long
  	as the name is changed.
 
             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 
   	0. You just DO WHAT THE FUCK YOU WANT TO.
  * 		
  * 
  * 		Credit to Colin Mitchell (http://muffinlabs.com)
  * 		whose Really Simple History API 
  * 		(https://github.com/muffinista/really-simple-history-api/)
  * 		project I used prior to developing this. 
  * 
  * 		The JSON return structure in this project is 
  * 		the same as his work so as to simplify porting, if required. 
  * 
  * 		You will require the HTMLCleaner library which can be downloaded
  * 		from http://htmlcleaner.sourceforge.net/
  */
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 import org.htmlcleaner.XPatherException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * @author Raveesh
  *
  */
 /**
  * @author Raveesh
  *
  */
 /**
  * @author Raveesh
  *
  */
 public class WikiOnThisDay {
 
 	JSONObject entireData;
 	JSONArray	Births, Deaths, Events;
 	
 	/**
 	 * @param month month in the form of integer, eg, 1 corresponds to January, 2 to February
 	 * @param date	date of the month
 	 * @throws JSONException
 	 * @throws IOException
 	 * @throws XPatherException
 	 */
 	public WikiOnThisDay(int month,int date) throws JSONException, IOException, XPatherException{
 		String[] months = {
 				"January",
 				"February",
 				"March",
 				"April",
 				"May",
 				"June",
 				"July",
 				"August",
 				"September",
 				"October",
 				"November",
 				"December"
 		};
		URL url = new URL("http://en.wikipedia.org/wiki/"+months[month-1]+"_"+date);
 		TagNode node;
 		HtmlCleaner cleaner = new HtmlCleaner();
         CleanerProperties props = cleaner.getProperties();
         props.setAllowHtmlInsideAttributes(true);
         props.setAllowMultiWordAttributes(true);
         props.setRecognizeUnicodeChars(true);
         props.setOmitComments(true);
         URLConnection conn = url.openConnection();
         conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17");
         InputStreamReader is = new InputStreamReader(conn.getInputStream());
         BufferedReader br = new BufferedReader(is);
         node = cleaner.clean(is);
        Object[] info_nodes = node.evaluateXPath("//div[@id='mw-content-text']");
        TagNode[] nodes = new TagNode[3];
        TagNode root = (TagNode) info_nodes[0];
        info_nodes = root.getElementsByName("ul", false);
         
        int eventsNode = 0;
         if (month == 1 && date == 1)
         	eventsNode++;
         TagNode EventsNode = (TagNode) info_nodes[eventsNode];
         List EventsChildren = EventsNode.getElementListByName("li", false);
     	Events = new JSONArray();
         for (int i=0;i<EventsChildren.size();i++){
         	TagNode child = (TagNode) EventsChildren.get(i);
         	String text = child.getText().toString();
         	String[] split = text.split(" – ");
         	JSONObject item = new JSONObject();
         	item.put("year", split[0]);
         	item.put("text", split[1]);
         	Events.put(item);
         }
         
         TagNode BirthsNode = (TagNode) info_nodes[eventsNode+1];
     	List BirthsChildren = BirthsNode.getElementListByName("li", false);
     	Births = new JSONArray();
     	for (int i=0;i<BirthsChildren.size();i++){
         	TagNode child = (TagNode) BirthsChildren.get(i);
         	String text = child.getText().toString();
         	String[] split = text.split(" – ");
         	JSONObject item = new JSONObject();
         	item.put("year", split[0]);
         	item.put("text", split[1]);
         	Births.put(item);
         }
 
         
     	TagNode DeathsNode = (TagNode) info_nodes[eventsNode+2];
     	List DeathsChildren = DeathsNode.getElementListByName("li", false);
     	Deaths = new JSONArray();
         for (int i=0;i<DeathsChildren.size();i++){
         	TagNode child = (TagNode) DeathsChildren.get(i);
         	String text = child.getText().toString();
         	String[] split = text.split(" – ");
         	JSONObject item = new JSONObject();
         	item.put("year", split[0]);
         	item.put("text", split[1]);
         	Deaths.put(item);
         }
 
         JSONObject data = new JSONObject();
         data.put("Deaths", Deaths);
         data.put("Births", Births);
         data.put("Events", Events);
         
         entireData = new JSONObject();
         entireData.put("data", data);
         entireData.put("url", url.toString());
         entireData.put("date", months[month-1]+" "+date);
 	}
 	
 	
 	/**
 	 * @return JSONObject for the entire data
 	 */
 	public JSONObject getEntireData(){
 		return entireData;
 	}
 	
 	/**
 	 * @return JSONArray for Births
 	 */
 	public JSONArray getBirths(){
 		return Births;
 	}
 	
 	/**
 	 * @return JSONArray for Deaths
 	 */
 	public JSONArray getDeaths(){
 		return Deaths;
 	}
 	
 	/**
 	 * @return JSONArray for Events
 	 */
 	public JSONArray getEvents(){
 		return Events;
 	}
 	
 }
