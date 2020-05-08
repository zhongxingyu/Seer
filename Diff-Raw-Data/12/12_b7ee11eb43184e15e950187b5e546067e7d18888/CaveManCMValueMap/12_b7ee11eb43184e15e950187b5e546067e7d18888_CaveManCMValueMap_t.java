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
 
 import java.lang.reflect.Array;
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import com.mebigfatguy.caveman.proto.CMBag;
 import com.mebigfatguy.caveman.proto.CMCollection;
 import com.mebigfatguy.caveman.proto.CMIterator;
 import com.mebigfatguy.caveman.proto.CMValueMap;
 import com.mebigfatguy.caveman.proto.CMValueMapIterator;
 import com.mebigfatguy.caveman.proto.aux.CM;
 
 public class CaveManCMValueMap<K> implements CMValueMap<K> {
 	private static final int DEFAULT_CAPACITY = 31;
 	private static final float DEFAULT_LOAD_FACTOR = 0.80f;
 
 	private CMBucket<K>[] buckets;
 	private int size;
 	private float loadFactor;
 	private int version;
 	
 	public CaveManCMValueMap() {
 		this(DEFAULT_CAPACITY);
 	}
 	
 	public CaveManCMValueMap(int initialCapacity) {
 		this(initialCapacity, DEFAULT_LOAD_FACTOR);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public CaveManCMValueMap(int initialCapacity, float loadingFactor) {
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
 	public boolean containsValue(CM value) {
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
 	public CM get(K key, CM notFoundValue) {
 		int hash = (key == null) ? 0 : (key.hashCode() % buckets.length);
 		CMBucket<K> b = buckets[hash];
 		
 		if (b != null) {
 			return b.get(key, notFoundValue);	
 		}
 		
 		return notFoundValue;
 	}
 	
 	@Override
 	public void put(K key, CM value) {
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
 	public void putAll(CMValueMap<K> m) {
 		++version;
 		
 		ensureSize(size + m.size());
 		
 		CMValueMapIterator<K> iterator = m.iterator();
 		
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
 	public CMValueMapIterator<K> iterator() {
 		return new CaveManCMValueMapIterator(version);
 	}
 	
 	@Override
 	public Set<K> keySet() {
 		return new CaveManCMValueKeySet();
 	}	
 	
 	@Override
 	public CMBag values() {
 		return new CaveManCMValuesBag();
 	}
 	
 	@SuppressWarnings("unchecked")
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
 		@SuppressWarnings("unchecked")
 		K[] keys = (K[])new Object[1];
 		CM[] values = new CM[1];
 		int bucketSize;
 		
 		public boolean add(K key, CM value) {
 			int existingIndex = indexOf(key);
 			if (existingIndex >= 0) {
 				values[existingIndex] = value;
 				return false;
 			}
 			
 			if (bucketSize >= keys.length) {
 				@SuppressWarnings("unchecked")
 				K[] newKeys = (K[])new Object[keys.length + 4];
 				System.arraycopy(keys,  0, newKeys, 0, bucketSize);
 				keys = newKeys;
 				CM[] newValues = new CM[values.length + 4];
 				System.arraycopy(values,  0, newValues, 0, bucketSize);
 				values = newValues;					
 			}
 			
 			keys[bucketSize] = key;
 			values[bucketSize++] = value;
 			
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
 		
 		public int removeAllValues(CM value) {
 			int removeCount = 0;
 			for (int i = 0; i < bucketSize; i++) {
 				if (value == values[i]) {
 					--bucketSize;
 					System.arraycopy(keys, i + 1, keys, i, bucketSize - i);
 					System.arraycopy(values, i + 1, values, i, bucketSize - i);
 					++removeCount;
 				}
 			}
 			return removeCount;
 		}
 		
 		public int indexOf(K key) {
 			for (int i = 0; i < bucketSize; i++) {
 				if (((key == null) && (keys[i] == null)) || key.equals(keys[i])) {
 					return i;
 				}
 			}
 			
 			return -1;
 		}
 		
 		public CM get(K key, CM notFoundValue) {
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
 	
 	private class CaveManCMValueMapIterator implements CMValueMapIterator<K> {
 
 		private final int iteratorVersion;
 		private int bucketIndex;
 		private int bucketSubIndex;
 		private int pos;
 		private K key;
 		private CM value;
 		
 		public CaveManCMValueMapIterator(int version) {
 			iteratorVersion = version;
 			
 			pos = 0;
 			if (size > 0) {
 				for (bucketIndex = 0; bucketIndex < buckets.length; bucketIndex++) {
 					CMBucket<K> b = buckets[bucketIndex];
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
 
 			CMBucket<K> b = buckets[bucketIndex];
 			key = b.keys[bucketSubIndex];
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
 		public CM value() {
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
 			
 			CMBucket<K> b = buckets[bucketIndex];
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
 	
 	class CaveManCMValueKeySet implements Set<K> {
 
 		@Override
 		public int size() {
 			return CaveManCMValueMap.this.size();
 		}
 
 		@Override
 		public boolean isEmpty() {
 			return CaveManCMValueMap.this.isEmpty();		
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean contains(Object o) {
 			return CaveManCMValueMap.this.containsKey((K) o);
 		}
 
 		@Override
 		public Iterator<K> iterator() {
 			return new CaveManCMValueMapKeySetIterator();
 		}
 
 		@Override
 		public Object[] toArray() {
 			Object[] data = new Object[size];
 			
 			int pos = 0;
 			for (CMBucket<K> bucket : buckets) {
 				if (bucket != null) {
 					for (int i = 0; i < bucket.bucketSize; ++i) {
 						data[pos++] = bucket.keys[i];
 					}
 				}
 			}
 			return data;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public <T> T[] toArray(T[] data) {
 			
 			if (data.length < size) {
 				data = (T[]) Array.newInstance(data.getClass().getComponentType(), size);
 			}
 			
 			int pos = 0;
 			for (CMBucket<K> bucket : buckets) {
 				if (bucket != null) {
 					for (int i = 0; i < bucket.bucketSize; ++i) {
 						data[pos++] = (T)bucket.keys[i];
 					}
 				}
 			}
 
 			return data;
 		}
 
 		@Override
 		public boolean add(K e) {
 			throw new UnsupportedOperationException("add from the KeySet of a CaveManCMValueMap is not supported, as there is no default value");
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean remove(Object o) {
 			int originalSize = size;
 			CaveManCMValueMap.this.remove((K) o);
 			return size != originalSize;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean containsAll(Collection<?> c) {
 			for (Object o : c) {
 				if (!containsKey((K) o)) {
 					return false;
 				}
 			}
 			
 			return true;
 		}
 
 		@Override
 		public boolean addAll(Collection<? extends K> c) {
 			throw new UnsupportedOperationException("addAll from the KeySet of a CaveManCMValueMap is not supported, as there are no default values");
 		}
 
 		@Override
 		public boolean retainAll(Collection<?> c) {
 			
 			int originalSize = size;
			
			CMValueMapIterator<K> it = CaveManCMValueMap.this.iterator();
			while (it.hasNext()) {
				it.next();
				K k = it.key();
				if (!c.contains(k)) {
					it.remove();
 				}
 			}
 			
 			return originalSize != size;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean removeAll(Collection<?> c) {
 			int originalSize = size;
 			for (Object o : c) {
 				CaveManCMValueMap.this.remove((K) o);
 			}
 			
 			return originalSize != size;
 		}
 
 		@Override
 		public void clear() {
 			CaveManCMValueMap.this.clear();
 		}	
 		
 		private class CaveManCMValueMapKeySetIterator implements Iterator<K> {
 
 			private final CMValueMapIterator<K> iterator = CaveManCMValueMap.this.iterator();
 			
 			@Override
 			public boolean hasNext() {
 				return iterator.hasNext();
 			}
 
 			@Override
 			public K next() {
 				iterator.next();
 				return iterator.key();
 			}
 
 			@Override
 			public void remove() {
 				iterator.remove();
 			}	
 		}
 	}
 	
 	private class CaveManCMValuesBag implements CMBag {
 
 		@Override
 		public int size() {
 			return CaveManCMValueMap.this.size();
 		}
 
 		@Override
 		public boolean isEmpty() {
 			return CaveManCMValueMap.this.isEmpty();
 		}
 
 		@Override
 		public boolean contains(CM item) {
 			return CaveManCMValueMap.this.containsValue(item);
 		}
 
 		@Override
 		public CMIterator iterator() {
 			return new CaveManCMValuesBagIterator();
 		}
 
 		@Override
 		public CM[] toArray() {
 			CM[] data = new CM[size];
 			
 			int pos = 0;
 			for (CMBucket<K> bucket : buckets) {
 				if (bucket != null) {
 					for (int i = 0; i < bucket.bucketSize; ++i) {
 						data[pos++] = bucket.values[i];
 					}
 				}
 			}
 			
 			return data;
 		}
 
 		@Override
 		public boolean add(CM item) {
 			throw new UnsupportedOperationException("add is not supported from the values bag of a CaveManCMValueMap as there's no obvious key");
 		}
 
 		@Override
 		public boolean remove(CM item) {
 			++version;
 
 			int removeCount = 0;
 			
 			for (CMBucket<K> bucket : buckets) {
 				if (bucket != null) {
 					removeCount += bucket.removeAllValues(item);
 				}
 			}
 			
 			size -= removeCount;
 			return removeCount != 0;
 		}
 
 		@Override
 		public void clear() {
 			CaveManCMValueMap.this.clear();
 		}
 
 		@Override
 		public boolean containsAll(CMCollection c) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean addAll(CMCollection c) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean retainAll(CMCollection c) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean removeAll(CMCollection c) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean removeOne(CM item) {
 			throw new UnsupportedOperationException();
 		}
 		
 		private class CaveManCMValuesBagIterator implements CMIterator {
 
 			private final CMValueMapIterator<K> iterator = CaveManCMValueMap.this.iterator();
 			
 			@Override
 			public boolean hasNext() {
 				return iterator.hasNext();
 			}
 
 			@Override
 			public CM next() throws NoSuchElementException {
 				iterator.next();
 				return iterator.value();
 			}
 
 			@Override
 			public void remove() {
 				iterator.remove();
 			}
 		}
 	}
 }
