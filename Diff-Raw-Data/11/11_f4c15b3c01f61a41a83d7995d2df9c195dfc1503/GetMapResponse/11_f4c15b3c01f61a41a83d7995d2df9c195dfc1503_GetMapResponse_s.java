 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses.wms;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.WmsException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.requests.Request;
 import org.vfny.geoserver.requests.wms.GetMapRequest;
 import org.vfny.geoserver.responses.Response;
 import org.vfny.geoserver.responses.wms.map.GetMapDelegate;
 import org.vfny.geoserver.responses.wms.map.JAIMapResponse;
 import org.vfny.geoserver.responses.wms.map.SVGMapResponse;
 
 
 /**
  * A GetMapResponse object is responsible for generating a map based on a
  * GetMap request. The way the map is generated is independent of this class,
  * wich will use a delegate object based on the output format requested
  *
  * @author Gabriel Roldn
  * @version $Id: GetMapResponse.java,v 1.11 2004/03/14 23:29:30 groldan Exp $
  */
 public class GetMapResponse implements Response {
 
     private static final Logger LOGGER = Logger.getLogger(
       GetMapResponse.class.getPackage().getName());
 
     /** DOCUMENT ME! */
     private static final List delegates = new LinkedList();
     private static final List supportedMimeTypes = new LinkedList();
 
     static {
         GetMapDelegate producer;
 
        producer = new SVGMapResponse();
         supportedMimeTypes.addAll(producer.getSupportedFormats());
         delegates.add(producer);
 
        producer = new JAIMapResponse();
         supportedMimeTypes.addAll(producer.getSupportedFormats());
         delegates.add(producer);
     }
 
     private GetMapDelegate delegate;
 
     /**
      * Creates a new GetMapResponse object.
      */
     public GetMapResponse() {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      */
     public void execute(Request request) throws ServiceException {
         GetMapRequest getMapReq = (GetMapRequest) request;
         this.delegate = getDelegate(getMapReq);
         delegate.execute(request);
         LOGGER.entering(getClass().getName(), "execute", new Object[]{request});
     }
 
     /**
      * asks the internal GetMapDelegate for the MIME type of the map that it
      * will generate or is ready to, and returns it
      *
      * @param gs DOCUMENT ME!
      *
      * @return the MIME type of the map generated or ready to generate
      *
      * @throws IllegalStateException if a GetMapDelegate is not setted yet
      */
     public String getContentType(GeoServer gs) throws IllegalStateException {
         if (delegate == null) {
             throw new IllegalStateException("No request has been proceced");
         }
 
         return delegate.getContentType(gs);
     }
 
     public String getContentEncoding(){
         LOGGER.finer("returning content encoding null");
         return null;
     }
     /**
      * if a GetMapDelegate is set, calls it's abort method. Elsewere do
      * nothing.
      *
      * @param gs DOCUMENT ME!
      */
     public void abort(Service gs) {
         if (delegate != null) {
             LOGGER.fine("asking delegate for aborting the process");
             delegate.abort(gs);
         }
     }
 
     /**
      * delegates the writing and encoding of the results of the request to
      * the <code>GetMapDelegate</code> wich is actually processing it, and
      * has been obtained when <code>execute(Request)</code> was called
      *
      * @param out the output to where the map must be written
      *
      * @throws ServiceException if the delegate throws a ServiceException inside
      * its <code>writeTo(OuptutStream)</code>, mostly due to
      *
      * @throws IOException if the delegate throws an IOException inside
      * its <code>writeTo(OuptutStream)</code>, mostly due to
      *
      * @throws IllegalStateException if this method is called before
      * <code>execute(Request)</code> has succeed
      */
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         if (delegate == null) {
             throw new IllegalStateException(
                 "No GetMapDelegate is setted, make sure you have called execute and it has succeed");
         }
         LOGGER.finer("asking delegate for write to " + out);
         delegate.writeTo(out);
     }
 
     /**
      * Creates a GetMapDelegate specialized in generating the requested map
      * format
      *
      * @param request a request parameter object wich holds the processed
      * request objects, such as layers, bbox, outpu format, etc.
      *
      * @return A specialization of <code>GetMapDelegate</code> wich can produce
      * the requested output map format
      *
      * @throws WmsException if no specialization is configured for the output
      *         format specified in <code>request</code> or if it can't be
      *         instantiated
      */
     private static GetMapDelegate getDelegate(GetMapRequest request)
         throws WmsException {
         String requestFormat = request.getFormat();
         LOGGER.finer("request format is " + requestFormat);
         GetMapDelegate delegate = null;
         Class delegateClass = null;
 
         for (Iterator it = delegates.iterator(); it.hasNext();) {
             delegate = (GetMapDelegate) it.next();
 
             if (delegate.canProduce(requestFormat)) {
                 delegateClass = delegate.getClass();
                 LOGGER.finer("found GetMapDelegate " + delegateClass);
                 break;
             }
         }
 
         if (delegateClass == null) {
             throw new WmsException(requestFormat
                 + " is not recognized as an output format for this server. "
                + "Please consult the Capabilities document",
                "GetMapResponse.getDelegate");
         }
 
         try {
             delegate = (GetMapDelegate) delegateClass.newInstance();
         } catch (Exception ex) {
             throw new WmsException(ex,
                 "Cannot obtain the map generator for the requested format",
                 "GetMapResponse::getDelegate()");
         }
 
         return delegate;
     }
 
     /**
      * iterates over the registered Map producers and fills a list with all the
      * map formats' MIME types that the producers can handle
      *
      * @return DOCUMENT ME!
      */
     static List getMapFormats() {
         return supportedMimeTypes;
     }
 }
