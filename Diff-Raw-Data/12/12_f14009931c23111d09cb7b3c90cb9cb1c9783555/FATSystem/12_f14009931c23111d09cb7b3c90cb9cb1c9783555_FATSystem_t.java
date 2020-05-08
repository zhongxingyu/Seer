 package com.test;
 
 import sun.nio.ch.DirectBuffer;
 
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.nio.file.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: uta
  */
 
 class FATSystem implements Closeable {
     //file system header with magic number abd etc
     final static int  HEADER_SIZE = 32;
     final static int  FREE_CLUSTER_COUNT_OFFSET = 4*4;
     final static int  VERSION     = 1;
     final static long MAPFILE_SIZE_LIMIT = Integer.MAX_VALUE;
 
     final static int FAT_E_SIZE  = 4; //bytes for FAT32
     final static int MAGIC_WORD  = 0x75616673;
     //final static int MAGIC_WORD  = 0x73666175; //check ENDIAN sfau/uafs as BIG/LITTLE
 
     final static int CLUSTER_INDEX  = 0x3FFFFFFF;
     final static int CLUSTER_STATUS = 0xC0000000;
     final static int CLUSTER_FREE      = 0x00000000;
     final static int CLUSTER_ALLOCATED = 0x40000000;
     final static int CLUSTER_EOC       = 0xC0000000;
     // Diagnostic
     final static int CLUSTER_UNUSED    = 0x0BADBEAF;
     final static int CLUSTER_DEALLOC   = 0x0CCCCCCC;
 
     //header
     private int fsVersion;
     private int clusterSize;
     private int clusterCount;
 
     //denomalized values
     private int entryPerCluster;
     private int fatOffset;
     private int dataOffset;
     private int freeClusterCount; //can get [-1] on "dirty FAT"
 
     private RandomAccessFile randomAccessFile;
     private FileChannel fileChannel;
     private MappedByteBuffer fatZone;
     private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN; //default encoding
     private boolean opened = true; //allocate opened storage, check status in fabric
 
     // lockFAT - lockData rule!
     private final Object lockFAT = new Object();
     private final Object lockData = new Object();
     private final boolean normalMode;
 
     /**
      * Returns the FS mode.
      * @return [true] means throwing the [IOException] exception when "dirty FAT" is detected.
      *         [false] means means maintenance mode.
      */
     public boolean isNormalMode() {
         return normalMode;
     }
 
     /**
      * Opens FS from the exist file in host FS.
      * @param path the path to storage file in host FS
      * @return In-file FS over the file that opened in host FS.
      */
     public static FATSystem open(Path path) throws IOException {
         return open(path, true);
     }
 
     /**
      * Opens FS from the exist file in host FS.
      * @param path the path to storage file in host FS
      * @param normalMode the [false] value allows to open dirty FAT for maintenance
      * @return In-file FS over the file that opened in host FS.
      */
     private static FATSystem open(Path path, boolean normalMode) throws IOException {
         if (Files.notExists(path))
             throw new FileNotFoundException();
         FATSystem ret = new FATSystem(normalMode);
         boolean success = false;
         try {
             ret.randomAccessFile = new RandomAccessFile(path.toString(), "rw");
             ret.initFromFile();
             success = true;
         } finally {
             if (!success)
                 ret.close();
         }
         return ret;
     }
 
     private boolean isOpen() {
         return opened;
     }
 
     private void initFromFile() throws IOException {
         if (randomAccessFile.length() < HEADER_SIZE)
             throw new IOException("Wrong media size. File is too short.");
 
         fileChannel = randomAccessFile.getChannel();
         ByteBuffer bf = allocateBuffer(HEADER_SIZE);
         readFromChannel(bf);
 
         // init header
         int magic = bf.getInt(); //media type
         if (magic != MAGIC_WORD)
             throw new IOException("Wrong media type. That is not FFS file");
        int version = bf.getInt();  //FS version
         if (version != VERSION)
             throw new IOException("Wrong version: " + version
                       + "Version " +  VERSION + " is the only supported.");
         clusterSize = bf.getInt();
         clusterCount = bf.getInt();
         freeClusterCount = bf.getInt();
         checkValidStatus();
 
         if (randomAccessFile.length() < ((long)clusterCount * clusterSize + HEADER_SIZE)) {
            LogError("Wrong storage size. Storage was truncated in host FS.");
             setDirtyStatus();
         }
 
         initDenormalized();
     }
 
     /**
      * Creates new file-based file system.
      * @param path is the path in host FS for file storage that need be created
      * @param clusterSize  the size of single cluster. Mast be at least [FolderEntry.RECORD_SIZE] size
      * @param clusterCount the total number of clusters in created file storage.
      * @return new In-file FS over the file that created in host FS.
      * @throws IOException for bad parameters or file access problem in the host FS
      */
     public static FATSystem create(Path path, int clusterSize, int clusterCount) throws IOException {
         if (clusterSize < FolderEntry.RECORD_SIZE)
             throw new IOException("Bad value of cluster size:" + clusterSize);
 
         if (clusterCount <= 0 || clusterCount > CLUSTER_INDEX || clusterCount*FAT_E_SIZE > MAPFILE_SIZE_LIMIT)
             throw new IOException("Bad value of cluster count:" + clusterCount);
 
         long length = (long)clusterCount * clusterSize;
         if (length/clusterSize != clusterCount)
             throw new IOException("File system is too big. FAT overloaded.");
 
         long sizeFS = length + HEADER_SIZE;
         if (sizeFS < length)
             throw new IOException("File system is too big. No space for header." );
 
         FATSystem ret = new FATSystem(true);
         boolean success = false;
         try {
             ret.randomAccessFile = new RandomAccessFile(path.toString(), "rw");
             ret.randomAccessFile.setLength(sizeFS);
             ret.initStorage(clusterSize, clusterCount);
             success = true;
         } finally {
             if (!success)
                 ret.close();
         }
         return ret;
     }
 
 
     private void initStorage(int _clusterSize, int _clusterCount) throws IOException {
         fsVersion = VERSION;
         clusterSize = _clusterSize;
         clusterCount = _clusterCount;
         initDenormalized();
         freeClusterCount = clusterCount;
 
         fileChannel = randomAccessFile.getChannel();
 
         writeToChannel(allocateBuffer(HEADER_SIZE)
             // init header
             .putInt(MAGIC_WORD)
             .putInt(fsVersion)     //FS version
             .putInt(clusterSize)
             .putInt(clusterCount)
             //Set dirty flag in free cluster count. We drop it on right close.
             .putInt(-1));
 
 
         // map FAT section
         fatZone = fileChannel.map(FileChannel.MapMode.READ_WRITE, fatOffset, clusterCount*FAT_E_SIZE);
         fatZone.order(byteOrder);
         // init FAT32
         for (int i = 0; i < clusterCount; ++i) {
             fatZone.putInt(CLUSTER_UNUSED);
         }
 
         // init Data Region
         //createRoot();
 
         flush();
     }
 
     @Override
     public void close() throws IOException {
         synchronized (lockFAT) {
             synchronized (lockData) {
                 if (!opened)
                     throw new IOException("Storage was closed earlier.");
                 try {
                     if (randomAccessFile != null) {
                         if (fileChannel != null) {
                             if (fatZone != null) {
                                 // saves real dirty status
                                 fileChannel.position(FREE_CLUSTER_COUNT_OFFSET);
                                 // always thinking about SPARC
                                 writeToChannel(allocateBuffer(4)
                                         .putInt(freeClusterCount));
                                 try {
                                     // That is bad, but it is the only available solution
                                     // http://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java
                                     sun.misc.Cleaner cleaner = ((DirectBuffer)fatZone).cleaner();
                                     cleaner.clean();
                                 } catch (Throwable any) {
                                     LogError("Not Oracle implementation for memory-mapped file."
                                            + "We can get a problem. Trying direct GC call.");
                                     System.gc();
                                 }
                             }
                             fileChannel.close();
                         }
                         randomAccessFile.close();
                     }
                 } finally {
                     // RIP
                     opened = false;
                 }
             }
         }
     }
 
     public void flush() throws IOException {
         // One is not a guaranty for another
         fileChannel.force(true);
         fatZone.force();
     }
 
     int getVersion() {
        return fsVersion;
     }
 
     int getEntryPerCluster() {
         return entryPerCluster;
     }
 
     /**
      * Returns the capacity of FS
      * @return the size of storage. That is the [Data Section] size.
      */
     public long getSize() {
         return  clusterCount*clusterSize;
     }
 
     /**
      * Returns the free capacity of FS
      * @return the free size in storage. The [-1] means dirty FAT and the system needs in maintenance.
      */
     public long getFreeSize() {
         return freeClusterCount*clusterSize;
     }
 
     /**
      * Reads cluster content.
      * @param cluster the index of the cluster in FAT
      * @return cluster content
      * @throws IOException
      */
     public ByteBuffer readCluster(int cluster) throws IOException {
         if (cluster < 0 || cluster >= clusterCount)
             throw new IOException("Bad cluster index:" + cluster);
 
         ByteBuffer bf = ByteBuffer.allocateDirect(clusterSize);  //check with alloc!
         synchronized (lockData) {
             checkValidStatus();
             fileChannel
                 .position(dataOffset + cluster * clusterSize)
                 .read(bf);
         }
         return bf;
     }
 
     /**
      * Reads FAT entry by index.
      * @param cluster the index of current cluster in the chain if any
      * @return the entry value
      */
     public int readFatEntry(int cluster) throws IOException {
         synchronized (lockFAT) {
             checkValidStatus();
             return fatZone.getInt(cluster*FAT_E_SIZE);
         }
     }
 
     /**
      * Allocates a cluster chain.
      * @param findAfter the index of the tail of the chain.
      *        The -1 value means that the chain should not be joined.
      *        Any other value means that allocated cain will join to [findAfter] tail
      * @param count the number of cluster in the returned chain
      * @return the index of the first cluster in allocated chain.
      * @throws IOException if the chain could not be allocated
      */
     int allocateClusters(int findAfter, int count) throws IOException {
         // - bitmap of free clusters in memory?
         // - chunk with free block counting for sequential allocation?
         // - two stage allocation with "gray"
         //    +final static int CLUSTER_GALLOCATED = 0x80000000;
         //    +final static int CLUSTER_GEOC       = 0xC8000000;
         //   up bit?
         // - resize if need?
         if (count < 1)
             throw new IOException("Cannot allocate" + count + "clusters.");
         if ((findAfter < clusterCount) && (
                 ((freeClusterCount >= 0) && (count <= freeClusterCount))
              || ((freeClusterCount  < 0) && (count <= clusterCount)))) // without guaranty on dirty FAT
         {
             synchronized (lockFAT) {
                 checkValidStatus();
                 try {
                     int currentOffset = (findAfter == -1)
                         ? 0
                         : findAfter + 1;
                     int headOffset = -1;
                     int tailOffset = findAfter;
                     int endOfLoop = currentOffset; // loop marker
                     while (true) {
                         if (currentOffset >= clusterCount) {
                             currentOffset = 0;
                             if (currentOffset == endOfLoop)
                                 break;
                         }
 
                         int fatEntry = fatZone.getInt(currentOffset*FAT_E_SIZE);
                         if ((fatEntry & CLUSTER_STATUS) == CLUSTER_FREE) {
                             if (headOffset == -1)
                                 headOffset = currentOffset;
 
                             // mark as EOC
                             --freeClusterCount;
                             fatZone.putInt(currentOffset*FAT_E_SIZE, CLUSTER_EOC);
                             if (tailOffset != -1) {
                                 // mark as ALLOCATED with forward index
                                 fatZone.putInt(tailOffset*FAT_E_SIZE, CLUSTER_ALLOCATED | currentOffset);
                             }
                             tailOffset = currentOffset;
                             --count;
                             if (count == 0)
                                 return headOffset;
                         }
                         ++currentOffset;
                         if (currentOffset == endOfLoop)
                             break;
                     }
 
                     LogError("[freeClusterCount] has wrong value.");
                     setDirtyStatus();
 
                     // rollback allocation.
                     if (findAfter == -1)
                         freeClusters(headOffset, true, false);
                     else
                         freeClusters(findAfter, false, false);
 
                 } finally {
                     fatZone.force();
                 }
             }
         }
         throw new IOException("Disk full.");
     }
 
     /**
      * Frees chain that start from [headOffset] cluster.
      * @param headOffset the head of the chain
      * @param freeHead if [true] the chain is freed together with head cluster
      *                 else head cluster is marked as [EOC]
      * @throws IOException
      */
     void freeClusters(int headOffset, boolean freeHead) throws IOException {
         freeClusters(headOffset, freeHead, true);
     }
 
     /**
      * Frees chain that start from [headOffset] cluster.
      * @param headOffset the head of the chain
      * @param freeHead if [true] the chain is freed together with head cluster
      *                 else head cluster is marked as [EOC]
      * @param forceChanges fix the changes to disk. Have to be [true] for external calls
      * @throws IOException
      */
      private void freeClusters(int headOffset, boolean freeHead, boolean forceChanges) throws IOException {
         synchronized (lockFAT) {
             if (forceChanges)
                 checkValidStatus();
             try {
                 if (!freeHead) {
                     int value = fatZone.getInt(headOffset*FAT_E_SIZE);
                     // CLUSTER_ALLOCATED only
                     if ((value & CLUSTER_STATUS) == CLUSTER_ALLOCATED) {
                         // mark as EOC
                         fatZone.putInt(headOffset*FAT_E_SIZE, CLUSTER_EOC);
                         headOffset = value & CLUSTER_INDEX;
                     } else {
                         LogError("Cluster double free in tail.  Cluster#:" + headOffset
                                 + " Value:" + value);
                         setDirtyStatus();
                     }
                 }
                 while (true) {
                     int value = fatZone.getInt(headOffset*FAT_E_SIZE);
                     // CLUSTER_ALLOCATED or CLUSTER_EOC
                     if ((value & CLUSTER_ALLOCATED) == CLUSTER_ALLOCATED) {
                         // mark as DEALLOC
                         fatZone.putInt(headOffset*FAT_E_SIZE, CLUSTER_DEALLOC);
                         ++freeClusterCount;
                         if ((value & CLUSTER_EOC) == CLUSTER_EOC)
                             break;
                         headOffset = value & CLUSTER_INDEX;
                     } else {
                         LogError("Cluster double free. Cluster#:" + headOffset
                                 + " Value:" + value);
                         setDirtyStatus();
                     }
                 }
             } finally {
                 if (forceChanges)
                     fatZone.force();
             }
         }
     }
 
     /**
      * Log the problem to error stream.
      * @param errorMessage  the problem description.
      */
     private void LogError(String errorMessage) {
         if (isNormalMode())
             System.err.println(errorMessage);
     }
 
     /**
      * Returns FS time counter.
      * @return the Java current time in milliseconds.
      */
     public static long getCurrentTime() {
         return System.currentTimeMillis();
     }
 
 
     private FATSystem(boolean _normalMode) {
         normalMode = _normalMode;
     }
 
     private void checkValidStatus() throws IOException {
         if (!isOpen())
             throw new IOException("The storage was closed.");
 
         // check for dirty FAT
         if (freeClusterCount < 0 && isNormalMode())
             throw new IOException("The storage needs maintenance.");
     }
 
     private void setDirtyStatus() throws IOException {
         if (freeClusterCount >= 0) {
             freeClusterCount = -1;
             checkValidStatus();
         }
     }
 
     private void initDenormalized() {
         entryPerCluster = clusterSize/FolderEntry.RECORD_SIZE;
         fatOffset = HEADER_SIZE; //version dependant
         dataOffset = fatOffset + clusterCount*FAT_E_SIZE;
     }
 
     private ByteBuffer allocateBuffer(int capacity) {
         return ByteBuffer.allocateDirect(capacity).order(byteOrder);
     }
 
     private void writeToChannel(ByteBuffer bf) throws IOException {
         bf.flip();
         while(bf.hasRemaining()) {
             fileChannel.write(bf);
         }
     }
 
     private void readFromChannel(ByteBuffer bf) throws IOException {
         bf.position(0);
         int size = bf.capacity();
         while(size > 0) {
             size -= fileChannel.read(bf);
         }
         bf.flip();
     }
 
 }
