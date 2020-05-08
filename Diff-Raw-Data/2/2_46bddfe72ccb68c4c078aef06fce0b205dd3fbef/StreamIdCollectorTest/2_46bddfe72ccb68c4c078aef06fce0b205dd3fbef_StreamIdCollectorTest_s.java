 package eu.play_project.querydispatcher.bdpl.tests;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Test;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryFactory;
 
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_platformservices.api.QueryDetails;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
 
 public class StreamIdCollectorTest {
 
 	@Test
 	public void testStreamIdCollector() {
 		
 		Set<String> expectedInputStreams = new HashSet<String>(Arrays.asList(new String[] {Stream.TwitterFeed.getTopicUri(), Stream.TaxiUCGeoLocation.getTopicUri(), Stream.TaxiUCGeoLocation.getTopicUri()}));
 		String expectedOutputStream = Stream.ContextualizedLatitudeFeed.getTopicUri();
 		
 		// Get query.
 		String queryString = BdplEleTest.getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
 		String queryId = "exampleQuery";
 		
 		assertNotNull("Testing Query was not found on classpath", queryString);
 		
 		Query q = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		
 		QueryDetails qd = new QueryDetails();
 		qd.setQueryId(queryId);
 
 
 		StreamIdCollector streamIdCollector = new StreamIdCollector();
 		streamIdCollector.getStreamIds(q, qd);
 
 		// Test output stream
 		assertTrue(qd.getOutputStream().equals(expectedOutputStream));
 		
 		// Test input streams
 		assertTrue(qd.getInputStreams().equals(expectedInputStreams));
 		
 	}
 	
 	@Test
 	public void testStreamIdCollectorHistoric() {
 		
 		Set<String> expectedInputStreams = new HashSet<String>(Arrays.asList(new String[] {"http://streams.event-processing.org/ids/Temperature"}));
 		Set<String> expectedHistoricalStreams = new HashSet<String>(Arrays.asList(new String[] {"http://streams.event-processing.org/ids/TemperatureA", "http://streams.event-processing.org/ids/TemperatureB"}));
 		String expectedOutputStream = "http://streams.event-processing.org/ids/Temperatures";
 		
 		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("BDPL-Query-Realtime-Historical-multiple-Clouds.eprq");
 		String queryId = "exampleQuery2";
 		
 		assertNotNull("Testing Query was not found on classpath", queryString);
 		
 		Query q = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		
 		QueryDetails qd = new QueryDetails();
 		qd.setQueryId(queryId);
 
 		StreamIdCollector streamIdCollector = new StreamIdCollector();
 		streamIdCollector.getStreamIds(q, qd);
 
 		// Test output stream
 		System.out.println(qd.getOutputStream());
 		assertTrue(qd.getOutputStream().equals(expectedOutputStream));
 		
 		// Test input streams
 		assertTrue(qd.getInputStreams().equals(expectedInputStreams));
 
 		// Test historical streams
 		assertTrue(qd.getHistoricStreams().equals(expectedHistoricalStreams));
 		
 	}
 }
