 package xmlParse;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.xpath.*;
 
 import org.joda.time.DateTime;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import Process.Event;
 
 /**
  * @author Glenn Rivkees
  */
 
 public class DukeCalFileFactory extends FileParseFactory {
 
 	public boolean isThisKindOfThing(Document doc) {
 		// TODO: find way to distinguish a duke cal, right now this works because it is the last one.
 		return true;
 	}
 
 	public List<Event> parseEvents(Document doc) {
 		XPathFactory factory = XPathFactory.newInstance();
 		XPath xpath = factory.newXPath();
 		
 		// Xpath compilation
 		NodeList myEvents;
 		XPathExpression startTime, endTime, title, location, description;
 		try {
 			XPathExpression events = xpath.compile("//event");
 			startTime = xpath.compile("./start/utcdate");
			endTime = xpath.compile("./end/unformatted");
 			title = xpath.compile("./summary");
 			location = xpath.compile("./location/address");
 			description = xpath.compile("./description");
 			myEvents = (NodeList) events.evaluate(doc, XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			throw new RuntimeException("XPath expression failed to compile and/or evaluate");
 		}
 		
 		// List of Events
 		ArrayList<Event> toReturnEvents = new ArrayList<Event>();
 		// Run through nodes labeled event, and add to arraylist
 		for (int i = 0; i < myEvents.getLength(); i++){
 			Node nEvent = myEvents.item(i);
 			// run xpaths and make event
 			try {
				DateTime start=new TimeParser().getDukeCalTime(startTime.evaluate(nEvent), "start");
				DateTime end=new TimeParser().getDukeCalTime(endTime.evaluate(nEvent), "end");
 				toReturnEvents.add(new Event(title.evaluate(nEvent), location.evaluate(nEvent), description.evaluate(nEvent), start, end, "")) ;
 			} catch (XPathExpressionException e) {
 				throw new RuntimeException("Event Xpath Parsing did not evaluate correctly");
 			}
 			
 		}
 		return toReturnEvents;
 	}
 
 }
