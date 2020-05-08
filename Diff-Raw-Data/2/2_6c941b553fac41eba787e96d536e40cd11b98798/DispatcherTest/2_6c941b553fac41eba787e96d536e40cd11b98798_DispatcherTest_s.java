 package eu.play_project.querydispatcher.bdpl.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.sparql.serializer.PlaySerializer;
 
 import eu.play_platform.platformservices.bdpl.VariableTypes;
 import eu.play_project.play_platformservices.api.HistoricalQuery;
 import eu.play_project.play_platformservices.api.QueryDetails;
 import eu.play_project.play_platformservices.api.QueryTemplate;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
 import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;
 /**
  * 
  * @author sobermeier
  *
  */
 public class DispatcherTest {
 	private static Logger logger;
 
 	@Test
 	public void testGetIoStreamIds() throws IOException{
 		logger = LoggerFactory.getLogger(DispatcherTest.class);
 		String queryString;
 		Set<String> expectedInputStreams = new HashSet<String>(Arrays.asList(new String[] {"http://streams.event-processing.org/ids/TwitterFeed", "http://streams.event-processing.org/ids/TaxiUCGeoLocation", "http://streams.event-processing.org/ids/TaxiUCGeoLocation"}));
 		String expectedOutputStream = "http://streams.event-processing.org/ids/ContextualizedLatitudeFeed";
 		
 		// Get query.
 		queryString = getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
 		
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		StreamIdCollector streamIdCollector = new StreamIdCollector();
 	
 		QueryDetails qd = new QueryDetails();
 		streamIdCollector.getStreamIds(query, qd);
 		
 		// Test toString() implementation
 		assertTrue(qd.toString().contains(expectedOutputStream));
 
 		// Test output stream
 		assertEquals(expectedOutputStream, qd.getOutputStream());
 		
 		// Test input streams
 		assertEquals(expectedInputStreams, qd.getInputStreams());
 
 	}
 	
 	@Test
 	public void testGetVariablesAndTypes() throws IOException{
 		if(logger == null){
 			logger= LoggerFactory.getLogger(DispatcherTest.class);
 		}
 		
 		// Get query.
 		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
 		
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		
 		VariableTypeManager vtm = new VariableTypeManager(query);
 		
 		vtm.collectVars();
 		
 		List<String> vars = vtm.getVariables(VariableTypes.CONSTRUCT_TYPE);
 		assertTrue(vars.size() == 5);
 		assertTrue(vars.contains("e1"));
 		assertTrue(vars.contains("e2"));
 		assertTrue(vars.contains("bob"));
 		assertTrue(vars.contains("alice"));
 		assertTrue(vars.contains("tweetContent"));
 
 		vars = vtm.getVariables(VariableTypes.REALTIME_TYPE);
 		assertTrue(vars.size() == 8);
 		assertTrue(vars.contains("id1"));
 		assertTrue(vars.contains("e1"));
 		assertTrue(vars.contains("e2"));
 		assertTrue(vars.contains("alice"));
 		assertTrue(vars.contains("bob"));
 		assertTrue(vars.contains("direction"));
 		assertTrue(vars.contains("firstEvent"));
 		assertTrue(vars.contains("id2"));
 
 
 		vars = vtm.getVariables(VariableTypes.HISTORIC_TYPE);
 		System.out.println(vars.size());
 		assertTrue(vars.size() == 5);
 		assertTrue(vars.contains("id3"));
 		assertTrue(vars.contains("e3"));
 		assertTrue(vars.contains("tweetTime"));
 		assertTrue(vars.contains("firstEvent"));
 		assertTrue(vars.contains("tweetContent"));
 	}
 	
 	@Test
 	public void testDispatchQueryHistoricalMultipleClouds() throws IOException {
 		// Get query.
		String queryString = getSparqlQuery("BDPL-Query-Realtime-Historical-multiple-Clouds.eprq");
 
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 
 		// Dispatch query
 		List<HistoricalQuery> queries = PlaySerializer.serializeToMultipleSelectQueries(query);
 
 		// Test results.
 		String temperatureAstream = "PREFIX : <http://events.event-processing.org/types/> \nPREFIX xsd: <http://events.event-processing.org/types/> \nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n\nSELECT DISTINCT  ?e2 ?temperature ?pub_date ?id2 ?e4 ?id4 \n WHERE { \nGRAPH ?id2\n  { ?e2 rdf:type :Temperature .\n    ?e2 :stream <http://streams.event-processing.org/ids/TemperatureA#stream> .\n    ?e2 :current ?temperature .\n    ?e2 :date ?pub_date\n  }\nGRAPH ?id4\n  { ?e4 rdf:type :Temperature .\n    ?e4 :stream <http://streams.event-processing.org/ids/TemperatureA#stream> .\n    ?e4 :current ?temperature\n  }} ";
 		String temperatureBstream = "PREFIX : <http://events.event-processing.org/types/> \nPREFIX xsd: <http://events.event-processing.org/types/> \nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n\nSELECT DISTINCT  ?pub_date ?e3 ?id3 \n WHERE { \nGRAPH ?id3\n  { ?e3 rdf:type :Temperature .\n    ?e3 :stream <http://streams.event-processing.org/ids/TemperatureB#stream> .\n    ?e3 :date ?pub_date\n  }} ";
 		
 		//Test if generated select query is OK.
 		assertEquals(temperatureAstream, queries.get(0).getQuery());
 		assertEquals(temperatureBstream, queries.get(1).getQuery());
 		
 		
 		for (String varname : queries.get(0).getVariables()) {
 			System.out.println(varname);
 		}
 
 	}
 	
 	@Test
 	public void testDispatchMissedCallsPlusTwitterQuery() throws IOException{
 		// Get query.
 		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
 		
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		
 		System.out.println(query);
 		
 		StreamIdCollector streamIdCollector = new StreamIdCollector ();
 		
 		QueryDetails qd = new QueryDetails();
 		streamIdCollector.getStreamIds(query, qd);
 
 		assertEquals("http://streams.event-processing.org/ids/TaxiUCClic2Call", qd.getOutputStream());
 		assertTrue(qd.getInputStreams().contains("http://streams.event-processing.org/ids/TaxiUCCall"));
 	}
 	
 	@Test
 	public void testQueryTemplateGenerator() throws IOException{
 		 QueryTemplateGenerator ab  =  new  QueryTemplateGenerator();
 		 
 		// Get query.
 		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
 			
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		
 		QueryTemplate qt = ab.createQueryTemplate(query);
 	}
 	
 	private String getSparqlQuery(String queryFile) throws IOException {
 		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(queryFile), StandardCharsets.UTF_8);	}
 	}
