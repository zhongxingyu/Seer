 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Server;
 
 import PropertyReader.RegistryProperties;
 import java.io.IOException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Scanner;
 
 /**
  * Starts the AuctionServer and Shuts it down, if user hits enter
  *
  * @author daniela
  */
 public class ServerStartUp {
 
     private static AuctionServer auctionServer;
     private static String analyticsBindingName, billingBindingName;
 
 
     public static void main(String[] args) throws IOException {
         RegistryProperties r = new RegistryProperties();
         int rport = RegistryProperties.getPort();
         String rhost = RegistryProperties.getHost();
         //Registry rmiRegistry = LocateRegistry.createRegistry(rport);
         //System.out.println("registry created: host " + rhost + " port " + rport);
 
         int port = 0;
         if (args.length != 3) {
             System.out.println("Please enter the port, analyticsBindingName and"
                     + "billingBindingName  as argument on which you want the server to run");
             System.out.println(args.length);
             System.exit(0);
         }
         if (args != null && !args[0].isEmpty()) {
             try {
                 port = Integer.parseInt(args[0]);
                analyticsBindingName = args[1];
                billingBindingName = args[2];
             } catch (NumberFormatException nfe) {
                 System.err.println("PortNumber has to be a number! E.g. Server <Port>");
             }
         } else {
             System.err.println("Please enter a Port Number!");
         }
         System.out.println("AuctionServer started, hit enter to shut down.");
 
         if (port > 0) {
             auctionServer = new AuctionServer(port, analyticsBindingName, billingBindingName);
             auctionServer.start();
         }
 
         Scanner scanner = new Scanner(System.in);
 
         if (scanner.hasNextLine()) {
             auctionServer.close();
         }
         System.out.println("Bye.");
 
     }
 }
