 package edu.hm.dako.echo.server.rmi;
 
 import java.rmi.registry.Registry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.PropertyConfigurator;
 
 import edu.hm.dako.echo.server.EchoServer;
 
 /**
  * Startet das RMI-Registry lokal und den RMI-Echo-Server.
  *
  * @author Mandl
  */
 public class RMIEchoServerImpl implements EchoServer {
     private static Log log = LogFactory.getLog(RMIEchoServerRemoteImpl.class);
     public static final String RMI_ECHO_SERVER_RMI_NAME = "RMIEchoServer";
     private Registry rmiRegistry;
     private final int port;
 
     public RMIEchoServerImpl(int port) {
         this.port = port;
     }
 
     @Override
     public void start() {
         PropertyConfigurator.configureAndWatch("log4j.server.properties", 60 * 1000);
         // RMI Registry lokal starten
         try {       	 
             rmiRegistry = java.rmi.registry.LocateRegistry.createRegistry(port); 
             log.debug("RMI registry bereit");
 
             //DONE Studienarbeit: RMI-Echo-Server-Implementierung instanziieren und rebind durchfuehren
             rmiRegistry.rebind(RMI_ECHO_SERVER_RMI_NAME, new RMIEchoServerRemoteImpl());
                         
             
             System.out.println("EchoServer gestartet, wartet auf Clients...");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void stop() throws Exception {
        rmiRegistry.unbind(RMI_ECHO_SERVER_RMI_NAME);
     }
 }
