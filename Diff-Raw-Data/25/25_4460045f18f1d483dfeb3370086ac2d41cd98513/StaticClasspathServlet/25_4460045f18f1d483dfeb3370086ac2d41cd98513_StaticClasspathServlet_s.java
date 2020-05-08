 package org.otherobjects.cms.servlet;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Fast servlet to serve static classpath resources for the webapp (such as 
  * those provided by OTHERobjects and other plugins).
  * 
  * Note: this will only serve resources that are in a /static/ folder as basic
  * attempt to a stop access to all Classpath resources.
  * 
  * <p>Used primarily for serving static content from the OTHERobjects classpath.
  * @author rich
  */
 public class StaticClasspathServlet extends StaticFileServlet
 {
     private static final long serialVersionUID = 3970455238515527584L;
 
     private final Logger logger = LoggerFactory.getLogger(StaticClasspathServlet.class);
 
     @Override
     protected ReadableByteChannel getInputChannel(String path, HttpServletRequest request) throws FileNotFoundException, NotModifiedException
     {
         String servletPath = request.getServletPath();
         path = path.substring(servletPath.length());
 
         logger.info("Static resource request: {}", path);
         
         //  Security check: so that non-static data is not served
         if (!path.contains("/static/") || path.contains(".."))
         {
             this.logger.warn("Prevented access to non-static resource: {}", path);
             //resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
             throw new FileNotFoundException();
         }
        URL resource = getClass().getResource(path);
         InputStream in = getClass().getResourceAsStream(path);
         return Channels.newChannel(in);
     }
 }
