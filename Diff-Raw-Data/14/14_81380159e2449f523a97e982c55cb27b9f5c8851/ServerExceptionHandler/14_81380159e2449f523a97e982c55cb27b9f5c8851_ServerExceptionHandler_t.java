 package ping.pong.net.server;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import java.io.EOFException;
 import java.net.BindException;
 import java.net.ConnectException;
 import java.net.NoRouteToHostException;
 import java.net.PortUnreachableException;
 import java.net.SocketException;
 
 import javax.net.ssl.SSLException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author mfullen
  */
 public class ServerExceptionHandler
 {
     public static final Logger logger = LoggerFactory.getLogger(ServerExceptionHandler.class);
 
     /**
      *
      * @param ex
      */
     public static void handleException(final Exception ex, Logger logger)
     {
         //    BindException, ConnectException, NoRouteToHostException, PortUnreachableException
         if (ex instanceof SocketException)
         {
             if ((ex instanceof BindException) || ex.getMessage().contains("JVM_Bind"))
             {
                 logger.error("Socket already in use. Try starting the server on another socket.", ex);
             }
             else if (ex instanceof ConnectException)
             {
                 logger.error("ConnectException.", ex);
             }
             else if (ex instanceof NoRouteToHostException)
             {
                 logger.error("NoRouteToHostException.", ex);
             }
             else if (ex instanceof PortUnreachableException)
             {
                 logger.error("PortUnreachableException.", ex);
             }
             else if (ex.getMessage().contains("Connection reset"))
             {
                 logger.error("Connection reset: Client Closed Connection foricibly.", ex);
             }
             else if (ex.getMessage().contains("socket closed"))
             {
                 logger.trace("Socket Closed. Server Shutdown");
             }
             else
             {
                 logger.error("Unknown SocketException Error", ex);
             }
         }
         else if (ex instanceof SSLException)
         {
             logger.error("SSL Error: ", ex);
         }
        else if (ex instanceof EOFException)
        {
            logger.error("End of client. Client must of disconnected", ex);
        }
         else
         {
            logger.error("Unknown Error", ex);
         }
     }
 }
