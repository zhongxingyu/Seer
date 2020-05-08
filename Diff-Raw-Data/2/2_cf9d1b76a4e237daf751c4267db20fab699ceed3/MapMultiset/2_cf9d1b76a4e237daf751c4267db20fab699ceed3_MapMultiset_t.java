 /*
  * Copyright (K) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.multiset;
 
 import java.util.Map;
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.common.collect.*;
 
 /**
  * Object which maps a key to a {@link Multiset}. Additionally, it provides
 * convenience functions like
  * {@link MapMultiset#add(java.lang.Object, java.lang.Object, int)}.
  *
  * <p>This object maps delegates most of its methods to the underlying
  * map, so you can use the common map interface.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  *
  * @param <K> the type of keys maintained by this map
  * @param <V> the type of values for {@link MultiSet}
  */
 public class MapMultiset<K, V> extends ForwardingMap<K, Multiset<V>> {
     private Map<K, Multiset<V>> map;
 
     /**
      * Creates a new empty {@link MapMultiset}.
      *
      * @param <K> the type of keys maintained by this map
      * @param <V> the type of values for {@link MultiSet}
      * @return a new empty {@link MapMultiset}
      */
     public static <K, V> MapMultiset<K, V> create() {
         return new MapMultiset();
     }
 
     /**
      * Creates a new empty {@link MapMultiset}.
      */
     public MapMultiset() {
         super();
         map = Maps.newHashMap();
     }
 
     /**
      * Returns a copy of the {@link Multiset} identified by {@code key} as an
      * {@link ImmutableMultiset} whose order is highest count first, with ties broken by
      * the iteration order of the original multiset.
      *
      * @param key the key to identify the multiset by
      * @return a new {@link ImmutableMultiset} whose order is highest count first
      */
     public ImmutableMultiset<V> getSorted(final K key) {
         return Multisets.copyHighestCountFirst(checkNotNull(get(key)));
     }
 
     /**
      * Adds a value to the {@link Multiset} identified by {@code key}.
      *
      * <p>If the multiset does not exist yet, it will be created.
      *
      * @param key the key to identify the multiset by
      * @param value the value of the multiset to add
      * @param count the number to add
      */
     public void add(final K key, final V value, final int count) {
         get(key).add(checkNotNull(value), count);
     }
 
     /**
      * Gets the {@link Multiset} identified by {@code key}.
      *
      * <p>If there is no multiset present which can be identified by a given key,
      * a new empty one is created and returned.
      *
      * @param key the key to identify the multiset by
      * @return the {link Multiset} identified by key or a new empty one
      */
     @Override
     public Multiset<V> get(Object key) {
         Multiset<V> multiset;
 
         K kKey = (K) checkNotNull(key);
 
         if (containsKey(kKey)) {
             multiset = super.get(key);
         } else {
             multiset = HashMultiset.create();
             put((K) key, multiset);
         }
         return multiset;
     }
 
     /**
      * Returns to total number of entries in all {@link Multiset} objects
      * combined.
      *
      * @return total number of entries
      */
     public long total() {
         long total = 0;
         for (Entry<K, Multiset<V>> entry : this.entrySet()) {
             total += entry.getValue().size();
         }
         return total;
     }
 
     @Override
     protected Map<K, Multiset<V>> delegate() {
         return map;
     }
 }
