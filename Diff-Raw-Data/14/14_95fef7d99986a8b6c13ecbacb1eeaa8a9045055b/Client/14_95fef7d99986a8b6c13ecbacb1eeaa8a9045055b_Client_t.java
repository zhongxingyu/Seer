 package ru.compscicenter.lidis.client;
 
 import ru.compscicenter.lidis.Node;
 import ru.compscicenter.lidis.Query;
 import ru.compscicenter.lidis.utils.Nullable;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 
 /**
  * Author: Vasiliy Khomutov
  * Date: 5.10.2013
  */
 public class Client {
 
     private static final int SOCKET_BUFFER_SIZE = 64 * 1024;
 
     private final ByteBuffer buffer = ByteBuffer.allocate(SOCKET_BUFFER_SIZE);
     private final StringBuilder stringBuilder = new StringBuilder();
 
     private final String host;
     private final int port;
 
     public Client(String host, int port) {
         this.host = host;
         this.port = port;
     }
 
     public Nullable<String> makeQuery(Query query) {
         try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
             socketChannel.connect(new InetSocketAddress(host, port));
 
             buffer.clear();
             buffer.put(query.toBytes());
             buffer.flip();
             socketChannel.write(buffer);
             socketChannel.shutdownOutput();
 
             buffer.clear();
             stringBuilder.delete(0, stringBuilder.length());
             while (socketChannel.read(buffer) != -1) {
                 buffer.flip();
                 stringBuilder.append(new String(buffer.array(), buffer.position(), buffer.remaining()));
                 buffer.clear();
             }
 
             if (stringBuilder.toString().equals(Node.NOTHING_MARK)) {
                 return Nullable.smth("NO SUCH KEY");
             }
 
             return Nullable.smth(stringBuilder.toString());
         } catch (IOException e) {
             System.out.println("CLIENT: ERROR: something wrong with I/O: " + e.getMessage());
             return Nullable.none();
         }
     }
 }
