 package org.ogreg.ase4j.file;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.nio.channels.FileChannel;
 import java.util.LinkedHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.ogreg.common.nio.BaseIndexedStore;
 import org.ogreg.common.nio.NioSerializer;
 import org.ogreg.common.nio.NioUtils;
 
 /**
  * File based, cached association block storage.
  * 
  * @author Gergely Kiss
  */
 class CachedBlockStore extends BaseIndexedStore<AssociationBlock> {
 	private static final byte[] MAGIC = new byte[] { 'A', 'S', '4', 'J' };
 
 	static final NioSerializer<AssociationBlock> Serializer = new AssociationBlockSerializer();
 
 	// The base capacity of a newly created association store
 	static int baseCapacity = 1024;
 
 	/** The in-memory cache of the association blocks. */
 	private LRUCache cache = new LRUCache();
 
 	/** The maximum number of cached blocks. */
 	private int maxCached = 1024;
 
 	/** The number of currently stored associations. */
 	private long associationCount = 0;
 
 	public CachedBlockStore() {
 		setSerializer(Serializer);
 	}
 
 	@Override
 	protected void writeHeader(FileChannel channel) throws IOException {
 		super.writeHeader(channel);
 
 		// Writing magic bytes
 		channel.write(ByteBuffer.wrap(MAGIC));
 
 		// Writing association count
 		NioUtils.writeLong(channel, associationCount);
 	}
 
 	@Override
 	protected void readHeader(FileChannel channel) throws IOException {
 		super.readHeader(channel);
 
 		// Reading magic bytes
 		channel.read(ByteBuffer.allocate(4));
 
 		// Reading association count
 		associationCount = NioUtils.readLong(channel);
 	}
 
 	/**
 	 * Merges all the associations to the store.
 	 * <p>
 	 * Grows and/or updates the current associations in the store.
 	 * </p>
 	 * 
 	 * @param assocs
 	 * @throws IOException in case of a storage failure
 	 */
 	public void merge(AssociationBlock assocs) throws IOException {
 		int from = assocs.from;
 
 		AssociationBlock oldAssocs = cache.get(from);
 
 		// Not found in cache
 		if (oldAssocs == null) {
 			oldAssocs = super.get(from);
 
 			// Not found in index, saving and caching, skipping merge
 			if (oldAssocs == null) {
 				super.add(from, assocs);
 				cache.put(from, assocs);
 				associationCount += assocs.size;
 
 				return;
 			}
 			// Found in index, caching
 			else {
 				cache.put(from, oldAssocs);
 			}
 		}
 
 		synchronized (oldAssocs) {
 			int oldSize = oldAssocs.size;
 			oldAssocs.merge(assocs);
 			associationCount += oldAssocs.size - oldSize;
 		}
 	}
 
 	/**
 	 * Returns the associations starting from <code>from</code>.
 	 * 
 	 * @param from
 	 * @return The asspciations row, or null if it was not found for the given
 	 *         <code>from</code>
 	 * @throws IOException in case of a storage failure
 	 */
 	@Override
 	public AssociationBlock get(int from) throws IOException {
 		AssociationBlock assocs = cache.get(from);
 
 		// Not found in cache
 		if (assocs == null) {
 			assocs = super.get(from);
 
 			if (assocs != null) {
 				cache.put(from, assocs);
 			}
 		}
 
 		return assocs;
 	}
 
 	/**
 	 * Returns the association strength between <code>from</code> and
 	 * <code>to</code>.
 	 * 
 	 * @param from
 	 * @param to
 	 * @return
 	 * @throws IOException in case of a storage failure
 	 */
 	public int get(int from, int to) throws IOException {
 		AssociationBlock assocs = get(from);
 
 		return (assocs == null) ? 0 : assocs.get(to);
 	}
 
 	public void setMaxCached(int maxCached) {
 		this.maxCached = maxCached;
 	}
 
 	@Override
 	protected void onBeforeFlush() throws IOException {
 
 		// Flushing the cache
 		for (AssociationBlock assocs : cache.values()) {
 			flush(assocs);
 		}
 	}
 
 	@Override
 	protected void onBeforeClose() {
 		cache.clear();
 	}
 
 	private void flush(AssociationBlock assocs) throws IOException {
 		update(assocs.from, assocs);
 	}
 
 	@Override
 	protected int getBaseCapacity() {
 		return baseCapacity;
 	}
 
 	long getAssociationCount() {
 		return associationCount;
 	}
 
 	LRUCache getCache() {
 		return cache;
 	}
 
 	// A simple LRU cache for association rows
 	class LRUCache extends LinkedHashMap<Integer, AssociationBlock> {
 		private static final long serialVersionUID = -5914565512322090452L;
 		private AtomicInteger cachedAssociationCount = new AtomicInteger(0);
 
 		@Override
 		public AssociationBlock put(Integer key, AssociationBlock value) {
 			AssociationBlock old = super.put(key, value);
 
 			if (old != null) {
 				cachedAssociationCount.addAndGet(-old.size);
 			}
 			cachedAssociationCount.addAndGet(value.size);
 
 			return old;
 		}
 
 		@Override
 		protected boolean removeEldestEntry(java.util.Map.Entry<Integer, AssociationBlock> eldest) {
 			boolean remove = size() > maxCached;
 
 			if (remove) {
 
 				try {
 					flush(eldest.getValue());
 				} catch (IOException e) {
 					throw new IllegalStateException("Failed to flush eldest cache entry", e);
 				}
 
				cachedAssociationCount.decrementAndGet();
 			}
 
 			return remove;
 		}
 
 		long getAssociationCount() {
 			return cachedAssociationCount.longValue();
 		}
 	}
 
 	// NIO Serializer for association blocks
 	private static class AssociationBlockSerializer implements NioSerializer<AssociationBlock> {
 
 		@Override
 		public void serialize(AssociationBlock value, ByteBuffer dest) {
 			IntBuffer iview = dest.asIntBuffer();
 
 			iview.put(value.capacity);
 			iview.put(value.size);
 			iview.put(value.from);
 			iview.put(value.tos);
 			iview.put(value.values);
 
 			// Resetting changedness
 			value.changed = false;
 			value.originalCapacity = value.capacity;
 		}
 
 		@Override
 		public AssociationBlock deserialize(ByteBuffer source) {
 			IntBuffer iview = source.asIntBuffer();
 
 			int capacity = iview.get();
 			int size = iview.get();
 			int from = iview.get();
 
 			AssociationBlock assocs = new AssociationBlock(capacity, size, from);
 
 			iview.get(assocs.tos);
 			iview.get(assocs.values);
 
 			assocs.changed = false;
 
 			return assocs;
 		}
 
 		@Override
 		public int sizeOf(AssociationBlock value) {
 			return sizeOf(value.capacity);
 		}
 
 		@Override
 		public int sizeOf(FileChannel channel, long pos) throws IOException {
 			return sizeOf(NioUtils.readInt(channel, pos));
 		}
 
 		private final int sizeOf(int capacity) {
 			// Size of an association:
 			// 4 + 4 + 4 + capacity * 4 + capacity * 4
 			// capacity + size + from + tos + values
 			return 12 + capacity * 8;
 		}
 	}
 }
