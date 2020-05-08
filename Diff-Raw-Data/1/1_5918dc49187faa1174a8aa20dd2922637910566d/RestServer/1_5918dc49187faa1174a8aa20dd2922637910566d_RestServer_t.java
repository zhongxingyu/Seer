 package edu.hawaii.ihale.backend.restserver;
 
 import org.restlet.Application;
 import org.restlet.Component;
 import org.restlet.Restlet;
 import org.restlet.data.Protocol;
 import org.restlet.routing.Router;
 import edu.hawaii.ihale.backend.restserver.resource.aquaponics.AquaponicsCommand;
 import edu.hawaii.ihale.backend.restserver.resource.aquaponics.AquaponicsState;
 import edu.hawaii.ihale.backend.restserver.resource.electrical.ElectricalCommand;
 import edu.hawaii.ihale.backend.restserver.resource.electrical.ElectricalState;
 import edu.hawaii.ihale.backend.restserver.resource.hvac.HvacCommand;
 import edu.hawaii.ihale.backend.restserver.resource.hvac.HvacState;
 import edu.hawaii.ihale.backend.restserver.resource.lighting.LightingCommand;
 import edu.hawaii.ihale.backend.restserver.resource.lighting.LightingState;
 import edu.hawaii.ihale.backend.restserver.resource.photovoltaics.PhotovoltaicsCommand;
 import edu.hawaii.ihale.backend.restserver.resource.photovoltaics.PhotovoltaicsState;
 
 /**
  * A simple HTTP server that provides external devices with access to the iHale system.
  * 
  * @author Philip Johnson
  * @author Michael Cera
  */
 public class RestServer extends Application {
 
   /**
    * Starts a server running on the specified port.
    * 
    * @param port The port on which this server should run.
    * @throws Exception if problems occur starting up this server.
    */
   public static void runServer(int port) throws Exception {
     Component component = new Component();
     component.getServers().add(Protocol.HTTP, port);
    component.getClients().add(Protocol.HTTP);
 
     Application application = new RestServer();
 
     String contextRoot = "";
     component.getDefaultHost().attach(contextRoot, application);
     component.start();
   }
 
   /**
    * Start the server on port 8111.
    * 
    * @param args Ignored.
    * @throws Exception If problems occur.
    */
   public static void main(String[] args) throws Exception {
     runServer(8111);
   }
 
   /**
    * Specify the dispatching restlet that maps URIs to their associated resources for processing.
    * 
    * @return A Router restlet that implements dispatching.
    */
   @Override
   public Restlet createInboundRoot() {
 
     Router router = new Router(getContext());
 
     // Attach resources to router.
     router.attach("/AQUAPONICS/state", AquaponicsState.class);
     router.attach("/AQUAPONICS/command/{command}", AquaponicsCommand.class);
 
     router.attach("/HVAC/state", HvacState.class);
     router.attach("/HVAC/command/{command}", HvacCommand.class);
 
     router.attach("/ELECTRICITY/state", ElectricalState.class);
     router.attach("/ELECTRICITY/command/{command}", ElectricalCommand.class);
 
     router.attach("/PHOTOVOLTAICS/state", PhotovoltaicsState.class);
     router.attach("/PHOTOVOLTAICS/command/{command}", PhotovoltaicsCommand.class);
     router.attach("/LIGHTING/state", LightingState.class);
     router.attach("/LIGHTING/command/{command}", LightingCommand.class);
     // Return the root router
     return router;
   }
 }
