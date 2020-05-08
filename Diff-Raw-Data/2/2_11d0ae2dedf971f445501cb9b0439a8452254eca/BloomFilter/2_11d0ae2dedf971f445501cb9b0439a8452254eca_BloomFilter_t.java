 package com.raltamirano.bloomfilter.core;
 
 import java.util.BitSet;
 
 /**
  * Bloom Filter implementation
  */
 public final class BloomFilter {
 
 	private final int filterLength;
 	private final BitSet filterBitSet;
 	private final HashFunction[] hashFunctions;
 
 	/**
 	 * Creates a Bloom filter with a pre-defined set of hash functions: 
 	 * FNV, Murmur and Jenkins hash functions are used.
 	 * @param length In Bloom filters theory, the "m" parameter.
 	 */
 	public BloomFilter(int length) {		
 		this(length, new HashFunction[] 
 				{
 					com.raltamirano.bloomfilter.hash.FNVHash.getInstance(),
 					com.raltamirano.bloomfilter.hash.MurmurHash.getInstance(),
 					com.raltamirano.bloomfilter.hash.JenkinsHash.getInstance()
 				});
 	}
 
 	/**
 	 * Creates a Bloom filters with a user-provided set of hash functions
 	 * @param length In Bloom filters theory, the "m" parameter.
 	 * @param hashFunctions The hash functions used to compute indexes for this filter. 
	 * The size of this array corresponds to the "k" parameter in Bloom filters theory.
 	 */
 	public BloomFilter(int length, HashFunction[] hashFunctions) {
 
 		if (length <= 0) {
 			throw new RuntimeException("Invalid bloom filter bit length: "+ length);
 		}
 		
 		if (hashFunctions == null || hashFunctions.length == 0) {
 			throw new RuntimeException("You should provide at least one hash function to a bloom filter!");
 		}
 		
 		this.filterLength  = length;
 		this.filterBitSet = new BitSet(this.filterLength);
 		this.hashFunctions = hashFunctions;
 	}
 
 	/**
 	 * Adds an element to this Bloom filter
 	 * @param element Element to add
 	 */
 	public void add(String element) {
 		add(element.getBytes());
 	}
 	
 	/**
 	 * Adds an element to this Bloom filter
 	 * @param data Element data to add
 	 */
 	public void add(byte[] data) {
 		int[] bloomFilterIndexes = calculateBloomFilterIndexes(data);
 		
 		for(int index = 0; index < bloomFilterIndexes.length; index++) {
 			filterBitSet.set(bloomFilterIndexes[index]);
 		}		
 	}
 	
 	/**
 	 * Query this Bloom filter for existence of an element
 	 * @param element Element to test existence in this instance
 	 * @return {@link false} if the element was definitely not added to this instance, 
 	 * {@link true} if the element was possibly added to this instance.
 	 */
 	public boolean contains(String element) {
 		return contains(element.getBytes());
 	}
 	
 	public boolean contains(byte[] data) {
 		int[] bloomFilterIndexes = calculateBloomFilterIndexes(data);			
 		
 		for(int index = 0; index < bloomFilterIndexes.length; index++) {
 			if (!filterBitSet.get(bloomFilterIndexes[index])) {
 				return false;
 			}
 		}
 		
 		return true;
 	}		
 
 	/**
 	 * Calculates Bloom filters indexes for a given input
 	 * @param data The data to generate indexes for
 	 * @return An array on indexes for a Bloom filter. The size of returned array is the 
 	 * "k" parameter in Bloom filter theory
 	 */
 	private int[] calculateBloomFilterIndexes(byte[] data) {		
 		int[] hashes = new int[hashFunctions.length];
 		
 		for(int index = 0; index < hashFunctions.length; index++)
 			hashes[index] = Math.abs(hashFunctions[index].getHash(data) % filterLength);
 
 		return hashes;		
 	}
 }
