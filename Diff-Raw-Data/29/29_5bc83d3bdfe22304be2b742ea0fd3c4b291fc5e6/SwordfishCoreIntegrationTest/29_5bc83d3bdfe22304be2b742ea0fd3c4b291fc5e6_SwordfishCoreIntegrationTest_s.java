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
 package org.eclipse.swordfish.core.test;
 
 import java.util.ArrayList;

 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.jbi.runtime.impl.EndpointImpl;
 import org.apache.servicemix.jbi.runtime.impl.MessageExchangeImpl;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.Exchange;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.api.Pattern;
 import org.apache.servicemix.nmr.core.ChannelImpl;
 import org.apache.servicemix.nmr.core.ExchangeImpl;
 import org.eclipse.swordfish.core.Interceptor;
 import org.eclipse.swordfish.core.planner.Planner;
 import org.eclipse.swordfish.core.test.util.OsgiSupport;
 import org.eclipse.swordfish.core.test.util.ServiceMixSupport;
 import org.eclipse.swordfish.core.test.util.SwordfishTestCase;
 import org.eclipse.swordfish.core.test.util.ServiceMixSupport.ExchangeProcessorImpl;
 import org.eclipse.swordfish.core.test.util.mock.MockInterceptor;
import org.eclipse.swordfish.internal.core.interceptor.EndpointResolverInterceptor;
 import org.eclipse.swordfish.internal.core.util.xml.StringSource;
 import org.osgi.framework.ServiceReference;
 
 public class SwordfishCoreIntegrationTest extends SwordfishTestCase {
 	public SwordfishCoreIntegrationTest() {
 		super();
 	}
	
     public void test1SimplePlannerInterceptorChain() throws Exception {
         ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(Planner.class.getName(), null);
         assertNotNull(serviceReferences);
         assertEquals(serviceReferences.length, 1);
         Planner planner = (Planner) bundleContext.getService(serviceReferences[0]);
        List<Interceptor> interceptors = planner.getInterceptorChain(planner.getRegisteredInterceptors(), new MessageExchangeImpl(null));
         assertTrue(interceptors.size() >= 1);
     }
 
     public void test2NonBlockingInterceptingCall() throws Exception {
         EndpointImpl endpointService1 = null;
         EndpointImpl endpointService2 = null;
         NMR nmr = OsgiSupport.getReference(bundleContext, NMR.class);
         assertNotNull(nmr);
         MockInterceptor mockInterceptor = new MockInterceptor();
         addRegistrationToCancel(bundleContext.registerService(Interceptor.class.getCanonicalName(), mockInterceptor, null));
         Thread.sleep(500);
         try {
             endpointService1 = ServiceMixSupport.createAndRegisterEndpoint(nmr, new QName("namespace", "Service1"), null);
             endpointService2 = ServiceMixSupport.createAndRegisterEndpoint(nmr, new QName("namespace", "Service2"),
                     new ExchangeProcessorImpl(new QName("namespace", "Service2").toString()));
             ExchangeImpl exchange = new ExchangeImpl(Pattern.InOut);
             exchange.setSource(((ChannelImpl) endpointService1.getChannel()).getEndpoint());
             exchange.getIn(true).setBody(new StringSource("<Hello/>"));
             Map<String, String> props = new HashMap<String, String>();
             props.put(Endpoint.SERVICE_NAME, endpointService2.getServiceName().toString());
             exchange.setTarget(ServiceMixSupport.lookup(nmr, props));
             assertEquals(endpointService2.getQueue().size(), 0);
             assertEquals(endpointService1.getQueue().size(), 0);
             nmr.createChannel().send(exchange);
             Thread.sleep(500);
             assertEquals(endpointService2.getQueue().size(), 1);
             assertEquals(endpointService1.getQueue().size(), 1);
             List<Exchange> exchanges = new ArrayList<Exchange>();
             for (MessageExchange messageExchangeIt : mockInterceptor.getExchanges()) {
                 Exchange innerExchange = ((MessageExchangeImpl) messageExchangeIt).getInternalExchange();
                 if (innerExchange == exchange) {
                     exchanges.add(innerExchange);
                 }
             }
             assertEquals(exchanges.size(), 2);
         } finally {
             nmr.getEndpointRegistry().unregister(endpointService1, null);
             nmr.getEndpointRegistry().unregister(endpointService2, null);
         }
     }
 
     public void test3BlockingInterceptingCall() throws Exception {
         System.out.println("!!!Test3");
         EndpointImpl endpointService1 = null;
         EndpointImpl endpointService2 = null;
         NMR nmr = OsgiSupport.getReference(bundleContext, NMR.class);
         assertNotNull(nmr);
         MockInterceptor mockInterceptor = new MockInterceptor();
         addRegistrationToCancel(bundleContext.registerService(Interceptor.class.getCanonicalName(), mockInterceptor, null));
         Thread.sleep(500);
         try {
             endpointService1 = ServiceMixSupport.createAndRegisterEndpoint(nmr, new QName("namespace", "Service1"), null);
             endpointService2 = ServiceMixSupport.createAndRegisterEndpoint(nmr, new QName("namespace", "Service2"),
                     new ExchangeProcessorImpl(new QName("namespace", "Service2").toString()));
             ExchangeImpl exchange = new ExchangeImpl(Pattern.InOut);
             exchange.setSource(((ChannelImpl) endpointService1.getChannel()).getEndpoint());
             exchange.getIn(true).setBody(new StringSource("<Hello/>"));
             Map<String, String> props = new HashMap<String, String>();
             props.put(Endpoint.SERVICE_NAME, endpointService2.getServiceName().toString());
             exchange.setTarget(ServiceMixSupport.lookup(nmr, props));
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
         } finally {
             nmr.getEndpointRegistry().unregister(endpointService1, null);
             nmr.getEndpointRegistry().unregister(endpointService2, null);
         }
     }
 
 }
