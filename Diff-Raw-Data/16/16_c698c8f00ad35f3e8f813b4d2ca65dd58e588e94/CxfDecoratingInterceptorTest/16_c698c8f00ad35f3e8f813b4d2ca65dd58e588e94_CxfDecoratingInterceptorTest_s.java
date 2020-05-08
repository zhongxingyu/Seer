 /*******************************************************************************
  * Copyright (c) 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.internal.core.interceptor;
 
 import static org.easymock.EasyMock.anyBoolean;
 import static org.easymock.EasyMock.createNiceControl;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.isA;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.xml.namespace.QName;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.cxf.service.model.EndpointInfo;
 import org.apache.cxf.service.model.ServiceInfo;
 import org.apache.servicemix.cxf.transport.nmr.NMRDestination;
 import org.apache.servicemix.jbi.runtime.impl.MessageExchangeImpl;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.EndpointRegistry;
 import org.apache.servicemix.nmr.api.Exchange;
 import org.apache.servicemix.nmr.api.Message;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.api.Role;
 import org.apache.servicemix.nmr.api.internal.InternalEndpoint;
 import org.apache.servicemix.nmr.api.internal.InternalExchange;
 import org.apache.servicemix.nmr.api.internal.InternalReference;
 import org.apache.servicemix.nmr.core.InternalEndpointWrapper;
 import org.easymock.EasyMock;
 import org.easymock.IArgumentMatcher;
 import org.easymock.IMocksControl;
 import org.eclipse.swordfish.internal.core.util.xml.XmlUtil;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  */
 public class CxfDecoratingInterceptorTest {
 
 	private static final String SOAP_MESSAGE =
 		"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
 			"<soap:Body>" +
 				"<ns2:sayHi xmlns:ns2=\"http://domain.util.test.core.swordfish.eclipse.org/\">" +
 					"<arg0>Swordfish</arg0>" +
 				"</ns2:sayHi>" +
 			"</soap:Body>" +
 		"</soap:Envelope>";
 
 	private static final String PAYLOAD =
 		"<ns2:sayHi xmlns:ns2=\"http://domain.util.test.core.swordfish.eclipse.org/\">" +
 			"<arg0>Swordfish</arg0>" +
 		"</ns2:sayHi>";
 
 	private CxfDecoratingInterceptor interceptor;
 	private IMocksControl ctrl = createNiceControl();
 
 	@Before
 	public void setUp() {
 		interceptor = new CxfDecoratingInterceptor();
 		ctrl.reset();
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testProcessNullExchange() {
 		interceptor.process(null);
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testProcessConsumerExchangeWithSoap() {
 		Source messageContents = new StreamSource(new StringReader(SOAP_MESSAGE));
 
 		Endpoint endpoint = createCXFEndpointStub();
 		InternalExchange exchange = createExchangeMock(endpoint);
 		Message message = ctrl.createMock(Message.class);
 
 		expect(exchange.getRole()).andReturn(Role.Consumer).atLeastOnce();
 		expect(exchange.getIn(anyBoolean())).andReturn(message).atLeastOnce();
 		expect(message.getBody(isA(Class.class))).andReturn(messageContents).atLeastOnce();
 
 		ctrl.replay();
 
 		MessageExchange me = createMessageExchange(exchange);
 		interceptor.process(me);
 		ctrl.verify();
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testProcessConsumerExchangeWithPayload() {
 		Endpoint endpoint = createCXFEndpointStub();
 		Source messageContents = new StreamSource(new StringReader(PAYLOAD));
 
 		Message message = ctrl.createMock(Message.class);
 		InternalExchange exchange = createExchangeMock(endpoint);
 
 		expect(exchange.getRole()).andReturn(Role.Consumer).atLeastOnce();
 		expect(exchange.getIn(anyBoolean())).andReturn(message).atLeastOnce();
 		expect(message.getBody(isA(Class.class))).andReturn(messageContents).atLeastOnce();
 
 		// Message must to be wrapped in a SOAP envelope
 		message.setBody(messageMatches("^.*<soap:Body.*$"));
 		expectLastCall().atLeastOnce();
 
 		ctrl.replay();
 
 		MessageExchange me = createMessageExchange(exchange);
 
 		interceptor.process(me);
 		ctrl.verify();
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testProcessProviderExchangeWithSoap() {
 		Endpoint endpoint = createCXFEndpointStub();
 		Source messageContents = new StreamSource(new StringReader(SOAP_MESSAGE));
 
 		Message message = ctrl.createMock(Message.class);
 		InternalExchange exchange = createExchangeMock(endpoint);
 
 		Map<String, Object> exchangeProps = new HashMap<String, Object>();
 		exchangeProps.put(
 			CxfDecoratingInterceptor.PROCESSED_BY_CXF_DECORATING_INTERCEPTOR, Boolean.TRUE);
 
 		expect(exchange.getRole()).andReturn(Role.Provider).atLeastOnce();
 		expect(exchange.getOut(anyBoolean())).andReturn(message).atLeastOnce();
 		expect(exchange.getProperties()).andReturn(exchangeProps).atLeastOnce();
 		expect(message.getBody(isA(Class.class))).andReturn(messageContents).atLeastOnce();
 
 		// message must be extracted from a SOAP envelope
 		message.setBody(messageMatches("^.*(?!soap:Body).*$"));
 		expectLastCall().atLeastOnce();
 
 		MessageExchange me = createMessageExchange(exchange);
 		ctrl.replay();
 
 		interceptor.process(me);
 		ctrl.verify();
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testProcessProviderExchangeWithPayload() {
 		Endpoint endpoint = createCXFEndpointStub();
 		Source messageContents = new StreamSource(new StringReader(PAYLOAD));
 
 		Message message = ctrl.createMock(Message.class);
 		InternalExchange exchange = createExchangeMock(endpoint);
 
 		Map<String, Object> exchangeProps = new HashMap<String, Object>();
 		exchangeProps.put(
 			CxfDecoratingInterceptor.PROCESSED_BY_CXF_DECORATING_INTERCEPTOR, Boolean.TRUE);
 
 		expect(exchange.getRole()).andReturn(Role.Provider).atLeastOnce();
 		expect(exchange.getOut(anyBoolean())).andReturn(message).atLeastOnce();
 		expect(exchange.getProperties()).andReturn(exchangeProps).atLeastOnce();
 		expect(message.getBody(isA(Class.class))).andReturn(messageContents).atLeastOnce();
 
 		MessageExchange me = createMessageExchange(exchange);
 		ctrl.replay();
 
 		interceptor.process(me);
 		ctrl.verify();
 	}
 
 	@Test
 	public void testProcessExchangeForNonCXFEndpoint() {
 		Endpoint endpoint = ctrl.createMock(Endpoint.class);
 		InternalExchange exchange = createExchangeMock(endpoint);
 
 		MessageExchange me = createMessageExchange(exchange);
 		ctrl.replay();
 
 		interceptor.process(me);
 		ctrl.verify();
 	}
 
 	private MessageExchange createMessageExchange(Exchange exchange) {
 		return new MessageExchangeImpl(exchange);
 	}
 
 	private Endpoint createCXFEndpointStub() {
 		ServiceInfo serviceInfo = new ServiceInfo();
 		serviceInfo.createInterface(new QName("test interface"));
 		serviceInfo.setName(new QName("test service"));
 
 		EndpointInfo endpointInfo = new EndpointInfo();
 		endpointInfo.setAddress("nmr:test-address");
 		endpointInfo.setName(new QName("test endpoint"));
 		endpointInfo.setService(serviceInfo);
 
 		return new NMRDestination(endpointInfo, null);
 	}
 
 	private InternalExchange createExchangeMock(Endpoint endpoint) {
 		NMR nmr = ctrl.createMock(NMR.class);
 		EndpointRegistry registry = ctrl.createMock(EndpointRegistry.class);
 
 		InternalExchange exchange = ctrl.createMock(InternalExchange.class);
 		InternalReference target = ctrl.createMock(InternalReference.class);
 
 		InternalEndpointWrapper wrapper =
 			new InternalEndpointWrapper(endpoint, new HashMap<String, Object>());
 		List<InternalEndpoint> endpoints = new ArrayList<InternalEndpoint>();
 		endpoints.add(wrapper);
 
 		expect(nmr.getEndpointRegistry()).andReturn(registry).atLeastOnce();
 		expect(exchange.getTarget()).andReturn(target).atLeastOnce();
 		expect(target.choose(isA(EndpointRegistry.class))).andReturn(endpoints);
 
 		interceptor.setNmr(nmr);
 
 		return exchange;
 	}
 
 	private Object messageMatches(String pattern) {
 	    EasyMock.reportMatcher(new MessageContentsMatcher(pattern));
 	    return null;
 	}
 
 	private class MessageContentsMatcher implements IArgumentMatcher {
 
 		private String pattern;
		private String ontents;
 
 	    public MessageContentsMatcher(String pattern) {
 			this.pattern = pattern;
 	    }
 
	    public boolean matches(Object ontents) {
	        if (!(ontents instanceof Source)) {
 	            return false;
 	        }
	        this.ontents = XmlUtil.toString((Source) ontents);
 
 	        Pattern p = Pattern.compile(this.pattern, Pattern.DOTALL);
	        Matcher m = p.matcher(this.ontents);
 	        return m.matches();
 	    }
 
 	    public void appendTo(StringBuffer buffer) {
 	        buffer.append("\nNo matchces for pattern: ").append(pattern);
 	        buffer.append("\nfound in: ");
 	        buffer.append("\n");
	        buffer.append(ontents);
 	        buffer.append("\n");
 	    }
 	}
 
 }
