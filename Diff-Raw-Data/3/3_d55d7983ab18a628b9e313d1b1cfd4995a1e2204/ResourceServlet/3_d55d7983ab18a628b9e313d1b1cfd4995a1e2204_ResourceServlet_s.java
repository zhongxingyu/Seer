 package fnug;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.zip.GZIPOutputStream;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import fnug.resource.Bundle;
 import fnug.resource.DefaultResource;
 import fnug.resource.Resource;
 import fnug.resource.ResourceResolver;
 import fnug.servlet.BadArg;
 import fnug.servlet.Bootstrap;
 import fnug.servlet.BundleNames;
 import fnug.servlet.ToServe;
 import fnug.servlet.ToServeBundle;
 import fnug.servlet.ToServeResource;
 
 /*
  Copyright 2010 Martin Algesten
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 /**
  * Servlet serving resources.
  * 
  * @author Martin Algesten
  * 
  */
 @SuppressWarnings("serial")
 public class ResourceServlet extends HttpServlet {
 
     private static final String PARAM_CALLBACK = "callback";
 
     public static final String UTF_8 = "utf-8";
 
     public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
     public static final String CONTENT_TYPE_JS = "text/javascript; charset=utf8";
 
     private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
     private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
     private static final String HEADER_LAST_MODIFIED = "Last-Modified";
     private static final String HEADER_EXPIRES = "Expires";
     private static final String HEADER_DATE = "Date";
     private static final String HEADER_CACHE_CONTROL = "Cache-Control";
 
     private static final String VALUE_GZIP = "gzip";
 
     private static final long ONE_YEAR = 365l * 24l * 60l * 60l * 1000l;
 
     private static final String PATH_IE_CSS = "/ie.css";
 
     private static final String CHAR_SLASH = "/";
 
     private static ThreadLocal<RequestEntry> reqEntry = new ThreadLocal<RequestEntry>();
 
     private ResourceResolver resolver;
     private ObjectMapper mapper = new ObjectMapper();
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
 
         initResolver(config);
 
     }
 
     private void initResolver(ServletConfig config) throws ServletException {
 
         String configStr = config.getInitParameter("config");
         if (configStr == null) {
             throw new ServletException("Missing config parameter 'config'");
         }
 
         String contextKey = ResourceResolver.class.getName() + "_" + configStr.hashCode();
 
         resolver = (ResourceResolver) config.getServletContext().getAttribute(contextKey);
         if (resolver == null) {
 
             String[] configs = configStr.split("\\s*,\\s*");
 
             LinkedList<Resource> resources = new LinkedList<Resource>();
 
             // add internal config resources first.
             resources.add(new DefaultResource("/fnug/", "bundles.js"));
 
             for (String s : configs) {
                 // normalize windowz strings in config to only use forward slashes.
                 if (File.separatorChar == '\\') {
                     s = s.replace(File.separatorChar, '/');
                 }
                 String basePath = s.substring(0, s.lastIndexOf(CHAR_SLASH) + 1);
                 String path = s.substring(s.lastIndexOf(CHAR_SLASH) + 1);
                 resources.add(new DefaultResource(basePath, path));
             }
 
             resolver = new ResourceResolver(resources);
             config.getServletContext().setAttribute(contextKey, resolver);
 
         }
 
     }
 
     @Override
     protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         String prefix = req.getContextPath() + req.getServletPath();
         prefix = prefix.endsWith(CHAR_SLASH) ?
                 prefix.substring(0, prefix.length() - 1) : prefix;
 
         if (req.getPathInfo().equals(PATH_IE_CSS)) {
             serviceIeIncludeCss(prefix, req, resp);
             return;
         }
 
         resolver.setThreadLocal();
         resolver.checkModified();
 
         String path = req.getPathInfo();
 
         String gzipHeader = req.getHeader(HEADER_ACCEPT_ENCODING);
         boolean gzip = gzipHeader != null && gzipHeader.indexOf(VALUE_GZIP) >= 0;
 
         String jsonp = req.getParameter(PARAM_CALLBACK);
         if (jsonp != null && jsonp.trim().equals("")) {
             jsonp = null;
         }
 
         RequestEntry entry = new RequestEntry(prefix, path, gzip, jsonp);
         reqEntry.set(entry);
 
         super.service(req, resp);
 
         // when the servlet container does a 304 not modified, Content-Type is
         // set to null, this often results in the Content-Type being set to a
         // default by the servlet container or a web server/cache in front of
         // the servlet container. Such deafult content type is often wrong about the
         // original resource (text/plain or similar). By always setting the
         // "correct" content type, we ensure to not pollute caches etc.
         // according to the HTTP spec, it's okay to set any meta header about the
         // content in a 304 as long as they are true for the original resource.
         if (resp.getContentType() == null && !resp.isCommitted()) {
             entry.setHeaders(resp);
         }
 
         reqEntry.remove();
 
     }
 
     private void serviceIeIncludeCss(String prefix, HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
 
         String f = req.getParameter("f");
 
         if (f == null || f.equals("")) {
             resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing req param 'f'");
             return;
         }
 
         String[] files = f.split(",");
 
         resp.setContentType("text/css");
         resp.setCharacterEncoding("utf-8");
         PrintWriter writer = resp.getWriter();
 
         for (String file : files) {
 
             // this also serves as a protection against injecting strange
             // stuff into the resulting style sheet.
             Resource r = resolver.resolve(file);
 
             if (r != null && r.isCss() && r.getLastModified() > 0) {
                 writer.println("@import url(" + prefix + "/" + file + ");");
             }
 
         }
 
     }
 
     @Override
     protected long getLastModified(HttpServletRequest req) {
         return reqEntry.get().getLastModified();
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         reqEntry.get().serve(resp, false);
     }
 
     @Override
     protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         reqEntry.get().serve(resp, true);
     }
 
     private class RequestEntry {
 
         private static final String SUFFIX_JS = "js";
         private static final String SUFFIX_ADD_JS = "add.js";
         private static final String CHAR_DOT = ".";
         private String prefix;
 
         /**
          * Full path: /some/path/to/file.js
          */
         private String path;
 
         /**
          * The filename without suffix: file
          */
         private String file;
 
         /**
          * The suffix: js
          */
         private String suffix;
 
         private Object toServe;
         private byte[] toServeBytes;
         private String jsonp;
         private boolean gzip;
 
         public RequestEntry(String prefix, String path, boolean gzip, String jsonp) {
 
             this.prefix = prefix;
             this.gzip = gzip;
             this.jsonp = jsonp;
 
             initPathFileSuffix(path);
 
             initToServe();
             initToServeBytes();
 
         }
 
         private void initPathFileSuffix(String inpath) {
             if (inpath == null) {
                 inpath = "";
             }
             inpath = normalizePath(inpath);
             int lastDot = inpath.lastIndexOf(CHAR_DOT);
             path = inpath;
             if (inpath.endsWith(SUFFIX_ADD_JS)) {
                 file = inpath.substring(0, inpath.length() - SUFFIX_ADD_JS.length() - 1);
                 suffix = SUFFIX_ADD_JS;
             } else if (lastDot > inpath.lastIndexOf(CHAR_SLASH)) {
                 file = inpath.substring(0, lastDot);
                 suffix = inpath.substring(lastDot + 1);
             } else {
                 file = inpath;
                 suffix = "";
             }
         }
 
         private String normalizePath(String path) {
 
             if (path.startsWith(CHAR_SLASH)) {
                 path = path.substring(1);
             }
 
             if (path.endsWith(CHAR_SLASH)) {
                 path = path.substring(0, path.length() - 1);
             }
             return path;
         }
 
         private void initToServe() {
 
             try {
 
                 if (path.equals("")) {
                     toServe = new BundleNames(mapper, jsonp);
                 } else if (Bundle.BUNDLE_ALLOWED_CHARS.matcher(file).matches()) {
                     Bundle bundle = resolver.getBundle(file);
                     if (bundle != null) {
                         bundle.checkModified();
                         if (suffix.equals("")) {
                             toServe = new ToServeBundle(mapper, bundle, jsonp);
                         } else if (suffix.equals(SUFFIX_ADD_JS)) {
                             toServe = new Bootstrap(mapper, prefix, bundle, true);
                         } else if (suffix.equals(SUFFIX_JS)) {
                             toServe = new Bootstrap(mapper, prefix, bundle, false);
                         } else {
                             toServe = null;
                         }
                     }
                 }
                 if (toServe == null) {
                     Resource r = resolver.resolve(path);
                     if (r != null) {
                         r.checkModified();
                     }
                     toServe = r == null || r.getLastModified() == -1 ? null : new ToServeResource(r, jsonp);
                 }
 
             } catch (IllegalArgumentException iae) {
                 toServe = new BadArg(iae.getMessage());
             } catch (IllegalStateException ise) {
                 toServe = new BadArg(ise.getMessage());
             }
 
         }
 
         private void initToServeBytes() {
             if (toServe != null && toServe instanceof ToServe) {
                 toServeBytes = ((ToServe) toServe).getBytes();
                 if (gzip) {
                     try {
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         GZIPOutputStream os = new GZIPOutputStream(baos);
                         os.write(toServeBytes);
                         os.close();
                         toServeBytes = baos.toByteArray();
                     } catch (IOException e) {
                         throw new RuntimeException("Failed to comress gzip", e);
                     }
                 }
             }
         }
 
         public void serve(HttpServletResponse resp, boolean head) throws IOException {
 
             if (head) {
                 // affects headers
                 gzip = false;
             }
 
             if (toServe == null) {
 
                 serve404(resp);
 
             } else if (toServe instanceof BadArg) {
 
                 serve400(resp, ((BadArg) toServe).getMessage());
 
             } else if (toServe instanceof ToServe) {
 
                 serveDefault(resp, head, (ToServe) toServe);
 
             }
 
         }
 
         private void serve404(HttpServletResponse resp) throws IOException {
 
             resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
 
         }
 
         private void serve400(HttpServletResponse resp, String msg) throws IOException {
 
             resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
 
         }
 
         private void serveDefault(HttpServletResponse resp, boolean head, ToServe toServe) throws IOException {
 
             setHeaders(resp);
             if (gzip) {
                 resp.setHeader(HEADER_CONTENT_ENCODING, VALUE_GZIP);
             }
             if (!head) {
                 OutputStream os = resp.getOutputStream();
                 os.write(toServeBytes);
             }
 
         }
 
         public void setHeaders(HttpServletResponse resp) {
 
             if (toServe != null && toServe instanceof ToServe) {
 
                 ToServe t = (ToServe) toServe;
 
                 resp.setDateHeader(HEADER_DATE, System.currentTimeMillis());
                 resp.setContentType(t.getContentType());
                 resp.setContentLength(toServeBytes.length);
                 resp.setDateHeader(HEADER_LAST_MODIFIED, t.getLastModified());
 
                 // some web caches are buggy and can't handle compressed
                 // resources, in which
                 // case we must avoid polluting that cache.
                 String cacheControl = gzip ? "private" : "";
 
                 if (t.futureExpires()) {
 
                     resp.setDateHeader(HEADER_EXPIRES, System.currentTimeMillis() + ONE_YEAR);
                     cacheControl += ", max-age=" + (ONE_YEAR / 1000);
 
                 } else {
 
                     // by setting an expiration in the past, we make extra sure
                     // all caches and browsers are treating this object as not
                     // cacheable. This will however not interfere with
                     // Last-Modified magic.
                     resp.setDateHeader(HEADER_EXPIRES, t.getLastModified());
                     cacheControl += ", max-age=0";
 
                 }
 
                 if (cacheControl.startsWith(", ")) {
                     cacheControl = cacheControl.substring(2);
                 }
                 resp.setHeader(HEADER_CACHE_CONTROL, cacheControl);
 
             }
 
         }
 
         public long getLastModified() {
 
             if (toServe != null && toServe instanceof ToServe) {
                 return ((ToServe) toServe).getLastModified();
             }
 
             return -1;
 
         }
     }
 
 }
