 /*
  ** 2013 June 15
  **
  ** The author disclaims copyright to this source code.  In place of
  ** a legal notice, here is a blessing:
  **    May you do good and not evil.
  **    May you find forgiveness for yourself and forgive others.
  **    May you share freely, never taking more than you give.
  */
 package info.ata4.io;
 
 import info.ata4.io.util.HalfFloat;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import org.apache.commons.io.EndianUtils;
 
 /**
  * DataOutput extension for more data access methods.
  * 
  * @author Nico Bergemann <barracuda415 at yahoo.de>
  */
 public class DataOutputWriter extends DataOutputWrapper implements DataOutputExtended {
     
     private static final String DEFAULT_CHARSET = "ASCII";
     private boolean swap;
     
     public DataOutputWriter(DataOutput out) {
         super(out);
     }
     
     public DataOutputWriter(RandomAccessFile raf) {
         super(new RandomAccessFileWrapper(raf));
     }
     
     public DataOutputWriter(OutputStream is) {
         super(new DataOutputStream(is));
     }
     
     public DataOutputWriter(ByteBuffer bb) {
         super(new ByteBufferOutput(bb));
     }
     
     public OutputStream getOutputStream() {
         DataOutput out = getWrapped();
         
         // try to find the most direct way to stream the wrapped object
         if (out instanceof InputStream) {
             return (OutputStream) out;
        } else if (out instanceof ByteBufferOutput) {
             ByteBuffer bb = ((ByteBufferOutput) out).getBuffer();
             return new ByteBufferOutputStream(bb);
         } else {
             return new InverseDataOutputStream(this);
         }
     }
     
     @Override
     public boolean isSwap() {
         if (super.isSwappable()) {
             return super.isSwap();
         } else {
             return swap;
         }
     }
 
     @Override
     public void setSwap(boolean swap) {
         if (super.isSwappable()) {
             super.setSwap(swap);
         } else {
             this.swap = swap;
         }
     }
 
     @Override
     public boolean isSwappable() {
         // supports manual swapping using EndianUtils if required
         return true;
     }
     
     @Override
     public void writeShort(int v) throws IOException {
         if (swap) {
             v = EndianUtils.swapShort((short) v);
         }
         super.writeShort(v);
     }
     
     @Override
     public void writeInt(int v) throws IOException {
         if (swap) {
             v = EndianUtils.swapInteger(v);
         }
         super.writeInt(v);
     }
     
     @Override
     public void writeFloat(float v) throws IOException {
         if (swap) {
             // NOTE: don't use writeFloat() plus EndianUtils.swapFloat() here!
             writeInt(Float.floatToRawIntBits(v));
         } else {
             super.writeFloat(v);
         }
     }
     
     @Override
     public void writeDouble(double v) throws IOException {
         if (swap) {
             // NOTE: don't use writeDouble() plus EndianUtils.swapDouble() here!
             writeLong(Double.doubleToRawLongBits(v));
         } else {
             super.writeDouble(v);
         }
     }
 
     @Override
     public void writeLong(long v) throws IOException {
         if (swap) {
             v = EndianUtils.swapLong(v);
         }
         super.writeLong(v);
     }
     
     @Override
     public void writeHalf(float f) throws IOException {
         int sval = HalfFloat.floatToIntBits(f);
         writeShort(sval);
     }
 
     @Override
     public void writeStringNull(String str, String charset) throws IOException {
         writeStringFixed(str, charset);
         writeByte(0);
     }
     
     @Override
     public void writeStringNull(String str) throws IOException {
         writeStringNull(str, DEFAULT_CHARSET);
     }
     
     @Override
     public void writeStringPadded(String str, int padding, String charset) throws IOException {
         int nullBytes = padding - str.length();
         if (nullBytes < 0) {
             throw new IllegalArgumentException("Invalid padding");
         }
         
         writeStringFixed(str, charset);
         skipBytes(nullBytes);
     }
 
     @Override
     public void writeStringPadded(String str, int padding) throws IOException {
         writeStringPadded(str, padding, DEFAULT_CHARSET);
     }
     
     @Override
     public void writeStringFixed(String str, String charset) throws IOException {
         write(str.getBytes(charset));
     }
     
     @Override
     public void writeStringFixed(String str) throws IOException {
         writeStringFixed(str, DEFAULT_CHARSET);
     }
     
     @Override
     public void writeStringInt(String str, String charset) throws IOException {
         writeInt(str.length());
         writeStringFixed(str, charset);
     }
 
     @Override
     public void writeStringInt(String str) throws IOException {
         writeStringInt(str, DEFAULT_CHARSET);
     }
 
     @Override
     public void writeStringShort(String str, String charset) throws IOException {
         if (str.length() > 0xffff) {
             throw new IllegalArgumentException("String is too long");
         }
         
         writeShort(str.length());
         writeStringFixed(str, charset);
     }
 
     @Override
     public void writeStringShort(String str) throws IOException {
         writeStringShort(str, DEFAULT_CHARSET);
     }
 
     @Override
     public void writeStringByte(String str, String charset) throws IOException {
         if (str.length() > 0xff) {
             throw new IllegalArgumentException("String is too long");
         }
         
         writeByte(str.length());
         writeStringFixed(str, charset);
     }
 
     @Override
     public void writeStringByte(String str) throws IOException {
         writeStringByte(str, DEFAULT_CHARSET);
     }
     
     @Override
     public void skipBytes(int n) throws IOException {
         for (int i = 0; i < n; i++) {
             writeByte(0);
         }
     }
 
     @Override
     public void align(int length, int align) throws IOException {
         if (align > 0) {
             int rem = length % align;
             if (rem != 0) {
                 skipBytes(align - rem);
             }
         }
     }
 }
