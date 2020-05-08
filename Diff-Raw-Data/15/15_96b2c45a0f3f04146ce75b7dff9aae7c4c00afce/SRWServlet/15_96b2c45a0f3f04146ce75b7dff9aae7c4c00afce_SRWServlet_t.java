 /*
  *
  * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
  * reserved.
  *
  *       \"This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/).\"
  */
 /*
    Copyright 2006 OCLC Online Computer Library Center, Inc.
 
     Licensed under the Apache License, Version 2.0 (the \"License\");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an \"AS IS\" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
  */
 
 package ORG.oclc.os.SRW;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.soap.SOAPException;
 
 import org.apache.axis.AxisEngine;
 import org.apache.axis.AxisFault;
 import org.apache.axis.ConfigurationException;
 import org.apache.axis.Constants;
 import org.apache.axis.Message;
 import org.apache.axis.MessageContext;
 import org.apache.axis.components.logger.LogFactory;
 import org.apache.axis.description.OperationDesc;
 import org.apache.axis.description.ServiceDesc;
 import org.apache.axis.handlers.soap.SOAPService;
 import org.apache.axis.security.servlet.ServletSecurityProvider;
 import org.apache.axis.transport.http.AxisHttpSession;
 import org.apache.axis.transport.http.AxisServlet;
 import org.apache.axis.transport.http.HTTPConstants;
 import org.apache.axis.transport.http.HTTPTransport;
 import org.apache.axis.transport.http.ServletEndpointContextImpl;
 import org.apache.axis.utils.Admin;
 import org.apache.axis.utils.JavaUtils;
 import org.apache.axis.utils.Messages;
 import org.apache.axis.utils.XMLUtils;
 import org.apache.commons.logging.Log;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  *
  * @author Doug Davis (dug@us.ibm.com)
  * @author Steve Loughran
  * xdoclet tags are not active yet; keep web.xml in sync
  * @web.servlet name="AxisServlet"  display-name="Apache-Axis Servlet"
  * @web.servlet-mapping url-pattern="/servlet/AxisServlet"
  * @web.servlet-mapping url-pattern="*.jws"
  * @web.servlet-mapping url-pattern="/services/\*"
  */
 public class SRWServlet extends AxisServlet {
     protected static Log log=LogFactory.getLog(SRWServlet.class.getName());
 
     /**
      * this log is for timing
      */
     private static Log tlog =
         LogFactory.getLog(Constants.TIME_LOG_CATEGORY);
 
     /**
      * a separate log for exceptions lets users route them
      * differently from general low level debug info
      */
     private static Log exceptionLog =
             LogFactory.getLog(Constants.EXCEPTION_LOG_CATEGORY);
 
     public static final String INIT_PROPERTY_TRANSPORT_NAME =
         "transport.name";
 
     public static final String INIT_PROPERTY_USE_SECURITY =
         "use-servlet-security";
     public static final String INIT_PROPERTY_ENABLE_LIST =
         "axis.enableListQuery";
 
     public static final String INIT_PROPERTY_JWS_CLASS_DIR =
         "axis.jws.servletClassDir";
 
     // These have default values.
     private String transportName;
 
     private ServletSecurityProvider securityProvider = null;
 
     /**
      * cache of logging debug option; only evaluated at init time.
      * So no dynamic switching of logging options with this servlet.
      */
      private static boolean isDebug = false;
 
     /**
      * Should we enable the "?list" functionality on GETs?  (off by
      * default because deployment information is a potential security
      * hole)
      */
     private boolean enableList = false;
 
 
     /**
      * Cached path to JWS output directory
      */
     private String jwsClassDir = null;
     @Override
     protected String getJWSClassDir() { return jwsClassDir; }
 
     protected SRWServletInfo srwInfo=null;
 
 
     /**
      * create a new servlet instance
      */
     public SRWServlet() {
         if(log instanceof org.apache.commons.logging.impl.SimpleLog)
             ((org.apache.commons.logging.impl.SimpleLog)log).setLevel(3); // info
     }
 
     /**
      * Initialization method.
      */
     @Override
     public void init() throws ServletException {
         srwInfo=new SRWServletInfo();
         srwInfo.init(getServletConfig());
 
         super.init();
 
         ServletContext context = getServletConfig().getServletContext();
 
 
         isDebug= log.isDebugEnabled();
         if(isDebug) log.debug("In servlet init");
 
         transportName = getOption(context,
                                   INIT_PROPERTY_TRANSPORT_NAME,
                                   HTTPTransport.DEFAULT_TRANSPORT_NAME);
 
         if (JavaUtils.isTrueExplicitly(getOption(context, INIT_PROPERTY_USE_SECURITY, null))) {
             securityProvider = new ServletSecurityProvider();
         }
 
         enableList =
             JavaUtils.isTrueExplicitly(getOption(context, INIT_PROPERTY_ENABLE_LIST, null));
 
         jwsClassDir = getOption(context, INIT_PROPERTY_JWS_CLASS_DIR, null);
 
         /**
          * There are DEFINATE problems here if
          * getHomeDir and/or getDefaultJWSClassDir return null
          * (as they could with WebLogic).
          * This needs to be reexamined in the future, but this
          * should fix any NPE's in the mean time.
          */
         if (jwsClassDir != null) {
             if (getHomeDir() != null) {
                 jwsClassDir = getHomeDir() + jwsClassDir;
             }
         } else {
             jwsClassDir = getDefaultJWSClassDir();
         }
     }
 
 
 
     /**
      * Process GET requests. This includes handoff of pseudo-SOAP requests
      *
      * @param request request in
      * @param response request out
      * @throws ServletException
      * @throws IOException
      */
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
     {
         log.debug("Enter: doGet()");
         if (log.isInfoEnabled()) {
             log.info(request.getRequestURL().append('?').append(request.getQueryString()).toString());
         }
 //        try {
 //            request.setCharacterEncoding("UTF-8");
 //        } catch (java.io.UnsupportedEncodingException e){}
 
 
         try 
         {
             AxisEngine engine = getEngine();
             ServletContext servletContext =
                 getServletConfig().getServletContext();
 
             String pathInfo = request.getPathInfo();
             String realpath = servletContext.getRealPath(request.getServletPath());
             if (realpath == null) {
                 realpath = request.getServletPath();
             }
 
             boolean wsdlRequested = false;
             boolean listRequested = false;
             boolean hasParameters = request.getParameterNames().hasMoreElements();
 
             //JWS pages are special; they are the servlet path and there
             //is no pathinfo...we map the pathinfo to the servlet path to keep
             //it happy
             boolean isJWSPage = request.getRequestURI().endsWith(".jws");
             if(isJWSPage) {
                 pathInfo= request.getServletPath();
             }
 
             // check first if we are doing WSDL or a list operation
             String queryString = request.getQueryString();
             boolean doExplain=false;
             if (queryString != null) {
                 if (queryString.equalsIgnoreCase("wsdl")) {
                     wsdlRequested = true;
                 } else if (queryString.equalsIgnoreCase("list")) {
                     listRequested = true;
                 }
                 else {
                     String operation=request.getParameter("operation");
                     //MIH changed to one if-statement
                     if(operation!=null && operation.equals("explain")) {
                          doExplain=true;
                     } else if(request.getParameter("query")==null &&
                       request.getParameter("scanClause")==null)
                         doExplain=true;
                 }
             }
             else
                 doExplain=true;
 
             boolean hasNoPath = (pathInfo == null || pathInfo.equals(""));
             if (!wsdlRequested && !listRequested && hasNoPath) {
                 // If the user requested the servlet (i.e. /axis/servlet/AxisServlet)
                 // with no service name, present the user with a list of deployed
                 // services to be helpful
                 // Don't do this if we are doing WSDL or list.
                 reportAvailableServices(response, request);
             } else if (realpath != null || doExplain) {
                 // We have a pathname, so now we perform WSDL or list operations
 
                 // get message context w/ various properties set
                 MessageContext msgContext = createMessageContext(engine, request, response);
 
                 if(doExplain) {
                     srwInfo.handleExplain(request, response, msgContext);
                     return;
                 }
 
                 // NOTE:  HttpUtils.getRequestURL has been deprecated.
                 // This line SHOULD be:
                 //    String url = req.getRequestURL().toString()
                 // HOWEVER!!!!  DON'T REPLACE IT!  There's a bug in
                 // req.getRequestURL that is not in HttpUtils.getRequestURL
                 // req.getRequestURL returns "localhost" in the remote
                 // scenario rather than the actual host name.
                 //
                 // ? Still true?  For which JVM's?
                 //String url = HttpUtils.getRequestURL(request).toString();
                 String url = request.getRequestURL().toString();
 
                 msgContext.setProperty(MessageContext.TRANS_URL, url);
 
 
                 if (wsdlRequested) {
                     // Do WSDL generation
                     msgContext.setTargetService("SRW");
                     processWsdlRequest(msgContext, response);
                 } else if (listRequested) {
                     // Do list, if it is enabled
                     processListRequest(response);
                 } else if (hasParameters) {
                     // If we have ?method=x&param=y in the URL, make a stab
                     // at invoking the method with the parameters specified
                     // in the URL
 
                     processMethodRequest(msgContext, request, response);
 
                 } else {
 
                     // See if we can locate the desired service.  If we
                     // can't, return a 404 Not Found.  Otherwise, just
                     // print the placeholder message.
 
                     String serviceName;
                     if (pathInfo.startsWith("/")) {
                         serviceName = pathInfo.substring(1);
                     } else {
                         serviceName = pathInfo;
                     }
 
                     SOAPService s = engine.getService(serviceName);
                     if (s == null) {
                         //no service: report it
                         if(isJWSPage) {
                             reportCantGetJWSService(request, response);
                         } else {
                             reportCantGetAxisService(request, response);
                         }
 
                     } else {
                         //print a snippet of service info.
                         reportServiceInfo(response, s, serviceName);
                     }
                 }
             } else {
                 // We didn't have a real path in the request, so just
                 // print a message informing the user that they reached
                 // the servlet.
 
                 response.setContentType("text/html");
                 PrintWriter writer=response.getWriter();
                 writer.println( "<html><h1>Axis HTTP Servlet</h1>" );
                 writer.println( Messages.getMessage("reachedServlet00"));
 
                 writer.println("<p>" +
                                Messages.getMessage("transportName00",
                                          "<b>" + transportName + "</b>"));
                 writer.println("</html>");
                 writer.close();
             }
         } catch (AxisFault fault) {
             reportTroubleInGet(fault, response);
         } catch (Exception e) {
             reportTroubleInGet(e, response);
         } finally {
             //writer.close();
             log.debug("Exit: doGet()");
         }
     }
 
     /**
      * when we get an exception or an axis fault in a GET, we handle
      * it almost identically: we go 'something went wrong', set the response
      * code to 500 and then dump info. But we dump different info for an axis fault
      * or subclass thereof.
      * @param exception what went wrong
      * @param response current response
      */
     private void reportTroubleInGet(Exception exception, HttpServletResponse response) throws IOException {
         response.setContentType("text/html");
         PrintWriter writer=response.getWriter();
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         writer.println("<h2>" +
                        Messages.getMessage("error00") +
                        "</h2>");
         writer.println("<p>" +
                        Messages.getMessage("somethingWrong00") +
                        "</p>");
         if(exception instanceof AxisFault) {
             AxisFault fault=(AxisFault)exception;
             processAxisFault(fault);
             writeFault(writer, fault);
         } else {
             logException(exception);
             writer.println("<pre>Exception - " + exception + "<br>");
             //dev systems only give fault dumps
             if (isDevelopment()) {
                 writer.println(JavaUtils.stackToString(exception));
             }
             writer.println("</pre>");
         }
         writer.close();
     }
 
     /**
      * routine called whenever an axis fault is caught; where they
      * are logged and any other business. The method may modify the fault
      * in the process
      * @param fault what went wrong.
      */
     @Override
     protected void processAxisFault(AxisFault fault) {
         //log the fault
         Element runtimeException = fault.lookupFaultDetail(
                 Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
         if (runtimeException != null) {
             exceptionLog.info(Messages.getMessage("axisFault00"), fault);
             //strip runtime details
             fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
         } else if (exceptionLog.isDebugEnabled()) {
             exceptionLog.debug(Messages.getMessage("axisFault00"), fault);
         }
         //dev systems only give fault dumps
         if (!isDevelopment()) {
             //strip out the stack trace
             fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_STACKTRACE);
         }
     }
 
     /**
      * log any exception to our output log, at our chosen level
      * @param e what went wrong
      */
     protected void logException(Exception e) {
         exceptionLog.info(Messages.getMessage("exception00"), e);
     }
 
     /**
      * this method writes a fault out to an HTML stream. This includes
      * escaping the strings to defend against cross-site scripting attacks
      * @param writer
      * @param axisFault
      */
     private void writeFault(PrintWriter writer, AxisFault axisFault) {
         String localizedMessage = XMLUtils.xmlEncodeString(axisFault.getLocalizedMessage());
         writer.println("<pre>Fault - " + localizedMessage + "<br>");
         writer.println(axisFault.dumpToString());
         writer.println("</pre>");
     }
     
     /**
      * handle a ?wsdl request
      * @param msgContext message context so far
      * @param response response to write to
      * @throws AxisFault when anything other than a Server.NoService fault is reported
      * during WSDL generation
      */
     protected void processWsdlRequest(MessageContext msgContext,
                                       HttpServletResponse response) throws AxisFault, IOException {
         AxisEngine engine = getEngine();
         PrintWriter writer;
         try {
             //MIH: retrieve WSDL hardcoded
             response.setContentType("text/xml");
             writer=response.getWriter();
             writer.print(getWsdl());
             writer.close();
             /////////////////////////////////
 //            engine.generateWSDL(msgContext);
 //            Document doc = (Document) msgContext.getProperty("WSDL");
 //            if (doc != null) {
 //                response.setContentType("text/xml");
 //                writer=response.getWriter();
 //                XMLUtils.DocumentToWriter(doc, writer);
 //                writer.close();
 //            } else {
 //                if (log.isDebugEnabled()) {
 //                    log.debug("processWsdlRequest: failed to create WSDL");
 //                }
 //                reportNoWSDL(response, "noWSDL02", null);
 //            }
         } catch (AxisFault axisFault) {
             //the no-service fault is mapped to a no-wsdl error
             if(axisFault.getFaultCode() .equals(Constants.QNAME_NO_SERVICE_FAULT_CODE)) {
                 //which we log
                 processAxisFault(axisFault);
                 //then report under a 404 error
                 response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                 reportNoWSDL(response, "noWSDL01", axisFault);
             } else {
                 //all other faults get thrown
                 throw axisFault;
             }
         }
     }
 
     /**
      * invoke an endpoint from a get request by building an XML request and
      * handing it down. If anything goes wrong, we generate an XML formatted
      * axis fault
      * @param msgContext current message
      * @param response to return data
      * @param method method to invoke (may be null)
      * @param args argument list in XML form
      * @throws AxisFault iff something goes wrong when turning the response message
      * into a SOAP string.
      */
     protected void invokeEndpointFromGet(MessageContext msgContext,
                                        HttpServletResponse response,
                                        String method,
                                        String args) throws AxisFault, IOException {
         String body =
             "<" + method + ">" + args + "</" + method + ">";
 
         String msgtxt =
             "<SOAP-ENV:Envelope" +
             " xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
             "<SOAP-ENV:Body>" + body + "</SOAP-ENV:Body>" +
             "</SOAP-ENV:Envelope>";
 
         Message responseMsg=null;
         try {
             ByteArrayInputStream istream =
                 new ByteArrayInputStream(msgtxt.getBytes("ISO-8859-1"));
 
             AxisEngine engine = getEngine();
             Message msg = new Message(istream, false);
             msgContext.setRequestMessage(msg);
             engine.invoke(msgContext);
             responseMsg = msgContext.getResponseMessage();
             //turn off caching for GET requests
             response.setHeader("Cache-Control", "no-cache");
             response.setHeader("Pragma", "no-cache");
             if (responseMsg == null) {
                 //tell everyone that something is wrong
                 throw new Exception(Messages.getMessage("noResponse01"));
             }
         } catch (AxisFault fault) {
             processAxisFault(fault);
             configureResponseFromAxisFault(response, fault);
             if (responseMsg == null) {
                 responseMsg = new Message(fault);
             }
         } catch (Exception e) {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             responseMsg = convertExceptionToAxisFault(e,responseMsg);
         }
         //this call could throw an AxisFault. We delegate it up, because
         //if we cant write the message there is not a lot we can do in pure SOAP terms.
         response.setContentType("text/xml");
         PrintWriter writer=response.getWriter();
         writer.println(responseMsg.getSOAPPartAsString());
         writer.close();
     }
 
     /**
      * print a snippet of service info.
      * @param service service
      * @param writer output channel
      * @param serviceName where to put stuff
      */
 
     protected  void reportServiceInfo(HttpServletResponse response, SOAPService service, String serviceName) throws IOException {
         response.setContentType("text/html");
         PrintWriter writer=response.getWriter();
 
         writer.println("<h1>"
                 + service.getName()
                 +"</h1>");
         writer.println(
                 "<p>" +
                 Messages.getMessage("axisService00") +
                 "</p>");
         writer.println(
                 "<i>" +
                 Messages.getMessage("perhaps00") +
                 "</i>");
         writer.close();
     }
 
     /**
      * respond to the ?list command.
      * if enableList is set, we list the engine config. If it isnt, then an
      * error is written out
      * @param response
      * @param writer
      * @throws AxisFault
      */
     protected void processListRequest(HttpServletResponse response) throws AxisFault, IOException {
         AxisEngine engine = getEngine();
         if (enableList) {
             Document doc = Admin.listConfig(engine);
             if (doc != null) {
                 response.setContentType("text/xml");
                 PrintWriter writer=response.getWriter();
                 XMLUtils.DocumentToWriter(doc, writer);
                 writer.close();
             } else {
                 //error code is 404
                 response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                 response.setContentType("text/html");
                 PrintWriter writer=response.getWriter();
                 writer.println("<h2>" +
                                Messages.getMessage("error00") +
                                "</h2>");
                 writer.println("<p>" +
                                Messages.getMessage("noDeploy00") +
                                "</p>");
                 writer.close();
             }
         } else {
             // list not enable, return error
             //error code is, what, 401
             response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
             response.setContentType("text/html");
             PrintWriter writer=response.getWriter();
             writer.println("<h2>" +
                            Messages.getMessage("error00") +
                            "</h2>");
             writer.println("<p><i>?list</i> " +
                            Messages.getMessage("disabled00") +
                            "</p>");
             writer.close();
         }
     }
 
     /**
      * report that we have no WSDL
      * @param res
      * @param moreDetailCode optional name of a message to provide more detail
      * @param axisFault optional fault string, for extra info at debug time only
      */
     protected void reportNoWSDL(HttpServletResponse res,
                                 String moreDetailCode, AxisFault axisFault) throws IOException {
         res.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
         res.setContentType("text/html");
         PrintWriter writer=res.getWriter();
         writer.println("<h2>" +
                        Messages.getMessage("error00") +
                        "</h2>");
         writer.println("<p>" +
                        Messages.getMessage("noWSDL00") +
                        "</p>");
         if(moreDetailCode!=null) {
             writer.println("<p>"
                     +Messages.getMessage(moreDetailCode)
                     +"</p>");
         }
 
         if(axisFault!=null && isDevelopment()) {
             //dev systems only give fault dumps
             writeFault(writer, axisFault);
         }
         writer.close();
     }
 
 
     /**
      * This method lists the available services; it is called when there is
      * nothing to execute on a GET
      * @param response
      * @param request
      * @throws ConfigurationException
      * @throws AxisFault
      */
     protected void reportAvailableServices(HttpServletResponse response,
                                        HttpServletRequest request)
             throws  ConfigurationException, AxisFault, IOException {
         AxisEngine engine = getEngine();
         response.setContentType("text/html");
         PrintWriter writer=response.getWriter();
         writer.println("<h2>And now... Some Services</h2>");
 
         Iterator i;
         try {
             i = engine.getConfig().getDeployedServices();
         } catch (ConfigurationException configException) {
             //turn any internal configuration exceptions back into axis faults
             //if that is what they are
             if(configException.getContainedException() instanceof AxisFault) {
                 throw (AxisFault) configException.getContainedException();
             } else {
                 throw configException;
             }
         }
         String baseURL = getWebappBase(request)+"/services/";
         writer.println("<ul>");
         while (i.hasNext()) {
             ServiceDesc sd = (ServiceDesc)i.next();
             StringBuffer sb = new StringBuffer();
             sb.append("<li>");
             String name = sd.getName();
             sb.append(name);
             sb.append(" <a href=\"");
             sb.append(baseURL);
             sb.append(name);
             sb.append("?wsdl\"><i>(wsdl)</i></a></li>");
             writer.println(sb.toString());
             ArrayList operations = sd.getOperations();
             if (!operations.isEmpty()) {
                 writer.println("<ul>");
                 for (Iterator it = operations.iterator(); it.hasNext();) {
                     OperationDesc desc = (OperationDesc) it.next();
                     writer.println("<li>" + desc.getName());
                 }
                 writer.println("</ul>");
             }
         }
         writer.println("</ul>");
         writer.close();
     }
 
     /**
      * generate the error response to indicate that there is apparently no endpoint there
      * @param request the request that didnt have an edpoint
      * @param response response we are generating
      */
     protected void reportCantGetAxisService(HttpServletRequest request, HttpServletResponse response) throws IOException {
         // no such service....
         response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
         response.setContentType("text/html");
         PrintWriter writer=response.getWriter();
         writer.println("<h2>" +
                 Messages.getMessage("error00") + "</h2>");
         writer.println("<p>" +
                 Messages.getMessage("noService06") +
                 "</p>");
         writer.close();
     }
 
     /**
      * probe for a JWS page and report 'no service' if one is not found there
      * @param request the request that didnt have an edpoint
      * @param response response we are generating
      */
     protected void reportCantGetJWSService(HttpServletRequest request, HttpServletResponse response) throws IOException {
         //first look to see if there is a service
         String realpath =
                 getServletConfig().getServletContext()
                 .getRealPath(request.getServletPath());
         boolean foundJWSFile=(new File(realpath).exists()) &&
                 (realpath.endsWith(Constants.JWS_DEFAULT_FILE_EXTENSION));
         response.setContentType("text/html");
         PrintWriter writer=response.getWriter();
         if(foundJWSFile) {
             response.setStatus(HttpURLConnection.HTTP_OK);
             writer.println(Messages.getMessage("foundJWS00") + "<p>");
             String url = request.getRequestURI();
             String urltext = Messages.getMessage("foundJWS01");
             writer.println("<a href='"+url+"?wsdl'>"+urltext+"</a>");
         } else {
             response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
             writer.println(Messages.getMessage("noService06") );
         }
         writer.close();
     }
 
 
     /**
      * Process a POST to the servlet by handing it off to the Axis Engine.
      * Here is where SOAP messages are received
      * @param req posted request
      * @param res respose
      * @throws ServletException trouble
      * @throws IOException different trouble
      */
      @Override
     public void doPost(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException
     {
         long t0=0, t1=0, t2=0, t3=0, t4=0;
         String soapAction=null;
         MessageContext msgContext=null;
         if (isDebug)
             log.debug("Enter: doPost()");
         if( tlog.isDebugEnabled() ) {
             t0=System.currentTimeMillis();
         }
 
         Message responseMsg = null;
         String  contentType = null;
 
         try {
             AxisEngine engine = getEngine();
 
             if (engine == null) {
                 // !!! should return a SOAP fault...
                 ServletException se =
                     new ServletException(Messages.getMessage("noEngine00"));
                 log.debug("No Engine!", se);
                 throw se;
             }
 
             res.setBufferSize(1024 * 8); // provide performance boost.
 
             /** get message context w/ various properties set
              */
             msgContext = createMessageContext(engine, req, res);
 
             // ? OK to move this to 'getMessageContext',
             // ? where it would also be picked up for 'doGet()' ?
             if (securityProvider != null) {
                 if (isDebug) log.debug("securityProvider:" + securityProvider);
                 msgContext.setProperty(MessageContext.SECURITY_PROVIDER, securityProvider);
             }
 
             /* Get request message
              */
             Message requestMsg =
                 new Message(req.getInputStream(),
                             false,
                             req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE),
                             req.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION));
 
             if(log.isInfoEnabled()) log.info("Request Message:" + requestMsg);
 
             /* Set the request(incoming) message field in the context */
             /**********************************************************/
             msgContext.setRequestMessage(requestMsg);
             //String url = HttpUtils.getRequestURL(req).toString();
             String url = req.getRequestURL().toString();
             msgContext.setProperty(MessageContext.TRANS_URL, url);
 
             try {
                 /**
                  * Save the SOAPAction header in the MessageContext bag.
                  * This will be used to tell the Axis Engine which service
                  * is being invoked.  This will save us the trouble of
                  * having to parse the Request message - although we will
                  * need to double-check later on that the SOAPAction header
                  * does in fact match the URI in the body.
                  */
                 // (is this last stmt true??? (I don't think so - Glen))
                 /********************************************************/
                 soapAction = getSoapAction(req);
 
                 if (soapAction != null) {
                     msgContext.setUseSOAPAction(true);
                     msgContext.setSOAPActionURI(soapAction);
                 }
 
                 // Create a Session wrapper for the HTTP session.
                 // These can/should be pooled at some point.
                 // (Sam is Watching! :-)
                 msgContext.setSession(new AxisHttpSession(req));
 
                 if( tlog.isDebugEnabled() ) {
                     t1=System.currentTimeMillis();
                 }
 
                 srwInfo.setSRWStuff(req, res, msgContext);
                 
                 /* Invoke the Axis engine... */
                 /*****************************/
                 if(isDebug) log.debug("Invoking Axis Engine.");
                 //here we run the message by the engine
                 engine.invoke(msgContext);
                 if(isDebug) log.debug("Return from Axis Engine.");
                 if( tlog.isDebugEnabled() ) {
                     t2=System.currentTimeMillis();
                 }
                 responseMsg = msgContext.getResponseMessage();
                 if (responseMsg == null) {
                     //tell everyone that something is wrong
                     throw new Exception(Messages.getMessage("noResponse01"));
                 }
             } catch (AxisFault fault) {
                 //log and sanitize
                 processAxisFault(fault);
                 configureResponseFromAxisFault(res,fault);
                 responseMsg = msgContext.getResponseMessage();
                 if (responseMsg == null) {
                     responseMsg = new Message(fault);
                 }
             } catch (Exception e) {
                 //other exceptions are internal trouble
                 responseMsg = msgContext.getResponseMessage();
                 res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 responseMsg = convertExceptionToAxisFault(e, responseMsg);
             }
         } catch (AxisFault fault) {
             processAxisFault(fault);
             configureResponseFromAxisFault(res, fault);
             responseMsg = msgContext.getResponseMessage();
             if (responseMsg == null) {
                 responseMsg = new Message(fault);
             }
         }
         //determine content type from message response
         contentType = responseMsg.getContentType(msgContext.getSOAPConstants());
         if( tlog.isDebugEnabled() ) {
             t3=System.currentTimeMillis();
         }
 
         /* Send response back along the wire...  */
         /***********************************/
         if (responseMsg != null) {
             sendResponse(getProtocolVersion(req), contentType,
                          res, responseMsg);
         }
 
         if (isDebug) {
             log.debug("Response sent.");
             log.debug("Exit: doPost()");
         }
         if( tlog.isDebugEnabled() ) {
             t4=System.currentTimeMillis();
             tlog.debug("axisServlet.doPost: " + soapAction +
                        " pre=" + (t1-t0) +
                        " invoke=" + (t2-t1) +
                        " post=" + (t3-t2) +
                        " send=" + (t4-t3) +
                        " " + msgContext.getTargetService() + "." +
                         ((msgContext.getOperation( ) == null) ?
                         "" : msgContext.getOperation().getName()) );
         }
 
     }
 
     /**
      * Configure the servlet response status code and maybe other headers
      * from the fault info.
      * @param response response to configure
      * @param fault what went wrong
      */
     private void configureResponseFromAxisFault(HttpServletResponse response,
                                                 AxisFault fault) {
         // then get the status code
         // It's been suggested that a lack of SOAPAction
         // should produce some other error code (in the 400s)...
         int status = getHttpServletResponseStatus(fault);
         if (status == HttpServletResponse.SC_UNAUTHORIZED) {
             // unauth access results in authentication request
             // TODO: less generic realm choice?
           response.setHeader("WWW-Authenticate","Basic realm=\"AXIS\"");
         }
         response.setStatus(status);
     }
 
     /**
  * turn any Exception into an AxisFault, log it, set the response
  * status code according to what the specifications say and
  * return a response message for posting. This will be the response
  * message passed in if non-null; one generated from the fault otherwise.
  *
  * @param exception what went wrong
  * @param responseMsg what response we have (if any)
  * @return a response message to send to the user
  */
     private Message convertExceptionToAxisFault(Exception exception,
                                                 Message responseMsg) {
         logException(exception);
         if (responseMsg == null) {
             AxisFault fault=AxisFault.makeFault(exception);
             processAxisFault(fault);
             responseMsg = new Message(fault);
         }
         return responseMsg;
     }
 
     /**
      * Extract information from AxisFault and map it to a HTTP Status code.
      *
      * @param af Axis Fault
      * @return HTTP Status code.
      */
     @Override
     protected int getHttpServletResponseStatus(AxisFault af) {
         // TODO: Should really be doing this with explicit AxisFault
         // subclasses... --Glen
                 return af.getFaultCode().getLocalPart().startsWith("Server.Unauth")
                          ? HttpServletResponse.SC_UNAUTHORIZED
                          : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                // This will raise a 401 for both
                // "Unauthenticated" & "Unauthorized"...
     }
 
     /**
      * write a message to the response, set appropriate headers for content
      * type..etc.
      * @param clientVersion client protocol, one of the HTTPConstants strings
      * @param res   response
      * @param responseMsg message to write
      * @throws AxisFault
      * @throws IOException if the response stream can not be written to
      */
     private void sendResponse(final String clientVersion, 
             String contentType,
             HttpServletResponse res, Message responseMsg)
         throws AxisFault, IOException
     {
         if (responseMsg == null) {
             res.setStatus(HttpServletResponse.SC_NO_CONTENT);
             if(isDebug) log.debug("NO AXIS MESSAGE TO RETURN!");
             //String resp = Messages.getMessage("noData00");
             //res.setContentLength((int) resp.getBytes().length);
             //res.getWriter().print(resp);
         } else {
             if(isDebug) {
                 log.debug("Returned Content-Type:" +
                     contentType);
                 // log.debug("Returned Content-Length:" +
                 //          responseMsg.getContentLength());
             }
 
             try {
                 res.setContentType(contentType);
 
                 /* My understand of Content-Length
                  * HTTP 1.0
                  *   -Required for requests, but optional for responses.
                  * HTTP 1.1
                  *  - Either Content-Length or HTTP Chunking is required.
                  *   Most servlet engines will do chunking if content-length is not specified.
                  *
                  *
                  */
 
                 //if(clientVersion == HTTPConstants.HEADER_PROTOCOL_V10) //do chunking if necessary.
                 //     res.setContentLength(responseMsg.getContentLength());
 
                 responseMsg.writeTo(res.getOutputStream());
             } catch (SOAPException e){
                 logException(e);
             }
         }
 
         if (!res.isCommitted()) {
             res.flushBuffer(); // Force it right now.
         }
     }
 
     /**
      * Place the Request message in the MessagContext object - notice
      * that we just leave it as a 'ServletRequest' object and let the
      * Message processing routine convert it - we don't do it since we
      * don't know how it's going to be used - perhaps it might not
      * even need to be parsed.
      * @return a message context
      */
     private MessageContext createMessageContext(AxisEngine engine,
                                                 HttpServletRequest req,
                                                 HttpServletResponse res)
     {
         MessageContext msgContext = new MessageContext(engine);
 
         if(isDebug) {
             log.debug("MessageContext:" + msgContext);
             log.debug("HEADER_CONTENT_TYPE:" +
                       req.getHeader( HTTPConstants.HEADER_CONTENT_TYPE));
             log.debug("HEADER_CONTENT_LOCATION:" +
                       req.getHeader( HTTPConstants.HEADER_CONTENT_LOCATION));
             log.debug("Constants.MC_HOME_DIR:" + String.valueOf(getHomeDir()));
             log.debug("Constants.MC_RELATIVE_PATH:"+req.getServletPath());
             
             log.debug("HTTPConstants.MC_HTTP_SERVLETLOCATION:"+ String.valueOf(getWebInfPath()));
             log.debug("HTTPConstants.MC_HTTP_SERVLETPATHINFO:" +
                       req.getPathInfo() );
             log.debug("HTTPConstants.HEADER_AUTHORIZATION:" +
                       req.getHeader(HTTPConstants.HEADER_AUTHORIZATION));
             log.debug("Constants.MC_REMOTE_ADDR:"+req.getRemoteAddr());
             log.debug("configPath:" + String.valueOf(getWebInfPath()));
         }
 
         /* Set the Transport */
         /*********************/
         msgContext.setTransportName(transportName);
 
         /* Save some HTTP specific info in the bag in case someone needs it */
         /********************************************************************/
         msgContext.setProperty(Constants.MC_JWS_CLASSDIR, jwsClassDir);
         msgContext.setProperty(Constants.MC_HOME_DIR, getHomeDir());
         msgContext.setProperty(Constants.MC_RELATIVE_PATH,
                                req.getServletPath());
         msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLET, this );
         msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, req );
         msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, res );
         msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETLOCATION,
                                getWebInfPath() );
         msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO,
                                req.getPathInfo() );
         msgContext.setProperty(HTTPConstants.HEADER_AUTHORIZATION,
                                req.getHeader(HTTPConstants.HEADER_AUTHORIZATION));
         msgContext.setProperty(Constants.MC_REMOTE_ADDR, req.getRemoteAddr());
 
         // Set up a javax.xml.rpc.server.ServletEndpointContext
         ServletEndpointContextImpl sec = new ServletEndpointContextImpl();
 
         msgContext.setProperty(Constants.MC_SERVLET_ENDPOINT_CONTEXT, sec);
         /* Save the real path */
         /**********************/
         String realpath =
             getServletConfig().getServletContext()
             .getRealPath(req.getServletPath());
 
         if (realpath != null) {
             msgContext.setProperty(Constants.MC_REALPATH, realpath);
         }
 
         msgContext.setProperty(Constants.MC_CONFIGPATH, getWebInfPath());
 
         return msgContext;
     }
 
     /**
      * Extract the SOAPAction header.
      * if SOAPAction is null then we'll we be forced to scan the body for it.
      * if SOAPAction is "" then use the URL
      * @param req incoming request
      * @return the action
      * @throws AxisFault
      */
     private String getSoapAction(HttpServletRequest req)
         throws AxisFault
     {
         String soapAction =req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
 
         if(isDebug) log.debug("HEADER_SOAP_ACTION:" + soapAction);
 
         /**
          * Technically, if we don't find this header, we should probably fault.
          * It's required in the SOAP HTTP binding.
          */
         if (soapAction == null) {
             //MIH: soapAction is not required
             soapAction="";
 //            AxisFault af = new AxisFault("Client.NoSOAPAction",
 //                                         Messages.getMessage("noHeader00",
 //                                                              "SOAPAction"),
 //                                         null, null);
 //
 //            exceptionLog.error(Messages.getMessage("genFault00"), af);
 //
 //            throw af;
         }
 
         if (soapAction.length()==0)
             soapAction = req.getContextPath(); // Is this right?
 
         return soapAction;
     }
 
     /**
      * Provided to allow overload of default JWSClassDir
      * by derived class.
      * @return directory for JWS files
      */
     @Override
     protected String getDefaultJWSClassDir() {
         return (getWebInfPath() == null)
                ? null  // ??? what is a good FINAL default for WebLogic?
                : getWebInfPath() + File.separator +  "jwsClasses";
     }
 
     /**
      * Return the HTTP protocol level 1.1 or 1.0
      * by derived class.
      * @return one of the HTTPConstants values
      */
     protected String getProtocolVersion(HttpServletRequest req){
         String ret= HTTPConstants.HEADER_PROTOCOL_V10;
         String prot= req.getProtocol();
         if(prot!= null){
             int sindex= prot.indexOf('/');
             if(-1 != sindex){
                 String ver= prot.substring(sindex+1);
                 if(HTTPConstants.HEADER_PROTOCOL_V11.equals(ver.trim())){
                     ret= HTTPConstants.HEADER_PROTOCOL_V11;
                 }
             }
         }
         return ret;
     }
 
 
     static Runtime rt=Runtime.getRuntime();
     protected void processMethodRequest(
       org.apache.axis.MessageContext msgContext, HttpServletRequest req,
       HttpServletResponse resp)
       throws org.apache.axis.AxisFault, IOException {
         long startTime=System.currentTimeMillis();
         log.debug("enter processMethodRequest");
 //        log.info("at start: totalMemory="+rt.totalMemory()+", freeMemory="+rt.freeMemory());
         log.info(req.getQueryString());
         if(!srwInfo.setSRWStuff(req, resp, msgContext)) {
             log.error("srwInfo.setSRWStuff failed!");
             return;
         }
         String operation=req.getParameter("operation"),
                query=req.getParameter("query"),
                scanClause=req.getParameter("scanClause");
         log.debug("in processMethodRequest: operation="+operation);
         if(query!=null) {
             log.debug("in processMethodRequest: query:\n"+Utilities.byteArrayToString(query.getBytes("UTF8")));
         }
         if(scanClause!=null)
             log.debug("in processMethodRequest: scanClause:\n"+Utilities.byteArrayToString(scanClause.getBytes("UTF8")));
         
         if((operation!=null && operation.equals("searchRetrieve")) || query!=null) { // searchRetrieveRequest
             int          i;
             StringBuffer sb=new StringBuffer();
 
             // utf-8 bytes seem to have been incorrectly loaded as characters
             // lets load them back into bytes and then rebuild the string
             //MIH: avoid NullPointerException
             if (query != null) {
                 byte[] qb=new byte[query.length()];
                 for(i=0; i<query.length(); i++)
                     qb[i]=(byte)query.charAt(i);
                 query=new String(qb, "utf-8");
             }
 
             sb.append("<soap:Envelope ")
               .append("xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
               .append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ")
               .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
               .append("<soap:Body>")
               .append("<srw:searchRetrieveRequest ")
               .append("xmlns:srw=\"http://www.loc.gov/zing/srw/\">")
               .append("<srw:query>").append(encode(query)).append("</srw:query>");
 
             Enumeration parms=req.getParameterNames();
             String extension, namespace, parm, t;
             while(parms.hasMoreElements()) {
                 parm=(String)parms.nextElement();
                 if(parm.equals("sortKeys")) {
                     t=req.getParameter(parm);
                     if(t!=null)
                         sb.append("<srw:sortKeys>").append(t).append("</srw:sortKeys>");
                 }
                 else if(parm.equals("startRecord")) {
                     t=req.getParameter(parm);
                     if(t!=null) {
                         try {
                             i=Integer.parseInt(t);
                             if(i<1)
                                 i=Integer.MAX_VALUE;
                         }
                         catch(NumberFormatException e){
                             i=Integer.MAX_VALUE;
                         }
                         sb.append("<srw:startRecord>").append(i).append("</srw:startRecord>");
                     }
                 }
                 else if(parm.equals("maximumRecords")) {
                     t=req.getParameter(parm);
                     if(t!=null) {
                         try {
                             i=Integer.parseInt(t);
                             if(i<0)
                                 i=Integer.MAX_VALUE;
                         }
                         catch(NumberFormatException e){
                             i=Integer.MAX_VALUE;
                         }
 
                         sb.append("<srw:maximumRecords>").append(i).append("</srw:maximumRecords>");
                     } 
                 }
                 else if(parm.equals("recordSchema")) {
                     t=req.getParameter(parm);
                     if(t!=null)
                         sb.append("<srw:recordSchema>").append(t).append("</srw:recordSchema>");
                 }
                 else if(parm.equals("recordPacking")) {
                     t=req.getParameter(parm);
                     if(t!=null)
                         sb.append("<srw:recordPacking>").append(t).append("</srw:recordPacking>");
                 }
                 else if(parm.equals("recordPacking")) {
                     t=req.getParameter(parm);
                     if(t!=null) {
                         try {
                             i=Integer.parseInt(t);
                             if(i<0)
                                 i=Integer.MAX_VALUE;
                         }
                         catch(NumberFormatException e){
                             i=Integer.MAX_VALUE;
                         }
 
                         sb.append("<srw:resultSetTTL>").append(i).append("</srw:resultSetTTL>");
                     }
                 }
             }
             parms=req.getParameterNames(); // walk through them again
             boolean hasExtraRequestData=false;
             while(parms.hasMoreElements()) {
                 parm=(String)parms.nextElement();
                 extension=srwInfo.getExtension(parm);
                 if(extension!=null) {
                    if(!hasExtraRequestData) {
                         sb.append("<srw:extraRequestData>");
                        hasExtraRequestData=true;
                    }
                     namespace=srwInfo.getNamespace(parm);
                     sb.append("    <").append(extension).append(" xmlns=\"").append(namespace).append("\"");
                     t=req.getParameter(parm);
                     if(t!=null && t.length()>0)
                         sb.append(">").append(t).append("</").append(extension).append(">");
                     else
                         sb.append("/>");
                 }
             }
            if(hasExtraRequestData) {
                sb.append("    </srw:extraRequestData>");
            }

             
             
             sb.append("</srw:searchRetrieveRequest></soap:Body></soap:Envelope>");
             if(log.isDebugEnabled()) {
                 log.debug("request="+sb.toString());
                 log.debug(Utilities.byteArrayToString(sb.toString().getBytes("UTF-8")));
             }
             msgContext.setProperty("sru", "");
             AxisEngine engine=getEngine();
             ByteArrayInputStream bais=new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
             Message msg=new Message(bais, false);
             msgContext.setRequestMessage(msg);
             try {
                 engine.invoke(msgContext);
             }
             catch(Exception e) {
                 log.error(e, e);
             }
             Message respMsg=msgContext.getResponseMessage();
             if(respMsg!=null) {
                 //resp.setContentType("text/xml; charset=UTF-8");
                 resp.setContentType("text/xml");
                 //PrintWriter writer=resp.getWriter();
                 javax.servlet.ServletOutputStream sos=resp.getOutputStream();
                 // code to strip SOAP stuff out.  Hope this can go away some day
                 String soapResponse=respMsg.getSOAPPartAsString();
                 int start=soapResponse.indexOf("<searchRetrieveResponse");
                 if(start>=0) {
                     int stop=soapResponse.indexOf("</searchRetrieveResponse>");
                     soapResponse=cleanup(soapResponse.substring(start, stop+25)
                         .toCharArray());                    
                     SRWDatabase db=(SRWDatabase)msgContext.getProperty("db");
                     //srwInfo.writeXmlHeader(writer, msgContext, req,
                     //    db.searchStyleSheet);
                     srwInfo.writeXmlHeader(sos, msgContext, req,
                         db.searchStyleSheet);
                 }
                 //writer.println(soapResponse);
                 //writer.close();
                 sos.write(soapResponse.getBytes("utf-8"));
                 sos.close();
             }
             else {
                 resp.setContentType("text/html");
                 PrintWriter writer=resp.getWriter();
                 writer.println("<p>"+Messages.getMessage("noResponse01")+"</p>");
                 log.error("request generated no response!");
                 writer.close();
             }
 //            log.info("at exit: totalMemory="+rt.totalMemory()+", freeMemory="+rt.freeMemory());
             log.debug("exit processMethodRequest");
             return;
         }
 
         if((operation!=null && operation.equals("scan")) || scanClause!=null) { // scanRequest
             int          i;
             String       t;
             StringBuffer sb=new StringBuffer();
             sb.append("<soap:Envelope ")
               .append("xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
               .append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ")
               .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
               .append("<soap:Body>")
               .append("<srw:scanRequest ")
               .append("xmlns:srw=\"http://www.loc.gov/zing/srw/\">");
             if(scanClause!=null) {
                 // utf-8 bytes seem to have been incorrectly loaded as characters
                 // lets load them back into bytes and then rebuild the string
                 byte[] scb=new byte[scanClause.length()];
                 for(i=0; i<scanClause.length(); i++) {
                     scb[i]=(byte)scanClause.charAt(i);
                 }
                 scanClause=new String(scb, "utf-8");
                 sb.append("<srw:scanClause>").append(scanClause).append("</srw:scanClause>");
             }
             
             t=req.getParameter("responsePosition");
             if(t!=null) {
                 try {
                     i=Integer.parseInt(t);
                     if(i<0)
                         i=Integer.MAX_VALUE;
                 }
                 catch(NumberFormatException e){
                     i=Integer.MAX_VALUE;
                 }
                     
                 sb.append("<srw:responsePosition>").append(i).append("</srw:responsePosition>");
             }
 
             t=req.getParameter("maximumTerms");
             if(t!=null) {
                 try {
                     i=Integer.parseInt(t);
                     if(i<1)
                         i=Integer.MAX_VALUE;
                 }
                 catch(NumberFormatException e){
                     i=Integer.MAX_VALUE;
                 }
                     
                 sb.append("<srw:maximumTerms>").append(i).append("</srw:maximumTerms>");
             }
             
             sb.append("</srw:scanRequest></soap:Body></soap:Envelope>");
             if(log.isDebugEnabled())
                 log.debug(sb.toString());
             msgContext.setProperty("sru", "");
             AxisEngine engine=getEngine();
             if(log.isDebugEnabled())
                 log.debug("request="+sb.toString());
             ByteArrayInputStream bais=new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
             Message msg=new Message(bais, false);
             msgContext.setRequestMessage(msg);
             try {
                 engine.invoke(msgContext);
             }
             catch(Exception e) {
                 log.error(e, e);
             }
             Message respMsg=msgContext.getResponseMessage();
             if(respMsg!=null) {
                 resp.setContentType("text/xml");
                 PrintWriter writer=resp.getWriter();
                 // code to strip SOAP stuff out.  Hope this can go away some day
                 String soapResponse=respMsg.getSOAPPartAsString();
                 int start=soapResponse.indexOf("<scanResponse");
                 if(start>=0) {
                     int stop=soapResponse.indexOf("</scanResponse>");
                     soapResponse=cleanup(soapResponse.substring(start, stop+15)
                         .toCharArray());
                     SRWDatabase db=(SRWDatabase)msgContext.getProperty("db");
                     srwInfo.writeXmlHeader(writer, msgContext, req,
                         db.scanStyleSheet);
                 }
                 writer.println(soapResponse);
                 writer.close();
             }
             else {
                 resp.setContentType("text/html");
                 PrintWriter writer=resp.getWriter();
                 writer.println("<p>"+Messages.getMessage("noResponse01")+"</p>");
                 log.error("request generated no response!");
                 writer.close();
             }
             log.error("elapsed time: "+(System.currentTimeMillis()-startTime)+"ms");
             log.debug("exit processMethodRequest");
             return;
         }
     }
 
 
     private static String cleanup(char[] buf) {
         int i, j, len=buf.length;
         boolean inRecordData = false;
         boolean startRecordData = false;
         //len=0;
         for(i=0; i<len; i++) {
             if(!inRecordData && buf[i]==' ' && len-i>9) // might be " xsi:type"
                 if(buf[i+1]=='x' && buf[i+2]=='s' && buf[i+3]=='i' &&
                   buf[i+4]==':' && buf[i+5]=='t' && buf[i+6]=='y' &&
                   buf[i+7]=='p' && buf[i+8]=='e') {
                     boolean foundQuote=false;
                     for(j=i+5; j<len; j++)
                         if(buf[j]=='"')
                             if(foundQuote)
                                 break;
                             else
                                 foundQuote=true;
                     if(j==len) // never found matching quotes, so ignore
                         continue;
                     // remove offending chars
                     //log.info("i="+i+", j="+j+", len="+len);
                     System.arraycopy(buf, j+1, buf, i, len-j-1);
                     len-=(j-i)+1;
                     i--;
                     continue;
                 }
 
             if(buf[i]=='<' && len-1>11) {
                 // might be "<recordData>"
                 if(buf[i+1]=='r' && buf[i+2]=='e' && buf[i+3]=='c' &&
                         buf[i+4]=='o' && buf[i+5]=='r' && buf[i+6]=='d' &&
                         buf[i+7]=='D' && buf[i+8]=='a' && buf[i+9]=='t' &&
                         buf[i+10]=='a') {
                           startRecordData = true;
                       }
             }
             if(startRecordData && buf[i]=='>') {
                 startRecordData = false;
                 inRecordData = true;
             }
             if(startRecordData && buf[i]=='/') {
                 startRecordData = false;
                 inRecordData = false;
             }
             if(inRecordData && buf[i]=='<' && len-1>12) {
                 // might be "</recordData>"
                 if(buf[i+1]=='/' && buf[i+2]=='r' && buf[i+3]=='e' && buf[i+4]=='c' &&
                         buf[i+5]=='o' && buf[i+6]=='r' && buf[i+7]=='d' &&
                         buf[i+8]=='D' && buf[i+9]=='a' && buf[i+10]=='t' &&
                         buf[i+11]=='a') {
                           inRecordData = false;
                       }
             }
 
         }
         return new String(buf, 0, len);
     }
 
     static String encode(String s) {
         StringBuffer sb=new StringBuffer();
         //MIH avoid NullPointerException
         if (s != null) {
             char c, chars[]=s.toCharArray();
             for(int i=0; i<chars.length; i++) {
                 c=chars[i];
                 if(c==' ' || c=='<' || c=='&' || c=='>' || c=='"' || c=='\'')
                     sb.append("&#").append(Integer.toString(c)).append(';');
                 else
                     sb.append(c);
             }
         }
         return sb.toString();
     }
     
     private String getWsdl() {
         return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
         + "<wsdl:definitions "
         + " targetNamespace=\"http://www.loc.gov/zing/srw/service/\" "
         + " xmlns:apachesoap=\"http://xml.apache.org/xml-soap\" "
         + " xmlns:impl=\"http://www.loc.gov/zing/srw/service/\" "
         + " xmlns:intf=\"http://www.loc.gov/zing/srw/service/\" "
         + " xmlns:tns1=\"http://www.loc.gov/zing/srw/\" "
         + " xmlns:tns2=\"http://srw.zing.www.loc.gov\" "
         + " xmlns:tns3=\"http://www.loc.gov/zing/cql/xcql/\" "
         + " xmlns:tns4=\"http://www.loc.gov/zing/srw/diagnostic/\" "
         + " xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" "
         + " xmlns:wsdlsoap=\"http://schemas.xmlsoap.org/wsdl/soap/\" "
         + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
         + " <!--WSDL created by Apache Axis version: 1.4 Built on Dec 21, 2006 (08:43:29 CET)-->"
         + " <wsdl:types>"
         + "     <schema elementFormDefault=\"qualified\" targetNamespace=\"http://srw.zing.www.loc.gov\" "
         + "         xmlns=\"http://www.w3.org/2001/XMLSchema\">"
         + "         <import namespace=\"http://www.loc.gov/zing/cql/xcql/\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/srw/diagnostic/\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/srw/\"/>"
         + "         <complexType name=\"RequestType\">"
         + "             <sequence>"
         + "                 <element name=\"version\" type=\"xsd:string\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"ResponseType\">"
         + "             <sequence>"
         + "                 <element name=\"version\" type=\"xsd:string\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <simpleType name=\"TermTypeWhereInList\">"
         + "             <restriction base=\"xsd:string\">"
         + "             <enumeration value=\"first\"/>"
         + "             <enumeration value=\"last\"/>"
         + "             <enumeration value=\"only\"/>"
         + "             <enumeration value=\"inner\"/>"
         + "             </restriction>"
         + "         </simpleType>"
         + "     </schema>"
         + "     <schema elementFormDefault=\"qualified\" "
         + "         targetNamespace=\"http://www.loc.gov/zing/srw/\" "
         + "         xmlns=\"http://www.w3.org/2001/XMLSchema\">"
         + "         <import namespace=\"http://srw.zing.www.loc.gov\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/cql/xcql/\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/srw/diagnostic/\"/>"
         + "         <import namespace=\"http://schemas.xmlsoap.org/soap/encoding/\"/>"
         + "         <complexType name=\"extraRequestData\">"
         + "             <sequence>"
         + "                 <any/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"searchRetrieveRequestType\">"
         + "             <complexContent>"
         + "                 <extension base=\"tns2:RequestType\">"
         + "                     <sequence>"
         + "                         <element name=\"query\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"startRecord\" type=\"xsd:positiveInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"maximumRecords\" type=\"xsd:nonNegativeInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordPacking\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordSchema\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordXPath\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"resultSetTTL\" type=\"xsd:nonNegativeInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"sortKeys\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"stylesheet\" type=\"xsd:anyURI\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraRequestData\" type=\"tns1:extraRequestData\"/>"
         + "                     </sequence>"
         + "                 </extension>"
         + "             </complexContent>"
         + "         </complexType>"
         + "         <element name=\"searchRetrieveRequest\" type=\"tns1:searchRetrieveRequestType\"/>"
         + "         <complexType name=\"stringOrXmlFragment\">"
         + "             <sequence>"
         + "                 <any/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"extraDataType\">"
         + "             <sequence>"
         + "                 <any/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"recordType\">"
         + "             <sequence>"
         + "                 <element name=\"recordSchema\" type=\"xsd:string\"/>"
         + "                 <element name=\"recordPacking\" type=\"xsd:string\"/>"
         + "                 <element name=\"recordData\" type=\"tns1:stringOrXmlFragment\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordPosition\" type=\"xsd:positiveInteger\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraRecordData\" type=\"tns1:extraDataType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"recordsType\">"
         + "             <sequence>"
         + "                 <element maxOccurs=\"unbounded\" name=\"record\" type=\"tns1:recordType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"sortKeyType\">"
         + "             <sequence>"
         + "                 <element name=\"path\" type=\"xsd:string\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"schema\" type=\"xsd:string\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"ascending\" type=\"xsd:boolean\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"caseSensitive\" type=\"xsd:boolean\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"missingValue\" type=\"xsd:string\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"xSortKeysType\">"
         + "             <sequence>"
         + "                 <element maxOccurs=\"unbounded\" name=\"sortKey\" type=\"tns1:sortKeyType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"echoedSearchRetrieveRequestType\">"
         + "             <complexContent>"
         + "                 <extension base=\"tns2:RequestType\">"
         + "                     <sequence>"
         + "                         <element name=\"query\" type=\"xsd:string\"/>"
         + "                         <element name=\"xQuery\" nillable=\"true\" type=\"tns3:operandType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"startRecord\" type=\"xsd:positiveInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"maximumRecords\" type=\"xsd:nonNegativeInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordPacking\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordSchema\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordXPath\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"resultSetTTL\" type=\"xsd:nonNegativeInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"sortKeys\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"xSortKeys\" nillable=\"true\" type=\"tns1:xSortKeysType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"stylesheet\" type=\"xsd:anyURI\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraRequestData\" type=\"tns1:extraDataType\"/>"
         + "                     </sequence>"
         + "                 </extension>"
         + "             </complexContent>"
         + "         </complexType>"
         + "         <complexType name=\"diagnosticsType\">"
         + "             <sequence>"
         + "                 <element maxOccurs=\"unbounded\" name=\"diagnostic\" type=\"tns4:diagnosticType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"searchRetrieveResponseType\">"
         + "             <complexContent>"
         + "                 <extension base=\"tns2:ResponseType\">"
         + "                     <sequence>"
         + "                         <element name=\"numberOfRecords\" type=\"xsd:nonNegativeInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"resultSetId\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"resultSetIdleTime\" type=\"xsd:positiveInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"records\" type=\"tns1:recordsType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"nextRecordPosition\" type=\"xsd:positiveInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"echoedSearchRetrieveRequest\" type=\"tns1:echoedSearchRetrieveRequestType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"diagnostics\" type=\"tns1:diagnosticsType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraResponseData\" type=\"tns1:extraDataType\"/>"
         + "                     </sequence>"
         + "                 </extension>"
         + "             </complexContent>"
         + "         </complexType>"
         + "         <element name=\"searchRetrieveResponse\" type=\"tns1:searchRetrieveResponseType\"/>"
         + "         <complexType name=\"scanRequestType\">"
         + "             <complexContent>"
         + "                 <extension base=\"tns2:RequestType\">"
         + "                     <sequence>"
         + "                         <element name=\"scanClause\" type=\"xsd:string\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"responsePosition\" type=\"xsd:nonNegativeInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"maximumTerms\" type=\"xsd:positiveInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"stylesheet\" type=\"xsd:anyURI\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraRequestData\" type=\"tns1:extraDataType\"/>"
         + "                     </sequence>"
         + "                 </extension>"
         + "             </complexContent>"
         + "         </complexType>"
         + "         <element name=\"scanRequest\" type=\"tns1:scanRequestType\"/>"
         + "         <complexType name=\"termType\">"
         + "             <sequence>"
         + "                 <element name=\"value\" type=\"xsd:string\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"numberOfRecords\" type=\"xsd:nonNegativeInteger\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"displayTerm\" type=\"xsd:string\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"whereInList\" nillable=\"true\" type=\"tns2:TermTypeWhereInList\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraTermData\" type=\"tns1:extraDataType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"termsType\">"
         + "             <sequence>"
         + "                 <element maxOccurs=\"unbounded\" name=\"term\" type=\"tns1:termType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"echoedScanRequestType\">"
         + "             <complexContent>"
         + "                 <extension base=\"tns2:RequestType\">"
         + "                     <sequence>"
         + "                         <element name=\"scanClause\" type=\"xsd:string\"/>"
         + "                         <element name=\"xScanClause\" nillable=\"true\" type=\"tns3:searchClauseType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"responsePosition\" type=\"xsd:nonNegativeInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"maximumTerms\" type=\"xsd:positiveInteger\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"stylesheet\" type=\"xsd:anyURI\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraRequestData\" type=\"tns1:extraDataType\"/>"
         + "                     </sequence>"
         + "                 </extension>"
         + "             </complexContent>"
         + "         </complexType>"
         + "         <complexType name=\"scanResponseType\">"
         + "             <complexContent>"
         + "                 <extension base=\"tns2:ResponseType\">"
         + "                     <sequence>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"terms\" type=\"tns1:termsType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"echoedScanRequest\" type=\"tns1:echoedScanRequestType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"diagnostics\" type=\"tns1:diagnosticsType\"/>"
         + "                         <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraResponseData\" type=\"tns1:extraDataType\"/>"
         + "                     </sequence>"
         + "                 </extension>"
         + "             </complexContent>"
         + "         </complexType>"
         + "         <element name=\"scanResponse\" type=\"tns1:scanResponseType\"/>"
         + "         <complexType name=\"ExplainRequestType\">"
         + "          <complexContent>"
         + "           <extension base=\"tns2:RequestType\">"
         + "            <sequence>"
         + "             <element maxOccurs=\"1\" minOccurs=\"0\" name=\"recordPacking\" type=\"xsd:string\"/>"
         + "             <element maxOccurs=\"1\" minOccurs=\"0\" name=\"stylesheet\" type=\"xsd:anyURI\"/>"
         + "             <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraRequestData\" type=\"tns1:extraRequestData\"/>"
         + "            </sequence>"
         + "           </extension>"
         + "          </complexContent>"
         + "         </complexType>"
         + "         <element name=\"explainRequest\" type=\"tns1:ExplainRequestType\"/>"
         + "         <complexType name=\"ExplainResponseType\">"
         + "          <complexContent>"
         + "           <extension base=\"tns2:ResponseType\">"
         + "            <sequence>"
         + "             <element name=\"record\" type=\"tns1:recordType\"/>"
         + "             <element maxOccurs=\"1\" minOccurs=\"0\" name=\"echoedExplainRequest\" type=\"tns1:ExplainRequestType\"/>"
         + "             <element maxOccurs=\"1\" minOccurs=\"0\" name=\"diagnostics\" type=\"tns1:diagnosticsType\"/>"
         + "             <element maxOccurs=\"1\" minOccurs=\"0\" name=\"extraResponseData\" type=\"tns1:extraDataType\"/>"
         + "            </sequence>"
         + "           </extension>"
         + "          </complexContent>"
         + "         </complexType>"
         + "         <element name=\"explainResponse\" type=\"tns1:ExplainResponseType\"/>"
         + "     </schema>"
         + "     <schema elementFormDefault=\"qualified\" "
         + "             targetNamespace=\"http://www.loc.gov/zing/cql/xcql/\" "
         + "             xmlns=\"http://www.w3.org/2001/XMLSchema\">"
         + "         <import namespace=\"http://srw.zing.www.loc.gov\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/srw/diagnostic/\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/srw/\"/>"
         + "         <complexType name=\"prefixType\">"
         + "             <sequence>"
         + "                 <element name=\"name\" type=\"xsd:string\"/>"
         + "                 <element name=\"identifier\" type=\"xsd:string\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"prefixesType\">"
         + "             <sequence>"
         + "                 <element maxOccurs=\"unbounded\" name=\"prefix\" type=\"tns3:prefixType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"modifierType\">"
         + "             <sequence>"
         + "                 <element name=\"type\" type=\"xsd:string\"/>"
         + "                 <element name=\"comparison\" type=\"xsd:string\"/>"
         + "                 <element name=\"value\" type=\"xsd:string\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"modifiersType\">"
         + "             <sequence>"
         + "                 <element maxOccurs=\"unbounded\" name=\"modifier\" type=\"tns3:modifierType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"booleanType\">"
         + "             <sequence>"
         + "                 <element name=\"value\" type=\"xsd:string\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"modifiers\" type=\"tns3:modifiersType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"tripleType\">"
         + "             <sequence>"
         + "                 <element name=\"boolean\" nillable=\"true\" type=\"tns3:booleanType\"/>"
         + "                 <element name=\"leftOperand\" type=\"tns3:operandType\"/>"
         + "                 <element name=\"rightOperand\" type=\"tns3:operandType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"relationType\">"
         + "             <sequence>"
         + "                 <element name=\"value\" type=\"xsd:string\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"modifiers\" type=\"tns3:modifiersType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"searchClauseType\">"
         + "             <sequence>"
         + "                 <element name=\"index\" type=\"xsd:string\"/>"
         + "                 <element name=\"relation\" type=\"tns3:relationType\"/>"
         + "                 <element name=\"term\" type=\"xsd:string\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "         <complexType name=\"operandType\">"
         + "             <sequence>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"prefixes\" type=\"tns3:prefixesType\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"triple\" type=\"tns3:tripleType\"/>"
         + "                 <element maxOccurs=\"1\" minOccurs=\"0\" name=\"searchClause\" type=\"tns3:searchClauseType\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "     </schema>"
         + "     <schema elementFormDefault=\"qualified\" "
         + "             targetNamespace=\"http://www.loc.gov/zing/srw/diagnostic/\" "
         + "             xmlns=\"http://www.w3.org/2001/XMLSchema\">"
         + "         <import namespace=\"http://srw.zing.www.loc.gov\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/cql/xcql/\"/>"
         + "         <import namespace=\"http://www.loc.gov/zing/srw/\"/>"
         + "         <complexType name=\"diagnosticType\">"
         + "             <sequence>"
         + "                 <element name=\"details\" nillable=\"true\" type=\"xsd:string\"/>"
         + "                 <element name=\"message\" nillable=\"true\" type=\"xsd:string\"/>"
         + "                 <element name=\"uri\" nillable=\"true\" type=\"xsd:anyURI\"/>"
         + "             </sequence>"
         + "         </complexType>"
         + "     </schema>"
         + " </wsdl:types>"
         + "  <wsdl:message name=\"SearchRetrieveOperationRequest\">"
         + "    <wsdl:part element=\"tns1:searchRetrieveRequest\" name=\"searchRetrieveRequest\"/>"
         + "  </wsdl:message>"
         + "  <wsdl:message name=\"SearchRetrieveOperationResponse\">"
         + "    <wsdl:part element=\"tns1:searchRetrieveResponse\" name=\"searchRetrieveResponse\"/>"
         + "  </wsdl:message>"
         + "  <wsdl:message name=\"ScanOperationResponse\">"
         + "    <wsdl:part element=\"tns1:scanResponse\" name=\"scanResponse\"/>"
         + ""
         + "  </wsdl:message>"
         + "  <wsdl:message name=\"ScanOperationRequest\">"
         + "    <wsdl:part element=\"tns1:scanRequest\" name=\"scanRequest\"/>"
         + "  </wsdl:message>"
         + "  <wsdl:portType name=\"SRWPort\">"
         + "    <wsdl:operation name=\"SearchRetrieveOperation\" parameterOrder=\"searchRetrieveRequest\">"
         + "      <wsdl:input message=\"impl:SearchRetrieveOperationRequest\" name=\"SearchRetrieveOperationRequest\"/>"
         + "      <wsdl:output message=\"impl:SearchRetrieveOperationResponse\" name=\"SearchRetrieveOperationResponse\"/>"
         + "    </wsdl:operation>"
         + ""
         + "    <wsdl:operation name=\"ScanOperation\" parameterOrder=\"scanRequest\">"
         + "      <wsdl:input message=\"impl:ScanOperationRequest\" name=\"ScanOperationRequest\"/>"
         + "      <wsdl:output message=\"impl:ScanOperationResponse\" name=\"ScanOperationResponse\"/>"
         + "    </wsdl:operation>"
         + "  </wsdl:portType>"
         + "  <wsdl:binding name=\"SRWSoapBinding\" type=\"impl:SRWPort\">"
         + "    <wsdlsoap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>"
         + "    <wsdl:operation name=\"SearchRetrieveOperation\">"
         + "      <wsdlsoap:operation soapAction=\"searchRetrieve\"/>"
         + ""
         + "      <wsdl:input name=\"SearchRetrieveOperationRequest\">"
         + "        <wsdlsoap:body use=\"literal\"/>"
         + "      </wsdl:input>"
         + "      <wsdl:output name=\"SearchRetrieveOperationResponse\">"
         + "        <wsdlsoap:body use=\"literal\"/>"
         + "      </wsdl:output>"
         + "    </wsdl:operation>"
         + "    <wsdl:operation name=\"ScanOperation\">"
         + "      <wsdlsoap:operation soapAction=\"scan\"/>"
         + ""
         + "      <wsdl:input name=\"ScanOperationRequest\">"
         + "        <wsdlsoap:body use=\"literal\"/>"
         + "      </wsdl:input>"
         + "      <wsdl:output name=\"ScanOperationResponse\">"
         + "        <wsdlsoap:body use=\"literal\"/>"
         + "      </wsdl:output>"
         + "    </wsdl:operation>"
         + "  </wsdl:binding>"
         + "  <wsdl:message name=\"ExplainOperationRequest\">"
         + "      <wsdl:part element=\"tns1:explainRequest\" name=\"explainRequest\"/>"
         + "   </wsdl:message>"
         + "   <wsdl:message name=\"ExplainOperationResponse\">"
         + "      <wsdl:part element=\"tns1:explainResponse\" name=\"explainResponse\"/>"
         + "   </wsdl:message>"
         + "   <wsdl:portType name=\"ExplainPort\">"
         + "      <wsdl:operation name=\"ExplainOperation\" parameterOrder=\"explainRequest\">"
         + "         <wsdl:input message=\"impl:ExplainOperationRequest\" name=\"ExplainOperationRequest\"/>"
         + "        <wsdl:output message=\"impl:ExplainOperationResponse\" name=\"ExplainOperationResponse\"/>"
         + "     </wsdl:operation>"
         + "   </wsdl:portType>"
         + "   <wsdl:binding name=\"ExplainSOAPSoapBinding\" type=\"impl:ExplainPort\">"
         + "      <wsdlsoap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>"
         + "      <wsdl:operation name=\"ExplainOperation\">"
         + "         <wsdlsoap:operation soapAction=\"explain\"/>"
         + "         <wsdl:input name=\"ExplainOperationRequest\">"
         + "           <wsdlsoap:body use=\"literal\"/>"
         + "         </wsdl:input>"
         + "         <wsdl:output name=\"ExplainOperationResponse\">"
         + "            <wsdlsoap:body use=\"literal\"/>"
         + "         </wsdl:output>"
         + "      </wsdl:operation>"
         + "   </wsdl:binding>"
         + "  <wsdl:service name=\"SRWSampleService\">"
         + "    <wsdl:port binding=\"impl:SRWSoapBinding\" name=\"SRW\">"
         + "      <wsdlsoap:address location=\"http://localhost:8080/srw/search/escidoc_all?wsdl\"/>"
         + "    </wsdl:port>"
         + "     <wsdl:port binding=\"impl:ExplainSOAPSoapBinding\" name=\"ExplainSOAP\">"
         + "        <wsdlsoap:address location=\"http://localhost:8080/srw/search/escidoc_all?wsdl\"/>"
         + "     </wsdl:port>"
         + "  </wsdl:service>"
         + "</wsdl:definitions>";
 
     }
 }
