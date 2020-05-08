 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.connectivity;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import org.apache.log4j.Logger;
 
 /**
  * Generic Socket multiplexer
  * This is so *awesome* your children will hear of this one day...
  * @created Dec 14, 2011
  * @author double-u
  */
 public class ConnectionMultiplexer implements Runnable {
 
     protected ConcurrentHashMap<Integer, SelectionKey> connections;
     protected Selector socketSelector;
     protected ByteBuffer readBuffer;
     protected final ConcurrentHashMap<Integer, ArrayBlockingQueue<ByteBuffer>> scheduledWrites;
     protected ArrayList<ActivityListener> activityListeners;
     protected Thread thread;
     protected boolean run;
 
     public ConnectionMultiplexer() {
         connections = new ConcurrentHashMap<>();
         scheduledWrites = new ConcurrentHashMap<>();
         activityListeners = new ArrayList<>();
         thread = new Thread(this);
         thread.setName("Multiplexer");
         thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
 
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 Logger.getLogger(getClass()).error("Uncaught exception in Multiplexer", e);
             }
         });
         try {
             socketSelector = SelectorProvider.provider().openSelector();
             readBuffer = ByteBuffer.allocateDirect(1024 * 1024);
         } catch (IOException ex) {
             Logger.getLogger(getClass()).error("Failed to ", ex);
         }
     }
 
     public void addActivityListener(ActivityListener al) {
         activityListeners.add(al);
     }
 
     public void addConnection(SocketAddress addr) throws IOException {
         SocketChannel channel = SocketChannel.open();
         channel.configureBlocking(false);
 
         socketSelector.wakeup();
         channel.register(socketSelector, SelectionKey.OP_CONNECT);
         channel.connect(addr);
     }
 
     public void write(int connection, ByteBuffer data) {
         if (!scheduledWrites.containsKey(connection)) {
             throw new IllegalArgumentException("Connection does not exist");
         }
 
         scheduledWrites.get(connection).add(data);
         socketSelector.wakeup();
     }
 
    protected void read(SelectionKey key) throws IOException {
         SocketChannel socketChannel = (SocketChannel) key.channel();
 
         // Clear out our read buffer so it's ready for new data
         this.readBuffer.clear();
 
         // Attempt to read off the channel
         int bytesRead;
         try {
             bytesRead = socketChannel.read(this.readBuffer);
         } catch (IOException e) {
             // The remote forcibly closed the connection, cancel
             // the selection key and close the channel.
             key.cancel();
             socketChannel.close();
 
             close((int) key.attachment());
 
             return;
         }
 
         if (bytesRead == -1) {
             // Remote entity shut the socket down cleanly. Do the
             // same from our end and cancel the channel.
             key.channel().close();
             key.cancel();
 
             close((int) key.attachment());
             return;
         }
 
         if (bytesRead < 1) {
             return;
         }
 
         readBuffer.flip();
 
         // Only send the received amount of data
         ByteBuffer bb = ByteBuffer.allocate(bytesRead);
         bb.put(readBuffer);
         for (ActivityListener al : activityListeners) {
             al.dataReceived(bb, (int) key.attachment(), this);
         }
 
     }
 
     public void close(int connection) throws IOException {
         connections.remove(connection);
         scheduledWrites.remove(connection);
         for (ActivityListener al : activityListeners) {
             al.connectionClosed(connection);
         }
     }
 
     protected void write(SelectionKey key) {
         SocketChannel socketChannel = (SocketChannel) key.channel();
 
         for (Iterator<Entry<Integer, ArrayBlockingQueue<ByteBuffer>>> it = scheduledWrites.entrySet().iterator(); it.hasNext();) {
             try {
                 Entry<Integer, ArrayBlockingQueue<ByteBuffer>> entry = it.next();
 
                 if (entry.getKey().equals((int) key.attachment())) {
                     ArrayBlockingQueue<ByteBuffer> writeOps = entry.getValue();
                     for (Iterator<ByteBuffer> itr = writeOps.iterator(); itr.hasNext();) {
                         ByteBuffer bb = itr.next();
                         socketChannel.write(bb);
 
                         if (bb.remaining() > 0) {
                             // Write has been interrupted
                             Logger.getLogger(getClass()).debug("Write interrupt on " + (int) key.attachment());
                             break;
                         }
                         itr.remove();
                     }
                 }
             } catch (IOException ex) {
                 Logger.getLogger(getClass()).error("Failed to write data", ex);
             }
         }
 
         key.interestOps(SelectionKey.OP_READ);
     }
 
     public void start() {
         this.run = true;
         this.thread.start();
     }
 
     public void stop() {
         this.run = false;
         this.socketSelector.wakeup();
 
         for (Map.Entry<Integer, SelectionKey> entry : connections.entrySet()) {
             try {
                 close(entry.getKey());
             } catch (IOException ex) {
                 Logger.getLogger(getClass()).error("Failed to close connection", ex);
             }
         }
     }
 
     public void run() {
         int connectionCounter = 0;
 
         while (run) {
             try {
                 for (Map.Entry<Integer, ArrayBlockingQueue<ByteBuffer>> entry : scheduledWrites.entrySet()) {
                     if (entry.getValue().size() < 1) {
                         continue;
                     }
 
                     SelectionKey sk = connections.get(entry.getKey());
                     if (sk.isValid() && (sk.interestOps() & SelectionKey.OP_WRITE) == 0) {
                         sk.interestOps(SelectionKey.OP_WRITE);
                         break;
                     }
                 }
 
                 this.socketSelector.select();
 
                 for (Iterator<SelectionKey> it = this.socketSelector.selectedKeys().iterator(); it.hasNext();) {
                     SelectionKey sk = it.next();
                     it.remove();
 
                     if (!sk.isValid()) {
                         Logger.getLogger(getClass()).info("Invalid connection " + ((SocketChannel) sk.channel()).socket().getInetAddress().getCanonicalHostName());
                         if (sk.attachment() != null && sk.attachment() instanceof Integer) {
                             close((int) sk.attachment());
                         }
                         continue;
                     }
 
                     if (sk.isConnectable() && sk.attachment() == null) {
                         boolean success = ((SocketChannel) sk.channel()).finishConnect();
                         if (!success) {
                             Logger.getLogger(getClass()).warn("Failed to connect to " + ((SocketChannel) sk.channel()).socket().getInetAddress().getCanonicalHostName());
                         }
 
                         connectionCounter++;
                         connections.put(connectionCounter, sk);
                         // We like to queue up 50 writes, if there are more they need to wait
                         scheduledWrites.put(connectionCounter, new ArrayBlockingQueue<ByteBuffer>(50));
                         sk.interestOps(SelectionKey.OP_READ);
                         sk.attach(connectionCounter);
 
                         for (ActivityListener al : activityListeners) {
                             al.connectionEstablished(connectionCounter, ((SocketChannel) sk.channel()).socket());
                         }
                     } else {
                         try {
                             if (sk.isReadable()) {
                                 read(sk);
                             }
                             if (sk.isWritable()) {
                                 write(sk);
                             }
                         } catch (CancelledKeyException ex) {
                             close((int) sk.attachment());
                         }
                     }
                 }
             } catch (IOException ex) {
                Logger.getLogger(getClass()).error("Failed to listen for incoming data", ex);
             }
         }
     }
 
     public static interface ConnectionListener {
 
         public void dataReceived(ByteBuffer data);
     }
 
     public static interface ActivityListener {
 
         public void dataReceived(ByteBuffer data, int connection, ConnectionMultiplexer cm);
 
         public void connectionEstablished(int connection, Socket s);
 
         public void connectionClosed(int connection);
     }
 }
