 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.plugins.resolver.test;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.easymock.EasyMock.reset;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.jbi.runtime.ComponentRegistry;
 import org.apache.servicemix.jbi.runtime.impl.EndpointImpl;
 import org.apache.servicemix.jbi.runtime.impl.MessageExchangeImpl;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.Exchange;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.api.Pattern;
 import org.apache.servicemix.nmr.core.ChannelImpl;
 import org.apache.servicemix.nmr.core.ExchangeImpl;
 import org.eclipse.swordfish.api.Interceptor;
 import org.eclipse.swordfish.api.registry.EndpointDescription;
 import org.eclipse.swordfish.api.registry.EndpointDocumentProvider;
 import org.eclipse.swordfish.api.registry.EndpointExtractor;
 import org.eclipse.swordfish.api.registry.ServiceDescription;
 import org.eclipse.swordfish.core.resolver.ServiceResolver;
 import org.eclipse.swordfish.core.resolver.ServiceResolverHolder;
 import org.eclipse.swordfish.core.test.util.OsgiSupport;
 import org.eclipse.swordfish.core.test.util.ServiceMixSupport;
 import org.eclipse.swordfish.core.test.util.ServiceMixSupport.ExchangeProcessorImpl;
 import org.eclipse.swordfish.core.test.util.base.TargetPlatformOsgiTestCase;
 import org.eclipse.swordfish.core.test.util.mock.MockInterceptor;
 import org.eclipse.swordfish.core.util.xml.StringSource;
 import org.eclipse.swordfish.plugins.resolver.ServiceResolverImpl;
 import org.osgi.framework.ServiceReference;
 
 
 public class ServiceResolverTest extends TargetPlatformOsgiTestCase {
 
 	private QName service1 = new QName("namespace", "Service1");
     private QName service2 = new QName("namespace", "Service2");
     private QName service2Interface = new QName("interface", "Service2");
 	private QName operation = new QName("namespace", "operation");
 
     private EndpointImpl endpointService1;
     private EndpointImpl endpointService2;
 
     MockInterceptor mockInterceptor;
 
 	private ServiceResolver resolverMock = createMock(ServiceResolver.class);
 	private ServiceResolver resolverRef;
 
 	@Override
 	protected void onSetUp() throws Exception {
         NMR nmr = OsgiSupport.getReference(bundleContext, NMR.class);
         assertNotNull(nmr);
 
 		endpointService1 = ServiceMixSupport.createAndRegisterEndpoint(nmr, service1, null);
         endpointService2 = ServiceMixSupport.createAndRegisterEndpoint(nmr, service2,
             new ExchangeProcessorImpl(service2.toString()));
 
         mockInterceptor = new MockInterceptor();
         addRegistrationToCancel(
         	bundleContext.registerService(Interceptor.class.getCanonicalName(),
             mockInterceptor, null));
         Thread.sleep(500);
 
 		ServiceResolverHolder holder = OsgiSupport.getReference(bundleContext, ServiceResolverHolder.class);
     	resolverRef = holder.getServiceResolver();
 
     	reset(resolverMock);
 
 		super.onSetUp();
 	}
 
 	@Override
 	protected void onTearDown() throws Exception {
         NMR nmr = OsgiSupport.getReference(bundleContext, NMR.class);
         assertNotNull(nmr);
 
     	ServiceMixSupport.unregisterEndpoints(nmr, endpointService1, endpointService2);
 
 		ServiceResolverHolder holder = OsgiSupport.getReference(bundleContext, ServiceResolverHolder.class);
     	holder.setServiceResolver(resolverRef);
     	assertFalse(holder.getServiceResolver().equals(resolverMock));
 
 		super.onTearDown();
 	}
 
     public void test1ResolverInitialization() throws Exception {
         ServiceReference ref =
         	bundleContext.getServiceReference(ServiceResolver.class.getName());
         assertNotNull("Reference to ServiceResolver is null", ref);
 
         ServiceResolver resolver = (ServiceResolver) bundleContext.getService(ref);
         assertNotNull("Couldn't find the ServiceResolver service", resolver);
 
         assertNotNull("Endpoint document provider is not initialized",
         	resolver.getServiceDescriptionProvider());
         assertNotNull("Endpoint extractors are not initialized",
             resolver.getEndpointExtractors());
         assertTrue("Endpoint extractors are not initialized",
         	resolver.getEndpointExtractors().size() > 0);
     }
 
     public void test2SuccessfullResolverUsingStub() throws Exception {
 		ServiceDescriptionStub stubDescr = new ServiceDescriptionStub(service2);
 		EndpointDescriptionStub stubEndp = new EndpointDescriptionStub(stubDescr);
 
 		List<EndpointDescription> stubEndpoints = new ArrayList<EndpointDescription>();
 		stubEndpoints.add(stubEndp);
 
 		ServiceResolverHolder holder = OsgiSupport.getReference(bundleContext, ServiceResolverHolder.class);
         holder.setServiceResolver(resolverMock);
 
 		expect(resolverMock.getEndpointsFor(service2Interface)).andStubReturn(stubEndpoints);
 		replay(resolverMock);
 
 		setExchangeThroughNMR();
 
 		verify(resolverMock);
     }
 
     public void test3SuccessfullResolverSequence() throws Exception {
 		ServiceResolverHolder holder = OsgiSupport.getReference(bundleContext, ServiceResolverHolder.class);
         holder.setServiceResolver(resolverMock);
 
 		EndpointDescription endpointMock = createMock(EndpointDescription.class);
 		ServiceDescription serviceMock = createMock(ServiceDescription.class);
 
 		List<EndpointDescription> endpoints = new ArrayList<EndpointDescription>();
 		endpoints.add(endpointMock);
 
 		expect(resolverMock.getEndpointsFor(service2Interface)).andReturn(endpoints).once();
 		expect(endpointMock.getServiceDescription()).andReturn(serviceMock).once();
 		expect(serviceMock.getServiceName()).andReturn(service2).once();
 
 		replay(resolverMock);
 		replay(endpointMock);
 		replay(serviceMock);
 
 		setExchangeThroughNMR();
 
 		verify(resolverMock);
 		verify(endpointMock);
 		verify(serviceMock);
     }
 
     public void test4FailedNoServiceDescription() throws Exception {
 		ServiceResolverHolder holder = OsgiSupport.getReference(bundleContext, ServiceResolverHolder.class);
         holder.setServiceResolver(resolverMock);
 
 		List<EndpointDescription> endpointsEmpty = new ArrayList<EndpointDescription>();
 
 		expect(resolverMock.getEndpointsFor(service2Interface)).andReturn(endpointsEmpty).once();
 		replay(resolverMock);
 
 		try {
 			setExchangeThroughNMR();
 			fail("The routing of exchange must fail");
 		} catch (RuntimeException e) {
 			// expected result
 		}
 		verify(resolverMock);
     }
 
     public void test5NoSuitableEndpointFound() throws Exception {
 		ServiceResolverHolder holder = OsgiSupport.getReference(bundleContext, ServiceResolverHolder.class);
         holder.setServiceResolver(resolverMock);
 
 		EndpointDescription endpointMock = createMock(EndpointDescription.class);
 		ServiceDescription serviceMock = createMock(ServiceDescription.class);
 
 		List<EndpointDescription> endpoints = new ArrayList<EndpointDescription>();
 		endpoints.add(endpointMock);
 
 		expect(resolverMock.getEndpointsFor(service2Interface)).andReturn(endpoints).once();
 		expect(endpointMock.getServiceDescription()).andReturn(serviceMock).times(2);
 		expect(serviceMock.getServiceName()).andReturn(new QName("dummy", "dummy")).times(2);
 		expect(endpointMock.getAddress()).andReturn("non-existing-one").once();
 		expect(endpointMock.getName()).andReturn("endpoint-name").once();
 
 		replay(resolverMock);
 		replay(endpointMock);
 		replay(serviceMock);
 
 		try {
 			setExchangeThroughNMR();
 			fail("The routing of exchange must fail");
 		} catch (RuntimeException e) {
 			// expected result
 		}
 
 		verify(resolverMock);
 		verify(endpointMock);
 		verify(serviceMock);
     }
 
     public void test6ServiceResolverInvocation() throws Exception {
         ServiceReference ref =
         	bundleContext.getServiceReference(ServiceResolver.class.getName());
         assertNotNull("Reference to ServiceResolver is null", ref);
 
         ServiceResolver resolver = (ServiceResolver) bundleContext.getService(ref);
         assertNotNull("Couldn't find the ServiceResolver service", resolver);
 
         Collection<EndpointDescription> c = resolver.getEndpointsFor(
                 new QName("http://service.dynamic.samples.swordfish.eclipse.org/", "FlightService"),
                 new QName("http://samples.swordfish.eclipse.org", "consumer"));
         assertNotNull("No endpoints resolved. ", c);
         assertEquals(c.size(), 1);
     }
 
     private void setExchangeThroughNMR() {
         NMR nmr = OsgiSupport.getReference(bundleContext, NMR.class);
         assertNotNull(nmr);
 
         ExchangeImpl exchange = new ExchangeImpl(Pattern.InOut);
         exchange.setSource(((ChannelImpl) endpointService1.getChannel()).getEndpoint());
         exchange.getIn(true).setBody(new StringSource("<Hello/>"));
 
         exchange.setOperation(operation);
         exchange.setProperty(MessageExchangeImpl.INTERFACE_NAME_PROP, service2Interface);
 
         Map<String, Object> props = new HashMap<String, Object>();
         props.put(Endpoint.ENDPOINT_NAME, "JustDummyEndpointName");
         exchange.setTarget(nmr.getEndpointRegistry().lookup(props));
 
         assertEquals(endpointService2.getQueue().size(), 0);
         assertEquals(endpointService1.getQueue().size(), 0);
 
         assertTrue(nmr.createChannel().sendSync(exchange));
 
         assertEquals(endpointService2.getQueue().size(), 1);
         assertEquals(endpointService1.getQueue().size(), 0);
 
         List<Exchange> exchanges = new ArrayList<Exchange>();
         for (MessageExchange messageExchangeIt : mockInterceptor.getExchanges()) {
             Exchange innerExchange = ((MessageExchangeImpl) messageExchangeIt).getInternalExchange();
             if (innerExchange == exchange) {
                 exchanges.add(innerExchange);
             }
         }
         assertEquals(exchanges.size(), 2);
     }
 
     @Override
 	protected String getManifestLocation() {
 		return "classpath:org/eclipse/swordfish/plugins/resolver/test/MANIFEST.MF";
 	}
 }
