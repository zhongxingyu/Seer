 package org.codehaus.xfire.transport.http;
 
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.attachments.Attachments;
 import org.codehaus.xfire.attachments.StreamedAttachments;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.handler.AbstractHandler;
 import org.codehaus.xfire.handler.Phase;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceRegistry;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.transport.Channel;
 import org.codehaus.xfire.transport.Transport;
 import org.codehaus.xfire.transport.TransportManager;
 import org.codehaus.xfire.util.STAXUtils;
 
 /**
  * Loads XFire and processes requests.
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Feb 13, 2004
  */
 public class XFireServletController
 {
     public static final String HTTP_SERVLET_REQUEST = "XFireServletController.httpServletRequest";
     public static final String HTTP_SERVLET_RESPONSE = "XFireServletController.httpServletResponse";
     public static final String HTTP_SERVLET_CONTEXT = "XFireServletController.httpServletContext";
     
     private static ThreadLocal requests = new ThreadLocal();
     private static ThreadLocal responses = new ThreadLocal();
     private final static Log logger = LogFactory.getLog(XFireServletController.class);
     
     protected XFire xfire;
 
     protected SoapHttpTransport transport;
     private ServletContext servletContext;
 
     public XFireServletController(XFire xfire)
     {
        this(xfire, null);
     }
    
     public XFireServletController(XFire xfire, ServletContext servletContext)
     {
         this.xfire = xfire;
         this.servletContext = servletContext;
         
         // Create a SOAP Http transport with all the servlet addons
         transport = new XFireServletTransport();
         
         transport.addFaultHandler(new FaultResponseCodeHandler());
         
         Transport oldSoap = getTransportManager().getTransport(SoapHttpTransport.SOAP11_HTTP_BINDING);
         
         if (oldSoap != null) getTransportManager().unregister(oldSoap);
         
         getTransportManager().register(transport);
     }
 
     public static HttpServletRequest getRequest()
     {
         return (HttpServletRequest) requests.get();
     }
 
     public static HttpServletResponse getResponse()
     {
         return (HttpServletResponse) responses.get();
     }
 
     protected TransportManager getTransportManager()
     {
         return getXFire().getTransportManager();
     }
 
     /**
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
             *      javax.servlet.http.HttpServletResponse)
      */
     public void doService(HttpServletRequest request,
                           HttpServletResponse response)
             throws ServletException, IOException
     {
         String serviceName = getService(request);
         if (serviceName == null) serviceName = "";
         
         ServiceRegistry reg = getServiceRegistry();
        
         response.setHeader("Content-Type", "UTF-8");
 
         try
         {
             requests.set(request);
             responses.set(response);
 
             boolean hasService = reg.hasService(serviceName);
             if (serviceName.length() == 0 || !hasService)
             {
                 if (!hasService)
                 {
                     response.setStatus(404);
                 }
 
                 generateServices(request,response);
                 return;
             }
             
             if (request.getQueryString() != null &&
                 request.getQueryString().trim().equalsIgnoreCase("wsdl"))
             {
                 generateWSDL(response, serviceName);
             }
             else
             {
                 invoke(request, response, serviceName);
             }
         }
         catch (Exception e)
         {
             logger.error("Couldn't invoke servlet request.", e);
             
             if (e instanceof ServletException)
             {
                 throw (ServletException) e;
             }
             else
             {
                 throw new ServletException(e);
             }
         }
         finally
         {
             requests.set(null);
             responses.set(null);
         }
     }
 
     
 
 	protected void generateService(HttpServletResponse response, String serviceName)
             throws ServletException, IOException
     {
         response.setContentType("text/html");
         Service endpoint = getServiceRegistry().getService(serviceName);
         HtmlServiceWriter writer = new HtmlServiceWriter();
         try
         {
             writer.write(response.getOutputStream(), endpoint);
         }
         catch (XMLStreamException e)
         {
             throw new ServletException("Error writing HTML services list", e);
         }
     }
 
 
     /**
      * @param response
      */
     protected void generateServices(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         response.setContentType("text/html");
 
         HtmlServiceWriter writer = new HtmlServiceWriter(request);
       
         try
         {
         	Object value = XFireFactory.newInstance().getXFire().getProperty(XFire.SERVICES_LIST_DISABLED);
         	if( value != null && "true".equals(value.toString().toLowerCase())){
         	  response.getOutputStream().write("Services list disabled".getBytes());
         	}else{
               writer.write(response.getOutputStream(), getServiceRegistry().getServices());
         	}
         }
         catch (XMLStreamException e)
         {
             throw new ServletException("Error writing HTML services list", e);
         }
     }
 
     /**
      * @param request
      * @param response
      * @param service
      * @throws ServletException
      * @throws IOException
      * @throws UnsupportedEncodingException
      */
     protected void invoke(HttpServletRequest request,
                           HttpServletResponse response,
                           String service)
             throws ServletException, IOException, UnsupportedEncodingException
     {
         response.setStatus(200);
         response.setBufferSize(1024 * 8);
 
         XFireHttpSession session = new XFireHttpSession(request);
         MessageContext context = new MessageContext();
         context.setXFire(getXFire());
         context.setSession(session);
         context.setService(getService(service));
         context.setProperty(HTTP_SERVLET_REQUEST, request);
         context.setProperty(HTTP_SERVLET_RESPONSE, response);
        
        if (servletContext != null)
            context.setProperty(HTTP_SERVLET_CONTEXT, servletContext);
         
         Channel channel;
         try
         {
             channel = transport.createChannel(request.getRequestURI());
         }
         catch (Exception e)
         {
             logger.debug("Couldn't open channel.", e);
             throw new ServletException("Couldn't open channel.", e);
         }
         
         String soapAction = getSoapAction(request);
         String contentType = request.getContentType();
         if (null == contentType)
         {
             response.setContentType("text/html; charset=UTF-8");
             // TODO: generate service description here
             
             response.getWriter().write("<html><body>Invalid SOAP request.</body></html>");
             response.getWriter().close();
         }
         else if (contentType.toLowerCase().indexOf("multipart/related") != -1)
         {
         	// Nasty Hack to workaraound bug with lowercasing contenttype by some serwers what cause problems with finding message parts.
         	// There should be better fix for this.
         	String ct = request.getContentType().replaceAll("--=_part_","--=_Part_");
         	
         	
             Attachments atts = new StreamedAttachments(request.getInputStream(), ct);
           
             String encoding = getEncoding(atts.getSoapContentType());
 
             XMLStreamReader reader = 
                 STAXUtils.createXMLStreamReader(atts.getSoapMessage().getDataHandler().getInputStream(), 
                                                 encoding,
                                                 context);
             InMessage message = new InMessage(reader, request.getRequestURI());
             message.setProperty(SoapConstants.SOAP_ACTION, soapAction);
             message.setAttachments(atts);
             
             channel.receive(context, message);
             
             try
             {
                 reader.close();
             }
             catch (XMLStreamException e)
             {
                 throw new XFireRuntimeException("Could not close XMLStreamReader.");
             }
         }
         else
         {
             // Remove " and ' char
             String charEncoding = request.getCharacterEncoding();
             if( charEncoding != null && charEncoding.length()> 0 ){
               if( ( charEncoding.charAt(0)=='"' && charEncoding.charAt(charEncoding.length()-1) == '"') 
                       || ( charEncoding.charAt(0)=='\'' && charEncoding.charAt(charEncoding.length()-1) == '\'')){
                 charEncoding = charEncoding.substring(1,charEncoding.length()-1);
               }
             }
             XMLStreamReader reader = 
                 STAXUtils.createXMLStreamReader(request.getInputStream(), 
                                                 charEncoding,context);
             
             InMessage message = new InMessage(reader, request.getRequestURI());
             message.setProperty(SoapConstants.SOAP_ACTION, soapAction);
             channel.receive(context, message);
             
             try
             {
                 reader.close();
             }
             catch (XMLStreamException e)
             {
                 throw new XFireRuntimeException("Could not close XMLStreamReader.");
             }
         }
     }
 
     private String getSoapAction(HttpServletRequest request)
     {
         String action = request.getHeader(SoapConstants.SOAP_ACTION);
         
         if (action != null && action.startsWith("\"") && action.endsWith("\"") && action.length() >= 2)
         {
             action = action.substring(1, action.length() - 1);
         }
         
         return action;
     }
 
     private String getEncoding(String enc) throws ServletException
     {
         if (enc == null)
             return "UTF-8";
         
         int typeI = enc.indexOf("type=");
         if (typeI == -1) return null;
         
         int charI = enc.indexOf("charset=", typeI);
         if (charI == -1) return null;
         
         int end = enc.indexOf("\"", charI);
         if (end == -1) end = enc.indexOf(";", charI);
         if (end == -1) throw new ServletException("Invalid content type: " + enc);
         
         return enc.substring(charI + 8, end);
     }
 
     /**
      * @param response
      * @param service
      * @throws ServletException
      * @throws IOException
      */
     protected void generateWSDL(HttpServletResponse response, String service)
             throws ServletException, IOException
     {
         
         Service userService = getXFire().getServiceRegistry().getService(service);
         Object value = userService.getProperty(Service.DISABLE_WSDL_GENERATION);
         boolean isWSDLDisabled = "true".equalsIgnoreCase((value!=null?value.toString():null));
         if( isWSDLDisabled ){
             logger.warn("WSDL generation disabled for service :"+ service);
             response.sendError(404, "No wsdl is avaiable for this service");
             return ;
         }
         
         response.setStatus(200);
         response.setContentType("text/xml");
 
         getXFire().generateWSDL(service, response.getOutputStream());
     }
 
     /**
      * Get the service that is mapped to the specified request.
      */
     protected String getService(HttpServletRequest request)
     {
         String pathInfo = request.getPathInfo();
 
         if (pathInfo == null)
             return null;
 
         String serviceName;
 
         if (pathInfo.startsWith("/"))
         {
             serviceName = pathInfo.substring(1);
         }
         else
         {
             serviceName = pathInfo;
         }
 
         return serviceName;
     }
 
     protected Service getService(String name)
     {
         return getXFire().getServiceRegistry().getService(name);
     }
 
     public XFire getXFire()
     {
         return xfire;
     }
 
     public ServiceRegistry getServiceRegistry()
     {
         return xfire.getServiceRegistry();
     }
     
     public static class FaultResponseCodeHandler
         extends AbstractHandler
     {        
 
         public FaultResponseCodeHandler() {
             super();
             setPhase(Phase.TRANSPORT);
         }
 
        /**
         * @see org.codehaus.xfire.handler.Handler#invoke(org.codehaus.xfire.MessageContext)
         * @param context
         */
        public void invoke(MessageContext context)
        {
            HttpServletResponse response = XFireServletController.getResponse();
            if ( response != null )
                response.setStatus(500);
        }    
     }
 }
