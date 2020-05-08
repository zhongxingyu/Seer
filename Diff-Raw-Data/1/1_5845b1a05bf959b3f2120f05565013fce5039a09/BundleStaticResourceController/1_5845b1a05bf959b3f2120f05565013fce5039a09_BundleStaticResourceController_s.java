 package ddth.dasp.framework.springmvc;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import ddth.dasp.common.rp.IRequestParser;
 import ddth.dasp.framework.resource.IResourceLoader;
 
 /**
  * This controller serves bundle's static content over the web.
  * 
  * @author NBThanh <btnguyen2k@gmail.com>
  * @version since v0.1.0
  */
 public class BundleStaticResourceController extends BaseAnnotationController {
 
     private String encoding = "utf-8";
     private String resourcePrefix = "";
     private IResourceLoader resourceLoader;
     private Map<String, String> mimetypes = new HashMap<String, String>();
 
     public BundleStaticResourceController() {
         mimetypes.put(".bmp", "image/bmp");
         mimetypes.put(".css", "text/css");
         mimetypes.put(".gif", "image/gif");
         mimetypes.put(".ico", "image/x-icon");
         mimetypes.put(".jpg", "image/jpeg");
         mimetypes.put(".js", "text/javascript");
         mimetypes.put(".png", "image/png");
     }
 
     protected Map<String, String> getMimetypes() {
         return mimetypes;
     }
 
     public void setMimetypes(Map<String, String> mimetypes) {
         this.mimetypes = mimetypes;
     }
 
     protected String getEncoding() {
         return encoding;
     }
 
     public void setEncoding(String encoding) {
         this.encoding = encoding;
     }
 
     protected String getResourcePrefix() {
         return resourcePrefix;
     }
 
     public void setResourcePrefix(String resourcePrefix) {
         this.resourcePrefix = resourcePrefix;
         if (resourcePrefix.endsWith("/")) {
             resourcePrefix.replaceAll("\\/+$", "/");
         } else {
             resourcePrefix += "/";
         }
     }
 
     protected IResourceLoader getResourceLoader() {
         return resourceLoader;
     }
 
     public void setResourceLoader(IResourceLoader resourceLoader) {
         this.resourceLoader = resourceLoader;
     }
 
     @RequestMapping
     public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
         IRequestParser rp = getRequestParser(request);
         String requestUri = rp.getRequestUri();
         if (requestUri.startsWith("/")) {
             requestUri = requestUri.replaceAll("^\\/+", "");
         }
 
         String resourceUri = resourcePrefix != null ? resourcePrefix + requestUri : requestUri;
         if (serveStaticResource(resourceUri, request, response)) {
             return;
         }
 
         String requestAction = getRequestAction(request);
         if (!StringUtils.isBlank(requestAction)) {
             if (requestAction.length() + 1 < requestUri.length()) {
                 requestUri = requestUri.substring(requestAction.length() + 1);
                 if (serveStaticResource(resourceUri, request, response)) {
                     return;
                 }
             }
         }
 
         response.sendError(HttpServletResponse.SC_NOT_FOUND);
     }
 
     protected String deltectMimetype(String resourceUri) {
         int index = resourceUri.lastIndexOf('.');
         String resourceExt = index < 0 ? "" : resourceUri.substring(index);
         String mimetype = mimetypes.get(resourceExt);
         if (StringUtils.isBlank(mimetype)) {
             mimetype = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(resourceUri);
         }
         return !StringUtils.isBlank(mimetype) ? mimetype : "application/octet-stream";
     }
 
     protected boolean serveStaticResource(String resourceUri, HttpServletRequest request,
             HttpServletResponse response) throws IOException {
         if (!resourceLoader.resourceExists(resourceUri)) {
             return false;
         }
 
         // return some HTTP headers first
         if (!StringUtils.isBlank(encoding)) {
             response.setCharacterEncoding(encoding);
         }
         String mimetype = deltectMimetype(resourceUri);
         response.setContentType(mimetype);
         byte[] content = resourceLoader.loadResourceAsBinary(resourceUri);
         response.setContentLength(content.length);
         String etag = DigestUtils.md5Hex(content);
         response.setHeader("ETag", etag);
         response.setHeader("Cache-control", "private");
         // 1 hour in seconds
         response.setIntHeader("Max-Age", 3600 * 1);
         long resourceTimestamp = resourceLoader.getLastModified(resourceUri);
         if (resourceTimestamp > 0) {
             response.setDateHeader("Last-Modified", resourceTimestamp);
             // 1 hour in millisecs
             response.setDateHeader("Expires", System.currentTimeMillis() + 1 * 3600000L);
         }
         String headerIfNoneMatch = request.getHeader("If-None-Match");
         if (headerIfNoneMatch != null) {
             if (headerIfNoneMatch.equals(etag)) {
                 response.setStatus(304);
                 return true;
             }
         }
         if (request.getMethod().equals("HEAD")) {
             return true;
         }
 
         // and then the content
         OutputStream os = response.getOutputStream();
         os.write(content);
         os.flush();
 
         // InputStream is = resourceLoader.loadResource(resourceUri);
         // try {
         // OutputStream os = response.getOutputStream();
         // byte[] buffer = new byte[1024];
         // int bytesRead = is.read(buffer);
         // while (bytesRead != -1) {
         // os.write(buffer, 0, bytesRead);
         // bytesRead = is.read(buffer);
         // }
         // os.flush();
         // } finally {
         // is.close();
         // }
         return true;
     }
 }
