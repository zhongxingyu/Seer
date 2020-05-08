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
 package com.github.fhirschmann.clozegen.lib.util;
 
 import com.google.common.collect.*;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Utility functions for Multisets.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public final class MultisetUtils {
     /** Constructor in utility class should not be called. */
     private MultisetUtils() {
     }
 
     /**
      * Sorts a multiset by its counts and returns a new {@link LinkedHashMultiset}.
      *
      * @param <E> the type of the multiset elements
      * @param multiset to multiset to sort
      * @return a new mutable sorted multiset
      */
     public static <E> LinkedHashMultiset<E> sortMultiSet(final Multiset<E> multiset) {
        ImmutableMultiset<E> immutableSet = Multisets.copyHighestCountFirst(multiset);
         return LinkedHashMultiset.create(immutableSet);
     }
 
     /**
      * Returns a limited list of all (distinct) elements of a multiset ordered
      * by their counts.
      *
      * @param <E> the type of the multiset elements
      * @param multiset the multiset to work on
      * @param limit the maximum number of elements to return
      * @return a limited list of elements ordered by their count
      */
     public static <E> List<E> sortedElementList(final Multiset<E> multiset, int limit) {
         final List<E> list = Lists.newLinkedList();
         final LinkedHashMultiset<E> sms = sortMultiSet(multiset);
 
         if (limit > multiset.elementSet().size()) {
             throw new IllegalArgumentException(
                     "The multiset does not contain that many keys.");
         } else if (limit == -1) {
             limit = multiset.elementSet().size();
         }
 
         final Iterator<E> it = sms.iterator();
 
         E next;
         while (list.size() < limit) {
             next = it.next();
             if (!list.contains(next)) {
                 list.add(next);
             }
         }
         return list;
     }
 
     /**
      * Returns a list of all (distinct) elements of a multiset ordered
      * by their counts.
      *
      * @param <E> the type of the multiset elements
      * @param multiset the multiset to work on
      * @return a limited list of elements ordered by their count
      */
     public static <E> List<E> sortedElementList(final Multiset<E> multiset) {
         return sortedElementList(multiset, -1);
     }
 
     /**
      * Merges two multisets.
      *
      * @param <E> the type of the elements of both multisets
      * @param multiset1 multiset to merge
      * @param multiset2 multiset to merge
      * @return a new merged multiset
      */
     public static <E> Multiset<E> mergeMultiSets(final Multiset<E> multiset1,
             final Multiset<E> multiset2) {
         final Multiset<E> multiset = LinkedHashMultiset.create(multiset1);
         Iterators.addAll(multiset, multiset2.iterator());
         return multiset;
     }
 }
