 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wcs.responses;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import javax.xml.transform.TransformerException;
 
 import org.springframework.context.ApplicationContext;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.global.WCS;
 import org.vfny.geoserver.util.requests.CapabilitiesRequest;
 import org.vfny.geoserver.wcs.WcsException;
 import org.vfny.geoserver.wcs.WcsException.WcsExceptionCode;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public class WCSCapabilitiesResponse implements Response {
     /** package's logger */
     private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(WCSCapabilitiesResponse.class.getPackage()
                                                                                        .getName());
 
     /**
      * Byte array holding the raw content of the capabilities document,
      * generated in <code>execute()</code>
      */
     private byte[] rawResponse;
 
     private ApplicationContext applicationContext;
 
 	public WCSCapabilitiesResponse(ApplicationContext applicationContext) {
 		this.applicationContext = applicationContext;
 	}
 	
     /**
      * Returns any extra headers that this service might want to set in the HTTP response object.
      * @see org.vfny.geoserver.Response#getResponseHeaders()
      */
     public HashMap getResponseHeaders() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request
      *            DOCUMENT ME!
      *
      * @throws ServiceException
      *             DOCUMENT ME!
      * @throws IllegalArgumentException
      *             DOCUMENT ME!
      * @throws WCSException
      *             DOCUMENT ME!
      */
     public void execute(Request request) throws ServiceException {
         if (!(request instanceof CapabilitiesRequest)) {
             throw new IllegalArgumentException("Not a GetCapabilities Request");
         }
         
         CapabilitiesRequest capreq = (CapabilitiesRequest)request;
         int reqUS = -1;
         if (capreq.getUpdateSequence() != null) {
 	        try {
 	        	reqUS = Integer.parseInt(capreq.getUpdateSequence());
 	        } catch (NumberFormatException nfe) {
 	        	throw new ServiceException("GeoServer only accepts numbers in the updateSequence parameter");
 	        }
         }
         int geoUS = request.getServiceRef().getServiceRef().getGeoServer().getUpdateSequence();
     	if (reqUS > geoUS) {
    		throw new WcsException("Client supplied an updateSequence that is greater than the current sever updateSequence", WcsExceptionCode.InvalidParameterValue, "");
     	}
     	if (reqUS == geoUS) {
     		throw new WcsException("WCS capabilities document is current (updateSequence = " + geoUS + ")", WcsExceptionCode.CurrentUpdateSequence, "");
     	}
     	//otherwise it's a normal response...
 
         WCSCapsTransformer transformer = new WCSCapsTransformer(request
 				.getBaseUrl(), applicationContext);
 
         transformer.setIndentation(2);
         final WCS wcsConfig = (WCS) applicationContext.getBean("wcs");
         final Charset encoding = wcsConfig.getCharSet();
         transformer.setEncoding(encoding);
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
             transformer.transform(request, out);
         } catch (TransformerException e) {
             throw new WcsException(e);
         }
 
         this.rawResponse = out.toByteArray();
     }
 
     /**
      * Returns the fixed capabilities MIME type (application/vnd.ogc.WCS_xml) as
      * specified in whe WCS spec, version 1.1.1, section 6.5.3, table 3.
      *
      * @param gs
      *            DOCUMENT ME!
      *
      * @return the capabilities document MIME type.
      *
      * @throws IllegalStateException
      *             if the response was not yet produced.
      */
     public String getContentType(GeoServer gs) throws IllegalStateException {
         if (rawResponse == null) {
             throw new IllegalStateException("execute() not called or not succeed.");
         }
 
         return gs.getMimeType();
     }
 
     /**
      * Just returns <code>null</code>, since no special encoding is applyed
      * to the output data.
      *
      * @return <code>null</code>
      */
     public String getContentEncoding() {
         return null;
     }
 
     /**
      * Just returns <code>null</code>, since no special encoding is applyed
      * to the output data.
      *
      * @return <code>null</code>
      */
     public String getContentDisposition() {
         return null;
     }
 
     /**
      * Writes the capabilities document generated in <code>execute()</code> to
      * the given output stream.
      *
      * @param out
      *            the capabilities document destination
      *
      * @throws ServiceException
      *             never, since the whole content was aquired in
      *             <code>execute()</code>
      * @throws IOException
      *             if it is thrown while writing to <code>out</code>
      * @throws IllegalStateException
      *             if <code>execute()</code> was not called/succeed before
      *             this method is called.
      */
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         if (rawResponse == null) {
             throw new IllegalStateException("execute() not called or not succeed.");
         }
 
         out.write(rawResponse);
     }
 
     /**
      * Does nothing, since no processing is done after <code>execute()</code>
      * has returned.
      *
      * @param gs
      *            the service instance
      */
     public void abort(Service gs) {
     }
 }
