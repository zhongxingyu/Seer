 /*
  * Created On:  26-Oct-06 6:22:07 PM
  */
 package com.thinkparity.codebase.model.stream;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.httpclient.methods.GetMethod;
 
 /**
  * <b>Title:</b>thinkParity Stream Reader<br>
  * <b>Description:</b>A stream client used to download streams from the server.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.8
  */
 public final class StreamReader {
 
     /** A stream monitor. */
     private final StreamMonitor monitor;
 
     /** A stream session. */
     private final StreamSession session;
 
     private OutputStream stream;
 
     /** A set of stream utilities. */
     private final StreamUtils utils;
 
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
         this.monitor = monitor;
         this.session = session;
         this.utils = new StreamUtils();
     }
 
     /**
      * Read a stream.
      * 
      * @param stream
      *            A target <code>OutputStream</code>.
      */
     public void read(final OutputStream stream) {
         this.stream = stream;
         try {
             executeGet();
         } catch (final IOException iox) {
             throw new StreamException(iox);
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
                         try {
                             while ((len = input.read(b)) > 0) {
                                 stream.write(b, 0, len);
                                 stream.flush();
                                 fireChunkReceived(len);
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
