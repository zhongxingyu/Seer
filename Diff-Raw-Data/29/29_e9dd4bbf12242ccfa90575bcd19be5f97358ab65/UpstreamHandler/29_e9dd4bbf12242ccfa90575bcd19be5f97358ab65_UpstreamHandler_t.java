 /**
  * 
  */
 package com.thinkparity.desdemona.model.stream;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.thinkparity.codebase.model.stream.StreamSession;
 
 /**
  * <b>Title:</b>thinkParity DesdemonaModel Upstream Handler<br>
  * <b>Description:</b>A runnable that will deliver a stream provided by the
  * input stream in the constructor to the stream server. The input stream
  * provided will be notified when complete by writing the stream id.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class UpstreamHandler extends AbstractStreamHandler implements
         Runnable {
     
     /** The <code>InputStream</code> to upload. */
     private final InputStream input;
 
     /** The <code>InputStream</code> to upload. */
     private final OutputStream output;
 
     /**
      * Create UpstreamHandler.
      * 
      * @param streamServer
      *            The <code>StreamServer</code>.
      * @param streamSession
      *            The <code>StreamSession</code>.
      * @param streamId
      *            The stream id <code>String</code>.
      * @param streamOffset
      *            The offset <code>Long</code> within the stream to upload
      *            from.
      * @param streamSize
      *            The size <code>Long</code> of the stream to upload.
      * @param input
      *            The <code>InputStream</code> to upload.
      * @param output
      *            The <code>OutputStream</code> to notify upon completion.
      */
     UpstreamHandler(final StreamServer streamServer,
             final StreamSession streamSession, final String streamId,
             final Long streamOffset, final Long streamSize,
             final InputStream input, final OutputStream output) {
         super(streamServer, streamSession, streamId, streamOffset, streamSize);
         this.input = input;
         this.output = output;
     }
 
     /**
      * Run the upstream socket handler. This will read the socket chanel and
      * look for key headers to initiate a upstream transfer. When the transfer
      * is complete; the stream id is written back to the underlying stream.
      * 
      * 
      */
     public void run() {
         LOGGER.logDebug("{0}  Begin upstream session.", streamId);
         try {
             final OutputStream output =
                 streamServer.openForUpstream(session, streamId, streamOffset);
             int len;
             long total = streamOffset.longValue();
             final byte[] b = new byte[session.getBufferSize()];
             try {
                 while (total < streamSize) {
                     len = input.read(b);
                     output.write(b, 0, len);
                     output.flush();
                     LOGGER.logDebug("{0}  Upstream bytes recieved:  {1}/{2}",
                             streamId, total += len, streamSize);
                 }
             } finally {
                 try {
                     output.flush();
                 } finally {
                     output.close();
                 }
             }
             streamServer.finalizeStream(session, streamId);
             // write back the stream id to notify completion
             this.output.write(streamId.getBytes());
             this.output.flush();
             LOGGER.logDebug("{0}  Upstream session complete.", streamId);
         } catch (final IOException iox) {
            LOGGER.logError(iox, "{0}  Upstream transfer error.", streamId);
             throw new StreamException(iox);
         }
     }
 }
