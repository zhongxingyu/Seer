 package org.littleshoot.util;
 
 import java.io.IOException;
import java.io.OutputStream;
 
 /**
  * An output stream that write encrypted records whose integrity is verified
  * with a SHA-256 MAC.
  */
 public class EncryptingOutputStream extends OutputStream {
 
     private final OutputStream os;
     private final byte[] key;
 
     /**
      * Creates a new encrypting output stream that uses the specified key. 
      * 
      * @param key The key to encrypt with.
      * @param os The {@link OutputStream} to wrap.
      */
     public EncryptingOutputStream(final byte[] key, final OutputStream os) {
         this.key = key;
         this.os = os;
     }
 
     /**
      * Write the data out, NOW.
      */
     @Override
    synchronized public void write(final byte data[], final int off, 
        final int len) throws IOException {
         
         // TODO: Ideally we'd make sure to fill up each message as much as
         // we can, but this will work for now!
         final byte[] encoded = CommonUtils.encode(this.key, data, off, len);
         os.write(encoded);
     }
 
     /**
      * Write one byte now.
      */
     @Override
     synchronized public void write(int i) throws IOException {
         write(new byte[] {(byte)i}, 0, 1);
     }
 
     @Override
     public void close() throws IOException {
        this.os.close();
     }
 
     // inherit no-op flush()
 }
