 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.types.container;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import org.jbasics.collection.CollectionUtilities;
 import org.jbasics.pattern.delegation.Delegate;
 import org.jbasics.pattern.factory.Factory;
 import org.jbasics.pattern.modifer.Concatable;
 import org.jbasics.pattern.resolver.Resolver;
 import org.jbasics.types.delegates.UnmodifiableDelegate;
 import org.jbasics.types.factories.MapFactory;
 import org.jbasics.utilities.DataUtilities;
 
 public class ChainedMap<K, V> implements Map<K, V>, Concatable<Map<K, V>>, Delegate<Map<K, V>>, Resolver<V, K> {
 	private final Delegate<Map<K, V>> parentMapDelegate;
 	private final Map<K, V> map;
 
 	public ChainedMap() {
 		this(null, (Map<K, V>) null);
 	}
 
 	public ChainedMap(Map<K, V> parentMap) {
 		this(UnmodifiableDelegate.create(parentMap), (Map<K, V>) null);
 	}
 
 	public ChainedMap(Delegate<Map<K, V>> parentMapDelegate) {
 		this(parentMapDelegate, (Map<K, V>) null);
 	}
 
 	@SuppressWarnings("unchecked")
 	public ChainedMap(Delegate<Map<K, V>> parentMapDelegate, Factory<Map<K, V>> mapStorageFactory) {
 		this(parentMapDelegate, DataUtilities.coalesce(mapStorageFactory, MapFactory.<K, V> unorderedMapFactory()).newInstance());
 	}
 
 	public ChainedMap(Delegate<Map<K, V>> parentMapDelegate, Map<K, V> map) {
 		this.parentMapDelegate = parentMapDelegate;
		this.map = map == null ? map : MapFactory.<K, V> unorderedMapFactory().newInstance();
 	}
 
 	@Override
 	public int size() {
 		return this.map.size() + CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate).size();
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return this.map.isEmpty() && CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate).isEmpty();
 	}
 
 	@Override
 	public boolean containsKey(Object key) {
 		return this.map.containsKey(key) || CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate).containsKey(key);
 	}
 
 	@Override
 	public boolean containsValue(Object value) {
 		return this.map.containsValue(value) || CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate).containsValue(value);
 	}
 
 	@Override
 	public V get(Object key) {
 		V temp = this.map.get(key);
 		return temp != null ? temp : CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate).get(key);
 	}
 
 	@Override
 	public V put(K key, V value) {
 		return this.map.put(key, value);
 	}
 
 	@Override
 	public V remove(Object key) {
 		return this.map.remove(key);
 	}
 
 	@Override
 	public void putAll(Map<? extends K, ? extends V> m) {
 		this.map.putAll(m);
 	}
 
 	@Override
 	public void clear() {
 		this.map.clear();
 	}
 
 	@Override
 	public Set<K> keySet() {
 		return CollectionUtilities.joinMapUnmodifiable(this.map, CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate))
 				.keySet();
 	}
 
 	@Override
 	public Collection<V> values() {
 		return CollectionUtilities.joinMapUnmodifiable(this.map, CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate))
 				.values();
 	}
 
 	@Override
 	public Set<java.util.Map.Entry<K, V>> entrySet() {
 		return CollectionUtilities.joinMapUnmodifiable(this.map, CollectionUtilities.dereferenceUnmodifiableMapDelegate(this.parentMapDelegate))
 				.entrySet();
 	}
 
 	@Override
 	public Map<K, V> concat(Map<K, V> other) {
 		return new ChainedMap<K, V>(this, other);
 	}
 
 	@Override
 	public Map<K, V> delegate() {
 		return this;
 	}
 
 	@Override
 	public V resolve(K request, V defaultResult) {
 		return DataUtilities.coalesce(get(request), defaultResult);
 	}
 }
