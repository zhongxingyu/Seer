 package de.meldanor.melchatclient;
 
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.SelectableChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.WritableByteChannel;
 import java.util.Iterator;
 
 import com.ronsoft.SystemInPipe;
 
 public class NetworkHandler {
 
     private Selector selector;
 
     private SocketChannel socketChannel;
     private SelectableChannel stdIn;
 
     public NetworkHandler(String host, String port) throws Exception {
         createConnection(host, port);
     }
 
     private void createConnection(String host, String port) throws Exception {
 
         selector = Selector.open();
         // Connect to the server
         SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(host, Integer.parseInt(port)));
        socketChannel.configureBlocking(false);
         socketChannel.register(selector, SelectionKey.OP_READ);
         // Wrapper for console input
         SystemInPipe stdinPipe = new SystemInPipe();
 
         stdIn = stdinPipe.getStdinChannel();
         stdinPipe.start();
         stdIn.register(selector, SelectionKey.OP_READ);
     }
     public void clientLoop() {
         ByteBuffer buffer = ByteBuffer.allocate(4096);
         try {
             while (true) {
                 int rdyChannels = selector.select(0);
                 if (rdyChannels == 0)
                     continue;
 
                 Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                 while (it.hasNext()) {
                     SelectionKey key = it.next();
                     buffer.clear();
                     if (key.isReadable()) {
                         // Server want something
                         if (key.channel() instanceof SocketChannel) {
                             SocketChannel sockChannel = (SocketChannel) key.channel();
                             sockChannel.read(buffer);
                             WritableByteChannel stdout = Channels.newChannel(System.out);
                             stdout.write(buffer);
                         }
                         // Client want something
                         else {
                             ReadableByteChannel channel = (ReadableByteChannel) stdIn;
                             channel.read(buffer);
                             socketChannel.write(buffer);
                         }
                     }
                     it.remove();
                 }
 
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 }
