 package parser;
 
 import java.util.*;
 
 import org.w3c.dom.*;
 
 import event.*;
 
 public class DukeCalendarParser extends Parser {
 	private String[] myTime = { "year", "twodigitmonth", "twodigitday",
 			"twodigithour", "twodigitminute" };
 
 	private String format(ArrayList<String> list) {
 		return list.get(0) + "-" + list.get(1) + "-" + list.get(2) + " "
 				+ list.get(3) + ":" + list.get(4) + ":" + "00";
 	}
 
 	public DukeCalendarParser() {
 		myDocument = null;
 		myEvents = new ArrayList<Event>();
 	}
 
 	@Override
 	protected String getHead() {
 		return "/events/event";
 	}
 
	private String getTitle(Node currentEvent) {
 		return getTagValue(currentEvent, "summary/text()");
 	}
 
	private String getSummary(Node currentEvent) {
 		return getTagValue(currentEvent, "description/text()");
 	}
 
	private String getURL(Node currentEvent) {
 		return getTagValue(currentEvent, "link/text()");
 	}
 
	private String getStartDate(Node currentEvent) {
 		ArrayList<String> time = new ArrayList<String>();
 		for (String s : myTime)
 			time.add(getTagValue(currentEvent, "start/" + s + "/text()"));
 		return format(time);
 	}
 
	private String getEndDate(Node currentEvent) {
 		ArrayList<String> time = new ArrayList<String>();
 		for (String s : myTime)
 			time.add(getTagValue(currentEvent, "end/" + s + "/text()"));
 		return format(time);
 	}
 
 	@Override
 	protected HashMap<String, String> getMyFields(Node currentEvent) {
 		// TODO Auto-generated method stub
 		HashMap<String, String> toReturn = new HashMap<String, String>();
 		toReturn.put("title", getTitle(currentEvent));
 		toReturn.put("summary", getSummary(currentEvent));
 		toReturn.put("startTime", getStartDate(currentEvent));
 		toReturn.put("endTime", getEndDate(currentEvent));
 		toReturn.put("url", getURL(currentEvent));
 		return toReturn;
 	}
 
 }
