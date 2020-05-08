 package eu.play_project.play_platformservices.tests;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.Service;
 
 import eu.play_project.play_platformservices.api.QueryDispatchApi;
 import eu.play_project.play_platformservices.api.QueryDispatchException;
 import eu.play_project.play_platformservices.jaxb.Query;
 
 public class ManualWebServiceTest {
 	
 	public static void  main(String[] args) throws QueryDispatchException {
 		URL wsdl = null;
		// Testing now tries to use a localhost server:
		//String address = Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint");
		// Using the production server:
		String address = "http://demo.play-project.eu:8084/play/QueryDispatchApi";
		
 		try {
			wsdl = new URL(address + "?wsdl");
 		} catch (MalformedURLException e) {
 		e.printStackTrace();
 		}
 
 		QName serviceName = new QName("http://play_platformservices.play_project.eu/", "QueryDispatchApi");
 
 		Service service = Service.create(wsdl, serviceName);
 		QueryDispatchApi queryDispatchApi = service
 		.getPort(eu.play_project.play_platformservices.api.QueryDispatchApi.class);
 
 		//Register query
 	//	String s = queryDispatchApi.registerQuery("http://patterns.event-processing.org/ids/webapp_" + Math.random(), "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX uctelco: <http://events.event-processing.org/uc/telco/>\nPREFIX geo:     <http://www.w3.org/2003/01/geo/wgs84_pos#>\nPREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\nPREFIX :        <http://events.event-processing.org/types/>\n\nCONSTRUCT {\n\t:e rdf:type :UcTelcoClic2Call .\n\t:e :stream <http://streams.event-processing.org/ids/TaxiUCClic2Call#stream>.\n\t:e uctelco:callerPhoneNumber ?alice .\n\t:e uctelco:calleePhoneNumber ?bob .\n\t:e uctelco:direction ?direction .\n\t:e :message \"The caller tried to reach the callee three times within 1 minute with no success.\" .\n\t:e :members ?e1, ?e2, ?e3 .\n}\nWHERE {\n\tWINDOW {\n\t\tEVENT ?id1 {\n\t\t\t?e1 rdf:type :UcTelcoCall .\n\t\t\t?e1 :stream <http://streams.event-processing.org/ids/TaxiUCCall#stream> .\n\t\t\t?e1 uctelco:callerPhoneNumber ?alice .\n\t\t\t?e1 uctelco:calleePhoneNumber ?bob .\n\t\t\t?e1 uctelco:direction ?direction .\n\t\t\t}\n\t\tSEQ\n\t\tEVENT ?id2 {\n\t\t\t?e2 rdf:type :UcTelcoCall .\n\t\t\t?e2 :stream <http://streams.event-processing.org/ids/TaxiUCCall#stream> .\n\t\t\t?e2 uctelco:callerPhoneNumber ?alice .\n\t\t\t?e2 uctelco:calleePhoneNumber ?bob .\n\t\t\t?e2 uctelco:direction ?direction .\n\t\t\t}\n\t\tSEQ\n\t\tEVENT ?id3 {\n\t\t\t?e3 rdf:type :UcTelcoCall .\n\t\t\t?e3 :stream <http://streams.event-processing.org/ids/TaxiUCCall#stream> .\n\t\t\t?e3 uctelco:callerPhoneNumber ?alice .\n\t\t\t?e3 uctelco:calleePhoneNumber ?bob .\n\t\t\t?e3 uctelco:direction ?direction .\n\t\t\t}\n\t} (\"PT1M\"^^xsd:duration, sliding)\n}");
 		
 		//Print registered queries.
 		for (Query string : queryDispatchApi.getRegisteredQueries()) {
 			System.out.println(string.id);
 			System.out.println(string.name);
 			System.out.println(string.recordDate);
 			System.out.println("===================================");
 		}
 		
 		//System.out.println(queryDispatchApi.getRegisteredQuery("http://patterns.event-processing.org/ids/webapp_0.6431289675616686"));
 	}
 
 }
