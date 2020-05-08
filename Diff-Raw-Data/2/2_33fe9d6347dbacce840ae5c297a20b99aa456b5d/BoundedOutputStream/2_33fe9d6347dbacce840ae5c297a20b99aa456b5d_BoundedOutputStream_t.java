 package fi.jpalomaki.ssh.jsch;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import fi.jpalomaki.ssh.util.Assert;
 
 /**
  * An {@link OutputStream} that limits the number of bytes written to it.
  *  
  * @author jpalomaki
  */
 final class BoundedOutputStream extends OutputStream {
 
     private final long maxBytes;
    private long bytesWritten = 0L;
     private final OutputStream sink;
     
     public BoundedOutputStream(long maxBytes, OutputStream sink) {
         Assert.isTrue(maxBytes > 0L, "Max bytes must be > 0");
         Assert.notNull(sink, "Sink must not be null");
         this.maxBytes = maxBytes;
         this.sink = sink;
     }
     
     @Override
     public void write(int b) throws IOException {
         if (bytesWritten++ > maxBytes) {
             throw new IOException("Exceeded max bytes written: " + maxBytes);
         }
         sink.write(b);
     }
 
     @Override
     public void flush() throws IOException {
         sink.flush();
     }
 
     @Override
     public void close() throws IOException {
         sink.close();
     }
 }
