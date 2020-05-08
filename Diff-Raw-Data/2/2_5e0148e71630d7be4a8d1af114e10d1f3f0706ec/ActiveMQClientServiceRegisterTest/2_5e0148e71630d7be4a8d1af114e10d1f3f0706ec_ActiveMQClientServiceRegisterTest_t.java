 /*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
 package org.eclipse.ecf.tests.provider.jms.activemq.remoteservice;
 
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Properties;
 
 import org.eclipse.ecf.core.ContainerFactory;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.util.Trace;
 import org.eclipse.ecf.remoteservice.Constants;
 import org.eclipse.ecf.remoteservice.IRemoteCall;
 import org.eclipse.ecf.remoteservice.IRemoteService;
 import org.eclipse.ecf.tests.internal.osgi.services.distribution.Activator;
 import org.eclipse.ecf.tests.osgi.services.distribution.AbstractServiceRegisterListenerTest;
 import org.eclipse.ecf.tests.osgi.services.distribution.TestService1;
 import org.eclipse.ecf.tests.osgi.services.distribution.TestServiceInterface1;
 import org.eclipse.ecf.tests.provider.jms.activemq.ActiveMQ;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 
 public class ActiveMQClientServiceRegisterTest extends AbstractServiceRegisterListenerTest {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		setClientCount(2);
 		createServerAndClients();
 		connectClients();
 		setupRemoteServiceAdapters();
 	}
 
 	protected String getClientContainerName() {
 		return ActiveMQ.CLIENT_CONTAINER_NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.tests.provider.jms.remoteservice.AbstractRemoteServiceTestCase#getServerContainerName()
 	 */
 	protected String getServerContainerName() {
 		return ActiveMQ.SERVER_CONTAINER_NAME;
 	}
 
 	protected ID getServerConnectID(int client) {
 		return IDFactory.getDefault().createID("ecf.namespace.jmsid", ActiveMQ.TARGET_NAME);
 	}
 	
 	protected IContainer createServer() throws Exception {
 		return ContainerFactory.getDefault().createContainer(getServerContainerName(), new Object[] {ActiveMQ.TARGET_NAME});
 	}
 
 	protected void tearDown() throws Exception {
 		cleanUpServerAndClients();
 		super.tearDown();
 	}
 
 	public void testRegisterServer() throws Exception {
 		Properties props = new Properties();
 		// *For testing purposes only* -- Set the service container id property, so that the service is not
 		// distributed by both the client and server (which are both running in the same process
 		// for junit plugin tests)
 		IContainer clientContainer = getClient(0);
 		props.put(Constants.SERVICE_CONTAINER_ID, clientContainer.getID());
 		
 		// Set OSGI property that identifies this service as a service to be remoted
 		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
 		// Actually register with default service (IConcatService)
 		ServiceRegistration registration = registerDefaultService(props);
 		// Wait a while
 		Thread.sleep(REGISTER_WAIT);
 		// Then unregister
 		registration.unregister();
 		Thread.sleep(REGISTER_WAIT);
 	}
 	
 	public void testGetProxy() throws Exception {
 		String classname = TestServiceInterface1.class.getName();
 		// Setup service tracker for client
 		ServiceTracker st = createProxyServiceTracker(classname);
 		
 		// Server - register service with required OSGI property and some test properties
 		Properties props = new Properties();
 		// *For testing purposes only* -- Set the service container id property, so that the service is not
 		// distributed by both the client and server (which are both running in the same process
 		// for junit plugin tests)
 		IContainer clientContainer = getClient(1);
 		props.put(Constants.SERVICE_CONTAINER_ID, clientContainer.getID());
 		// Set required OSGI property that identifies this service as a service to be remoted
 		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
 		// Put property foo with value bar into published properties
 		String testPropKey = "foo";
 		String testPropVal = "bar";
 		props.put(testPropKey, testPropVal);
 		// Actually register and wait a while
 		ServiceRegistration registration = registerService(classname, new TestService1(),props);
 		Thread.sleep(REGISTER_WAIT);
 		
 		// Client - Get service references that are proxies
 		ServiceReference [] remoteReferences = st.getServiceReferences();
 		assertTrue(remoteReferences != null);
 		assertTrue(remoteReferences.length > 0);
 		for(int i=0; i < remoteReferences.length; i++) {
 			// Get OBJECTCLASS property from first remote reference
 			String[] classes = (String []) remoteReferences[i].getProperty(org.osgi.framework.Constants.OBJECTCLASS);
 			assertTrue(classes != null);
 			// Check object class
 			assertTrue(classname.equals(classes[0]));
 			// Check the prop
 			String prop = (String) remoteReferences[i].getProperty(testPropKey);
 			assertTrue(prop != null);
 			assertTrue(prop.equals(testPropVal));
 		}
 		// Now unregister original registration and wait
 		registration.unregister();
 		st.close();
 		Thread.sleep(REGISTER_WAIT);
 	}
 
 	public void testGetAndUseProxy() throws Exception {
 		String classname = TestServiceInterface1.class.getName();
 		// Setup service tracker for client
 		ServiceTracker st = createProxyServiceTracker(classname);
 		
 		// Server - register service with required OSGI property and some test properties
 		Properties props = new Properties();
 		// *For testing purposes only* -- Set the service container id property, so that the service is not
 		// distributed by both the client and server (which are both running in the same process
 		// for junit plugin tests)
 		IContainer clientContainer = getClient(0);
 		props.put(Constants.SERVICE_CONTAINER_ID, clientContainer.getID());
 		// Set required OSGI property that identifies this service as a service to be remoted
 		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
 		// Actually register and wait a while
 		ServiceRegistration registration = registerService(classname, new TestService1(),props);
 		Thread.sleep(REGISTER_WAIT);
 		
 		// Client - Get service references from service tracker
 		ServiceReference [] remoteReferences = st.getServiceReferences();
 		assertTrue(remoteReferences != null);
 		assertTrue(remoteReferences.length > 0);
 		
 		for(int i=0; i < remoteReferences.length; i++) {
 			// Get proxy/service
 			TestServiceInterface1 proxy = (TestServiceInterface1) getContext().getService(remoteReferences[0]);
 			assertNotNull(proxy);
 			// Now use proxy
 			String result = proxy.doStuff1();
 			Trace.trace(Activator.PLUGIN_ID, "proxy.doStuff1 result="+result);
 			assertTrue(TestServiceInterface1.TEST_SERVICE_STRING1.equals(result));
 		}
 		
 		// Unregister on server and wait
 		registration.unregister();
 		st.close();
 		Thread.sleep(REGISTER_WAIT);
 	}
 
 	public void testGetAndUseIRemoteService() throws Exception {
 		String classname = TestServiceInterface1.class.getName();
 		// Setup service tracker for client
 		ServiceTracker st = createProxyServiceTracker(classname);
 		
 		// Server - register service with required OSGI property and some test properties
 		Properties props = new Properties();
 		// *For testing purposes only* -- Set the server container id property, so that the service is not
 		// distributed by both the client and server (which are both running in the same process
 		// for junit plugin tests)
 		IContainer clientContainer = getClient(1);
 		props.put(Constants.SERVICE_CONTAINER_ID, clientContainer.getID());
 		// Set required OSGI property that identifies this service as a service to be remoted
 		props.put(SERVICE_EXPORTED_INTERFACES, new String[] {SERVICE_EXPORTED_INTERFACES_WILDCARD});
 		// Actually register and wait a while
 		ServiceRegistration registration = registerService(classname, new TestService1(),props);
 		Thread.sleep(REGISTER_WAIT);
 		
 		// Client - Get service references from service tracker
 		ServiceReference [] remoteReferences = st.getServiceReferences();
 		assertTrue(remoteReferences != null);
 		assertTrue(remoteReferences.length > 0);
 		
 		for(int i=0; i < remoteReferences.length; i++) {
 			Object o = remoteReferences[i].getProperty(SERVICE_IMPORTED);
 			assertNotNull(o);
 			assertTrue(o instanceof IRemoteService);
 			IRemoteService rs = (IRemoteService) o;
 			// Now call rs methods
			IRemoteCall call = createRemoteCall();
 			if (call != null) {
 				// Call synchronously
 				Object result = rs.callSync(call);
 				Trace.trace(Activator.PLUGIN_ID, "callSync.doStuff1 result="+result);
 				assertNotNull(result);
 				assertTrue(result instanceof String);
 				assertTrue(TestServiceInterface1.TEST_SERVICE_STRING1.equals(result));
 			}
 		}
 		
 		// Unregister on server
 		registration.unregister();
 		st.close();
 		Thread.sleep(REGISTER_WAIT);
 	}
 
 	/*
 	public void testGetExposedServicesFromDistributionProvider() throws Exception {
 		String classname = TestServiceInterface1.class.getName();
 		// Setup service tracker for distribution provider
 		ServiceTracker st = new ServiceTracker(getContext(),DistributionProvider.class.getName(),null);
 		st.open();
 		DistributionProvider distributionProvider = (DistributionProvider) st.getService();
 		assertNotNull(distributionProvider);
 		
 		// The returned collection should not be null
 		Collection exposedServices = distributionProvider.getExposedServices();
 		assertNotNull(exposedServices);
 
 		// Server - register service with required OSGI property and some test properties
 		Properties props = new Properties();
 		// *For testing purposes only* -- Set the server container id property, so that the service is not
 		// distributed by both the client and server (which are both running in the same process
 		// for junit plugin tests)
 		IContainer clientContainer = getClient(0);
 		props.put(Constants.SERVICE_CONTAINER_ID, clientContainer.getID());
 		// Set required OSGI property that identifies this service as a service to be remoted
 		props.put(REMOTE_INTERFACES, new String[] {REMOTE_INTERFACES_WILDCARD});
 		// Actually register and wait a while
 		ServiceRegistration registration = registerService(classname, new TestService1(),props);
 		Thread.sleep(REGISTER_WAIT);
 
 		// Client
 		exposedServices = distributionProvider.getExposedServices();
 		assertNotNull(exposedServices);
 		int exposedLength = exposedServices.size();
 		assertTrue(exposedLength > 0);
 		for(Iterator i=exposedServices.iterator(); i.hasNext(); ) {
 			Object o = ((ServiceReference) i.next()).getProperty(REMOTE_INTERFACES);
 			assertTrue(o != null);
 		}
 
 		// Unregister on server
 		registration.unregister();
 		st.close();
 		Thread.sleep(REGISTER_WAIT);
 		
 		// Check to see that the exposed service went away
 		exposedServices= distributionProvider.getExposedServices();
 		assertNotNull(exposedServices);
 		assertTrue(exposedServices.size() == (exposedLength - 1));
 
 	}
 
 	public void testGetRemoteServicesFromDistributionProvider() throws Exception {
 		String classname = TestServiceInterface1.class.getName();
 		// Setup service tracker for distribution provider
 		ServiceTracker st = new ServiceTracker(getContext(),DistributionProvider.class.getName(),null);
 		st.open();
 		DistributionProvider distributionProvider = (DistributionProvider) st.getService();
 		assertNotNull(distributionProvider);
 		
 		// The returned collection should not be null
 		Collection remoteServices = distributionProvider.getRemoteServices();
 		assertNotNull(remoteServices);
 
 		// Server - register service with required OSGI property and some test properties
 		Properties props = new Properties();
 		// *For testing purposes only* -- Set the server container id property, so that the service is not
 		// distributed by both the client and server (which are both running in the same process
 		// for junit plugin tests)
 		IContainer clientContainer = getClient(1);
 		props.put(Constants.SERVICE_CONTAINER_ID, clientContainer.getID());
 		// Set required OSGI property that identifies this service as a service to be remoted
 		props.put(REMOTE_INTERFACES, new String[] {REMOTE_INTERFACES_WILDCARD});
 		// Actually register and wait a while
 		ServiceRegistration registration = registerService(classname, new TestService1(),props);
 		Thread.sleep(REGISTER_WAIT);
 		
 		// Check that distribution provider (client) has remote services now
 		remoteServices = distributionProvider.getRemoteServices();
 		assertNotNull(remoteServices);
 		int remotesLength = remoteServices.size();
 		assertTrue(remotesLength > 0);
 		for(Iterator i=remoteServices.iterator(); i.hasNext(); ) {
 			Object o = ((ServiceReference) i.next()).getProperty(REMOTE);
 			assertTrue(o != null);
 		}
 		// Unregister on server
 		registration.unregister();
 		st.close();
 		Thread.sleep(REGISTER_WAIT);
 		
 		// Remote services should have gone down by one (because of unregister
 		remoteServices= distributionProvider.getRemoteServices();
 		assertNotNull(remoteServices);
 		assertTrue(remoteServices.size() < remotesLength);
 
 	}
 */
 }
