 package eu.play_project.play_eventadapter.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.Scanner;
 
 import org.event_processing.events.types.PachubeEvent;
 import org.event_processing.events.types.UcTelcoClic2Call;
 import org.event_processing.events.types.UcTelcoGeoLocation;
 import org.junit.Before;
 import org.junit.Test;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdfreactor.runtime.ReactorResult;
 
 import eu.play_project.play_eventadapter.NoRdfEventException;
 
 public class AbstractReceiverTest {
 
 	private TestConsumerImpl eventConsumer;
 	
 	@Before
 	public void setup() {
 		eventConsumer = new TestConsumerImpl();
 	}
 	
 	@Test
 	public void testPachubeNotify() {
 		try {
 			String xmlText = new Scanner(this.getClass().getClassLoader().getResourceAsStream("PachubeEvent.notify.xml")).useDelimiter("\\A").next();
 			/*
 			 * Read RDF from the XML message
 			 */
 			Model model = eventConsumer.parseRdf(xmlText);
 			assertEquals("Parsed statements in model", 29, model.size());
 			
 			/*
			 * Instanciate a Pachube event
 			 */
 			ReactorResult<? extends PachubeEvent> result = PachubeEvent.getAllInstances_as(model);
 			List<? extends PachubeEvent> l = result.asList();
 			assertEquals("Expecting to find a Pachube event", 1, l.size());
 			
 			/*
 			 * Look into some attributes
 			 */
 			PachubeEvent event = l.get(0);
 			assertEquals("Checking for event timestamp", javax.xml.bind.DatatypeConverter
 					.parseDateTime("2012-04-01T02:00:06.905Z"), event.getEndTime());
 		} catch (NoRdfEventException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testTaxiUCGeoLocationEvent() {
 		try {
 			String xmlText = new Scanner(this.getClass().getClassLoader().getResourceAsStream("TaxiUCGeoLocation.notify.xml")).useDelimiter("\\A").next();
 			/*
 			 * Read RDF from the XML message
 			 */
 			Model model = eventConsumer.parseRdf(xmlText);
 			assertEquals("Parsed statements in model", 9, model.size());
 			
 			/*
 			 * Instanciate an event object
 			 */
 			ReactorResult<? extends UcTelcoGeoLocation> result = UcTelcoGeoLocation.getAllInstances_as(model);
 			List<? extends UcTelcoGeoLocation> l = result.asList();
 			assertEquals("Expecting to find a Pachube event", 1, l.size());
 			
 			/*
 			 * Look into some attributes
 			 */
 			UcTelcoGeoLocation event = l.get(0);
 			assertNotNull("Checking for an event location", event.getLocation());
 		} catch (NoRdfEventException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testUcTelcoClic2CallEvent() {
 		try {
 			String xmlText = new Scanner(this.getClass().getClassLoader().getResourceAsStream("UcTelcoClic2Call.notify.xml")).useDelimiter("\\A").next();
 			/*
 			 * Read RDF from the XML message
 			 */
 			Model model = eventConsumer.parseRdf(xmlText);
 			assertEquals("Parsed statements in model", 7, model.size());
 	
 			/*
 			 * Instantiate an event object
 			 */
 			ReactorResult<? extends UcTelcoClic2Call> result = UcTelcoClic2Call.getAllInstances_as(model);
 			List<? extends UcTelcoClic2Call> l = result.asList();
 			assertEquals("Expecting to find a UcTelcoClic2Call event", 1, l.size());
 			
 			/*
 			 * Look into some attributes using the getters:
 			 */
 			UcTelcoClic2Call clic2callEvent = l.get(0);
 			assertNotNull("Checking if there was an event instantiated.", clic2callEvent);
 			assertEquals("Checking for an event phone number", "33600000010", clic2callEvent.getUcTelcoCalleePhoneNumber());
 			assertEquals("Checking for an event uniqueId", "cl1-24", clic2callEvent.getUcTelcoUniqueId());
 		} catch (NoRdfEventException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testGenericEvent() {
 		String xmlText = new Scanner(this.getClass().getClassLoader().getResourceAsStream("UcTelcoClic2Call.notify.xml")).useDelimiter("\\A").next();
 
 		UcTelcoClic2Call clic2callEvent;
 		try {
 			clic2callEvent = eventConsumer.getEvent(xmlText, UcTelcoClic2Call.class);
 		} catch (NoRdfEventException e) {
 			fail(e.getMessage());
 			return;
 		}
 
 		assertNotNull("Checking if there was an event instantiated.", clic2callEvent);
 		assertEquals("Checking for an event phone number", "33600000010", clic2callEvent.getUcTelcoCalleePhoneNumber());
 		assertEquals("Checking for an event uniqueId", "cl1-24", clic2callEvent.getUcTelcoUniqueId());
 
 	}
 	
 	/**
 	 * This test should throw an exception because an non-rdf event is parsed.
 	 * 
 	 * @throws NoRdfEventException
 	 */
 	@Test(expected = NoRdfEventException.class)
 	public void testNonRdfMessageException() throws NoRdfEventException {
 		String xmlText = new Scanner(this.getClass().getClassLoader()
 				.getResourceAsStream("NonRDFEvent.soap.xml")).useDelimiter(
 				"\\A").next();
 		/*
 		 * Read RDF from the XML message
 		 */
 		Model model = eventConsumer.parseRdf(xmlText);
 		assertEquals("Parsed statements in model", 9, model.size());
 
 		/*
 		 * Instanciate an event object
 		 */
 		ReactorResult<? extends UcTelcoGeoLocation> result = UcTelcoGeoLocation
 				.getAllInstances_as(model);
 		List<? extends UcTelcoGeoLocation> l = result.asList();
 		assertEquals("Expecting to find a Pachube event", 1, l.size());
 
 		/*
 		 * Look into some attributes
 		 */
 		UcTelcoGeoLocation event = l.get(0);
 		assertNotNull("Checking for an event location", event.getLocation());
 	}
 }
