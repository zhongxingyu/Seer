 package org.ogreg.common.nio;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Detailed statistic utility for {@link BaseIndexedStore}s.
  * 
  * @author Gergely Kiss
  */
 public class BaseIndexedStoreStatsProvider {
 
 	/**
 	 * Returns detailed statistics about the indexed store.
 	 * <p>
 	 * The store must already be open.
 	 * </p>
 	 * 
 	 * @param store
 	 * @return
 	 * @throws IOException on storage error
 	 */
 	public static Stats getStats(BaseIndexedStore<?> store) throws IOException {
 		IndexEntries ie = store.getIndex();
 		NioSerializer<?> serializer = store.getSerializer();
 
 		IndexEntry[] entries = new IndexEntry[ie.getMaxKey() + 1];
 		long unusedEntryBytes = 0;
 		long unusedIndexBytes = 0;
 		int numEntries = 0;
 
 		for (int i = 0; i <= ie.getMaxKey(); i++) {
 			long pos = ie.get(i);
 
 			if (pos <= 0) {
 				unusedIndexBytes += 8;
 				continue;
 			}
 
 			int entryCapacity = serializer.sizeOf(store.getStorageChannel(), pos);
 			int entrySize = entryCapacity; // TODO impl specific size
 
 			IndexEntry e = new IndexEntry(pos, i, entrySize, entryCapacity);
 			entries[i] = e;
 			numEntries++;
 		}
 
 		unusedIndexBytes += (ie.getCapacity() - ie.getMaxKey()) * 8;
 
 		// Sorting the index entries by file position to detect "holes"
 		Arrays.sort(entries, new Comparator<IndexEntry>() {
 			@Override
 			public int compare(IndexEntry o1, IndexEntry o2) {
 				if (o1 == null) {
 					return 1;
 				}
 				if (o2 == null) {
 					return -1;
 				}
 				return o1.position < o2.position ? -1 : o1.position > o2.position ? +1 : 0;
 			}
 		});
 
 		List<IndexHole> holes = new ArrayList<IndexHole>();
 
 		long lastPos = ie.getOffset() + IndexEntries.ENTRY_OFFSET + ie.getCapacity() * 8;
 		for (int i = 0; i < numEntries; i++) {
 			IndexEntry e = entries[i];
 
 			// A hole is an unused space between two index entries - this is
 			// usually 0, but when growing an entry, the old entry may be
 			// left there
 			int holeSize = (int) (e.position - lastPos);
 
 			if (holeSize != 0) {
 				holes.add(new IndexHole(lastPos, holeSize));
 				unusedEntryBytes += holeSize;
 			}
 
 			lastPos = e.position + e.entryCapacity;
 		}
 
 		// It may be impossible that the end of the file is a hole, but
 		// we're prepared for that none the less
 		int holeSize = (int) (store.getStorageChannel().size() - lastPos);
 
 		if (holeSize != 0) {
 			holes.add(new IndexHole(lastPos, holeSize));
 			unusedEntryBytes += holeSize;
 		}
 
		return new Stats(holes, numEntries, ie.getCapacity(), unusedIndexBytes, unusedEntryBytes);
 	}
 
 	/**
 	 * Detailed index entry statistics.
 	 * 
 	 * @author Gergely Kiss
 	 */
 	public static class Stats {
 		public final List<IndexHole> holes;
 		public final int numEntries;
		public final int maxEntries;
 		public final long unusedIndexBytes;
 		public final long unusedEntryBytes;
 
		public Stats(List<IndexHole> holes, int numEntries, int maxEntries, long unusedIndexBytes,
 				long unusedEntryBytes) {
 			this.holes = holes;
 			this.numEntries = numEntries;
			this.maxEntries = maxEntries;
 			this.unusedIndexBytes = unusedIndexBytes;
 			this.unusedEntryBytes = unusedEntryBytes;
 		}
 	}
 
 	public static class IndexHole {
 		public final long position;
 		public final int size;
 
 		public IndexHole(long position, int size) {
 			this.position = position;
 			this.size = size;
 		}
 	}
 
 	static class IndexEntry {
 		public final long position;
 		public final int id;
 		public final int entrySize;
 		public final int entryCapacity;
 
 		public IndexEntry(long position, int id, int entrySize, int entryCapacity) {
 			this.position = position;
 			this.id = id;
 			this.entrySize = entrySize;
 			this.entryCapacity = entryCapacity;
 		}
 	}
 }
