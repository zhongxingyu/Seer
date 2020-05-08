 import java.rmi.*;
 import java.rmi.server.*;
 import java.rmi.registry.LocateRegistry;
 
 public class Server {
 
     public static void main(String[] args) {
         if( args.length != 1 ) {
             System.out.println("Usage: java <PORT>");
             System.exit(-1);
         }
         try {
            LocateRegistry.createRegistry(Integer.parseInt(args[0]));
             Naming.rebind("Remote", new RemoteListener());
         } catch(Exception e) {
             e.printStackTrace();
         }
         
     }
 
 }
