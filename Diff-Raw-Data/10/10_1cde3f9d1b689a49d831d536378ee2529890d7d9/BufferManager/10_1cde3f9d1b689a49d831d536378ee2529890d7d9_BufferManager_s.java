 package com.iusenko.subrip;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 /**
  *
  * @author iusenko
  */
 public class BufferManager {
 
     public static final byte[] EMPTY_BUFFER = new byte[0];
     public static final int BUFFER_SIZE = 1024;
     private RandomAccessFile file;
     private String path;
     private long position = 0;
     private byte[] buffer = new byte[BUFFER_SIZE];
 
     public BufferManager(String path, long seekPosition) throws FileNotFoundException, IOException {
         this.path = path;
         this.file = new RandomAccessFile(path, "r");
 
         this.position = seekPosition;
         this.file.seek(this.position);
     }
 
     public BufferManager(String path) throws FileNotFoundException, IOException {
         this(path, 0);
     }
 
     public int updateWithNext() throws IOException {
         if (position >= file.length()) {
             return -1;
         }
         int read = file.read(buffer);
         if (read > 0) {
             position += read;
         }
         return read;
     }
 
     public int updateWithPrev() throws IOException {
         long backPosition = position - BUFFER_SIZE * 2;
        position = backPosition > 0 ? backPosition : 0l;
        System.err.println("seek to " + position);
         file.seek(position);
         int read = file.read(buffer);
         if (read > 0) {
             position += read;
         }
         return read;
     }
 
     public void close() {
         try {
             file.close();
         } catch (IOException ex) {
             // ignore it
         }
     }
 
     public final String getPath() {
         return path;
     }
 
     public final long getPosition() {
         return position;
     }
 
     public final byte[] getBuffer() {
         return buffer;
     }
 }
