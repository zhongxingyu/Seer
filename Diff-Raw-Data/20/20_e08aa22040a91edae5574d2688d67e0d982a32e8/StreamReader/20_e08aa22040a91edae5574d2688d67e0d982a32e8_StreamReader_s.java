 /*
  * Created On:  26-Oct-06 6:22:07 PM
  */
 package com.thinkparity.codebase.model.stream;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.SocketException;
 
 import com.thinkparity.codebase.delegate.CancelException;
 import com.thinkparity.codebase.delegate.Cancelable;
 
 import org.apache.commons.httpclient.methods.GetMethod;
 
 /**
  * <b>Title:</b>thinkParity Stream Reader<br>
  * <b>Description:</b>A stream client used to download streams from the server.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.8
  */
 public final class StreamReader implements Cancelable {
 
     /** A cancel indicator. */
     private boolean cancel;
 
     /** A stream monitor. */
     private final StreamMonitor monitor;
 
     /** A stream session. */
     private final StreamSession session;
 
     private OutputStream stream;
 
     /** A set of stream utilities. */
     private final StreamUtils utils;
 
     /** A run indicator. */
     private boolean running;
 
     /**
      * Create StreamReader.
      * 
      * @param monitor
      *            A <code>StreamMonitor</code>.
      * @param session
      *            A <code>StreamSession</code>.
      */
     public StreamReader(final StreamMonitor monitor, final StreamSession session) {
         super();
         this.cancel = false;
         this.monitor = monitor;
         this.running = false;
         this.session = session;
         this.utils = new StreamUtils();
     }
 
     /**
      * @see com.thinkparity.codebase.delegate.Cancelable#cancel()
      *
      */
     public void cancel() throws CancelException {
         this.cancel = true;
         if (running) {
             synchronized (this) {
                 try {
                     wait();
                 } catch (final InterruptedException ix) {
                     throw new CancelException(ix);
                 }
             }
         }
     }
 
     /**
      * Read a stream.
      * 
      * @param stream
      *            A target <code>OutputStream</code>.
      */
     public void read(final OutputStream stream) {
         this.stream = stream;
         running = true;
         try {
             executeGet();
         } catch (final IOException iox) {
             throw new StreamException(iox);
         } finally {
             running = false;
             synchronized (this) {
                 notifyAll();
             }
         }
     }
 
     /**
      * Execute the http get method that will download the stream.
      * 
      * @throws IOException
      */
     private void executeGet() throws IOException {
         StreamClientMetrics.begin(session);
         try {
             final GetMethod method = new GetMethod(session.getURI());
             try {
                 utils.setHeaders(method, session.getHeaders());
                 switch (utils.execute(method)) {
                 case 200:
                     final InputStream input = method.getResponseBodyAsStream();
                     try {
                         int len = 0;
                         final byte[] b = new byte[session.getBufferSize()];
                         if (cancel) {
                             return;
                         }
                         try {
                             while ((len = input.read(b)) > 0) {
                                 if (cancel) {
                                     return;
                                 }
                                 stream.write(b, 0, len);
                                 stream.flush();
                                 fireChunkReceived(len);
                                 if (cancel) {
                                     return;
                                 }
                             }
                         } finally {
                             stream.flush();
                         }
                     } finally {
                         input.close();
                     }
                     break;
                 case 500:
                     utils.writeError(method);
                     throw new StreamException(Boolean.TRUE,
                             "Could not download stream.  {0}:{1}{2}{3}",
                             method.getStatusCode(), method.getStatusLine(),
                             "\n\t", method.getStatusText());
                 default:
                     utils.writeError(method);
                     throw new StreamException(
                             "Could not download stream.  {0}:{1}{2}{3}",
                             method.getStatusCode(), method.getStatusLine(),
                             "\n\t", method.getStatusText());
                 }
             } catch (final SocketException sx) {
                 utils.writeError(method);
                 throw new StreamException(Boolean.TRUE,
                         "Could not download stream.  {0}", sx.getMessage());
             } finally {
                 method.releaseConnection();
             }
         } finally {
             StreamClientMetrics.end("GET", session);
         }
     }
 
     /**
      * Notiry the client monitor that a chunk has been received.
      * 
      * @param chunkSize
      *            The <code>int</code> chunk size.
      */
     private void fireChunkReceived(final int chunkSize) {
         utils.logMonitorChunkReceived(monitor, chunkSize);
         try {
             monitor.chunkReceived(chunkSize);
         } catch (final Throwable cause) {
             utils.logMonitorError(monitor, cause);
         }
     }
 }
