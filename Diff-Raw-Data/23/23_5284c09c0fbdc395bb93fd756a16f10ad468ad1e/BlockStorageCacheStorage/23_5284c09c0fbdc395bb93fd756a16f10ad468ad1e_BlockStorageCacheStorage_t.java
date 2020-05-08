 package com.thimbleware.jmemcached.storage.bytebuffer;
 
 import com.thimbleware.jmemcached.Key;
 import com.thimbleware.jmemcached.LocalCacheElement;
 import com.thimbleware.jmemcached.storage.CacheStorage;
 import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
 import com.thimbleware.jmemcached.storage.hash.SizedItem;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * Implementation of the concurrent (linked) sized map using the block buffer storage back end.
  *
  * TODO Rather sub-optimal global locking strategy could be improved with a more intricate :dstriped locking implementation.
  */
 public final class BlockStorageCacheStorage implements CacheStorage<Key, LocalCacheElement> {
 
     ByteBufferBlockStore[] blockStorage;
     ReentrantReadWriteLock[] storageLock;
 
     final AtomicInteger ceilingBytes;
     final AtomicInteger maximumItems;
     final ConcurrentMap<Key, StoredValue> index;
     final long maximumSizeBytes;
 
 
     /**
      * Representation of the stored value, encoding its expiration, block region, and attached flags.
      * TODO investigate whether this can be collapsed into a subclass of LocalCacheElement instead?
      */
     final class StoredValue implements SizedItem {
         final int flags;
         final int expire;
         final long casUnique;
         final int[] buckets;
         final Region[] regions;
 
         StoredValue(int flags, int expire, long casUnique, int[] buckets, Region ... regions) {
             this.flags = flags;
             this.expire = expire;
             this.buckets = buckets;
             this.regions = regions;
             this.casUnique = casUnique;
         }
 
         public final LocalCacheElement toElement(Key key) {
             final LocalCacheElement element = new LocalCacheElement(key, flags, expire, casUnique);
             element.setData(getData());
             return element;
         }
 
         public final void free() {
             for (int i = 0; i < regions.length; i++) {
                 final int bucket = buckets[i];
 
                 blockStorage[bucket].free(regions[i]);
             }
         }
 
         public final ChannelBuffer getData() {
             ChannelBuffer buffers[] = new ChannelBuffer[regions.length];
             for (int i = 0; i < regions.length; i++) {
                 final int bucket = buckets[i];
                 buffers[i] = blockStorage[bucket].get(regions[i]);
             }
             return ChannelBuffers.wrappedBuffer(buffers);
         }
 
         public final int size() {
             int size = 0;
             for (Region region : regions) {
                 size+= region.size;
             }
             return size;
         }
     }
 
     public BlockStorageCacheStorage(int blockStoreBuckets, int ceilingBytesParam, int blockSizeBytes, long maximumSizeBytes, int maximumItemsVal, BlockStoreFactory factory) {
         this.blockStorage = new ByteBufferBlockStore[blockStoreBuckets];
         this.storageLock= new ReentrantReadWriteLock[blockStoreBuckets];
 
         long bucketSizeBytes = maximumSizeBytes / blockStoreBuckets;
         for (int i = 0; i < blockStoreBuckets; i++) {
             this.blockStorage[i] = factory.manufacture(bucketSizeBytes, blockSizeBytes);
             this.storageLock[i] = new ReentrantReadWriteLock();
         }
 
         this.ceilingBytes = new AtomicInteger(ceilingBytesParam);
         this.maximumItems = new AtomicInteger(maximumItemsVal);
         this.maximumSizeBytes = maximumSizeBytes;
 
//        this.index = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.LRU, maximumItemsVal, maximumSizeBytes, new ConcurrentLinkedHashMap.EvictionListener<Key, StoredValue>(){
//            public void onEviction(Key key, StoredValue value) {
//                value.free();
//            }
//        });

         this.index = new ConcurrentHashMap<Key, StoredValue>();
     }
 
     private int pickBucket(Key key, int partitionNum) {
         return new Random().nextInt(blockStorage.length);
 //        return Math.abs(key.hashCode() * partitionNum) % blockStorage.length;
     }
 
     public final long getMemoryCapacity() {
         long capacity = 0;
         for (ByteBufferBlockStore byteBufferBlockStore : blockStorage) {
             capacity += byteBufferBlockStore.getStoreSizeBytes();
         }
         return capacity;
     }
 
     public final long getMemoryUsed() {
         long memUsed = 0;
         for (ByteBufferBlockStore byteBufferBlockStore : blockStorage) {
             memUsed += (byteBufferBlockStore.getStoreSizeBytes() - byteBufferBlockStore.getFreeBytes());
         }
         return memUsed;
     }
 
     public final int capacity() {
         return maximumItems.get();
     }
 
     public final void close() throws IOException {
         // first clear all items
         clear();
 
         // then ask the block store to close
         for (ByteBufferBlockStore byteBufferBlockStore : blockStorage) {
             byteBufferBlockStore.close();
         }
         this.blockStorage = null;
         this.storageLock = null;
     }
 
     public final LocalCacheElement putIfAbsent(Key key, LocalCacheElement item) {
         // if the item already exists in the store, don't replace!
         StoredValue val = index.get(key);
 
         // present, return current value
         if (val != null) {
             return val.toElement(key);
         }
 
         put(key, item);
 
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public final boolean remove(Object key, Object value) {
         if (!(key instanceof Key) || (!(value instanceof LocalCacheElement))) return false;
         StoredValue val = index.get(key);
         LocalCacheElement el = val.toElement((Key) key);
 
         if (!el.equals(value)) {
             return false;
         } else {
             index.remove(key);
             val.free();
 
             return true;
         }
     }
 
     public final boolean replace(Key key, LocalCacheElement original, LocalCacheElement replace) {
         StoredValue val = index.get(key);
         LocalCacheElement el = val.toElement(key);
 
         if (!el.equals(original)) {
             return false;
         } else {
             // TODO not atomic; should share the write lock here
             remove(key);
             put(key, replace);
 
             return true;
         }
 
     }
 
     public final LocalCacheElement replace(Key key, LocalCacheElement replace) {
         StoredValue val = index.get(key);
         if (!index.containsKey(key)) {
             return null;
         } else {
             // TODO not atomic; should share the write lock here
             remove(key);
             put(key, replace);
 
             return replace;
         }
     }
 
     public final int size() {
         return index.size();
     }
 
     public final boolean isEmpty() {
         return index.isEmpty();
     }
 
     public final boolean containsKey(Object o) {
         return index.containsKey(o);
     }
 
     public final boolean containsValue(Object o) {
        throw new RuntimeException("operation not supported");
     }
 
     public final  LocalCacheElement get(Object key) {
         if (!(key instanceof Key)) return null;
         StoredValue val = index.get(key);
         if (val == null) return null;
         try {
             lockRead(val);
             return val.toElement((Key) key);
         } finally {
             unlockRead(val);
         }
     }
 
     public static int numBuckets(int size, int bucketSize) {
         int mod = size % bucketSize;
         int div = size / bucketSize;
         return mod == 0 ? div : div + 1;
     }
 
     public final LocalCacheElement put(final Key key, final LocalCacheElement item) {
         final int numBuckets = numBuckets(item.size(), (int) (this.maximumSizeBytes / this.blockStorage.length));
         final ChannelBuffer readBuffer = item.getData();
         final Region[] regions = new Region[numBuckets];
         final int buckets[] = new int[numBuckets];
 
         for (int i = 0; i < numBuckets; i++) {
             final int bucket = pickBucket(key, i);
             buckets[i] = bucket;
             storageLock[bucket].writeLock().lock();
         }
         for (int i = 0; i < numBuckets; i++) {
             final int bucket = buckets[i];
             final int fragmentSize = (i < numBuckets - 1) ? item.size() / numBuckets : readBuffer.readableBytes();
             regions[i] = blockStorage[bucket].alloc(fragmentSize, readBuffer);
         }
         for (final int bucket : buckets) {
             storageLock[bucket].writeLock().unlock();
         }
 
         final StoredValue old = index.put(key, new StoredValue(item.getFlags(), item.getExpire(), item.getCasUnique(), buckets, regions));
         if (old != null) { old.free(); }
 
         return null;
     }
 
     public final LocalCacheElement remove(Object key) {
         if (!(key instanceof Key)) return null;
         StoredValue val = index.get(key);
         if (val != null) {
             LocalCacheElement el = val.toElement((Key) key);
             lockWrite(val);
             val.free();
             unlockWrite(val);
             el.setData(val.getData());
 
             index.remove(key);
 
             return el;
         } else
             return null;
     }
 
     public final void putAll(Map<? extends Key, ? extends LocalCacheElement> map) {
         // absent, lock the store and put the new value in
         for (Entry<? extends Key, ? extends LocalCacheElement> entry : map.entrySet()) {
             Key key = entry.getKey();
             LocalCacheElement item;
             item = entry.getValue();
             put(key, item);
         }
     }
 
 
     public final void clear() {
         lockWriteAll();
         index.clear();
         for (ByteBufferBlockStore aBlockStorage : blockStorage) {
             aBlockStorage.clear();
         }
         unlockWriteAll();
     }
 
     public void lockReadAll() {
         for (ReentrantReadWriteLock reentrantReadWriteLock : storageLock) {
             reentrantReadWriteLock.readLock().lock();
         }
     }
 
     public void unlockReadAll() {
         for (ReentrantReadWriteLock reentrantReadWriteLock : storageLock) {
             reentrantReadWriteLock.readLock().unlock();
         }
     }
 
 
     public void lockWriteAll() {
         for (ReentrantReadWriteLock reentrantReadWriteLock : storageLock) {
             reentrantReadWriteLock.writeLock().lock();
         }
     }
 
     public void unlockWriteAll() {
         for (ReentrantReadWriteLock reentrantReadWriteLock : storageLock) {
             reentrantReadWriteLock.writeLock().unlock();
         }
     }
 
     public void lockRead(StoredValue value) {
         for (int bucket : value.buckets) {
             this.storageLock[bucket].readLock().lock();
         }
     }
 
     public void unlockRead(StoredValue value) {
         for (int bucket : value.buckets) {
             this.storageLock[bucket].readLock().unlock();
         }
     }
 
     public void lockWrite(StoredValue value) {
         for (int bucket : value.buckets) {
             this.storageLock[bucket].writeLock().lock();
         }
     }
 
     public void unlockWrite(StoredValue value) {
         for (int bucket : value.buckets) {
             this.storageLock[bucket].writeLock().unlock();
         }
     }
 
     public Set<Key> keySet() {
         return index.keySet();
     }
 
     public Collection<LocalCacheElement> values() {
        throw new RuntimeException("operation not supported");
     }
 
     public Set<Entry<Key, LocalCacheElement>> entrySet() {
        throw new RuntimeException("operation not supported");
     }
 }
