 package org.codehaus.xfire.transport.http;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Properties;

 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.attachments.JavaMailAttachments;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceRegistry;
 import org.codehaus.xfire.transport.TransportManager;
 
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
 
     private File webInfPath;
 
     protected XFire xfire;
 
     protected ServletContext context;
 
     protected SoapHttpTransport transport;
     
     public XFireServletController(XFire xfire, ServletContext context)
     {
         this.xfire = xfire;
         this.context = context;
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
     
     public File getWebappBase()
     {
         if (webInfPath == null)
         {
             webInfPath = new File(context.getRealPath("/WEB-INF"));
         }
 
         return webInfPath;
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
         String service = getService(request);
         ServiceRegistry reg = getServiceRegistry();
 
         String wsdl = request.getParameter("wsdl");
         response.setHeader("Content-Type", "UTF-8");
 
         requests.set(request);
         responses.set(response);
 
         if (service == null || service.equals("") || !reg.hasService(service))
         {
             generateServices(response);
             return;
         }
 
         try
         {
             if (wsdl != null)
             {
                 generateWSDL(response, service);
             }
             else
             {
                 invoke(request, response, service);
             }
         }
         catch (Exception e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     /**
      * @param response
      * @throws IOException
      * @throws ServletException
      */
     protected void generateServices(HttpServletResponse response)
             throws IOException
     {
         response.setContentType("text/html");
         StringBuffer sb = new StringBuffer();
         sb.append("<html><head><title>XFire Services</title></head>").append(
                 "<body>No such service.").append("<p>Services:<ul>.");
 
         ServiceRegistry registry = getServiceRegistry();
         Collection services = registry.getServices();
 
         for (Iterator itr = services.iterator(); itr.hasNext();)
         {
             Service service = (Service) itr.next();
             sb.append("<li>").append(service.getName()).append("</li>");
         }
         sb.append("</ul></p></body></html>");
 
         response.getWriter().write(sb.toString());
         response.getWriter().flush();
         // response.getWriter().close();
 
         return;
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
         MessageContext context = 
             new MessageContext(service, 
                                null, 
                                response.getOutputStream(), 
                                session, 
                                request.getRequestURI());
         context.setTransport(transport);
         
         String contentType = request.getHeader("Content-Type");
        if ( contentType.toLowerCase().indexOf("multipart/related") != -1 )
         {
             try
             {
                 getXFire().invoke(createMIMERequest(request, context), context);
             }
             catch (MessagingException e)
             {
                 throw new XFireRuntimeException("Couldn't parse request message.", e);
             }
         }
         else
         {
             getXFire().invoke(request.getInputStream(), context);
         }
     }
 
     protected InputStream createMIMERequest(HttpServletRequest request, MessageContext context) 
     	throws MessagingException, IOException
     {
         Session session = Session.getDefaultInstance(new Properties(), null);
         MimeMessage inMsg = new MimeMessage(session, request.getInputStream());
         inMsg.addHeaderLine("Content-Type: " + request.getContentType());
        
        MimeMultipart inMP = (MimeMultipart) inMsg.getContent();
 
        JavaMailAttachments atts = new JavaMailAttachments(inMP);
        context.setProperty(JavaMailAttachments.ATTACHMENTS_KEY, atts);
        
        return atts.getSoapMessage().getDataHandler().getInputStream();
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
         // response.setBufferSize(1024 * 8);
 
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
