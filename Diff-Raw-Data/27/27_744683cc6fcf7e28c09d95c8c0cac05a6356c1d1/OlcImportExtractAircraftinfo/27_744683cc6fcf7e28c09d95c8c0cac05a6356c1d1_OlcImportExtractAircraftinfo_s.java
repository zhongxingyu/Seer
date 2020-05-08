 package ch.ubx.startlist.server.admin;
 import java.net.URL;
 import java.util.List;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Source;
 
 public class OlcImportExtractAircraftinfo {
 
 	private Source source;
 	private String callsign;
 	private String competitionID;
 
 	public OlcImportExtractAircraftinfo(String sourceUrlString) throws Exception {
 		source = new Source(new URL(sourceUrlString));
 		source.fullSequentialParse();
 		List<Element> ddElements = null;
 		List<Element> divElements = source.getAllElements(HTMLElementName.DIV);
 		for (Element element : divElements) {
 			String id = element.getAttributeValue("id");
 			if (id != null && id.equals("aircraftinfo")) {
 				ddElements = element.getAllElements(HTMLElementName.DD);
 				break;
 			}
 		}
 
 		if (ddElements != null) {
 			Element e = ddElements.get(1);
 			callsign = e != null ? e.getContent().toString() : null;
 			e = ddElements.get(2);
 			competitionID = e != null ? e.getContent().toString() : null;
 		}
 	}
 
 	public String getCallsign() {
 		return callsign;
 	}
 
 	public String getCompetitionID() {
 		return competitionID;
 	}
 
 }
