 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.File;
 import java.io.FileInputStream;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Enumeration;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** A servlet that serves static content (gif, png, css, etc) from the
  * classpath.
  *
  * It only serves content that matches one of the specified mime types.
  *
  * In addition to the parameters for the base class, this servlet accepts the
  * following configuration parameters:<br>
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
 public class StaticContentServlet extends BaseStaticContentServlet {
 
   /** The serialization version number.
    *
    * This number must change every time a new serialization incompatible change
    * is introduced in the class.
    */
   private static final long serialVersionUID = 1;
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(
       StaticContentServlet.class);
 
   /** Store the path prefix to use with static resources.
    */
   private String pathPrefix = "";
 
   /** The map of resource extensions to the corresponding mime type.
    *
    * It contains entries of the form "gif", "image/gif", This is never null.
    */
   private Map<String, String> mimeTypes = new HashMap<String, String>();
 
   /** A prefix to use to find the resources in the disk as a file.
    *
    * It is initialized from debugPrefix servlet parameter. It is never null.
    */
   private String debugPrefix = ".";
 
   /** Initializes the servlet.
    *
    * It sets the default packages for static resources.
    *
    * @param config The servlet configuration
    */
   public void init(final ServletConfig config) throws ServletException {
     log.trace("Entering init");
     super.init(config);
 
     String pathPrefixValue = config.getInitParameter("staticContentPath");
     if (pathPrefixValue == null) {
       throw new ServletException("You must specify staticContentPath");
     }
     pathPrefix = pathPrefixValue.trim();
     if (pathPrefix.startsWith("/")) {
       throw new ServletException(pathPrefixValue + " should not start with /");
     }
 
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
 
   /** {@inheritDoc}
    */
   @Override
   protected String getContentType(final String name) {
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
 
   /** {@inheritDoc}
    */
   @Override
   protected InputStream findInputStream(final String name) throws IOException {
     log.trace("Entering findInputStream('{}')", name);
 
     Validate.notNull(name, "The resource name cannot be null");
 
     String resourcePath = buildPath(pathPrefix, name);
 
     if (isInDebugMode()) {
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
 }
 
