package eu.stratuslab.storage.main;
 
 import java.util.logging.Logger;
 
 import org.restlet.Application;
 import org.restlet.Restlet;
 import org.restlet.resource.Directory;
 import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
 
 import eu.stratuslab.storage.disk.resources.DiskResource;
 
 public class PersistentDiskApplication extends Application {
 
     protected Logger logger = getLogger();
 
     public PersistentDiskApplication() {
 
         setName("StratusLab Persistent Disk Server");
         setDescription("StratusLab server for persistent disk storage.");
         setOwner("StratusLab");
         setAuthor("Charles Loomis");
 
         getTunnelService().setUserAgentTunnel(true);
 
     }
 
     @Override
     public Restlet createInboundRoot() {
 
         // Create a router Restlet that defines routes.
         Router router = new Router(getContext());
 
         Directory indexDir = new Directory(getContext(), "war:///");
         indexDir.setNegotiatingContent(false);
         indexDir.setIndexName("index.html");
 
         // Defines a route for the resource "list of metadata entries"
         router.attach("/disk", DiskResource.class);
 
         router.attach("/", indexDir);
 
         return router;
     }
 
 }
