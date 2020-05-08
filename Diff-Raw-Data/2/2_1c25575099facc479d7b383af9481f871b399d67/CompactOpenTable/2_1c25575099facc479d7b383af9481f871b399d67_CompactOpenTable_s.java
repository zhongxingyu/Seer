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
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * If asked for a value that is not present create one using given factory, add it to our map and return
  * it - all in a thread-safe manner.
  *
  * @author jules
  *
  * @param <V>
  */
 public class CompactOpenTable<V> implements Table<Integer, V> {
 
 	public static interface ResizeStrategy {
 		int getNewSize(int oldSize, int newKey);
 	}
 	
 	private static final Logger LOG = LoggerFactory.getLogger(CompactOpenTable.class);
 
 	private final Factory<Integer, V> factory;
 	private final ResizeStrategy resizer;
 	
 	private V[] values;
 
 	@Deprecated
 	public CompactOpenTable(List<V> values, Factory<Integer, V> factory) {
 		this(values, factory, new ResizeStrategy() {
 			@Override
 			public int getNewSize(int oldSize, int newKey) {
 				return newKey + 1; // very dumb - user should provide something more sophisticated
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	public CompactOpenTable(List<V> values, Factory<Integer, V> factory, ResizeStrategy resizer) {
 		this.factory = factory;
		this.values = (V[])values.toArray();
 		this.resizer = resizer;
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected void resize(Integer key) {
 		// we need to resize to accommodate the new item that we are about
 		// to create...
 		V[] newValues = (V[])new Object[resizer.getNewSize(values.length, key)];
 		System.arraycopy(values, 0, newValues, 0, values.length);
 		values = newValues;
 	}
 	
 	@Override
 	public V get(Integer key) {
 		V value = null;
 		synchronized (this) {
 			if (key < values.length) {
 				value = values[key];
 			} else {
 				resize(key);
 			}
 			if (value == null) {
 				try {
 					value = (values[key] = factory.create(key));
 				} catch (Exception e) {
 					LOG.error("unable to create new Table item", e);
 				}
 			}
 		}
 		return value;
 
 	}
 
 	@Override
 	public V put(Integer key, V newValue) {
 		V oldValue;
 		synchronized (this) {
 			if (key >= values.length) {
 				resize(key);
 				oldValue =null;
 			} else {
 				oldValue = values[key];
 			}
 
 			values[key] = newValue;
 		}
 		return oldValue;
 	}
 
 //	@Override
 //	public V rem(Integer key, V value) {
 //		values.remove(key);
 //		throw new UnsupportedOperationException("NYI");
 //		// TODO: check...
 //		//return value;
 //	}
 }
