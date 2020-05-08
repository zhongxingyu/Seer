 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 import org.geotools.feature.Feature;
 import org.geotools.filter.Filter;
 import org.geotools.filter.FilterHandler;
 import org.geotools.gml.GMLHandlerFeature;
 import org.vfny.geoserver.responses.WfsException;
 import org.vfny.geoserver.responses.WfsTransactionException;
 
 /**
  * Uses SAX to extact a Transactional request from and incoming XML stream.
  *
  * @version $VERSION$
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  */
 public class TransactionHandler 
     extends XMLFilterImpl  
     implements ContentHandler, FilterHandler, GMLHandlerFeature {
 
     private static final short UNKNOWN = 0;
     private static final short INSERT = 1;
     private static final short DELETE = 2;
     private static final short UPDATE = 3;
     private static final short PROPERTY_NAME = 4;
     private static final short VALUE = 5;
     private static final short PROPERTY = 6;
 
     /** Class logger */
     private static Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.requests");
 
     /** Internal transaction request for construction. */
     private TransactionRequest request = new TransactionRequest();
     
     /** Tracks current sub request */
     private SubTransactionRequest subRequest = null;
 
     /** Tracks tag we are currently inside: helps maintain state. */
     private short state = UNKNOWN;
   
     /** holds the property name for an update request. */
     private String curPropertyName;
 
     /** holds the property value for an update request. */
     private String curPropertyValue;
 
     /** holds the list of features for an insert request. */
     private List curFeatures;
 
     /** Empty constructor. */
     public TransactionHandler () { super(); }
     
     
     /**
      * Returns the Transaction request.
      */ 
     public TransactionRequest getRequest() {
         return request;
     }
     
     /**
      * Gets the short representation of the element we're on.
      *
      * @param stateName the localName of an element.
      * @return the short representation of the localName.
      */    
     private static short setState(String stateName) {
         if(stateName.equals("Insert")) {
             return INSERT;
         } else if(stateName.equals("Delete")) {
             return DELETE;
         } else if(stateName.equals("Update")) {
             return  UPDATE;
         } else if(stateName.equals("Name")) {
             return PROPERTY_NAME;
         } else if(stateName.equals("Value")) {
             return VALUE;
         } else if(stateName.equals("Property")) {
             return PROPERTY;
         }else {
             return UNKNOWN;
         }           
     }
 
 
     /*************************************************************************
      *  Standard SAX content handler methods                                 *
      *************************************************************************/
     /**
      * Notes the start of the element and sets type names and query attributes.
      * @param namespaceURI URI for namespace appended to element.
      * @param localName Local name of element.
      * @param rawName Raw name of element.
      * @param atts Element attributes.
      * @throws SAXException When the XML is not well formed.
      */ 
     public void startElement(String namespaceURI, String localName, 
                              String rawName, Attributes atts)
         throws SAXException {
         LOGGER.finest("at start element: " + localName);
         
         // at start of element, set insidetag flag to whatever tag we're inside
         state = setState(localName);
         
         // if at a query element, empty the current query, set insideQuery 
         //  flag, and get query typeNames
         if(state == DELETE || state == UPDATE || state == INSERT) {
             if(state == DELETE) {
                 subRequest = new DeleteRequest();
             } else if(state == UPDATE) {
                 subRequest = new UpdateRequest();                
             } else if(state == INSERT) {
                 subRequest = new InsertRequest();   
 		curFeatures = new ArrayList();
             }
 	    
             for(int i = 0, n = atts.getLength(); i < n; i++) {
                 String name = atts.getLocalName(i);
                 String value = atts.getValue(i);
                 LOGGER.finest("found attribute '" + name + "'=" + value);
                 if(name.equals("typeName")) {
                     subRequest.setTypeName(value);
                 } else if(name.equals("handle")) {
                     subRequest.setHandle(value);
                 }
 	    }
 	} else if(localName.equals("Transaction")) {
 	    for(int i = 0; i < atts.getLength(); i++) {
 		if( atts.getLocalName(i).equals("handle") ) {
 		    LOGGER.finest("found handle: " + atts.getValue(i));
 		    request.setHandle(atts.getValue(i));
 		}
 	    }
 	}
     }
     
     
     /**
      * Notes the end of the element exists query or bounding box.
      * @param namespaceURI URI for namespace appended to element.
      * @param localName Local name of element.
      * @param rawName Raw name of element.
      * @throws SAXException When the XML is not well formed.
      */ 
     public void endElement(String namespaceURI,String localName,String rawName)
         throws SAXException {        
         LOGGER.finer("at end element: " + localName);
 
         // as we leave query, set insideTag to "NULL" (otherwise the stupid 
         //  characters method picks up external chars)
         state = setState(localName);
         
         // set insideQuery flag as we leave the query and add the query to the 
         //  return list
         if(state == DELETE || state == UPDATE || state == INSERT) {
	    if (state == INSERT) {
 		try {
 		((InsertRequest) subRequest).addFeatures
 		    ((Feature [])curFeatures.toArray(new Feature[0])) ;
 		} catch (WfsException we) {
 		    throw new SAXException("Problem adding features: " + 
 					   we.getMessage(), we);
 		}
 		curFeatures = new ArrayList();
 	    }
             request.addSubRequest(subRequest);
         } else if(state == PROPERTY) {
 	    if (subRequest.getClass().equals(UpdateRequest.class)) {
 		((UpdateRequest) subRequest).addProperty(curPropertyName,
 							 curPropertyValue);
 		curPropertyName = new String();
 		curPropertyValue = new String();
 	    } else {
 		throw new SAXException("<property> element should only occur " 
 				       + "within a <update> element.");
 	    }
 	}
 
         state = UNKNOWN;
     }
         
     /**
      * Checks if inside parsed element and adds its contents to appropriate 
      * variable.
      * @param ch URI for namespace appended to element.
      * @param start Local name of element.
      * @param length Raw name of element.
      * @throws SAXException When the XML is not well formed.
      */ 
     public void characters(char[] ch, int start, int length)
         throws SAXException {
         
         // if inside a property element, add the element
         if(state == PROPERTY_NAME) {
             String s = new String(ch, start, length);
             LOGGER.finest("found property name: " + s);
 	    curPropertyName = s;
         } else if (state == VALUE) {
             String s = new String(ch, start, length);
             curPropertyValue = s;
         }        
     }
     
     /**
      * Gets a filter and adds it to the appropriate query (or queries).
      * @param filter (OGC WFS) Filter from (SAX) filter.
      */ 
     public void filter(Filter filter) {
         try {
             subRequest.setFilter(filter);
         } catch(WfsException e) {
         }
     }    
 
     /**
      * Gets a feature and adds it to the list of current features,
      * to be added to the insert request when it finishes.  This
      * class is called by children filters, needed to implement
      * GMLHandlerFilter.
      *
      * @param feature to be added to the request.
      */
     public void feature(Feature feature) {
 	    curFeatures.add(feature); 
 	    LOGGER.finest("feature added: " + feature);
 
     }
     
 	    
 }
