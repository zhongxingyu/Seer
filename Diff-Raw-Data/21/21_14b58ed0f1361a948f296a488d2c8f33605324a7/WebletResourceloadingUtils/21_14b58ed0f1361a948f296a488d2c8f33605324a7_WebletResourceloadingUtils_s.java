 package net.java.dev.weblets.packaged;
 
 import net.java.dev.weblets.WebletConfig;
 import net.java.dev.weblets.WebletRequest;
 import net.java.dev.weblets.WebletResponse;
 import net.java.dev.weblets.util.CopyProvider;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.TimeZone;
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 /**
  * helper class to be shared by various weblet loaders
  *
  * @author Werner Punz
  */
 public class WebletResourceloadingUtils {
     static WebletResourceloadingUtils instance = new WebletResourceloadingUtils();
 
     public static WebletResourceloadingUtils getInstance() {
         return instance;
     }
 
     /**
      * loads a resource from a given url
      *
      * @param config       the current weblet config
      * @param request      the current weblet request
      * @param response     the current weblet response
      * @param url          the current url
      * @param copyProvider the processing filter chain for the weblet serving
      */
     public void loadFromUrl(WebletConfig config, WebletRequest request,
                             WebletResponse response, URL url, CopyProvider copyProvider)
             throws IOException {
         if (url != null) {
 
             URLConnection conn = url.openConnection();
             long lastmodified = conn.getLastModified();
             loadResourceFromStream(config, request, response, copyProvider, conn.getInputStream(), lastmodified);
 
         } else {
             response.setStatus(WebletResponse.SC_NOT_FOUND);
         }
     }
 
     /**
      * sets initial response params upon the version state
      *
      * @param config       the weblet config
      * @param response     the weblet response
      * @param lastmodified local resource lastmodified
      */
     private void prepareVersionedResponse(WebletConfig config,
                                           WebletResponse response, long lastmodified, long timeout) {
        long never;
         if (!isVersionedWeblet(config.getWebletVersion()))
             response.setLastModified(lastmodified);
         else {
             // we lock out resource loading once versioned, we do not run
             // into the chain
             // just in case the expires is ignored
             // this enforces the loading from cache on some browsers
             // even if refresh is pressed, this is by
             // definition the wanted behavior if versioning is on!
             // some browsers like firefox despite
             // having a future number pass a local date on refresh maybe we
             // lock this out as well
            //<>
 
            //Long timeout = timeoutOffset;
             response.setLastModified(timeout);
 
             // this should prevent requests entirely!
             response.setContentVersion(config.getWebletVersion(), timeout);
         }
     }
 
    private Long getTimeout(WebletConfig config) {
         String cacheControlTimeout = config.getInitParameter("cachecontrol-timeout");
        Long timeout = null;
         if (!StringUtils.isBlank(cacheControlTimeout)) {
             try {
                 timeout = Long.parseLong(cacheControlTimeout);
             } catch (RuntimeException ex) {
                 Log log = LogFactory.getLog(this.getClass());
                 log.error("Weblets: Cache control is set but to an invalid value setting now never instead");
             }
         }
        if (timeout == null)
            timeout = WebletResourceloadingUtils.getNever();
         return timeout;
     }
 
     /**
      * fixes internal time values
      * browsers deliver time values on seconds
      * internally it is calculated in miliseconds
      *
      * @param browserTimeValue the browser time value
      * @return returns a fixed second time value for the input
      */
     private long fixTimeValue(long browserTimeValue) {
         // some browsers only work on seconds (Mozilla) so we go down to one
         // second for a shared
         // common response time
         // we cannot tamper the cache state here, because
         // otherwise firefox will fail with an emptied page resource cache
         // (shift f5)
 
         if (browserTimeValue > 1000)
             browserTimeValue = browserTimeValue - browserTimeValue % 1000;
         return browserTimeValue;
     }
 
     /**
      * loads a given resource from an input stream
      * it uses internal timestamps for resource handling
      * and resource serving this works on most browser
      * but safari seems to ignore the timestamps
      * and always sends a modifiedSince for resources
      * for 1.1.1970
      *
      * @param config               the weblets config for this resource loading request
      * @param request              the weblets request
      * @param response             the weblets response
      * @param copyProvider         a given processing copy provider
      * @param in                   the input stream for the processing
      * @param resourceLastmodified the lastmodified for the given input stream
      * @throws IOException in case of an internal processing error
      */
     public void loadResourceFromStream(WebletConfig config,
                                        WebletRequest request, WebletResponse response,
                                        CopyProvider copyProvider, InputStream in, long resourceLastmodified)
             throws IOException {
 
         if (in != null) {
 
             // mime-type
 
             long requestCacheState = request.getIfModifiedSince();
             //the browser sends the utc timestamp
 
             requestCacheState = fixTimeValue(requestCacheState);
             long resourceModifiedState = resourceLastmodified;
             resourceModifiedState = fixTimeValue(resourceModifiedState);
 
 
             boolean load = false;
 
             long currentTime = System.currentTimeMillis();
             //utc time mapping
             long currentUTCTime = currentTime - TimeZone.getDefault().getOffset(currentTime);
             long utcResourceModifiedState = (resourceModifiedState - TimeZone.getDefault().getOffset(resourceModifiedState))+getTimeout(config);
             load = (requestCacheState < utcResourceModifiedState)
                     /*-1 or smaller value on reload pressed*/
                     || requestCacheState < currentUTCTime;
             /*cache control timeout reached we reload no matter what!*/
 
 
 
             if (load) {
                 prepareVersionedResponse(config, response, resourceLastmodified, System.currentTimeMillis()+ getTimeout(config));
                 response.setContentType(null); // Bogus "text/html" overriding
 
                 loadResourceFromStream(config, request, response, copyProvider,
                         in);
                 //response.setStatus(200);
 
             } else {
                 /*we have to set the timestamps as well here*/
                 prepareVersionedResponse(config, response, resourceLastmodified, request.getIfModifiedSince()+TimeZone.getDefault().getOffset(request.getIfModifiedSince()) );
                 response.setContentType(null); // Bogus "text/html" overriding
 
                 response.setStatus(WebletResponse.SC_NOT_MODIFIED);
             }
         } else {
             response.setStatus(WebletResponse.SC_NOT_FOUND);
         }
 
     }
 
     /**
      * loads the resource from a given input stream note, this api is under
      * construction we have caching not enabled yet
      *
      * @param config       the weblet config to load the resource
      * @param request      the weblet request
      * @param response     the weblet response
      * @param copyProvider the processing copy provider
      * @param in           the resource serving input stream
      * @throws IOException
      */
     public void loadResourceFromStream(WebletConfig config,
                                        WebletRequest request, WebletResponse response,
                                        CopyProvider copyProvider, InputStream in) throws IOException {
         OutputStream out = response.getOutputStream();
 
         copyProvider.copy(request.getWebletName(), response.getDefaultContentType(), in, out);
     }
 
     /* unified version checker for weblet versions maybe in existence */
     public static boolean isVersionedWeblet(String webletVersion) {
         return webletVersion != null && !webletVersion.trim().equals("")
                 && !webletVersion.endsWith("-SNAPSHOT");
     }
 
     /* defined never value used system internally */
     public static long getNever() {
         long now = System.currentTimeMillis();
        return now + 100l * 60l * 60l * 24l * 365l;
     }
 
 }
