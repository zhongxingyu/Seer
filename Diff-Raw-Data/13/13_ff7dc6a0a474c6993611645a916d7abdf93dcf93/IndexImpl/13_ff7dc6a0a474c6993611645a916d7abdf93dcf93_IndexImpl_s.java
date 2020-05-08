 package dk.diku.pcsd.assignment1.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import dk.diku.pcsd.keyvaluebase.exceptions.BeginGreaterThanEndException;
 import dk.diku.pcsd.keyvaluebase.exceptions.KeyAlreadyPresentException;
 import dk.diku.pcsd.keyvaluebase.exceptions.KeyNotFoundException;
 import dk.diku.pcsd.keyvaluebase.interfaces.Index;
 import dk.diku.pcsd.keyvaluebase.interfaces.Pair;
 
 public class IndexImpl implements Index<KeyImpl, ValueListImpl> {
 
 	private static IndexImpl instance;
 
 	private StoreImpl store;
 
 	private ValueSerializerImpl vs = new ValueSerializerImpl();
 
 	private int fileLength = 0;
 
 	// List of empty parts in the MMF
 	// private List<SpaceIdent> emptyList = Collections
 	// .synchronizedList(new LinkedList<SpaceIdent>());
 	private SortedSet<SpaceIdent> emptyList = Collections
 			.synchronizedSortedSet(new TreeSet<SpaceIdent>());
 
 	// Mapping of keys to the respective parts of the MMF
 	private Map<KeyImpl, SpaceIdent> mappings = new Hashtable<KeyImpl, SpaceIdent>();
 
 	// Lock on the mappings table
 	private final ReadWriteLock mappingsLock;
 
 	private IndexImpl() {
 		this.store = StoreImpl.getInstance();
 		mappingsLock = new ReentrantReadWriteLock(true);
 	}
 
 	public synchronized static IndexImpl getInstance() {
 		if (instance == null) {
 			instance = new IndexImpl();
 		}
 		return instance;
 	}
 
 	/*
 	 * Finds the location of an empty area in the MMF that has at least the
 	 * specified length. The first such area is returned, no matter how big it
 	 * is. If no such area is found, it returns a pointer to the end of the
 	 * currently used space. Removes the found space from the emptyList.
 	 */
 	private SpaceIdent findFreeSpace(int length) {
 		synchronized (emptyList) {
 			SpaceIdent previous = null;
 			SpaceIdent result = null;
 
 			for (Iterator<SpaceIdent> i = emptyList.iterator(); i.hasNext();) {
 				SpaceIdent current = i.next();
 
 				if (previous != null && previous.getNext() == current.getPos()) {
 					i.remove();
 					previous.setLength(previous.getLength()
 							+ current.getLength());
 					current = previous;
 				}
 
 				if (current.getLength() >= length) {
 					result = current;
 					break;
 				}
 
 				previous = current;
 			}
 
 			if (result != null) {
 				emptyList.remove(result);
 				return result;
 			}
 
 			result = new SpaceIdent(fileLength, length);
 			fileLength += length;
 
 			return result;
 		}
 	}
 
 	/*
 	 * Marks the given space as free, i.e. inserts it into the emptyList. The
 	 * space itself is not overwritten or deleted until it is used by another
 	 * value. Also checks if there are areas of free space right before and/or
 	 * after the specified area and, if this is the case, concatenates them to
 	 * one big free area to avoid fragmentation.
 	 */
 	private void freeSpace(SpaceIdent s) {
 		synchronized (emptyList) {
 			emptyList.add(s);
 		}
 	}
 
 	/*
 	 * Inserts a new value in the list. Throws an exception if the specified key
 	 * already exists in the store. (non-Javadoc)
 	 * 
 	 * @see
 	 * dk.diku.pcsd.keyvaluebase.interfaces.Index#insert(dk.diku.pcsd.keyvaluebase
 	 * .interfaces.Key, dk.diku.pcsd.keyvaluebase.interfaces.Value)
 	 */
 	public void insert(KeyImpl k, ValueListImpl v)
 			throws KeyAlreadyPresentException, IOException {
 		Lock writeLock = k.getWriteLock();
 		writeLock.lock();
 		try {
 
 			mappingsLock.readLock().lock();
 			boolean containsKey;
 			try {
 				containsKey = mappings.containsKey(k);
 			} finally {
 				mappingsLock.readLock().unlock();
 			}
 			if (containsKey)
 				throw new KeyAlreadyPresentException(k);
 
 			byte[] toWrite = vs.toByteArray(v);
 
 			SpaceIdent space = findFreeSpace(toWrite.length);
 
 			store.write(space.getPos(), toWrite);
 
			// and add new empty area to list if necessary
			int ldiff = space.getLength() - toWrite.length;

			if (ldiff > 0)
				emptyList.add(new SpaceIdent(space.getPos() + toWrite.length,
						ldiff));
 
 			mappingsLock.writeLock().lock();
 			try {
 				mappings.put(k, space);
 			} finally {
 				mappingsLock.writeLock().unlock();
 			}
 		} finally {
 			writeLock.unlock();
 		}
 	}
 
 	/*
 	 * Removes a key-value-pair from the store. Throws an exception if the
 	 * specified key does not exist. (non-Javadoc)
 	 * 
 	 * @see
 	 * dk.diku.pcsd.keyvaluebase.interfaces.Index#remove(dk.diku.pcsd.keyvaluebase
 	 * .interfaces.Key)
 	 */
 	public void remove(KeyImpl k) throws KeyNotFoundException {
 		Lock writeLock = k.getWriteLock();
 		writeLock.lock();
 		try {
 			mappingsLock.readLock().lock();
 			SpaceIdent s;
 			try {
 				s = mappings.get(k);
 			} finally {
 				mappingsLock.readLock().unlock();
 			}
 
 			if (s == null) {
 				throw new KeyNotFoundException(k);
 			} else {
 				// free the space
 				freeSpace(s);
 
 				// and remove the key from the mapping
 				mappingsLock.writeLock().lock();
 				mappings.remove(k);
 				mappingsLock.writeLock().unlock();
 			}
 			k.removeLock();
 		} finally {
 			writeLock.unlock();
 		}
 	}
 
 	/*
 	 * Gets the value associated with a given key from the store. Throws an
 	 * exception if the specified key does not exist. (non-Javadoc)
 	 * 
 	 * @see
 	 * dk.diku.pcsd.keyvaluebase.interfaces.Index#get(dk.diku.pcsd.keyvaluebase
 	 * .interfaces.Key)
 	 */
 	public ValueListImpl get(KeyImpl k) throws KeyNotFoundException,
 			IOException {
 		Lock readLock = k.getReadLock();
 		readLock.lock();
 		try {
 			mappingsLock.readLock().lock();
 			SpaceIdent s;
 			try {
 				s = mappings.get(k);
 			} finally {
 				mappingsLock.readLock().unlock();
 			}
 
 			if (s == null) {
 				throw new KeyNotFoundException(k);
 			} else {
 				byte[] read = store.read(s.getPos(), s.getLength());
 				return vs.fromByteArray(read);
 			}
 		} finally {
 			readLock.unlock();
 		}
 	}
 
 	/*
 	 * Updates the value associated with a given key. Throws an exception if the
 	 * specified key does not exist. (non-Javadoc)
 	 * 
 	 * @see
 	 * dk.diku.pcsd.keyvaluebase.interfaces.Index#update(dk.diku.pcsd.keyvaluebase
 	 * .interfaces.Key, dk.diku.pcsd.keyvaluebase.interfaces.Value)
 	 */
 	public void update(KeyImpl k, ValueListImpl v) throws KeyNotFoundException,
 			IOException {
 		Lock writeLock = k.getWriteLock();
 		writeLock.lock();
 		try {
 			mappingsLock.readLock().lock();
 			SpaceIdent s;
 			try {
 				s = mappings.get(k);
 			} finally {
 				mappingsLock.readLock().unlock();
 			}
 
 			if (s == null) {
 				throw new KeyNotFoundException(k);
 			} else {
 				byte[] toWrite = vs.toByteArray(v);
 
 				int ldiff = s.getLength() - toWrite.length;
 
 				// if the currently used space is not big enough for the new
 				// value
 				if (ldiff < 0) {
 					// free the current space
 					freeSpace(s);
 
 					// find new space
 					s = findFreeSpace(toWrite.length);
 
 					// and store the new value there
 					store.write(s.getPos(), toWrite);
 					mappingsLock.writeLock().lock();
 					try {
 						mappings.put(k, s);
 					} finally {
 						mappingsLock.writeLock().unlock();
 					}
 				} else {
 					// or, if the new value fits into the space of the new one
 
 					// just write it into the current space
 					store.write(s.getPos(), toWrite);
 
 					// and mark any leftover space as free
 					if (ldiff > 0) {
 						freeSpace(new SpaceIdent(s.getPos() + toWrite.length,
 								ldiff));
 					}
 
 					// adjust the length value in the map
 					mappingsLock.writeLock().lock();
 					try {
 						mappings.put(k, new SpaceIdent(s.getPos(),
 								toWrite.length));
 					} finally {
 						mappingsLock.writeLock().unlock();
 					}
 				}
 			}
 		} finally {
 			writeLock.unlock();
 		}
 	}
 
 	/*
 	 * Returns the values for all keys that are in the specified range. The
 	 * returned list ist NOT sorted. Throws an exception if begin > end
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * dk.diku.pcsd.keyvaluebase.interfaces.Index#scan(dk.diku.pcsd.keyvaluebase
 	 * .interfaces.Key, dk.diku.pcsd.keyvaluebase.interfaces.Key)
 	 */
 	public List<ValueListImpl> scan(KeyImpl begin, KeyImpl end)
 			throws BeginGreaterThanEndException, IOException {
 		if (begin.compareTo(end) > 0)
 			throw new BeginGreaterThanEndException(begin, end);
 
 		mappingsLock.readLock().lock();
 		ConcurrentSkipListSet<KeyImpl> keys;
 		try {
 			keys = new ConcurrentSkipListSet<KeyImpl>(mappings.keySet());
 		} finally {
 			mappingsLock.readLock().unlock();
 		}
 
 		List<ValueListImpl> result = new ArrayList<ValueListImpl>();
 
 		// TODO: it may be more efficient to sort the list first
 		// and only check for one property in each iteration
 		for (Iterator<KeyImpl> i = keys.iterator(); i.hasNext();) {
 			KeyImpl current = i.next();
 			if (begin.compareTo(current) <= 0 && end.compareTo(current) >= 0) {
 				try {
 					result.add(get(current));
 				} catch (KeyNotFoundException e) {
 					// scan is not atomic so this may happen - just carry on
 				}
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public List<ValueListImpl> atomicScan(KeyImpl begin, KeyImpl end)
 			throws BeginGreaterThanEndException, IOException {
 		if (begin.compareTo(end) > 0)
 			throw new BeginGreaterThanEndException(begin, end);
 
 		mappingsLock.readLock().lock();
 		try {
 			SortedSet<KeyImpl> keys = new TreeSet<KeyImpl>(mappings.keySet());
 
 			for (KeyImpl ki : keys) {
 				ki.getReadLock().lock();
 			}
 			try {
 				List<ValueListImpl> result = new ArrayList<ValueListImpl>();
 
 				for (Iterator<KeyImpl> i = keys.iterator(); i.hasNext();) {
 					KeyImpl current = i.next();
 					if (begin.compareTo(current) <= 0
 							&& end.compareTo(current) >= 0) {
 						try {
 							result.add(get(current));
 						} catch (KeyNotFoundException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 				return result;
 			} finally {
 				for (KeyImpl ki : keys) {
 					ki.getReadLock().unlock();
 				}
 			}
 		} finally {
 			mappingsLock.readLock().unlock();
 		}
 	}
 
 	/*
 	 * TODO: writeLock throughout is probably not necessary, but just a readLock
 	 * throughout causes a deadlock and i think we need some kind of lock
 	 * throughout. yes we do. (non-Javadoc)
 	 * 
 	 * @see dk.diku.pcsd.keyvaluebase.interfaces.Index#bulkPut(java.util.List)
 	 */
 	public void bulkPut(List<Pair<KeyImpl, ValueListImpl>> newPairs)
 			throws IOException {
 
 		SortedSet<Pair<KeyImpl, ValueListImpl>> pairs = new TreeSet<Pair<KeyImpl, ValueListImpl>>(
 				newPairs);
 
 		for (Pair<KeyImpl, ValueListImpl> p : pairs) {
 			p.getKey().getWriteLock().lock();
 		}
 		try {
 			for (Pair<KeyImpl, ValueListImpl> p : newPairs) {
 				try{
 					boolean containsKey = mappings.containsKey(p.getKey());
 
 					if (containsKey) 
 						update(p.getKey(), p.getValue());
 					else 
 						insert(p.getKey(), p.getValue());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} finally {
 			for (Pair<KeyImpl, ValueListImpl> p : pairs) {
 				p.getKey().getWriteLock().unlock();
 			}
 		}
 
 	}
 
 }
