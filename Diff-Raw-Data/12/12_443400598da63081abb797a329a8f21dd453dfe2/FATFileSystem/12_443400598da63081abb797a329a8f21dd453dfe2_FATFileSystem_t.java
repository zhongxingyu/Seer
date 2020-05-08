 package com.test;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.file.Path;
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: uta
  *
  * The File System over the FAT System.
  *
  * All [transact-safe] methods have the [ts_] prefix.
  * The [transact-safe] means that methods can terminated successfully,
  *   or restore the object state to initial condition and throw exception,
  *   or mark system as "dirty" and throw exception (critical error in host FS).
  *
  * All public functions have to be [transact-safe] by default.
  *
  * Each public call with r/w operation need to be executed in FS transaction like
 * <pre><code>
  *              boolean success = false;
  *              try {
  *                  begin(isWriteTransaction);
  *                  ...action...
  *                  //commit
  *                  success = true;
  *             } finally {
  *                 if (!success) {
  *                     //rollback action
  *                 }
  *                 end();
  *             }
 * </code></pre>
  */
 public class FATFileSystem implements Closeable {
     private FATSystem fat;
     private FATFolder root;
 
     private HashMap<Integer, FATFolder> folderCache = new HashMap<>();
     private HashMap<Integer, FATFile>   fileCache = new HashMap<>();
 
     // treeLock->fileLock lock sequence
     final Object treeLock = new Object();
     final Object fileLock = new Object();
 
     // smart termination procedure as
     //  Transaction counting + shutdown signal + wait for execution finish
     private boolean shutdown = false;
     private long transactionCounter = 0L;
 
     final Object transactionCounterLock = new Object();
     final Object shutdownSignal = new Object();
 
 
     private FATFileSystem() {}
 
     /**
      * Creates new file-based file system.
      * @param path is the path in host FS for file storage that need be created
      * @param clusterSize  the size of single cluster. Mast be at least [FATFile.RECORD_SIZE] size
      * @param clusterCount the total number of clusters in created file storage.
      * @return new In-file FS over the file that created in host FS.
      * @throws IOException for bad parameters or file access problem in the host FS
      */
     public static FATFileSystem create(Path path, int clusterSize, int clusterCount) throws IOException {
         return create(path, clusterSize, clusterCount, FATSystem.ALLOCATOR_CLASSIC_HEAP);
     }
 
     /**
      * Creates new file-based file system.
      * @param path is the path in host FS for file storage that need be created
      * @param clusterSize  the size of single cluster. Mast be at least [FATFile.RECORD_SIZE] size
      * @param clusterCount the total number of clusters in created file storage.
      * @param allocatorType the cluster allocation strategy
      * @return new In-file FS over the file that created in host FS.
      * @throws IOException for bad parameters or file access problem in the host FS
      */
     public static FATFileSystem create(Path path, int clusterSize,int clusterCount,
                                    int allocatorType) throws IOException {
         FATFileSystem ret = new FATFileSystem();
         boolean success = false;
         try {
             ret.fat = FATSystem.create(path, clusterSize, clusterCount, allocatorType);
             ret.root = FATFolder.ts_createRoot(ret, 0);
             success = true;
         } finally {
             if (!success)
                 ret.close();
         }
         return ret;
     }
 
 
     /**
      * Opens FS from the exist file in host FS.
      * @param path the path to storage file in host FS
      * @return In-file FS over the file that opened in host FS.
      */
     public static FATFileSystem open(Path path) throws IOException {
         return open(path, true);
     }
 
     /**
      * Opens FS from the exist file in host FS.
      * @param path the path to storage file in host FS
      * @param normalMode the [false] value allows to open dirty FAT for maintenance
      * @return In-file FS over the file that opened in host FS.
      */
     private static FATFileSystem open(Path path, boolean normalMode) throws IOException {
         FATFileSystem ret = new FATFileSystem();
         boolean success = false;
         try {
             ret.fat = FATSystem.open(path, normalMode);
             ret.root = FATFolder.ts_openRoot(ret);
             success = true;
         } finally {
             if (!success)
                 ret.close();
         }
         return ret;
     }
 
     /**
      * Closes this stream and releases any system resources associated
      * with it. If the stream is already closed then invoking this
      * method has no effect.
      * 
      * <p> As noted in {@link AutoCloseable#close()}, cases where the
      * close may fail require careful attention. It is strongly advised
      * to relinquish the underlying resources and to internally
      * <em>mark</em> the {@code Closeable} as closed, prior to throwing
      * the {@code IOException}.
      *
      * @throws java.io.IOException if an I/O error occurs
      */
     @Override
     public void close() throws IOException {
         synchronized (treeLock) {
             synchronized (fileLock) {
                 if (!shutdown())
                     fat.setDirtyStatus("Alarm shutdown precess.", true);
                 if (fat != null)
                     fat.close();
             }
         }
     }
 
     /**
      * Get file system version.
      *
      * @return version of file system in action.
      */
     public int getVersion() {
         // single version for all system
         return fat.getVersion();
     }
 
     /**
      * Returns the capacity of File System.
      *
      * No staff info above pure FAT.
      *
      * @return the size of storage. That is the [Data Section] size.
      */
     public long getSize() {
         return  fat.getSize();
     }
 
     /**
      * Returns the free capacity of File System
      *
      * No forward reservation for staff objects.
      *
      * @return the free size in storage. The [&lt;0] means dirty FAT and the
      *         system needs in maintenance.
      */
     public long getFreeSize() {
         return fat.getFreeSize();
     }
 
     /**
      * Returns FS time counter.
      * @return the Java current time in milliseconds.
      */
     public static long getCurrentTime() {
         return System.currentTimeMillis();
     }
 
     ByteBuffer ts_allocateBuffer(int recordSize) {
         // potentially the Folder record could be in reverse byte order,
         // but it is not a good idea
         return fat.allocateBuffer(recordSize);
     }
 
     int ts_allocateFileSpace(long size) throws IOException {
         if (size < 0)
             throw new IOException("Wrong file size.");
 
         // use startCluster as fileId
         return fat.allocateClusters(-1, fat.getSizeInClusters(size));
     }
 
     /**
      * Flush file content.
      *
      * @param file the file to flush
      * @param updateMetadata
      * @throws IOException
      */
     void ts_forceFileContent(FATFile file, boolean updateMetadata) throws IOException {
         fat.forceChannel(updateMetadata);
     }
 
 
     void setFileLength(FATFile file, long newLength, long oldLength) throws IOException {
         if (file.ts_getFileId() == FATFile.INVALID_FILE_ID)
             throw new IOException("Bad file state.");
         fat.adjustClusterChain(file.ts_getFileId(), newLength, oldLength);
     }
 
     int writeFileContext(FATFile file, long position,
                                 ByteBuffer src) throws IOException {
         int wasWritten = 0;
         Integer startCluster = file.ts_getFileId();
         Long pos = position;
         while (src.hasRemaining()) {
             wasWritten += fat.writeChannel(startCluster, pos, src);
         }
         return wasWritten;
     }
 
     int readFileContext(FATFile file, long position,
                         ByteBuffer dst) throws IOException {
         int wasRead = 0;
         Integer startCluster = file.ts_getFileId();
         Long pos = position;
         while (dst.hasRemaining()) {
             int read = fat.readChannel(startCluster, pos, dst);
             if (read < 0) {
                 if (wasRead == 0)
                     return -1;
                 break;
             }
             wasRead += read;
         }
         return wasRead;
     }
 
     /**
      * Creates new file.
      *
      * Returns file has no connection with folder.
      *
      * @param fileName the name of created file
      * @param type FATFile.TYPE_XXXX const
      * @param size the space for allocation
      * @param access desired access to file
      * @return created file
      * @throws IOException
      */
     FATFile ts_createFile(String fileName, int type, long size, int access) throws IOException {
         synchronized (fileLock) {
             // create new
             FATFile ret = new FATFile(this, fileName, type, size, access);
             fileCache.put(ret.ts_getFileId(), ret);
             return ret;
         }
     }
 
     /**
      * Rollback procedure for [{@see ts_createFile}] return value
      *
      * @param file the file for drop.
      */
     void ts_dropDirtyFile(FATFile file) throws IOException {
         synchronized (fileLock) {
             if (file.ts_getFileId() == FATFile.INVALID_FILE_ID)
                 throw new IOException("Bad file id.");
             
             fileCache.remove(file.ts_getFileId());
             try {
                 fat.freeClusters(file.ts_getFileId(), true);
             } finally {
                 //no rollback from fat level - set dirty inside
                 file.ts_setFileId(FATFile.INVALID_FILE_ID);
             }
         }
     }
 
 
     /**
      * Opens file from folder record.
      * Returns file that has no connection with folder.
      *
      * @param bf the storage of attributes
      * @return opened file
      * @throws IOException
      */
     FATFile ts_openFile(ByteBuffer bf, int parentId) throws IOException {
         synchronized (fileLock) {
             // open existent if can
             int fileId = bf.getInt();
             int type = bf.getInt();
             FATFile ret =  fileCache.get(fileId);
             if (ret == null) {
                 // open existent
                 int fatEntry = fat.getFatEntry(fileId);
                 if ((fatEntry & FATClusterAllocator.CLUSTER_ALLOCATED) == 0)
                     throw new IOException("Invalid file id.");
 
                 ret = new FATFile(this, type, fileId, parentId);
                 fileCache.put(ret.ts_getFileId(), ret);
             } // else check the type?
             ret.ts_initFromBuffer(bf);
             return ret;
         }
     }
 
 
 
     /**
      * Restores folder from [fileId] or gets it from cache.
      *
      * @param fileId the [fileId] of folder storage file.
      * @return the folder object
      */
     FATFolder ts_getFolder(int fileId) {
         synchronized (treeLock) {
             FATFolder ret = folderCache.get(fileId);
             if (ret == null) {
                 ret = new FATFolder(ts_getFile(fileId));
                 folderCache.put(fileId, ret);
             }
             return ret;
         }
     }
 
     /**
      * Gets a file object from cache, or return null.
      *
      * @param fileId
      * @return
      */
     FATFile ts_getFile(int fileId) {
         synchronized (fileLock) {
             return  fileCache.get(fileId);
         }
     }
 
     public FATFolder getRoot() {
         return root;
     }
 
     public Object getTreeLock() {
         return treeLock;
     }
 
     /**
      * Signal to start transaction.
      */
     void begin(boolean writeOperation) throws IOException {
         synchronized (transactionCounterLock) {
             // we need unwind nested transactions.
             if (transactionCounter==0 && shutdown)
                 throw new IOException("System shutdown.");
             transactionCounter += 1;
             if (writeOperation)
                 fat.checkCanWrite();
             else
                 fat.checkCanRead();
         }
     }
 
     /**
      * Signal to end transaction.
      */
     void end() {
         synchronized (transactionCounterLock) {
             transactionCounter -= 1;
             if (transactionCounter == 0 && shutdown) {
                 synchronized (shutdownSignal) {
                     shutdownSignal.notify();
                 }
             }
         }
     }
 
     /**
      * Sends shutdown signal to the file system.
      *
      * Can be called multiple times. Once all nested
      * transactions are terminated, the file system goes to
      * the frozen state and ready to be closed.
      *
      * If there are no active transactions in the thread stack,
      * the [{@link #waitForShutdown(long)}] method could be called directly.
      *
      * @return [true] if the file system is ready be closed
      */
     public boolean shutdown() {
         synchronized (transactionCounterLock) {
             shutdown = true;
             return (transactionCounter == 0);
         }
     }
 
     /**
      * Waits for the file system shutdown.
      *
      * @param  timeout   the maximum time to wait in milliseconds.
      * @throws InterruptedException
      * @see    java.lang.Object#wait(long) ()
      */
     public void waitForShutdown(long timeout) throws InterruptedException {
         synchronized (transactionCounterLock) {
             synchronized (shutdownSignal) {
                 shutdown = true;
                 // just for protection from spurious wakeup
                 // don't worry about longer time in wait for [spurious wakeup]
                 while (transactionCounter != 0)
                     shutdownSignal.wait(timeout);
             }
         }
     }
 
     /**
      * Set system to dirty state.
      *
      * @param message
      */
     void ts_setDirtyState(String message, boolean throwExeption) throws IOException {
         fat.setDirtyStatus(message, throwExeption);
     }
 
     void updateRootRecord(ByteBuffer rootInfo) throws IOException {
         fat.writeRootInfo(rootInfo);
     }
 
     public ByteBuffer getRootInfo() throws IOException {
         return fat.getRootInfo();
     }
 }
