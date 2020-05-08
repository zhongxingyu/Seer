 package eu.play_project.dcep;
 
 import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.etsi.uri.gcm.util.GCM;
 import org.event_processing.events.types.UcTelcoCall;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Factory;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.fractal.api.NoSuchInterfaceException;
 import org.objectweb.fractal.api.control.IllegalLifeCycleException;
 import org.objectweb.proactive.ActiveObjectCreationException;
 import org.objectweb.proactive.api.PAActiveObject;
 import org.objectweb.proactive.core.component.adl.FactoryFactory;
 import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
 import org.objectweb.proactive.core.node.NodeException;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.graph.NodeFactory;
 
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 import eu.play_project.play_platformservices.api.QueryDispatchApi;
 import eu.play_project.play_platformservices.api.QueryDispatchException;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.Quadruple;
 
 
 
 
 public class CommonsPatternTest {
 
 	public static QueryDispatchApi queryDispatchApi;
 	public static DistributedEtalisTestApi testApi;
 	boolean start = false;
 	static Component root;
 	public static boolean test;
 	private final Logger logger = LoggerFactory.getLogger(CommonsPatternTest.class);
 	
 	@Before
 	public void instantiatePlayPlatform()
 			throws IllegalLifeCycleException, NoSuchInterfaceException,
 			ADLException {
 
 		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
 				.setValue("proactive.java.policy");
 
 		CentralPAPropertyRepository.GCM_PROVIDER
 				.setValue("org.objectweb.proactive.core.component.Fractive");
 
 		
 		Factory factory = FactoryFactory.getFactory();
 		HashMap<String, Object> context = new HashMap<String, Object>();
 
 		root = (Component) factory.newComponent("PsDcepComponent", context);
 		GCM.getGCMLifeCycleController(root).startFc();
 
 		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));
 		testApi = (DistributedEtalisTestApi) root.getFcInterface("DistributedEtalisTestApi");
 		
 		try {
 			Thread.sleep(10000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@After
 	public void terminatPlayPlatform() {
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
 				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
 			 }
 	
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testClic2callPattern() throws IllegalLifeCycleException,
 			NoSuchInterfaceException, ADLException, InterruptedException, QueryDispatchException {
 
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("play-epsparql-clic2call.eprq");
 
 		// Compile query
 		queryDispatchApi.registerQuery("abc0", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		try {
 			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		} catch (ActiveObjectCreationException e) {
 			e.printStackTrace();
 		} catch (NodeException e) {
 			e.printStackTrace();
 		}
 
 		testApi.attach(subscriber);
 	
 		logger.info("Publish events");
 		for (int i = 0; i < 5; i++) {
 			CompoundEvent event = createTaxiUCCallEvent("example1" + Math.random());
 			testApi.publish(event);
 		}
 
 		// Wait
 		delay();
 
 		assertTrue(subscriber.getComplexEvents().size()==3);
 		
 
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			for (Component subcomponent : GCM.getContentController(root)
 					.getFcSubComponents()) {
 				logger.info("Terminating component: "
 						+ subcomponent.getFcType());
 				GCM.getGCMLifeCycleController(subcomponent)
 						.terminateGCMComponent();
 			}
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * One events contains multiple topics a person is talking about.
 	 */
 	@Test
 	public void testSetOperation() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException{
 	String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("play-bdpl-all-topics-he-talks-about-setoperation-example.eprq");
 
 		// Compile query
 		queryDispatchApi.registerQuery("abc", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		try {
 			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		} catch (ActiveObjectCreationException e) {
 			e.printStackTrace();
 		} catch (NodeException e) {
 			e.printStackTrace();
 		}
 
 		testApi.attach(subscriber);
 	
 		logger.info("Publish events");
 		for (int i = 0; i < 5; i++) {
 			CompoundEvent event = createFacebookTopicEvent("example1" + Math.random());
 			testApi.publish(event);
 		}
 
 		// Wait
 		delay();
 		
 		//Contains coffee and tea.
 		System.out.println();
 		assertTrue(subscriber.getComplexEvents().size()==5);
 		assertEquals(subscriber.getComplexEvents().get(0).getTriples().get(7).getMatchObject().toString(), "\"Tea\"");
 		assertEquals(subscriber.getComplexEvents().get(0).getTriples().get(8).getMatchObject().toString(), "\"Coffee\"");
 	}
 	
 	@Test
 	public void testCrisis01() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException, ActiveObjectCreationException, NodeException, InterruptedException {
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("play-bdpl-crisis-01a-radiation.eprq");
 
 		// Compile query
 		queryDispatchApi.registerQuery("queryIdf", queryString);
 
 		queryString = queryString.replace("?localisation", "?localisation2");
 		queryString = queryString.replace("?e1", "?e2");
 		queryString = queryString.replace("?id1", "?id2");
 		queryString = queryString.replace("?value", "?value2");
 		queryDispatchApi.registerQuery("queryIdd", queryString);
 
 
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		testApi.attach(subscriber);
 	
 		logger.info("Publish events");
 		for (int i = 0; i < 30; i++) {
 			LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 			Quadruple q1 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/MeasureEvent"));
 			Quadruple q2 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
 					NodeFactory.createURI("\"2013-10-21T16:41:46.671Z\"^^xsd:dateTime"));
 			Quadruple q3 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/source"),
 					NodeFactory.createURI("http://sources.event-processing.org/ids/WebApp#source"));
 			Quadruple q4 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/stream"),
 					NodeFactory.createURI("http://streams.event-processing.org/ids/situationalEvent#stream"));
 			Quadruple q5 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/localisation"),
 					NodeFactory.createURI("Karlsruhe"));
 			Quadruple q6 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/unit"),
 					NodeFactory.createURI("mSv"));
 			Quadruple q7 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/value"),
 					NodeFactory.createURI("110"));
 
 			quads.add(q1);
 			quads.add(q2);
 			quads.add(q3);
 			quads.add(q4);
 			quads.add(q5);
 			quads.add(q6);
 			quads.add(q7);
 			testApi.publish(new CompoundEvent(quads));
 			
 			Thread.sleep(100);
 		}
 
 		// Wait
 		delay();
 
 
 		assertEquals(subscriber.getComplexEvents().size(), 60);
 
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			for (Component subcomponent : GCM.getContentController(root)
 					.getFcSubComponents()) {
 				logger.info("Terminating component: "
 						+ subcomponent.getFcType());
 				GCM.getGCMLifeCycleController(subcomponent)
 						.terminateGCMComponent();
 			}
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testCrisis02b() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException, ActiveObjectCreationException, NodeException, InterruptedException {
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("play-bdpl-crisis-02b-windintensity.eprq");
 		System.out.println(queryString);
 		
 		// Compile query
 		queryDispatchApi.registerQuery("queryId", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		testApi.attach(subscriber);
 	
 		logger.info("Publish evetns");
 		for (int i = 0; i < 16; i++) {
 			LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 			Quadruple q1 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/MeasureEvent"));
 			Quadruple q2 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
 					NodeFactory.createURI("\"2013-10-21T16:41:46.671Z\"^^xsd:dateTime"));
 			Quadruple q4 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/stream"),
 					NodeFactory.createURI("http://streams.event-processing.org/ids/situationalEvent#stream"));
 			Quadruple q5 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/localisation"),
 					NodeFactory.createURI("Karlsruhe"));
 			Quadruple q6 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/unit"),
 					NodeFactory.createURI("km/h"));
 			Quadruple q7 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/value"),
 					NodeFactory.createURI("" + (100 * i) + ""));
 					//NodeFactory.createURI("" + (100) + ""));
 
 			quads.add(q1);
 			quads.add(q2);
 			quads.add(q4);
 			quads.add(q5);
 			quads.add(q6);
 			quads.add(q7);
 			testApi.publish(new CompoundEvent(quads));
 			
 			Thread.sleep(100);
 		}
 
 		// Wait
 		delay();
 
 
 		assertEquals(1, subscriber.getComplexEvents().size());
 
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			for (Component subcomponent : GCM.getContentController(root)
 					.getFcSubComponents()) {
 				logger.info("Terminating component: "
 						+ subcomponent.getFcType());
 				GCM.getGCMLifeCycleController(subcomponent)
 						.terminateGCMComponent();
 			}
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testCrisis01b() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException, ActiveObjectCreationException, NodeException, InterruptedException {
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("play-bdpl-crisis-01b-radiationincrease.eprq");
 		System.out.println(queryString);
 		
 		// Compile query
 		queryDispatchApi.registerQuery("queryId", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		testApi.attach(subscriber);
 	
 		logger.info("Publish evetns");
 		for (int i = 0; i < 16; i++) {
 			LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 			Quadruple q1 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/MeasureEvent"));
 			Quadruple q2 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
 					NodeFactory.createURI("\"2013-10-21T16:41:46.671Z\"^^xsd:dateTime"));
 			Quadruple q4 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/stream"),
 					NodeFactory.createURI("http://streams.event-processing.org/ids/situationalEvent#stream"));
 			Quadruple q5 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/localisation"),
 					NodeFactory.createURI("Karlsruhe"));
 			Quadruple q6 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/unit"),
 					NodeFactory.createURI("km/h"));
 			Quadruple q7 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.mines-albi.fr/nuclearcrisisevent/value"),
 					NodeFactory.createURI("" + (100 * i) + ""));
 					//NodeFactory.createURI("" + (100) + ""));
 
 			quads.add(q1);
 			quads.add(q2);
 			quads.add(q4);
 			quads.add(q5);
 			quads.add(q6);
 			quads.add(q7);
 			testApi.publish(new CompoundEvent(quads));
 			
 			Thread.sleep(100);
 		}
 
 		// Wait
 		delay();
 
 
 		assertEquals(1, subscriber.getComplexEvents().size());
 
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			for (Component subcomponent : GCM.getContentController(root)
 					.getFcSubComponents()) {
 				logger.info("Terminating component: "
 						+ subcomponent.getFcType());
 				GCM.getGCMLifeCycleController(subcomponent)
 						.terminateGCMComponent();
 			}
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void historicalQueryNoSharedVariables() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException, ActiveObjectCreationException, NodeException, InterruptedException {
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("historical-query-no-shard-variables.eprq");
 		System.out.println(queryString);
 		
 		// Compile query
 		queryDispatchApi.registerQuery("queryId", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		testApi.attach(subscriber);
 	
 		logger.info("Publish evetns");
 		for (int i = 0; i < 5; i++) {
 			CompoundEvent event = createTaxiUCCallEvent("historical-query-no-shard-variables_e" + i);
 			testApi.publish(event);
 		}
 		
 
 		// Wait
 		delay();
 
 
 		assertEquals(1, subscriber.getComplexEvents().size());
 
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			for (Component subcomponent : GCM.getContentController(root)
 					.getFcSubComponents()) {
 				logger.info("Terminating component: "
 						+ subcomponent.getFcType());
 				GCM.getGCMLifeCycleController(subcomponent)
 						.terminateGCMComponent();
 			}
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void realtimeHistoricEvents() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException, ActiveObjectCreationException, NodeException, InterruptedException {
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("patterns/historic-realtime-query2.eprq");
 
 		// Compile query
 		queryDispatchApi.registerQuery("play-bdpl-overall-scenario-03.eprq", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		testApi.attach(subscriber);
 	
 		logger.info("Publish evetns");
 		for (int i = 0; i < 30; i++) {
 			LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 			Quadruple q1 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 					NodeFactory.createURI("http://events.event-processing.org/types/google"));
 			Quadruple q2 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
 					NodeFactory.createURI("\"2013-10-21T16:41:46.671Z\"^^xsd:dateTime"));
 			Quadruple q3 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/screenName"),
 					NodeFactory.createURI("screen1./..-+"));
 			Quadruple q4 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " #event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i),
 					NodeFactory.createURI("http://events.event-processing.org/types/stream"),
 					NodeFactory.createURI("http://streams.event-processing.org/ids/TwitterFeed#stream"));
 
 			quads.add(q1);
 			quads.add(q2);
 			quads.add(q3);
 			quads.add(q4);
 			testApi.publish(new CompoundEvent(quads));
 			Thread.sleep(100);
 			
 			quads = new LinkedList<Quadruple>();
 			q1 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " b#event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i + "b"),
 					NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 					NodeFactory.createURI("http://events.event-processing.org/types/apple"));
 			q2 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " b#event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i + "b"),
 					NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
 					NodeFactory.createURI("\"2013-10-21T16:41:46.671Z\"^^xsd:dateTime"));
 			q3 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " b#event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i + "b"),
 					NodeFactory.createURI("http://events.event-processing.org/types/screenName"),
 					NodeFactory.createURI("screen2./..-+"));
 			q4 = new Quadruple(
 					NodeFactory.createURI("http://events.event-processing.org/ids/webapp_11_measure_d0f808a8-029d-4e6a-aa8c-ad61d936d8a4" + i + " b#event"),
 					NodeFactory.createURI("http://events.event-processing.org/eventId/" + i + "b"),
 					NodeFactory.createURI("http://events.event-processing.org/types/stream"),
 					NodeFactory.createURI("http://streams.event-processing.org/ids/TwitterFeed#stream"));
 
 			quads.add(q1);
 			quads.add(q2);
 			quads.add(q3);
 			quads.add(q4);
 			testApi.publish(new CompoundEvent(quads));
 			
 			Thread.sleep(100);
 		}
 
 		// Wait
 		delay();
 
 
 		assertEquals(subscriber.getComplexEvents().size(), 0);
 
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			for (Component subcomponent : GCM.getContentController(root)
 					.getFcSubComponents()) {
 				logger.info("Terminating component: "
 						+ subcomponent.getFcType());
 				GCM.getGCMLifeCycleController(subcomponent)
 						.terminateGCMComponent();
 			}
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testClic2callPatternPlusTweet() throws IllegalLifeCycleException,
 			NoSuchInterfaceException, ADLException, InterruptedException, QueryDispatchException {
 
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQueries("play-epsparql-clic2call-plus-tweet.eprq");
 
 		// Compile query
 		queryDispatchApi.registerQuery("example", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		try {
 			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		} catch (ActiveObjectCreationException e) {
 			e.printStackTrace();
 		} catch (NodeException e) {
 			e.printStackTrace();
 		}
 		
 		testApi.attach(subscriber);
 		
 		
 		logger.info("Publish events");
 		for (int i = 0; i < 10; i++) {
 			CompoundEvent event = createTaxiUCCallEvent("example" + Math.random());
 			logger.debug("Publish event" +  event);
 			testApi.publish(event);
 		}
 
 		// Wait
 		delay();
 
 		assertTrue(subscriber.getComplexEvents().size()==9);
 	
 		
 		
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
 				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
 			 }
 	
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 }
 
 	/**
 	 * Aggregate values in time-window.
 	 * A complex event is created if the average value is >=5 in a 5s window.
 	 * Event e1,e2,e3 are in window w1 and the average is > 5.
 	 * For event e4 the average is < 5, because e1 is out of window.
 	 */
 	@Test
 	public void testAggregateAverageWindSpeed() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException, InterruptedException{
 		String queryString;
 
 		// Get query.
		queryString = getSparqlQueries("patterns/weather_wind_speed.eprq");
 
 		// Compile query
 		queryDispatchApi.registerQuery("example", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		try {
 			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		} catch (ActiveObjectCreationException e) {
 			e.printStackTrace();
 		} catch (NodeException e) {
 			e.printStackTrace();
 		}
 		
 		testApi.attach(subscriber);
 		
 		
 		logger.info("Publish events");
 
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 20)); // e1 start window w1.
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 1));  // e2
 		Thread.sleep(3000);													 // wait 3s
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 1));  // e3
 		Thread.sleep(2000);													 // wait 2s
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 1));  // e4 is out of window w1.
 	
 		
 		// Wait
 		delay();
 		assertTrue(subscriber.getComplexEvents().size()==3);
 		
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
 				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
 			 }
 	
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static CompoundEvent createTaxiUCCallEvent(String eventId){
 		
 		UcTelcoCall event = new UcTelcoCall(
 				// set the RDF context part
 				EventHelpers.createEmptyModel(eventId),
 				// set the RDF subject
 				eventId + EVENT_ID_SUFFIX,
 				// automatically write the rdf:type statement
 				true);
 
 		// Run some setters of the event
 		event.setUcTelcoCalleePhoneNumber("49123456789");
 		event.setUcTelcoCallerPhoneNumber("49123498765");
 		event.setUcTelcoDirection("incoming");
 		
 		double longitude = 123;
 		double latitude = 345;
 		EventHelpers.setLocationToEvent(event, longitude, latitude);
 		
 		// Create a Calendar for the current date and time
 		event.setEndTime(Calendar.getInstance());
 		event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));
 
 		//Push events.
 		return EventCloudHelpers.toCompoundEvent(event);
 	}
 	
 	private static CompoundEvent createWeatherEvent(String eventId, double value) {
 
 		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 
 		Quadruple q1 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e1"),
 				NodeFactory.createURI("http://events.event-processing.org/types/stream"),
 				NodeFactory.createURI("http://streams.event-processing.org/ids/Srbench#stream"));
 
 		Quadruple q3 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e2"),
 				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#WindSpeedObservation"));
 
 		Quadruple q4 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e2"),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#observedProperty"),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#_WindSpeed"));
 
 		Quadruple q5 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e2"),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"));
 
 		Quadruple q6 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
 				NodeFactory.createURI(value + ""));
 
 //		Quadruple q7 = new Quadruple(
 //				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 //						+ eventId),
 //				NodeFactory.createURI("http://prefix.example.com/e1"),
 //				NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
 //				NodeFactory.createURI(new SimpleDateFormat(
 //						eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601)
 //						.format(new Date())));
 		Quadruple q7 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e1"),
 				NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
 				NodeFactory.createURI(System.currentTimeMillis() + ""));
 
 		Quadruple q8 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e2"),
 				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#procedure"),
 				NodeFactory.createURI("http://sensor.example.com/S1"));
 
 		quads.add(q1);
 		// quads.add(q2);
 		quads.add(q3);
 		quads.add(q4);
 		quads.add(q5);
 		quads.add(q6);
 		quads.add(q7);
 		quads.add(q8);
 
 		return new CompoundEvent(quads);
 	}
 	
 	private static CompoundEvent createFacebookTopicEvent(String eventId) {
 
 		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 
 		Quadruple q1 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
 				NodeFactory.createURI("http://prefix.example.com/e1"),
 				NodeFactory.createURI("http://events.event-processing.org/types/stream"),
 				NodeFactory.createURI("http://streams.event-processing.org/ids/FacebookStatusFeed#stream"));
 
 		Quadruple q3 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e1"),
 				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 				NodeFactory.createURI("http://events.event-processing.org/types/FacebookStatusFeedEvent"));
 
 		Quadruple q4 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e1"),
 				NodeFactory.createURI("http://events.event-processing.org/types/status"),
 				NodeFactory.createURI("Tea"));
 		
 		Quadruple q5 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e1"),
 				NodeFactory.createURI("http://events.event-processing.org/types/status"),
 				NodeFactory.createURI("Coffee"));
 
 		Quadruple q6 = new Quadruple(
 				NodeFactory.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				NodeFactory.createURI("http://prefix.example.com/e1"),
 				NodeFactory.createURI("http://graph.facebook.com/schema/user#name"),
 				NodeFactory.createURI("Max"));
 	
 		quads.add(q1);
 		quads.add(q3);
 		quads.add(q4);
 		quads.add(q5);
 		quads.add(q6);
 
 		return new CompoundEvent(quads);
 	}
 
 	private String getSparqlQueries(String queryFile){
 		try {
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
 			BufferedReader br =new BufferedReader(new InputStreamReader(is));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			
 			while (null != (line = br.readLine())) {
 					sb.append(line);
 					sb.append("\n");
 			}
 			//System.out.println(sb.toString());
 			br.close();
 			is.close();
 
 			return sb.toString();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	
 	}
 	
 	private void delay(){
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
