 package org.jboss.seam.xwidgets.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.inject.Inject;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jboss.seam.solder.resourceLoader.ResourceProvider;
 import org.jboss.logging.Logger;
 
 /**
  * Main entry point for XWidgets service integration
  * 
  * @author Shane Bryzak
  *
  */
 public class XWidgetsServlet extends HttpServlet
 {
    private static final long serialVersionUID = 5987854458419051909L;
    
    private static final Logger log = Logger.getLogger(XWidgetsServlet.class);
    
    @Inject ResourceProvider resourceProvider;
    
    private ServletConfig servletConfig;
    
    @Override
    public void init(ServletConfig config) throws ServletException
    {
       this.servletConfig = config;
    }   
 
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws ServletException, IOException
    {
       String pathInfo = request.getPathInfo();
 
       // Nothing to do
       if (pathInfo == null)
       {
          response.sendError(HttpServletResponse.SC_NOT_FOUND, "No path information provided");
          return;
       }
 
       if (pathInfo.startsWith(servletConfig.getServletContext().getContextPath()))
       {
          pathInfo = pathInfo.substring(servletConfig.getServletContext().getContextPath().length());
       }      
       
       // Check if this is a resource request
       if (pathInfo.startsWith("/resources/"))
       {
          String resourcePath = pathInfo.substring(11);
          
          log.info("Processing request for resource: " + resourcePath);
          
          InputStream is = resourceProvider.loadResourceStream(resourcePath);
          
         if (is == null) {
             response.sendError(404);
             log.error("Specified resource '" + pathInfo + "' not found.");
         } else {         
             if (pathInfo.endsWith(".js")) {
                 response.setContentType("text/javascript");
             }
             
             byte[] buffer = new byte[512];
             int read = is.read(buffer);
             
             while (read != -1)
             {
                response.getOutputStream().write(buffer, 0, read);
                read = is.read(buffer);
             }
             response.getOutputStream().flush();
          }
       }
    }
 
 }
