 /*
  * Copyright 2011 Gregory P. Moyer
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
 package org.syphr.mythtv.util.socket;
 
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.ByteChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class manages a low-level network connection. It provides the necessary read/write
  * capabilities as well as the ability to take over the communications channel entirely to
  * perform bulk raw data transfer, such as transferring a file while another manager
  * controls the flow.
  *
  * @author Gregory P. Moyer
  */
 public class SocketManager
 {
     private final Logger logger = LoggerFactory.getLogger(SocketManager.class);
 
     private final Packet packet;
 
     private final BlockingQueue<String> queue;
     private final AtomicInteger skippedResponses;
     private final ExecutorService receiverExecutor;
 
     private SocketChannel socket;
     private Selector readSelector;
     private Selector writeSelector;
 
     private Future<?> receiver;
 
     private Interceptor interceptor;
 
     private ByteChannel redirect;
 
     /**
      * Construct a new socket manager that is not connected to a server.
      *
      * @param packet
      *            a packet implementation that will handle formatting outgoing messages
      *            and parsing incoming messages
      */
     public SocketManager(Packet packet)
     {
         this.packet = packet;
 
         queue = new LinkedBlockingQueue<String>();
         skippedResponses = new AtomicInteger(0);
         receiverExecutor = Executors.newSingleThreadExecutor(new ThreadFactory()
         {
             @Override
             public Thread newThread(Runnable r)
             {
                 SecurityManager s = System.getSecurityManager();
                 ThreadGroup group = (s != null)
                         ? s.getThreadGroup()
                         : Thread.currentThread().getThreadGroup();
 
                 Thread t = new Thread(group, r, SocketManager.class.getSimpleName()
                                                 + " Receiver Thread", 0);
                 if (!t.isDaemon())
                 {
                     t.setDaemon(true);
                 }
                 if (t.getPriority() != Thread.NORM_PRIORITY)
                 {
                     t.setPriority(Thread.NORM_PRIORITY);
                 }
 
                 return t;
             }
         });
     }
 
     /**
      * Provide a means for intercepting messages. This allows clients of this class to
      * override normal behavior upon the receipt of data over the socket.
      *
      * @param interceptor
      *            the interceptor to set
      */
     public void setInterceptor(Interceptor interceptor)
     {
         this.interceptor = interceptor;
     }
 
     /**
      * Connect to a server. This method will block until the connection completes.
      * If a connection is already active, this method will do nothing.
      *
      * @see #connect(InetSocketAddress, long)
      *
      * @param host
      *            the hostname (or IP address) of the server
      * @param port
      *            the port on the server
      * @param timeout
      *            number of milliseconds to wait before assuming the connection failed
      *            (values < 1 indicate no timeout)
      * @throws IOException
      *             if the connection could not be completed
      */
     public void connect(String host, int port, final long timeout) throws IOException
     {
         connect(new InetSocketAddress(host, port), timeout);
     }
 
     /**
      * Connect to a server. This method will block until the connection completes.
      * If a connection is already active, this method will do nothing.
      *
      * @see #connect(String, int, long)
      *
      * @param addr
      *            the server location
      * @param timeout
      *            number of milliseconds to wait before assuming the connection failed
      *            (values < 1 indicate no timeout)
      * @throws IOException
      *             if the connection could not be completed
      */
     public void connect(InetSocketAddress addr, final long timeout) throws IOException
     {
         if (isConnected())
         {
             return;
         }
 
         logger.info("Connecting to {}:{}", addr.getHostName(), addr.getPort());
 
         socket = SocketChannel.open();
         socket.configureBlocking(true);
 
         final Thread connectionThread = Thread.currentThread();
         Thread timeoutThread = new Thread("Connection Timeout Listener")
         {
             @Override
             public void run()
             {
                 try
                 {
                     if (timeout < 1)
                     {
                         return;
                     }
 
                     logger.trace("Starting connection timeout for {} milliseconds", timeout);
                     Thread.sleep(timeout);
 
                     logger.error("Connection timed out after {} milliseconds", timeout);
                     connectionThread.interrupt();
                 }
                 catch (InterruptedException e)
                 {
                     /*
                      * Let this thread die when it's interrupted.
                      */
                     logger.trace("Connection completed, stopping timeout thread");
                 }
             }
         };
 
         timeoutThread.start();
         try
         {
             socket.connect(addr);
         }
         finally
         {
             timeoutThread.interrupt();
         }
 
         logger.info("Connected");
 
         socket.configureBlocking(false);
         openSelectors();
         startReceiver();
     }
 
     /**
      * Open the read and write selectors. This needs to be done before data can be send
      * through this socket manager using its own API (not the {@link #redirectChannel()
      * redirected channel}). These selectors should be {@link #closeSelectors() closed}
      * before redirecting the channel to prevent corruption.
      *
      * @throws IOException
      *             if an error occurs while opening either selector
      */
     private void openSelectors() throws IOException
     {
         readSelector = Selector.open();
         socket.register(readSelector, SelectionKey.OP_READ);
 
         writeSelector = Selector.open();
         socket.register(writeSelector, SelectionKey.OP_WRITE);
     }
 
     /**
      * Close the read and write selectors. This will prevent any further communication to
      * the connected server through this manager until the selectors are
      * {@link #openSelectors() opened} again. This should happen before
      * {@link #redirectChannel() redirecting the channel}. If any errors occur here they
      * will be logged, but not thrown.
      */
     private void closeSelectors()
     {
         if (readSelector != null)
         {
             try
             {
                 readSelector.close();
             }
             catch (IOException e)
             {
                 logger.debug("Error while closing read selector", e);
             }
         }
 
         if (writeSelector != null)
         {
             try
             {
                 writeSelector.close();
             }
             catch (IOException e)
             {
                 logger.debug("Error while closing write selector", e);
             }
         }
     }
 
     /**
      * Start the receiver thread. This thread will wait for data to arrive from the
      * connected server and deal with it (as either a response or an unsolicited
      * message). The reciever must be started before communication can proceed, but it
      * should be {@link #stopReceiver() stopped} before redirecting the channel to prevent
      * corruption.
      */
     private void startReceiver()
     {
         if (receiver != null)
         {
             return;
         }
 
         receiver = receiverExecutor.submit(new Runnable()
         {
             @Override
             public void run()
             {
                 while (true)
                 {
                     try
                     {
                         if (readSelector.select() != 1 || Thread.interrupted())
                         {
                             break;
                         }
 
                         readSelector.selectedKeys().clear();
 
                         String value = packet.read(socket);
 
                         logger.trace("Received message: {}", value);
 
                         if (interceptor != null
                             && interceptor.intercept(value))
                         {
                             continue;
                         }
 
                         /*
                          * If the client stops waiting for a response, it will increment
                          * this value. To keep things in sync, those skipped responses
                          * need to be thrown away when they arrive.
                          */
                         if (skippedResponses.get() > 0)
                         {
                             skippedResponses.decrementAndGet();
                             continue;
                         }
 
                         queue.add(value);
                     }
                     catch (InterruptedIOException e)
                     {
                         logger.info("Receiver interrupted");
                         break;
                     }
                     catch (IOException e)
                     {
                         logger.error("Connection error", e);
 
                         disconnect();
                         break;
                     }
                 }
             }
         });
     }
 
     /**
      * Stop the receiver thread from pulling incoming data off the channel. Once this
      * occurs, communication from the server will be ignored. This must occur before
      * {@link #redirectChannel() redirecting the channel}.
      */
     private void stopReceiver()
     {
         if (receiver == null)
         {
             return;
         }
 
         receiver.cancel(true);
         receiver = null;
     }
 
     /**
      * Determine whether or not this manager has an active connection to a server.
      *
      * @return <code>true</code> if the manager is connected; <code>false</code> otherwise
      */
     public boolean isConnected()
     {
         return socket != null && socket.isConnected();
     }
 
     /**
      * Retrieve the address to which this instance is currently connected.
      *
      * @see #isConnected()
      *
      * @return the connected address or <code>null</code> if this manager is not connected
      */
     public InetSocketAddress getConnectedAddress()
     {
         return isConnected() ? (InetSocketAddress)socket.socket().getRemoteSocketAddress() : null;
     }
 
     /**
      * Create a copy of this manager that is connected to the same server (if this
      * instance is connected).
      *
      * @see #isConnected()
      *
      * @return the new manager instance
      * @throws IOException
      *             if the connection could not be completed
      */
     public SocketManager newConnection() throws IOException
     {
         SocketManager newManager = new SocketManager(packet);
 
         if (isConnected())
         {
             newManager.connect(getConnectedAddress(), 0);
         }
 
         return newManager;
     }
 
     /**
      * Close the active connection and clean up any resources associated with it. If there
      * is no active connection, this method will make sure that resources are cleaned up
      * and return.
      */
     public void disconnect()
     {
         /*
          * Make sure the receiver thread is cancelled and forgotten in case this
          * disconnect call is made after the socket has already been closed for some other
          * reason (like an error).
          */
         stopReceiver();
 
         if (!isConnected())
         {
             return;
         }
 
         logger.info("Disconnecting");
 
         closeSelectors();
 
         try
         {
             socket.close();
         }
         catch (IOException e)
         {
             logger.debug("Error while closing socket", e);
         }
 
         logger.info("Disconnected");
     }
 
     /**
      * Send a message over the active connection. This method will not wait for a response
      * and should not be used with messages that will trigger a response from the server.
      *
      * @param message
      *            the message to send
      * @throws IOException
      *             if this manager is not connected to a server, is interrupted while
      *             sending the message, or some other communication error occurs
      */
     public void send(String message) throws IOException
     {
         logger.trace("Sending message: {}", message);
 
         if (writeSelector.select() != 1 || Thread.interrupted())
         {
             throw new InterruptedIOException();
         }
 
         writeSelector.selectedKeys().clear();
 
         packet.write(socket, message);
 
         logger.trace("Message sent");
     }
 
     /**
      * Send a message to the server and wait for a response. This method will wait
      * indefinitely and should not be used with messages that do not or may not cause the
      * server to respond.
      *
      * @param message
      *            the message to send
      * @return the response from the server or <code>null</code> if the thread is
      *         interrupted
      * @throws IOException
      *             if this manager is not connected to a server or some other
      *             communication error occurs
      */
     public String sendAndWait(String message) throws IOException
     {
         return sendAndWait(message, 0, null);
     }
 
     /**
      * Send a message to the server and wait for a response up to the given
      * timeout value.
      *
      * @param message
      *            the message to send
      * @param timeout
      *            the length of time in units of <code>unit</code> to wait for a
      *            response
      * @param unit
      *            units used to interpret the <code>timeout</code> parameter
     * @return the response from the server or <code>null</code> if the thread
      *         is interrupted or the timeout is reached
      * @throws IOException
      *             if this manager is not connected to a server or some other
      *             communication error occurs
      */
     public String sendAndWait(String message, long timeout, TimeUnit unit) throws IOException
     {
         synchronized (Lock.SEND_AND_WAIT)
         {
             send(message);
 
             try
             {
                 logger.trace("Waiting for reply");
 
                 if (timeout < 1)
                 {
                     return queue.take();
                 }
 
                 String response = queue.poll(timeout, unit);
                 if (response == null)
                 {
                     /*
                      * Indicate to the receiver thread that this response is being skipped
                      * so that it can be thrown away when it arrives.
                      */
                     skippedResponses.incrementAndGet();
 
                     response = "";
                 }
 
                 return response;
             }
             catch (InterruptedException e)
             {
                 logger.info("Interrupted while waiting for response", e);
                 return "";
             }
         }
     }
 
     /**
      * Redirect the data channel used by this socket manager. While the channel is redirected, normal
      * communication is suspended. To release the redirected channel, simply close
      * it.
      *
      * @return the redirected channel stream
      */
     public ByteChannel redirectChannel()
     {
         synchronized (Lock.REDIRECT_CHANNEL)
         {
             stopReceiver();
             closeSelectors();
 
             if (redirect == null)
             {
                 redirect = new RedirectedChannel();
             }
 
             return redirect;
         }
     }
 
     private enum Lock
     {
         SEND_AND_WAIT, REDIRECT_CHANNEL
     }
 
     private class RedirectedChannel implements ByteChannel
     {
         private volatile boolean closed;
 
         @Override
         public void close() throws IOException
         {
             synchronized (Lock.REDIRECT_CHANNEL)
             {
                 if (closed)
                 {
                     return;
                 }
 
                 closed = true;
                 redirect = null;
 
                 openSelectors();
                 startReceiver();
             }
         }
 
         @Override
         public int read(ByteBuffer dst) throws IOException
         {
             checkClosed();
             return socket.read(dst);
         }
 
         @Override
         public int write(ByteBuffer src) throws IOException
         {
             checkClosed();
             return socket.write(src);
         }
 
         @Override
         public boolean isOpen()
         {
             return !closed;
         }
 
         private void checkClosed() throws IOException
         {
             if (!isOpen())
             {
                 throw new IOException("redirected channel is no longer accessible");
             }
         }
     }
 }
