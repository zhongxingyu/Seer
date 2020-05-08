 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Server;
 
 import PropertyReader.RegistryProperties;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author daniela
  */
 public class RMIRegistry {
 
     private static int port = RegistryProperties.getPort();
     private static Registry rmiRegistry = null;
 
     public synchronized static Registry getRmiRegistry() {
 
         //creates RMI Registry, if it already exist - get it
         if(rmiRegistry == null) {
             createRegistry();
         }
         return rmiRegistry;
     }
 
     private static void createRegistry() {
         try {
             rmiRegistry = LocateRegistry.createRegistry(port);
         } catch (RemoteException ex) {
             Logger.getLogger(RMIRegistry.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
