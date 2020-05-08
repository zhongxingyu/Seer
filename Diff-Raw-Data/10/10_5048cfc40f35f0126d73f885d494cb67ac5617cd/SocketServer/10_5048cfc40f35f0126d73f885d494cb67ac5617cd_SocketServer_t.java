 package org.nebulostore.communication.jxta;
 
 import java.io.DataInput;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.text.MessageFormat;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import net.jxta.document.AdvertisementFactory;
 import net.jxta.impl.id.CBID.PipeID;
 import net.jxta.peergroup.PeerGroup;
 import net.jxta.pipe.PipeService;
 import net.jxta.protocol.PipeAdvertisement;
 import net.jxta.socket.JxtaServerSocket;
 
 import org.apache.log4j.Logger;
 import org.nebulostore.communication.jxta.utils.IDFactory;
 
 /**
  * @author Marcin Walas
  */
 public class SocketServer implements Runnable {
 
   private static Logger logger_ = Logger.getLogger(SocketServer.class);
 
   public static final String SOCKETIDSTR = "socketServerAdv";
   private transient PeerGroup netPeerGroup_;
   private final ExecutorService pool_;
 
   public SocketServer(PeerGroup netPeerGroup) {
     pool_ = Executors.newCachedThreadPool();
     this.netPeerGroup_ = netPeerGroup;
   }
 
   public PipeAdvertisement createSocketAdvertisement() {
     PipeID socketID = IDFactory.createPipeID(netPeerGroup_.getPeerGroupID(),
         SOCKETIDSTR);
 
     PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory
         .newAdvertisement(PipeAdvertisement.getAdvertisementType());
 
     advertisement.setPipeID(socketID);
     advertisement.setType(PipeService.UnicastType);
     advertisement.setName("Nebulostore socket.");
 
     return advertisement;
   }
 
   /**
    * Wait for connections.
    */
   @Override
   public void run() {
 
     logger_.debug("Starting ServerSocket");
     JxtaServerSocket serverSocket = null;
     try {
       serverSocket = new JxtaServerSocket(netPeerGroup_,
           createSocketAdvertisement(), 10);
       serverSocket.setSoTimeout(0);
     } catch (IOException e) {
       logger_.error(e);
       return;
     }
 
     while (true) {
       try {
         logger_.debug("Waiting for connections");
         Socket socket = serverSocket.accept();
         if (socket != null) {
           logger_.debug("New socket connection accepted");
           pool_.execute(new ConnectionHandler(socket));
         }
       } catch (IOException e) {
         logger_.error("Exception: ", e);
       }
     }
   }
 
   /**
    * @author Marcin Walas
    */
   private class ConnectionHandler implements Runnable {
     Socket socket_;
 
     ConnectionHandler(Socket socket) {
       socket_ = socket;
     }
 
     /**
      * Sends data over socket.
      * 
      * @param socket
      *          the socket
      */
     private void sendAndReceiveData(Socket socket) {
       try {
 
         long start = System.currentTimeMillis();
 
         // get the socket output stream
         OutputStream out = socket.getOutputStream();
         // get the socket input stream
         InputStream in = socket.getInputStream();
         DataInput dis = new DataInputStream(in);
 
         long iterations = dis.readLong();
         int size = dis.readInt();
         long total = iterations * size * 2L;
         long current = 0;
 
         logger_.debug(MessageFormat.format("Sending/Receiving {0} bytes.",
             total));
         while (current < iterations) {
           byte[] buf = new byte[size];
           dis.readFully(buf);
           out.write(buf);
           out.flush();
           current++;
         }
 
         out.close();
         in.close();
 
         long finish = System.currentTimeMillis();
         long elapsed = finish - start;
         logger_.debug(MessageFormat.format(
             "EOT. Received {0} bytes in {1} ms. Throughput = {2} KB/sec.",
             total, elapsed, (total / elapsed) * 1000 / 1024));
         socket.close();
         logger_.debug("Connection closed");
       } catch (IOException ie) {
         ie.printStackTrace();
       }
     }
 
     @Override
     public void run() {
      //newFixedThreadPool(10);
     }
   }
 
 }
