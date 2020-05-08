 package au.com.sensis.mobile.web.component.core.bundle;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.springframework.web.servlet.mvc.LastModified;
 
 import au.com.sensis.wireless.web.common.exception.ApplicationRuntimeException;
 import au.com.sensis.wireless.web.common.validation.Validatable;
 import au.com.sensis.wireless.web.common.validation.ValidatableUtils;
 
 /**
  * {@link org.springframework.web.servlet.mvc.Controller} base class that loads
  * and returns resource bundles by delegating to
  * {@link #getResourceBundleLoader()}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public abstract class AbstractResourceBundleLoaderController extends
         AbstractController implements LastModified, Validatable {
 
    private static final String URL_EMBEDDED_COOKIE_PREFIX = ";";
 
     /**
      * Name of the session attribute that requests that the <b>client</b> side
      * cache be bypassed. ie.
      * {@link LastModified#getLastModified(HttpServletRequest)} should return
      * -1. This parameter is only handled if
      * {@link FeatureEnablementRegistry#isBypassClientCacheEnabled()} is true.
      */
     public static final String BYPASS_CLIENT_CACHE_SESSION_KEY =
             AbstractResourceBundleLoaderController.class.getName()
                     + ".bypassClientCache";
 
     /**
      * Value returned by {@link #getLastModified(HttpServletRequest)} when the
      * modification time is unknown or cannot be determined.
      */
     public static final int LAST_MODIFIED_UNKNOWN = -1;
 
     private ResourceBundleLoader resourceBundleLoader;
 
     private String resourceNameRequestUriPrefix;
 
     private FeatureEnablementRegistry featureEnablementRegistry;
 
     /**
      * {@inheritDoc}
      */
     public void validateState() throws ApplicationRuntimeException {
         ValidatableUtils.validateObjectIsNotNull(getResourceBundleLoader(),
                 "resourceBundleLoader");
         ValidatableUtils.validateStringIsNotBlank(
                 getResourceNameRequestUriPrefix(),
                 "resourceNameRequestUriPrefix");
         ValidatableUtils.validateObjectIsNotNull(getFeatureEnablementRegistry(),
             "featureEnablementRegistry");
     }
 
     /**
      * Subclasses should override this method to define their own logger.
      *
      * @return Subclasses should override this method to define their own
      *         logger.
      */
     protected abstract Logger getLogger();
 
     /**
      * @param request
      *            {@link HttpServletRequest}
      * @return true if {@link #isAllowBypassClientCacheSessionKey()} is true and
      *         the {@link #BYPASS_CLIENT_CACHE_SESSION_KEY} session key is set.
      */
     protected final boolean isByassClientCacheRequested(
             final HttpServletRequest request) {
         if (getFeatureEnablementRegistry().isBypassClientCacheEnabled()) {
             final Object bypassClientCacheSessionValue =
                     request.getSession().getAttribute(
                             BYPASS_CLIENT_CACHE_SESSION_KEY);
             return (bypassClientCacheSessionValue != null)
                     && Boolean.TRUE.equals(bypassClientCacheSessionValue);
         } else {
             return false;
         }
 
     }
 
     /**
      * Extracts the resource name requested by extracting all text after
      * {@link #getResourceNameRequestUriPrefix()} from the
      * {@link HttpServletRequest#getRequestURI()}.
      *
      * @param request
      *            {@link HttpServletRequest}
      * @return Resource name requested,
      */
     protected final String extractResourceNameRequested(
             final HttpServletRequest request) {
         final String resourceRequested =
                 StringUtils.substringAfterLast(request.getRequestURI(),
                         getResourceNameRequestUriPrefix());
         return StringUtils.defaultString(
                 StringUtils.substringBefore(resourceRequested,
                        URL_EMBEDDED_COOKIE_PREFIX));
 
     }
 
     /**
      * @param response
      *            {@link HttpServletResponse}
      * @param inputStream
      *            {@link InputStream} to be written to the response.
      * @throws IOException
      *             Thrown if an error occurs.
      */
     protected final void writeInputStreamToResponse(
             final HttpServletResponse response, final InputStream inputStream)
             throws IOException {
 
         try {
             final PrintWriter printWriter = response.getWriter();
             printWriter.println(IOUtils.toString(inputStream));
         } finally {
             if (inputStream != null) {
                 inputStream.close();
             }
         }
     }
 
     /**
      * @return the resourceBundleLoader
      */
     public ResourceBundleLoader getResourceBundleLoader() {
         return resourceBundleLoader;
     }
 
     /**
      * @param resourceBundleLoader
      *            the resourceBundleLoader to set
      */
     public void setResourceBundleLoader(
             final ResourceBundleLoader resourceBundleLoader) {
         this.resourceBundleLoader = resourceBundleLoader;
     }
 
     /**
      * @return the resourceNameRequestUriPrefix
      */
     public String getResourceNameRequestUriPrefix() {
         return resourceNameRequestUriPrefix;
     }
 
     /**
      * @param resourceNameRequestUriPrefix
      *            the resourceNameRequestUriPrefix to set
      */
     public void setResourceNameRequestUriPrefix(
             final String resourceNameRequestUriPrefix) {
         this.resourceNameRequestUriPrefix = resourceNameRequestUriPrefix;
     }
 
     /**
      * @return the featureEnablementRegistry
      */
     public FeatureEnablementRegistry getFeatureEnablementRegistry() {
         return featureEnablementRegistry;
     }
 
     /**
      * @param featureEnablementRegistry the featureEnablementRegistry to set
      */
     public void setFeatureEnablementRegistry(
             final FeatureEnablementRegistry featureEnablementRegistry) {
         this.featureEnablementRegistry = featureEnablementRegistry;
     }
 
 }
