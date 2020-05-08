 package org.otherobjects.cms.servlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Fast servlet to serve static resources for the webapp. Static resources
  * may come from local project files or the classpath (such as those provided
  * by otherobjects and other plugins).
  * 
  * <p>TODO Check caching and gzip
  * <br>TODO Local file serving  - check not cached
  * <br>TODO Check content types
  * <br>TODO Adapt to mapped path correctly
  * <br>TODO Check this works in non-Jetty containers
  * 
  * @author rich
  */
 public class StaticResourceServlet extends HttpServlet
 {
     private final Logger logger = LoggerFactory.getLogger(StaticResourceServlet.class);
 
     private static final long serialVersionUID = 3970455238515527584L;
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
     {
         String path = req.getPathInfo();
        //path = path.substring("resources".length() + 2);
 
         if (path == null)
             return;
 
         // FIXME Security check: so that configuration data can't be served
        //        if (!path.startsWith("static/") && !path.startsWith("templates/"))
        //            return null;
 
         logger.info("Requested resource: {}", path);
 
         // Add cache header
         resp.addHeader("Cache-Control", "max-age=3600");
        
         // FIXME Is there a faster way of servig these?
         // FIXME Cache?
         InputStream in = getClass().getResourceAsStream(path);
         OutputStream out = resp.getOutputStream();
         try
         {
            byte[] buf = new byte[4 * 1024]; // 4K buffer
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1)
             {
                out.write(buf, 0, bytesRead);
             }
         }
         finally
         {
             if (in != null)
                 in.close();
         }
        
        
     }
 
 }
