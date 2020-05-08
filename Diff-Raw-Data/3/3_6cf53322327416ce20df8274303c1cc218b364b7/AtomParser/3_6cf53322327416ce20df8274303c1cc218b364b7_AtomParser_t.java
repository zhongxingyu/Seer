 package com.hotcats.mp4artextractor.parse.atom;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.hotcats.mp4artextractor.data.atom.Atom;
 import com.hotcats.mp4artextractor.data.atom.AtomType;
 
 public abstract class AtomParser {
 
   public static final int INT_SIZE = 4;
   public static final int LONG_SIZE = 8;
   public static final int TYPE_SIZE = 4;
 
   public static final Map<AtomType, AtomParserFactory> parsers;
   static {
     Map<AtomType, AtomParserFactory> parsersTemp = new HashMap<>();
 
     parsersTemp.put(AtomType.FTYP, new FtypAtomParser.Factory());
     for (AtomType type : AtomType.RECURSIVE_ATOMS) {
       parsersTemp.put(type, new RecursiveAtomParser.Factory());
     }
 
     parsers = Collections.unmodifiableMap(parsersTemp);
   }
 
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
 
     AtomType type = new AtomType(readBytes(fileInput, TYPE_SIZE));
     bytesRead += TYPE_SIZE;
 
     long extendedSize = 0;
     if (size == 1) {
       extendedSize = readLong(fileInput);
       bytesRead += LONG_SIZE;
     }
 
     if (type.equals(AtomType.META)) {
       // This is not in the spec, but the meta atom seems to have an extra byte
       // here.
       readInt(fileInput);
       bytesRead += INT_SIZE;
     }
 
     if (parsers.containsKey(type)) {
       return parsers.get(type).getInstance(type, fileInput, bytesRead, size,
           extendedSize);
     } else {
       System.err.println("Skipping unknown atom type: " + type);
       return new SkipAtomParser(fileInput, bytesRead, size, extendedSize, type);
     }
   }
 
   protected FileInputStream getFileInput() {
     return fileInput;
   }
 
   protected int readInt() throws IOException {
     bytesRead += INT_SIZE;
     return readInt(fileInput);
   }
 
   private static int readInt(FileInputStream fileInput) throws IOException {
     byte[] bytes = readBytes(fileInput, INT_SIZE);
     return ByteBuffer.wrap(bytes).getInt();
   }
 
   protected long readLong() throws IOException {
     bytesRead += LONG_SIZE;
     return readLong(fileInput);
   }
 
   private static long readLong(FileInputStream fileInput) throws IOException {
     byte[] bytes = readBytes(fileInput, LONG_SIZE);
     return ByteBuffer.wrap(bytes).getLong();
   }
 
   protected int readNumber(int num) throws IOException {
     bytesRead += num;
     return readNumber(fileInput, num);
   }
 
   protected static int readNumber(FileInputStream fileInput, int num)
       throws IOException {
    byte[] bytes = new byte[INT_SIZE];
    fileInput.read(bytes, INT_SIZE - num, num);
     return ByteBuffer.wrap(bytes).getInt();
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
