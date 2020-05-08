 package spellcheck;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 public class HashTable implements Iterator<String> {
 
 	private String[] table;
 	
 	//Should this be a field?
 	private HashCode hashCode;
 	private float loadFactor;
 	private int load;
 	
 	private int probes;
 	
 	//for Iterator
 	private int curIndex;
 	
 	public HashTable() {
 				
 	}
 
 	public HashTable(int initialCapacity, float loadFactor, HashCode sH) {
 		
 		//Get the next prime for the capacity
 		int primeCap = findNextPrime(initialCapacity);
 		table = new String[primeCap];
 		
 		this.loadFactor = loadFactor;
 		
 		hashCode = sH;
 		
 		load = 0;
 	}
 
 	/**
 	 * Add a value to the HashMap
 	 * @param value
 	 */
 	public void insert(String value) {
 		int hashVal = hashCode.giveCode(value);
 		
 		int count = 0;
 		
 		
 		
		float curLoad = (float) load / (float) size();
 		
 		//Check the load factor, rehash if necessary
 		if (curLoad >= loadFactor) {
 			rehash();
 		}
 		
		boolean valid=false;
		
		
		int hash = Math.abs(compress(hashVal));

 		//Create a copy of the Compressed hash
 		int compressed = hash;
 		while (!valid) {
 			if (table[hash] != null) {
 				count++;
 				hash = compressAgain(hashVal, compressed, count);
 			} else {
 				valid=true;
 			}
 		}
 			
 		table[hash] = value;
 		load++;
 	}
 	
 	/**
 	 * Compresses a hash code once
 	 */
 	public int compress(int hash) {
 		
 		//a and b should be randomly selected on every re-hash
 		// to ensure a cannot be divided by N
 		int a = 241;
 		int b = 13;
 		
		int compress = Math.abs(((a * hash) + b) % size());
 		
		return compress;
 	}
 	
 	/**
 	 * Hash the code a second, third, nth time (specified by j)
 	 * @param k - the original, uncompressed value
 	 * @param hash - the first compressed hash
 	 * @param j
 	 * @return
 	 */
 	public int compressAgain(int k, int hash, int j) {
 		
 		probes++;
 		
 		//Half the size and find the next prime
 		int d = findNextPrime(size()/2);
 		
 		int rehash = d - (k % d);
 		
 		int compress =  (hash + (j*rehash)) % size(); 
 
		return Math.abs(compress);
 	}
 	
 	public void remove(String value) {
 		int hash = getDoubleHash(value);
 		
 		if (hash>-1) {
 			//Need to create a special value
 			table[hash] = null;
 			load--;
 		} else {
 			//throw null pointer exception
 		}
 	}
 	
 	/**
 	 * Find if a value exists or not in the table
 	 * 
 	 * @param value
 	 * @return 
 	 */
 	public boolean contains(String value) {
 		if (getDoubleHash(value) > -1) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Try to double hash the value
 	 * Return -1 if nothing was found
 	 * @return
 	 */
 	private int getDoubleHash(String value) {
 		int hashVal = hashCode.giveCode(value);
 		
 		int count=1;
 		boolean found = false;
 		
 		int hash = compress(hashVal);
 		int compressed = hash;

 		
		while (!found && count < size()) {
 			if (table[hash] == null){
 				//If the bucket is empty, the value isn't here, break loop
 				break;
 			} 
 			
 			if (!table[hash].equals(value)){
 				hash = compressAgain(hashVal, compressed, count);
 				count++;
 			} else {
 				found=true;
 			}
 		}
 		
 		if (found) {
 			return hash;
 		} else {
 			return -1;
 		}
 	}
 	
 	/**
 	 * Increase the size of the collection and re-order elements
 	 */
 	public void rehash() {
 		int prime = findNextPrime(size() * 2);
 		
 		//Create a copy of the old array
 		String[] oldTable = table;
 		
 		//Increase the size of the old table
 		table = new String[prime];
 		
 		for (int i=0; i<oldTable.length; i++) {
 			if (oldTable[i] != null) {
 				insert(oldTable[i]);
 			}
 		}
 	}
 	
 	public int size() {
 		return table.length;
 	}
 	
 	/**
 	 * Finds the next prime number after candidate
 	 * 
 	 * @param candidate
 	 * @return
 	 */
 	private int findNextPrime(int candidate) {
 		
 		int trial = 2;
 		boolean prime=false;
 		
 				
 		while (!prime) {
 			
 			
 			
 			prime = true;
 			
 			//Checks if candidate is divisible by trail^2
 			while (trial * trial <= candidate ) {
 				if (candidate % trial == 0) {
 					prime = false;
 				}
 				trial++;
 			}	
 			
 			
 			if (!prime) {
 				//Increase candidate number as the current in non-prime
 				candidate++;
 			}
 		}
 		
 		return candidate;
 	}
 
 	public boolean isEmpty() {
 		if (size() != 0 ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Check if there is a next value for the Iterator
 	 */
 	@Override
 	public boolean hasNext() {
 		int tempIndex = curIndex;
 		
 		//Skip over null values
 		while (table[tempIndex] == null) {
 			if (tempIndex + 1 < size()) {
 				
 				tempIndex++;
 			} else {
 				break; 
 			}
 		}
 		if (tempIndex + 1 < size()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * return the next element in the Iterator
 	 */
 	@Override
 	public String next() {
 		
 		while (table[curIndex] == null) {		
 			curIndex++;
 		}
 		
 		String el = table[curIndex];
 		curIndex++;
 		
 		return el;
 	}
 
 	@Override
 	public void remove() {
 		table[curIndex]	= null;	
 	}
 		
 	public int getProbes() {
 		return probes;
 	}
 }
