 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import java.util.*;
 
 
 /**
  * Compares two collections, returning a list of the additions, changes, and
  * deletions between them. A <code>Comparator</code> may be passed as an
  * argument to the constructor, and will thus be used. If not provided, the
  * initial value in the <code>a</code> ("from") collection will be looked at to
  * see if it supports the <code>Comparable</code> interface. If so, its
  * <code>equals</code> and <code>compareTo</code> methods will be invoked on the
  * instances in the "from" and "to" collections; otherwise, for speed, hash
  * codes from the objects will be used instead for comparison.
  * <p/>
  * <p>The file FileDiff.java shows an example usage of this class, in an
  * application similar to the Unix "diff" program.</p>
  *
  * @author Jeff Pace (jpace at incava dot org) - original author
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxDiff {
 
     /**
      * Represents a difference, as used in <code>FxDiff</code>. A difference consists
      * of two pairs of starting and ending points, each pair representing either the
      * "from" or the "to" collection passed to <code>Diff</code>. If an ending point
      * is -1, then the difference was either a deletion or an addition. For example,
      * if <code>getDeletedEnd()</code> returns -1, then the difference represents an
      * addition.
      */
     public static class Difference {
         public static final int NONE = -1;
 
         /**
          * The point at which the deletion starts.
          */
         private int delStart = NONE;
 
         /**
          * The point at which the deletion ends.
          */
         private int delEnd = NONE;
 
         /**
          * The point at which the addition starts.
          */
         private int addStart = NONE;
 
         /**
          * The point at which the addition ends.
          */
         private int addEnd = NONE;
 
         /**
          * Creates the difference for the given start and end points for the
          * deletion and addition.
          *
          * @param delStart point at which the deletion starts
          * @param delEnd   point at which the deletion ends
          * @param addStart point at which the addition starts
          * @param addEnd   point at which the addition ends
          */
         public Difference(int delStart, int delEnd, int addStart, int addEnd) {
             this.delStart = delStart;
             this.delEnd = delEnd;
             this.addStart = addStart;
             this.addEnd = addEnd;
         }
 
         /**
          * The point at which the deletion starts, if any. A value equal to
          * <code>NONE</code> means this is an addition.
          *
          * @return point at which the deletion starts
          */
         public int getDeletedStart() {
             return delStart;
         }
 
         /**
          * The point at which the deletion ends, if any. A value equal to
          * <code>NONE</code> means this is an addition.
          *
          * @return point at which the deletion ends
          */
         public int getDeletedEnd() {
             return delEnd;
         }
 
         /**
          * The point at which the addition starts, if any. A value equal to
          * <code>NONE</code> means this must be an addition.
          *
          * @return point at which the addition starts
          */
         public int getAddedStart() {
             return addStart;
         }
 
         /**
          * The point at which the addition ends, if any. A value equal to
          * <code>NONE</code> means this must be an addition.
          *
          * @return point at which the addition ends
          */
         public int getAddedEnd() {
             return addEnd;
         }
 
         /**
          * Sets the point as deleted. The start and end points will be modified to
          * include the given line.
          *
          * @param line line number
          */
         public void setDeleted(int line) {
             delStart = Math.min(line, delStart);
             delEnd = Math.max(line, delEnd);
         }
 
         /**
          * Sets the point as added. The start and end points will be modified to
          * include the given line.
          *
          * @param line line number
          */
         public void setAdded(int line) {
             addStart = Math.min(line, addStart);
             addEnd = Math.max(line, addEnd);
         }
 
         /**
          * Does this difference contain an addition?
          *
          * @return add?
          */
         public boolean isAdd() {
             return addEnd != NONE;
         }
 
         /**
          * Does this difference contain a deletion?
          *
          * @return delete?
          */
         public boolean isDelete() {
             return delEnd != NONE;
         }
 
         /**
          * Compares this object to the other for equality. Both objects must be of
          * type Difference, with the same starting and ending points.
          */
         @Override
         public boolean equals(Object obj) {
             if (obj instanceof Difference) {
                 Difference other = (Difference) obj;
 
                 return (delStart == other.delStart &&
                         delEnd == other.delEnd &&
                         addStart == other.addStart &&
                         addEnd == other.addEnd);
             } else {
                 return false;
             }
         }
 
         @Override
         public int hashCode() {
             int result;
             result = delStart;
             result = 31 * result + delEnd;
             result = 31 * result + addStart;
             result = 31 * result + addEnd;
             return result;
         }
 
         /**
          * Returns a string representation of this difference.
          */
         @Override
         public String toString() {
             StringBuffer buf = new StringBuffer();
             buf.append("del: [").append(delStart).append(", ").append(delEnd).append("]");
             buf.append(" ");
             buf.append("add: [").append(addStart).append(", ").append(addEnd).append("]");
             return buf.toString();
         }
 
     }
 
     /**
      * The source array, AKA the "from" values.
      */
     protected Object[] a;
 
     /**
      * The target array, AKA the "to" values.
      */
     protected Object[] b;
 
     /**
      * The list of differences, as <code>Difference</code> instances.
      */
     protected List<Difference> diffs = new ArrayList<Difference>();
 
     /**
      * The pending, uncommitted difference.
      */
     private Difference pending;
 
     /**
      * The comparator used, if any.
      */
     private Comparator<Object> comparator;
 
     /**
      * The thresholds.
      */
     private TreeMap<Integer, Integer> thresh;
 
     /**
      * Constructs the Diff object for the two arrays, using the given comparator.
      *
      * @param a    array A
      * @param b    array B
      * @param comp comparator
      */
     public FxDiff(Object[] a, Object[] b, Comparator<Object> comp) {
         this.a = a.clone();
         this.b = b.clone();
         this.comparator = comp;
         this.thresh = null;     // created in getLongestCommonSubsequences
     }
 
     /**
      * Constructs the Diff object for the two arrays, using the default
      * comparison mechanism between the objects, such as <code>equals</code> and
      * <code>compareTo</code>.
      *
      * @param a array A
      * @param b array B
      */
     public FxDiff(Object[] a, Object[] b) {
         this(a.clone(), b.clone(), null);
     }
 
     /**
      * Constructs the Diff object for the two collections, using the given
      * comparator.
      *
      * @param a    collection A
      * @param b    collection B
      * @param comp comparator
      */
     public FxDiff(Collection a, Collection b, Comparator<Object> comp) {
         this(a.toArray(), b.toArray(), comp);
     }
 
     /**
      * Constructs the Diff object for the two collections, using the default
      * comparison mechanism between the objects, such as <code>equals</code> and
      * <code>compareTo</code>.
      *
      * @param a collection A
      * @param b collection B
      */
     public FxDiff(Collection a, Collection b) {
         this(a, b, null);
     }
 
     /**
      * Runs diff and returns the results.
      *
      * @return list of differences
      */
     public List<Difference> diff() {
         traverseSequences();
 
         // add the last difference, if pending:
         if (pending != null) {
             diffs.add(pending);
         }
 
         return diffs;
     }
 
     /**
      * Traverses the sequences, seeking the longest common subsequences,
      * invoking the methods <code>finishedA</code>, <code>finishedB</code>,
      * <code>onANotB</code>, and <code>onBNotA</code>.
      */
     protected void traverseSequences() {
         Integer[] matches = getLongestCommonSubsequences();
 
         int lastA = a.length - 1;
         int lastB = b.length - 1;
         int bi = 0;
         int ai;
 
         int lastMatch = matches.length - 1;
 
         for (ai = 0; ai <= lastMatch; ++ai) {
             Integer bLine = matches[ai];
 
             if (bLine == null) {
                 onANotB(ai, bi);
             } else {
                 while (bi < bLine) {
                     onBNotA(ai, bi++);
                 }
 
                 onMatch(ai, bi++);
             }
         }
 
         boolean calledFinishA = false;
         boolean calledFinishB = false;
 
         while (ai <= lastA || bi <= lastB) {
 
             // last A?
             if (ai == lastA + 1 && bi <= lastB) {
                 if (!calledFinishA && callFinishedA()) {
                     finishedA(lastA);
                     calledFinishA = true;
                 } else {
                     while (bi <= lastB) {
                         onBNotA(ai, bi++);
                     }
                 }
             }
 
             // last B?
             if (bi == lastB + 1 && ai <= lastA) {
                 if (!calledFinishB && callFinishedB()) {
                     finishedB(lastB);
                     calledFinishB = true;
                 } else {
                     while (ai <= lastA) {
                         onANotB(ai++, bi);
                     }
                 }
             }
 
             if (ai <= lastA) {
                 onANotB(ai++, bi);
             }
 
             if (bi <= lastB) {
                 onBNotA(ai, bi++);
             }
         }
     }
 
     /**
      * Override and return true in order to have <code>finishedA</code> invoked
      * at the last element in the <code>a</code> array.
      *
      * @return return true in order to have <code>finishedA</code> invoked
      *         at the last element in the <code>a</code> array
      */
     protected boolean callFinishedA() {
         return false;
     }
 
     /**
      * Override and return true in order to have <code>finishedB</code> invoked
      * at the last element in the <code>b</code> array.
      *
      * @return return true in order to have <code>finishedA</code> invoked
      *         at the last element in the <code>a</code> array
      */
     protected boolean callFinishedB() {
         return false;
     }
 
     /**
      * Invoked at the last element in <code>a</code>, if
      * <code>callFinishedA</code> returns true.
      *
      * @param lastA last line in A
      */
     protected void finishedA(int lastA) {
     }
 
     /**
      * Invoked at the last element in <code>b</code>, if
      * <code>callFinishedB</code> returns true.
      *
      * @param lastB last line in B
      */
     protected void finishedB(int lastB) {
     }
 
     /**
      * Invoked for elements in <code>a</code> and not in <code>b</code>.
      *
      * @param ai line in A
      * @param bi line in B
      */
     protected void onANotB(int ai, int bi) {
         if (pending == null) {
             pending = new Difference(ai, ai, bi, -1);
         } else {
             pending.setDeleted(ai);
         }
     }
 
     /**
      * Invoked for elements in <code>b</code> and not in <code>a</code>.
      *
      * @param ai line in A
      * @param bi line in B
      */
     protected void onBNotA(int ai, int bi) {
         if (pending == null) {
             pending = new Difference(ai, -1, bi, bi);
         } else {
             pending.setAdded(bi);
         }
     }
 
     /**
      * Invoked for elements matching in <code>a</code> and <code>b</code>.
      *
      * @param ai line in A
      * @param bi line in B
      */
     protected void onMatch(int ai, int bi) {
         if (pending == null) {
             // no current pending
         } else {
             diffs.add(pending);
             pending = null;
         }
     }
 
     /**
      * Compares the two objects, using the comparator provided with the
      * constructor, if any.
      *
      * @param x X
      * @param y Y
      * @return equality
      */
     protected boolean equals(Object x, Object y) {
         return comparator == null ? x.equals(y) : comparator.compare(x, y) == 0;
     }
 
     /**
      * Returns an array of the longest common subsequences.
      *
      * @return array of the longest common subsequences
      */
     public Integer[] getLongestCommonSubsequences() {
         int aStart = 0;
         int aEnd = a.length - 1;
 
         int bStart = 0;
         int bEnd = b.length - 1;
 
         TreeMap<Integer, Integer> matches = new TreeMap<Integer, Integer>();
 
         while (aStart <= aEnd && bStart <= bEnd && equals(a[aStart], b[bStart])) {
             matches.put(aStart++, bStart++);
         }
 
         while (aStart <= aEnd && bStart <= bEnd && equals(a[aEnd], b[bEnd])) {
             matches.put(aEnd--, bEnd--);
         }
 
         Map<? super Object, List<Integer>> bMatches;
         if (comparator == null) {
             if (a.length > 0 && a[0] instanceof Comparable) {
                 // this uses the Comparable interface
                 bMatches = new TreeMap<Object, List<Integer>>();
             } else {
                 // this just uses hashCode()
                 bMatches = new HashMap<Object, List<Integer>>();
             }
         } else {
             // we don't really want them sorted, but this is the only Map
             // implementation (as of JDK 1.4) that takes a comparator.
             bMatches = new TreeMap<Object, List<Integer>>(comparator);
         }
 
         for (int bi = bStart; bi <= bEnd; ++bi) {
             Object element = b[bi];
             List<Integer> positions = bMatches.get(element);
             if (positions == null) {
                 positions = new ArrayList<Integer>();
                 bMatches.put(element, positions);
             }
             positions.add(bi);
         }
 
         thresh = new TreeMap<Integer, Integer>();
         Map<Integer, Object[]> links = new HashMap<Integer, Object[]>();
 
         for (int i = aStart; i <= aEnd; ++i) {
             Object aElement = a[i]; // keygen here.
             List<Integer> positions = bMatches.get(aElement);
 
             if (positions != null) {
                 Integer k = 0;
                 ListIterator<Integer> pit = positions.listIterator(positions.size());
                 while (pit.hasPrevious()) {
                     Integer j = pit.previous();
 
                     k = insert(j, k);
 
                     if (k == null) {
                         // nothing
                     } else {
                         Object value = k > 0 ? links.get(k - 1) : null;
                         links.put(k, new Object[]{value, i, j});
                     }
                 }
             }
         }
 
         if (thresh.size() > 0) {
             Integer ti = thresh.lastKey();
             Object[] link = links.get(ti);
             while (link != null) {
                 Integer x = (Integer) link[1];
                 Integer y = (Integer) link[2];
                 matches.put(x, y);
                 link = (Object[]) link[0];
             }
         }
 
         return toArray(matches);
     }
 
     /**
      * Converts the map (indexed by java.lang.Integers) into an array.
      *
      * @param map map the convert
      * @return array
      */
     protected static Integer[] toArray(TreeMap<Integer, Integer> map) {
         int size = map.size() == 0 ? 0 : 1 + map.lastKey();
        Integer[] result = new Integer[size];
 
        for (Map.Entry<Integer, Integer> entry : map.entrySet())
            result[entry.getKey()] = entry.getValue();
        return result;
     }
 
     /**
      * Returns whether the integer is not zero (including if it is not null).
      *
      * @param i value to check
      * @return non zero
      */
     protected static boolean isNonzero(Integer i) {
         return i != null && i != 0;
     }
 
     /**
      * Returns whether the value in the map for the given index is greater than
      * the given value.
      *
      * @param index index in threshold
      * @param val   value to compare
      * @return if greater
      */
     protected boolean isGreaterThan(Integer index, Integer val) {
         Integer lhs = thresh.get(index);
         return lhs != null && val != null && lhs.compareTo(val) > 0;
     }
 
     /**
      * Returns whether the value in the map for the given index is less than
      * the given value.
      *
      * @param index index in threshold
      * @param val   value to compare
      * @return if less than
      */
     protected boolean isLessThan(Integer index, Integer val) {
         Integer lhs = thresh.get(index);
         return lhs != null && (val == null || lhs.compareTo(val) < 0);
     }
 
     /**
      * Returns the value for the greatest key in the map.
      *
      * @return last value
      */
     protected Integer getLastValue() {
         return thresh.get(thresh.lastKey());
     }
 
     /**
      * Adds the given value to the "end" of the threshold map, that is, with the
      * greatest index/key.
      *
      * @param value the value to append
      */
     protected void append(Integer value) {
         Integer addIdx;
         if (thresh.size() == 0) {
             addIdx = 0;
         } else {
             Integer lastKey = thresh.lastKey();
             addIdx = lastKey + 1;
         }
         thresh.put(addIdx, value);
     }
 
     /**
      * Inserts the given values into the threshold map.
      *
      * @param j j
      * @param k k
      * @return k
      */
     protected Integer insert(Integer j, Integer k) {
         if (isNonzero(k) && isGreaterThan(k, j) && isLessThan(k - 1, j)) {
             thresh.put(k, j);
         } else {
             int hi = -1;
 
             if (isNonzero(k)) {
                 hi = k;
             } else if (thresh.size() > 0) {
                 hi = thresh.lastKey();
             }
 
             // off the end?
             if (hi == -1 || j.compareTo(getLastValue()) > 0) {
                 append(j);
                 k = hi + 1;
             } else {
                 // binary search for insertion point:
                 int lo = 0;
 
                 while (lo <= hi) {
                     int index = (hi + lo) / 2;
                     Integer val = thresh.get(Integer.valueOf(index));
                     int cmp = j.compareTo(val);
 
                     if (cmp == 0) {
                         return null;
                     } else if (cmp > 0) {
                         lo = index + 1;
                     } else {
                         hi = index - 1;
                     }
                 }
 
                 thresh.put(lo, j);
                 k = lo;
             }
         }
         return k;
     }
 
 }
