 /*
  ** 2014 February 22
  **
  ** The author disclaims copyright to this source code.  In place of
  ** a legal notice, here is a blessing:
  **    May you do good and not evil.
  **    May you find forgiveness for yourself and forgive others.
  **    May you share freely, never taking more than you give.
  */
 package info.ata4.io.buffer;
 
 import java.io.DataOutput;
 import java.io.IOException;
 import java.nio.BufferOverflowException;
 import java.nio.ByteBuffer;
 
 /**
  * DataOutput wrapper for byte buffers.
  * 
  * @author Nico Bergemann <barracuda415 at yahoo.de>
  */
 public class ByteBufferDataOutput implements DataOutput {
     
     private final ByteBuffer buf;
     
     public ByteBufferDataOutput(ByteBuffer buf) {
         this.buf = buf;
     }
     
     public ByteBuffer getByteBuffer() {
         return buf;
     }
 
     @Override
     public void write(int b) throws IOException {
         writeByte(b);
     }
 
     @Override
     public void write(byte[] b) throws IOException {
         try {
             buf.put(b);
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void write(byte[] b, int off, int len) throws IOException {
         try {
             buf.put(b, off, len);
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeBoolean(boolean v) throws IOException {
         try {
             write(v ? (byte) 1 : (byte) 0);
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeByte(int v) throws IOException {
         try {
             buf.put((byte) (v & 0xff));
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeShort(int v) throws IOException {
         try {
             buf.putShort((short) (v & 0xffff));
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeChar(int v) throws IOException {
         try {
            buf.putChar((char) (v & 0xffff));
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeInt(int v) throws IOException {
         try {
             buf.putInt(v);
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeLong(long v) throws IOException {
         try {
             buf.putLong(v);
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeFloat(float v) throws IOException {
         try {
             buf.putFloat(v);
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeDouble(double v) throws IOException {
         try {
             buf.putDouble(v);
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeBytes(String s) throws IOException {
         write(s.getBytes());
     }
 
     @Override
     public void writeChars(String s) throws IOException {
         try {
             final int len = s.length();
             for (int i = 0; i < len; i++) {
                 writeChar(s.charAt(i));
             }
         } catch (BufferOverflowException ex) {
             throw new IOException(ex);
         }
     }
 
     @Override
     public void writeUTF(String s) throws IOException {
         write(s.getBytes("UTF-8"));
     }
     
 }
