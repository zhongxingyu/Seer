 package de.deepamehta.plugins.webactivator;
 
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.core.service.event.PreProcessRequestListener;
 
 import com.sun.jersey.api.view.Viewable;
 // ### TODO: remove Jersey dependency. Move to JAX-RS 2.0.
 import com.sun.jersey.spi.container.ContainerRequest;
 
 import org.thymeleaf.TemplateEngine;
 import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.WebContext;
 import org.thymeleaf.resourceresolver.IResourceResolver;
 import org.thymeleaf.templateresolver.TemplateResolver;
 
 import org.osgi.framework.Bundle;
 
import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
 
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
 
    @Context private HttpServletRequest request;
    @Context private HttpServletResponse response;
    @Context private ServletContext servletContext;
 
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
        request.setAttribute(ATTR_CONTEXT, new WebContext(request, response, servletContext));
     }
 
 
 
     // ===
 
     public TemplateEngine getTemplateEngine() {
         if (templateEngine == null) {
             throw new RuntimeException("The template engine for " + this + " is not initialized. " +
                "Don't forget calling initTemplateEngine() from your plugin's init() hook.");
         }
        //
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
 
    protected void viewData(String name, Object value) {
         context().setVariable(name, value);
     }
 
     protected Viewable view(String templateName) {
         return new Viewable(templateName, context());
     }
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
    private AbstractContext context() {
        return (AbstractContext) request.getAttribute(ATTR_CONTEXT);
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
