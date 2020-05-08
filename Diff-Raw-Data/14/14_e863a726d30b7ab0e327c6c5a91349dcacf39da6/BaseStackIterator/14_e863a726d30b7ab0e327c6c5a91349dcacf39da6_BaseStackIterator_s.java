 package org.remus.core;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.remus.JSON;
 
 abstract public class BaseStackIterator<T> implements Iterable<T>, Iterator<T> {
 	boolean hasMore = true, firstSlice = true, elemAdded;
 	int maxFetch = 100;
 	Integer maxCount = null;
 	LinkedList<T> outList;
 	String keyStart,  keyEnd;
 	protected boolean stop = false;
 
 	private BaseStackNode node;
 	private boolean loadVal;
 
 	public BaseStackIterator(BaseStackNode node, String keyStart, String keyEnd, boolean loadVal) {
 		this.node = node;
 		this.keyStart = keyStart;
		this.keyStart = keyEnd;
 		outList = new LinkedList<T>();
 		this.loadVal = loadVal;
 	}
 
 	@Override
 	public Iterator<T> iterator() {
 		return this;
 	}
 
 	@Override
 	public boolean hasNext() {		
 		if (stop) {
 			return false;
 		}
 		if (hasMore && outList.size() < maxFetch) {
 			hasMore = getNextSlice();
 		}
 		if (maxCount != null && maxCount <= 0) {
 			return false;
 		}
 		if (outList.size() <= 0) {
 			return false;
 		}
 		return true;
 	}
 
 	private boolean getNextSlice() {
 		String lastKey = null;
 		elemAdded = false;
 		if (loadVal) {
 			List<String> curlist = node.keySlice(keyStart, maxFetch);
			for (String key : curlist){
 				if (stop) {
 					return false;
 				}
 				if (firstSlice || key.compareTo(new String(keyStart)) != 0) {
 					for (String valJson : node.getValueJSON(key)) {
 						processKeyValue(key, JSON.loads(valJson));
 					}
 					lastKey = key;
 					elemAdded = true;
 				}
 			}
 		} else {
 			List<String> curlist = node.keySlice(keyStart, maxFetch);
 			for (String key : curlist) {
 				if (stop) {
 					return false;
 				}
 				if (firstSlice || key.compareTo(new String(keyStart)) != 0) {
 					processKeyValue(key, null);
 					lastKey = key;
 					elemAdded = true;
 				}
 			}
 		}
 		keyStart = lastKey;
 		firstSlice = false;
 
 		return elemAdded;
 	}
 
 	public abstract void processKeyValue(String key, Object val);
 
 	public void stop() {
 		stop = true;
 	}
 
 	public void addElement( T elem ) {
 		elemAdded = true;
 		outList.add( elem );
 	}
 	@Override
 	public T next() {
 		if ( maxCount != null ) {
 			maxCount--;
 		}
 		return outList.removeFirst();
 	}
 
 	@Override
 	public void remove() { }
 }
