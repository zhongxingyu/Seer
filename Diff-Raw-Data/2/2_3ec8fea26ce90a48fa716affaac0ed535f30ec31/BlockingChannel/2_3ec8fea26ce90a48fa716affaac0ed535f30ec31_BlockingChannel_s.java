 package com.sohu.jafka.network;
 
 import com.sohu.jafka.common.annotations.NotThreadSafe;
 import com.sohu.jafka.utils.Closer;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.*;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * A simple blocking channel with timeouts correctly enabled.
  *
  * @author adyliu (imxylz@gmail.com)
 * @since 2013-07-12
  */
 @NotThreadSafe
 public class BlockingChannel {
 
     private final String host;
     private final int port;
     private final int readBufferSize;
     private final int writeBufferSize;
     private final int readTimeoutMs;
 
     private boolean connected = false;
     private SocketChannel channel;
     private ReadableByteChannel readChannel;
     private GatheringByteChannel writeChannel;
     private final ReentrantLock lock = new ReentrantLock();
 
     public BlockingChannel(String host, int port, int readBufferSize, int writeBufferSize, int readTimeoutMs) {
         this.host = host;
         this.port = port;
         this.readBufferSize = readBufferSize;
         this.writeBufferSize = writeBufferSize;
         this.readTimeoutMs = readTimeoutMs;
     }
 
     public void connect() throws IOException {
         lock.lock();
         try {
             if (!connected) {
                 channel = SocketChannel.open();
                 if (readBufferSize > 0) {
                     channel.socket().setReceiveBufferSize(readBufferSize);
                 }
                 if (writeBufferSize > 0) {
                     channel.socket().setSendBufferSize(writeBufferSize);
                 }
                 channel.configureBlocking(true);
                 channel.socket().setSoTimeout(readTimeoutMs);
                 channel.socket().setKeepAlive(true);
                 channel.socket().setTcpNoDelay(true);
                 channel.connect(new InetSocketAddress(host, port));
 
                 writeChannel = channel;
                 readChannel = Channels.newChannel(channel.socket().getInputStream());
                 connected = true;
             }
         } finally {
             lock.unlock();
         }
     }
 
     public void disconnect() {
         lock.lock();
         try {
             if (connected || channel != null) {
                 Closer.closeQuietly(channel);
                 Closer.closeQuietly(channel.socket());
                 Closer.closeQuietly(readChannel);
                 channel = null;
                 readChannel = null;
                 writeChannel = null;
                 connected = false;
             }
 
         } finally {
             lock.unlock();
         }
     }
 
     public boolean isConnected() {
         return connected;
     }
 
     public int send(BoundedByteBufferSend bufferSend) throws IOException {
         if (!isConnected()) {
             throw new ClosedChannelException();
         }
         return bufferSend.writeCompletely(writeChannel);
     }
 }
