 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.servlets.wms;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.vfny.geoserver.WmsException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.requests.readers.DispatcherKvpReader;
 import org.vfny.geoserver.requests.readers.KvpRequestReader;
 import org.vfny.geoserver.servlets.Dispatcher;
 
 
 /**
  * Routes requests made at the top-level URI to appropriate interface servlet.
  * Note that the logic of this method could be generously described as
  * 'loose.' It is not checking for request validity in any way (this is done
  * by the reqeust- specific servlets).  Rather, it is attempting to make a
  * reasonable guess as to what servlet to call, given that the client is
  * routing to the top level URI as opposed to the request-specific URI, as
  * specified in the GetCapabilities response. Thus, this is a convenience
  * method, which allows for some slight client laziness and helps explain to
  * lost souls/spiders what lives at the URL. Due to the string parsing, it is
  * much faster (and recommended) to use the URIs specified in the
  * GetCapabablities response.  Currently does not support post requests, but
  * most requests for this will likely come with get.
  *
  * @author Chris Holmes, TOPP
 * @version $Id: WmsDispatcher.java,v 1.7 2004/07/15 21:13:13 jmacgill Exp $
  *
  * @task TODO: rework to work too for WMS servlets, and to get the servlets
  *       from ServletContext instead of having them hardcoded
  */
 public class WmsDispatcher extends Dispatcher {
     /** Class logger */
     private static Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.servlets.wms");
 
     /**
      * Passes the Post method to the Get method, with no modifications.
      *
      * @param request The servlet request object.
      * @param response The servlet response object.
      *
      * @throws ServletException For any servlet problems.
      * @throws IOException For any io problems.
      *
      * @task REVISIT: This is not working yet, as we can't seem to figure out
      *       how to read the reader twice.  It must be read once to see what
      *       the request type is,  and again to actually analyze it.  But we
      *       haven't yet found the way  to read it twice.  There should be
      *       some way to do this, but it doesn't seem that important, as users
      *       who use post should be able to figure out which servlet to send
      *       it to. I'm removing DispatcherReaderXml and DispatcherHandler
      *       from cvs, so that they don't get in the 1.0 release.  If anyone
      *       attempts to implement this there are deleted versions in cvs.
      *       Check the attic on the webcvs, or just do a checkout with the
      *       rel_0_98 tag.
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
         //BufferedReader tempReader = request.getReader();
         //String tempResponse = new String();
         int targetRequest = 0;
         LOGGER.finer("got to post request");
         doResponse(false, request, response, targetRequest);
     }
 
     /**
      * Handles all Get requests.  This method implements the main matching
      * logic for the class.
      *
      * @param request The servlet request object.
      * @param response The servlet response object.
      *
      * @throws ServletException For any servlet problems.
      * @throws IOException For any io problems.
      */
     public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
         int targetRequest = 0;
 
         // Examine the incoming request and create appropriate server objects
         //  to deal with each request
         //              try {
         if (request.getQueryString() != null) {
             Map kvPairs = KvpRequestReader.parseKvpSet(request.getQueryString());
             targetRequest = DispatcherKvpReader.getRequestType(kvPairs);
         } else {
             targetRequest = UNKNOWN;
 
             //throw exception
         }
 
         doResponse(false, request, response, targetRequest);
     }
 
     protected void doResponse(boolean isPost, HttpServletRequest request,
         HttpServletResponse response, int req_type)
         throws ServletException, IOException {
         HttpServlet dispatched;
         LOGGER.finer("req_type is " + req_type);
 
         switch (req_type) {
         case GET_CAPABILITIES_REQUEST:
             dispatched = new Capabilities();
 
             break;
 
         case GET_MAP_REQUEST:
             dispatched = new GetMap();
 
             break;
             
         case GET_FEATURE_INFO_REQUEST:
             dispatched = new GetFeatureInfo();
 
             break;   
 
         default:
             dispatched = null;
         }
 
        //TODO: catch the servlet exceptions from the other servlets.
         if ((dispatched != null) && !isPost) {
             dispatched.init(servletConfig); //only needed for init hack, see

            //dispatcher.init()
             dispatched.service(request, response);
         } else {
             String message;
 
             if (isPost) {
                 message = "Post requests are not supported with the dispatcher "
                     + "servlet.  Please try the request using the appropriate "
                     + "request servlet, such as GetCapabilities or GetFeature";
             } else {
                 message = "No wms kvp request recognized.  The REQUEST parameter"
                     + " must be one of GetMap or GetCapabilities";
             }
             
             HttpSession session = request.getSession();
             ServletContext context = session.getServletContext();
 
             GeoServer geoServer = (GeoServer) context.getAttribute(GeoServer.WEB_CONTAINER_KEY);
             
             WmsException wmse = new WmsException(message);
            String tempResponse = wmse.getXmlResponse(geoServer.isVerboseExceptions());
 
             response.setContentType(geoServer.getCharSet().toString());
             response.getWriter().write(tempResponse);
         }
     }
 }
