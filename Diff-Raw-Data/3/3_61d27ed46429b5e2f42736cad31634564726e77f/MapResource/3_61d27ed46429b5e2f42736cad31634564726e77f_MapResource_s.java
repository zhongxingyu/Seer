 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.rest;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.geoserver.ows.util.RequestUtils;
 import org.geoserver.platform.GeoServerExtensions;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Resource;
 import org.restlet.resource.StringRepresentation;
 import org.vfny.geoserver.global.GeoServer;
 
 
 /**
  * Base class that converts back and forth between varied data formats and a Java Map to
  * simplify the implementation of a RESTful web API.
  *
  * @author David Winslow <dwinslow@openplans.org>
  */
 public abstract class MapResource extends Resource {
     private Map myFormatMap;
     private DataFormat myRequestFormat;
 	private GeoServer myGeoserver;
     protected static Logger LOG = org.geotools.util.logging.Logging.getLogger("org.geoserver.community");
 
     public MapResource() {
         super();
         myFormatMap = getSupportedFormats();
 		myGeoserver = (GeoServer)GeoServerExtensions.bean("geoServer");
     }
 
     public MapResource(Context context, Request request, Response response) {
         super(context, request, response);
         myFormatMap = getSupportedFormats();
         myRequestFormat = (DataFormat) myFormatMap.get(request.getAttributes().get("type"));
     }
 
     /**
      * This method should return a map of format name == DataFormat for the MapResource to use 
      * in translating to and from different output Formats
      */
     public abstract Map getSupportedFormats();
 
     public void handleGet() {
         try {
 			Object details = getMap();
 
 			String formatName = (String)getRequest().getAttributes().get("type");
 			myRequestFormat = (DataFormat) myFormatMap.get(formatName);
 			getResponse().setEntity(myRequestFormat.makeRepresentation(details));
 				
 	        if ((myRequestFormat == null) | (details == null)) {
 	        	LOG.info("Failed MapResource request; format: " + myRequestFormat + "; details: " + details);
 	        	
 	        	throw new RestletException(new StringRepresentation(
 	        			"Could not find requested resource; format=" + formatName 
 	        			+ "; resource type is: " + MediaType.TEXT_PLAIN
 	        			),
 	        			Status.CLIENT_ERROR_NOT_FOUND
 	        	);
 	        }
 		} catch (RestletException re) {
 			getResponse().setEntity(re.getRepresentation());
 			getResponse().setStatus(re.getStatus());
 		}
     }
 
     /**
      * This method must be overridden by subclasses; it will be called to handle the HTTP GET method.
      * @param details the Map equivalent of the uploaded Representation
      */
     public abstract Object getMap() throws RestletException;
 
     /**
      * Put some metadata about the HTTP location of the resource into a
      * map so that it can be made available to the DataFormat.
      *
      * @return a Map containing page metadata
      */
     public Map getPageDetails() {
         Map map = new HashMap();
         String currentURL = getRequest().getResourceRef().getBaseRef().toString();
         currentURL = RequestUtils.proxifiedBaseURL(currentURL, 
                 myGeoserver.getProxyBaseUrl());
 
         // LOG.info("Proxy Base URL: " + myGeoserver.getProxyBaseUrl());
 
         String formatName = (String) getRequest().getAttributes().get("type");
 
         if (formatName != null) {
             currentURL = currentURL.substring(0, currentURL.length() - (formatName.length() + 2));
         }
 
 		if (currentURL.endsWith("/")){
 			currentURL = currentURL.substring(0, currentURL.length() - 1);
 		}
 
         map.put("currentURL", currentURL);
 
         return map;
     }
 
     public void handlePut() {
         try {
             myRequestFormat = (DataFormat)myFormatMap.get(getRequest().getAttributes().get("type"));
 
             Object details = myRequestFormat.readRepresentation(getRequest().getEntity());
 
             if ((myRequestFormat == null) || (details == null)) {
                 throw new RestletException(new StringRepresentation("Could not find  requested resource",
                         MediaType.TEXT_PLAIN),
                         Status.CLIENT_ERROR_NOT_FOUND
                 );
             }
 
             putMap(details);
         } catch (RestletException re) {
             getResponse().setEntity(re.getRepresentation());
             getResponse().setStatus(re.getStatus());
         }
     }
 
     /**
      * This method should be overridden by subclasses that wish to implement the HTTP PUT method.
      * @param details the Map equivalent of the uploaded Representation
      * @throws RestletException if anything goes wrong.  The Status and Representation in the RestletException will be sent to the client.
      */
     protected void putMap(Object details) throws RestletException {
     	throw new RestletException("PUT not supported for this resource", Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
     }
 }
