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
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
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
     protected ConcurrentHashMap<Integer, ArrayList<ByteBuffer>> scheduledWrites;
     protected ArrayList<ActivityListener> activityListeners;
     protected Thread thread;
     protected boolean run;
 
     /*public static void main(String[] args) throws IOException {
     Logger root = Logger.getRootLogger();
     root.setLevel(Level.DEBUG);
     root.addAppender(new ConsoleAppender(new TTCCLayout()));
     
     ConnectionMultiplexer vnc = new ConnectionMultiplexer();
     ActivityListener al = new ActivityListener() {
     
     @Override
     public void dataReceived(ByteBuffer data, int connection, ConnectionMultiplexer cm) {
     String recv = new String(data.array());
     cm.write(connection, ByteBuffer.wrap(("You said the following : " + recv).getBytes()));
     }
     
     @Override
     public void connectionClosed(int connection) {
     }
     };
     vnc.addActivityListener(al);
     vnc.addConnection(new InetSocketAddress("localhost", 30000));
     vnc.addConnection(new InetSocketAddress("localhost", 20000));
     vnc.start();
     vnc.run();
     }*/
     public ConnectionMultiplexer() {
         connections = new ConcurrentHashMap<>();
         scheduledWrites = new ConcurrentHashMap<>();
         activityListeners = new ArrayList<>();
         thread = new Thread(this);
         thread.setName("Multiplexer");
         try {
             socketSelector = SelectorProvider.provider().openSelector();
             readBuffer = ByteBuffer.allocateDirect(1024 ^ 2);
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
         scheduledWrites.get(connection).add(data);
     }
     
     private void read(SelectionKey key) throws IOException {
         SocketChannel socketChannel = (SocketChannel) key.channel();
 
         // Clear out our read buffer so it's ready for new data
         this.readBuffer.clear();
 
         // Attempt to read off the channel
         int numRead;
         try {
             numRead = socketChannel.read(this.readBuffer);
         } catch (IOException e) {
             // The remote forcibly closed the connection, cancel
             // the selection key and close the channel.
             key.cancel();
             socketChannel.close();
             
             close((int) key.attachment());
             
             return;
         }
         
         if (numRead == -1) {
             // Remote entity shut the socket down cleanly. Do the
             // same from our end and cancel the channel.
             key.channel().close();
             key.cancel();
             
             close((int) key.attachment());
             
             return;
         }
         
         readBuffer.flip();
         
         ByteBuffer bb = ByteBuffer.allocate(1024 ^ 2);
         bb.put(readBuffer);
         for (ActivityListener al : activityListeners) {
             al.dataReceived(bb, (int) key.attachment(), this);
         }
         
     }
     
     public void close(int connection) throws IOException {
         if (connections.get(connection).isValid()) {
             SelectionKey key = connections.get(connection);
             key.channel().close();
             key.cancel();
         }
         
         connections.remove(connection);
         scheduledWrites.remove(connection);
         for (ActivityListener al : activityListeners) {
             al.connectionClosed(connection);
         }
     }
     
     protected void write(SelectionKey key) {
         SocketChannel socketChannel = (SocketChannel) key.channel();
         for (Iterator<Entry<Integer, ArrayList<ByteBuffer>>> it = scheduledWrites.entrySet().iterator(); it.hasNext();) {
             try {
                 Entry<Integer, ArrayList<ByteBuffer>> entry = it.next();
                 
                 if (entry.getKey().equals((int) key.attachment())) {
                     for (Iterator<ByteBuffer> itr = entry.getValue().iterator(); itr.hasNext();) {
                         ByteBuffer bb = itr.next();
                         
                         socketChannel.write(bb);
                         if (bb.remaining() > 0) {
                             // Write has been interrupted
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
                 for (Map.Entry<Integer, ArrayList<ByteBuffer>> entry : scheduledWrites.entrySet()) {
                     SelectionKey sk = connections.get(entry.getKey());
                     if (sk.isValid() && sk.isReadable()) {
                         sk.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                     }
                 }
                 
                 this.socketSelector.select();
                 
                 for (Iterator<SelectionKey> it = this.socketSelector.selectedKeys().iterator(); it.hasNext();) {
                     SelectionKey sk = it.next();
                     
                     if (!sk.isValid()) {
                         if (sk.attachment() != null && sk.attachment() instanceof Integer) {
                             close((int) sk.attachment());
                         }
                         continue;
                     }
                     
                     if (sk.isConnectable() && ((SocketChannel) sk.channel()).finishConnect()) {
                         connectionCounter++;
                         connections.put(connectionCounter, sk);
                         scheduledWrites.put(connectionCounter, new ArrayList<ByteBuffer>());
                         sk.interestOps(SelectionKey.OP_READ);
                         sk.attach(connectionCounter);
                         
                         for (ActivityListener al : activityListeners) {
                             al.connectionEstablished(connectionCounter, ((SocketChannel)sk.channel()).socket());
                         }
                     } else if (sk.isReadable()) {
                         read(sk);
                     } else if (sk.isWritable()) {
                         write(sk);
                     }
                 }
             } catch (IOException ex) {
                 Logger.getLogger(getClass()).error("Failed to listen for incoming data");
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
