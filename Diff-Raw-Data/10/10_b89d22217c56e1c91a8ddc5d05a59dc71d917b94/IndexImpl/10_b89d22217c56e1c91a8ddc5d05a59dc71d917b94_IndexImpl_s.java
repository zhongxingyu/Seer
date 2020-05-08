 package assignmentImplementation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 import java.util.concurrent.ConcurrentSkipListMap;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
 
 import keyValueBaseExceptions.BeginGreaterThanEndException;
 import keyValueBaseExceptions.KeyAlreadyPresentException;
 import keyValueBaseExceptions.KeyNotFoundException;
 import keyValueBaseInterfaces.Index;
 import keyValueBaseInterfaces.Pair;
 import keyValueBaseInterfaces.Store;
 
 /**
  * IndexImpl implements a basic memory map over a StoreImpl object. It uses a
  * Reentrant locking scheme to enforce atomicity.
  * 
  * 
  * @author jens
  * 
  */
 public class IndexImpl implements Index<KeyImpl, ValueListImpl> {
 
 	/**
 	 * Private data structures
 	 * 
 	 * storage is the Store containing raw data addressed by position and offset
 	 * locationTable is the mapping of keys to spaces in memory freeSpaces is
 	 * the current set of available free space
 	 * 
 	 */
 	private Store storage;
 
 	// Index data structures:
 	private ConcurrentSkipListMap<KeyImpl, Space> allocatedSpaces;
 	private NavigableMap<Long, Long> freeSpaces;
 
 	// Lock manager for keys - see below:
 	private final KeyLockManager keyLockManager = new KeyLockManager();;
 
 	// locks for freeSpaces map
 	private ReentrantReadWriteLock freeSpacesLock = new ReentrantReadWriteLock(
 			true);
 	private ReadLock freeread = freeSpacesLock.readLock();
 	private WriteLock freewrite = freeSpacesLock.writeLock();
 
 	// Utilities:
 	private ValueSerializerImpl serializer = new ValueSerializerImpl();
 
 	/**
 	 * Constructor - creates storage and initialises data structures
 	 * 
 	 * @param storePath
 	 * @param size
 	 */
 	public IndexImpl(String storePath, long size) {
 
 		try {
 			storage = new StoreImpl(storePath, size);
 			allocatedSpaces = new ConcurrentSkipListMap<KeyImpl, Space>();
 			freeSpaces = new TreeMap<Long, Long>();
 			freeSpaces.put(0L, size);
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 
 		}
 
 	}
 
 	/**
 	 * Insert a key and value in the data store and update memory table
 	 */
 	@Override
 	public void insert(KeyImpl k, ValueListImpl v)
 			throws KeyAlreadyPresentException, IOException {
 
 		KeyLock lock = null;
 		try {
 			if (allocatedSpaces.containsKey(k))
 				throw new KeyAlreadyPresentException(k);
 			else {
 
 				// obtain lock
 
 				lock = keyLockManager.getLock(k);
 				lock.lock();
 				try {
 					// serialise valuelist to byte array
 					byte[] data = serializer.toByteArray(v);
 
 					// allocate space - this throws an IO exception if there is
 					// not
 					// enough free space
 					allocateSpace(k, data);
 				} finally {
 					// release lock
 					lock.unlock();
 				}
 
 			}
 		} finally {
 			if (lock != null) {
 				lock.releaseLock();
 			}
 		}
 
 	}
 
 	/**
 	 * Removes a key from the allocation table and marks the space as free
 	 */
 	@Override
 	public void remove(KeyImpl k) throws KeyNotFoundException {
 		KeyLock lock = null;
 		try {
 			lock = keyLockManager.getLock(k);
 			lock.lock();
 			try {
 				// find the allocation
 				if (allocatedSpaces.containsKey(k)) {
 
 					Space toDelete = allocatedSpaces.remove(k);
 
 					addFreeSpace(toDelete.getOffset(), toDelete.getSize());
 
 				} else
 					throw new KeyNotFoundException(k);
 			} finally {
 				lock.unlock();
 			}
 		} finally {
 			if (lock != null) {
 				lock.releaseLock();
 			}
 		}
 	}
 
 	@Override
 	/**
 	 * Returns the value list for the specified key
 	 * @param k key to lookup
 	 */
 	public ValueListImpl get(KeyImpl k) throws KeyNotFoundException,
 			IOException {
 
 		if (allocatedSpaces.containsKey(k)) {
 			KeyLock lock = null;
 
 			try {
 				lock = keyLockManager.getLock(k);
 				lock.lock();
 
 				try {
 					Space locationData = allocatedSpaces.get(k);
 					// this could throw IOException!
 					// nb. Spaces which are allocated have max size MAXINT
 					// Spaces which are free could have 64bit size
 					byte[] data = storage.read(locationData.getOffset(),
 							(int) locationData.getSize());
 
 					ValueListImpl vl = serializer.fromByteArray(data);
 					return vl;
 				} finally {
 					lock.unlock();
 				}
 			} finally {
 				if (lock != null)
 					lock.releaseLock();
 			}
 		}
 
 		else {
 			throw new KeyNotFoundException(k);
 		}
 
 	}
 
 	@Override
 	public void update(KeyImpl k, ValueListImpl v) throws KeyNotFoundException,
 			IOException {
 
 		if (allocatedSpaces.containsKey(k)) {
 
 			KeyLock lock = null;
 			try {
 				lock = keyLockManager.getLock(k);
 				lock.lock();
 
 				try {
 					// serialise the new value:
 					byte[] newValue = serializer.toByteArray(v);
 
 					// get the current location data:
 					Space locationData = allocatedSpaces.get(k);
 
 					// find the size of the new and the old value
 					int oldSize = (int) locationData.getSize();
 					int newSize = newValue.length;
 
 					// Handle size differences in the new and old value:
 					if (oldSize == newSize) {
 						// replace data in store since no change needed
 						storage.write(locationData.getOffset(), newValue);
 
 					} else if (oldSize > newSize) {
 						// If less memory is needed we can create a new free
 						// space.
 
 						try {
 							Space newSpace = new Space(newSize,
 									locationData.getOffset());
 
 							allocatedSpaces.replace(k, newSpace);
 
 							// Store data
 							storage.write(newSpace.getOffset(),
 									serializer.toByteArray(v));
 
 							// Create a new free space immediately after the new
 							// allocation
 							addFreeSpace(newSpace.getOffset() + newSize,
 									(long) oldSize - newSize);
 
 						} catch (Exception ex) {
 							// TODO what could go wrong?
 							ex.printStackTrace();
 						}
 
 					} else {
 						// If the new data is larger, a new free space is needed
 						freewrite.lock();
 						// Find a big enough space:
 						long newOffset = findSpace(newSize); // freeSpaces.higher(new
 																// Space(newSize));
 
 						if (newOffset >= 0) { // findSpace returns -1L if no
 												// space
 												// found
 
 							// take the freeSpace so no other operation can use
 							// it
 
 							long availableSize = freeSpaces.remove(newOffset);
 
 							// safe for other threads to access freeSpaces
 							freewrite.unlock();
 
 							Space newAllocationData = new Space(newSize,
 									newOffset);
 
 							// put the data address and write to store
 							allocatedSpaces.replace(k, newAllocationData);
 							storage.write(newOffset, newValue);
 
 							// Replace any free space that we are not using
 							addFreeSpace(newOffset + newSize, availableSize
 									- newSize);
 
 							// Set the previous storage space as a free space
 							addFreeSpace(locationData.getOffset(),
 									(long) oldSize);
 
 						} else {
 							throw new IOException(
 									"Could not find large enough space in memory");
 						}
 					}
 				} finally {
 					lock.unlock();
 				}
 
 			} finally {
 				if (lock != null)
 					lock.releaseLock();
 			}
 		} else {
 			throw new KeyNotFoundException(k);
 		}
 
 	}
 
 	@Override
 	public List<ValueListImpl> scan(KeyImpl begin, KeyImpl end)
 			throws BeginGreaterThanEndException, IOException {
 
 		if (end.compareTo(begin) == -1)
 			throw new BeginGreaterThanEndException(begin, end);
 		else {
 			List<ValueListImpl> values = new ArrayList<ValueListImpl>();
 
 			KeyLock lock = null;
 
 			// loop over a submap of the allocated Spaces:
 			for (KeyImpl k : allocatedSpaces.subMap(begin, true, end, true)
 					.keySet()) {
 				try {
 					lock = keyLockManager.getLock(k);
 					lock.lock();
 					try {
 						values.add(get(k));
 					} catch (KeyNotFoundException e) {
 						// Theoretically should not happen
 						// the keys have been selected based on the state of the
 						// Map
 					} finally {
 						lock.unlock();
 					}
 				} finally {
 					lock.releaseLock();
 				}
 			}
 			return values;
 
 		}
 
 	}
 
 	@Override
 	public List<ValueListImpl> atomicScan(KeyImpl begin, KeyImpl end)
 			throws BeginGreaterThanEndException, IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void bulkPut(List<Pair<KeyImpl, ValueListImpl>> keys)
 			throws IOException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/******************************************************************
 	 * Internal methods
 	 */
 
 	/**
 	 * 
 	 * Adding a free space can modify other parts of memory - hence locking
 	 * needed
 	 * 
 	 * @param size
 	 * @param offset
 	 */
 	private void addFreeSpace(long offset, long size) {
 		freewrite.lock();
 		try {
 			// note that if size 0 supplied there is no need to perform
 			// insertion!
 			if (size > 0) {
 
 				/*
 				 * Defragmentation logic: Searches for neighbouring free space
 				 * and merges
 				 */
 				boolean changeNeeded = false;
 				long currentOffset = offset;
 				long currentSize = size;
 				Long predecessor = freeSpaces.lowerKey(currentOffset);
 				Long successor = freeSpaces.higherKey(currentOffset);
 
 				if (predecessor != null) {
 					long predecessorSize = freeSpaces.get(predecessor);
 					// check if predecessor offset + size is the same as current
 					// freespace offset
 					if (predecessorSize + predecessor == currentOffset) {
 						currentOffset = predecessor;
 						currentSize += predecessorSize;
 						changeNeeded = true;
 						freeSpaces.remove(predecessor);
 					}
 				}
 				if (successor != null) {
 					// check if successor offset is the current offset + size
 					if (currentSize + currentOffset == successor) {
 						currentSize += freeSpaces.get(successor);
 						changeNeeded = true;
 						freeSpaces.remove(successor);
 
 					}
 				}
 
 				if (changeNeeded) {
 					// insert the new free Space if changed
 					freeSpaces.put(currentOffset, currentSize);
 				} else {
 					// no neighbours were found, just a simple put
 					freeSpaces.put(offset, size);
 				}
 			}
 		} finally {
 			freewrite.unlock();
 		}
 
 	}
 
 	/**
 	 * Allocate and write data:
 	 * 
 	 * This method deals with finding free space and inserting the space into
 	 * the allocation map
 	 * 
 	 * @param k
 	 *            the key to map
 	 * @param size
 	 *            the size of the memory to allocate
 	 * @return the memory offset to store the data
 	 * @throws KeyAlreadyPresentException
 	 * @throws IOException
 	 */
 	private void allocateSpace(KeyImpl k, byte[] data) throws IOException,
 			KeyAlreadyPresentException {
 
 		try {
 			// need to lock the freeSpaces map while allocating
 			freewrite.lock();
 
 			// find the free space that is smallest possible space to fit
 			// this new data:
 			long size = data.length;
 
 			long selectedOffset = findSpace(size);
 
 			if (selectedOffset >= 0) {
 
 				// remove the old free space
 				long freeSize = freeSpaces.remove(selectedOffset);
 
 				// release the freeSpaces lock as now no other thread can take
 				// the same space
 				freewrite.unlock();
 
 				// add free space after allocated memory
 				addFreeSpace(selectedOffset + size, freeSize - size);
 
 				// set the location and size for the data
 				Space toAllocate = new Space(data.length, selectedOffset);
 
 				// update allocation table atomically
 				Space inserted = allocatedSpaces.putIfAbsent(k, toAllocate);
 
 				if (inserted == null) {
 					// write the data now that nothing can 'steal' the free
 					// space
 					storage.write(selectedOffset, data);
 				} else {
 					// this should only happen if some locking has gone wrong
 
 					// restore the allocated space as a freespace:
 					addFreeSpace(selectedOffset, size);
 					// nb. this will be merged with the previously created space
 					throw new KeyAlreadyPresentException(k);
 				}
 
 			} else {
 				throw new IOException(
 						"Not enough free space to hold data for key " + k);
 			}
 
 		} finally {
 			// not used
 		}
 
 	}
 
 	/**
 	 * Find a space in memory to insert specified size TODO: improve by creating
 	 * an exception instead of invalid value
 	 * 
 	 * @param size
 	 * @return memory offset
 	 */
 	private long findSpace(long size) {
 		freeread.lock();
 		try {
 			for (long offset : freeSpaces.keySet()) {
 				if (freeSpaces.get(offset) >= size)
 					return offset;
 			}
 		} finally {
 			freeread.unlock();
 		}
 		// invalid offset if none found
 		return -1L;
 	}
 
 	/**
 	 * Space: a class to represent some area of memory It implements comparable
 	 * so that it can be ordered in a data structure This ordering is based on
 	 * the size (byte length) of the space.
 	 * 
 	 * It can be extended to add access time and extra locking properties.
 	 * 
 	 * @author jens
 	 * 
 	 */
 	private class Space implements Comparable<Space> {
 
 		protected Long size; // could occupy entire memory
 		protected Long offset;
 
 		/**
 		 * Construct a space with specified size and position
 		 * 
 		 * @param size
 		 *            Size
 		 * @param offset
 		 *            Offset
 		 */
 		public Space(int size, long offset) {
 
 			this.size = (long) size;
 			this.offset = offset;
 
 		}
 
 		/**
 		 * Comparison based on size of space
 		 */
 		@Override
 		public int compareTo(Space s) {
 
 			try {
 				return size.compareTo(s.size);
 			} finally {
 
 			}
 		}
 
 		/**
 		 * Getters and Setters for size and offset
 		 */
 
 		public long getSize() {
 
 			try {
 				return size;
 			} finally {
 
 			}
 		}
 
 		public void setSize(long size) {
 
 			try {
 				this.size = size;
 			} finally {
 
 			}
 		}
 
 		public long getOffset() {
 
 			try {
 				return this.offset;
 			} finally {
 
 			}
 
 		}
 
 		public void setOffset(Long offset) {
 
 			try {
 				this.offset = (long) offset;
 			} finally {
 
 			}
 		}
 
 	}
 

 	/**
 	 * Class to aid locking for specific keys
 	 * 
 	 * Tracks number of accessing threads and whether they key is being read
 	 * 
 	 * Partially based on accepted solution to
 	 * http://stackoverflow.com/questions
 	 * /2844744/locking-database-edit-by-key-name
 	 * 
 	 * @author jens
 	 * 
 	 */
 	private class KeyLock {
 		private final Lock l;
 		private final KeyImpl key;
 		private final KeyLockManager manager;
 
 		public KeyLock(KeyImpl k, Lock l, KeyLockManager klm) {
 			this.l = l;
 			this.key = k;
 			this.manager = klm;
 		}
 
 		public void lock() {
 			l.lock();
 		}
 
 		public void unlock() {
 			l.unlock();
 		}
 
 		public void releaseLock() {
 			manager.releaseLock(key);
 		}
 
 		@Override
 		protected void finalize() {
 			releaseLock();
 		}
 
 	}
 
 	/**
 	 * Key Lock manager maintains a map of keys to locks This allows Partially
 	 * based on accepted solution to
 	 * http://stackoverflow.com/questions/2844744/locking
 	 * -database-edit-by-key-name
 	 * 
 	 * @author jens
 	 * 
 	 */
 	private class KeyLockManager {
 		private class ManagedLock {
 			int count = 0;
 			final Lock l = new ReentrantLock();
 		}
 
 		private final TreeMap<KeyImpl, ManagedLock> keylocks = new TreeMap<KeyImpl, ManagedLock>();
 		private final Object mutex = new Object();
 
 		/**
 		 * 
 		 * @param k
 		 * @return
 		 */
 		public KeyLock getLock(KeyImpl k) {
 			synchronized (mutex) {
 				ManagedLock l = keylocks.get(k);
 				if (l == null) {
 					l = new ManagedLock();
 					keylocks.put(k, l);
 				}
 				l.count++;
 				return new KeyLock(k, l.l, this);
 			}
 		}
 
 		/**
 		 * Releases the lock for the specified key
 		 * 
 		 * @param k
 		 */
 		public void releaseLock(KeyImpl k) {
 			synchronized (mutex) {
 
 				ManagedLock l = keylocks.get(k);
				l.count--;
				if (l.count == 0)
					keylocks.remove(k);
 			}
 		}
 	}
 
 }
