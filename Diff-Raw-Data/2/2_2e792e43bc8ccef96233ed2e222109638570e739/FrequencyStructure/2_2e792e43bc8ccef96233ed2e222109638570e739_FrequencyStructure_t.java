 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
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
 package com.github.fhirschmann.clozegen.lib.frequency;
 
 import ca.odell.glazedlists.BasicEventList;
 import ca.odell.glazedlists.EventList;
 import ca.odell.glazedlists.SortedList;
 import com.github.fhirschmann.clozegen.lib.util.ListUtils;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class represents a datastructure for managing frequencies for
  * objects.
  * <p>
  * Internally, this structure is based on multiple structures, namely
  * Lists and Maps. The frequency along with the object it relates to
  * are stored in {@link FrequencyPair} and put into a list. Another list
  * which transforms this list into a sorted list (glazed lists, see below)
  * is used to support methods like {@link getAdjacentTo}.<br />
  * For fast lookup of an individual frequency, a HashMap is used. The Value
  * of this HashMap points to the FrequencyPair as it appears in the list.
  * </p>
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class FrequencyStructure<V> implements Iterable {
     /** Map for fast lookups. */
     private final Map<V, Integer> hashMap = Maps.newHashMap();
 
     /** List for fast addition of new items. */
     private final EventList<FrequencyPair<V>> basicList = new BasicEventList();
 
     /** Sorted List for accessing intervals. */
     private final transient EventList<FrequencyPair<V>> sortedList =
             new SortedList(basicList);
 
     /**
      * Adds a value with a given frequency to this structure.
      *
      * @param value the value to add
      * @param frequency the frequency of this value
      * @return if this structure did not already contain the specified element
      */
     public boolean add(final V value, final int frequency) {
         if (hashMap.containsKey(value)) {
             return false;
         }
         basicList.add(new FrequencyPair(value, frequency));
         hashMap.put(value, basicList.size() - 1);
 
         return true;
     }
 
     /**
      * Returns a {@link FrequencyPair} for a given value in constant time.
      *
      * @param value the value to look up
      * @return a pair of the value and the frequency
      */
     public FrequencyPair<V> getFrequencyPair(final V value) {
         if (!hashMap.containsKey(value)) {
             return null;
         }
         final int index = hashMap.get(value);
 
         return basicList.get(index);
     }
 
     /**
      * Returns the frequency for a given value in constant time.
      *
      * @param value the value to look up
      * @return the frequency
      */
     public int getFrequency(final V value) {
         final FrequencyPair<V> frequencyPair = getFrequencyPair(value);
 
         if (frequencyPair == null) {
             return 0;
         }
 
         return frequencyPair.getFrequency();
     }
 
     /**
      * Extracts the values from a {@link FrequencyPair} and puts it into a list.
      *
      * @param <V> type of values
      * @param pairs list of {@link FrequencyPair}
      * @return list of V
      */
     public static <V> List<V> frequencyPairs2Values(final List<FrequencyPair<V>> pairs) {
         final List<V> result = Lists.newArrayList();
 
         for (FrequencyPair<V> pair : pairs) {
             result.add(pair.getValue());
         }
         return result;
     }
 
     /**
      * Returns the number of elements in this structure (its cardinality).
      *
      * @return the size of this structure
      */
     public int size() {
         return basicList.size();
     }
 
     /**
      * Returns a list of the adjacent neighbors of a {@link FrequencyPair}.
      *
      * @see ListUtils#getAdjacentTo(java.util.List, java.lang.Object, int)
      * @param frequencyPair the frequency pair to get the neighbors for
      * @param num number of neighbors to get (on each side)
      * @return the neighbors (as list of {@link FrequencyPair})
      */
     public List<FrequencyPair<V>> getAdjacentTo(final FrequencyPair<V> frequencyPair,
             final int num) {
 
         if (frequencyPair == null) {
             return null;
         }
 
         return ListUtils.<FrequencyPair<V>>getAdjacentTo(sortedList, frequencyPair, num);
     }
 
     /**
      * Returns a list of the adjacent neighbors of a value.
      *
      * @see ListUtils#getAdjacentTo(java.util.List, java.lang.Object, int)
      * @param value the value to get the neighbors for
      * @param num number of neighbors to get (on each side)
      * @return the neighbors (as list of V)
      */
     public List<V> getAdjacentTo(final V value, final int num) {
         final FrequencyPair<V> frequencyPair = getFrequencyPair(value);
 
         if (frequencyPair == null) {
             return null;
         }
 
         final List<FrequencyPair<V>> adjacent = getAdjacentTo(frequencyPair, num);
 
         return frequencyPairs2Values(adjacent);
     }
 
 
     /**
      * This will return a number of {@FrequencyPair} closest to the
      * value given.
      * <p>
      * The FrequencyPair's frequency attribute will contain the distance to
      * the frequency of the value you give.
      * </p>
      *
     * @param fp the FrequencyPair
      * @param num the number of close items to get
      * @return a list of FrequencyPair
      */
     public List<FrequencyPair<V>> getClosestTo(final FrequencyPair<V> fp, final int num) {
         final List<FrequencyPair<V>> adjacent = getAdjacentTo(fp, num);
         final List<FrequencyPair<V>> sorted = Lists.newLinkedList();
 
         for (FrequencyPair<V> frequencyPair : adjacent) {
             if (frequencyPair.equals(fp)) {
                 continue;
             }
             final FrequencyPair newFrequencyPair =
                     new FrequencyPair<V>(
                         frequencyPair.getValue(),
                         Math.abs(frequencyPair.getFrequency() - fp.getFrequency()));
             sorted.add(newFrequencyPair);
         }
         Collections.sort(sorted);
         return sorted.subList(0, num);
     }
 
 
     /**
      * This will return a list of V closest to the V given.
      *
      * @param value the value
      * @param num the number of close items to get
      * @return a list of V
      */
     public List<V> getClosestTo(final V value, final int num) {
         final FrequencyPair<V> frequencyPair = getFrequencyPair(value);
 
         if (frequencyPair == null) {
             return null;
         }
         final List<FrequencyPair<V>> closest = getClosestTo(frequencyPair, num);
         return frequencyPairs2Values(closest);
     }
 
     @Override
     public Iterator iterator() {
         return sortedList.iterator();
     }
 }
