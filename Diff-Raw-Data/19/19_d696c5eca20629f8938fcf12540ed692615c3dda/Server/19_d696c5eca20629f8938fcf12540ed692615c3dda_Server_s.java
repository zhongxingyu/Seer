 package com.github.zhongl.nij.bio;
 
 import java.io.*;
 import java.net.*;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.github.zhongl.nij.util.Utils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Server {
   private static volatile boolean running = true;
   private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
   private static final ExecutorService SERVICE = Executors
       .newFixedThreadPool(Integer.getInteger("thread.pool.size", Runtime.getRuntime().availableProcessors() * 2));
   private static final byte[] BUFFER = new byte[1024];
   private static final byte[] RESPONSE = "HTTP/1.0 200 OK\r\nContent-Length:1\r\n\r\na".getBytes();
 
   public static void main(String... args) throws Exception {
     final String host = args[0];
     final int port = Integer.parseInt(args[1]);
     final int backlog = Integer.parseInt(args[2]);
     final int size = Integer.parseInt(args[3]);
     final String type = args[4];
     final SocketAddress address = new InetSocketAddress(host, port);
 
     Runtime.getRuntime().addShutdownHook(new Thread("shutdow-hook") {
       @Override
       public void run() { running = false; }
     });
 
 
     System.out.println("Started at " + host + ":" + port +
         " with backlog: " + backlog +
         " receive buffer: " + size + "k.");
 
     Acceptor acceptor = null;
 
    if (type.equals("s")) acceptor = new SocketAcceptor(address, size, backlog);
    else acceptor = new ChannelAcceptor(address, size, backlog);
 
     while (running) {
       try {
         handle(acceptor.accept());
       } catch (SocketTimeoutException e) { }
     }
     silentClose(acceptor);
     SERVICE.shutdownNow();
     System.out.println("Stopped.");
   }
 
   public static void handle(final Socket accept) {
     SERVICE.execute(new Runnable() {
       @Override
       public void run() {
         try {
           accept.setTcpNoDelay(true);
           accept.setSendBufferSize(1 * 1024);
           final InputStream inputStream = new BufferedInputStream(accept.getInputStream());
           final OutputStream outputStream = new BufferedOutputStream(accept.getOutputStream());
           inputStream.read(BUFFER);
           outputStream.write(RESPONSE);
//          outputStream.flush();
//          outputStream.close();
//          inputStream.close();
         } catch (IOException e) {
           LOGGER.error("Unexpected error", e);
         } finally {
           silentClose(accept);
         }
       }
     });
   }
 
   private static void silentClose(Closeable closeable) { try {closeable.close(); } catch (IOException e) { } }
 
   private static void silentClose(Socket accept) { try { accept.close(); } catch (IOException e1) { } }
 
   public interface Acceptor extends Closeable {
     Socket accept() throws IOException;
   }
 
   private static class ChannelAcceptor implements Acceptor {
     private final Selector selector;
     private final ServerSocketChannel channel;
 
     public ChannelAcceptor(SocketAddress address, int size, int backlog) throws IOException {
       this.selector = Selector.open();
       this.channel = ServerSocketChannel.open();
       channel.socket().setReceiveBufferSize(Utils.kb(size));
       channel.socket().setReuseAddress(true);
       channel.socket().bind(address, backlog);
       channel.configureBlocking(false);
     }
 
     @Override
     public Socket accept() throws IOException {
       while (selector.select(500L) == 0) ;
       selector.selectedKeys().clear();
       return channel.accept().socket();
     }
 
     @Override
     public void close() throws IOException {
       selector.close();
       channel.close();
     }
   }
 
   private static class SocketAcceptor implements Acceptor {
     private ServerSocket socket;
 
     public SocketAcceptor(SocketAddress address, int size, int backlog) throws IOException {
       socket = new ServerSocket();
       socket.setReceiveBufferSize(Utils.kb(size));
       socket.setReuseAddress(true);
       socket.setSoTimeout(500);
       socket.bind(address, backlog);
     }
 
     @Override
     public Socket accept() throws IOException {return socket.accept();}
 
     @Override
     public void close() throws IOException { socket.close(); }
   }
 }
