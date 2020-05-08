 package AssocArray;
 
 
 import java.util.Iterator;
 
 
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
 
 	public E first() throws IndexOutOfBoundsException {
 		return this.values[0];
 	}
 
 	public E last() throws IndexOutOfBoundsException {
 		return this.values[this.values.length-1];
 	}
 
	public E get(String key) throws NullPointerException {
 		for(int i = 0; i < this.keys.length; i++) {
 			if (this.keys[i] == key) {
 				return this.values[i];
 			}
 		}
 		return null;
 	}
 	
 	public E get(int index) throws IndexOutOfBoundsException {
 		return this.values[index];
 	}
 
 	public boolean isEmpty() {
 		return this.numItems == 0;
 	}
 
 	public int lenght() {
 		return this.numItems;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean remove(String key) throws IndexOutOfBoundsException {
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
 		
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean remove(int index) throws IndexOutOfBoundsException {
 		if (index <= this.values.length && index >= 0) {
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
 		return false;
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
 	public E getRemove(String key) throws IndexOutOfBoundsException {
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
 			throw new IndexOutOfBoundsException();
 		}
 		
 		
 	}
 
 	/**
 	 * Remove the element with the given index and return the value
 	 */
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
 }
