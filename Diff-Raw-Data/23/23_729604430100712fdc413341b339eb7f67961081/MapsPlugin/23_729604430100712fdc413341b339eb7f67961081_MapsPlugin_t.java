
 package de.kiezatlas.plugins.view.maps;
 
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.core.service.ClientState;
 import java.io.InputStream;
import java.util.logging.Logger;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 
/**
 * Kiezatlas New (Mobile) Maps 1.0
 *
 * Depending on Kiezatlas 2.1.1 and DeepaMehta 4.1.
 *
 * @since 26. September 2013
 * @author Malte Reißig <malte@mikromedia.de>, 2013
 *
 */

 @Path("/")
 @Produces(MediaType.TEXT_HTML)
 public class MapsPlugin extends PluginActivator {
 
    private Logger log = Logger.getLogger(getClass().getName());

     @GET
     @Produces(MediaType.TEXT_HTML)
     public InputStream getStartView(@HeaderParam("Cookie") ClientState clientState) {
         return invokeStartView();
     }
 
     // ------------------------------------------------------------------------------------------------ Private Methods
 
     private InputStream invokeStartView() {
         try {
             return dms.getPlugin("de.kiezatlas.new-maps").getResourceAsStream("web/script/index.html");
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
 }
