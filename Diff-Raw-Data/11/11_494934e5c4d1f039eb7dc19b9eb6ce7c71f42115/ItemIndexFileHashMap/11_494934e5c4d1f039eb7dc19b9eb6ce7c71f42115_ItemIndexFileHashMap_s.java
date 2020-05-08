 package com.github.zhongl.ipage;
 
 import com.google.common.base.Throwables;
 
 import javax.annotation.concurrent.NotThreadSafe;
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 
 /**
  * {@link ItemIndexFileHashMap} is a dir-based hash map for {@link ItemIndex}.
  * <p/>
  * It is a implemente of separate chain hash table, more infomation you can find in "Data Structures & Algorithms In Java".
  *
  * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
  */
 @NotThreadSafe
public final class ItemIndexFileHashMap implements Closeable {
 
     private final File file;
 
     private final RandomAccessFile randomAccessFile;
     private final MappedByteBuffer mappedByteBuffer;
     private final Bucket[] buckets;
 
     public ItemIndexFileHashMap(File file, int initCapacity) throws IOException {
         this.file = file;
         randomAccessFile = new RandomAccessFile(file, "rws");
         randomAccessFile.setLength(initCapacity);
        mappedByteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.PRIVATE, 0L, initCapacity);
         buckets = createBuckets(initCapacity / Bucket.LENGTH);
     }
 
     public File basefile() {
         return file;
     }
 
     public ItemIndex put(Md5Key key, ItemIndex itemIndex) {
         ItemIndex previous = buckets[hashAndMode(key)].put(key, itemIndex);
         fsync();
         return previous;
     }
 
     public ItemIndex get(Md5Key key) {
         return buckets[hashAndMode(key)].get(key);
     }
 
     public ItemIndex remove(Md5Key key) {
         ItemIndex exit = buckets[hashAndMode(key)].remove(key);
         fsync();
         return exit;
     }
 
     @Override
     public void close() throws IOException {
         DirectByteBufferCleaner.clean(mappedByteBuffer);
         randomAccessFile.close();
     }
 
     private int hashAndMode(Md5Key key) {
         return Math.abs(key.hashCode()) % buckets.length;
     }
 
     private Bucket[] createBuckets(int size) {
         Bucket[] buckets = new Bucket[size];
         for (int i = 0; i < buckets.length; i++) {
             buckets[i] = new Bucket(slice(mappedByteBuffer, i * Bucket.LENGTH, Bucket.LENGTH));
         }
         return buckets;
     }
 
     private void fsync() {
         try {
             randomAccessFile.getChannel().force(false);
         } catch (IOException e) {
             Throwables.propagate(e);
         }
     }
 
     private static ByteBuffer slice(ByteBuffer buffer, int offset, int length) {
         buffer.position(offset);
         buffer.limit(offset + length);
         return buffer.slice();
     }
 
     /**
      * Bucket has 141 slot for storing tuple of {@link Md5Key} and {@link ItemIndex}.
      * <p/>
      * Every slot has a head byte to indicate it is empty, occupied or released.
      */
     private static class Bucket {
 
         public static final int LENGTH = 4096; // 4K
         private final Slot[] slots;
 
         public Bucket(ByteBuffer buffer) {
             slots = createSlots(buffer);
         }
 
         private Slot[] createSlots(ByteBuffer buffer) {
             Slot[] slots = new Slot[LENGTH / Slot.LENGTH];
             for (int i = 0; i < slots.length; i++) {
                 slots[i] = new Slot(slice(buffer, i * Slot.LENGTH, Slot.LENGTH));
             }
             return slots;
         }
 
         public ItemIndex put(Md5Key key, ItemIndex itemIndex) {
             int firstRelease = -1;
             for (int i = 0; i < slots.length; i++) {
                 switch (slots[i].state()) {
                     case EMPTY:
                         return slots[i].add(key, itemIndex);
                     case OCCUPIED:
                         if (slots[i].keyEquals(key)) return slots[i].replace(key, itemIndex);
                         break;
                     case RELEASED:
                         if (firstRelease < 0) firstRelease = i;
                         break;
                 }
                 // continue to check rest slots whether contain the key.
             }
             if (firstRelease < 0) throw new OverflowException("No slot for new item index.");
             return slots[firstRelease].add(key, itemIndex);
         }
 
         public ItemIndex get(Md5Key key) {
             for (Slot slot : slots) {
                 if (slot.state() == Slot.State.EMPTY) return null; // because rest slots are all empty
                 if (slot.state() == Slot.State.OCCUPIED && slot.keyEquals(key)) return slot.itemIndex();
             }
             return null;
         }
 
         public ItemIndex remove(Md5Key key) {
             for (Slot slot : slots) {
                 if (slot.state() == Slot.State.EMPTY) return null; // because rest slots are all empty
                 if (slot.state() == Slot.State.OCCUPIED && slot.keyEquals(key)) return slot.release();
             }
             return null;
         }
 
         private static class Slot {
 
             public static final int LENGTH = 1/*head byte*/ + Md5Key.LENGTH + ItemIndex.LENGTH;
             private final ByteBuffer buffer;
 
             public Slot(ByteBuffer buffer) {
                 this.buffer = buffer;
             }
 
             public ItemIndex add(Md5Key key, ItemIndex itemIndex) {
                 buffer.position(0);
                 buffer.put(State.OCCUPIED.toByte());
                 key.writeTo(buffer);
                 itemIndex.writeTo(buffer);
                 return null;
             }
 
             public State state() {
                 return State.valueOf(buffer.get(0));
             }
 
             public boolean keyEquals(Md5Key key) {
                 buffer.position(1); // skip head byte
                 return Md5Key.readFrom(buffer).equals(key);
             }
 
             public ItemIndex replace(Md5Key key, ItemIndex itemIndex) {
                 ItemIndex previous = itemIndex();
                 add(key, itemIndex);
                 return previous;
             }
 
             public ItemIndex itemIndex() {
                 buffer.position(1 + Md5Key.LENGTH); // skip head byte and key
                 return ItemIndex.readFrom(buffer);
             }
 
             public ItemIndex release() {
                 buffer.put(0, State.RELEASED.toByte());
                 return itemIndex();
             }
 
             enum State {
                 EMPTY, OCCUPIED, RELEASED;
 
                 public byte toByte() {
                     return (byte) this.ordinal();
                 }
 
                 public static State valueOf(byte b) {
                     if (EMPTY.toByte() == b) return EMPTY;
                     if (OCCUPIED.toByte() == b) return OCCUPIED;
                     if (RELEASED.toByte() == b) return RELEASED;
                     throw new IllegalStateException("Unknown slot state: " + b);
                 }
             }
         }
 
     }
 }
