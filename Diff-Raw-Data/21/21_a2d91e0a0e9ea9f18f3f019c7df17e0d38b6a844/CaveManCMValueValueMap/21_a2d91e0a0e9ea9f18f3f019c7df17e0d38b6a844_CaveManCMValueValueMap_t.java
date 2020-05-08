 /*
  * caveman - A primitive collection library
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.caveman.proto.impl;
 
 import java.util.ConcurrentModificationException;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import com.mebigfatguy.caveman.proto.CMValueValueMap;
 import com.mebigfatguy.caveman.proto.CMValueValueMapIterator;
 import com.mebigfatguy.caveman.proto.aux.CMValue;
 
 public class CaveManCMValueValueMap<K> implements CMValueValueMap<K> {
 	private static final int DEFAULT_CAPACITY = 31;
 	private static final float DEFAULT_LOAD_FACTOR = 0.80f;
 
 	private CMBucket<K>[] buckets;
 	private int size;
 	private float loadFactor;
 	private int version;
 	
 	public CaveManCMValueValueMap() {
 		this(DEFAULT_CAPACITY);
 	}
 	
 	public CaveManCMValueValueMap(int initialCapacity) {
 		this(initialCapacity, DEFAULT_LOAD_FACTOR);
 	}
 	
 	public CaveManCMValueValueMap(int initialCapacity, float loadingFactor) {
 		loadFactor = loadingFactor;
 		size = 0;
 		buckets = new CMBucket[initialCapacity];
 	}
 	
 	@Override
 	public int size() {
 		return size;
 	}
 	
 	@Override
 	public boolean isEmpty() {
 		return size == 0;
 	}
 	
 	@Override
 	public boolean containsKey(K key) {
 		int hash = (key == null) ? 0 : (key.hashCode() % buckets.length);
 		CMBucket<K> b = buckets[hash];
 		
 		if (b == null)
 			return false;
 		
 		return b.indexOf(key) >= 0;
 	}
 	
 	@Override
 	public boolean containsValue(CMValue value) {
 		for (CMBucket<K> bucket : buckets) {
 			if (bucket != null) {
 				for (int i = 0; i < bucket.bucketSize; ++i) {
 					if (bucket.values[i] == value) {
 						return true;
 					}
 				}
 			}
 		}
 		
 		return false;	
 	}
 	
 	@Override
 	public CMValue get(K key, CMValue notFoundValue) {
 		int hash = (key == null) ? 0 : (key.hashCode() % buckets.length);
 		CMBucket<K> b = buckets[hash];
 		
 		if (b != null) {
 			return b.get(key, notFoundValue);	
 		}
 		
 		return notFoundValue;
 	}
 	
 	@Override
 	public void put(K key, CMValue value) {
 		++version;
 		
 		ensureSize(size + 1);
 
 		
 		int hash = (key == null) ? 0 : (key.hashCode() % buckets.length);
 		CMBucket<K> b = buckets[hash];
 		
 		if (b == null) {
 			b = new CMBucket<K>();
 			buckets[hash] = b;
 		}
 		
 		if (b.add(key, value)) {
 			++size;
 		}
 	}
 	
 	@Override
 	public void remove(K key) {
 		++version;
 		
 		int hash = (key == null) ? 0 : (key.hashCode() % buckets.length);
 		CMBucket<K> b = buckets[hash];
 		
 		if (b != null) {
 			if (b.remove(key)) {
 				--size;
 			}
 		}
 	}
 	
 	@Override
 	public void putAll(CMValueValueMap<K> m) {
 		++version;
 		
 		ensureSize(size + m.size());
 		
 		CMValueValueMapIterator<K> iterator = m.iterator();
 		
 		while (iterator.hasNext()) {
 			iterator.next();
 			put(iterator.key(), iterator.value());
 		}
 	}
 	
 	@Override
 	public void clear() {
 		++version;
 		
 		for (CMBucket<K> b : buckets) {
 			if (b != null) {
 				b.clear();
 			}
 		}
 		size = 0;
 	}
 	
 	@Override
 	public CMValueValueMapIterator<K> iterator() {
		return new CaveManCMValueValueMapIterator(version);
 	}
 	
 	@Override
 	public Set<K> keySet() {
 		throw new UnsupportedOperationException();
 	}	
 	
 	@Override
 	public CaveManCMBag values() {
 		throw new UnsupportedOperationException();
 	}
 	
 	private void ensureSize(int newSize) {
 		if ((newSize / (double) buckets.length) > loadFactor) {
 			int newBucketSize = (int) ((2.0 * loadFactor) * newSize);
 			CMBucket<K>[] newBuckets = new CMBucket[newBucketSize];
 			
 			for (CMBucket<K> oldBucket : buckets) {
 				if (oldBucket != null) {
 					for (int oldBucketIndex = 0; oldBucketIndex < oldBucket.bucketSize; ++oldBucketIndex) {
 						K key = oldBucket.keys[oldBucketIndex];
 						int hash = (key == null) ? 0 : (key.hashCode() % buckets.length);
 						CMBucket<K> newBucket = newBuckets[hash];
 						if (newBucket == null) {
 							newBucket = new CMBucket<K>();
 							newBuckets[hash] = newBucket;
 						}
 						newBucket.add(key, oldBucket.values[oldBucketIndex]);
 					}
 				}
 			}
 			buckets = newBuckets;		
 		}
 	}
 	
 	private static class CMBucket<K> {
 		K[] keys = (K[])new Object[1];
 		CMValue[] values = new CMValue[1];
 		int bucketSize;
 		
 		public boolean add(K key, CMValue value) {
 			int existingIndex = indexOf(key);
 			if (existingIndex >= 0) {
 				values[existingIndex] = value;
 			} else {
 				if (bucketSize >= keys.length) {
 					K[] newKeys = (K[])new Object[keys.length + 4];
 					System.arraycopy(keys,  0, newKeys, 0, bucketSize);
 					keys = newKeys;
 					CMValue[] newValues = new CMValue[values.length + 4];
 					System.arraycopy(values,  0, newValues, 0, bucketSize);
 					values = newValues;					
 				}
 				
 				keys[bucketSize] = key;
 				values[bucketSize++] = value;
 			}
 			
 			return true;
 		}
 		
 		public boolean remove(K key) {
 			for (int i = 0; i < bucketSize; i++) {
 				if (((key == null) && (keys[i] == null)) || key.equals(keys[i])) {
 					--bucketSize;
 					System.arraycopy(keys, i + 1, keys, i, bucketSize - i);
 					System.arraycopy(values, i + 1, values, i, bucketSize - i);
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		public int indexOf(K key) {
 			for (int i = 0; i < bucketSize; i++) {
 				if (((key == null) && (keys[i] == null)) || key.equals(keys[i])) {
 					return i;
 				}
 			}
 			
 			return -1;
 		}
 		
 		public CMValue get(K key, CMValue notFoundValue) {
 			for (int i = 0; i < bucketSize; i++) {
 				if (((key == null) && (keys[i] == null)) || key.equals(keys[i])) {
 					return values[i];
 				}
 			}
 			
 			return notFoundValue;
 		}
 		
 		public void clear() {
 			bucketSize = 0;
 		}
 	}
 	
	private class CaveManCMValueValueMapIterator<K> implements CMValueValueMapIterator<K> {
 
 		private final int iteratorVersion;
 		private int bucketIndex;
 		private int bucketSubIndex;
 		private int pos;
 		private K key;
 		private CMValue value;
 		
		public CaveManCMValueValueMapIterator(int version) {
 			iteratorVersion = version;
 			
 			pos = 0;
 			if (size > 0) {
 				for (bucketIndex = 0; bucketIndex < buckets.length; bucketIndex++) {
 					CMBucket b = buckets[bucketIndex];
 					if ((b != null) && (b.bucketSize > 0)) {
 						bucketSubIndex = 0;
 						break;
 					}
 				}
 				//?? shouldn't get here
 			}
 		}
 		
 		@Override
 		public boolean hasNext() {
 			if (iteratorVersion != version) {
 				throw new ConcurrentModificationException((version - iteratorVersion) + " changes have been made since the iterator was created");
 			}
 
 			return pos >= size;
 		}
 
 		@Override
 		public void next() throws NoSuchElementException {
 			if (iteratorVersion != version) {
 				throw new ConcurrentModificationException((version - iteratorVersion) + " changes have been made since the iterator was created");
 			}
 			
 
 			if (pos >= size) {
 				throw new NoSuchElementException("Index " + pos + " is out of bounds [0, " + (size - 1) + "]");
 			}
 
 			CMBucket b = buckets[bucketIndex];
 			key = (K)b.keys[bucketSubIndex];
 			value = b.values[bucketSubIndex++];
 			
 			if (bucketSubIndex >= b.keys.length) {
 				bucketSubIndex = 0;
 				for (;bucketIndex < buckets.length; bucketIndex++) {
 					b = buckets[bucketIndex];
 					if ((b != null) && (b.bucketSize > 0)) {
 						break;
 					}
 				}
 			}
 			++pos;
 		}
 
 		@Override
 		public K key() {
 			if (iteratorVersion != version) {
 				throw new ConcurrentModificationException((version - iteratorVersion) + " changes have been made since the iterator was created");
 			}
 
 			return key;
 		}
 
 		@Override
 		public CMValue value() {
 			if (iteratorVersion != version) {
 				throw new ConcurrentModificationException((version - iteratorVersion) + " changes have been made since the iterator was created");
 			}
 
 			return value;
 		}
 
 		@Override
 		public void remove() {
 			if (iteratorVersion != version) {
 				throw new ConcurrentModificationException((version - iteratorVersion) + " changes have been made since the iterator was created");
 			}
 			
 			if (pos >= size) {
 				throw new NoSuchElementException("Index " + pos + " is out of bounds [0, " + (size - 1) + "]");
 			}
 			
 			CMBucket b = buckets[bucketIndex];
 			System.arraycopy(b.keys, bucketSubIndex + 1, b.keys, bucketSubIndex, b.bucketSize - bucketSubIndex);
 			System.arraycopy(b.values, bucketSubIndex + 1, b.values, bucketSubIndex, b.bucketSize - bucketSubIndex);
 			--b.bucketSize;
 			if (bucketSubIndex >= b.bucketSize) {
 				bucketSubIndex = 0;
 				for (;bucketIndex < buckets.length; bucketIndex++) {
 					b = buckets[bucketIndex];
 					if ((b != null) && (b.bucketSize > 0)) {
 						break;
 					}
 				}
 			}
 			--pos;
 		}	
 	}
 
 }
