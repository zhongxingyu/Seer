 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.servlets;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.SocketException;
 import java.nio.charset.Charset;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.vfny.geoserver.ExceptionHandler;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.requests.Request;
 import org.vfny.geoserver.requests.readers.KvpRequestReader;
 import org.vfny.geoserver.requests.readers.XmlRequestReader;
 import org.vfny.geoserver.responses.Response;
 
 
 /**
  * Represents a service that all others extend from.  Subclasses should provide
  * response and exception handlers as appropriate.
  * 
  * <p>
  * It is really important to ensure the following workflow:
  * 
 =======
  * It is <b>really</b> important to ensure the following workflow:
  * 
  * <ol>
  * <li>
  * get a Request reader
  * </li>
  * <li>
  * ask the Request Reader for the Request object - t this time, request
  * parameters should be fully checked<br>
  * i.e. the Request objects contains the list of FeatureTypeInfo's rather
  * than just the type names.
  * </li>
  * <li>
  * Provide the resulting Request with the ServletRequest that generated it 
  * </li>
  * <li>
  * get the appropiate ResponseHandler
  * </li>
  * <li>
  * ask it to execute the Request
  * </li>
  * <li>
  * set the response content type
  * </li>
  * <li>
  * write to the http response's output stream
  * </li>
  * <li>
  * pending - call Response cleanup
  * </li>
  * </ol>
  * </p>
  * 
  * <p>
  * If anything goes wrong a ServiceException can be thrown and will be written
  * to the output stream instead.
  * </p>
  * 
  * <p>
  * This is because we have to be sure that no exception have been produced
  * before setting the response's content type, so we can set the exception
  * specific content type; and that Response.getContentType is called AFTER
  * Response.execute, since the MIME type can depend on any request parameter
  * or another kind of desission making during the execute process. (i.e.
  * FORMAT in WMS GetMap)
  * </p>
  * 
  * <p>
  * TODO: We need to call Response.abort() if anything goes wrong to allow the
  * Response a chance to cleanup after itself.
  * </p>
  *
  * @author Gabriel Roldn
  * @author Chris Holmes
  * @author Jody Garnett
 =======
 * @version $Id: AbstractService.java,v 1.9 2004/01/20 06:31:36 jive Exp $
  */
 public abstract class AbstractService extends HttpServlet {
     /** Class logger */
     private static Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.servlets");
 
     /** DOCUMENT ME! */
     //protected static final GeoServer config = GeoServer.getInstance();
 
     /** Specifies mime type */
     //protected static final String MIME_TYPE = config.getMimeType();
     private static Map context;
 
     /**
      * GR: if SPEED, FILE and BUFFER are static instances, so their methods
      * should be synchronized, ending in a not multiuser server, so I made
      * saftyMode dynamically instantiated in init() and the stratagy choosed
      * at server config level in web.xml. If I'm wrong, just tell me. If this
      * is correct, may be it will be better to allow for user customized
      * ServiceStratagy implementations to be parametrized by a servlet context
      * param JG: You are exactly right! My-Bad, I was just trying to
      * understand what chris was talking about.
      */
     public static final Map serviceStratagys = new HashMap();
 
     static {
         serviceStratagys.put("SPEED", SpeedStratagy.class);
         serviceStratagys.put("FILE", FileStratagy.class);
         serviceStratagys.put("BUFFER", BufferStratagy.class);
     }
 
     /** Controls the Safty Mode used when using execute/writeTo. */
     private static Class saftyMode;
 
     /**
      * loads the "serviceStratagy" servlet context parameter and checks it if
      * reffers to a valid ServiceStratagy (by now, one of SPEED, FILE or
      * BUFFER); if no, just sets the stratagy to BUFFER as default
      *
      * @param config the servlet environment
      *
      * @throws ServletException if the configured stratagy class is not a
      *         derivate of ServiceStratagy or it is thrown by the parent class
      */
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         LOGGER.info("Looking for configured service responses' stratagy");
 
         ServletContext context = config.getServletContext();
         String stgyKey = context.getInitParameter("serviceStratagy");
         Class stgyClass = BufferStratagy.class;
 
         if (stgyKey == null) {
             LOGGER.info("No service stratagy configured, defaulting to BUFFER");
         } else {
             LOGGER.info("Looking for configured service stratagy " + stgyKey);
 
             Class configurefStgyClass = (Class) serviceStratagys.get(stgyKey);
 
             if (configurefStgyClass == null) {
                 LOGGER.info("No service stratagy named " + stgyKey
                     + "found, defaulting to BUFFER. Please check your config");
             } else {
                 stgyClass = configurefStgyClass;
             }
         }
 
         LOGGER.fine("verifying configured stratagy");
 
         if (!(ServiceStratagy.class.isAssignableFrom(stgyClass))) {
             throw new ServletException("the configured service stratagy "
                 + stgyClass + " is not a ServiceStratagy derivate");
         }
 
         LOGGER.info("Using service stratagy " + stgyClass);
         AbstractService.saftyMode = stgyClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request DOCUMENT ME!
      * @param response DOCUMENT ME!
      *
      * @throws ServletException DOCUMENT ME!
      * @throws IOException DOCUMENT ME!
      */
     protected void doGet(HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException {
         // implements the main request/response logic
         Request serviceRequest = null;
         try {
             String qString = request.getQueryString();
 
             //Map requestParams = KvpRequestReader.parseKvpSet(qString);
             Map requestParams = new HashMap();
             String paramName;
             String paramValue;
 
             for (Enumeration pnames = request.getParameterNames();
                     pnames.hasMoreElements();) {
                 paramName = (String) pnames.nextElement();
                 paramValue = request.getParameter(paramName);
                 requestParams.put(paramName.toUpperCase(), paramValue);
             }
 
             KvpRequestReader requestReader = getKvpReader(requestParams);
             serviceRequest = requestReader.getRequest();
         } catch (ServiceException se) {
             sendError(response, se);
 
             return;
         } catch (Throwable e) {
             sendError(response, e);
 
             return;
         }
 
         doService(request, response, serviceRequest);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request DOCUMENT ME!
      * @param response DOCUMENT ME!
      *
      * @throws ServletException DOCUMENT ME!
      * @throws IOException DOCUMENT ME!
      */
     protected void doPost(HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException {
         Request serviceRequest = null;
 
         // implements the main request/response logic
         try {
             XmlRequestReader requestReader = getXmlRequestReader();
             serviceRequest = requestReader.read(request.getReader());
         } catch (ServiceException se) {
             sendError(response, se);
 
             return;
         } catch (Throwable e) {
             sendError(response, e);
 
             return;
         }
 
         doService(request, response, serviceRequest);
     }
 
     /**
      * Peforms service according to ServiceStratagy.
      * 
      * <p>
      * This method has very strict requirements, please see the class
      * description for the specifics.
      * </p>
      * 
      * <p>
      * It has a lot of try/catch blocks, but they are fairly necessary to
      * handle things correctly and to avoid as many ugly servlet responses, so
      * that everything is wrapped correctly.
      * </p>
      *
      * @param request
      * @param response
      * @param serviceRequest
      *
      * @throws ServletException if the stratagy can't be instantiated
      *
      * @task TODO: move gzip response encoding to a filter servlet
      */
     protected void doService(HttpServletRequest request,
         HttpServletResponse response, Request serviceRequest)
         throws ServletException {
         ServiceStratagy stratagy = null;
         LOGGER.finest("getting stratagy instance");
 
         try {
             stratagy = (ServiceStratagy) saftyMode.newInstance();
         } catch (Exception ex) {
             throw new ServletException(stratagy
                 + " is not a valid ServiceStratagy", ex);
         }
 
         LOGGER.finer("stratagy is: " + stratagy);
 
         Response serviceResponse = null;
 
         try {
             serviceResponse = getResponseHandler();
         } catch (Throwable t) {
             sendError(response, t);
             return;
         }
 		LOGGER.finer("serviceRequest provided with HttpServletRequest: " + request);
 		serviceRequest.setHttpServletRequest( request );
 		
         try {
             // execute request
             LOGGER.fine("executing request");
             serviceResponse.execute(serviceRequest);
         } catch (ServiceException serviceException) {
             LOGGER.warning("service exception while executing request: "
                 + serviceException.getMessage());
             serviceResponse.abort(serviceRequest.getGeoServer());
             sendError(response, serviceException);
 
             return;
         } catch (Throwable t) {
             //we can safelly send errors here, since we have not touched response yet
             serviceResponse.abort(serviceRequest.getGeoServer());
             sendError(response, t);
 
             return;
         }
 
         try { //catch block to wrap exceptions properly.
 
             String mimeType = serviceResponse.getContentType(serviceRequest.getGeoServer());
             response.setContentType(mimeType);
             LOGGER.fine("execution succeed, mime type is: " + mimeType);
         } catch (Throwable t) {
             sendError(response, t);
 
             return;
         }
 
         OutputStream stratagyOuput = null;
 
         //obtain the stratagy output stream
         try {
             LOGGER.finest("getting stratagy output");
             stratagyOuput = stratagy.getDestination(response);
             LOGGER.finer("stratagy output is: "
                 + stratagyOuput.getClass().getName());
         } catch (SocketException socketException) {
             LOGGER.fine(
                 "it seems that the user has closed the request stream: "
                 + socketException.getMessage());
 
             // It seems the user has closed the request stream
             // Apparently this is a "cancel" and will quietly go away
             //
             // I will still give stratagy and serviceResponse
             // a chance to clean up
             //
             serviceResponse.abort(serviceRequest.getGeoServer());
             stratagy.abort();
 
             return;
         } catch (IOException ex) {
             serviceResponse.abort(serviceRequest.getGeoServer());
             stratagy.abort();
             sendError(response, ex);
 
             return;
         }
 
         try {
             // gather response
             serviceResponse.writeTo(stratagyOuput);
             stratagyOuput.flush();
             stratagy.flush();
         } catch (java.net.SocketException sockEx) { // user cancel
             serviceResponse.abort(serviceRequest.getGeoServer());
             stratagy.abort();
 
             return;
         }catch (IOException ioException) { // stratagyOutput error
             serviceResponse.abort(serviceRequest.getGeoServer());
             stratagy.abort();
             sendError(response, ioException);
 
             return;
         }catch (ServiceException writeToFailure) { // writeTo Failure
             serviceResponse.abort(serviceRequest.getGeoServer());
             stratagy.abort();
             sendError(response, writeToFailure);
 
             return;
         }catch (Throwable help) { // This is an unexpected error(!)
             serviceResponse.abort(serviceRequest.getGeoServer());
             stratagy.abort();
             sendError(response, help);
 
             return;
         }
 
         // Finish Response
         // I have moved closing the output stream out here, it was being
         // done by a few of the ServiceStratagy
         //
         // By this time serviceResponse has finished successfully
         // and stratagy is also finished
         //
         try {
             response.getOutputStream().flush();
             response.getOutputStream().close();
         } catch (SocketException sockEx) { // user cancel
             LOGGER.warning("Could not send completed response to user:"
                 + sockEx);
 
             return;
         }catch (IOException ioException) {
             // This is bad, the user did not get the completed response
             LOGGER.warning("Could not send completed response to user:"
                 + ioException);
 
             return;
         }
 
         LOGGER.info("Service handled");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected abstract Response getResponseHandler();
 
     /**
      * DOCUMENT ME!
      *
      * @param params DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws ServiceException if the set of key/value pairs of parameters
      *         defined by <code>params</code> can't be parsed to a valid
      *         <code>RequestKvpReader</code>
      */
     protected abstract KvpRequestReader getKvpReader(Map params);
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected abstract XmlRequestReader getXmlRequestReader();
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected abstract ExceptionHandler getExceptionHandler();
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected String getMimeType() {
 		ServletContext context = getServletContext();
 		try{
 			return ((GeoServer)context.getAttribute( "GeoServer" )).getMimeType();
 		}catch(NullPointerException e){
 			return "text/xml; charset=" + Charset.forName("ISO-8859-1").displayName();
 		}
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param response DOCUMENT ME!
      * @param content DOCUMENT ME!
      */
     protected void send(HttpServletResponse response, CharSequence content) {
         try {
             response.setContentType(getMimeType());
             response.getWriter().write(content.toString());
         } catch (IOException ex) { //stream closed by client, do nothing
             LOGGER.fine(ex.getMessage());
         }
     }
 
     /**
      * Send error produced during getService opperation.
      * 
      * <p>
      * Some errors know how to write themselves out WfsTransactionException for
      * instance. It looks like this might be is handled by
      * getExceptionHandler().newServiceException( t, pre, null ). I still
      * would not mind seeing a check for ServiceConfig Exception here.
      * </p>
      * 
      * <p>
      * This code says that it deals with UNCAUGHT EXCEPTIONS, so I think it
      * would be wise to explicitly catch ServiceExceptions.
      * </p>
      *
      * @param response DOCUMENT ME!
      * @param t DOCUMENT ME!
      */
     protected void sendError(HttpServletResponse response, Throwable t) {
         if (t instanceof ServiceException) {
             sendError(response, (ServiceException) t);
 
             return;
         }
 
         LOGGER.info("Had an undefined error: " + t.getMessage());
 
         //TODO: put the stack trace in the logger.
         t.printStackTrace();
 
         String pre = "UNCAUGHT EXCEPTION";
         ExceptionHandler exHandler = getExceptionHandler();
         ServiceException se = exHandler.newServiceException(t, pre, null);
 
         //sendError(response, se);
         send(response, se.getXmlResponse(true));
     }
 
     /**
      * Send a serviceException produced during getService opperation.
      *
      * @param response DOCUMENT ME!
      * @param se DOCUMENT ME!
      */
     protected void sendError(HttpServletResponse response, ServiceException se) {
         send(response, se.getXmlResponse());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param response DOCUMENT ME!
      * @param result DOCUMENT ME!
      */
     protected void send(HttpServletResponse response, Response result) {
         OutputStream responseOut = null;
 
         try {
             responseOut = response.getOutputStream();
         } catch (IOException ex) { //stream closed, do nothing.
             LOGGER.info("apparently client has closed stream: "
                 + ex.getMessage());
         }
 
         OutputStream out = new BufferedOutputStream(responseOut);
 		ServletContext context = getServletContext();
         response.setContentType(result.getContentType((GeoServer)context.getAttribute( "GeoServer" )));
 
         try {
             result.writeTo(out);
             out.flush();
             responseOut.flush();
         } catch (IOException ioe) {
             //user just closed the socket stream, do nothing
             LOGGER.fine("connection closed by user: " + ioe.getMessage());
         } catch (ServiceException ex) {
             sendError(response, ex);
         }
     }
 
     /**
      * Checks if the client requests supports gzipped responses by quering it's
      * 'accept-encoding' header.
      *
      * @param request the request to query the HTTP header from
      *
      * @return true if 'gzip' if one of the supported content encodings of
      *         <code>request</code>, false otherwise.
      */
     protected boolean requestSupportsGzip(HttpServletRequest request) {
         boolean supportsGzip = false;
         String header = request.getHeader("accept-encoding");
 
         if ((header != null) && (header.indexOf("gzip") > -1)) {
             supportsGzip = true;
         }
 
         return supportsGzip;
     }
 
     /**
      * Interface used for ServiceMode stratagy objects.
      * 
      * <p>
      * While this interface resembles the Enum idiom in that only three
      * instances are available SPEED, BUFFER and FILE, we are using this class
      * to plug-in the implementation for our doService request in the manner
      * of the Stratagy pattern.
      * </p>
      *
      * @author Jody Garnett, Refractions Research
      */
     static public interface ServiceStratagy {
         /**
          * Get a OutputStream we can use to add content.
          * 
          * <p>
          * JG - Can we replace this with a Writer?
          * </p>
          *
          * @param response
          *
          * @return
          *
          * @throws IOException
          */
         public OutputStream getDestination(HttpServletResponse response)
             throws IOException;
 
         /**
          * Complete opperation in the positive.
          * 
          * <p>
          * Gives service a chance to finish with destination, and clean up any
          * resources.
          * </p>
          */
         public void flush() throws IOException;
 
         /**
          * Complete opperation in the negative.
          * 
          * <p>
          * Gives ServiceConfig a chance to clean up resources
          * </p>
          */
         public void abort();
     }
 }
 
 
 /**
  * Fast and Dangeroud service stratagy.
  * 
  * <p>
  * Will fail when a ServiceException is encountered on writeTo, and will not
  * tell the user about it!
  * </p>
  * 
  * <p>
  * This is the worst case scenario, you are trading speed for danger by using
  * this ServiceStratagy.
  * </p>
  *
  * @author jgarnett
  */
 class SpeedStratagy implements AbstractService.ServiceStratagy {
     private OutputStream out = null;
 
     /**
      * Works against the real output stream provided by the response.
      * 
      * <p>
      * This is dangerous of course, but fast and exciting.
      * </p>
      *
      * @param response Response provided by doService
      *
      * @return An OutputStream that works against, the response output stream.
      *
      * @throws IOException If response output stream could not be aquired
      */
     public OutputStream getDestination(HttpServletResponse response)
         throws IOException {
         out = response.getOutputStream();
         out = new BufferedOutputStream(out);
 
         return out;
     }
 
     /**
      * Completes writing to Response.getOutputStream.
      *
      * @throws IOException If Response.getOutputStream not available.
      */
     public void flush() throws IOException {
         if (out != null) {
             out.flush();
         }
 
         // should we close? BufferStratagy uses close?
         // out.close();
     }
 
     /* (non-Javadoc)
      * @see org.vfny.geoserver.servlets.AbstractService.ServiceStratagy#abort()
      */
     public void abort() {
         // out.close();
     }
 }
 
 
 /**
  * A safe Service stratagy that buffers output until writeTo completes.
  * 
  * <p>
  * This stratagy wastes memory, for saftey. It represents a middle ground
  * between SpeedStratagy and FileStratagy
  * </p>
  *
  * @author jgarnett To change the template for this generated type comment go
  *         to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
  *         Comments
  */
 class BufferStratagy implements AbstractService.ServiceStratagy {
     ByteArrayOutputStream buffer = null;
     private HttpServletResponse response;
 
     /**
      * Provides a ByteArrayOutputStream for writeTo.
      *
      * @param response Response being processed.
      *
      * @return A ByteArrayOutputStream for writeTo opperation.
      *
      * @throws IOException DOCUMENT ME!
      */
     public OutputStream getDestination(HttpServletResponse response)
         throws IOException {
         this.response = response;
         buffer = new ByteArrayOutputStream(1024 * 1024);
 
         return buffer;
     }
 
     /**
      * Copies Buffer to Response output output stream.
      *
      * @throws IOException If the response outputt stream is unavailable.
      */
     public void flush() throws IOException {
         if ((buffer == null) || (response == null)) {
             return; // should we throw an Exception here
         }
 
         OutputStream out = response.getOutputStream();
         BufferedOutputStream buffOut = new BufferedOutputStream(out, 1024 * 1024);
         buffer.writeTo(buffOut);
         buffOut.flush();
         //buffOut.close(); // I think this cloes response.getOutputStream()
     }
 
     /**
      * Clears the buffer with out writing anything out to response.
      *
      * @see org.vfny.geoserver.servlets.AbstractService.ServiceStratagy#abort()
      */
     public void abort() {
         if (buffer == null) {
             return;
         }
     }
 }
 
 
 /**
  * A safe ServiceConfig stratagy that uses a temporary file until writeTo completes.
  *
  * @author $author$
 * @version $Revision: 1.9 $
  */
 class FileStratagy implements AbstractService.ServiceStratagy {
     /** Buffer size used to copy safe to response.getOutputStream() */
     private static int BUFF_SIZE = 4096;
 
     /** Temporary file number */
     static int sequence = 0;
 
     /** Response being targeted */
     private HttpServletResponse response;
 
     /** OutputStream provided to writeTo method */
     private OutputStream safe;
 
     /** Temporary file used by safe */
     private File temp;
 
     /**
      * Provides a outputs stream on a temporary file.
      * 
      * <p>
      * I have changed this to use a BufferedWriter to agree with SpeedStratagy.
      * </p>
      *
      * @param response Response being handled
      *
      * @return Outputstream for a temporary file
      *
      * @throws IOException If temporary file could not be created.
      */
     public OutputStream getDestination(HttpServletResponse response)
         throws IOException {
         // REVISIT: Should do more than sequence here
         // (In case we are running two GeoServers at once)
         // - Could we use response.getHandle() in the filename?
         // - ProcessID is traditional, I don't know how to find that in Java
         sequence++;
         temp = File.createTempFile("wfs" + sequence, "tmp");
 
         safe = new BufferedOutputStream(new FileOutputStream(temp));
 
         return safe;
     }
 
     /**
      * Closes safe output stream, copies resulting file to response.
      *
      * @throws IOException If temporay file or response is unavailable
      * @throws IllegalStateException if flush is called before getDestination
      */
     public void flush() throws IOException {
         if ((temp == null) || (response == null) || (safe == null)
                 || !temp.exists()) {
             throw new IllegalStateException(
                 "flush should only be called after getDestination");
         }
 
         InputStream copy = null;
 
         try {
             safe.flush();
             safe.close();
             safe = null;
 
             // service succeeded in producing a response!
             // copy result to the real output stream
             copy = new BufferedInputStream(new FileInputStream(temp));
 
             OutputStream out = response.getOutputStream();
             out = new BufferedOutputStream(out, 1024 * 1024);
 
             byte[] buffer = new byte[BUFF_SIZE];
             int b;
 
             while ((b = copy.read(buffer, 0, BUFF_SIZE)) > 0) {
                 out.write(buffer, 0, b);
             }
 
             // Speed Writer closes output Stream
             // I would prefer to leave that up to doService...
             // out.flush();
             // out.close();
         } catch (IOException ioe) {
             throw ioe;
         } finally {
             if (copy != null) {
                 try {
                     copy.close();
                 } catch (Exception ex) {
                 }
             }
 
             copy = null;
 
             if ((temp != null) && temp.exists()) {
                 temp.delete();
             }
 
             temp = null;
             response = null;
             safe = null;
         }
     }
 
     /**
      * Clean up after writeTo fails.
      *
      * @see org.vfny.geoserver.servlets.AbstractService.ServiceStratagy#abort()
      */
     public void abort() {
         if (safe != null) {
             try {
                 safe.close();
             } catch (IOException ioException) {
             }
 
             safe = null;
         }
 
         if ((temp != null) && temp.exists()) {
             temp.delete();
         }
 
         temp = null;
         response = null;
     }
 }
