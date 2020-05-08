 package org.lastbamboo.common.util.mina;
 
 import org.apache.mina.common.IoServiceListener;
 
 /**
  * Generic interface for MINA servers.
  */
 public interface MinaServer
     {
 
     /**
      * Starts the MINA server.
     * @param port The port to listen on.
      */
    void start(int port);
     
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
