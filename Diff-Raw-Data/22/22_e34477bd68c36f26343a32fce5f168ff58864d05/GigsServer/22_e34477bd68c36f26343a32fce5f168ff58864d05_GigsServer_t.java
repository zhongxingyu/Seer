 package net.mightybyte.gigs;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import net.mightybyte.gigs.commands.CommandMap;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class GigsServer {
 
  public static int port = 8000;
  private Logger logger;
 
   /**
    * @param args
    */
   public static void main(String[] args) {
     PropertyConfigurator.configure("logging.props");
    
    if ( args.length > 0 ) {
      port = Integer.parseInt(args[0]);
    }
     new GigsServer();
 
   }
 
   public GigsServer() {
     ServerSocket sksListener;
     Socket skcConnection;
     ClientConnection cc;
     
    logger = Logger.getLogger("gigs");
     CommandMap.getInstance();
     try {
       logger.debug("Server started");
      sksListener = new ServerSocket(port);
       while (true) {
         skcConnection = sksListener.accept();
         cc = new ClientConnection(skcConnection);
         cc.start();
       }
     } catch (IOException e) {
       System.err.println(e.getMessage());
     }
   }
 
 }
