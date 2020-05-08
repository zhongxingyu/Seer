 package org.msgpack.rpc.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 
 import org.msgpack.Packer;
 import org.msgpack.Unpacker;
 import org.msgpack.rpc.Request;
 import org.msgpack.rpc.Response;
 import org.msgpack.rpc.RpcController.Callback;
 
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

 public class SocketRpcClient implements Client {
     
     protected final String host;
     
     protected final int port;
     
     protected final InetAddress addr;
     
     protected final ExecutorService executor;
     
     protected SocketRpcClient(String host, int port, ExecutorService executor) throws UnknownHostException {
         this.host = host;
         this.port = port;
         this.addr = InetAddress.getByName(host);
         this.executor = executor;
     }
     
     protected Socket create() throws IOException {
         // TODO: connection pool
         final Socket socket = new Socket(addr, port);
         socket.setTcpNoDelay(true);
         socket.setKeepAlive(true);
         socket.setReuseAddress(true);
         return socket;
     }
     
     public Response call(final Request request) throws IOException, InterruptedException, ExecutionException {
         final Socket socket = create();
         Future<Response> future = executor.submit(new Handler(socket, request));
         return future.get();
     }
 
     public void send(Request request, final Callback callback) throws IOException {
         final Socket socket = create();
         final Handler handler = new Handler(socket, request);
         executor.execute(new Runnable() {
             public void run(){
                 try {
                     Response response = handler.call();
                     callback.apply(response);
                 } catch (Exception e) {
                     Response response = new Response();
                     response.setId(handler.request.getId());
                     response.setErrorMessage(e.getMessage());
                     callback.apply(response);
                 }
             }
         });
     }
     
     protected static class Handler implements Callable<Response> {
         protected static final Logger logger = Logger.getLogger(Handler.class.getName());
         
         protected final Socket socket;
         
         protected final Request request;
         
         protected Handler(Socket socket, Request request){
             this.socket = socket;
             this.request = request;
         }
         
         public Response call() throws Exception {
             InputStream in = null;
             OutputStream out = null;
             
             //
             // いつかコードをは整える
             //
             
             try {
                 in = socket.getInputStream();
                 out = socket.getOutputStream();
                 
                 // send
                 // new Packer(out).pack(request);
                 
                ByteOutputStream o = new ByteOutputStream();
                 new Packer(o).pack(request);
                 
                out.write(o.getBytes());
                 out.flush();
                 socket.shutdownOutput();
                 
                 // reciev
                 
                 // TODO: this should not be hardcoded to 4096 bytes
                 final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                 ReadableByteChannel channel = Channels.newChannel(in);
                 if(!socket.isClosed()){
                     // TODO: this should not be hardcoded to 1024 bytes
                     final ByteBuffer tmp = ByteBuffer.allocate(1024);
                     while(0 < channel.read(tmp)){
                         tmp.flip();
                         buffer.put(tmp);
                         buffer.flip();
                         tmp.clear();
                     }
                 }
                 socket.shutdownInput();
                 
                 final Unpacker unpacker = new Unpacker();
                 byte[] data = new byte[buffer.remaining()];
                 buffer.get(data);
                 unpacker.wrap(data);
                 if(!unpacker.execute()){
                     throw new IOException("Invalid unpacked data");
                 }
                 
                 Response response = new Response();
                 response.messageUnpack(unpacker);
                 return response;
             } catch(IOException e){
                 e.printStackTrace();
                 logger.warning("Error while reading/writing:" + e);
                 throw e;
             } finally {
                 logger.info("connection closed.[" + socket + "]");
 
                 try {
                     if(null != in){
                         in.close();
                     }
                     
                     if(null != out){
                         out.close();
                     }
 
                     socket.close();
                 } catch(IOException e){
                     logger.warning("Error while closing I/O:" + e);
                 }
             }
         }
     }
 
 }
