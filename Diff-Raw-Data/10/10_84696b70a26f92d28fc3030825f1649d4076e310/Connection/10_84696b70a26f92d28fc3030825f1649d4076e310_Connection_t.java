 /*
  * Copyright 2012 Piratenpartei Schweiz
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.piratenpartei.pivote.rpc;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Nullable;
 
 import org.slf4j.Logger;
 
 import ch.piratenpartei.pivote.serialize.DataInput;
 import ch.piratenpartei.pivote.serialize.DataOutput;
 import ch.piratenpartei.pivote.serialize.SerializationContext;
 import ch.piratenpartei.pivote.serialize.SerializationException;
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.net.HostAndPort;
 import com.google.common.util.concurrent.ForwardingListenableFuture;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.SettableFuture;
 import com.google.common.util.concurrent.Uninterruptibles;
 
 import ch.raffael.util.common.logging.EnhancedLogger;
 import ch.raffael.util.common.logging.LogUtil;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public class Connection {
 
     @SuppressWarnings("UnusedDeclaration")
     private final Logger log;
 
     private final SerializationContext serializationContext;
     private final HostAndPort piVoteServer;
     private long requestTimeout = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
     private Timer requestTimer;
     private final Map<UUID, ResponseFuture> requestMap = new ConcurrentHashMap<UUID, ResponseFuture>();
     private LinkedBlockingQueue<RpcRequest> requestQueue = new LinkedBlockingQueue<RpcRequest>();
     private Socket socket;
     private BufferedInputStream socketInputStream;
     private BufferedOutputStream socketOutputStream;
 
     private Thread inputThread;
     private Thread outputThread;
 
     private volatile boolean disconnect = false;
 
     public Connection(SerializationContext serializationContext, HostAndPort piVoteServer) {
         log = new EnhancedLogger(LogUtil.getLogger(), new Function<String, String>() {
             @Override
             public String apply(@Nullable String input) {
                 return "Connection:" + Connection.this.piVoteServer + ": " + input;
             }
         });
         this.serializationContext = serializationContext.withLogger(log);
         this.piVoteServer = piVoteServer;
     }
 
     public synchronized void connect() throws IOException {
         boolean success = false;
         Preconditions.checkState(socket == null, "Already connected");
         log.info("Connecting to {}", piVoteServer);
         socket = new Socket(piVoteServer.getHostText(), piVoteServer.getPort());
         try {
             socketInputStream = new BufferedInputStream(socket.getInputStream());
             socketOutputStream = new BufferedOutputStream(socket.getOutputStream());
             requestTimer = new Timer("Connection:" + piVoteServer + ":timer");
             inputThread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     reader();
                 }
             }, "Connection:" + piVoteServer + ":reader");
             outputThread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     writer();
                 }
             }, "Connection:" + piVoteServer + ":output");
             inputThread.start();
             outputThread.start();
             success = true;
         }
         finally {
             if ( !success ) {
                 disconnect = true;
                 log.debug("Attempting to close failed connection to {}", piVoteServer);
                 try {
                     socket.close();
                 }
                 catch ( Exception e ) {
                     log.error("Error closing connection to {}", piVoteServer);
                 }
             }
         }
     }
 
     public void disconnect() {
         synchronized ( this ) {
             if ( disconnect || socket == null ) {
                 log.debug("Not connected to {}", piVoteServer);
                 disconnect = true;
                 return;
             }
             log.info("Disconnecting from {}", piVoteServer);
             disconnect = true;
         }
         boolean interrupted = false;
         // wait for all pending requests to be sent
         while ( !requestQueue.isEmpty() ) {
             synchronized ( requestQueue ) {
                 try {
                     requestQueue.wait();
                 }
                 catch ( InterruptedException e ) {
                     interrupted = true;
                 }
             }
         }
         // stop our threads
         outputThread.interrupt();
         inputThread.interrupt();
         Uninterruptibles.joinUninterruptibly(outputThread);
         Uninterruptibles.joinUninterruptibly(inputThread); // will exit on empty response map
         requestTimer.cancel();
         // close the socket
         try {
             socket.close();
         }
         catch ( Exception e ) {
             log.error("Error closing socket to " + piVoteServer, e);
         }
         // re-interrupt if we've been interrupted while disconnecting
         if ( interrupted ) {
             Thread.currentThread().interrupt();
         }
     }
 
     public synchronized ListenableFuture<RpcResponse> sendRequest(RpcRequest request) {
         Preconditions.checkState(socket != null && !disconnect, "Not connected");
         log.debug("Enqueueing request: {}; current queue size: {}", request, requestQueue.size());
        request.setRequestId(UUID.randomUUID());
         ResponseFuture result = new ResponseFuture(request.getRequestId());
         requestMap.put(request.getRequestId(), result);
         requestQueue.offer(request);
         return result;
     }
 
     private void writer() {
         ByteBuffer sizeBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
         while ( !disconnect ) {
             RpcRequest request;
             try {
                 request = requestQueue.take();
                 synchronized ( requestQueue ) {
                     requestQueue.notifyAll();
                 }
             }
             catch ( InterruptedException e ) {
                 if ( disconnect ) {
                     log.debug("Writer thread exiting");
                     return;
                 }
                 else {
                     log.warn("Ignoring interrupt");
                     continue;
                 }
             }
             try {
                 log.trace("Preparing request: {}", request);
                 ByteArrayOutputStream requestData = new ByteArrayOutputStream();
                 request.write(new DataOutput(requestData, serializationContext));
                sizeBuffer.rewind();
                 sizeBuffer.putInt(requestData.size());
                 log.debug("Sending request: {} ({} bytes)", request, requestData.size());
                 requestData.writeTo(socketOutputStream);
                 socketOutputStream.flush();
             }
             catch ( IOException e ) {
                 log.error("Error sending request {}", request, e);
                 if ( !socket.isConnected() ) {
                     disconnect();
                 }
             }
         }
     }
 
     private void reader() {
         ByteBuffer sizeBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
         while ( true ) {
             if ( requestMap.isEmpty() && disconnect ) {
                 return;
             }
             try {
                sizeBuffer.rewind();
                 if ( !read(sizeBuffer.array()) ) {
                     log.info("Server disconnected");
                     disconnect();
                     return;
                 }
                 int size = sizeBuffer.getInt();
                 if ( log.isDebugEnabled() ) {
                     log.debug("Retrieving message ({} bytes)", size);
                 }
                 byte[] buf = new byte[size];
                 read(buf);
                 DataInput data = new DataInput(new ByteArrayInputStream(buf), serializationContext);
                 Object object = data.readObject();
                 if ( !(object instanceof RpcResponse) ) {
                     log.error("Invalid response : {}", object);
                 }
                 else {
                     RpcResponse response = (RpcResponse)object;
                     ResponseFuture retval = requestMap.remove(response.getRequestId());
                     if ( retval == null ) {
                         log.error("Umatched response ({}): {}", response.getRequestId(), response);
                     }
                     else {
                         log.debug("Received response: {}", response);
                         retval.received(response);
                     }
                 }
             }
             catch ( SerializationException e ) {
                 log.error("Error unmarshalling data", e);
             }
             catch ( IOException e ) {
                 log.error("Error reading data", e);
                 disconnect();
                 requestMap.clear();
             }
         }
     }
 
     private boolean read(byte[] dest) throws IOException {
         int count = 0;
         int c;
         while ( count < dest.length ) {
             c = socketInputStream.read(dest, count, dest.length - count);
             if ( c < 0 ) {
                 if ( count > 0 ) {
                     throw new EOFException("Expected at least " + (dest.length - count) + " more bytes of data");
                 }
                 else {
                     return false;
                 }
             }
             count += c;
         }
         return true;
     }
 
     private final class ResponseFuture extends ForwardingListenableFuture.SimpleForwardingListenableFuture<RpcResponse> {
         private final UUID requestId;
         private final TimerTask timeout = new TimerTask() {
             @Override
             public void run() {
                 requestMap.remove(requestId);
                 log.debug("Request timeout");
                 ((SettableFuture)delegate()).setException(new RpcTimeoutException("Request " + requestId + " timed out"));
             }
         };
         private ResponseFuture(UUID requestId) {
             super(SettableFuture.<RpcResponse>create());
             this.requestId = requestId;
             requestTimer.schedule(timeout, requestTimeout);
         }
         @SuppressWarnings({ "unchecked", "ThrowableResultOfMethodCallIgnored" })
         private void received(RpcResponse response) {
             timeout.cancel();
             if ( response.getException() != null ) {
                 ((SettableFuture)delegate()).setException(response.getException());
             }
             else {
                 ((SettableFuture)delegate()).set(response);
             }
         }
     }
 
 }
