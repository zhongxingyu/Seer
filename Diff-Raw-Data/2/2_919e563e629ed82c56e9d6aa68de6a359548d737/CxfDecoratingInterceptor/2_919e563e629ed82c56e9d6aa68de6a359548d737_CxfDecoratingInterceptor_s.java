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
 package org.eclipse.swordfish.internal.core.interceptor;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.runtime.impl.AbstractComponentContext;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.Exchange;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.api.Reference;
 import org.apache.servicemix.nmr.api.Role;
 import org.apache.servicemix.nmr.api.Status;
 import org.apache.servicemix.nmr.api.internal.InternalEndpoint;
 import org.apache.servicemix.nmr.api.internal.InternalExchange;
 import org.apache.servicemix.nmr.core.DynamicReference;
 import org.apache.servicemix.nmr.core.InternalEndpointWrapper;
 import org.apache.servicemix.nmr.core.util.Filter;
 import org.apache.servicemix.soap.util.DomUtil;
 import org.eclipse.swordfish.core.Interceptor;
 import org.eclipse.swordfish.core.SwordfishException;
 import org.eclipse.swordfish.internal.core.util.ReflectionUtil;
 import org.eclipse.swordfish.internal.core.util.smx.ServiceMixSupport;
 import org.eclipse.swordfish.internal.core.util.xml.StringSource;
 import org.eclipse.swordfish.internal.core.util.xml.XmlUtil;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class CxfDecoratingInterceptor implements Interceptor {
 
 	private static final Log LOG = LogFactory.getLog(LoggingInterceptor.class);
 	public final static String PROCESSED_BY_CXF_DECORATING_INTERCEPTOR = "PROCESSED_BY_CXF_DECORATING_INTERCEPTOR";
 	private final static String SOAP_MESSAGE_PREFIX = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
 			+ "<soap:Body>";
 	private final static String SOAP_MESSAGE_SUFFIX = "</soap:Body></soap:Envelope>";
 
 	private static Map<String, Object> properties = new HashMap<String, Object>();
 
 	private NMR nmr;
 
 	{
 		properties.put(Interceptor.TYPE_PROPERTY, new QName("http://interceptor.core.internal.swordfish.eclipse.org/", "CxfDecoratingInterceptor"));
 	}
 
	public synchronized NMR getNmr() {
 		return nmr;
 	}
 
 	public void setNmr(NMR nmr) {
 		this.nmr = nmr;
 	}
 
 	private boolean isCxfEndpoint(InternalEndpoint endpoint) {
 		if (endpoint == null) {
 			return false;
 		}
 		try {
 			InternalEndpointWrapper endpointWrapper = (InternalEndpointWrapper) endpoint;
 
 			Field endpointField = InternalEndpointWrapper.class
 					.getDeclaredField("endpoint");
 			endpointField.setAccessible(true);
 			Endpoint innerEndpoint = (Endpoint) endpointField
 					.get(endpointWrapper);
 			if (innerEndpoint != null
 					&& innerEndpoint.getClass().getCanonicalName() != null
 					&& innerEndpoint.getClass().getCanonicalName().contains(
 							"cxf")) {
 				return true;
 			}
 		} catch (Exception ex) {
 			LOG.warn(ex.getMessage(), ex);
 			return false;
 		}
 		return false;
 	}
 
 	private Map<String, ?> getTargetProperties(Exchange messageExchange) {
 		Reference reference = messageExchange.getTarget();
 		if (reference == null || !(reference instanceof DynamicReference)) {
 			return null;
 		}
 		Filter filter = (Filter) ReflectionUtil.getDeclaredField(reference,
 				DynamicReference.class, "filter");
 		if (filter == null) {
 			return null;
 		}
 		List<Object> instanceVariables = ReflectionUtil
 				.getAnonymousClassInstanceValues(filter);
 		if (instanceVariables.size() == 0) {
 			return null;
 		}
 		if (instanceVariables.get(0) instanceof Map) {
 			return (Map<String, ?>) instanceVariables.get(0);
 		}
 		return null;
 
 	}
 
 	public void process(MessageExchange exchange) throws SwordfishException {
 		InternalExchange messageExchange = (InternalExchange) ServiceMixSupport
 				.toNMRExchange(exchange);
 		if (messageExchange.getTarget() == null) {
 			return;
 		}
 		InternalEndpoint endpoint = extractTargetEndpoint(messageExchange);
 		if (!isCxfEndpoint(endpoint)) {
 			return;
 		}
 		try {
 			if (messageExchange.getRole() == Role.Consumer) {
 				org.apache.servicemix.nmr.api.Message inMessage = messageExchange.getIn(false);
 				wrapEnvelope(inMessage, messageExchange);
 			} else if (messageExchange.getRole() == Role.Provider
 					&& messageExchange.getOut(false) != null) {
 				org.apache.servicemix.nmr.api.Message outMessage = messageExchange.getOut(false);
 				unwrapEnvelope(outMessage, messageExchange);
 			}
 			if (messageExchange.getStatus() == Status.Active) {
 				if (messageExchange.getFault(false) != null
 						&& messageExchange.getFault(false).getBody() == null) {
 					messageExchange.setFault(null);
 				}
 				if (messageExchange.getOut(false) != null
 						&& messageExchange.getOut(false).getBody() == null) {
 					messageExchange.setOut(null);
 				}
 			}
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	private InternalEndpoint extractTargetEndpoint(InternalExchange messageExchange) {
 		InternalEndpoint endpoint = ServiceMixSupport.getEndpoint(nmr,
 				messageExchange.getTarget());
 		if (endpoint == null) {
 			Map<String, ?> targetProps = getTargetProperties(messageExchange);
 			if (targetProps != null
 					&& targetProps
 							.containsKey(AbstractComponentContext.INTERNAL_ENDPOINT)) {
 				targetProps.remove(AbstractComponentContext.INTERNAL_ENDPOINT);
 				try {
 					Field field = DynamicReference.class
 							.getDeclaredField("matches");
 					field.setAccessible(true);
 					field.set(messageExchange.getTarget(), null);
 					endpoint = ServiceMixSupport.getEndpoint(nmr,
 							messageExchange.getTarget());
 				} catch (Exception ex) {
 					LOG.error(ex.getMessage(), ex);
 				}
 			}
 		}
 		return endpoint;
 	}
 
 	private void wrapEnvelope(org.apache.servicemix.nmr.api.Message inMessage, Exchange messageExchange) {
 		String message = XmlUtil.toString(inMessage.getBody(Source.class));
 		if (!message.contains(":Body")) {
 			int index = message.indexOf(">") + 1;
 			String xmlPrefix = message.substring(0, index);
 			String cutMessage = message.substring(index);
 			String soapMessage = xmlPrefix + SOAP_MESSAGE_PREFIX
 					+ cutMessage + SOAP_MESSAGE_SUFFIX;
 			inMessage.setBody(new StringSource(soapMessage));
 			messageExchange.setProperty(PROCESSED_BY_CXF_DECORATING_INTERCEPTOR, true);
 
 		} else {
 			inMessage.setBody(new StringSource(message));
 		}
 	}
 
 	private void unwrapEnvelope(org.apache.servicemix.nmr.api.Message outMessage, Exchange messageExchange)
 			throws ParserConfigurationException, IOException, SAXException,
 			TransformerException {
 		if (!messageExchange.getProperties().containsKey(PROCESSED_BY_CXF_DECORATING_INTERCEPTOR) || !((Boolean)messageExchange.getProperties().get(PROCESSED_BY_CXF_DECORATING_INTERCEPTOR))) {
 			return;
 		}
 		String message = XmlUtil.toString(outMessage.getBody(Source.class));
 		if (message.contains(":Body")) {
 			int index = message.indexOf(">") + 1;
 			String xmlPrefix = message.substring(0, index);
 			if (envelopeIsEmpty(message)) {
 				Document document = DomUtil.createDocument();
 				QName operation = messageExchange.getOperation();
 				DomUtil.createElement(document, new QName(operation.getNamespaceURI(), operation.getLocalPart() + "Response", operation.getPrefix()));
 				outMessage.setBody(new DOMSource(document));
 				return;
 			}
 			String cutMessage = message.substring(index);
 			int startIndex = cutMessage.indexOf(":Body");
 			startIndex = cutMessage.indexOf(">", startIndex) + 1;
 			int endIndex = cutMessage.indexOf(":Body", startIndex);// end	of body
 			endIndex = cutMessage.substring(0, endIndex).lastIndexOf("</");
 			String bodyMessage = cutMessage.substring(startIndex, endIndex);
 			bodyMessage = xmlPrefix + bodyMessage;
 			outMessage.setBody(new SourceTransformer().toDOMSource(new StringSource(bodyMessage)));
 		}
 	}
 
 	private boolean envelopeIsEmpty(String message) {
 		return message.contains(":Body/>");
 	}
 
 	public Map<String, ?> getProperties() {
 		return properties;
 	}
 }
