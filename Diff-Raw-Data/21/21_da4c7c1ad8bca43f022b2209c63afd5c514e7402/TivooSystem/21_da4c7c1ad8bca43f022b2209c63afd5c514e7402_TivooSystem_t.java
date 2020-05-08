 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import xmlParse.XmlParser;
 
 import HTMLoutput.HTMLdetailPage;
 import HTMLoutput.HTMLsummaryPage;
 import Process.Event;
 
 
 public class TivooSystem {
 	
 	List<Event> myEvents;
 	
 	public TivooSystem(){
 		myEvents = new ArrayList<Event>();
 	}
 
 	/*
 	 * Loads a  cal file into the Tivoo Systems
 	 */
 	public void loadCal(String link){
 		try {
 			myEvents.addAll(XmlParser.loadAndParse(link));
 		} catch (Exception e) {
 			System.out.println("File \"" + link + "\" could not be loaded");
 		}
 	}
 	
 	public void filterByKeyword(String keyword){
 		myEvents = new xmlProcess(myEvents).process(keyword);
 	}
 	public void filterByTime(DateTime time){
 		myEvents = new xmlProcess(myEvents).timeFilter(time);
 	}
 	
 	public void outputSummaryAndDetailsPages(String localPathSummary) {
	    if (myEvents.isEmpty()) {
	        throw new RuntimeException("Could not output html: the list myEvents is empty.");
	    }
	    
 	    HTMLsummaryPage summaryPage = new HTMLsummaryPage(myEvents, localPathSummary);
 	    summaryPage.createHTMLpage();
 	    
 	    HTMLdetailPage detailPage = new HTMLdetailPage(myEvents, localPathSummary);
         detailPage.createHTMLpage(); 
 	}
 }
