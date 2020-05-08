 /*
  * Created On:  4-Jun-07 11:27:51 AM
  */
 package com.thinkparity.codebase.model.queue.notification;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.nio.charset.Charset;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 import com.thinkparity.codebase.net.SocketFactory;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public abstract class NotificationClient {
 
     /** A log4j wrapper. */
     protected static final Log4JWrapper LOGGER;
 
     /** A default notification monitor.  Uses logger to write updates */
     private static final NotificationMonitor DEFAULT_MONITOR;
 
     /** A java socket factory. */
     private static javax.net.SocketFactory SOCKET_FACTORY;
 
     static {
         LOGGER = new Log4JWrapper("NotificationClient");
         DEFAULT_MONITOR = new NotificationMonitor() {
             public void chunkReceived(final int chunkSize) {
                 LOGGER.logTraceId();
                 LOGGER.logVariable("chunkSize", chunkSize);
             }
             public void chunkSent(final int chunkSize) {
                 LOGGER.logTraceId();
                 LOGGER.logVariable("chunkSize", chunkSize);
             }
             public void headerReceived(final String header) {
                 LOGGER.logTraceId();
                 LOGGER.logVariable("header", header);
             }
             public void headerSent(final String header) {
                 LOGGER.logTraceId();
                 LOGGER.logVariable("header", header);
             }
             public void streamError(final NotificationException error) {
                 LOGGER.logTraceId();
                 LOGGER.logError(error, "A notification error has occured.");
             }
         };
         SOCKET_FACTORY = SocketFactory.getInstance();
     }
 
     /** The socket input stream. */
     private InputStream input;
 
     /** A <code>NotificationMonitor</code>. */
     private final NotificationMonitor monitor;
 
     /** The socket output stream. */
     private OutputStream output;
 
     /** A notification session. */
     private final NotificationSession session;
 
     /** The notification socket. */
     private Socket socket;
 
     /** The backing socket's address. */
     private final InetSocketAddress socketAddress;
 
     /**
      * Create NotificationClient.
      * 
      * @param monitor
      *            A notification monitor.
      * @param session
      *            A notifcation session.
      */
     protected NotificationClient(final NotificationMonitor monitor,
             final NotificationSession session) {
         super();
         this.monitor = monitor;
         this.session = session;
         this.socketAddress = new InetSocketAddress(session.getServerHost(),
                 session.getServerPort());
     }
 
     /**
      * Create NotificationClient.
      *
      * @param session
      *            A notifcation session.
      */
     protected NotificationClient(final NotificationSession session) {
         this(DEFAULT_MONITOR, session);
     }
 
     /**
      * Connect the notification client.
      * 
      * @throws IOException
      */
     protected final void connect() throws IOException {
         connectImpl();
         write(new NotificationHeader(NotificationHeader.Type.SESSION_ID, session.getId()));
     }
 
     /**
      * Disconnect the notification client.
      * 
      * @throws IOException
      */
     protected final void disconnect() throws IOException {
         try {
             output.flush();
         } finally {
             try {
                 output.close();
             } finally {
                 output = null;
                 try {
                     input.close();
                 } finally {
                     input = null;
                     try {
                         socket.close();
                     } finally {
                         socket = null;
                     }
                 }
             }
         }
     }
 
     /**
      * Obtain the client charset (encoding).
      * 
      * @return A <code>Charset</code>.
      */
     protected final Charset getCharset() {
         return session.getCharset();
     }
 
     /**
      * Obtain the notification session id.
      * 
      * @return A session id <code>String</code>.
      */
     protected final String getSessionId() {
         return session.getId();
     }
 
     /**
      * Determine if the client is connected.
      * 
      * @return True if the client is connected.
      */
     protected final Boolean isConnected() {
         return Boolean.valueOf(null != socket && socket.isConnected());
     }
 
     /**
      * Read from the socket input into the buffer.
      * 
      * @param buffer
      *            A buffer <code>byte[]</code>.
      */
     protected final void read(final byte[] buffer) {
         LOGGER.logTraceId();
         try {
             final int len = input.read(buffer);
             if (-1 == len) {
                 // the stream has been terminated from the other end
                 fireStreamError(new NotificationException("Remote disconnect."));
             } else {
                 fireChunkReceived(len);
             }
         } catch (final IOException iox) {
             fireStreamError(new NotificationException(iox));
         }
     }
 
     /**
      * Disconnect the notification client.
      * 
      * @throws IOException
      */
     protected final void terminate() throws IOException {
         input = null;
         output = null;
         socket = null;
     }
 
     /**
      * Connect implementation. Establish the socket connection and grab the io
      * streams.
      * 
      */
     private void connectImpl() throws IOException {
         socket = SOCKET_FACTORY.createSocket(
                 socketAddress.getAddress(), socketAddress.getPort());
        // TIMEOUT NotificationClient#connectImpl() - 35s
        socket.setSoTimeout(35 * 1000);
         input = socket.getInputStream();
         output = socket.getOutputStream();
     }
 
     /**
      * Notiry the monitor that a chunk has been received.
      * 
      * @param chunkSize
      *            The <code>int</code> chunk size.
      */
     private void fireChunkReceived(final int chunkSize) {
         try {
             monitor.chunkReceived(chunkSize);
         } catch (final Throwable t) {
             // do nothing; this is a monitor issue
             LOGGER.logError(t, "Notification monitor error:  {0}", session);
         }
     }
 
     /**
      * Notify the client monitor that a header has been sent.
      * 
      * @param header
      *            The <code>NotificationHeader</code>.
      */
     private void fireHeaderSent(final NotificationHeader header) {
         try {
             monitor.headerSent(header.toHeader());
         } catch (final Throwable t) {
             // do nothing; this is a client monitor issue
             LOGGER.logError(t, "Notification monitor error:  {0}", session);
         }
     }
 
     /**
      * Notify the client monitor that a stream error has occured.
      * 
      * @param error
      *            The <code>StreamException</code>.
      */
     private void fireStreamError(final NotificationException error) {
         try {
             monitor.streamError(error);
         } catch (final Throwable t) {
             // do nothing; this is a client monitor issue
             LOGGER.logError(t, "Stream monitor error:  {0}", session);
         }
     }
 
     /**
      * Panic. Check if the error is one that can be recovered from; and create
      * the appropriate error response.
      * 
      * @return A <code>NotificationException</code>.
      */
     private NotificationException panic(final Throwable t) {
         return new NotificationException(t);
     }
 
     /**
      * Write a notification header.
      * 
      * @param header
      *            A <code>NotificationHeader</code>.
      */
     private void write(final NotificationHeader header) {
         try {
             write(header.toHeader());
             fireHeaderSent(header);
         } catch (final IOException iox) {
             throw panic(iox);
         }
     }
 
     /**
      * Write a message to the output stream.
      * 
      * @param message
      *            A message <code>String</code>.
      * @throws IOException
      */
     private void write(final String message) throws IOException {
         Assert.assertTrue(isConnected(),
                 "{0} - No longer connected.", socketAddress);
         output.write(message.getBytes(session.getCharset().name()));
         output.flush();
     }
 }
