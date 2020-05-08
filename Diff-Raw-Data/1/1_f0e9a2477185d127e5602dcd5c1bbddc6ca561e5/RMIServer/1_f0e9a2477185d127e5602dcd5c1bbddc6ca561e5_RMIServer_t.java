 package svm.rmi.server;
 
 import svm.rmi.abstraction.factory.IRMIControllerFactory;
 import svm.rmi.implementation.rmiControllerFactory.RMIControllerFactory;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 
 /**
  * Projectteam : Team C
  * Date: 01.11.12
  */
 public class RMIServer {
     public static void main(String[] args) {
         try {
 
             LocateRegistry.createRegistry(1099);
 
             //Setzen der Codebase     und der Policy
             System.setProperty("java.rmi.server.codebase", RMIServer.class
                     .getProtectionDomain().getCodeSource().getLocation().toString());
             String help = RMIServer.class.getProtectionDomain().getCodeSource().getLocation().toString();
            System.out.println("CodeBase: " + help);
             //Hole Argument (IP)
             //String ip = args[0];
             String ip = "127.0.0.1";
             //ip="172.16.63.174";
             //Erzeugen eines SayHello Objektes
             IRMIControllerFactory factory = RMIControllerFactory.getInstance();
 
 
             //Binden des Objekts
             Naming.rebind("rmi://" + ip + ":1099/RMI", factory);
             System.out.println("Path: rmi://" + ip + ":1099/RMI");
             System.out.println("RMIFactory bound in registry");
             System.out.println("Server ist running");
 
         } catch (RemoteException e) {
             System.out.println("RMI Server error: " + e.getMessage());
             e.printStackTrace();
         } catch (MalformedURLException e) {
             System.out.println("RMI Server error: " + e.getMessage());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 }
