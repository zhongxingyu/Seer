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
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Range;
 import com.google.common.collect.Ranges;
 import com.google.common.collect.Sets;
 import java.util.List;
 
 /**
  * Collection of utility functions in order to deal with lists.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public final class CollectionUtils {
     /** Constructor in utility class should not be called. */
     private CollectionUtils() {
     }
 
     /**
      * Returns a view of a list made up of the n adjacent neighbors of an element
      * and the element itself.
      * <p>
      * For example, assuming the list is something like [1, 2, 3, 4, 5, 6],
      * then getAdjacentTo(3, 1) will yield [2, 3, 4].
      * </p>
      * <p>
      * The returned list is backed by this list, so non-structural changes in the
      * returned list are reflected in this list, and vice-versa. The returned list
      * supports all of the optional list operations supported by this list.
      * </p>
      * <p>
      *
      * @param <T> the type of the list elements
      * @param list the list to use
      * @param index the index of the element to get the adjacent neighbors for
      * @param num the number of neighbors (on each side) to include
      * @return a view based on the specified parameters
      */
     public static <T> List<T> getAdjacentTo(final List<T> list, final int index,
             final int num) {
 
         /* The num adjacent neighbors of an element intersected with the list's bounds */
         final Range<Integer> range = Ranges.closed(index - num, index + num).
                 intersection(Ranges.closed(0, list.size() - 1));
 
         return list.subList(range.lowerEndpoint(), range.upperEndpoint() + 1);
     }
 
     /**
      * Returns a view of a list made up of the n adjacent neighbors of an element
      * and the element itself.
      *
      * If the element does not have any neighbors, null will be inserted.
      *
      * @param <T> the type of the list elements
      * @param list the list to use
      * @param index the index of the element to get the adjacent neighbors for
      * @param num the number of neighbors (on each side) to include
      * @return a view based on the specified parameters
      */
     public static <T> List<T> getNullPaddedAdjacentTo(final List<T> list, final int index,
             final int num) {
        final List<T> paddingNulls = Lists.newArrayList();
         for (int i = 0; i < num; i++) {
             paddingNulls.add(null);
         }
         return getAdjacentTo(Lists.newArrayList(
                 Iterables.concat(paddingNulls, list, paddingNulls)), index + num, num);
     }
 
     /**
      * Check if two lists have the same distinct values.
      * This is a Convenience method to work around UIMA's insufficient type system.
      *
      * @param <T> the type of both lists
      * @param list1 list to compare
      * @param list2 list to compare
      * @return true if both lists have the same distinct values
      */
     public static <T> boolean listAsSetEquals(final List<T> list1,
             final List<T> list2) {
         return Sets.newHashSet(list1).equals(Sets.newHashSet(list2));
     }
 }
