 package AssocArray;
 
 
 import java.util.*;
 
 
 /**
  * TODO: Allow adding of Collections/Arrays/HashMaps
  * TODO: Add Method in Interface for adding at a specific position
  * 
  * 
  * 
  * This class extends the AssociativeListInterface. It provides functionality 
  * for an array to also be associative by String index Keys.
  * 
  * This Associative List is dynamically increasing the length of the base array
  * it is created on. 
  * 
  * @author Al Petersen
  *
  * @param <E>
  */
 public class AssocArray<E> implements AssocArrayInterface<E>, Iterable<E> {
 	
 	private String[] keys;
 	private E[] values;
 	private int numItems;
 	
 	private static final int INIT_SIZE = 10;
 	
 	/**
 	 * Create an Associative List with a default size of 10
 	 */
 	@SuppressWarnings("unchecked")
 	public AssocArray() {
 		this.keys = new String[INIT_SIZE];
 		this.values = (E[])(new Object[INIT_SIZE]);
 		this.numItems = 0;
 		
 	}
 	
 	/**
 	 * Create an Associative List with an initial size
 	 * @param size
 	 */
 	@SuppressWarnings("unchecked")
 	public AssocArray(int size) {
 		this.keys = new String[size];
 		this.values = (E[])(new Object[size]);
 		this.numItems = 0;
 	}
 
 	/**
 	 * Add to end of array and resize array based on number of elements.
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean add(String key, E item) {
 		// add a new element to the end of the values array
 		// increment numItems by one
 		// add string index to the end of the keys array
 		String[] tempS = new String[this.numItems + 1];
 		E[] tempO = (E[])(new Object[this.numItems + 1]);
 		
 		for (int i = 0; i < keys.length; i++) {
 			if (this.keys[i] != null) {
 				tempS[i] = this.keys[i];
 			}
 			
 			if (this.values[i] != null) {
 				tempO[i] = this.values[i];
 			}
 		}
 		
 		//only add new key/item if key hasn't been used already
 		boolean contains = false;
 		for (int i = 0; i < this.keys.length; i++) {
 			if (keys[i] == key) {
 				contains = true;
 			}
 		}
 		if (!contains) {
 			tempS[tempS.length - 1] = key;
 			tempO[tempO.length - 1] = item;
 
 			//the original array references are now pointing to the newly created
 			// temp array memory. 
 			this.keys = tempS;
 			this.values = tempO;
 			
 			this.numItems += 1;
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean add(String key, E item, int position) {
 		boolean contains = false;
 		for (int i = 0; i < this.keys.length; i++) {
 			if (keys[i] == key) {
 				contains = true;
 			}
 		}
 
 		if (!contains) {
 
 			// need to copy array up until the given position, then add the new item, then copy the rest of the array
 			//copy original arrays first into new arrays with size+1;
 			String[] tempS = new String[this.numItems + 1];
 			E[] tempO = (E[])(new Object[this.numItems + 1]);
 			
 			for (int i = 0; i < position; i++) {
 				if (this.keys[i] != null) {
 					tempS[i] = this.keys[i];
 				}
 				
 				if (this.values[i] != null) {
 					tempO[i] = this.values[i];
 				}
 			}
 
 			tempS[position] = key;
 			tempO[position] = item;
 
 			for (int i = position; i < this.keys.length; i++) {
 				tempS[i+1] = keys[i];
 				tempO[i+1] = values[i];
 			}
 
 			this.keys = tempS;
 			this.values = tempO;
 
 			this.numItems += 1;
 
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	public E first() {
 		return this.values[0];
 	}
 
 	public E last() {
 		return this.values[this.values.length-1];
 	}
 
 	public E get(String key) throws NullPointerException {
 		for(int i = 0; i < this.keys.length; i++) {
 			if (this.keys[i] == key) {
 				return this.values[i];
 			}
 		}
 		throw new NullPointerException();
 	}
 	
 	public E get(int index) throws IndexOutOfBoundsException {
 		if(index < numItems) {
 			return this.values[index];
 		}
 		else {
 			throw new IndexOutOfBoundsException();
 		}
 	}
 
 	public boolean isEmpty() {
 		return this.numItems == 0;
 	}
 
 	public int length() {
 		return this.numItems;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean remove(String key) throws NullPointerException {
 		int index = -1;
 		for(int i = 0; i < this.keys.length; i++) {
 			if (this.keys[i] == key) {
 				index = i;
 			}
 		}
 		
 		if (index != -1) {
 			String[] tempS = new String[numItems - 1];
 			E[] tempE = (E[])new Object[numItems - 1];
 		
 			for (int i = 0; i < index; i++) {
 				tempS[i] = this.keys[i];
 				tempE[i] = this.values[i];
 			}
 			
 			for (int i = index; i <= this.keys.length - 2; i++) {
 				tempS[i] = this.keys[i+1];
 				tempE[i] = this.values[i+1];
 			}
 			
 			this.keys = tempS;
 			this.values = tempE;
 			
 			this.numItems -= 1;
 			
 			return true;
 		}
 		
 		throw new NullPointerException();
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean remove(int index) throws IndexOutOfBoundsException {
 		if (index < this.values.length && index >= 0) {
 			String[] tempS = new String[numItems-1];
 			E[] tempE = (E[])new Object[numItems-1];
 		
 			for (int i = 0; i < index; i++) {
 				tempS[i] = this.keys[i];
 				tempE[i] = this.values[i];
 			}
 			
 			for (int i = index; i <= this.keys.length - 2; i++) {
 				tempS[i] = this.keys[i+1];
 				tempE[i] = this.values[i+1];
 			}
 			
 			this.keys = tempS;
 			this.values = tempE;
 			
 			this.numItems -= 1;
 			
 			return true;
 		}
 		throw new IndexOutOfBoundsException();
 	}
 
 	/**
 	 * Return a new iterator for this list
 	 */
 	public Iterator<E> iterator() {
 		return new AssocArrayIterator<E>(this);
 	}
 
 	/**
 	 * Remove an element with the given key and return the value
 	 */
 	@SuppressWarnings("unchecked")
 	public E getRemove(String key) throws NullPointerException {
 		int index = -1;
 		E rTemp = null;
 		for(int i = 0; i < this.keys.length; i++) {
 			if (this.keys[i] == key) {
 				index = i;
 				rTemp = this.values[i];
 			}
 		}
 		
 		if (index != -1) {
 			String[] tempS = new String[numItems - 1];
 			E[] tempE = (E[])new Object[numItems - 1];
 		
 			for (int i = 0; i < index; i++) {
 				tempS[i] = this.keys[i];
 				tempE[i] = this.values[i];
 			}
 			
 			for (int i = index; i <= this.keys.length - 2; i++) {
 				tempS[i] = this.keys[i+1];
 				tempE[i] = this.values[i+1];
 			}
 			
 			this.keys = tempS;
 			this.values = tempE;
 			
 			this.numItems -= 1;
 			
 			return rTemp;
 		} else {
 			throw new NullPointerException();
 		}
 		
 		
 	}
 
 	/**
 	 * Remove the element with the given index and return the value
 	 */
 	@SuppressWarnings("unchecked")
 	public E getRemove(int index)  throws IndexOutOfBoundsException {
		if (index <= this.values.length && index >= 0) {
 			String[] tempS = new String[numItems-1];
 			E[] tempE = (E[])new Object[numItems-1];
 			
 			E rTemp = values[index];
 		
 			for (int i = 0; i < index; i++) {
 				tempS[i] = this.keys[i];
 				tempE[i] = this.values[i];
 			}
 			
 			for (int i = index; i <= this.keys.length - 2; i++) {
 				tempS[i] = this.keys[i+1];
 				tempE[i] = this.values[i+1];
 			}
 			
 			this.keys = tempS;
 			this.values = tempE;
 			
 			this.numItems -= 1;
 			
 			return rTemp;
 		}
 		else {
 			throw new IndexOutOfBoundsException();
 		}
 		
 	}
 
 	/**
 	 * Returns a HashMap of the AssocArray
 	 */
 	public HashMap<String, E> export() {
 		HashMap<String, E> map = new HashMap<String, E>();
 		
 		for (int i = 0; i < this.length(); i++) {
 			map.put(this.keys[i], this.values[i]);
 		}
 		
 		return map;
 	}
 
 
 }
