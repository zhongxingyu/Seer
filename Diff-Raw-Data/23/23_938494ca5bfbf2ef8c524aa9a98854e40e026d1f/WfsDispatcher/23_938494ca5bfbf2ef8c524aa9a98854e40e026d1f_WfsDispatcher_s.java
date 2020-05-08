 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.servlets.wfs;
 
 import org.vfny.geoserver.WfsException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.requests.readers.DispatcherKvpReader;
 import org.vfny.geoserver.requests.readers.DispatcherXmlReader;
 import org.vfny.geoserver.requests.readers.KvpRequestReader;
 import org.vfny.geoserver.servlets.AbstractService;
 import org.vfny.geoserver.servlets.Dispatcher;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Map;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 
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
 * @version $Id: WfsDispatcher.java,v 1.7 2004/04/22 20:31:34 emperorkefka Exp $
  */
 public class WfsDispatcher extends Dispatcher {
     /** Class logger */
     private static Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.servlets.wfs");
     private static int sequence = 123;
 
     /** Temporary file used to store the request */
     private File temp;
 
     /**
      * This figures out a dispatched post request.  It writes the request to a
      * temp file, and then reads it in twice from there.  I found no other way
      * to do this, but this solution doesn't seem to bad.  Obviously it is
      * better to use the servlets directly, without having to go through this
      * random file writing and reading.  If our xml handlers were written more
      * dynamically this probably wouldn't be necessary, but it is.  Hopefully
      * this should help GeoServer's interoperability with clients, since most
      * of them are kinda stupid, and can't handle different url locations for
      * the different requests.
      *
      * @param request The servlet request object.
      * @param response The servlet response object.
      *
      * @throws ServletException For any servlet problems.
      * @throws IOException For any io problems.
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
         int targetRequest = 0;
 
         try {
             BufferedReader tempReader = new BufferedReader(request.getReader());
 
             // REVISIT: Should do more than sequence here
             // (In case we are running two GeoServers at once)
             // - Could we use response.getHandle() in the filename?
             // - ProcessID is traditional, I don't know how to find that in Java
             sequence++;
             temp = File.createTempFile("wfsdispatch" + sequence, "tmp");
 
             FileWriter out = new FileWriter(temp);
             int c;
 
             while ((c = tempReader.read()) != -1) {
                 out.write(c);
             }
 
             tempReader.close();
             out.close();
 
             BufferedReader disReader = new BufferedReader(new FileReader(temp));
             BufferedReader requestReader = new BufferedReader(new FileReader(
                         temp));
 
             if (disReader != null) {
                 DispatcherXmlReader requestTypeAnalyzer = new DispatcherXmlReader();
                 requestTypeAnalyzer.read(disReader, request);
                 targetRequest = requestTypeAnalyzer.getRequestType();
             } else {
                 targetRequest = UNKNOWN;
             }
 
             LOGGER.fine("post got request " + targetRequest);
 
             doResponse(requestReader, request, response, targetRequest);
         } catch (WfsException wfs) {
             HttpSession session = request.getSession();
             ServletContext context = session.getServletContext();
             GeoServer geoServer = (GeoServer) context.getAttribute(GeoServer.WEB_CONTAINER_KEY);
            String tempResponse = wfs.getXmlResponse(geoServer.isVerboseExceptions());
 
             response.setContentType(geoServer.getCharSet().toString());
             response.getWriter().write(tempResponse);
         }
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
 
         doResponse(null, request, response, targetRequest);
     }
 
     /**
      * Does the actual response, creates the appropriate servlet to handle the
      * detected request.  Note that the requestReader is a bit of a hack, if
      * it is null then it is from a get Request, if not then that means the
      * request is being stored as a file, and needs to be read with this
      * particular reader.
      * 
      * <p>
      * This does have the downside of forcing us to have doGet and doPost
      * methods of AbstractService be public, perhaps there is a good pattern
      * for handling  this.  Or we could try to re-write Dispatcher to extend
      * AbstractService, but it may be tricky.
      * </p>
      *
      * @param requestReader The reader of a file that contains the request,
      *        null if from a get request.
      * @param request The http request that was made.
      * @param response The response to be returned.
      * @param req_type The request type.
      *
      * @throws ServletException for any problems.
      * @throws IOException If anything goes wrong reading or writing.
      */
     protected void doResponse(Reader requestReader, HttpServletRequest request,
         HttpServletResponse response, int req_type)
         throws ServletException, IOException {
         AbstractService dispatched;
         LOGGER.info("req_type is " + req_type);
 
         switch (req_type) {
         case GET_CAPABILITIES_REQUEST:
             dispatched = new Capabilities();
 
             break;
 
         case DESCRIBE_FEATURE_TYPE_REQUEST:
             dispatched = new Describe();
 
             break;
 
         case GET_FEATURE_REQUEST:
             dispatched = new Feature();
 
             break;
 
         case TRANSACTION_REQUEST:
             dispatched = new Transaction();
 
             break;
 
         case GET_FEATURE_LOCK_REQUEST:
             dispatched = new FeatureWithLock();
 
             break;
 
         case LOCK_REQUEST:
             dispatched = new Lock();
 
             break;
 
         default:
             dispatched = null;
         }
 
         if ((dispatched != null)) {
             dispatched.init(servletConfig); //only really needed for init 
 
             if (requestReader == null) {
                 dispatched.doGet(request, response);
             } else {
                 dispatched.doPost(request, response, requestReader);
             }
         } else {
             String message;
 
             if (requestReader == null) {
                 message = "No request recognized.  The REQUEST parameter"
                     + " must be one of GetFeature, GetFeatureWithLock, "
                     + "DescribeFeatureType, LockFeature, or Transaction.";
             } else {
                 message = "The proper request could not be extracted from the"
                     + "the xml posted.  Make sure the case is correct.  The "
                     + "request must be one of GetFeature, GetFeatureWithLock, "
                     + "DescribeFeatureType, LockFeature, or Transaction";
             }
             HttpSession session = request.getSession();
             ServletContext context = session.getServletContext();
             GeoServer geoServer = (GeoServer) context.getAttribute(GeoServer.WEB_CONTAINER_KEY);
             
             WfsException wfse = new WfsException(message);
            String tempResponse = wfse.getXmlResponse(geoServer.isVerboseExceptions());
 
             response.setContentType(geoServer.getCharSet().toString());
             response.getWriter().write(tempResponse);
         }
     }
 }
