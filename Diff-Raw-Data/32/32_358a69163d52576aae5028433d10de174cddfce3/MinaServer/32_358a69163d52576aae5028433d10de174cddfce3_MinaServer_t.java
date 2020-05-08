 package org.lastbamboo.common.util.mina;
 
import java.io.IOException;

 import org.apache.mina.common.IoServiceListener;
 
 /**
  * Generic interface for MINA servers.
  */
 public interface MinaServer
     {
 
     /**
      * Starts the MINA server.
     * @param localPort The port to listen on.
     * 
     * @throws IOException If we cannot bind to the port.
      */
    void start(int localPort) throws IOException;
     
     /**
      * Stops the MINA server.
      */
     void stop();
     
     /**
      * Adds the specified {@link IoServiceListener} to the server.
      * 
      * @param serviceListener The listener to add.
      */
     void addIoServiceListener(IoServiceListener serviceListener);
     }
