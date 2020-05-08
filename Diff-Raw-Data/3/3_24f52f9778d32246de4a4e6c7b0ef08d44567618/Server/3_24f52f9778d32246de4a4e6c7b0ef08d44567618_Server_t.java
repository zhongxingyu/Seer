 package com.github.zhongl.nij.bio;
 
 import java.io.*;
 import java.net.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.*;
 import java.util.concurrent.*;
 
 import com.github.zhongl.nij.util.Utils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Server {
   private static volatile boolean running = true;
   private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
   private static final byte[] BUFFER = new byte[1024];
   private static final byte[] RESPONSE = "HTTP/1.0 200 OK\r\nContent-Length:1\r\n\r\na".getBytes();
   private static final ByteBuffer BUFFER_IN = ByteBuffer.allocateDirect(1024);
   private static final ByteBuffer BUFFER_OUT;
 
   static {
     BUFFER_OUT = ByteBuffer.allocateDirect(RESPONSE.length);
     BUFFER_OUT.put(RESPONSE);
     BUFFER_OUT.flip();
   }
 
   public static void main(String... args) throws Exception {
     final String host = args[0];
     final int port = Integer.parseInt(args[1]);
     final int backlog = Integer.parseInt(args[2]);
     final int size = Integer.parseInt(args[3]);
     final String acceptorType = args[4];
     final String handlerType = args[5];
     final String schedulerType = args[6];
     final int thread = Integer.getInteger("thread.pool.size", Runtime.getRuntime().availableProcessors() * 2);
     final SocketAddress address = new InetSocketAddress(host, port);
 
     Runtime.getRuntime().addShutdownHook(new Thread("shutdow-hook") {
       @Override
       public void run() { running = false; }
     });
 
 
     System.out.println("Started at " + host + ":" + port +
         " with backlog: " + backlog + " receive buffer: " + size + "k.");
 
     final Acceptor acceptor = AcceptorType.valueOf(acceptorType.toUpperCase()).build(address, size, backlog);
     final Handler handler = Handler.valueOf(handlerType.toUpperCase());
    final Scheduler scheduler = SchedulerType.valueOf(schedulerType.toUpperCase())
                                             .builder(thread, (backlog > 0 ? backlog : Integer.MAX_VALUE));
 
     while (running) {
       try { scheduler.schedule(acceptor.accept(), handler); } catch (SocketTimeoutException e) { }
     }
     silentClose(acceptor);
     scheduler.shutdown();
     System.out.println("Stopped.");
   }
 
   static abstract class Scheduler {
 
     abstract void schedule(Socket socket, Handler handler);
 
     abstract void shutdown();
   }
 
   static class ProcessorScheduler extends Scheduler {
     private final BlockingQueue<Object[]> queue;
     private volatile boolean stop = false;
 
     class Processor extends Thread {
 
       @Override
       public void run() {
         while (!stop) {
           try {
             Object[] args = queue.poll(1L, TimeUnit.SECONDS);
             if (args == null) continue;
             final Socket socket = (Socket) args[0];
             final Handler handler = (Handler) args[1];
             handle(socket, handler);
           } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
           }
         }
       }
     }
 
     ProcessorScheduler(int thread, int backlog) {
       queue = new LinkedBlockingQueue<Object[]>(backlog);
       for (int i = 0; i < thread; i++) {
         new Processor().start();
       }
     }
 
     @Override
     void schedule(Socket socket, Handler handler) {
       try {
         queue.put(new Object[]{socket, handler});
       } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
       }
     }
 
     @Override
     void shutdown() { stop = true; }
   }
 
   static class ExecutorScheduler extends Scheduler {
     private final ExecutorService service;
 
     ExecutorScheduler(int thread, int backlog) {
       final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(backlog);
       this.service = new ThreadPoolExecutor(thread, thread, 0L, TimeUnit.MILLISECONDS, workQueue);
     }
 
     @Override
     public void schedule(final Socket socket, final Handler handler) {
       service.execute(new Runnable() {
         @Override
         public void run() { handle(socket, handler); }
       });
     }
 
     @Override
     void shutdown() { service.shutdownNow(); }
   }
 
   enum SchedulerType {
     E {
       @Override
       Scheduler builder(int thread, int backlog) { return new ExecutorScheduler(thread, backlog); }
     }, T {
       @Override
       Scheduler builder(int thread, int backlog) { return new ProcessorScheduler(thread, backlog); }
     };
 
     abstract Scheduler builder(int thread, int backlog);
   }
 
   public static void handle(final Socket accept, final Handler handler) {
     try {
       accept.setTcpNoDelay(true);
       accept.setSendBufferSize(1 * 1024);
       accept.setKeepAlive(false);
       accept.setSoLinger(true, 1);
       handler.readAndWrite(accept);
     } catch (IOException e) {
       LOGGER.error("Unexpected error", e);
     } finally {
       silentClose(accept);
     }
   }
 
   enum Handler {
     S {
       @Override
       void readAndWrite(Socket socket) throws IOException {
         final InputStream inputStream = new BufferedInputStream(socket.getInputStream());
         final OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
         inputStream.read(BUFFER);
         outputStream.write(RESPONSE);
         outputStream.close();
         inputStream.close();
       }
     }, C {
       @Override
       void readAndWrite(Socket socket) throws IOException {
         final ReadableByteChannel readableByteChannel = Channels.newChannel(socket.getInputStream());
         final WritableByteChannel writableByteChannel = Channels.newChannel(socket.getOutputStream());
         readableByteChannel.read(BUFFER_IN.duplicate());
         writableByteChannel.write(BUFFER_OUT.asReadOnlyBuffer());
         socket.shutdownOutput();
       }
     };
 
     abstract void readAndWrite(Socket socket) throws IOException;
   }
 
   private static void silentClose(Closeable closeable) { try {closeable.close(); } catch (IOException e) { } }
 
   private static void silentClose(Socket accept) { try { accept.close(); } catch (IOException e1) { } }
 
   public enum AcceptorType {
     S {
       @Override
       Acceptor build(SocketAddress address, int size, int backlog) throws IOException {
         return new SocketAcceptor(address, size, backlog);
       }
     }, C {
       @Override
       Acceptor build(SocketAddress address, int size, int backlog) throws IOException {
         return new ChannelAcceptor(address, size, backlog);
       }
     };
 
     abstract Acceptor build(SocketAddress address, int size, int backlog) throws IOException;
   }
 
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
 
       channel.register(selector, SelectionKey.OP_ACCEPT);
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
