 package org.fs.impl;
 
 import org.fs.impl.streams.IRandomAccessFile;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 /**
  * @author Yury Litvinov
  */
 class RandomAccessFileWrapper implements IRandomAccessFile {
 
     private final RandomAccessFile randomAccessFile;
 
     public RandomAccessFileWrapper(File file) throws FileNotFoundException {
         randomAccessFile = new RandomAccessFile(file, "rw");
     }
 
     @Override
    public void write(int offset, byte[] data) throws IOException {
         randomAccessFile.seek(offset);
         randomAccessFile.write(data);
     }
 
     @Override
    public void read(int offset, byte[] data) throws IOException {
         randomAccessFile.seek(offset);
         randomAccessFile.read(data);
     }
 
    public boolean isNewFile() throws IOException {
         return randomAccessFile.length() == 0;
     }
 
    public void resizeFileToFitChunk(Integer chunkNumber) {
         long requiredLength = (chunkNumber + 1) * FileSystemImpl.CHUNK_SIZE;
         try {
             long currentLength = randomAccessFile.length();
             if (currentLength < requiredLength) {
                 randomAccessFile.setLength(requiredLength);
             }
         } catch (IOException e) {
             throw new IllegalStateException("Failed during resizing file. Required length=" + requiredLength, e);
         }
     }
 
 }
