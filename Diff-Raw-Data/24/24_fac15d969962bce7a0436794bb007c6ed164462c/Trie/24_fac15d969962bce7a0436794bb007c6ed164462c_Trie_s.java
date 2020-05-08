 // Copyright 2011 Waterken Inc. under the terms of the MIT X license found at
 // http://www.opensource.org/licenses/mit-license.html
 package org.k2v.trie;
 
 import java.io.BufferedOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.WritableByteChannel;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.concurrent.Semaphore;
 import java.util.zip.CRC32;
 import java.util.zip.CheckedOutputStream;
 
 import org.k2v.Value;
 import org.k2v.K2V;
 
 /**
  * A monotonically increasing trie file format.
  */
 public final class Trie implements org.k2v.K2V {
   
   /** default file extension */
   static public final String Ext = ".k2v";
 
   /**
    * A record reference is a {@code long} where the high two bytes is a type
    * code and the rest is the file position of the end of the referenced record.
    *
    * Almost all the type codes are negative numbers, so buggy code that forgets
    * to strip the type code when dereferencing a reference will likely cause a
    * runtime exception instead of returning the wrong data.
    */
   static short type(final ByteBuffer var) { return var.getShort(0); }
   static final int TypeBits = Short.SIZE;
   static final int DataBits = Long.SIZE - Short.SIZE;
   static final int TypeBytes = TypeBits / Byte.SIZE;
   static final int DataBytes = DataBits / Byte.SIZE;
   static short type(final long ref) { return (short)(ref >> DataBits); }
   static final long DataMask = (1L << DataBits) - 1;
   static long data(final long ref) { return ref & DataMask; }
   static final long TypeMask = ~DataMask;
   static long ref(final short type, final long data) {
     assert 0 == (data & TypeMask);
     return (((long)type) << DataBits) | data;
   }
   static final long MaxAddress = DataMask;
 
   /** Creates a mutable reference to a long field. */
   static ByteBuffer var(final ByteBuffer record, final int offset) {
     record.position(record.limit() - offset);
     final ByteBuffer field = record.slice();
     field.limit(8);
     return field;
   }
   static long value(final ByteBuffer record, final int offset) {
     return record.getLong(record.limit() - offset);
   }
   
   /**
    * byte head[arity] | byte bitmap[{@link #SizeOfBitmap}]
    * long branchReference[arity]
    */
   static final byte MajorTypeOfMap = -1;
   static final int MapMaxArity = 1 << Byte.SIZE;
   static final short TypeOfNull = MajorTypeOfMap << Byte.SIZE;
   static final long Null = ref(TypeOfNull, 0);
   static boolean isAMap(final short type) {
     return (type >> Byte.SIZE) == MajorTypeOfMap && type != TypeOfNull;
   }
   static int arityOfMap(final short type) { return (type & 0xFF) + 1; }
   static final int SizeOfBitmap = MapMaxArity / Byte.SIZE;
   static int sizeOfMap(final int arity) {
     return Math.min(arity, SizeOfBitmap) + 8 * arity;
   }
   static final int SizeOfMapMax = sizeOfMap(MapMaxArity);
   static ByteBuffer allocateMap() {
     // leave room in the allocated buffer to grow the map to capacity
     final ByteBuffer map = ByteBuffer.allocate(SizeOfMapMax);
     map.position(SizeOfMapMax - sizeOfMap(0));
     return map.slice();
   }
   static final short typeOfMap(final int arity) {
     assert 2 <= arity && MapMaxArity >= arity;
     return (short)((MajorTypeOfMap << Byte.SIZE) | (arity - 1));
   }
   static ByteBuffer listMap(final ByteBuffer map, final int arity) {
     if (SizeOfBitmap >= arity) {
       final ByteBuffer heads = map.duplicate();
       heads.rewind().limit(arity);
       map.position(arity);
       return heads;
     }
     final ByteBuffer heads = ByteBuffer.allocate(arity);
     for (int i = 0; i != SizeOfBitmap; i += 1) {
       final byte flags = map.get(i);
       for (int j = 0, mask = 0x80; 0 != mask; j += 1, mask >>= 1) {
         if (0 != (flags & mask)) {
           heads.put((byte)(i * 8 + j));
         }
       }
     }
     heads.rewind();
     map.position(SizeOfBitmap);
     return heads;
   }
   static int searchMap(final ByteBuffer map, final int arity, final byte head) {
     final int unsigned = head & 0xFF;
     if (SizeOfBitmap >= arity) {
       for (int i = 0; i != arity; ++i) {
         if ((map.get(i) & 0xFF) >= unsigned) { return i; }
       }
       return arity;
     }
     final int block = unsigned / Byte.SIZE;
     final int flag = 0x80 >> (unsigned % Byte.SIZE);
     int index = 0;
     for (int i = 0; i != block; i += 1) {
       final int flags = map.get(i);
       for (int mask = 0x80; 0 != mask; mask >>= 1) {
         if (0 != (flags & mask)) {
           index += 1;
         }
       }
     }
     {
       final int flags = map.get(block);
       for (int mask = 0x80; flag != mask; mask >>= 1) {
         if (0 != (flags & mask)) {
           index += 1;
         }
       }
     }
     return index;
   }
   static boolean inMap(final ByteBuffer map, final int arity,
                        final int index, final byte head) {
     if (SizeOfBitmap >= arity) {return index < arity && map.get(index) == head;}
     final int uhead = head & 0xFF;
     return 0 != (map.get(uhead / Byte.SIZE) & (0x80 >> (uhead % Byte.SIZE)));
   }
   static ByteBuffer growMap(final ByteBuffer map, final int arity,
                             final int index, final byte head) {
     if (SizeOfBitmap > arity) {
       final byte[] array = map.array();
       final int off = map.arrayOffset();
       System.arraycopy(array, off, array, off - 1, index);
       array[off - 1 + index] = head;
       System.arraycopy(array, off - 1, array, off - 1 - 8, arity + 1 + index*8);
       return ByteBuffer.wrap(array, off - 1 - 8, map.limit() + 1 + 8).slice().
         putLong(arity + 1 + index * 8, Null);
     }
     if (SizeOfBitmap == arity) {
       // reformat list as a bitmap
       final byte[] bitmap = new byte[SizeOfBitmap];
       for (int i = 0; i != arity; ++i) {
         final int unsigned = map.get(i) & 0xFF;
         bitmap[unsigned / Byte.SIZE] |= 0x80 >> (unsigned % Byte.SIZE);
       }
       map.position(0);
       map.put(bitmap);
     }
     final int unsigned = head & 0xFF;
     final byte[] array = map.array();
     final int off = map.arrayOffset();
     array[off + unsigned / Byte.SIZE] |= 0x80 >> (unsigned % Byte.SIZE);
     System.arraycopy(array, off, array, off - 8, SizeOfBitmap + index * 8);
     return ByteBuffer.wrap(array, off - 8, map.limit() + 8).slice().
       putLong(SizeOfBitmap + index * 8, Null);
   }
   
   /**
    * byte run[length]
    * long branchReference
    */
   static final byte MajorTypeOfRun = MajorTypeOfMap - 1;
   static final int RunMaxLength = 1 << Byte.SIZE;
   static boolean isARun(final short type) {return (type>>8) == MajorTypeOfRun;}
   static int lengthOfRun(final short type) { return (type & 0xFF) + 1; }
   static final int RunBranch    = 8;
   static int sizeOfRun(final int length) { return length + RunBranch; }
   static final int SizeOfRunMax = sizeOfRun(RunMaxLength);
   static final short typeOfRun(final int length) {
     assert 1 <= length && RunMaxLength >= length;
     return (short)((MajorTypeOfRun << 8) | (length - 1));
   }
   static ByteBuffer allocateRun(final ByteBuffer segment) {
     return ByteBuffer.allocate(sizeOfRun(segment.remaining())).
       put(segment).putLong(Null);
   }
   
   /**
    * long childReference
    * long branchReference
    */
   static final short TypeOfLeaf = (MajorTypeOfRun << 8) - 1;
   static final int LeafBranch = 8;
   static final int LeafChild  = LeafBranch + 8;
   static final int SizeOfLeaf = LeafChild;
   static ByteBuffer allocateLeaf(final long branch, final long child) {
     return ByteBuffer.allocate(SizeOfLeaf).putLong(child).putLong(branch);
   }
   
   /**
    * long count
    * long bytes
    * long top
    * long version
    * byte flags
    */
   static final short TypeOfFolder       = TypeOfLeaf - 1;
   static final byte FolderDefaultFlags  = 0;
   static final int FolderAbsoluteFlag   = 0x01;
   static final int FolderFlags    = 1;
   static final int FolderVersion  = FolderFlags + 8;
   static final int FolderTop      = FolderVersion + 8;
   static final int FolderBytes    = FolderTop + 8;
   static final int FolderCount    = FolderBytes + 8;
   static final int SizeOfFolder   = FolderCount;
   static ByteBuffer allocateFolder(final byte flags, final long version) {
     return ByteBuffer.allocate(SizeOfFolder).
       putLong(0).putLong(SizeOfFolder).putLong(Null).
       putLong(version).put(flags);
   }
   static public final class Folder implements org.k2v.Folder {
     protected final Folder parent;
     protected final ByteBuffer key;
     protected final Object brand;
     private   final ByteBuffer record;
 
     private
     Folder(final Folder parent, final ByteBuffer key,
            final Object brand, final ByteBuffer record) {
       this.parent = parent;
       this.key = null != key ? key.asReadOnlyBuffer() : null;
       this.brand = brand;
       this.record = record.asReadOnlyBuffer();
     }
 
     Folder(final ByteBuffer record) {
       this.parent = null;
       this.key = null;
       this.brand = new Object();
       this.record = record.asReadOnlyBuffer();
     }
 
     /**
      * Gets the version number.
      * <p>Each version has a number that is greater than the prior one.
      */
     public long getVersion(){return record.getLong(SizeOfFolder-FolderVersion);}
     protected long getBytes(){return record.getLong(SizeOfFolder-FolderBytes);}
     protected long getTop() {return record.getLong(SizeOfFolder-FolderTop);}
     protected boolean isAbsolute() {
       return 0 != (record.get(SizeOfFolder - FolderFlags) & FolderAbsoluteFlag);
     }
     
    Folder nest(final ByteBuffer k, final ByteBuffer v) {
       return new Folder(this, ByteBuffer.allocate(k.remaining()).put(k),
                        brand, ByteBuffer.allocate(v.remaining()).put(v));
     }
     
     Folder version(final Object newBrand, final ByteBuffer newRecord) {
       return new Folder(parent, key, newBrand, newRecord);
     }
   }
   
   /**
    * byte value[length]
    * long length
    */
   static final short TypeOfDocument = TypeOfFolder - 1;
   static final int DocumentLength = 8;
   static long sizeOfDocument(final long length) {return length+DocumentLength;}
   
   /** reference data is value */
   static final int SizeOfMaxMicroDocument = DataBits / Byte.SIZE;
   static boolean isAMicroDocument(final short type) {
     return 0 <= type && SizeOfMaxMicroDocument >= type;
   }
   static short typeOfMicroDocument(final long length) {
     assert SizeOfMaxMicroDocument >= length;
     return (short)length;
   }
   static int lengthOfMicroDocument(final short type) {
     assert isAMicroDocument(type);
     return type;
   }
   
   /**
    * byte value[length]
    */
   static final int SizeOfMaxSmallDocument = (TypeOfDocument - 1) & 0xFFFF;
   static boolean isASmallDocument(final short type) {
     return SizeOfMaxSmallDocument >= (type & 0xFFFF) && !isAMicroDocument(type);
   }
   static short typeOfSmallDocument(final long length) {
     assert SizeOfMaxSmallDocument >= length;
     assert SizeOfMaxMicroDocument < length;
     return (short)length;
   }
   static int lengthOfSmallDocument(final short type) {
     assert isASmallDocument(type);
     return type & 0xFFFF;
   }
   static long sizeOfSmallDocument(final int length) { return length; }
   
   static final int SizeOfChecksum   = 4;
   static final int SizeOfSeparator  = 128 / Byte.SIZE;
   /**
    * long priorFileLength > 0
    * byte separator[{@link #SizeOfSeparator}]
    * int  crc32SincePriorFileLength or {@link #DoubleSyncInsteadOfChecksum}
    */
   static final int SizeOfCommit     = SizeOfChecksum + SizeOfSeparator + 8;  
   static final int DoubleSyncInsteadOfChecksum = 0x512C512C;
 
   /**
    * byte magic[8] = "irT\r\n\EOF\n"
    * long firstVersion
    * byte separator[{@link #SizeOfSeparator}]
    */
   static final int SizeOfHeader   = SizeOfSeparator + 8 + 8;
   static final long MagicNumber   = 0x896972540D0A1A0AL;
 
   static long sizeOfVersion(final long rootBytes) {
     return SizeOfHeader + rootBytes + SizeOfCommit;
   }
   static final long sizeOfMinVersion = sizeOfVersion(SizeOfFolder);
 
   /**
    * Folder root
    * Commit commit
    */
   static final class Version {
     final long fileLength;
     final long rootRef;
     final Folder root;
     
     Version(final long fileLength, final Folder root) {
       this.fileLength = fileLength;
       this.rootRef = ref(TypeOfFolder, fileLength - SizeOfCommit);
       this.root = root;
     }
     
     Version(final long firstVersion) {
       this.fileLength = 0;
       this.rootRef = Null;
       this.root = new Folder(allocateFolder(FolderDefaultFlags, firstVersion));
     }
     
     protected long getBytes() { return sizeOfVersion(root.getBytes()); } 
   }
 
   protected final    File file;
   /** filename */
   public    final    String name;
   protected final    ByteBuffer separator;
   /** first version number */
   public    final    long firstVersion;
   protected final    Object brand;
 
   /** committed bytes shared cursor */
   protected final    RandomAccessFile cursor; 
   /** object that last used the cursor */
   protected          Object reading = null;
 
   protected final    Object queryLock = new Object();
   protected          int querying = 0;
   protected volatile Version committed;
 
   protected final    Semaphore updateLock = new Semaphore(1, true);
   protected          FileOutputStream tail = null;
   protected          boolean dirty;
   protected          Version pending;
 
   private Trie(final File file, final RandomAccessFile cursor,
                final ByteBuffer separator, final long firstVersion,
                final boolean dirty, final Version committed) {
     this.file = file;
     this.name = file.getName();
     this.separator = separator.asReadOnlyBuffer();
     this.firstVersion = firstVersion;
     this.brand = committed.root.brand;
     this.cursor = cursor;
     this.dirty = dirty;
     this.pending = this.committed = committed;
   }
 
   private Trie(final File file, final ByteBuffer separator,
                final long firstVersion) throws IOException {
     this(file, new RandomAccessFile(file, "r"), separator, firstVersion, false,
          new Version(firstVersion));
   }
   
   /**
    * Creates a new {@link Trie} file.
    * @param file  file location
    * @param prng  strong random number generator
    * @return empty {@link Trie} that MUST be {@linkplain #update() updated}
    */
   static public Trie create(final File file,
                             final SecureRandom prng) throws IOException {
     if (!file.createNewFile()) { throw new IOException(); }
     final ByteBuffer separator = ByteBuffer.allocate(SizeOfSeparator);
     prng.nextBytes(separator.array());
     return new Trie(file, separator, System.currentTimeMillis());
   }
   
   /**
    * Opens an existing {@link Trie} file.
    * @param file  file location
    * @return {@link Trie} ready for use
    * @throws EOFException {@code file} has invalid format
    */
   static public Trie open(final File file) throws IOException {
     if (!file.isFile()) { throw new EOFException(); }
     final RandomAccessFile cursor = new RandomAccessFile(file, "r");
     if (MagicNumber != cursor.readLong()) { throw new EOFException(); }
     final long firstVersion = cursor.readLong();
     final ByteBuffer separator = ByteBuffer.allocate(SizeOfSeparator);
     cursor.readFully(separator.array());
     // Search backwards through the file for a checkpoint separator.
     final ByteBuffer segment = ByteBuffer.allocate(SizeOfSeparator);
     final long capacity = cursor.length();
     for (long length = capacity; true; length -= 1) {
       if (sizeOfMinVersion > length) { throw new EOFException(); }
       cursor.seek(length - SizeOfCommit);
       final long priorFileLength = cursor.readLong();
       cursor.readFully(segment.array());
       if (!separator.equals(segment)) { continue; }
       final int checksumValue = cursor.readInt();
       if (DoubleSyncInsteadOfChecksum != checksumValue) {
         if (0 > priorFileLength || length - SizeOfChecksum < priorFileLength) {
           continue;
         }
         cursor.seek(priorFileLength);
         final CRC32 checksum = new CRC32();
         final byte[] buffer = new byte[1024];
         for (long n = length - SizeOfChecksum - priorFileLength; 0 != n;){
           final int d = cursor.read(buffer, 0, (int)Math.min(n, buffer.length));
           if (-1 == d) { throw new EOFException(); }
           checksum.update(buffer, 0, d);
           n -= d;
         }
         if (checksumValue != (int)checksum.getValue()) { continue; }
       }
       final ByteBuffer root = ByteBuffer.allocate(SizeOfFolder);
       cursor.seek(length - SizeOfCommit - SizeOfFolder);
       cursor.readFully(root.array());
       return new Trie(file, cursor, separator, firstVersion, length != capacity,
                       new Version(length, new Folder(root)));
     }
   }
   
   public void close() throws IOException {
     synchronized (queryLock) {
       while (0 != querying) {
         try {
           queryLock.wait();
         } catch (final InterruptedException e) {
           throw (IOException)new InterruptedIOException().initCause(e);
         }
       }
     }
     try {
       updateLock.acquire();
     } catch (final InterruptedException e) {
       throw (IOException)new InterruptedIOException().initCause(e);
     }
     try {
       if (null != tail) {
         tail.getChannel().force(true);
         tail.close();
       }
       cursor.close();
     } finally {
       updateLock.release();
     }
   }
 
   /** Gets the fraction of the file that is storing current data. */
   public float getLoadFactor() {
     final Version current = committed;
     return current.getBytes() / (float)current.fileLength;
   }
   
   /** Gets the last version number. */
   private long getLastVersion() { return committed.root.getVersion(); }
 
   public org.k2v.Query query() throws IOException {
     return new Query(committed.root);
   }
 
   static public final class Null extends org.k2v.Null {
     
     /**
      * a prior incarnation might exist
      */
     public final boolean absolute;
     
     /**
      * @param absolute  {@link #absolute}
      */
     public Null(final boolean absolute) {
       this.absolute = absolute;
     }
   }
 
   static final class Node {
     /** key length at this Trie node */
     protected final int depth;
     /** iterator over the last key byte for each branch out of this node */
     protected final ByteBuffer heads;
     /** iterator over each branch out of this node */
     protected final ByteBuffer branches;
     
     Node(final int depth, final ByteBuffer heads, final ByteBuffer branches) {
       this.depth = depth;
       this.heads = heads;
       this.branches = branches;
     }
   }
   
   protected final class Query extends org.k2v.Query {
     /** most recent version included in this query */
     protected final long lastVersion;
     protected       boolean closed = false;
     
     Query(final Folder root) {
       super(root);
       lastVersion = root.getVersion();
       synchronized (queryLock) {
         querying += 1;
       }
     }
     
     public void close() {
       synchronized (queryLock) {
         if (!closed) {
           closed = true;
           querying -= 1;
           if (0 == querying) {
             queryLock.notifyAll();
           }
         }
       }
       synchronized (cursor) {
         reading = null;
       }
     }
 
     public Value
     find(final org.k2v.Folder folder, final byte[] child) throws IOException {
       return find((Folder)folder, child);
     }
     Value find(final Folder folder, final byte[] child) throws IOException {
       if (closed) { throw new IOException(); }
       final Value owned = own(folder);
       if (!(owned instanceof Folder)) {
         return owned instanceof Null ? owned : new Null(true);
       }
       long ref = ((Folder)owned).getTop();
       if (Null != ref) {
         int depth = 0;
         while (true) {
           final short type = type(ref);
           final long at = data(ref);
           if (isAMap(type)) {
             if (child.length == depth) { break; }
             final int arity = arityOfMap(type);
             final ByteBuffer map = read(sizeOfMap(arity), at);
             final int index = searchMap(map, arity, child[depth]);
             if (!inMap(map, arity, index, child[depth])) { break; }
             depth += 1;
             ref = value(map, 8 * (arity - index));
           } else if (isARun(type)) {
             final int length = lengthOfRun(type);
             if (child.length < depth + length) { break; }
             final ByteBuffer run = read(sizeOfRun(length), at);
             boolean matching = true;
             for (int i = 0; i != length && matching; ++i, ++depth) {
               matching = child[depth] == run.get(i);
             }
             if (!matching) { break; }
             ref = value(run, RunBranch);
           } else if (TypeOfLeaf == type) {
             final ByteBuffer leaf = read(SizeOfLeaf, at);
             if (child.length == depth) {
               ref = value(leaf, LeafChild);
             } else {
               ref = value(leaf, LeafBranch);
             }
           } else {
             if (child.length != depth) { break; }
             return readEntry(folder, ByteBuffer.wrap(child), type, at);
           }
         }
       }
       return new Null(((Folder)owned).isAbsolute());
     }
 
     Value readEntry(final Folder folder, final ByteBuffer child,
                     final short type, final long at) throws IOException {
       if (isASmallDocument(type)) {
         final long length = lengthOfSmallDocument(type);
         return new Document(length, at - length);
       } else if (isAMicroDocument(type)) {
         return new MicroDocument(lengthOfMicroDocument(type), at);
       } else if (TypeOfDocument == type) {
        final long length = ref(type, at) == folder.getTop() ?
          folder.getBytes() - SizeOfFolder - DocumentLength :
          read(DocumentLength, at).getLong();
         return new Document(length, at - DocumentLength - length);
       } else if (TypeOfFolder == type) {
        return folder.nest(child, read(SizeOfFolder, at));
       } else if (TypeOfNull == type) {
         return new Null(true);
       } else {
         throw new AssertionError();
       }
     }
 
     private final ByteBuffer buffer =
       ByteBuffer.allocate(Math.max(SizeOfMapMax, SizeOfRunMax));
     ByteBuffer read(final int size, final long address) throws IOException {
       synchronized (cursor) {
         reading = null;
         cursor.seek(address - size);
         cursor.readFully(buffer.array(), 0, size);
       }
       buffer.rewind().limit(size);
       return buffer;
     }
     
     final class MicroDocument extends org.k2v.Document {
       private long data;
       private int position = 0;
       private int markedPosition = position;
       
       MicroDocument(final int length, final long data) {
         super(length);
         this.data = data;
       }
 
       public @Override boolean markSupported() { return true; }
       public @Override void mark(final int readlimit) {
         markedPosition = position;
       }
       public @Override void reset() {
         position = markedPosition;
       }
 
       public @Override int read() throws IOException {
         if (length == position) { return -1; }
         final byte r = (byte)(data >> ((DataBytes - position - 1) * Byte.SIZE));
         position += 1;
         return r & 0xFF;
       }
     }
 
     final class Document extends org.k2v.Document {
       private final long last;
       private       long position;
       private       long markedPosition;
 
       Document(final long length, final long value) {
         super(length);
         last = value + length;
         position = value;
         markedPosition = position;
       }
 
       private void restore() throws IOException {
         if (this != reading) {
           if (closed) { throw new IOException(); }
           cursor.seek(position);
           reading = this;
         }
       }
 
       public @Override boolean markSupported() { return true; }
       public @Override void mark(final int readlimit) {
         markedPosition = position;
       }
       public @Override void reset() {
         synchronized (cursor) {
           if (this == reading) {
             reading = null;
           }
         }
         position = markedPosition;
       }
 
       public @Override int read() throws IOException {
         if (last == position) { return -1; }
         synchronized (cursor) {
           restore();
           reading = null;
           final byte r = cursor.readByte();
           reading = this;
           position += 1;
           return r & 0xFF;
         }
       }
 
       public @Override int
       read(final byte[] b, final int off, final int len) throws IOException {
         if (0 == len) { return 0; }
         if (last == position) { return -1; }
         synchronized (cursor) {
           restore();
           reading = null;
           final int d = cursor.read(b, off, (int)Math.min(len, last-position));
           if (-1 == d) { throw new EOFException(); }
           reading = this;
           position += d;
           return d;
         }
       }
 
       public @Override long skip(final long n) throws IOException {
         if (0 >= n) { return 0; }
         synchronized (cursor) {
           if (this == reading) {
             reading = null;
           }
         }
         final long d = Math.min(n, last - position);
         position += d;
         return d;
       }
     }
     
     private Value own(final Folder folder) throws IOException {
       if (brand == folder.brand && lastVersion >= folder.getVersion()) {
         return folder;
       }
       if (null == folder.parent) { return root; }
       final ByteBuffer key = folder.key.duplicate();
       final byte[] keyBytes = new byte[key.limit()];
       key.rewind();
       key.get(keyBytes);
       return find(folder.parent, keyBytes);
     }
     
     /**
      * Lexicographical listing of a {@link Folder}'s keys.
      * <p>A key {@code byte} is compared as an unsigned number; consequently,
      * UTF-8 keys are returned in lexicographical order.
      */
     protected Iterator list(final Folder folder) throws IOException {
       if (closed) { throw new IOException(); }
       final Value owned = own(folder);
       if (!(owned instanceof Folder)) { return new Iterator(folder); }
       final long top = ((Folder)owned).getTop();
       if (TypeOfNull == type(top)) { return new Iterator(folder); }
       return new Iterator(folder, ByteBuffer.allocate(8).putLong(0, top));
     }
     
     protected final class Iterator implements java.util.Iterator<ByteBuffer> {
       private final Folder folder;
       private final ArrayList<Node> stack = new ArrayList<Node>();
       private       ByteBuffer rwkey = ByteBuffer.allocate(16);
       private       ByteBuffer rokey = rwkey.asReadOnlyBuffer();
       private       short type = type(Null);
       private       long at = data(Null);
       
       Iterator(final Folder folder) {
         this.folder = folder;
       }
       
       Iterator(final Folder folder, final ByteBuffer top) {
         this.folder = folder;
         stack.add(new Node(0, null, top));
       }
       
       protected short getValueType() { return type; }
       protected long getValueAddress() { return at; }
       /**
        * Reads the {@link Value} for the last {@linkplain #readNext read key}.
        */
       public Value readValue() throws IOException {
         if (closed) { throw new IOException(); }
         rokey.rewind();
         return readEntry(folder, rokey, type, at);
       }
       protected long transferValueTo(final WritableByteChannel target
                                      ) throws IOException {
         if (closed) { throw new IOException(); }
         final long size = TypeOfDocument == type ?
          (ref(type, at) == folder.getTop() ?
             folder.getBytes() - SizeOfFolder :
             sizeOfDocument(read(DocumentLength, at).getLong())) :
           sizeOfSmallDocument(lengthOfSmallDocument(type));
         if (size != cursor.getChannel().transferTo(at - size, size, target)) {
           throw new IOException();
         }
         return size;
       }
 
       /**
        * Moves to the next {@link Value}.
        * @return key of the next {@link Value}
        */
       public ByteBuffer readNext() throws IOException {
         if (closed) { throw new IOException(); }
         while (true) {
           if (stack.isEmpty()) { throw new NoSuchElementException(); }
           final Node node = stack.get(stack.size() - 1);
           if (!node.branches.hasRemaining()) {
             stack.remove(stack.size() - 1);
             continue;
           }
           rwkey.limit(node.depth);
           rokey.limit(rwkey.limit());
           if (node.depth > 0) { rwkey.put(node.depth - 1, node.heads.get()); }
           long ref = node.branches.getLong();
           while (true) {
             type = type(ref);
             at = data(ref);
             if (!isARun(type)) { break; }
             final int length = lengthOfRun(type);
             final int size = sizeOfRun(length);
             final ByteBuffer run = read(size, at);
             ref = value(run, RunBranch);
             if (rwkey.limit() + length > rwkey.capacity()) {
               rwkey = ByteBuffer.allocate(2*(rwkey.limit()+length)).put(rwkey);
               rokey = rwkey.asReadOnlyBuffer();
             }
             rwkey.limit(rwkey.limit() + length);
             rokey.limit(rwkey.limit());
             run.limit(length);
             rwkey.put(run);
           }
           if (isAMap(type)) {
             final int arity = arityOfMap(type);
             final int size = sizeOfMap(arity);
             final ByteBuffer map = read(size, at);
             if (rwkey.limit() == rwkey.capacity()) {
               rwkey = ByteBuffer.allocate(2 * (rwkey.limit() + 1)).put(rwkey);
               rokey = rwkey.asReadOnlyBuffer();
             }
             stack.add(new Node(rwkey.limit() + 1, listMap(map, arity),
                                (ByteBuffer)ByteBuffer.allocate(map.remaining()).
                                  put(map).rewind()));
           } else if (TypeOfLeaf == type) {
             final ByteBuffer leaf = read(SizeOfLeaf, at);
             final long child = leaf.getLong();
             type = type(child);
             at = data(child);
             final int depth = rwkey.limit();
             stack.add(new Node(depth, 0 == depth ? null :
                 ByteBuffer.allocate(1).put(0, rwkey.get(depth - 1)),
                 ByteBuffer.allocate(8).putLong(0, leaf.getLong())));
             rokey.rewind();
             return rokey;
           } else {
             rokey.rewind();
             return rokey;
           }
         }
       }
       
       public boolean hasNext() {
         for (int i = stack.size(); 0 != i--;) {
           final ByteBuffer branches = stack.get(i).branches;
           if (branches.position() != branches.limit()) { return true; }
         }
         return false;
       }
       public ByteBuffer next() {
         try {
           return readNext();
         } catch (final IOException e) { throw new RuntimeException(e); }
       }
       public void remove() { throw new UnsupportedOperationException(); }
     }
   }
   
   static final class Slot {
     protected final Slot folderSlot;
     protected       ByteBuffer var;
     protected       ByteBuffer record;
     
     Slot(final Slot folderSlot, final ByteBuffer var, final ByteBuffer record) {
       this.folderSlot = folderSlot;
       this.var = var;
       this.record = record;
     }
   }
 
   public org.k2v.Update update() throws IOException {
     try {
       updateLock.acquire();
     } catch (final InterruptedException e) {
       throw (IOException)new InterruptedIOException().initCause(e);
     }
     Update r = null;
     try {
       r = new Update(pending);
     } finally {
       if (null == r) {
         updateLock.release();
       }
     }
     return r;
   }
   
   final class Update implements org.k2v.Update {
     
     /** All Trie nodes that need to be written out. */
     private final ArrayList<Slot> todo = new ArrayList<Slot>();
 
     /**
      * Maps an address from the prior version into a {@link #todo} index.
      * A synthetic address is used for newly created records.
      */
     private final HashMap<Long,Integer> modified = new HashMap<Long,Integer>();
     
     /** Maps an old ref to the size of the document that overwrote it. */
     protected final HashMap<Long,Long> written = new HashMap<Long,Long>();
     
     private         boolean checksumming = true;
     protected       boolean corrupted = false;
     private         boolean closed = false;
     private   final Version prior;
     protected final long version; 
     protected       long address;
     private   final CRC32 checksum;
     protected final OutputStream out;
     protected final ByteBuffer microBuffer =
       ByteBuffer.wrap(new byte[TypeBytes + DataBytes], TypeBytes, DataBytes);
     
     Update(final Version prior) throws IOException {
       this.prior = prior;
       this.version = Math.max(prior.root.getVersion() + 1,
                               System.currentTimeMillis());
       this.address = prior.fileLength;
       
       // Revert any previously failed update.
       if (dirty) {
         if (null != tail) {
           tail.close();
           tail = null;
         }
         final RandomAccessFile trim = new RandomAccessFile(file, "rw");
         trim.setLength(prior.fileLength);
         trim.close();
       } else {
         dirty = true;
       }
       if (null == tail) {
         tail = new FileOutputStream(file, true);
       }
       checksum = new CRC32();
       out = new BufferedOutputStream(new CheckedOutputStream(tail, checksum));
       if (0 == address) {
         // Initialize a new file.
         out.write(ByteBuffer.allocate(8).putLong(MagicNumber).array());
         out.write(ByteBuffer.allocate(8).putLong(firstVersion).array());
         separator.rewind();
         out.write(
             ByteBuffer.allocate(separator.remaining()).put(separator).array());
         address = 8 + 8 + separator.limit();
         modified.put(data(prior.rootRef), todo.size());
         todo.add(new Slot(null, ByteBuffer.allocate(8),
                           allocateFolder(FolderDefaultFlags, version)));
       }
     }
 
     public void close() {
       if (!closed) {
         closed = true;
         if (!corrupted && todo.isEmpty()) {
           dirty = false;
         }
         corrupted = true;
         updateLock.release();
       }
     }
 
     public void commit() throws IOException {
       if (corrupted) { throw new IOException(); }
       corrupted = true;
       if (todo.isEmpty()) {
         dirty = false;
         return;
       }
       out.flush();
 
       // allocate addresses for all the pending nodes
       final long startAddress = address;
       final ByteBuffer[] nodes = new ByteBuffer[todo.size() + 3];
       for (int i = todo.size(), j = 0; 0 != i--; ++j) {
         final Slot slot = todo.get(i);
         nodes[j] = slot.record;
         address += slot.record.rewind().remaining();
         slot.var.putLong(0, ref(type(slot.var), address));
       }
       nodes[nodes.length-3]=ByteBuffer.allocate(8).putLong(0, prior.fileLength);
       address += nodes[nodes.length-3].remaining();
       separator.rewind();
       nodes[nodes.length-2]=ByteBuffer.allocate(SizeOfSeparator).put(separator);
       address += nodes[nodes.length-2].rewind().remaining(); 
       nodes[nodes.length-1] = ByteBuffer.allocate(SizeOfChecksum);
       nodes[nodes.length-1].limit(0);
 
       final int checksumValue;
       if (checksumming) {
         for (final ByteBuffer node : nodes) {
           checksum.update(node.array(),
                           node.arrayOffset() + node.position(),
                           node.remaining());
         }
         checksumValue = (int)checksum.getValue();
       } else {
         checksumValue = DoubleSyncInsteadOfChecksum;
       }
       address += nodes[nodes.length - 1].limit(SizeOfChecksum).remaining(); 
       nodes[nodes.length - 1].putInt(0, checksumValue);
       final FileChannel channel = tail.getChannel();
       if (DoubleSyncInsteadOfChecksum == checksumValue) {
         if (address - SizeOfChecksum - SizeOfSeparator - startAddress !=
           channel.write(nodes, 0, nodes.length - 2)) {throw new IOException();}
         channel.force(false);
         if (SizeOfSeparator + SizeOfChecksum !=
             channel.write(nodes, nodes.length-2, 2)) {throw new IOException();}
       } else {
         if (address - startAddress != channel.write(nodes)) {
           throw new IOException();
         }
       }
       
       // commit the update
       final Version next = pending =
         new Version(address, prior.root.version(brand, nodes[nodes.length-4]));
       dirty = false;
       close();          // allow a concurrent update while waiting for the sync
       channel.force(false);
       synchronized (queryLock) {
         if (next.fileLength > committed.fileLength) {
           committed = next;
         }
       }
     }
 
     public OutputStream open(final org.k2v.Folder folder,
                              final byte[] key) throws IOException {
       if (corrupted) { throw new IOException(); }
       corrupted = true;
       for (int i = TypeBytes; i != microBuffer.position(); ++i) {
         microBuffer.put(i, (byte)0);
       }
       microBuffer.position(TypeBytes);
       return new OutputStream() {
         private final long startAddress = address;
         private       boolean incomplete = false;
         
         @Override public void close() throws IOException {
           if (incomplete) { return; }
           incomplete = true;
           final Folder parent = (Folder)folder;
           final long length = address - startAddress;
           if (0 == length) {
             replace(descend(parent, ByteBuffer.wrap(key)), 0,
                     ref(typeOfMicroDocument(microBuffer.position() - TypeBytes),
                         microBuffer.getLong(0)));
           } else if (SizeOfMaxSmallDocument >= length) {
             final long ref = ref(typeOfSmallDocument(length), address);
             written.put(ref, length);
             replace(descend(parent, ByteBuffer.wrap(key)), length, ref);
           } else {
             out.write(ByteBuffer.allocate(8).putLong(length).array());
             address += 8;
             final long ref = ref(TypeOfDocument, address); 
             written.put(ref, length + 8);
             replace(descend(parent, ByteBuffer.wrap(key)), length + 8, ref);
           }
           corrupted = false;
         }
 
         @Override public void write(final int b) throws IOException {
           if (incomplete) { throw new IOException(); }
           incomplete = true;
           if (startAddress == address) {
             if (microBuffer.hasRemaining()) {
               microBuffer.put((byte)b);
             } else {
               out.write(microBuffer.array(), TypeBytes, DataBytes);
               address += DataBytes;
               out.write(b);
               address += 1;
             }
           } else {
             out.write(b);
             address += 1;
           }
           incomplete = false;
         }
         
         @Override public void write(final byte[] b, final int off,
                                     final int len) throws IOException {
           if (incomplete) { throw new IOException(); }
           incomplete = true;
           if (startAddress == address) {
             if (microBuffer.position() + len <= microBuffer.limit()) {
               microBuffer.put(b, off, len);
             } else {
               final int n = microBuffer.position() - TypeBytes;
               if (0 != n) {
                 out.write(microBuffer.array(), TypeBytes, n);
                 address += n;
               }
               out.write(b, off, len);
               address += len;
             }
           } else {
             out.write(b, off, len);
             address += len;
           }
           incomplete = false;
         }
       };
     }
 
     private Folder lastTouched = null;
     private Slot lastTouchedSlot = null;
 
     public Folder
     touch(final org.k2v.Folder folder) throws IOException {
       if (corrupted) { throw new IOException(); }
       corrupted = true;
       descend((Folder)folder, null);
       lastTouched = lastTouched.version(brand, lastTouchedSlot.record);
       corrupted = false;
       return lastTouched;
     }
 
     public Folder nest(final org.k2v.Folder folder,
                        final byte[] key) throws IOException {
       if (corrupted) { throw new IOException(); }
       corrupted = true;
      descend(((Folder)folder).nest(ByteBuffer.wrap(key),
                                     ByteBuffer.allocate(0)), null);
       lastTouched = lastTouched.version(brand, lastTouchedSlot.record);
       corrupted = false;
       return lastTouched;
     }
 
     /**
      * Get the corresponding variable for a {@link Folder} key.
      * @param folder  {@link Folder} to update
      * @param key     key for {@link Value} to create or update
      * @return corresponding variable for the {@link Value}
      * @throws IOException  any I/O problem
      */
     protected ByteBuffer descend(final Folder folder,
                                  final ByteBuffer key) throws IOException {
       final Slot folderSlot;
       if (folder == lastTouched) {
         folderSlot = lastTouchedSlot;
       } else {
         if (null == folder.parent) {
           folderSlot = cache(null, SizeOfFolder, SizeOfFolder,
                              ByteBuffer.allocate(8).putLong(0, prior.rootRef));
         } else {
           final ByteBuffer var = descend(folder.parent, folder.key);
           final short type = type(var);
           if (TypeOfFolder == type) {
             folderSlot = cache(lastTouchedSlot, SizeOfFolder,SizeOfFolder, var);
           } else {
             final byte flags = TypeOfNull == type ?
               lastTouchedSlot.record.get(SizeOfFolder - FolderFlags) :
               FolderAbsoluteFlag; 
             folderSlot = new Slot(lastTouchedSlot, var,
                                   allocateFolder(flags, version));
             replace(var, SizeOfFolder, newRef(TypeOfFolder, folderSlot));
           }
         }
         var(folderSlot.record, FolderVersion).putLong(0, version);
         lastTouched = folder;
         lastTouchedSlot = folderSlot;
       }
       if (null == key) { return folderSlot.var; }
       ByteBuffer var = var(folderSlot.record, FolderTop);
       int depth = 0;
       long delta = 0;
       while (true) {
         final short type = type(var);
         if (key.limit() == depth) {
           // at the Trie depth for storing the new child
           if (isAMap(type) || isARun(type)) {
             // create a new Leaf to hold the new child
             final long branch = var.getLong(0);
             final ByteBuffer leaf = allocateLeaf(branch, Null);
             final Integer slot = modified.get(data(branch));
             if (null != slot) {
               todo.get(slot).var = var(leaf, LeafBranch);
             }
             var.putLong(0, newRef(TypeOfLeaf, new Slot(folderSlot, var, leaf)));
             delta += SizeOfLeaf;
             var = var(leaf, LeafChild);
           } else if (TypeOfLeaf == type) {
             // overwrite the child of an existing Leaf
             var = var(cache(folderSlot, SizeOfLeaf, SizeOfLeaf, var).record,
                       LeafChild);
           } else {
             // overwrite a Leaf-less child
           }
           break;
         } else if (isAMap(type)) {
           // insert into an existing Map
           final int arity = arityOfMap(type);
           final int size = sizeOfMap(arity);
           final Slot map = cache(folderSlot, SizeOfMapMax, size, var);
           final byte head = key.get(depth);
           final int index = searchMap(map.record, arity, head);
           if (inMap(map.record, arity, index, head)) {
             var = var(map.record, (arity - index) * 8);
           } else {
             map.record = growMap(map.record, arity, index, head);
             delta += map.record.limit() - size;
             for (int j = 0; j != index; ++j) {
               final ByteBuffer pos = var(map.record, (arity + 1 - j) * 8);
               final Integer slot = modified.get(data(pos.getLong(0)));
               if (null != slot) {
                 todo.get(slot).var = pos;
               }
             }
             final long old = var.getLong(0);
             final long ref = ref(typeOfMap(arity + 1), data(old)); 
             var.putLong(0, ref);
             var = var(map.record, (arity + 1 - index) * 8);
           }
           depth += 1;
         } else if (isARun(type)) {
           final int length = lengthOfRun(type);
           final int size = sizeOfRun(length);
           final Slot run = cache(folderSlot, size, size, var);
           for (int i = 0; true; ++i) {
             if (i == length) {
               var = var(run.record, RunBranch);
               break;
             }
             if (key.limit() != depth && key.get(depth) == run.record.get(i)) {
               depth += 1;
               continue;
             }
             if (0 != i) {
               // create a new Run to retain the prefix
               final ByteBuffer prefix = ByteBuffer.allocate(sizeOfRun(i)).
                 put(run.record.array(), run.record.arrayOffset(), i).
                 putLong(var.getLong(0));
               var.putLong(0, newRef(typeOfRun(i),
                                     new Slot(folderSlot, var, prefix)));
               delta += prefix.limit();
               var = var(prefix, RunBranch);
             }
             if (key.limit() == depth) {
               // use the rest of the Run in a new Leaf branch
               final long old = var.getLong(0);
               final long suffix = ref(typeOfRun(length - i), data(old));
               final ByteBuffer leaf = allocateLeaf(suffix, Null);
               run.var = var(leaf, LeafBranch);
               run.record.position(i);
               run.record = run.record.slice();
               delta -= i;
               var.putLong(0, newRef(TypeOfLeaf, new Slot(folderSlot,var,leaf)));
               delta += leaf.limit();
               var = var(leaf, LeafChild);
             } else {
               // create a new map
               final int arity = 2;
               final ByteBuffer one = growMap(allocateMap(), 0,
                                              0, run.record.get(i));
               final int index = searchMap(one, 1, key.get(depth));
               final ByteBuffer map = growMap(one, 1, index, key.get(depth));
               final ByteBuffer other = var(map, (arity - (index ^ 1)) * 8);
               if (i + 1 == length) {
                 // discard the remaining Run, saving just its branch
                 final long branch = value(run.record, RunBranch);
                 final Integer slot = modified.get(data(branch));
                 if (null != slot) {
                   todo.get(slot).var = other;
                 }
                 other.putLong(0, branch);
                 run.record = ByteBuffer.allocate(0);
                 run.var = ByteBuffer.allocate(8);
                 delta -= size;
               } else {
                 // use the rest of the Run as the other branch
                 final long old = var.getLong(0);
                 final long suffix = ref(typeOfRun(length - i - 1), data(old));
                 other.putLong(0, suffix);
                 run.var = other;
                 run.record.position(i + 1);
                 run.record = run.record.slice();
                 delta -= i + 1;
               }
               var.putLong(0, newRef(typeOfMap(arity),
                                     new Slot(folderSlot, var, map)));
               delta += map.limit();
               var = var(map, (arity - index) * 8);
               depth += 1;
             }
             break;
           }
         } else if (TypeOfLeaf == type) {
           // proceed down the Leaf branch
           final Slot leaf = cache(folderSlot, SizeOfLeaf, SizeOfLeaf, var);
           var = var(leaf.record, LeafBranch);
         } else if (TypeOfNull == type) {
           // create a new Run to store remaining key bytes
           final int length = Math.min(key.limit() - depth, RunMaxLength);
           final ByteBuffer segment = key.duplicate();
           segment.position(depth);
           segment.limit(depth + length);
           final ByteBuffer run = allocateRun(segment);
           var.putLong(0, newRef(typeOfRun(length),
                                 new Slot(folderSlot, var, run)));
           delta += run.limit();
           var = var(run, RunBranch);
           depth += length;
         } else {
           // put the existing child in a Leaf and continue with a Null Branch
           final long child = var.getLong(0);
           final ByteBuffer leaf = allocateLeaf(Null, child);
           var.putLong(0, newRef(TypeOfLeaf, new Slot(folderSlot, var, leaf)));
           delta += leaf.limit();
           final Integer slot = modified.get(data(child));
           if (null != slot) {
             todo.get(slot).var = var(leaf, LeafChild);
           }
           var = var(leaf, LeafBranch);
         }
       }
       if (0 != delta) { changeSize(folderSlot, delta); }
       return var;
     }
     
     protected void replace(final ByteBuffer var, final long size,
                            final long ref) throws IOException {
       final long garbage;
       final short type = type(var);
       switch (type) {
         case TypeOfNull: {
           final ByteBuffer count = var(lastTouchedSlot.record, FolderCount);
           count.putLong(0, count.getLong(0) + 1);
           garbage = 0;
           break;
         }
         case TypeOfFolder: {
           final ByteBuffer record;
           final long at = data(var.getLong(0));
           final Integer index = modified.get(at);
           if (null != index) {
             final Slot slot = todo.get(index);
             record = slot.record;
             slot.record = ByteBuffer.allocate(0);
             slot.var = ByteBuffer.allocate(8);
           } else {
             record = ByteBuffer.allocate(SizeOfFolder);
             read(at, record);
           }
           garbage = value(record, FolderBytes);
           break;
         }
         case TypeOfDocument: {
           final long old = var.getLong(0);
           Long oldSize = written.get(old);
           if (null == oldSize) {
             final ByteBuffer length = ByteBuffer.allocate(DocumentLength);
             read(data(old), length);
             oldSize = sizeOfDocument(length.getLong());
           }
           garbage = oldSize;
           break;
         }
         default: {
           if (isAMicroDocument(type)) {
             garbage = 0;
           } else if (isASmallDocument(type)) {
             garbage = sizeOfSmallDocument(lengthOfSmallDocument(type)); 
           } else { throw new AssertionError(); }
         }
       }
       var.putLong(0, ref);
       if (size != garbage) { changeSize(lastTouchedSlot, size - garbage); }
     }
     
     /** Allocates a temporary address from the end of the file. */
     private long newRef(final short type, final Slot slot) {
       final long at = MaxAddress - todo.size();
       if (at <= address) { throw new AssertionError(); }
       modified.put(at, todo.size());
       todo.add(slot);
       return ref(type, at);
     }
 
     /**
      * Propagates a size change all the way up to the root {@link Folder}.
      * @param folderSlot  {@link Folder} that changed size
      * @param delta       size change in bytes
      */
     private void changeSize(Slot folderSlot, final long delta) {
       do {
         final ByteBuffer bytes = var(folderSlot.record, FolderBytes);
         bytes.putLong(0, bytes.getLong(0) + delta);
       } while (null != (folderSlot = folderSlot.folderSlot));
     }
 
     /**
      * Reads a record and adds it to the dirty set.
      * @param folderSlot  parent {@link Folder} record
      * @param capacity    max capacity of record
      * @param size        current size of record
      * @param var         variable in parent record that refers to the record
      * @return dirty slot for the loaded record
      */
     private Slot cache(final Slot folderSlot, final int capacity,
                        final int size, final ByteBuffer var) throws IOException{
       final long ref = var.getLong(0);
       final long at = data(ref);
       final Integer index = modified.get(at);
       if (null != index) { return todo.get(index); }
       final Slot slot = new Slot(folderSlot, var, ByteBuffer.wrap(
           new byte[capacity], capacity - size, size).slice());
       read(at, slot.record);
       modified.put(at, todo.size());
       todo.add(slot);
       return slot;
     }
 
     private void read(final long at, final ByteBuffer record)throws IOException{
       synchronized (cursor) {
         reading = null;
         cursor.seek(at - record.remaining());
         cursor.readFully(record.array(),
                          record.arrayOffset() + record.position(),
                          record.remaining());
         record.position(record.limit());
       }
     }
     
     private void patch(final FileChannel channel,
                        final Folder to, final Slot toSlot,
                        final Query query, final Folder from) throws IOException{
       final Query.Iterator i = query.list(from);
       while (i.hasNext()) {
         final ByteBuffer key = i.readNext();
         final short type = i.getValueType();
         if (TypeOfFolder == type) {
           final byte[] child = new byte[key.limit()];
           key.get(child);
           patch(channel, nest(to, child), lastTouchedSlot,
                 query, (Folder)i.readValue());
        } else if (isAMicroDocument(type)) {
           replace(descend(to, key), 0, ref(type, i.getValueAddress()));
         } else {
           final long size = i.transferValueTo(channel); 
           address += size;
           final long ref = ref(type, address);
           written.put(ref, size);
           replace(descend(to, key), size, ref);
         }
       }
       var(toSlot.record, FolderVersion).putLong(0, from.getVersion());
     }
 
     protected void
     patch(final Query query, final Folder base) throws IOException {
       checksumming = false;
       descend(base, null);
       out.flush();
       patch(tail.getChannel(), base, lastTouchedSlot, query, base);
     }
   }
   
   /**
    * Recursively copies a {@link Folder} to this {@link K2V}.
    * @param base  base {@link Folder} to copy to this {@link K2V}
    */
   private void patch(final Query query, final Folder base) throws IOException {
     final Update update = (Update)update();
     try {
       update.patch(query, base);
       update.commit();
     } finally {
       update.close();
     }
   }
   
   /**
    * Copies the current state to a new file.
    * @param to  file location
    * @return compacted {@link Trie} ready for use
    */
   public Trie compact(final File to) throws IOException {
     if (!to.createNewFile()) { throw new IOException(); }
     final Trie compact = new Trie(to, separator.duplicate(), firstVersion);
     final Query query = (Query)query();
     try {
       compact.patch(query, (Folder)query.root);
     } finally {
       query.close();
     }
     return compact;
   }
   
   /**
    * Creates a patch file that can be {@linkplain #merge merged} into this one.
    * @param to  file location
    * @return empty {@link Trie} that MUST be {@linkplain #update() updated}
    */
   public Trie spawn(final File to) throws IOException {
     if (!to.createNewFile()) { throw new IOException(); }
     return new Trie(to, separator.duplicate(),
                     Math.max(getLastVersion() + 1, System.currentTimeMillis()));
   }
 
   /**
    * Copies the current content of another {@link Trie} into this one.
    * @param patch {@link Trie} to copy
    */
   public void merge(final Trie patch) throws IOException {
     final Query query = (Query)patch.query();
     try {
       patch(query, (Folder)query.root);
     } finally {
       query.close();
     }
   }
 }
