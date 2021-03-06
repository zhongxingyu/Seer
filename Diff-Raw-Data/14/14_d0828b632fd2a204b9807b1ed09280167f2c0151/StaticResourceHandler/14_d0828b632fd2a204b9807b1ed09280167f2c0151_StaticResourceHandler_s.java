 package com.astamuse.asta4d.web.builtin;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.SoftReference;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FilenameUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.astamuse.asta4d.Configuration;
 import com.astamuse.asta4d.Context;
 import com.astamuse.asta4d.web.WebApplicationContext;
 import com.astamuse.asta4d.web.dispatch.mapping.UrlMappingRule;
 import com.astamuse.asta4d.web.dispatch.request.RequestHandler;
 import com.astamuse.asta4d.web.dispatch.response.provider.BinaryDataProvider;
 import com.astamuse.asta4d.web.dispatch.response.provider.HeaderInfo;
 import com.astamuse.asta4d.web.dispatch.response.provider.HeaderInfoProvider;
 import com.astamuse.asta4d.web.util.BinaryDataUtil;
 
 /**
  * A static resouce handler for service static resources such as js, css or
  * static html files.
  * 
  * The following path vars can be configured for specializing response headers:
  * 
  * <ul>
  * <li>{@link #VAR_CONTENT_TYPE}
  * <li>{@link #VAR_CONTENT_CACHE_SIZE_LIMIT_K}
  * <li>{@link #VAR_CACHE_TIME}
  * <li>{@link #VAR_LAST_MODIFIED}
  * </ul>
  * 
  * Or the following methods can be override for more complex calculations:
  * 
  * <ul>
  * <li>{@link #judgContentType(String)}
  * <li>{@link #getContentCacheSizeLimit(String)}
  * <li>{@link #decideCacheTime(String)}
  * <li>{@link #getLastModifiedTime(String)}
  * </ul>
  * 
  * @author e-ryu
  * 
  */
 public class StaticResourceHandler extends AbstractGenericPathHandler {
 
     /**
      * see {@link #judgContentType(String)}
      */
     public final static String VAR_CONTENT_TYPE = StaticResourceHandler.class.getName() + "#content_type";
 
     /**
      * see {@link #getContentCacheSizeLimit(String)}
      */
     public final static String VAR_CONTENT_CACHE_SIZE_LIMIT_K = StaticResourceHandler.class.getName() + "#content_cache_size_limit_k";
 
     /**
      * see {@link #decideCacheTime(String)}
      */
     public final static String VAR_CACHE_TIME = StaticResourceHandler.class.getName() + "#cache_time";
 
     /**
      * see {@link #getLastModifiedTime(String)}
      */
     public final static String VAR_LAST_MODIFIED = StaticResourceHandler.class.getName() + "#last_modified";
 
     private final static Logger logger = LoggerFactory.getLogger(StaticResourceHandler.class);
 
    private final static long DefaultLastModified = getCurrentSystemTimeInGMT();
     // one hour
     private final static long DefaultCacheTime = 1000 * 60 * 60;
 
     private final static class StaticFileInfoHolder {
         String contentType;
         String path;
         long lastModified;
         int cacheLimit;
         SoftReference<byte[]> content;
         InputStream firstTimeInput;
     }
 
     private final static StaticFileInfoHolder NoContent = new StaticFileInfoHolder();
 
     private final static ConcurrentHashMap<String, StaticFileInfoHolder> StaticFileInfoMap = new ConcurrentHashMap<>();
 
     public StaticResourceHandler() {
         super();
     }
 
     public StaticResourceHandler(String basePath) {
         super(basePath);
     }
 
    private final static long getCurrentSystemTimeInGMT() {
        DateTime current = DateTime.now();
        return DateTimeZone.getDefault().convertLocalToUTC(current.getMillis(), false);
    }

     private HeaderInfoProvider createSimpleHeaderResponse(int status) {
         HeaderInfo header = new HeaderInfo(status);
         HeaderInfoProvider provider = new HeaderInfoProvider(header);
         provider.setContinuable(false);
         return provider;
     }
 
     @RequestHandler
     public Object handler(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext,
             UrlMappingRule currentRule) throws FileNotFoundException, IOException {
         String path = convertPath(request, currentRule);
 
         if (path == null) {
             return createSimpleHeaderResponse(404);
         }
 
         boolean cacheEnable = Configuration.getConfiguration().isCacheEnable();
 
         StaticFileInfoHolder info = cacheEnable ? StaticFileInfoMap.get(path) : null;
 
         if (info == null) {
             info = createInfo(servletContext, path);
             StaticFileInfoMap.put(path, info);
         }
 
         if (info == NoContent) {
             return createSimpleHeaderResponse(404);
         }
 
         if (cacheEnable) {
             long clientTime = retrieveClientCachedTime(request);
 
             if (clientTime >= info.lastModified) {
                 return createSimpleHeaderResponse(304);
             }
         }
 
         // our header provider is not convenient for date header... hope we can
         // improve it in future
         long cacheTime = decideCacheTime(path);
         response.setStatus(200);
         response.setHeader("Content-Type", info.contentType);
        response.setDateHeader("Expires", getCurrentSystemTimeInGMT() + cacheTime);
         response.setDateHeader("Last-Modified", info.lastModified);
        response.setHeader("Cache-control", "max-age=" + cacheTime);
 
         // here we do not synchronize threads because we do not matter
 
         if (info.content == null && info.firstTimeInput != null) {
             // no cache, and we have opened it at first time
             InputStream input = info.firstTimeInput;
             info.firstTimeInput = null;
             return new BinaryDataProvider(input);
         } else if (info.content == null && info.firstTimeInput == null) {
             // no cache
             return new BinaryDataProvider(servletContext, this.getClass().getClassLoader(), info.path);
         } else {
             // should cache
             byte[] data = null;
             data = info.content.get();
             if (data == null) {
                 InputStream input = BinaryDataUtil.retrieveInputStreamByPath(servletContext, this.getClass().getClassLoader(), path);
                 data = retrieveBytesFromInputStream(input, info.cacheLimit);
                 info.content = new SoftReference<byte[]>(data);
             }
             return new BinaryDataProvider(data);
         }
     }
 
     private long retrieveClientCachedTime(HttpServletRequest request) {
         try {
             long time = request.getDateHeader("If-Modified-Since");
             return DateTimeZone.getDefault().convertLocalToUTC(time, false);
         } catch (Exception e) {
             logger.debug("retrieve If-Modified-Since failed", e);
             return -1;
         }
     }
 
     private StaticFileInfoHolder createInfo(ServletContext servletContext, String path) throws FileNotFoundException, IOException {
 
         InputStream input = BinaryDataUtil.retrieveInputStreamByPath(servletContext, this.getClass().getClassLoader(), path);
 
         if (input == null) {
             return NoContent;
         }
 
         StaticFileInfoHolder info = new StaticFileInfoHolder();
         info.contentType = judgContentType(path);
         info.path = path;
         info.lastModified = getLastModifiedTime(path);
 
         // cut the milliseconds
         info.lastModified = info.lastModified / 1000 * 1000;
 
         info.cacheLimit = getContentCacheSizeLimit(path);
 
         if (info.cacheLimit == 0) {// don't cache
             info.content = null;
             info.firstTimeInput = input;
         } else {
             byte[] contentData = retrieveBytesFromInputStream(input, info.cacheLimit);
             try {
                 info.content = new SoftReference<byte[]>(contentData);
             } finally {
                 input.close();
             }
             info.firstTimeInput = null;
         }
         return info;
     }
 
     private byte[] retrieveBytesFromInputStream(InputStream input, int cacheSize) throws IOException {
         int bufferSize = cacheSize + 1;
         byte[] b = new byte[bufferSize];
         int len = input.read(b);
         if (len > cacheSize) {// over the limit of cache size
             return null;
         } else {
             byte[] rtn = new byte[len];
             System.arraycopy(b, 0, rtn, 0, len);
             return rtn;
         }
     }
 
     private final static Map<String, String> MimeTypeMap = new HashMap<>();
     static {
         MimeTypeMap.put("js", "application/javascript");
         MimeTypeMap.put("css", "text/css");
     }
 
     /**
      * The header value of Content-Type
      * 
      * @param path
      * @return a guess of the content type by file name extension,
      *         "application/octet-stream" when not matched
      */
     protected String judgContentType(String path) {
 
         Context context = Context.getCurrentThreadContext();
 
         String forceContentType = context.getData(WebApplicationContext.SCOPE_PATHVAR, VAR_CONTENT_TYPE);
         if (forceContentType != null) {
             return forceContentType;
         }
 
         String fileName = FilenameUtils.getName(path);
 
         // guess the type by file name extension
         String type = URLConnection.guessContentTypeFromName(fileName);
 
         if (type == null) {
             type = MimeTypeMap.get(FilenameUtils.getExtension(fileName));
         }
 
         if (type == null) {
             type = "application/octet-stream";
         }
         return type;
     }
 
     /**
      * The header value of Cache-control and Expires.
      * 
      * override this method to supply the specialized cache time.
      * 
      * @param path
      * @return cache time in millisecond unit
      */
     protected long decideCacheTime(String path) {
         Long varCacheTime = Context.getCurrentThreadContext().getData(WebApplicationContext.SCOPE_PATHVAR, VAR_CACHE_TIME);
         if (varCacheTime != null) {
             return varCacheTime;
         } else {
             return DefaultCacheTime;
         }
     }
 
     /**
      * 
      * The header value of Last-Modified.
      * 
      * override this method to supply the specialized last modified time
      * 
      * @param path
      * @return the time of last modified time in millisecond unit(In http
      *         protocol, the time unit should be second, but we will cope with
      *         this matter)
      */
     protected long getLastModifiedTime(String path) {
         WebApplicationContext context = Context.getCurrentThreadContext();
         Long varLastModified = context.getData(WebApplicationContext.SCOPE_PATHVAR, VAR_LAST_MODIFIED);
         if (varLastModified != null) {
             return varLastModified;
         } else {
             long retrieveTime = BinaryDataUtil.retrieveLastModifiedByPath(context.getServletContext(), this.getClass().getClassLoader(),
                     path);
             if (retrieveTime == 0L) {
                 return DefaultLastModified;
             } else {
                 return retrieveTime;
             }
         }
     }
 
     /**
      * Retrieve the max cachable size limit for a certain path in byte unit.Be
      * care of that the path var is set by kilobyte unit for convenience but
      * this method will return in byte unit. <br>
      * This is a default implementation which does not see the path and will
      * return 0 for not caching when path var is not set.
      * <p>
      * Note: we do not cache it by default because the resources in war should
      * have been cached by the servlet container.
      * 
      * 
      * @param path
      * @return the max cachable size limit for a certain path in byte unit.
      */
     protected int getContentCacheSizeLimit(String path) {
         Integer varCacheSize = Context.getCurrentThreadContext().getData(WebApplicationContext.SCOPE_PATHVAR,
                 VAR_CONTENT_CACHE_SIZE_LIMIT_K);
         if (varCacheSize != null) {
             return varCacheSize;
         } else {
             return 0;
         }
     }
 
 }
