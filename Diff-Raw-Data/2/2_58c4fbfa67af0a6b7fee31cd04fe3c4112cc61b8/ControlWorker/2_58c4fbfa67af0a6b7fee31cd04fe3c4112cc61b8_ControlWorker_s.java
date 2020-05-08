 package cpsc471.ftp.server.control;
 
 import org.apache.log4j.Logger;
 
 import java.net.Socket;
 
 /**
  * Handles client requests
  */
 public class ControlWorker implements Runnable {
 
     private Logger logger = Logger.getLogger(ControlWorker.class);
 
     /**
     * Create a worker to handle a connect
      * @param socket a socket representing the connection for this
      *               worker to handle
      */
     public ControlWorker(Socket socket) {
 
     }
 
     @Override
     public void run() {
         //To change body of implemented methods use File | Settings | File Templates.
         logger.info("Servicing connection");
     }
 }
