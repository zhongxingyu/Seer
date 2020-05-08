 package net.contextfw.web.commons.minifier;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.contextfw.web.application.WebApplicationException;
 
 abstract class ContentServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     
     private ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
             return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
         }
     };
 
     private String content;
     
     private final String host;
     
     private final String minifiedPath;
     
     private final long started;
     
     private static final long EXPIRATION = 60 * 60 * 1000 * 8;
     
     protected ContentServlet(String host,
                              String minifiedPath,
                              long started) {
         this.host = host;
         this.started = started;
         this.minifiedPath = minifiedPath;
         this.modifiedSince = new Date(started);
     }
     
     protected URL getUrl(String src) {
         try {
             return new URL(host + src);
         } catch (MalformedURLException e) {
             throw new WebApplicationException(e);
         }
     }
     
     String getMinifiedPath() {
         return minifiedPath.replaceFirst("<timestamp>", Long.toString(started));
     }
     
     private final Date modifiedSince;
     
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
             IOException {
         
         resp.setContentType(getContentType());
         String modifiedHeader = req.getHeader("If-Modified-Since");
         Date mod = null;
         
         if (modifiedHeader != null) {
             try {
                 mod = format.get().parse(modifiedHeader);
            } catch (Exception e) {
                // Just ignore
             }
         }
 
         HttpServletResponse httpResponse = (HttpServletResponse) resp;
         httpResponse.setDateHeader("Expires", started + EXPIRATION);
         httpResponse.setDateHeader("Date", started);
        httpResponse.setDateHeader("Last-Modified", started+1000);
         httpResponse.setHeader("Cache-Control", "max-age=2246400, must-revalidate");
         httpResponse.setHeader("Pragma", "cache");
         
         if (mod != null && modifiedSince.before(mod)) {
             resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
         } else {
             resp.getWriter().print(content);
         }
     }
     
     protected abstract String getContentType();
 
     public void setContent(String content) {
         this.content = content;
     }
 }
