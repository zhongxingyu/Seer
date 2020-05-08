 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 
 import java.net.URL;
 import java.net.URLDecoder;
 
 import java.util.Calendar;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Enumeration;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** A servlet that serves static content (gif, png, css, etc) from the
  * classpath.
  *
  * It only serves content that matches one of the specified mime types.
  *
  * This accepts the following configuration parameters:<br>
  *
  * staticContentPath: the base classpath fragment that is prepended to the
  * static content name. It must be specified or the servlet throws an
  * exception. This parameter must not start with '/'.<br>
  *
  * requestCacheContent: whether to send the cache headers to the client with an
  * expiration date in the future, or the headers that state that the content
  * should not be cached (true / false). It is false by default.<br>
  *
  * mimeType_[extension]: the mime type for the corresponding extension, for
  * example mymeType_gif = "image/gif".<br>
  *
  * debug: whether to enable debug mode or not. In debug mode, the servlet
  * attempts to load the requested content directly from the file system. This
  * makes it possible to edit the resources directly from disk and see the
  * results inmediately without a redeploy. It is false by default.<br>
  *
  * debugPrefix: in debug mode, a prefix that is prepended to the
  * staticContentPath to search for the resource as a File. A typical value
  * would be something like ../katari-style/src/main/resources. This is useful
  * in a project with the standard maven layout where the webapp is a sibling of
  * the module that contains the static resources.<br>
  */
 public class StaticContentServlet extends HttpServlet {
 
   /** The serialization version number.
    *
    * This number must change every time a new serialization incompatible change
    * is introduced in the class.
    */
   private static final long serialVersionUID = 20071005;
 
   /** The buffer size used to transfer bytes to the client.
    */
   private static final int BUFFER_SIZE = 4096;
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(
       StaticContentServlet.class);
 
   /** Provide a formatted date for setting heading information when caching
    * static content.
    */
   private final Calendar lastModified = Calendar.getInstance();
 
   /** Store the path prefix to use with static resources.
    */
   private String pathPrefix = "";
 
   /** Whether to send the client the cache header asking to cache the content
    * served by this servlet or not.
    */
   private boolean requestCacheContent = false;
 
   /** The map of resource extensions to the corresponding mime type.
    *
    * It contains entries of the form "gif", "image/gif", This is never null.
    */
   private Map<String, String> mimeTypes = new HashMap<String, String>();
 
   /** Whether debug mode is enabled.
    *
    * Initialized from the debug servlet parameter.
    */
   private boolean debug = false;
 
   /** A prefix to use to find the resources in the disk as a file.
    *
    * It is initialized from debugPrefix servlet parameter. It is never null.
    */
   private String debugPrefix = ".";
 
   /** Initializes the servlet.
    *
    * It  by setting the default packages for static resources..
    *
    * @param config The servlet configuration
    */
   public void init(final ServletConfig config) {
     log.trace("Entering init");
 
     String pathPrefixValue = config.getInitParameter("staticContentPath");
     if (pathPrefixValue == null) {
       throw new RuntimeException("You must specify staticContentPath");
     }
     pathPrefix = pathPrefixValue.trim();
     if (pathPrefix.startsWith("/")) {
       throw new RuntimeException(pathPrefixValue + " should not start with /");
     }
 
     String applyCacheInfo = config.getInitParameter("requestCacheContent");
     requestCacheContent = Boolean.valueOf(applyCacheInfo);
 
     String debugValue = config.getInitParameter("debug");
     debug = Boolean.valueOf(debugValue);
 
     String debugPrefixValue = config.getInitParameter("debugPrefix");
     if (debugPrefixValue != null) {
       debugPrefix = debugPrefixValue.trim();
     }
 
     initMimeTypes(config);
     log.trace("Leaving init");
   }
 
   /** Initializes the mime types collection from the servlet init parameters.
    *
    * This method obtains the parameter names that start with mimeType_ and adds
    * the corresponding mime type to the mime types collection.
    *
    * @param config The servlet config that contains the configuration
    * parameters. It cannot be null.
    */
   private void initMimeTypes(final ServletConfig config) {
     log.trace("Entering initMimeTypes");
     Validate.notNull(config, "The servlet config cannot be null");
     Enumeration<?> parameterNames = config.getInitParameterNames();
     while (parameterNames.hasMoreElements()) {
       String name = (String) parameterNames.nextElement();
       log.debug("Parameter: {}", name);
 
       if (name.startsWith("mimeType_")) {
         String extension = name.substring("mimeType_".length());
         log.debug("Mapped extension {} to mime {}", extension,
             config.getInitParameter(name));
         mimeTypes.put(extension, config.getInitParameter(name));
       }
     }
     log.trace("Leaving initMimeTypes");
   }
 
   /** Serves a get request.
    *
    * @param request The request object.
    *
    * @param response The response object.
    *
    * @throws IOException in case of an io error.
    *
    * @throws ServletException in case of error.
    */
   @Override
   protected void doGet(final HttpServletRequest request, final
       HttpServletResponse response) throws ServletException, IOException {
     serveStaticContent(request, response);
   }
 
   /** Serves a post request.
    *
    * @param request The request object.
    *
    * @param response The response object.
    *
    * @throws IOException in case of an io error.
    *
    * @throws ServletException in case of error.
    */
   @Override
   protected void doPost(final HttpServletRequest request, final
       HttpServletResponse response) throws ServletException, IOException {
     serveStaticContent(request, response);
   }
 
   /** Serves some static content.
    *
    * @param request The request object.
    *
    * @param response The response object.
    *
    * @throws IOException in case of an io error.
    *
    * @throws ServletException in case of error.
    *
    * TODO See if it shuold use pathInfo instead of servletPath.
    */
   private void serveStaticContent(final HttpServletRequest request, final
       HttpServletResponse response) throws ServletException, IOException {
     log.trace("Entering serveStaticContent");
     String resourcePath = getServletPath(request);
     findStaticResource(resourcePath, request, response);
     log.trace("Leaving serveStaticContent");
   }
 
   /** Locate a static resource and copy directly to the response, setting the
    * appropriate caching headers.
    *
    * A URL decoder is run on the resource path and it is configured to use the
    * UTF-8 encoding because according to the World Wide Web Consortium
    * Recommendation UTF-8 should be used and not doing so may introduce
    * incompatibilites.
    *
    * @param theName The resource name
    *
    * @param request The request
    *
    * @param response The response
    *
    * @throws IOException If anything goes wrong
    */
   private void findStaticResource(final String theName, final
       HttpServletRequest request, final HttpServletResponse response) throws
       IOException {
     log.trace("Entering findStaticResource('{}', ...)", theName);
 
     String name = URLDecoder.decode(theName, "UTF-8");
 
     // Checks if the requested resource matches a recognized content type.
     String contentType = getContentType(name);
     if (contentType == null) {
       response.sendError(HttpServletResponse.SC_NOT_FOUND);
       response.getWriter().write("<html><head><title>404</title></head>"
           + "<body>Resource not found</body></html>");
       log.trace("Leaving findStaticResource with SC_NOT_FOUND");
       response.flushBuffer();
       return;
     }
 
     // Looks for the resource.
     InputStream is = findInputStream(name, pathPrefix);
     if (is == null) {
       response.sendError(HttpServletResponse.SC_NOT_FOUND);
       log.trace("Leaving findStaticResource with SC_NOT_FOUND");
       response.getWriter().write(
           "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN'"
           + " 'http://www.w3.org/TR/html4/strict.dtd'> "
           + "<html><head><title>404</title></head>"
           + "<body>Resource not found</body></html>");
       log.trace("Leaving findStaticResource with SC_NOT_FOUND");
       response.flushBuffer();
       return;
     }
 
     Calendar cal = Calendar.getInstance();
     // check for if-modified-since, prior to any other headers
     long requestedOn = 0;
     try {
       requestedOn = request.getDateHeader("If-Modified-Since");
     } catch (Exception e) {
       log.warn("Invalid If-Modified-Since header value: '"
           + request.getHeader("If-Modified-Since") + "', ignoring");
     }
     long lastModifiedMillis = lastModified.getTimeInMillis();
     long now = cal.getTimeInMillis();
     cal.add(Calendar.DAY_OF_MONTH, 1);
     long expires = cal.getTimeInMillis();
 
     boolean notModified;
     notModified =  0 < requestedOn && requestedOn <= lastModifiedMillis;
 
     if (!debug && notModified) {
       // not modified, content is not sent - only basic headers and status
       // SC_NOT_MODIFIED
       response.setDateHeader("Expires", expires);
       response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
       is.close();
 
       log.trace("Leaving findStaticResource with SC_NOT_MODIFIED");
       return;
     }
 
     // set the content-type header
     response.setContentType(contentType);
 
    if (requestCacheContent) {
       // set heading information for caching static content
       response.setDateHeader("Date", now);
       response.setDateHeader("Expires", expires);
       response.setDateHeader("Retry-After", expires);
       response.setHeader("Cache-Control", "public");
       response.setDateHeader("Last-Modified", lastModifiedMillis);
     } else {
       response.setHeader("Cache-Control", "no-cache");
       response.setHeader("Pragma", "no-cache");
       response.setHeader("Expires", "-1");
     }
 
     try {
       copy(is, response.getOutputStream());
     } finally {
       is.close();
     }
     log.trace("Leaving findStaticResource");
   }
 
   /**
    * Determine the content type for the resource name.
    *
    * @param name The resource name.
    *
    * @return The mime type.
    */
   private String getContentType(final String name) {
     log.trace("Entering getContentType");
     int dotPosition = name.lastIndexOf('.');
     if (dotPosition != -1) {
       String contentType = mimeTypes.get(name.substring(dotPosition + 1));
       log.trace("Leaving getContentType with {}", contentType);
       return contentType;
     } else {
       log.trace("Leaving getContentType with null");
       return null;
     }
   }
 
   /**
    * Copy bytes from the input stream to the output stream.
    *
    * @param input The input stream
    * @param output The output stream
    * @throws IOException If anytSrtringhing goes wrong
    */
   private void copy(final InputStream input, final OutputStream output) throws
       IOException {
     final byte[] buffer = new byte[BUFFER_SIZE];
     int n;
     while (-1 != (n = input.read(buffer))) {
         output.write(buffer, 0, n);
     }
     output.flush();
   }
 
   /** Look for a static resource in the classpath.
    *
    * @param name The resource name. It cannot be null.
    *
    * @param packagePrefix The package prefix to use to locate the resource. It
    * cannot be null.
    *
    * @return the inputstream of the resource.
    *
    * @throws IOException If there is a problem locating the resource.
    */
   private InputStream findInputStream(final String name, final String
       packagePrefix) throws IOException {
     log.trace("Entering findInputStream('{}', '{}')", name, packagePrefix);
 
     Validate.notNull(name, "The resource name cannot be null");
     Validate.notNull(packagePrefix, "The package prefix cannot be null");
 
     String resourcePath = buildPath(packagePrefix, name);
 
     if (debug) {
       String filePath = buildPath(debugPrefix, resourcePath);
       log.debug("In debug mode, looking for file {}", filePath);
       File file = new File(filePath);
       if (file.exists()) {
         log.trace("Leaving findInputStream with a file resource");
         return new FileInputStream(file);
       }
     }
 
     log.debug("Looking for resource {}", resourcePath);
     InputStream result = getResourceAsStream(resourcePath);
 
     log.trace("Leaving findInputStream");
 
     return result;
   }
 
   /** Concatenates two file names.
    *
    * @param prefix The first component of the file name. It cannot be null.
    *
    * @param name The second component of the file name. It cannot be null.
    *
    * @return A file name of the form prefix/name with the correct number of /.
    */
   private String buildPath(final String prefix, final String name) {
     Validate.notNull(prefix, "The file component prefix cannot be null.");
     Validate.notNull(name, "The second file component cannot be null.");
 
     if (prefix.endsWith("/") && name.startsWith("/")) {
       return prefix + name.substring(1);
     } else if (prefix.endsWith("/") || name.startsWith("/")) {
       return prefix + name;
     }
     return prefix + "/" +  name;
   }
 
   /** This is a convenience method to load a resource as a stream.
    *
    * The algorithm used to find the resource is given in getResource().
    *
    * @param resourceName The name of the resource to load.
    *
    * @return Returns an input stream representing the resource, null if not
    * found.
    */
   private InputStream getResourceAsStream(final String resourceName) {
     URL url = getResource(resourceName);
     if (url == null) {
       return null;
     }
     try {
       return url.openStream();
     } catch (IOException e) {
       log.debug("Exception opening resource: " + resourceName, e);
       return null;
     }
   }
 
   /**
    * Load a given resource.
    * <p/>
    * This method will try to load the resource using the following methods (in
    * order):
    *
    * <ul>
    *
    * <li>From {@link Thread#getContextClassLoader()
    * Thread.currentThread().getContextClassLoader()}
    *
    * <li>From the {@link Class#getClassLoader() getClass().getClassLoader() }
    *
    * </ul>
    *
    * @param resourceName The name of the resource to load
    *
    * @return Returns the url of the reesource, null if not found.
    */
   private URL getResource(final String resourceName) {
     URL url = null;
 
     // Try the context class loader.
     ClassLoader contextClassLoader;
     contextClassLoader = Thread.currentThread().getContextClassLoader();
     if (null != contextClassLoader) {
       url = contextClassLoader.getResource(resourceName);
     }
 
     // Try the current class class loader if the context class loader failed.
     if (url == null) {
       url = getClass().getClassLoader().getResource(resourceName);
     }
 
     return url;
   }
 
   /**
    * Retrieves the current request servlet path.
    * Deals with differences between servlet specs (2.2 vs 2.3+)
    *
    * @param request the request
    * @return the servlet path
    */
   private String getServletPath(final HttpServletRequest request) {
     String servletPath = request.getServletPath();
 
     if (null != servletPath && !"".equals(servletPath)) {
       return servletPath;
     }
 
     String requestUri = request.getRequestURI();
     int startIndex = request.getContextPath().length();
     int endIndex = 0;
     if (request.getPathInfo() == null) {
       endIndex = requestUri.length();
     } else {
       endIndex = requestUri.lastIndexOf(request.getPathInfo());
     }
     if (startIndex > endIndex) { // this should not happen
       endIndex = startIndex;
     }
     return requestUri.substring(startIndex, endIndex);
   }
 }
 
