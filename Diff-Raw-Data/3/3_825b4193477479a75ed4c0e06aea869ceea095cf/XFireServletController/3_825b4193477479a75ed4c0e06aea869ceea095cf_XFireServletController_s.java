 package org.codehaus.xfire.transport.http;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.attachments.JavaMailAttachments;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceRegistry;
 import org.codehaus.xfire.transport.Channel;
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
     private static ThreadLocal requests = new ThreadLocal();
     private static ThreadLocal responses = new ThreadLocal();
     private final static Log logger = LogFactory.getLog(XFireServletController.class);
     
     protected XFire xfire;
 
     protected SoapHttpTransport transport;
 
     private Map channels = new HashMap();
     
     public XFireServletController(XFire xfire)
     {
         this.xfire = xfire;
         this.transport = new SoapHttpTransport();
 
         registerTransport();
     }
 
     protected void registerTransport()
     {
         TransportManager service = getTransportManager();
         service.register(transport);
     }
 
     public static HttpServletRequest getRequest()
     {
         return (HttpServletRequest) requests.get();
     }
 
     public static HttpServletResponse getResponse()
     {
         return (HttpServletResponse) responses.get();
     }
 
     /**
      * @return
      */
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
         ServiceRegistry reg = getServiceRegistry();
 
         response.setHeader("Content-Type", "UTF-8");
 
         requests.set(request);
         responses.set(response);
 
         if (serviceName == null || serviceName.length() == 0 || !reg.hasService(serviceName))
         {
             if (!reg.hasService(serviceName))
             {
                 response.setStatus(404);
             }
 
             generateServices(response);
             return;
         }
 
         try
         {
             String wsdl = request.getParameter("wsdl");

             if (wsdl != null)
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
     protected void generateServices(HttpServletResponse response)
             throws ServletException, IOException
     {
         response.setContentType("text/html");
 
         HtmlServiceWriter writer = new HtmlServiceWriter();
         try
         {
             writer.write(response.getOutputStream(), getServiceRegistry().getServices());
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
         // TODO: Return 500 on a fault
         // TODO: Determine if the request is a soap request
         // and if not responsed appropriately
         
         response.setStatus(200);
         // response.setBufferSize(1024 * 8);
         response.setContentType("text/xml; charset=UTF-8");
 
         XFireHttpSession session = new XFireHttpSession(request);
         MessageContext context = new MessageContext(service, null, session);
         context.setService(getService(service));
         
         Channel channel = transport.createChannel(getService(service));
         
         String contentType = request.getContentType();
         if (null == contentType || contentType.toLowerCase().indexOf("multipart/related") != -1)
         {
             try
             {
                 InputStream stream = createMIMERequest(request, context);
                 
                 XMLStreamReader reader = 
                     STAXUtils.createXMLStreamReader(stream, request.getCharacterEncoding());
                 InMessage message = new InMessage(reader, request.getRequestURI());
                 channel.receive(context, message);
             }
             catch (MessagingException e)
             {
                 throw new XFireRuntimeException("Couldn't parse request message.", e);
             }
         }
         else
         {
             XMLStreamReader reader = 
                 STAXUtils.createXMLStreamReader(request.getInputStream(), 
                                                 request.getCharacterEncoding());
             
             InMessage message = new InMessage(reader, request.getRequestURI());
             channel.receive(context, message);
         }
     }
 
     protected InputStream createMIMERequest(HttpServletRequest request, MessageContext context)
             throws MessagingException, IOException
     {
         Session session = Session.getDefaultInstance(new Properties(), null);
         MimeMessage inMsg = new MimeMessage(session, request.getInputStream());
         inMsg.addHeaderLine("Content-Type: " + request.getContentType());
 
         final Object content = inMsg.getContent();
 
         if (content instanceof MimeMultipart)
         {
             MimeMultipart inMP = (MimeMultipart) content;
 
             JavaMailAttachments atts = new JavaMailAttachments(inMP);
             context.setProperty(JavaMailAttachments.ATTACHMENTS_KEY, atts);
 
             return atts.getSoapMessage().getDataHandler().getInputStream();
         }
         else
         {
             throw new UnsupportedOperationException();
             //TODO set 500 and bail
         }
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
         response.setStatus(200);
         response.setContentType("text/xml");
 
         getXFire().generateWSDL(service, response.getOutputStream());
     }
 
     /**
      * @param request
      * @return
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
 
     /**
      * @return
      */
     public XFire getXFire()
     {
         return xfire;
     }
 
     public ServiceRegistry getServiceRegistry()
     {
         return xfire.getServiceRegistry();
     }
 }
