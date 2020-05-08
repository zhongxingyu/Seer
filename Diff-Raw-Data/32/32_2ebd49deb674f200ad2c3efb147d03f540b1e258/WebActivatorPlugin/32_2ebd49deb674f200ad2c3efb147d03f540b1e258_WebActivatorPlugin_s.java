 package de.deepamehta.plugins.webactivator;
 
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.core.service.event.PreProcessRequestListener;
 
 import com.sun.jersey.api.view.Viewable;
 // ### TODO: remove Jersey dependency. Move to JAX-RS 2.0.
 import com.sun.jersey.spi.container.ContainerRequest;
 
 import org.thymeleaf.TemplateEngine;
 import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.Context;
 import org.thymeleaf.resourceresolver.IResourceResolver;
 import org.thymeleaf.templateresolver.TemplateResolver;
 
 import org.osgi.framework.Bundle;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Path;
 
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 
 
 // Note: although no REST service is provided the plugin is annotated as a root resource class.
 // Otherwise we can't receive JAX-RS context injections (HttpServletRequest).
 @Path("/webactivator")
 public class WebActivatorPlugin extends PluginActivator implements PreProcessRequestListener {
 
     // ------------------------------------------------------------------------------------------------------- Constants
 
     private static String ATTR_CONTEXT = "de.deepamehta.plugins.webactivator.Context";
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private TemplateEngine templateEngine;
 
    @javax.ws.rs.core.Context
    private HttpServletRequest request;
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // ********************************
     // *** Listener Implementations ***
     // ********************************
 
 
 
     @Override
     public void preProcessRequest(ContainerRequest req) {
         // Note: we don't operate on the passed ContainerRequest but on the injected HttpServletRequest.
         // At this spot we could use req.getProperties().put(..) instead of request.setAttribute(..) but at the other
         // spots (setViewModel() and view()) we could not inject a ContainerRequest but only a javax.ws.rs.core.Request
         // and Request does not provide a getProperties() method. And we neither can cast a Request into a
         // ContainerRequest as the injected Request is actually a proxy object (in order to deal with multi-threading).
        request.setAttribute(ATTR_CONTEXT, new Context());
     }
 
 
 
     // ===
 
     public TemplateEngine getTemplateEngine() {
         if (templateEngine == null) {
             throw new RuntimeException("The template engine for " + this + " is not initialized. " +
                "Don't forget calling initTemplateEngine() from the plugin's init() hook.");
         }
         return templateEngine;
     }
 
 
 
     // ----------------------------------------------------------------------------------------------- Protected Methods
 
     protected void initTemplateEngine() {
         TemplateResolver templateResolver = new TemplateResolver();
         templateResolver.setResourceResolver(new BundleResourceResolver(bundle));
         //
         templateEngine = new TemplateEngine();
         templateEngine.setTemplateResolver(templateResolver);
     }
 
    protected void setViewModel(String name, Object value) {
         context().setVariable(name, value);
     }
 
     protected Viewable view(String templateName) {
         return new Viewable(templateName, context());
     }
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
    private Context context() {
        return (Context) request.getAttribute(ATTR_CONTEXT);
     }
 
     // --------------------------------------------------------------------------------------------------- Inner Classes
 
     private class BundleResourceResolver implements IResourceResolver {
 
         private Bundle bundle;
 
         private BundleResourceResolver(Bundle bundle) {
             this.bundle = bundle;
         }
 
         @Override
         public String getName() {
             return "BundleResourceResolver";
         }
 
         @Override
         public InputStream getResourceAsStream(TemplateProcessingParameters params, String resourceName) {
             try {
                 return bundle.getResource(resourceName).openStream();
             } catch (Exception e) {
                 return null;
             }
         }
     }
 }
