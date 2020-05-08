 package edu.hawaii.ihale.housesimulator;
 
 import java.io.File;
import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Scanner;
 import org.restlet.Application;
 import org.restlet.Component;
 import org.restlet.data.Protocol;
 import org.restlet.routing.VirtualHost;
 import edu.hawaii.ihale.housesimulator.aquaponics.AquaponicsSystem;
 import edu.hawaii.ihale.housesimulator.electrical.ElectricalSystem;
 import edu.hawaii.ihale.housesimulator.hvac.HVACSystem;
 import edu.hawaii.ihale.housesimulator.lighting.bathroom.LightingBathroomSystem;
 import edu.hawaii.ihale.housesimulator.lighting.dining.LightingDiningSystem;
 import edu.hawaii.ihale.housesimulator.lighting.kitchen.LightingKitchenSystem;
 import edu.hawaii.ihale.housesimulator.lighting.living.LightingLivingSystem;
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
     component.getServers().add(Protocol.HTTP, 7104);
     component.getServers().add(Protocol.HTTP, 7105);
     component.getServers().add(Protocol.HTTP, 7106);
 
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
 
     
     String lighting = "/lighting"; // To satisfy PMD
     
     host = new VirtualHost(component.getContext());
     host.setHostPort("7103");
     host.attach(lighting, new LightingLivingSystem());
     component.getHosts().add(host);
 
     host = new VirtualHost(component.getContext());
     host.setHostPort("7104");
     host.attach(lighting, new LightingDiningSystem());
     component.getHosts().add(host);
     
     host = new VirtualHost(component.getContext());
     host.setHostPort("7105");
     host.attach(lighting, new LightingKitchenSystem());
     component.getHosts().add(host);
     
     host = new VirtualHost(component.getContext());
     host.setHostPort("7106");
     host.attach(lighting, new LightingBathroomSystem());
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
     if (args.length == 2 && "-stepinterval".equalsIgnoreCase(args[0])) {
 
       // Get the users home directory and establish the ".ihale" directory
       File theDir = new File(System.getProperty("user.home"), ".ihale");
       // Create the properties file in the ".ihale" directory
       File propFile = new File(theDir, "device-urls.properties");
       // Create the properties object to write to file.
       Properties prop = new Properties();
 
       // System URI's
       String aquaponics = "http://localhost:7101/";
       String hvac = "http://localhost:7102/";
       String lightingLiving = "http://localhost:7103/";
       String lightingDining = "http://localhost:7104/";
       String lightingKitchen = "http://localhost:7105/";
       String lightingBathroom = "http://localhost:7106/";
       String pv = "http://localhost:7001/";
       String electrical = "http://localhost:7002/";
       
       // Set the properties value.
       prop.setProperty("aquaponics-state", aquaponics);
       prop.setProperty("aquaponics-control", aquaponics);
       prop.setProperty("hvac-state", hvac);
       prop.setProperty("hvac-control", hvac);
       prop.setProperty("lighting-living-state", lightingLiving);
       prop.setProperty("lighting-living-control", lightingLiving);
       prop.setProperty("lighting-dining-state", lightingDining);
       prop.setProperty("lighting-dining-control", lightingDining);
       prop.setProperty("lighting-kitchen-state", lightingKitchen);
       prop.setProperty("lighting-kitchen-control", lightingKitchen);
       prop.setProperty("lighting-bathroom-state", lightingBathroom);
       prop.setProperty("lighting-bathroom-control", lightingBathroom);
       prop.setProperty("pv-state", pv);
       prop.setProperty("electrical-state", electrical);
 
       // Check if the properties file exists or not.
       if (propFile.exists()) {
         System.out.println("File already exists: " + propFile.getAbsolutePath());
 
         // Initialize scanner and input string.
         Scanner sc;
         String input = "";
 
         // Keep asking user if they want to overwrite the file if they don't say y or n.
         do {
 
           System.out.println("Would you like to overwrite this properties file? y/n");
           sc = new Scanner(System.in);
           input = sc.next();
 
           // Overwrite the file.
           if ("y".equalsIgnoreCase(input)) {
             // Try to store the properties object in the properties file.
             try {
               System.out.println("Overwriting properties file: " + propFile.getAbsolutePath());
               prop.store(new FileOutputStream(propFile), null);
             }
             catch (IOException ex) {
               ex.printStackTrace();
             }
           }
           // Leave existing file.
           else if ("n".equalsIgnoreCase(input)) {
             System.out.println("Starting simulation using exisiting properties file.");
           }
 
         }
         while (!"y".equalsIgnoreCase(input) && !"n".equalsIgnoreCase(input));
 
       }
       else {
 
         System.out.println("Creating properties file: " + propFile.getAbsolutePath());
         // Create the Directory.
         if (theDir.mkdir()) {
           // Create the Properties file.
           if (propFile.createNewFile()) {
             // Try to store the properties object in the properties file.
             try {
               prop.store(new FileOutputStream(propFile), null);
             }
             catch (IOException ex) {
               ex.printStackTrace();
             }
           }
           else {
             System.out.println("Failed to create properties file: " + propFile.getAbsolutePath());
             System.exit(1);
           }
         }
         else {
           System.out.println("Failed to create directory: " + theDir.getAbsolutePath());
           System.exit(1);
         }
 
       }
 
       runServer();
       SimulationTimer.startTimer(Integer.parseInt(args[1]));
     }
     else {
       System.out.println("Usage: java -jar <jar filename> -stepinterval N");
       System.out.println("Where N is the step interval value, in seconds.");
       System.out.println("New sensor data will be updated every N seconds.");
       System.exit(0);
     }
   }
 }
