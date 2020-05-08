 /*
  * Created On: Oct 22, 2006 3:46:10 PM
  */
 package com.thinkparity.codebase.model.stream;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.net.SocketException;
 
 import com.thinkparity.codebase.model.util.http.HttpUtils;
 
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 
 /**
  * <b>Title:</b>thinkParity CommonModel Stream Writer<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class StreamWriter implements RequestEntity {
 
     /** A stream monitor. */
     private final StreamMonitor monitor;
 
     /** A stream session. */
     private final StreamSession session;
 
     /** A stream. */
     private InputStream stream;
 
     /** A stream size. */
     private long streamSize;
 
     /** A set of stream utilities. */
     private StreamUtils utils;
 
     /**
      * Create StreamWriter.
      * 
      * @param session
      *            A stream <code>Session</code>.
      */
     public StreamWriter(final StreamMonitor monitor, final StreamSession session) {
         super();
         this.monitor = monitor;
         this.session = session;
         this.utils = new StreamUtils();
     }
 
     /**
      * @see org.apache.commons.httpclient.methods.RequestEntity#getContentLength()
      *
      */
     public long getContentLength() {
         return streamSize;
     }
 
     /**
      * @see org.apache.commons.httpclient.methods.RequestEntity#getContentType()
      *
      */
     public String getContentType() {
         return HttpUtils.ContentTypeNames.BINARY_OCTET_STREAM;
     }
 
     /**
      * @see org.apache.commons.httpclient.methods.RequestEntity#isRepeatable()
      *
      */
     public boolean isRepeatable() {
         return false;   // cannot re-open the input stream from here
     }
 
     /**
      * Write a stream.
      * 
      * @param stream
      *            An <code>InputStream</code>.
      * @param streamSize
      *            The stream size <code>Long</code>.
      */
     public void write(final InputStream stream, final Long streamSize) {
         this.stream = stream;
         this.streamSize = streamSize;
         try {
             executePut();
         } catch (final IOException iox) {
             throw new StreamException(iox);
         }
     }
 
     /**
      * @see org.apache.commons.httpclient.methods.RequestEntity#writeRequest(java.io.OutputStream)
      *
      */
     public void writeRequest(final OutputStream out) throws IOException {
         int len = 0;
         final byte[] b = new byte[session.getBufferSize()];
         while ((len = stream.read(b)) > 0) {
             out.write(b, 0, len);
             out.flush();
             fireChunkSent(len);
         }
     }
 
     /**
      * Execute a http put; writing the stream.
      * 
      * @throws HttpException
      * @throws IOException
      */
     private void executePut() throws IOException {
         StreamClientMetrics.begin(session);
         try {
             final PutMethod method = new PutMethod(session.getURI());
             try {
                 utils.setHeaders(method, session.getHeaders());
                 method.setRequestEntity(this);
                 switch (utils.execute(method)) {
                 case 200:
                     break;
                 case 500:
                     utils.writeError(method);
                     throw new StreamException(Boolean.TRUE,
                             "Could not upload stream.  {0}:{1}{2}{3}",
                             method.getStatusCode(), method.getStatusLine(),
                             "\n\t", method.getStatusText());
                 default:
                     utils.writeError(method);
                     throw new StreamException(
                             "Could not upload stream.  {0}:{1}{2}{3}",
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
             StreamClientMetrics.end("PUT", session);
         }
     }
 
     /**
      * Notify the client monitor that a chunk has been sent.
      * 
      * @param chunkSize
      *            The <code>int</code> size of the chunk.
      */
     private void fireChunkSent(final int chunkSize) {
         utils.logMonitorChunkSent(monitor, chunkSize);
         try {
             monitor.chunkSent(chunkSize);
         } catch (final Throwable cause) {
             utils.logMonitorError(monitor, cause);
         }
     }
 }
