 package edu.hawaii.ihale.housesimulator;
 
 import org.restlet.Application;
 import org.restlet.Component;
 import org.restlet.data.Protocol;
 import org.restlet.routing.VirtualHost;
 import edu.hawaii.ihale.housesimulator.aquaponics.AquaponicsSystem;
 import edu.hawaii.ihale.housesimulator.electrical.ElectricalSystem;
 import edu.hawaii.ihale.housesimulator.hvac.HVACSystem;
 import edu.hawaii.ihale.housesimulator.lighting.LightingSystem;
 import edu.hawaii.ihale.housesimulator.photovoltaics.PhotovoltaicsSystem;
 import edu.hawaii.ihale.housesimulator.simulationtimer.SimulationTimer;
 
 /**
  * An HTTP server that provides access to simulator data via a REST interface.
  * 
  * @author Michael Cera
  * @author Anthony Kinsey
  */
 public class SimulatorServer extends Application {
 
   /**
    * Start servers running beginning on ports. Applications and their resources are specified in
    * their respective classes.
    * 
    * @throws Exception if problems occur starting up this server.
    */
   public static void runServer() throws Exception {
     // Create a component and open several ports.
     Component component = new Component();
     component.getServers().add(Protocol.HTTP, 7001);
     component.getServers().add(Protocol.HTTP, 7002);
     component.getServers().add(Protocol.HTTP, 7101);
     component.getServers().add(Protocol.HTTP, 7102);
     component.getServers().add(Protocol.HTTP, 7103);
 
     // Create virtual hosts. E-Gauge boards will be on port ranges 7001-7100, Arduino boards on port
     // ranges 7101+.
     VirtualHost host = new VirtualHost(component.getContext());
     host.setHostPort("7001");
     host.attach("/photovoltaics", new PhotovoltaicsSystem());
     component.getHosts().add(host);
 
     host = new VirtualHost(component.getContext());
     host.setHostPort("7002");
     host.attach("/electrical", new ElectricalSystem());
     component.getHosts().add(host);
 
     host = new VirtualHost(component.getContext());
     host.setHostPort("7101");
     host.attach("/aquaponics", new AquaponicsSystem());
     component.getHosts().add(host);
 
     host = new VirtualHost(component.getContext());
     host.setHostPort("7102");
     host.attach("/hvac", new HVACSystem());
     component.getHosts().add(host);
 
     host = new VirtualHost(component.getContext());
     host.setHostPort("7103");
     host.attach("/lighting", new LightingSystem());
     component.getHosts().add(host);
 
     component.start();
   }
 
   /**
    * This main method starts up a web application.
    * 
    * @param args Ignored.
    * @throws Exception If problems occur.
    */
   public static void main(String[] args) throws Exception {
     if (args.length == 0) {
       System.out.println("Must enter an integer as a valid argument.");
       System.out.println("New sensor data will be updated according to argument.");
       System.exit(0);
     }
     runServer();
     SimulationTimer.startTimer(Integer.parseInt(args[0]));
   }

 }

