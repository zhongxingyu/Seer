 package au.com.sensis.mobile.crf.presentation;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.servlet.HttpServletBean;
 
 import au.com.sensis.mobile.crf.config.DeploymentMetadata;
 import au.com.sensis.mobile.crf.service.Resource;
 import au.com.sensis.mobile.crf.service.ResourceResolverEngine;
 import au.com.sensis.mobile.crf.util.FileIoFacadeFactory;
 import au.com.sensis.wireless.common.volantis.devicerepository.api.Device;
 import au.com.sensis.wireless.web.mobile.MobileContext;
 
 /**
  * Maps abstract image paths ( eg. /resources/images/common/background.image) to
  * concrete, device specific image paths (eg. common/iphone/background.png),
  * then writes the image to the {@link HttpServletResponse}. The actual path
  * mapping is performed by delegating to a
  * {@link ResourceResolverEngine}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class ImageServlet extends HttpServletBean {
 
     private static final long serialVersionUID = 1L;
     private String imageServletDependenciesBeanName;
     private ImageServletDependencies imageServletDependencies;
     private String expectedRequestedResourcePathPrefix;
 
     /**
      * Lookup the {@link WebApplicationContext} and retrieve the
      * {@link ImageServletDependencies} from it.
      *
      * {@inheritDoc}
      */
     @Override
     public void init(final ServletConfig config) throws ServletException {
         super.init(config);
 
         final WebApplicationContext webApplicationContext =
                 WebApplicationContextUtils.getRequiredWebApplicationContext(config
                         .getServletContext());
         setImageServletDependencies((ImageServletDependencies) webApplicationContext
                 .getBean(getImageServletDependenciesBeanName()));
         setExpectedRequestedResourcePathPrefix(
                 getImageServletDependencies().getImagesClientPathPrefix()
                     + getImageServletDependencies().getDeploymentMetadata().getVersion()
                     + "/images/");
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doGet(final HttpServletRequest request,
             final HttpServletResponse response) throws ServletException,
             IOException {
 
         if (requestedResourcePathHasCorrectPrefix(request)) {
 
             final Resource resource =
                     getImageServletDependencies().getResourceResolverEngine()
                             .getResource(getDevice(request),
                                     getRequestedResourcePath(request));
 
             if (resource != null) {
                 setResponseHeaders(response, resource);
                 writeImageToResponse(response, resource);
             } else {
                 setFileNotFoundResponseStatus(response);
             }
 
         } else {
             throw new ServletException(
                     "Requests for abstract images should have a requestUri starting with '"
                    + getImageServletDependencies()
                            .getImagesClientPathPrefix()
                     + "'. However, requestUri is '" + request.getRequestURI()
                     + "'");
         }
 
     }
 
     private boolean requestedResourcePathHasCorrectPrefix(final HttpServletRequest req) {
         return (req.getRequestURI() != null)
                 && req.getRequestURI().startsWith(
                         getExpectedRequestedResourcePathPrefix());
     }
 
     private void setFileNotFoundResponseStatus(final HttpServletResponse resp) {
         resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
     }
 
     private void writeImageToResponse(final HttpServletResponse resp,
             final Resource resource) throws IOException {
         FileIoFacadeFactory.getFileIoFacadeSingleton()
                 .writeFileAndCloseStream(
                         resource.getNewFile(),
                         resp.getOutputStream());
     }
 
     private void setResponseHeaders(final HttpServletResponse resp,
             final Resource resource) {
         resp.setContentType(getMimeType(resource));
         resp.setContentLength(resource.getNewFileLengthAsInt());
         resp.setDateHeader("Last-Modified", resource
                 .getNewFile().lastModified());
     }
 
     private String getMimeType(final Resource resource) {
         return getServletContext().getMimeType(
                 resource.getNewFile().getPath());
     }
 
     private String getRequestedResourcePath(final HttpServletRequest req) {
         return StringUtils.substringAfter(req.getRequestURI(),
                 getExpectedRequestedResourcePathPrefix());
     }
 
     private Device getDevice(final HttpServletRequest req) {
         final MobileContext context =
                 (MobileContext) req.getSession().getAttribute(
                         MobileContext.MOBILE_CONTEXT_KEY);
         return context.getDevice();
     }
 
     /**
      * @param beanName
      *            Name of the {@link ImageServletDependencies} bean to
      *            obtain from the Spring context.
      */
     public void setImageServletDependenciesBeanName(
             final String beanName) {
         imageServletDependenciesBeanName = beanName;
     }
 
     private String getImageServletDependenciesBeanName() {
         return imageServletDependenciesBeanName;
     }
 
     private ImageServletDependencies getImageServletDependencies() {
         return imageServletDependencies;
     }
 
     private void setImageServletDependencies(
             final ImageServletDependencies imageServletDependencies) {
         this.imageServletDependencies = imageServletDependencies;
     }
 
     /**
      * @return the expectedRequestedResourcePathPrefix
      */
     private String getExpectedRequestedResourcePathPrefix() {
         return expectedRequestedResourcePathPrefix;
     }
 
     /**
      * @param expectedRequestedResourcePathPrefix
      *            the expectedRequestedResourcePathPrefix to set
      */
     private void setExpectedRequestedResourcePathPrefix(
             final String expectedRequestedResourcePathPrefix) {
         this.expectedRequestedResourcePathPrefix = expectedRequestedResourcePathPrefix;
     }
 
     /**
      * Simple memento encapsulating the singleton collaborators of this
      * {@link ImageServlet} An instance of this memento will be programmatically
      * retrieved from the Spring context.
      */
     public static class ImageServletDependencies {
 
         private final ResourceResolverEngine resourceResolverEngine;
         private final String imagesClientPathPrefix;
         private final DeploymentMetadata deploymentMetadata;
 
         /**
          * Constructor.
          *
          * @param resourceResolverEngine
          *            {@link ResourceResolverEngine} to use
          *            to resolve abstract image paths to real paths.
          * @param imagesClientPathPrefix
          *            Client side prefix for all image paths.
          *            eg. "/resources/images".
          * @param deploymentMetadata {@link DeploymentMetadata}.
          */
         public ImageServletDependencies(
                 final ResourceResolverEngine resourceResolverEngine,
                 final String imagesClientPathPrefix,
                 final DeploymentMetadata deploymentMetadata) {
             this.resourceResolverEngine = resourceResolverEngine;
             this.imagesClientPathPrefix = imagesClientPathPrefix;
             this.deploymentMetadata = deploymentMetadata;
         }
 
         /**
          * @return resourceResolverEngine
          *         {@link ResourceResolverEngine} to use to
          *         resolve abstract image paths to real paths.
          */
         public ResourceResolverEngine getResourceResolverEngine() {
             return resourceResolverEngine;
         }
 
         /**
          * @return imagesClientPathPrefix Client side prefix for all image paths.
          *         eg. "/resources/images".
          */
         public String getImagesClientPathPrefix() {
             return imagesClientPathPrefix;
         }
 
         /**
          * @return the deploymentMetadata
          */
         public DeploymentMetadata getDeploymentMetadata() {
             return deploymentMetadata;
         }
     }
 }
