 package org.nr.roulette;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.glassfish.grizzly.http.server.HttpServer;
 import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
 import org.glassfish.jersey.jettison.JettisonFeature;
 import org.glassfish.jersey.server.ResourceConfig;
 
 import casino.Croupie;
 
 /**
  * Utility class which can create {@link org.glassfish.jersey.server.ApplicationHandler} instance and provides support
  * for running this sample from command line.
  *
  */
 public class App {
 
     private static Croupie croupie = Croupie.newInstance();
     private static String BASE_URI = "http://localhost:portnumber/casino/";
     private static boolean isManual = false;
     private static Integer portnumber = 9999;
     
 
     public static void main(String[] args) {
         
         // args[0] application port
         // args[1] mode: "true" -> isManual, anyOther or missing -> not manual
         // args[2] number for setManualSpin
         
         // Checking and setting port number argument
        System.out.println("X1");
         try {
             if (args.length > 0)
             {
                 try {
                     portnumber = Integer.valueOf(args[2]);
                 } catch (NumberFormatException e) {
                     System.out.println("Cannot parse the first parameter (port number). It should be an integer if given. The default port 9999 will be used");
                 }
            }
            BASE_URI = BASE_URI.replaceFirst("portnumber", portnumber.toString());
            System.out.println("X2");
             
             
             // Checking and setting "manual mode" argument and "number" argument 
             if (args.length > 1)
             {
                 isManual = Boolean.valueOf(args[1]);
                 if (isManual)
                 {
                     Integer number = null;
                     if (args.length > 2)
                     {
                         try {
                             number = Integer.valueOf(args[2]);
                         } catch (NumberFormatException e) {
                             System.out.println("The third parameter should be an integer if given. Terminating");
                             System.exit(1);
                         }
                         
                     }
                     croupie.setManualSpin(true, number);
                     System.out.println("This Roulette application will run in manual mode without automatical spins");
                 }
             }
            System.out.println("X3");
            System.out.println("Listening URI is " + BASE_URI);
             final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), createApp());
             
             
             if (!isManual)
             {
             // Concurrent timer implementation
             final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
             service.scheduleAtFixedRate(new Runnable() {
                 @Override
                 public void run() {
                     if (!server.isStarted())
                     {
                         // If the HTTP server is not running kill the Executor thread also
                         service.shutdown();
                     }
                     croupie.performGameMove();
                     
                 }
             }, 0, 5, TimeUnit.SECONDS);
             }
            System.out.println("X4");
             
             
             System.out.println("Listening URI is " + BASE_URI);
             System.out.println("Rules can be found on " + BASE_URI + "rules");
             System.out.println("Game stats can be obtained from " + BASE_URI + "stats");
             System.out.println("The WADL file can be found on " + BASE_URI + "application.wadl");
             System.out.println(String.format("Application started.%nHit enter to stop it..."));
             System.in.read();
             server.stop();
         } catch (IOException ex) {
             Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static ResourceConfig createApp() {
         return new ResourceConfig().
                 register(new JettisonFeature()).
                 packages("org.nr.roulette");
     }
 }
