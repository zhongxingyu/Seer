 package de.guderlei.pubsub.integrationtest;
 
 import java.io.File;
 
 import javax.servlet.Servlet;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Inject;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 
 
 import de.guderlei.pubsub.model.Producer;
 import de.guderlei.pubsub.model.Subscriber;
 import de.guderlei.pubsub.producer.PublisherServlet;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.ops4j.pax.exam.CoreOptions.*;
 import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
 
 @RunWith(JUnit4TestRunner.class)
 public class ServiceAvailability {
 	@Inject
 	BundleContext bundleContext;
 	
 	@Configuration
 	public Option[] configure() {
 		return options(equinox(), felix(), dsProfile(), configProfile(), 
 				rawPaxRunnerOption("http.proxyHost", "proxy"),
 				rawPaxRunnerOption("http.proxyPort", "3128"),
 				provision(
 				bundle(new File("./../model/build/libs/model-0.0.1.jar").toURI().toString()),
 				bundle(new File("./../subscribers/ds_subscriber/build/libs/ds_subscriber-0.0.1.jar").toURI().toString()),
 				bundle(new File("./../subscribers/peaberry_subscriber/build/libs/peaberry_subscriber-0.0.1.jar").toURI().toString()),
 				bundle(new File("./../message-hub/build/libs/message-hub-0.0.1.jar").toURI().toString()),
 				bundle(new File("./../producer/build/libs/producer-0.0.1.jar").toURI().toString()),
 				bundle(new File("./../lib/compile/jsr305-1.3.9.jar").toURI().toString()),
 				bundle(new File("./../lib/runtime/aopalliance-1.0.jar").toURI().toString()),
 				bundle(new File("./../lib/compile/guice-2.0.jar").toURI().toString()),
 				bundle(new File("./../lib/compile/peaberry-1.1.1.jar").toURI().toString()),
 				bundle(new File("./../lib/runtime/peaberry.activation-1.2-SNAPSHOT.jar").toURI().toString()),
 				bundle(new File("./../lib/compile/org.apache.felix.http.bundle-2.0.4.jar").toURI().toString()),
 				bundle(new File("./../lib/compile/org.apache.felix.log-1.0.0.jar").toURI().toString())
 		));
 	}
 	
 	@Test
 	public void bundle_context_available(){
 		assertNotNull(bundleContext);
 	}
 	
 	@Test
 	public void subscriber_bundles_are_started(){
 		boolean ds = false;
 		boolean peaberry = false;
 		for(Bundle bundle: bundleContext.getBundles()){
 			if(bundle.getSymbolicName().equals("pubsub.ds_subscriber")){
 				ds = true;
 				assertEquals(bundle.getState(), Bundle.ACTIVE);
 			}
 			if(bundle.getSymbolicName().equals("pubsub.peaberry_subscriber")){
 				peaberry=true;
 				assertEquals(bundle.getState(), Bundle.ACTIVE);
 			}
 		}
 		assertTrue(ds);
 		assertTrue(peaberry);
 	}
 	
 	@Test
	public void peaberry_activation_is_started() throws BundleException{		
 		for(Bundle bundle: bundleContext.getBundles()){
 			if(bundle.getSymbolicName().equals("org.ops4j.peaberry.activation")){
				// equinox seems to start the bundle on demand ...
				bundle.start();
 				assertEquals("peaberry bundle not active", Bundle.ACTIVE, bundle.getState());
 			}
 		}
 	}
 	
 	@Test
	public void two_subscriber_services_are_available() throws InterruptedException, BundleException{
			//equinox seems to start some bundles on demand ...
			for(Bundle bundle: bundleContext.getBundles()){
				bundle.start();
			}
		
 			ServiceTracker tracker = new ServiceTracker(bundleContext, Subscriber.class.getName(),null);
 			tracker.open(true);
 			
 			ServiceReference[] subscribers = tracker.getServiceReferences();
 			assertNotNull("no subscribers found", subscribers);			
 			assertEquals(2, subscribers.length);
 			
 			tracker.close();			
 	}
 	
 	@Test
 	public void one_producer_service_is_available() throws InterruptedException{
 		ServiceTracker tracker = new ServiceTracker(bundleContext, Producer.class.getName(),null);
 		tracker.open();		
 		Producer prod = (Producer) tracker.waitForService(3000);
 		assertNotNull(prod);		
 		assertEquals("de.guderlei.pubsub.hub.MessageHub", prod.getClass().getName());
 		tracker.close();
 	}
 	
 	/**
 	 * Checks whether one {@link PublisherServlet} is registered
 	 */
 	@Test
 	public void one_servlet_is_registered()throws InterruptedException{
 		ServiceTracker tracker = new ServiceTracker(bundleContext, Servlet.class.getName(),null);
 		tracker.open();
 		
 		Servlet srvlt = (Servlet) tracker.waitForService(3000);
 		assertNotNull(srvlt);
 		assertEquals("PublisherServlet", srvlt.getClass().getSimpleName());			
 		tracker.close();
 	}
 }
