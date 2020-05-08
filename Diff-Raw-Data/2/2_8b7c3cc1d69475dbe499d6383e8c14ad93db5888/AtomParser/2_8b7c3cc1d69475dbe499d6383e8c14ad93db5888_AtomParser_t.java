 package com.hotcats.mp4artextractor.parse;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import com.hotcats.mp4artextractor.data.atom.Atom;
 
 public abstract class AtomParser {
 
   public static final int INT_SIZE = 4;
  public static final int LONG_SIZE = 8;
   public static final int TYPE_SIZE = 4;
   public static final byte[] FREE_BYTES = { 'f', 'r', 'e', 'e' };
   public static final byte[] FTYP_BYTES = { 'f', 't', 'y', 'p' };
   public static final byte[] MOOV_BYTES = { 'm', 'o', 'o', 'v' };
 
   private final FileInputStream fileInput;
   private int bytesRead;
   private final int size;
   private final long extendedSize;
 
   public AtomParser(FileInputStream fileInput, int bytesRead, int size,
       long extendedSize) {
     this.fileInput = fileInput;
     this.bytesRead = bytesRead;
     this.size = size;
     this.extendedSize = extendedSize;
   }
 
   public abstract Atom parse() throws IOException;
 
   protected int getBytesRead() {
     return bytesRead;
   }
 
   protected void skipRest() throws IOException {
     long toSkip = getSize() - getBytesRead();
     long skipped = fileInput.skip(toSkip);
     if (toSkip != skipped) {
       throw new IOException("expected to skip " + toSkip
           + " bytes but actually skipped " + skipped + " bytes.");
     }
   }
 
   public int getSize() {
     return size;
   }
 
   public long getExtendedSize() {
     return extendedSize;
   }
 
   public static AtomParser getAtomParser(FileInputStream fileInput)
       throws IOException {
     int bytesRead = 0;
 
     int size = readInt(fileInput);
     bytesRead += INT_SIZE;
 
     if (size == 0) {
       // last atom of file
     }
 
     byte[] type = readBytes(fileInput, TYPE_SIZE);
     bytesRead += TYPE_SIZE;
 
     long extendedSize = 0;
     if (size == 1) {
       extendedSize = readLong(fileInput);
       bytesRead += LONG_SIZE;
     }
 
     if (Arrays.equals(FTYP_BYTES, type)) {
       return new FtypAtomParser(fileInput, bytesRead, size, extendedSize);
     } else if (Arrays.equals(MOOV_BYTES, type)) {
       return new MoovAtomParser(fileInput, bytesRead, size, extendedSize);
     } else if (Arrays.equals(FREE_BYTES, type)) {
       return new FreeAtomParser(fileInput, bytesRead, size, extendedSize);
     }
     throw new UnsupportedOperationException("Illegal type: "
         + Arrays.toString(type) + " (" + new String(type) + ")");
   }
 
   protected int readInt() throws IOException {
     return readInt(fileInput);
   }
 
   private static int readInt(FileInputStream fileInput) throws IOException {
     byte[] bytes = readBytes(fileInput, INT_SIZE);
     return ByteBuffer.wrap(bytes).getInt();
   }
 
   protected long readLong() throws IOException {
     return readLong(fileInput);
   }
 
   private static long readLong(FileInputStream fileInput) throws IOException {
     byte[] bytes = readBytes(fileInput, LONG_SIZE);
     return ByteBuffer.wrap(bytes).getLong();
   }
 
   protected byte[] readBytes(int num) throws IOException {
     bytesRead += num;
     return readBytes(fileInput, num);
   }
 
   private static byte[] readBytes(FileInputStream fileInput, int num)
       throws IOException {
     byte[] bytes = new byte[num];
     fileInput.read(bytes);
     return bytes;
   }
 }
