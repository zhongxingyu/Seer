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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.dada.slf4j.Logger;
 import org.dada.slf4j.LoggerFactory;
 
 public class Splitter<K, V> implements View<V> {
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 
 	// stateless / context-free splits
 	public interface StatelessStrategy<K, V> {
 		boolean getMutable();
		K getKey(V value); // any value // TODO: should allow return of multiple keys
 		Collection<View<V>> getViews(K key);
 	}
 
 	// stateful / context-sensitive splits
 	public interface StatefulStrategy<K, V> {
 		boolean getMutable();
 		Collection<K> createKeys(V value); // unknown value
 		Collection<K> findKeys(V value); // known value
 		Collection<View<V>> getViews(K key);
 	}
 	
 	private final StatefulStrategy<K, V> strategy;
 	private final boolean mutable;
 
 	// allows plugging in of a stateless strategy via a stateful strategy interface
 	private static class Adaptor<K, V> implements StatefulStrategy<K, V> {
 		
 		private final StatelessStrategy<K,V> strategy;
 		
 		public Adaptor(StatelessStrategy<K, V> strategy) {
 			this.strategy = strategy;
 		}
 
 		@Override
 		public boolean getMutable() {
 			return strategy.getMutable();
 		}
 
 		@Override
 		public Collection<K> createKeys(V value) {
			return Collections.singleton(strategy.getKey(value));
 		}
 
 		@Override
 		public Collection<K> findKeys(V value) {
			return Collections.singleton(strategy.getKey(value));
 		}
 
 		@Override
 		public Collection<View<V>> getViews(K key) {
 			return strategy.getViews(key);
 		}
 	};
 	
 	public Splitter(StatelessStrategy<K, V> strategy) {
 		this.strategy = new Adaptor<K, V>(strategy);
 		this.mutable = this.strategy.getMutable();
 		
 	}
 
 	public Splitter(StatefulStrategy<K, V> strategy) {
 		this.strategy = strategy;
 		this.mutable = this.strategy.getMutable();
 	}
 
 	private Batch<V> ensureBatch(Map<K, Batch<V>> map, K key) {
 		Batch<V> batch = map.get(key);
 		if (batch == null)
 			map.put(key, batch = new Batch<V>());
 		
 		return batch;
 	}
 	
 	// TODO: optimise for single update case...
 
 	@Override
 	public void update(Collection<Update<V>> insertions, Collection<Update<V>> alterations, Collection<Update<V>> deletions) {
 
 		// map to hold strategy insertions/alterations/deletions
 		Map<K, Batch<V>> keyToBatch = new HashMap<K, Batch<V>>();
 
 		// strategy insertions
 		for (Update<V> insertion : insertions)
 			for (K key : strategy.createKeys(insertion.getNewValue()))
 				ensureBatch(keyToBatch, key).addInsertion(insertion);
 
 		// strategy alterations
 		for (Update<V> alteration : alterations) {
 			Collection<K> newKeys = strategy.createKeys(alteration.getNewValue());
 			Collection<K> oldKeys;
 			// the boolean test of 'mutable 'does not add any further constraint - it simply heads off a more expensive
 			// test if possible - therefore we cannot produce coverage for the case where an immutable attribute is mutated...
 			if (mutable && (oldKeys = strategy.findKeys(alteration.getOldValue())) != newKeys) {
 				for (K key : newKeys)
 					ensureBatch(keyToBatch, key).addInsertion(alteration);
 				for (K key : oldKeys)
 					ensureBatch(keyToBatch, key).addDeletion(alteration);
 			} else {
 				for (K key : newKeys)
 					ensureBatch(keyToBatch, key).addAlteration(alteration);
 			}
 		}
 
 		// strategy deletions
 		for (Update<V> deletion : deletions)
 			for (K key : strategy.findKeys(deletion.getOldValue()))
 				ensureBatch(keyToBatch, key).addDeletion(deletion);
 		
 		// dispatch strategy on relevant Views...
 		for (Entry<K, Batch<V>> entry : keyToBatch.entrySet()) {
 			Batch<V> batch = entry.getValue();
 			for (View<V> view : strategy.getViews(entry.getKey()))
 				try {
 					view.update(batch.getInsertions(), batch.getAlterations(), batch.getDeletions());
 				} catch (Throwable t) {
 					logger.error("problem updating View", t);
 				}
 		}
 	}
 
 }
