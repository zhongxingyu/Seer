 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.core;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 
 import clojure.lang.Indexed;
 
 /**
  * Temporary class, needed whilst Clojure sequences are not Serializable - due to be remedied soon.
  * I will use this for compound keys etc which must be Comparable (unlike standard Java Collections) and Serializable (unlike current Clojure sequences).
  * We also implement Indexed, so we can be deconstructed in Clojure expressions e.g. (let [[a b c] (Tuple. 1 2 3)]...)
  *  
  * @author jules
  *
  * @param <V>
  */
 public class Tuple<V> implements Collection<V>, Serializable, Indexed, Comparable<Tuple<V>> {
 	
 	private final Object[] values;
 	
 	public Tuple(V... values) {
 		this.values = new Object[values.length];
 		int n =0;
 		for (V value : values) this.values[n++] = value;
 	}
 
 	public Tuple(Collection<V> values) {
 		this.values = values.toArray();
 	}
 	
 	public Tuple(V value1) {
 		this.values = new Object[1];
 		values[0] = value1;
 	}
 	
 	public Tuple(V value1, V value2) {
 		this.values = new Object[2];
 		values[0] = value1;
 		values[1] = value2;
 	}
 	
 	public Tuple(V value1, V value2, V value3) {
 		this.values = new Object[3];
 		values[0] = value1;
 		values[1] = value2;
 		values[2] = value3;
 	}
 	
 	public Tuple(V value1, V value2, V value3, V value4) {
 		this.values = new Object[4];
 		values[0] = value1;
 		values[1] = value2;
 		values[2] = value3;
 		values[3] = value4;
 	}
 	
 	public Tuple(V value1, V value2, V value3, V value4, V value5) {
 		this.values = new Object[5];
 		values[0] = value1;
 		values[1] = value2;
 		values[2] = value3;
 		values[3] = value4;
 		values[4] = value5;
 	}
 	
 	public Tuple(V value1, V value2, V value3, V value4, V value5, V value6) {
 		this.values = new Object[6];
 		values[0] = value1;
 		values[1] = value2;
 		values[2] = value3;
 		values[3] = value4;
 		values[4] = value5;
 		values[5] = value6;
 	}
 	
 	// Indexed ...
 	
 	@Override
 	public Object nth(int i) {
 		return values[i];
 	}
 
 	@Override
 	public Object nth(int i, Object notFound) {
 		return (i >= 0 && i < values.length) ? values[i] : notFound;
 	}
 
 	@Override
 	public int count() {
 		return values.length;
 	}
 
 	// Collection
 	
 	@Override
 	public boolean add(V e) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean addAll(Collection<? extends V> c) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public void clear() {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean contains(Object o) {
 		for (Object value : values)
 			if (value == o) return true;
 		return false;
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c) {
 		for (Object o : c)
 			if (!contains(o)) return false;
 		return true;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return false;
 	}
 
 	@Override
 	public Iterator<V> iterator() {
 		return new Iterator<V>() {
 
 			private int i = 0;
 			
 			@Override
 			public boolean hasNext() {
 				return i < values.length;
 			}
 
 			@Override
 			public V next() {
 				return (V)values[i++];
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException("Tuples are Immutable");
 			}
 		};
 	}
 
 	@Override
 	public boolean remove(Object o) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> c) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> c) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public int size() {
 		return values.length;
 	}
 
 	@Override
 	public Object[] toArray() {
 		return values;
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a) {
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	// Comparable
 	
 	@Override
 	public int compareTo(Tuple<V> that) {
 		int diff = this.size() - that.size();
 		if (diff != 0)
 			return diff;
 		else {
 			int size = values.length;
 			for (int i = 0; i < size; i++) {
 				int comparison = ((Comparable)this.values[i]).compareTo(that.values[i]);
 				if (comparison != 0) return comparison;
 			}
 			return 0;
 		}
 	}
 	
 	// Object
 	
 	@Override
 	public boolean equals(Object that) {
	    return (that instanceof Tuple) && Arrays.deepEquals(values, ((Tuple)that).values);
 	}
 
 	@Override
 	public int hashCode() {
 		return Arrays.deepHashCode(values);
 	}
 	
 	@Override
 	public String toString() {
 		return Arrays.deepToString(values);
 	}
 	
 }
