 package org.cytoscape.integration; 
 
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import org.junit.Test;
import org.junit.Before;
 import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
import java.util.Properties;
import java.util.Dictionary;
 
 /**
  * A Pax Exam support class that provides methods useful for verifying 
  * the existance of OSGi services.
  */
 public abstract class ServiceTestSupport {
 
 	/**
 	 * The OSGi BundleContext made available for additional testing.
 	 */
 	@Inject
 	protected BundleContext bundleContext;
 
 	/**
 	 * Registers a mock service of type clazz with the OSGi service registry. 
 	 * This is meant to be used to provide prerequisite services for testing
 	 * purposes. Will register the service with an empty Properties object. 
 	 * @param clazz The class type describing the type of mock service to register.
 	 */
 	protected void registerMockService(Class<?> clazz) {
 		registerMockService(clazz, new Properties());
 	}
 
 	/**
 	 * Registers a mock service of type clazz with the OSGi service registry. 
 	 * This is meant to be used to provide prerequisite services for testing
 	 * purposes.
 	 * @param clazz The class type describing the type of mock service to register.
 	 * @param d The service properties. 
 	 */
 	protected void registerMockService(Class<?> clazz, Dictionary d) {
 		bundleContext.registerService(clazz.getName(), createMock(clazz), d);
 	}
 
 	/**
 	 * Registers a mock service of type clazz with the OSGi service registry. 
 	 * This is meant to be used to provide prerequisite services for testing
 	 * purposes. Will register the service with an empty Properties object. 
 	 * @param clazz The class type describing the type of mock service to register.
 	 */
 	protected void registerMockService(final Class<?> clazz, final Object mockObject) {
 		registerMockService(clazz, mockObject, new Properties());
 	}
 
 	/**
 	 * Registers a mock service of type clazz with the OSGi service registry. 
 	 * This is meant to be used to provide prerequisite services for testing
 	 * purposes.
 	 * @param clazz The class type describing the type of mock service to register.
 	 * @param d The service properties. 
 	 */
 	protected void registerMockService(final Class<?> clazz, final Object mockObject,
 					   final Dictionary d)
 	{
 		if (!clazz.isAssignableFrom(mockObject.getClass()))
 			throw new IllegalArgumentException("the mock object is an instance of the wrong class.");
 		bundleContext.registerService(clazz.getName(), mockObject, d);
 	}
 
 	/**
 	 * Asserts that a service of the specified type exists. Uses
 	 * a wait time of 1 second.
 	 * @param clazz The service interface type to be checked. 
 	 */
 	protected void checkService(Class<?> clazz) {
 		checkService(clazz,1000);
 	}
 
 	/**
 	 * Asserts that a service of the specified type exists.
 	 * @param clazz The service interface type to be checked. 
 	 * @param waitTime The time the service tracker should wait to 
 	 * find the specified service in milliseconds.
 	 */
 	protected void checkService(Class<?> clazz, int waitTime) {
 		try {  
 			ServiceTracker tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
 			tracker.open();
 			Object service = tracker.waitForService(waitTime);
 			tracker.close();
 			assertNotNull(service);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 }
