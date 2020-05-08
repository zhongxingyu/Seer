 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004 Megatome Technologies.  All rights
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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.ServletContext;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.megatome.frame2.Frame2Exception;
 import org.megatome.frame2.errors.Error;
 import org.megatome.frame2.errors.Errors;
 import org.megatome.frame2.event.Event;
 import org.megatome.frame2.event.Responder;
 import org.megatome.frame2.event.xml.PassthruEvent;
 import org.megatome.frame2.front.config.ResolveType;
 import org.megatome.frame2.front.config.ViewType;
 import org.megatome.frame2.log.Logger;
 import org.megatome.frame2.log.LoggerFactory;
 import org.megatome.frame2.util.MessageFormatter;
 import org.megatome.frame2.util.ResourceLocator;
 import org.megatome.frame2.util.dom.DOMStreamConverter;
 import org.megatome.frame2.util.soap.SOAPException;
 import org.megatome.frame2.util.soap.SOAPFault;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 
 /**
  * A request models the execution of a SOAP request through the Event and the EventHandlers.  It
  * is a primary delegate of RequestProcessor, and brings together the data and logic necessary for
  * processing the request.
  */
 public class SoapRequestProcessor extends RequestProcessorBase {
    private Element[] _elements;
    private String _eventPkg;
    
    private Logger getLogger() {
       return LoggerFactory.instance(SoapRequestProcessor.class.getName());
    }      
 
    /**
     * Create a new instance of SoapRequestProcessor
     * @param config Configuration from file
     * @param elements
     * @param eventPkg
     */
    public SoapRequestProcessor(Configuration config, Element[] elements, String eventPkg) {
       super(config);
       _elements = elements;
      _errors = Errors.newInstance();
       _context = new ContextImpl();
       _eventPkg = eventPkg;
    }
 
    /**
     * Process the request.
     * @return Results of processing request
     * @throws Exception
     * @see org.megatome.frame2.front.RequestProcessor#processRequest()
     */
    public Object processRequest() throws Exception {
       getLogger().debug("In SoapRequestProcessor processRequest()");
       final Element[] elements = new Element[0];
       List resultList = new ArrayList();
 
       // get event objects from request
       List events = getEvents();
 
       for (int i = 0; i < events.size(); i++) {
          SoapEventMap event = (SoapEventMap) events.get(i);
          String eventName = event.getEventName();
          boolean validate = getConfig().validateFor(eventName);
          int listIndex = -1;
 
          try {
             // iterate over this event's list of events
             Iterator eventList = event.getEventsIterator();
 
             while (eventList.hasNext()) {
                listIndex++;
 
                Event childEvent = (Event) eventList.next();
                childEvent.setName(eventName);
 
                boolean valid = true;
 
                if (validate) {
                   valid = validateEvent(childEvent);
                }
 
                if (valid) {
                   ForwardProxy fwd = callHandlers(eventName, childEvent, ViewType.XML);
 
                   if (fwd.isResourceType()) {
                      Element marshalledResult = marshallResponse(_context.getRequestAttribute(
                               fwd.getPath()));
 
                      event.setResponse(listIndex, marshalledResult);
                   } else if (fwd.isResponderType()) {
                      // create responder
                      String type = fwd.getPath();
 							Responder responder = (Responder) Class.forName(type).newInstance();
                      Object response = responder.respond(getContextWrapper());
 
                      if (response instanceof org.w3c.dom.Element) {
                         event.setResponse(listIndex, response);
                      } else { // marshall response
 
                         Element marshalledResult = marshallResponse(response);
 
                         event.setResponse(listIndex, marshalledResult);
                      }
                   }
                } else {
                   event.setResponse(listIndex, createFault(_errors));
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
          SoapEventMap event = (SoapEventMap) events.get(i);
          Iterator iter = event.getResponsesIterator();
 
          if ((event.getResolve() == ResolveType.PARENT) ||
                (event.getResolve() == ResolveType.PASSTHRU)) {
             //no extra processing, 1-1            
             resultList.add((Element) iter.next());
          } else if (event.getResolve().equals(ResolveType.CHILDREN)) {
             // NIT: wrap in event name element for now
             Document doc = builder.newDocument();
             Element parent = doc.createElement(event.getEventName());
 
             doc.appendChild(parent);
 
             while (iter.hasNext()) {
                Element elem = (Element) iter.next();
 
                parent.appendChild(doc.importNode(elem, true));
             }
 
             resultList.add(parent);
          }
       }
       return resultList.toArray(elements);
    }
    
    /**
     * SoapRequestProcessor only generates a log message for this method.
     * @see org.megatome.frame2.front.RequestProcessor#preProcess()
     */
    public void preProcess() {
       getLogger().debug("In SoapRequestProcessor preProcess()");
    }
 
 	/**
     * SoapRequestProcessor only generates a log message for this method.
     * @see org.megatome.frame2.front.RequestProcessor#preProcess()
     */
    public void postProcess() {
       getLogger().debug("In SoapRequestProcessor postProcess()");
    }
 /*
    private Element marshallResultAsResourceKey(String key)
       throws TranslationException {
       Element marshalledResult = marshallResponse(_context.getRequestAttribute(key));
 
       return marshalledResult;
    }
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
 
    private Element createFault(Errors errors) throws SOAPException {
       SOAPFault fault = new SOAPFault();
 
       StringBuffer buffer = new StringBuffer();
 
       ResourceBundle bundle = ResourceLocator.getBundle();
 
       Error[] error = errors.get();
 
       for (int i = 0; i < error.length; i++) {
          String msg = bundle.getString(error[i].getKey());
 
          buffer.append(MessageFormatter.format(msg, error[i].getValues()));
          buffer.append("\n");
       }
 
       fault.setDetailMessage(buffer.toString(), true);
 
       return fault.getElement();
    }
 
    /**
     * Release resources held by the processor.
     * @see org.megatome.frame2.front.RequestProcessor#release()
     */
    public void release() {
       super.release();
       _elements = null;
       _eventPkg = null;
    }
 
    /**
     * Method getEvents.
     */
    List getEvents() throws TranslationException {
       // list of SoapEventMap objs
       List events = new ArrayList();
 
       try {
          JAXBContext jcontext = JAXBContext.newInstance(_eventPkg);
 
          Unmarshaller unmarshaller = jcontext.createUnmarshaller();
 
          if (_elements != null) {
             for (int i = 0; i < _elements.length; i++) {
                if (_elements[i] != null) {
                   SoapEventMap event = new SoapEventMap();
                   List eventList = new ArrayList();
 
                   String eventName = _elements[i].getTagName();
 
                   event.setEventName(eventName);
 
                   EventProxy eventProxy = (EventProxy) getConfig().getEventProxy(eventName);
 
                   if (eventProxy == null) {
                      throw new TranslationException("Unable to map event: " + eventName +
                         " to Config file");
                   }
 
                   if (eventProxy.isParent()) {
                      // put eventNames in arraylist for iteration
                      // put events in list mapped by eventName      
                      eventList.add(unmarshaller.unmarshal(DOMStreamConverter.toInputStream(
                               _elements[i])));
                      event.setResolve(ResolveType.PARENT);
                      event.setEvents(eventList);
                      events.add(event);
                   } else if (eventProxy.isChildren()) {
                      NodeList nodeList = _elements[i].getChildNodes();
 
                      for (int j = 0; j < nodeList.getLength(); j++) {
                         if (nodeList.item(j).getNodeType() == Element.ELEMENT_NODE) {
                            eventList.add(unmarshaller.unmarshal(DOMStreamConverter.toInputStream(
                                     nodeList.item(j))));
                         }
                      }
 
                      event.setResolve(ResolveType.CHILDREN);
                      event.setEvents(eventList);
                      events.add(event);
                   } else if (eventProxy.isPassThru()) {
                      PassthruEvent psevent = (PassthruEvent) eventProxy.getEvent();
 
                      psevent.setPassthruData(_elements[i]);
                      eventList.add(psevent);
                      event.setResolve(ResolveType.PASSTHRU);
                      event.setEvents(eventList);
                      events.add(event);
                   }
                }
             }
          }
       } catch (Exception e) {
          throw new TranslationException("Unable to unmarshall element", e);
       }
 
       return events;
    }
 
    /**
     * Method getContext.
     */
    ContextWrapper getContextWrapper() {
       return _context;
    }
 
    /**
     * Method marshallResponse.
     *
     * @param poi
     *
     * @return Element
     */
    Element marshallResponse(Object obj) throws TranslationException {
       Element result = null;
 
       if ((obj != null) && obj instanceof Element) {
          result = (Element) obj;
       } else if (obj != null) {
          try {
             JAXBContext jcontext = JAXBContext.newInstance(_eventPkg);
             Marshaller marshaller = jcontext.createMarshaller();
 
             Document doc = getTargetDocument();
 
             marshaller.marshal(obj, doc);
             result = doc.getDocumentElement();
          } catch (JAXBException e) {
             throw new TranslationException("Unable to marshall response", e);
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
          throw new TranslationException("Unable to create target document", e);
       }
 
       return result;
    }
 
    String configResourceType() {
       return Configuration.XML_TOKEN;
    }
 
    /**
     * Validate the indicated event. Validation is performed against the
     * schema and against the Commons Validator, if it has been configured.
     * @param event The event to validate.
     * @return True if the event passed validation.
     */
    public boolean validateEvent(Event event) {
       return ((event != null) ? event.validate(_errors) : true);
    }
 
    boolean isUserAuthorizedForEvent(String event) {
       return true;
    }
 
    class ContextImpl implements ContextWrapper {
       private Map _initParms;
       private Map _requestAttributes;
       private Map _sessionAttributes;
       private Set _redirectAttrs = new TreeSet();
 
       public ServletContext getServletContext() {
          return null;
       }
 
       public String getInitParameter(String key) {
          return (String) getIfNotNull(key, _initParms);
       }
 
       public Object getRequestAttribute(String key) {
          return getIfNotNull(key, _requestAttributes);
       }
 
       public String[] getRedirectAttributes() {
          return (String[]) _redirectAttrs.toArray();
       }
 
       public Errors getRequestErrors() {
          return _errors;
       }
 
       public Object getSessionAttribute(String key) {
          return getIfNotNull(key, _sessionAttributes);
       }
 
       public void removeRequestAttribute(String key) {
          removeIfNotNull(key, _requestAttributes);
          _redirectAttrs.remove(key);
       }
 
       public void removeSessionAttribute(String key) {
          removeIfNotNull(key, _sessionAttributes);
       }
 
       public void setRequestAttribute(String key, Object value) {
          if (_requestAttributes == null) {
             _requestAttributes = new HashMap();
          }
 
          _requestAttributes.put(key, value);
       }
 
       public void setRequestAttribute(String key, Object value, boolean redirectAttr) {
          if (redirectAttr) {
             _redirectAttrs.add(key);
          } else {
             _redirectAttrs.remove(key);
          }
 
          setRequestAttribute(key, value);
       }
 
       public void setSessionAttribute(String key, Object value) {
          if (_sessionAttributes == null) {
             _sessionAttributes = new HashMap();
          }
 
          _sessionAttributes.put(key, value);
       }
 
       public void setInitParameters(Map initParms) {
          _initParms = initParms;
       }
 
       private Object getIfNotNull(String key, Map map) {
          return ((map != null) ? map.get(key) : null);
       }
 
       private void removeIfNotNull(String key, Map map) {
          if (map != null) {
             map.remove(key);
          }
       }
    }
 }
