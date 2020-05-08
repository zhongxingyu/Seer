 /*
  * Copyright (C) 2011  Francois Forster
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.tcpdelay;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.DelayQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Class taking request (connection and data) and processing them.
  */
 public class TCPDataReader implements Runnable {
     private static Logger LOGGER = Logger.getLogger(TCPDataReader.class.getName());
 
     private BlockingQueue<TCPData> queue;
 
     private Selector dataSelector;
     private Map<SocketChannel, SocketChannel> channelPairs;
 
     private int localPort;
     private String remoteHost;
     private int remotePort;
     private long delayMs;
 
     public TCPDataReader(int localPort, String remoteHost, int remotePort, long delayMs) {
         this.localPort = localPort;
         this.remoteHost = remoteHost;
         this.remotePort = remotePort;
         this.delayMs = delayMs;
         channelPairs = new ConcurrentHashMap<SocketChannel, SocketChannel>();
         queue = new DelayQueue<TCPData>();
     }
 
     /**
      * Listen to the local port. Handle new connections and reads from open sockets.
      */
     public void run() {
         // Writer is done in another thread so we can add a delay.
         // Data is passed from reader to writer through a queue so order is
         // guaranteed.
         // Since there is one queue, the same delay is assumed for all packets.
         TCPDataWriter writer = new TCPDataWriter(queue);
         Thread thread = new Thread(writer);
         thread.start();
 
         try {
             dataSelector = Selector.open();
             ServerSocketChannel ssc = ServerSocketChannel.open();
             ssc.configureBlocking(false);
             ServerSocket serverSocket = ssc.socket();
             serverSocket.bind(new InetSocketAddress(localPort));
             ssc.register(dataSelector, SelectionKey.OP_ACCEPT);
 
             LOGGER.log(Level.INFO, "Accepting requests on port {0}", localPort);
             if (delayMs > 0) {
                 LOGGER.log(Level.INFO, "Applying a delay of {0} ms to all communication", delayMs);
             }
 
             Set<SelectionKey> keys = null;
             while (true) {
                 try {
                     if (dataSelector.select() == 0) {
                         continue;
                     }
                     long selectTimeNanos = System.nanoTime();
                     keys = dataSelector.selectedKeys();
                     for (SelectionKey key : keys) {
                         if (key.isValid()
                                 && (key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                             ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
                                     .channel();
                             handleConnection(serverSocketChannel);
                         }
                         if (key.isValid()
                                 && (key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                             SocketChannel sc = (SocketChannel) key.channel();
                             handleRead(key, sc, selectTimeNanos);
                         }
                     }
                 } catch (IOException e) {
                     LOGGER.log(Level.WARNING, "Exception during selection: {0}", e.getMessage());
                 } finally {
                     if (keys != null) {
                         keys.clear();
                     }
                 }
             }
         } catch (Exception e) {
             LOGGER.severe(e.getMessage());
             System.exit(1);
         }
     }
 
     /**
      * Handles read requests from an open socket. Puts it in the queue for the writer to pick up after the delay.
      */
     private void handleRead(SelectionKey key, SocketChannel sc, long selectTimeNanos) {
         boolean close = false;
         ByteBuffer buffer = ByteBuffer.allocate(16384);
         int len = 0;
         SocketChannel pairSc = channelPairs.get(sc);
         try {
             len = sc.read(buffer);
             Utils.logVarArgs(LOGGER, Level.FINE, "Read {0} bytes from {1}", len, pairSc);
             if (len < 0 || pairSc == null) {
                 close = true;
             }
         } catch (Exception e) {
             LOGGER.log(Level.FINE, "Exception reading: {0}. Closing", e.getMessage());
             close = true;
         } finally {
             if (close) {
                 len = -1;
                 key.cancel();
                 closeInput(sc);
                 channelPairs.remove(sc);
             }
             long delayedUntilNanos = 0;
             if (delayMs > 0) {
                 delayedUntilNanos = selectTimeNanos + delayMs * 1000000;
             }
             try {
                 queue.add(new TCPData(buffer, len, pairSc, delayedUntilNanos));
             } catch (Exception e) {
                 LOGGER.warning(e.getMessage());
             }
         }
     }
 
     /**
      * Handles a new connection. Connects to the remote host. If that connection isn't successful, the socket is closed.
      */
     private void handleConnection(ServerSocketChannel serverSocketChannel) {
         LOGGER.log(Level.INFO, "Got a request {0}", serverSocketChannel.socket().getLocalSocketAddress());
         Socket clientSocket = null;
         try {
             ServerSocket serverSocket = serverSocketChannel.socket();
             clientSocket = serverSocket.accept();
             SocketChannel sc = clientSocket.getChannel();
             sc.configureBlocking(false);
 
             InetSocketAddress address = new InetSocketAddress(remoteHost, remotePort);
             LOGGER.log(Level.FINE, "Connecting to remote host {0}", address);
             SocketChannel pairSc = SocketChannel.open(address);
             LOGGER.log(Level.INFO, "Connected to remote host {0}", address);
             pairSc.configureBlocking(false);
 
             channelPairs.put(sc, pairSc);
             channelPairs.put(pairSc, sc);
             sc.register(dataSelector, SelectionKey.OP_READ);
             pairSc.register(dataSelector, SelectionKey.OP_READ);
         } catch (Exception e) {
             LOGGER.log(Level.WARNING, "Error connecting to remote host: {0}", e.getMessage());
             if (clientSocket != null) {
                 LOGGER.log(Level.WARNING, "Closing {0}", clientSocket.getChannel());
                 try {
                     clientSocket.close();
                 } catch (Exception ee) {
                     // do nothing
                 }
             }
         }
     }
 
     private static void closeInput(SocketChannel sc) {
         Socket socket = sc.socket();
         if (socket.isClosed()) {
             return;
         }
         if (socket.isOutputShutdown()) {
             try {
                 LOGGER.log(Level.INFO, "Closing {0}", sc);
                 socket.close();
             } catch (Exception e) {
                 // do nothing
             }
         } else if (!socket.isInputShutdown()) {
             try {
                 LOGGER.log(Level.INFO, "Closing input {0}", sc);
                 socket.shutdownInput();
             } catch (Exception e) {
                 // do nothing
             }
         }
     }
 
 }
