 package xmlParse;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /*
  * @author Gang Song
  */
 
 public class TimeParser {
 
	public DateTime getDukeCalTime(String content, String period) {
		//TODO: make joda parser
		return null;
	}
	
 	/*
 	 * create Joda Time from a specific period (a subnode of an event like
 	 * 'start' or 'end') in an event (this method is used for parsing
 	 * DukeCalendar)
 	 */
 	public DateTime getDukeCalTime(Node nEvent, String period) {
 		NodeList children = nEvent.getChildNodes();
 		Node myTime = null;
 		for (int j = 0; j < children.getLength(); j++) {
 			Node child = children.item(j);
 			if (child.getNodeName().equals(period)) {
 				myTime = child;
 				break;
 			}
 		}
 		DateTime dt = null;
 		dt = new DateTime(getIntOfTime(myTime, "year"), getIntOfTime(myTime,
 				"month"), getIntOfTime(myTime, "day"), getIntOfTime(myTime,
 				"hour24"), getIntOfTime(myTime, "minute"), 0, 0);
 		return dt;
 	}
 
 	/*
 	 * Extract specific time from a node in Duke Calendar and return an int
 	 */
 
 	private int getIntOfTime(Node node, String tag) {
 		Element n = (Element) node;
 		try {
 			return Integer.parseInt(n.getElementsByTagName(tag).item(0)
 					.getTextContent());
 		} catch (NullPointerException e) {
 			return 0;
 		}
 	}
 
 	/*
 	 * create a DateTime Object from a string in Google Calendar
 	 */
 
 	public DateTime getGoogleCalTime(String content, String period) {
 
 		String[] sections = content.split(" ");
 
 		if (sections[0].equals("Recurring")) {
 			String date = sections[3];
 			String time = sections[4];
 			DateTime dt = new DateTime(date + "T" + time);
 			if (period.equals("start"))
 				return dt;
 			else
 				return dt.plusSeconds(Integer.parseInt(sections[6].substring(0,
 						4)));
 		} else {
 			DateTimeFormatterBuilder myFormatBuilder = new DateTimeFormatterBuilder();
 			myFormatBuilder.appendMonthOfYearShortText().appendDayOfMonth(1)
 					.appendLiteral(',').appendYear(4, 4);
 			DateTimeFormatter myFormat;
 
 			String myYear;
 			String myTime;
 			String myMonth = sections[2];
 			String myDay = sections[3];
 			if (sections[4].contains("<")) {
 				myYear = sections[4].substring(0, sections[4].indexOf("<"));
 				myFormat = myFormatBuilder.toFormatter();
 				return myFormat.parseDateTime(myMonth + myDay + myYear);
 			}
 			myYear = sections[4];
 
 			if (period.equals("start")) {
 
 				myTime = sections[5];
 			} else {
 				myTime = sections[7].substring(0, sections[7].indexOf("&"));
 			}
 
 			myFormatBuilder = checkAndBuildHourFormat(myFormatBuilder, myTime);
 			myFormat = myFormatBuilder.toFormatter();
 			return myFormat.parseDateTime(myMonth + myDay + myYear + myTime);
 		}
 
 	}
 
 	/*
 	 * construct a DateTimeFormatterBuilder according to different hour and
 	 * minute format
 	 */
 	private DateTimeFormatterBuilder checkAndBuildHourFormat(
 			DateTimeFormatterBuilder myBuilder, String myHourAndMinute) {
 		if (myHourAndMinute.contains(":"))
 			return myBuilder.appendClockhourOfHalfday(1).appendLiteral(':')
 					.appendMinuteOfHour(2).appendHalfdayOfDayText();
 		else
 			return myBuilder.appendClockhourOfHalfday(1)
 					.appendHalfdayOfDayText();
 
 	}
 
 }
