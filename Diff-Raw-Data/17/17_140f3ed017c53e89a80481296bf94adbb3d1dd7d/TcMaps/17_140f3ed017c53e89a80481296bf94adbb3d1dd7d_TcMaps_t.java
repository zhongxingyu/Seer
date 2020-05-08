 /*
  * Copyright (C) 2008 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.collection;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.Maps;
 
 import java.util.Map;
 import java.util.Map.Entry;
import java.util.Set;
 
import org.trancecode.function.TcPredicates;
 import org.trancecode.lang.TcObjects;
 
 /**
  * Utility methods related to {@link Map}.
  * 
  * @author Herve Quiroz
  */
 public final class TcMaps
 {
     private TcMaps()
     {
         // No instantiation
     }
 
     public static <K, V> Map<K, V> merge(final Map<K, V> map1, final Map<? extends K, ? extends V> map2)
     {
         final Builder<K, V> builder = ImmutableMap.builder();
        @SuppressWarnings("unchecked")
        final Set<K> map2keys = (Set<K>) map2.keySet();
        final Predicate<K> keyFilter = Predicates.not(TcPredicates.isContainedBy(map2keys));
        final Map<K, V> map1WithoutKeysFromMap2 = Maps.filterKeys(map1, keyFilter);
        return builder.putAll(map1WithoutKeysFromMap2).putAll(map2).build();
     }
 
     public static <K, V> Map<K, V> copyAndPut(final Map<K, V> map, final K key, final V value)
     {
         Preconditions.checkNotNull(map);
         Preconditions.checkNotNull(key);
         if (map instanceof ImmutableMap && TcObjects.equals(map.get(key), value))
         {
             return map;
         }
 
         final Builder<K, V> builder = ImmutableMap.builder();
        final Map<K, V> mapWithoutKey = Maps.filterKeys(map, Predicates.not(Predicates.equalTo(key)));
        return builder.putAll(mapWithoutKey).put(key, value).build();
     }
 
     public static <K, V> V get(final Map<K, V> map, final K key, final V defaultValue)
     {
         if (map.containsKey(key))
         {
             return map.get(key);
         }
 
         return defaultValue;
     }
 
     public static <K, V> Map<K, V> fromEntries(final Iterable<Entry<K, V>> entries)
     {
         final Function<Entry<K, V>, K> keyFunction = MapFunctions.getKey();
         final Function<Entry<K, V>, V> valueFunction = MapFunctions.getValue();
         final Map<K, Entry<K, V>> intermediate = Maps.uniqueIndex(entries, keyFunction);
         return Maps.transformValues(intermediate, valueFunction);
     }
 }
