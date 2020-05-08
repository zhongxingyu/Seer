 /*
  * Created On:  4-Jun-07 9:28:26 AM
  */
 package com.thinkparity.desdemona.model.queue.notification;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 
 import com.thinkparity.codebase.model.queue.notification.NotificationException;
 import com.thinkparity.codebase.model.queue.notification.NotificationHeader;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 final class NotificationSocketDelegate implements Runnable {
 
     /** A log4j wrapper. */
     private final Log4JWrapper logger;
 
     /** Whether to send a notification or to send the current date/time. */
     private boolean notify;
 
     /** The notification server. */
     private final NotificationServer server;
 
     private byte[] sessionIdBytes;
 
     /** A client socket. */
     private final Socket socket;
 
     /**
      * Create NotificationSocketDelegate.
      * 
      * @param socket
      *            A client <code>Socket</code>.
      */
     NotificationSocketDelegate(final NotificationServer server,
             final Socket socket) {
         super();
         this.logger = new Log4JWrapper("NotificationService");
         this.server = server;
         this.socket = socket;
     }
 
     /**
      * @see java.lang.Runnable#run()
      *
      */
     public void run() {
         logger.logTraceId();
         logger.logInfo("Starting notification delegate {0}.", getRemoteAddress());
         final NotificationHeader sessionId;
         try {
             final HeaderReader reader = new HeaderReader();
             sessionId = reader.readNext();
             if (null == sessionId) {
                 logger.logWarning("Missing session id header.");
             } else {
                 server.getSession(sessionId.getValue()).setDelegate(this);
                 sessionIdBytes = sessionId.getValue().getBytes(server.getCharset());
             }
         } catch (final IOException iox) {
             throw new NotificationException(iox);
         }
         while (true) {
             try {
                 synchronized (this) {
                    // TIMEOUT - NotificationSocketDelegate#run() - 2.5s
                    wait(2500);
                 }
             } catch (final InterruptedException ix) {}
             logger.logInfo("Waking up notification delegate {0}.", sessionId.getValue());
             if (isWritable()) {
                 try {
                     socket.getOutputStream().write(getWriteBytes());
                 } catch (final IOException iox) {
                     logger.logError(iox,
                             "Could not write to notification delegate {0}.",
                             getRemoteAddress());
                     break;
                 }
             } else {
                 break;
             }
         }
        server.removeSession(sessionId.getValue());
     }
 
     /**
      * Obtain the remote address of the delegate.
      * 
      * @return The socket's remote socket address <code>String</code>.
      */
     String getRemoteAddress() {
         return socket.getRemoteSocketAddress().toString();
     }
 
     /**
      * Send the notification bytes.
      *
      */
     void sendNotify() {
         notify = true;
         synchronized (this) {
             notify();
         }
     }
 
     /**
      * Obtain the bytes to write to the socket. If notify is set; write the
      * session id otherwise write the current date/time.
      * 
      * @return A <code>byte[]</code>.
      */
     private byte[] getWriteBytes() {
         if (notify) {
             notify = false;
             return sessionIdBytes;
         } else {
             return String.valueOf(System.currentTimeMillis()).getBytes(server.getCharset());
         }
     }
 
     /**
      * Determine if the socket is writable.
      * 
      * @return True if the socket is writable.
      */
     private boolean isWritable() {
         return socket.isBound() && socket.isConnected() && !socket.isClosed()
                 && !socket.isOutputShutdown() && !socket.isInputShutdown();
     }
 
      /**
      * <b>Title:</b>thinkParity StreamSocketDelegate Header Reader<br>
      * <b>Description:</b>Reads an input stream a character at a time until a
      * header can be constructed.<br>
      * 
      * @author raymond@thinkparity.com
      * @version 1.1.2.1
      */
     private final class HeaderReader {
 
         /** An apache logger wrapper. */
         private final Log4JWrapper logger;
 
         /** An <code>InputStream</code>. */
         private final InputStream stream;
 
         /**
          * Create HeaderReader.
          * 
          */
         private HeaderReader() throws IOException {
             super();
             this.logger = new Log4JWrapper();
             this.stream = socket.getInputStream();
         }
 
         /**
          * Read the next byte of the stream.
          * 
          * @return The next byte of the stream; or -1 if the stream is at an
          *         end.
          * @throws IOException
          */
         private final int read() throws IOException {
             return stream.read();
         }
 
         /**
          * Read the next notification header.
          * 
          * @return A notification header; or null if the end of stream is
          *         reached.
          * @throws IOException
          */
         private final NotificationHeader readNext() throws IOException {
             int nextByte;
             final StringBuffer headerBuffer = new StringBuffer();
             while (-1 != (nextByte = read())) {
                 final char c = new String(new byte[] { (byte) nextByte },
                         server.getCharset().name()).charAt(0);
                 headerBuffer.append(c);
                 if (';' == c) {
                     /*
                      * here we have just completed what looks like a
                      * header declaration - we analyze the header and
                      * take appropriate action
                      */
                     logger.logVariable("header", headerBuffer);
                     return NotificationHeader.valueOf(headerBuffer.toString());
                 }
             }
             return null;
         }
     }
 }
