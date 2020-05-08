 // a bit of a hack, putting test here so I can use OwlClassImpl package private constructor 
 package org.cichonski.ontviewer.model;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.cichonski.ontviewer.parse.OwlSaxHandler;
 
 public class OwlSaxHandlerTest extends TestCase {
 	private static final Map<String, OwlClass> expectedClasses = new HashMap<String, OwlClass>();
 	private static OwlClass expectedRoot = null;
 	// class URIs
     private static final String OWL_THING = "http://www.w3.org/2002/07/owl#Thing";
 	private static final String BOTNET = "http://foo.org/vocabulary/indicators#Botnet";
 	private static final String ATTACK_PHASE = "http://foo.org/vocabulary/indicators#AttackPhase";
 	private static final String FILE = "http://foo.org/vocabulary/indicators#File";
 	private static final String INTERNAL_INDICATOR = "http://foo.org/vocabulary/indicators#InternalIndicator";
 	private static final String EXTERNAL_INDICATOR = "http://foo.org/vocabulary/indicators#ExternalIndicator";
 	private static final String INDICATOR = "http://foo.org/vocabulary/indicators#Indicator";
 	// predicate URIs
 	private static final String CALLS_BACK_TO = "http://foo.org/vocabulary/indicators#callsbackTo";
 	private static final String DROPS_FILE = "http://foo.org/vocabulary/indicators#dropsFile";
 	private static final String SERVES_FILE = "http://foo.org/vocabulary/indicators#servesFile";
 	private static final String USES = "http://foo.org/vocabulary/indicators#uses";
 	private static final String TEST_PROP = "http://foo.org/vocabulary/indicators#testDataTypeProperty";
 	// data  URIs
 	private static final String DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";
 
 	static {
 		try {
 			/* create botnet class*/
 			final OwlClass botnet = new OwlClassImpl(new URI(BOTNET), "Botnet", "Typically a host used to control another host or malicious process. This could technically be internal or external.", 
 					new HashSet<OwlClass>(), new HashSet<Property>(), new HashSet<Property>());
 			expectedClasses.put(BOTNET, botnet);
 			
 			/* attack phase class */
 			final OwlClass attackPhase = new OwlClassImpl(new URI(ATTACK_PHASE), "Attack Phase", "test attack phase comment", 
 					new HashSet<OwlClass>(), new HashSet<Property>(), new HashSet<Property>());
 			expectedClasses.put(ATTACK_PHASE, attackPhase);
 			
 			/* file class */
 			final OwlClass file = new OwlClassImpl(new URI(FILE), "File", "Any type of file that signifies that an incident may have occurred or may be currently occurring.", 
 					new HashSet<OwlClass>(), new HashSet<Property>(), new HashSet<Property>());
 			expectedClasses.put(FILE, file);
 			
 			/* internal indicator class, has file as subClass */
 			final Set<OwlClass> internalIndSubClasses = Collections.singleton(file);
 			final Property callsBackTo = new PropertyImpl("callsbackTo", "Identifies the relationship where one indicator callsback to another indicator for some purpose.", 
 					new URI(CALLS_BACK_TO),  new HashSet<URI>(Collections.singleton(new URI(INTERNAL_INDICATOR))),  new HashSet<URI>(Collections.singleton(new URI(EXTERNAL_INDICATOR))));
 			final OwlClass internalIndicator = new OwlClassImpl(new URI(INTERNAL_INDICATOR), "InternalIndicator", "Indicators that are typically found on the system(s) that are the target of the incident.", 
 					internalIndSubClasses, Collections.singleton(callsBackTo), new HashSet<Property>());
 			expectedClasses.put(INTERNAL_INDICATOR, internalIndicator);
 			
 			
 			/* external indicator class, has a botnet as a subclass */
 			final Set<OwlClass> externalIndSubClasses = Collections.singleton(botnet);
 			final Property servesFile = new PropertyImpl("servesFile", "Some 'thing' serves a file", 
 					new URI(SERVES_FILE),  new HashSet<URI>(Collections.singleton(new URI(EXTERNAL_INDICATOR))),  new HashSet<URI>(Collections.singleton(new URI(FILE))));
 			final OwlClass externalIndicator = new OwlClassImpl(new URI(EXTERNAL_INDICATOR), "ExternalIndicator", "Indicators that identify something that is participating in the incident in some way, but is not physically located on the targeted system(s). These indicators are typically represented by an IP address, DNS name, or URL.", 
 					externalIndSubClasses, Collections.singleton(servesFile), new HashSet<Property>());
 			expectedClasses.put(EXTERNAL_INDICATOR, externalIndicator);
 			
 			/* indicator class, has internal and external indicators as subClasses */
 			Set<OwlClass> indSubClasses = new HashSet<OwlClass>();
 			indSubClasses.add(internalIndicator); 
 			indSubClasses.add(externalIndicator);
 			final Property dropsFile = new PropertyImpl("dropsFile", "Identifies the relationship where some indicator drops a file onto a system", 
 					new URI(DROPS_FILE),  new HashSet<URI>(Collections.singleton(new URI(INDICATOR))),  new HashSet<URI>(Collections.singleton(new URI(FILE))));
 			final Property uses = new PropertyImpl("uses", "Some indicator uses another for some purpose", 
 					new URI(USES),  new HashSet<URI>(Collections.singleton(new URI(INDICATOR))),  new HashSet<URI>(Collections.singleton(new URI(INDICATOR))));
 			Set<Property> objPreds = new HashSet<Property>();
 			objPreds.add(dropsFile);
 			objPreds.add(uses);
 			final Property testProp = new PropertyImpl("Datatype Property Mapping", "a datatype property", 
 					new URI(TEST_PROP),  new HashSet<URI>(Collections.singleton(new URI(INDICATOR))),  new HashSet<URI>(Collections.singleton(new URI(DATE_TIME))));
 			final OwlClass indicator = new OwlClassImpl(new URI(INDICATOR), "Indicator", "A sign that an incident may have occurred or may be currently occurring. (source: NIST SP 800-61 rev.1). ", 
 					indSubClasses, objPreds, Collections.singleton(testProp));
 			expectedClasses.put(INDICATOR, indicator);
 			
 			Set<OwlClass> rootSubClasses = new HashSet<OwlClass>();
 			rootSubClasses.add(indicator);
 			rootSubClasses.add(attackPhase);
 			expectedRoot = new OwlClassImpl(new URI(OWL_THING), "OWL Thing", "OWL Thing, the root of the tree", 
 					rootSubClasses, new HashSet<Property>(), new HashSet<Property>());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
     
     public OwlSaxHandlerTest(String testName) {
         super(testName);
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite() {
         // adds all methods beginning with 'test' to the suite.
         return new TestSuite(OwlSaxHandlerTest.class);
     }
     
     /**
      * Test that the handler generated the correct class cache representation. 
      * @throws Exception
      */
     public void testClassCache() throws Exception {
     	InputStream ont = null;
     	try {
         	ont = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontologies/subset_indicators-vocabulary.owl");
             final OwlSaxHandler handler = new OwlSaxHandler();
             final SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setFeature("http://xml.org/sax/features/namespaces", true);
             factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
             final SAXParser parser = factory.newSAXParser();
             parser.parse(ont, handler);
             
             final Map<URI, OwlClass> classCache = handler.getClassCache();
             
             assertEquals(classCache.size(), 6);
             // test all classess
             for (Map.Entry<URI, OwlClass> entry : classCache.entrySet()){
             	final OwlClass owlClass = entry.getValue();
             	final OwlClass expectedClass = expectedClasses.get(entry.getKey().toString());
             	testClasses(owlClass, expectedClass);
             }
             
     	} catch (Exception e){
     		e.printStackTrace();
     		throw new Exception(e);
     	} finally {
     		ont.close();
     	}
     }
     
     /**
      * Test that the handler generated the correct class tree representation.
      */
     public void testClassTree() throws Exception{
     	InputStream ont = null;
     	try {
         	ont = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontologies/subset_indicators-vocabulary.owl");
             final OwlSaxHandler handler = new OwlSaxHandler();
             final SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setFeature("http://xml.org/sax/features/namespaces", true);
             factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
             final SAXParser parser = factory.newSAXParser();
             parser.parse(ont, handler);
             
             final OwlClass treeRoot = handler.getRoot();
             testClasses(treeRoot, expectedRoot);
             
            //TODO: set up mock class tree, then test against this one.

            
     	} catch (Exception e){
     		e.printStackTrace();
     		throw new Exception(e);
     	} finally {
     		ont.close();
     	}
     }
     
     
     private void testClasses(OwlClass owlClass, OwlClass expectedClass){
     	System.out.println("testing generated class: " + owlClass);
     	assertNotNull(owlClass);
     	assertNotNull(expectedClass);
     	assertEquals(owlClass, expectedClass); //only inspects URI
     	assertEquals(owlClass.hashCode(), expectedClass.hashCode()); //only inspects URI
     	assertEquals(owlClass.getLabel(), expectedClass.getLabel());
     	assertEquals(owlClass.getDescritpion(), expectedClass.getDescritpion());
     	assertEquals(owlClass.getURI(), expectedClass.getURI());
     	
     	assertEquals(owlClass.getDataTypeProperty().size(), expectedClass.getDataTypeProperty().size());
     	testPredicates(owlClass.getDataTypeProperty(), expectedClass.getDataTypeProperty());
     	
     	assertEquals(owlClass.getObjectProperties().size(), expectedClass.getObjectProperties().size());
     	testPredicates(owlClass.getObjectProperties(), expectedClass.getObjectProperties());
     	
     	assertEquals(owlClass.getSubClasses().size(), expectedClass.getSubClasses().size());
     	// may test a class more than once, but that is fine
     	final Set<OwlClass> subClasses = new TreeSet<OwlClass>(owlClass.getSubClasses()); // ensure correct order
     	final Iterator<OwlClass> subClassIter = subClasses.iterator();
     	final Set<OwlClass> expectedSubClasses = new TreeSet<OwlClass>(expectedClass.getSubClasses());
     	final Iterator<OwlClass> expectedSubClassIter = expectedSubClasses.iterator();
     	while (expectedSubClassIter.hasNext()) {
     		// already ensured they are the same size, 
     		testClasses(subClassIter.next(), expectedSubClassIter.next());
     	}
     }
     
 
     private void testPredicates(Set<Property> preds, Set<Property> expectedPreds){
     	assertEquals(preds.size(), expectedPreds.size());
     	//ensure correct order
     	preds = new TreeSet<Property>(preds);
     	final Iterator<Property> predsIter = preds.iterator();
     	expectedPreds = new TreeSet<Property>(expectedPreds);
     	final Iterator<Property> expectedPredsIter = expectedPreds.iterator();
     	while (predsIter.hasNext()){
     		Property pred = predsIter.next();
     		Property expectedPred = expectedPredsIter.next();
     		assertEquals(pred, expectedPred);
     		assertEquals(pred.getLabel(), expectedPred.getLabel());
     		assertEquals(pred.getDescription(), expectedPred.getDescription());
     		assertEquals(pred.getURI(), expectedPred.getURI());
     		assertEquals(pred.getDomains().size(), expectedPred.getDomains().size());;
     		testURIs(pred.getDomains(), expectedPred.getDomains());
     		assertEquals(pred.getRanges().size(), expectedPred.getRanges().size());
     		testURIs(pred.getRanges(), expectedPred.getRanges());
     	}
     }
     
     private void testURIs(Set<URI> uris, Set<URI> expectedUris){
     	assertEquals(uris.size(), expectedUris.size());
     	//ensure correct order
     	uris = new TreeSet<URI>(uris);
     	final Iterator<URI> urisIter = uris.iterator();
     	expectedUris = new TreeSet<URI>(expectedUris);
     	final Iterator<URI> expectedUrisIter = expectedUris.iterator();
     	while (urisIter.hasNext()){
     		URI uri = urisIter.next();
     		URI expectedUri = expectedUrisIter.next();
     		assertEquals(uri, expectedUri);
     	}
     }
     
 
     
     
     
 }
