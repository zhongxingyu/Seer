 package dk.diku.pcsd.assignment1.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
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
 	private List<SpaceIdent> emptyList = Collections
 			.synchronizedList(new LinkedList<SpaceIdent>());
 
 	// Mapping of keys to the respective parts of the MMF
 	private Map<KeyImpl, SpaceIdent> mappings = new Hashtable<KeyImpl, SpaceIdent>();
 
 	private IndexImpl() {
 		this.store = StoreImpl.getInstance();
 	}
 	
 	public static IndexImpl getInstance(){
 		if (instance==null){
 			instance = new IndexImpl();
 		}
 		return instance;
 	}
 
 	/*
 	 * Finds the location of an empty area in the MMF that has at least the
 	 * specified length. The first such area is returned, no matter how big it
 	 * is. If no such area is found, it returns a pointer to the end of the
 	 * currently used space.
 	 */
 	private SpaceIdent findFreeSpace(int length) {
 		SpaceIdent result = null;
 
 		// Search for empty areas in the emptyList
 		for (Iterator<SpaceIdent> i = emptyList.iterator(); i.hasNext();) {
 			SpaceIdent current = i.next();
 			if (current.getLength() >= length) {
 				return current;
 			}
 		}
 
 		result = new SpaceIdent(fileLength, length);
 		fileLength += length;
 
 		return result;
 	}
 
 	/*
 	 * Marks the given space as free, i.e. inserts it into the emptyList. The
 	 * space itself is not overwritten or deleted until it is used by another
 	 * value. Also checks if there are areas of free space right before and/or
 	 * after the specified area and, if this is the case, concatenates them to
 	 * one big free area to avoid fragmentation.
 	 */
 	private void freeSpace(SpaceIdent s) {
 		boolean done = false;
 
 		// look for adjacent areas of free space
 		for (Iterator<SpaceIdent> i = emptyList.iterator(); i.hasNext()
 				&& !done;) {
 			SpaceIdent current = i.next();
 
 			// look for free space right BEFORE the newly freed space
 			if (s.getPos() == (current.getPos() + current.getLength())) {
 				current.setLength(current.getLength() + s.getLength());
 				s = current;
 				done = true;
 			}
 
 			// look for free space right AFTER the newly freed space
 			if (current.getPos() == s.getPos() + s.getLength()) {
 				current.setPos(s.getPos());
 				current.setLength(current.getLength() + s.getLength());
 				s = current;
 				done = true;
 			}
 		}
 
 		// if the newly freed space could not be joined with another free area
 		// add it to the emptyList
 		if (!done)
 			emptyList.add(s);
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
 		if (mappings.containsKey(k)) {
 			throw new KeyAlreadyPresentException(k);
 		}
 
 		byte[] toWrite = vs.toByteArray(v);
 
 		SpaceIdent space = findFreeSpace(toWrite.length);
 
 		store.write(space.getPos(), toWrite);
 
 		// remove used space from emptyList
 		emptyList.remove(space);
 
 		// and add new empty area to list if necessary
 		int ldiff = space.getLength() - toWrite.length;
 
 		if (ldiff > 0)
 			emptyList
 					.add(new SpaceIdent(space.getPos() + toWrite.length, ldiff));
 
 		mappings.put(k, space);
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
 		SpaceIdent s = mappings.get(k);
 
 		if (s == null) {
 			throw new KeyNotFoundException(k);
 		} else {
 			// free the space
 			freeSpace(s);
 
 			// and remove the key from the mapping
 			mappings.remove(k);
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
 		SpaceIdent s = mappings.get(k);
 
 		if (s == null) {
 			throw new KeyNotFoundException(k);
 		} else {
 			byte[] read = store.read(s.getPos(), s.getLength());
 			return vs.fromByteArray(read);
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
 		SpaceIdent s = mappings.get(k);
 
 		if (s == null) {
 			throw new KeyNotFoundException(k);
 		} else {
 			byte[] toWrite = vs.toByteArray(v);
 
 			int ldiff = s.getLength() - toWrite.length;
 
 			// if the currently used space is not big enough for the new value
 			if (ldiff < 0) {
 				// free the current space
 				freeSpace(s);
 
 				// find new space
 				s = findFreeSpace(toWrite.length);
				emptyList.remove(s);
 
 				// and store the new value there
 				store.write(s.getPos(), toWrite);
 				mappings.put(k, s);
 			} else {
 				// or, if the new value fits into the space of the new one
 
 				// just write it into the current space
 				store.write(s.getPos(), toWrite);
 
 				// and mark any leftover space as free
 				if (ldiff > 0) {
 					freeSpace(new SpaceIdent(s.getPos() + toWrite.length, ldiff));
 				}
 				
 				// adjust the length value in the map
 				s.setLength(toWrite.length);
 			}
 		}
 
 	}
 
 	/*
 	 * Returns the values for all keys that are in the specified range.
 	 * The returned list ist NOT sorted.
 	 * Throws an exception if begin > end
 	 * (non-Javadoc)
 	 * @see dk.diku.pcsd.keyvaluebase.interfaces.Index#scan(dk.diku.pcsd.keyvaluebase.interfaces.Key, dk.diku.pcsd.keyvaluebase.interfaces.Key)
 	 */
 	public List<ValueListImpl> scan(KeyImpl begin, KeyImpl end)
 			throws BeginGreaterThanEndException, IOException {
 		if (begin.compareTo(end) > 0)
 			throw new BeginGreaterThanEndException(begin, end);
 		
 		Set<KeyImpl> keys = mappings.keySet();
 		
 		List<ValueListImpl> result = new ArrayList<ValueListImpl>();
 		
 		// TODO: it may be more efficient to sort the list first
 		// and only check for one property in each iteration
 		for (Iterator<KeyImpl> i = keys.iterator(); i.hasNext(); ){
 			KeyImpl current = i.next();
 			if (begin.compareTo(current) <= 0 && end.compareTo(current) >= 0){
 				try {
 					result.add(get(current));
 				} catch (KeyNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return result;
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
 
 }
