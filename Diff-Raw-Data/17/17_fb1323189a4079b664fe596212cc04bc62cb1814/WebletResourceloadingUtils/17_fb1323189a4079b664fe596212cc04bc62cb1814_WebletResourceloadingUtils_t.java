 package net.java.dev.weblets.packaged;
 
 import net.java.dev.weblets.WebletConfig;
 import net.java.dev.weblets.WebletRequest;
 import net.java.dev.weblets.WebletResponse;
 import net.java.dev.weblets.util.IStreamingFilter;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * helper class to be shared by various weblet loaders
  */
 public class WebletResourceloadingUtils {
     static WebletResourceloadingUtils instance = new WebletResourceloadingUtils();
 
     public static WebletResourceloadingUtils getInstance() {
         return instance;
     }
 

     public void loadFromUrl(WebletConfig config, WebletRequest request, WebletResponse response, URL url, IStreamingFilter filterChain) throws IOException {
         if (url != null) {
 
             URLConnection conn = url.openConnection();
             response.setLastModified(conn.getLastModified());

             response.setContentType(null); // Bogus "text/html" overriding mime-type
             response.setContentVersion(config.getWebletVersion());
 
            //some browsers only work on seconds  (Mozilla)  so we go down to one second for a shared
            //common response time
            long requestCacheState = request.getIfModifiedSince();
            if (requestCacheState > 1000)
                requestCacheState = requestCacheState - requestCacheState % 1000;
            long responseCacheState = conn.getLastModified();
            if (responseCacheState > 1000)
                responseCacheState = responseCacheState - responseCacheState % 1000;
 
            if (requestCacheState < responseCacheState) {
                 InputStream in = conn.getInputStream();
                 OutputStream out = response.getOutputStream();
                 try {
                     filterChain.filter(config, request, response, in, out);
                 } finally {
                     filterChain.close();
                     //just in case we missed something in the lower rim
                     try {
                         in.close();
                     } catch (IOException e) {
                         Log log = LogFactory.getLog(this.getClass());
                         log.error(e);
                     }
                     try {
                         out.close();
                     } catch (IOException e) {
                         Log log = LogFactory.getLog(this.getClass());
                         log.error(e);
                     }
                 }
             } else {
                 response.setStatus(WebletResponse.SC_NOT_MODIFIED);
             }
         } else {
             response.setStatus(WebletResponse.SC_NOT_FOUND);
         }
     }
 
 }
