 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004-2007 Megatome Technologies.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution, if
  *    any, must include the following acknowlegement:
  *       "This product includes software developed by
  *        Megatome Technologies."
  *    Alternately, this acknowlegement may appear in the software itself,
  *    if and wherever such third-party acknowlegements normally appear.
  *
  * 4. The names "The Frame2 Project", and "Frame2", 
  *    must not be used to endorse or promote products derived
  *    from this software without prior written permission. For written
  *    permission, please contact iamthechad@sourceforge.net.
  *
  * 5. Products derived from this software may not be called "Frame2"
  *    nor may "Frame2" appear in their names without prior written
  *    permission of Megatome Technologies.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL MEGATOME TECHNOLOGIES OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  */
 package org.megatome.frame2.front;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.ServletContext;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.validation.Schema;
 
 import org.megatome.frame2.Frame2Exception;
 import org.megatome.frame2.errors.Error;
 import org.megatome.frame2.errors.Errors;
 import org.megatome.frame2.errors.impl.ErrorsFactory;
 import org.megatome.frame2.event.Event;
 import org.megatome.frame2.event.Responder;
 import org.megatome.frame2.event.xml.PassthruEvent;
 import org.megatome.frame2.front.config.ResolveType;
 import org.megatome.frame2.front.config.ViewType;
 import org.megatome.frame2.jaxb.JaxbEventBase;
 import org.megatome.frame2.log.Logger;
 import org.megatome.frame2.log.LoggerFactory;
 import org.megatome.frame2.util.MessageFormatter;
 import org.megatome.frame2.util.ResourceLocator;
 import org.megatome.frame2.util.dom.DOMStreamConverter;
 import org.megatome.frame2.util.soap.SOAPException;
 import org.megatome.frame2.util.soap.SOAPFault;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * A request models the execution of a SOAP request through the Event and the
  * EventHandlers. It is a primary delegate of RequestProcessor, and brings
  * together the data and logic necessary for processing the request.
  */
 public class SoapRequestProcessor extends RequestProcessorBase {
 	private static final Logger LOGGER = LoggerFactory.instance(SoapRequestProcessor.class.getName());
 	private List<Element> elements;
 	private String eventPkg;
 
 	/**
 	 * Create a new instance of SoapRequestProcessor
 	 * 
 	 * @param config
 	 *            Configuration from file
 	 * @param elements
 	 * @param eventPkg
 	 */
 	public SoapRequestProcessor(Configuration config, Element[] elements,
 			String eventPkg) {
 		super(config);
 		this.elements = Collections.unmodifiableList(Arrays.asList(elements));
 		this.errors = ErrorsFactory.newInstance();
 		this.context = new ContextImpl();
 		this.eventPkg = eventPkg;
 	}
 
 	/**
 	 * Process the request.
 	 * 
 	 * @return Results of processing request
 	 * @throws Exception
 	 * @see org.megatome.frame2.front.RequestProcessor#processRequest()
 	 */
 	public Object processRequest() throws Exception {
 		LOGGER.debug("In SoapRequestProcessor processRequest()"); //$NON-NLS-1$
 		List<Element> resultList = new ArrayList<Element>();
 
 		// get event objects from request
 		List<SoapEventMap> events = getEvents();
 
 		for (int i = 0; i < events.size(); i++) {
 			SoapEventMap event = events.get(i);
 			String eventName = event.getEventName();
 			boolean validate = getConfig().validateFor(eventName);
 			int listIndex = -1;
 
 			try {
 				// iterate over this event's list of events
 				for (Event childEvent : event.getEvents()) {
 					listIndex++;
 
 					childEvent.setEventName(eventName);
 
 					boolean valid = true;
 
 					if (validate) {
 						valid = validateEvent(childEvent);
 					}
 
 					if (valid) {
 						// TODO Is it possible to get an event type forward here? 
 						ForwardProxy fwd = callHandlers(eventName, childEvent,
 								ViewType.XML);
 
 						if (fwd.isResourceType()) {
 							Element marshalledResult = marshallResponse(this.context
 									.getRequestAttribute(fwd.getPath()));
 
 							event.setResponse(listIndex, marshalledResult);
 						} else if (fwd.isResponderType()) {
 							// create responder
 							String type = fwd.getPath();
 							Responder responder = (Responder) Class.forName(
 									type).newInstance();
 							Object response = responder
 									.respond(getContextWrapper());
 
 							if (response instanceof org.w3c.dom.Element) {
 								event
 										.setResponse(listIndex,
 												(Element) response);
 							} else { // marshall response
 
 								Element marshalledResult = marshallResponse(response);
 
 								event.setResponse(listIndex, marshalledResult);
 							}
 						}
 					} else {
 						event.setResponse(listIndex, createFault(this.errors));
 					}
 				}
 			} catch (TranslationException e) {
 				event.setResponse(listIndex, createFault(e));
 			} catch (Frame2Exception e) {
 				event.setResponse(listIndex, createFault(e));
 			}
 		}
 
 		// build element[]
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
 		factory.setNamespaceAware(true);
 
 		DocumentBuilder builder = factory.newDocumentBuilder();
 
 		for (int i = 0; i < events.size(); i++) {
 			SoapEventMap event = events.get(i);
 
 			if ((event.getResolve() == ResolveType.PARENT)
 					|| (event.getResolve() == ResolveType.PASSTHRU)) {
 				// no extra processing, 1-1
 				resultList.add(event.getResponses().get(0));
 			} else if (event.getResolve().equals(ResolveType.CHILDREN)) {
 				// NIT: wrap in event name element for now
 				Document doc = builder.newDocument();
 				Element parent = doc.createElement(event.getEventName());
 
 				doc.appendChild(parent);
 
 				for (Element elem : event.getResponses()) {
 					parent.appendChild(doc.importNode(elem, true));
 				}
 
 				resultList.add(parent);
 			}
 		}
 		return resultList.toArray(new Element[0]);
 	}
 
 	/**
 	 * SoapRequestProcessor only generates a log message for this method.
 	 * 
 	 * @see org.megatome.frame2.front.RequestProcessor#preProcess()
 	 */
 	public void preProcess() {
 		LOGGER.debug("In SoapRequestProcessor preProcess()"); //$NON-NLS-1$
 	}
 
 	/**
 	 * SoapRequestProcessor only generates a log message for this method.
 	 * 
 	 * @see org.megatome.frame2.front.RequestProcessor#preProcess()
 	 */
 	public void postProcess() {
 		LOGGER.debug("In SoapRequestProcessor postProcess()"); //$NON-NLS-1$
 	}
 
 	/*
 	 * private Element marshallResultAsResourceKey(String key) throws
 	 * TranslationException { Element marshalledResult =
 	 * marshallResponse(context.getRequestAttribute(key)); return
 	 * marshalledResult; }
 	 */
 	private Element createFault(Throwable e) {
 		SOAPFault fault = new SOAPFault();
 
 		fault.setDetailMessage(e.getMessage(), true);
 
 		Element elem = null;
 
 		try {
 			elem = fault.getElement();
 		} catch (SOAPException se) {
 			// NIT maybe not catch, shouldn't happen.
 		}
 
 		return elem;
 	}
 
 	private Element createFault(Errors errs) throws SOAPException {
 		SOAPFault fault = new SOAPFault();
 
 		StringBuffer buffer = new StringBuffer();
 
 		ResourceBundle bundle = ResourceLocator.getBundle();
 
 		for (Error error : errs.get()) {
 			String msg = bundle.getString(error.getKey());
 
 			if (msg == null) {
 				buffer.append("Could not find resource for key: " + error.getKey()); //$NON-NLS-1$
 			} else {
 				buffer.append(MessageFormatter.format(msg, error.getValues()));
 			}
 			buffer.append("\n"); //$NON-NLS-1$
 		}
 
 		fault.setDetailMessage(buffer.toString(), true);
 
 		return fault.getElement();
 	}
 
 	/**
 	 * Release resources held by the processor.
 	 * 
 	 * @see org.megatome.frame2.front.RequestProcessor#release()
 	 */
 	@Override
 	public void release() {
 		super.release();
 		this.elements = null;
 		this.eventPkg = null;
 	}
 
 	/**
 	 * Method getEvents.
 	 */
 	@SuppressWarnings("deprecation")
 	List<SoapEventMap> getEvents() throws TranslationException {
 		// list of SoapEventMap objs
 		List<SoapEventMap> events = new ArrayList<SoapEventMap>();
 
 		try {
 			JAXBContext jcontext = JAXBContext.newInstance(this.eventPkg);
 
 			Unmarshaller unmarshaller = jcontext.createUnmarshaller();
 
 			if (this.elements != null) {
 				for (Element element : this.elements) {
 					if (element != null) {
 						SoapEventMap event = new SoapEventMap();
 						List<Event> eventList = new ArrayList<Event>();
 
 						String eventName = element.getTagName();
 
 						event.setEventName(eventName);
 
 						EventProxy eventProxy = getConfig().getEventProxy(
 								eventName);
 						Schema s = getConfig().getValidatingSchema(eventName);
 						
 						if (eventProxy == null) {
 							throw new TranslationException(
 									"Unable to map event: " + eventName //$NON-NLS-1$
 											+ " to Config file"); //$NON-NLS-1$
 						}
 
 						if (eventProxy.isParent()) {
 							// put eventNames in arraylist for iteration
 							// put events in list mapped by eventName
 							JaxbEventBase evt = unmarshall(unmarshaller, DOMStreamConverter
 									.toInputStream(element));
 							evt.setValidatingSchema(s);
 							eventList.add(evt);
 							/*
 							 * eventList.add((Event)unmarshaller
 							 * .unmarshal(DOMStreamConverter
 							 * .toInputStream(this.elements[i])));
 							 */
 							event.setResolve(ResolveType.PARENT);
 							event.setEvents(eventList);
 							events.add(event);
 						} else if (eventProxy.isChildren()) {
 							NodeList nodeList = element.getChildNodes();
 
 							for (int j = 0; j < nodeList.getLength(); j++) {
 								if (nodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
 									JaxbEventBase evt = unmarshall(unmarshaller,
 											DOMStreamConverter
 													.toInputStream(nodeList
 															.item(j)));
 									evt.setValidatingSchema(s);
 									eventList.add(evt);
 									/*
 									 * eventList.add((Event)unmarshaller
 									 * .unmarshal(DOMStreamConverter
 									 * .toInputStream(nodeList .item(j))));
 									 */
 								}
 							}
 
 							event.setResolve(ResolveType.CHILDREN);
 							event.setEvents(eventList);
 							events.add(event);
 						} else if (eventProxy.isPassThru()) {
 							PassthruEvent psevent = (PassthruEvent) eventProxy
 									.getEvent();
 
 							psevent.setPassthruData(element);
 							eventList.add(psevent);
 							event.setResolve(ResolveType.PASSTHRU);
 							event.setEvents(eventList);
 							events.add(event);
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new TranslationException("Unable to unmarshall element", e); //$NON-NLS-1$
 		}
 
 		return events;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends JaxbEventBase> T unmarshall(Unmarshaller unm, 
 			InputStream is) throws JAXBException {
 		Object obj = unm.unmarshal(is);
 		if (obj instanceof JAXBElement) {
 			JAXBElement<T> element = (JAXBElement<T>) obj;
 			return element.getValue();
 		}
 		
 		return (T)obj;
 	}
 
 	/**
 	 * Method getContext.
 	 */
 	@Override
 	protected ContextWrapper getContextWrapper() {
 		return this.context;
 	}
 
 	/**
 	 * Method marshallResponse.
 	 * 
 	 * @param poi
 	 * @return Element
 	 */
 	Element marshallResponse(Object obj) throws TranslationException {
 		Element result = null;
 
 		if ((obj != null) && obj instanceof Element) {
 			result = (Element) obj;
 		} else if (obj != null) {
 			try {
 				JAXBContext jcontext = JAXBContext.newInstance(this.eventPkg);
 				Marshaller marshaller = jcontext.createMarshaller();
 
 				Document doc = getTargetDocument();
 				
 				if (obj instanceof JaxbEventBase) {
 					JaxbEventBase jeb = (JaxbEventBase)obj;
 					marshaller.marshal(jeb.getMarshallableType(), doc);
 				} else {
 					marshaller.marshal(obj, doc);
 				}
 				result = doc.getDocumentElement();
 			} catch (JAXBException e) {
 				throw new TranslationException("Unable to marshall response", e); //$NON-NLS-1$
 			} catch (Exception e) {
 				throw new TranslationException("Unable to find marshallable object", e); //$NON-NLS-1$
 			}
 		}
 
 		return result;
 	}
 
 	private Document getTargetDocument() throws TranslationException {
 		Document result = null;
 
 		try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
 			dbf.setNamespaceAware(true);
 
 			DocumentBuilder db = dbf.newDocumentBuilder();
 
 			result = db.newDocument();
 		} catch (Exception e) {
 			throw new TranslationException("Unable to create target document", //$NON-NLS-1$
 					e);
 		}
 
 		return result;
 	}
 
 	@Override
 	protected String configResourceType() {
 		return Configuration.XML_TOKEN;
 	}
 
 	/**
 	 * Validate the indicated event. Validation is performed against the schema
 	 * and against the Commons Validator, if it has been configured.
 	 * 
 	 * @param event
 	 *            The event to validate.
 	 * @return True if the event passed validation.
 	 */
 	public boolean validateEvent(Event event) {
 		return ((event != null) ? event.validate(this.errors) : true);
 	}
 
 	@Override
 	protected boolean isUserAuthorizedForEvent(@SuppressWarnings("unused")
 	String event) {
 		return true;
 	}
 
 	class ContextImpl implements ContextWrapper {
 		private Map<String, String> initParms;
 
 		private Map<String, Object> requestAttributes;
 
 		private Map<String, Object> sessionAttributes;
 
 		private Set<String> redirectAttrs = new TreeSet<String>();
 		private Map<String, Object> responseAttrs = new HashMap<String,Object>();
 
 		public ServletContext getServletContext() {
 			return null;
 		}
 
 		public String getInitParameter(String key) {
 			return (String) getIfNotNull(key, this.initParms);
 		}
 
 		public Object getRequestAttribute(String key) {
 			return getIfNotNull(key, this.requestAttributes);
 		}
 
 		public String[] getRedirectAttributes() {
 			return this.redirectAttrs.toArray(new String[]{});
 		}
 
 		public Errors getRequestErrors() {
 			return SoapRequestProcessor.this.errors;
 		}
 
 		public Object getSessionAttribute(String key) {
 			return getIfNotNull(key, this.sessionAttributes);
 		}
 
 		public void removeRequestAttribute(String key) {
 			removeIfNotNull(key, this.requestAttributes);
 			this.redirectAttrs.remove(key);
 		}
 
 		public void removeSessionAttribute(String key) {
 			removeIfNotNull(key, this.sessionAttributes);
 		}
 
 		@Override
 		public void setRequestAttribute(String key, Object value) {
 			if (this.requestAttributes == null) {
 				this.requestAttributes = new HashMap<String, Object>();
 			}
 
 			this.requestAttributes.put(key, value);
 		}
 
 		@Override
 		public void setRequestAttribute(String key, Object value,
 				boolean redirectAttr) {
 			if (redirectAttr) {
 				this.redirectAttrs.add(key);
 			} else {
 				this.redirectAttrs.remove(key);
 			}
 
 			setRequestAttribute(key, value);
 		}
 
 		@Override
 		public void setSessionAttribute(String key, Object value) {
 			if (this.sessionAttributes == null) {
 				this.sessionAttributes = new HashMap<String, Object>();
 			}
 
 			this.sessionAttributes.put(key, value);
 		}
 
 		@Override
 		public void setInitParameters(Map<String, String> initParms) {
 			this.initParms = initParms;
 		}
 
 		private Object getIfNotNull(String key,
 				Map<String, ? extends Object> map) {
 			return ((map != null) ? map.get(key) : null);
 		}
 
 		private void removeIfNotNull(String key, Map<String, Object> map) {
 			if (map != null) {
 				map.remove(key);
 			}
 		}
 
 		@Override
 		public void addResponseURIAttribute(String key, Object value) {
 			this.responseAttrs.put(key, value);
 		}
 
 		@Override
 		public Map<String, Object> getResponseURIAttributes() {
 			return Collections.unmodifiableMap(this.responseAttrs);
 		}
 
 		@Override
 		public boolean hasResponseURIAttributes() {
 			return (!this.responseAttrs.isEmpty());
 		}
 
 		@Override
 		public void clearResponseURIAttributes() {
 			this.responseAttrs.clear();
 		}
 	}
 }
