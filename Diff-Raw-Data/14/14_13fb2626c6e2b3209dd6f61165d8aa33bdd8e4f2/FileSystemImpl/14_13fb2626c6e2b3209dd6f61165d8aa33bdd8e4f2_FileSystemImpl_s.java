 package org.fs.impl;
 
 import org.fs.ISingleFileFS;
 import org.fs.common.CounterMap;
 import org.fs.common.ThreadSafe;
 import org.fs.common.concurrent.EqualObjectsMutex;
 import org.fs.impl.streams.chunk.ChunkInputStream;
 import org.fs.impl.streams.chunk.ChunkOutputStream;
 import org.fs.impl.streams.screening.ScreeningInputStream;
 import org.fs.impl.streams.screening.ScreeningOutputStream;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Yury Litvinov
  */
 @ThreadSafe
 public class FileSystemImpl implements ISingleFileFS {
 
     public static final int CHUNK_SIZE = 1 << 12; //4k size of the each chunk
 
     private final RandomAccessFileWrapper randomAccessFile;
     private final MetadataHandler chunksMetadataHandler;
     private final FileNamesKeeper fileNamesKeeper;
 
     private final CounterMap<Integer> filesReads = new CounterMap<Integer>();
     private final CounterMap<Integer> filesWrites = new CounterMap<Integer>();
     private final Object metadataLock = new Object();
     private final EqualObjectsMutex<String> mutexes = new EqualObjectsMutex<String>();
 
     public FileSystemImpl(File file) throws IOException {
         this.randomAccessFile = new RandomAccessFileWrapper(file);
         if (randomAccessFile.isNewFile()) {
             chunksMetadataHandler = new MetadataHandler();
             fileNamesKeeper = new FileNamesKeeper();
         } else {
             try {
                 // reading chunks metadata
                 ObjectInputStream metadataStream = createMetadataInputStream();
                 chunksMetadataHandler = (MetadataHandler) metadataStream.readObject();
 
                 // reading file names
                 InputStream filesNamesInputStream = createInputStream(FileNamesKeeper.RESERVED_ID_FOR_FILE_NAMES);
                 fileNamesKeeper = (FileNamesKeeper) new ObjectInputStream(filesNamesInputStream).readObject();
             } catch (ClassNotFoundException e) {
                 throw new IOException(e);
             }
         }
     }
 
     @Override
     public InputStream readFile(String fileName) throws IOException {
         checkArguments(fileName);
         synchronized (mutexes.getMutex(fileName)) {
             if (!fileNamesKeeper.hasFile(fileName)) {
                 throw new IOException("File with name '" + fileName + "' is not found");
             }
             int fileId = fileNamesKeeper.getFileId(fileName);
             if (filesWrites.getCount(fileId) > 0) {
                 throw new IOException("Someone is currently writing to this file");
             }
             filesReads.increase(fileId);
             return createInputStream(fileId);
         }
     }
 
     @Override
     public OutputStream writeFile(String fileName) throws IOException {
         checkArguments(fileName);
         synchronized (mutexes.getMutex(fileName)) {
             if (fileNamesKeeper.hasFile(fileName)) {
                 throw new IOException("File '" + fileName + "' already exists");
             }
            int fileId = fileNamesKeeper.add(fileName);
             filesWrites.increase(fileId);
             return createOutputStream(fileId);
         }
     }
 
     @Override
     public void deleteFile(String fileName) throws IOException {
         checkArguments(fileName);
         synchronized (mutexes.getMutex(fileName)) {
             if (!fileNamesKeeper.hasFile(fileName)) {
                 throw new IOException("File with name '" + fileName + "' does not exist in file system");
             }
             int fileId = fileNamesKeeper.getFileId(fileName);
             if (filesReads.getCount(fileId) > 0) {
                 throw new IOException("File is opened for reading");
             }
             if (filesWrites.getCount(fileId) > 0) {
                 throw new IOException("File is opened for writing");
             }
 
             // make sure that we don't flush at the moment
             synchronized (metadataLock) {
                 fileNamesKeeper.remove(fileName);
                 chunksMetadataHandler.releaseChunksForFile(fileId);
             }
         }
     }
 
     @Override
     public void flushMetadata() throws IOException {
         // we don't want any changes being made to metadata during flushing
         synchronized (metadataLock) {
             // removing chunks for FileNames structure
             chunksMetadataHandler.releaseChunksForFile(FileNamesKeeper.RESERVED_ID_FOR_FILE_NAMES);
 
             // write FileNames structure
             ObjectOutputStream fileObjectInputStream = new ObjectOutputStream(createOutputStream(FileNamesKeeper.RESERVED_ID_FOR_FILE_NAMES));
             fileObjectInputStream.writeObject(fileNamesKeeper);
             fileObjectInputStream.close();
 
             // write metadata
             ObjectOutputStream metadataOutputStream = createMetadataOutputStream();
             metadataOutputStream.writeObject(chunksMetadataHandler);
             metadataOutputStream.close();
         }
     }
 
     private ScreeningOutputStream createOutputStream(final int fileId) {
         ChunkOutputStream.ChunksAllocator chunksAllocator = new ChunkOutputStream.ChunksAllocator() {
             @Override
             public int allocateNewChunk() {
                 synchronized (metadataLock) {
                     int chunkNumber = chunksMetadataHandler.allocateNewChunkForFile(fileId);
                     randomAccessFile.resizeFileToFitChunk(chunkNumber);
                     return chunkNumber;
                 }
             }
         };
         ChunkOutputStream chunkOutputStream = new ChunkOutputStream(randomAccessFile, chunksAllocator) {
             @Override
             public synchronized void close() throws IOException {
                 filesWrites.decrease(fileId);
                 super.close();
             }
         };
         return new ScreeningOutputStream(chunkOutputStream, ChunkOutputStream.EOF);
     }
 
     private ScreeningInputStream createInputStream(final int fileId) {
         List<Integer> chunkNumbers = chunksMetadataHandler.getChunksForFile(fileId);
         ChunkInputStream chunkInputStream = new ChunkInputStream(randomAccessFile, chunkNumbers) {
             boolean closed = false;
 
             @Override
             public synchronized int read() throws IOException {
                 int value = super.read();
                 if (value == -1) {
                     close();
                 }
                 return value;
             }
 
             @Override
             public synchronized void close() throws IOException {
                    if (!closed) {
                        filesReads.decrease(fileId);
                    }
                    closed = true;
                 super.close();
             }
         };
         return new ScreeningInputStream(chunkInputStream, ChunkOutputStream.EOF);
     }
 
     private ObjectInputStream createMetadataInputStream() throws IOException {
         ChunkInputStream chunkInputStream = new ChunkInputStream(randomAccessFile, Arrays.asList(MetadataHandler.METADATA_CHUNK_NUMBER));
         return new ObjectInputStream(new ScreeningInputStream(chunkInputStream, ChunkOutputStream.EOF));
     }
 
     private ObjectOutputStream createMetadataOutputStream() throws IOException {
         ChunkOutputStream chunkOutputStream = new ChunkOutputStream(randomAccessFile, new ChunkOutputStream.ChunksAllocator() {
             boolean invoked;
 
             @Override
             public int allocateNewChunk() {
                 if (invoked) {
                     throw new IllegalStateException("Metadata does not fit into single chunk. This is a limitation of the current prototype");
                 }
                 invoked = true;
                 return MetadataHandler.METADATA_CHUNK_NUMBER;
             }
         });
         return new ObjectOutputStream(new ScreeningOutputStream(chunkOutputStream, ChunkOutputStream.EOF));
     }
 
     private void checkArguments(String fileName) {
         if (fileName == null) {
             throw new IllegalArgumentException("File name could no be null");
         }
     }
 
 }
