 package net.mightybyte.gigs;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import net.mightybyte.gigs.commands.CommandMap;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class GigsServer {
 
  private static Logger logger = Logger.getLogger("gigs");
  public final int PORT = 8000;
 
   /**
    * @param args
    */
   public static void main(String[] args) {
     PropertyConfigurator.configure("logging.props");
     new GigsServer();
 
   }
 
   public GigsServer() {
     ServerSocket sksListener;
     Socket skcConnection;
     ClientConnection cc;
     
     CommandMap.getInstance();
     try {
       logger.debug("Server started");
      sksListener = new ServerSocket(PORT);
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
