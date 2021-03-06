 //
 // $Id$
 //
 // samskivert library - useful routines for java programs
 // Copyright (C) 2001-2007 Michael Bayne
 //
 // This library is free software; you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published
 // by the Free Software Foundation; either version 2.1 of the License, or
 // (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 package com.samskivert.util;
 
 import java.io.Serializable;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Provides an {@link IntSet} implementation using a sorted array of integers to maintain the
  * contents of the set.
  */
 public class ArrayIntSet extends AbstractIntSet
     implements Cloneable, Serializable
 {
     /**
      * Construct an ArrayIntSet with the specified starting values.
      */
     public ArrayIntSet (int[] values)
     {
         this(values.length);
         _size = _values.length;
         System.arraycopy(values, 0, _values, 0, _size);
         Arrays.sort(_values);
         removeDuplicates();
     }
 
     /**
      * Construct an ArrayIntSet with the specified starting values.
      *
      * @throws NullPointerException if the collection contains any null values.
      */
     public ArrayIntSet (Collection<Integer> values)
     {
         this(values.size());
         _size = values.size();
         Iterator<Integer> iter = values.iterator();
         for (int ii = 0; iter.hasNext(); ii++) {
             _values[ii] = iter.next().intValue();
         }
         Arrays.sort(_values);
         removeDuplicates();
     }
 
     /**
      * Construct an ArrayIntSet of the specified initial capacity.
      */
     public ArrayIntSet (int initialCapacity)
     {
         _values = new int[initialCapacity];
     }
 
     /**
      * Constructs an empty set with the default initial capacity.
      */
     public ArrayIntSet ()
     {
         this(DEFAULT_CAPACITY);
     }
 
     /**
      * Add all of the values in the supplied array to the set.
      *
      * @param values elements to be added to this set.
      *
      * @return <tt>true</tt> if this set did not already contain all of the specified elements.
      */
     public boolean add (int[] values)
     {
         boolean modified = false;
         int vlength = values.length;
         for (int i = 0; i < vlength; i++) {
             modified = (add(values[i]) || modified);
         }
         return modified;
     }
 
     /**
      * Removes all values in the supplied array from the set. Any values that are in the array but
      * not in the set are simply ignored.
      *
      * @param values elements to be removed from the set.
      *
      * @return <tt>true</tt> if this set contained any of the specified elements (which will have
      * been removed).
      */
     public boolean remove (int[] values)
     {
         boolean modified = false;
         int vcount = values.length;
         for (int i = 0; i < vcount; i++) {
             modified = (remove(values[i]) || modified);
         }
         return modified;
     }
 
     /**
      * Returns the element at the specified index. Note that the elements in the set are unordered
      * and could change order after insertion or removal. This method is useful only for accessing
      * elements of a static set (and has the desirable property of allowing access to the values in
      * this set without having to create integer objects).
      */
     public int get (int index)
     {
         if (index >= _size) {
             throw new IndexOutOfBoundsException("" + index + " >= " + _size);
         }
         return _values[index];
     }
 
     /**
      * Serializes this int set into an array at the specified offset. The array must be large
      * enough to hold all the integers in our set at the offset specified.
      *
      * @return the array passed in.
      */
     public int[] toIntArray (int[] target, int offset)
     {
         System.arraycopy(_values, 0, target, offset, _size);
         return target;
     }
 
     /**
      * Creates an array of shorts from the contents of this set. Any values outside the range of a
      * short will be truncated by way of a cast.
      */
     public short[] toShortArray ()
     {
         short[] values = new short[_size];
         for (int ii = 0; ii < _size; ii++) {
             values[ii] = (short)_values[ii];
         }
         return values;
     }
 
     @Override // from interface IntSet
     public boolean contains (int value)
     {
         return (binarySearch(value) >= 0);
     }
 
     @Override // from interface IntSet
     public boolean add (int value)
     {
         int index = binarySearch(value);
         if (index >= 0) {
             return false;
         }
 
         // convert the return value into the insertion point
         index += 1;
         index *= -1;
 
         // expand the values array if necessary, leaving room for the newly added element
         int valen = _values.length;
         int[] source = _values;
         if (valen == _size) {
            _values = new int[Math.max(DEFAULT_CAPACITY, valen*2)];
             System.arraycopy(source, 0, _values, 0, index);
         }
 
         // shift and insert
         if (_size > index) {
             System.arraycopy(source, index, _values, index+1, _size-index);
         }
         _values[index] = value;
 
         // increment our size
         _size += 1;
 
         return true;
     }
 
     @Override // from interface IntSet
     public boolean remove (int value)
     {
         int index = binarySearch(value);
         if (index < 0) {
             return false;
         }
         _size--;
         if ((_values.length > DEFAULT_CAPACITY) && (_size < _values.length/8)) {
             // if we're using less than 1/8 of our capacity, shrink by half
             int[] newVals = new int[_values.length/2];
             System.arraycopy(_values, 0, newVals, 0, index);
             System.arraycopy(_values, index+1, newVals, index, _size-index);
             _values = newVals;
 
         } else {
             // shift entries past the removed one downwards
             System.arraycopy(_values, index+1, _values, index, _size-index);
             //_values[_size] = 0;
         }
         return true;
     }
 
     // from interface IntSet
     public Interator interator ()
     {
         return new AbstractInterator() {
             public boolean hasNext () {
                 return (_pos < _size);
             }
 
             public int nextInt () {
                 if (_pos >= _size) {
                     throw new NoSuchElementException();
                } else {
                    return _values[_pos++];
                 }
             }
 
             @Override public void remove () {
                if (_pos == 0) {
                     throw new IllegalStateException();
                 }
                // does not correctly return IllegalStateException if
                // remove() is called twice in a row...
                 System.arraycopy(_values, _pos, _values, _pos - 1, _size - _pos);
                 _pos--;
                 _size--; //_values[--_size] = 0;
             }
 
             protected int _pos;
         };
     }
 
     @Override // from interface IntSet
     public int[] toIntArray ()
     {
         int[] values = new int[_size];
         System.arraycopy(_values, 0, values, 0, _size);
         return values;
     }
 
     @Override // from AbstractSet<Integer>
     public int size ()
     {
         return _size;
     }
 
     @Override // from AbstractIntSet
     public boolean retainAll (Collection<?> c)
     {
         if (c instanceof IntSet) {
             IntSet other = (IntSet)c;
             int removals = 0;
 
             // go through our array sliding all elements in the union
             // toward the front; overwriting any elements that were to be
             // removed
             for (int ii = 0; ii < _size; ii++) {
                 if (other.contains(_values[ii])) {
                     if (removals != 0) {
                         _values[ii - removals] = _values[ii];
                     }
                 } else {
                     removals++;
                 }
             }
 
             _size -= removals;
             return (removals > 0);
         }
         return super.retainAll(c);
     }
 
     @Override // from AbstractSet<Integer>
     public void clear ()
     {
         _size = 0;
         //Arrays.fill(_values, 0); // not necessary
     }
 
     @Override // from AbstractSet<Integer>
     public boolean equals (Object o)
     {
         // use an optimized equality test for another ArrayIntSet
         if (o instanceof ArrayIntSet) {
             ArrayIntSet other = (ArrayIntSet)o;
             if (other._size != _size) {
                 return false;
             }
             // we can't use Arrays.equals() because we only want to compare the first _size values
             for (int ii = 0; ii < _size; ii++) {
                 if (_values[ii] != other._values[ii]) {
                     return false;
                 }
             }
             return true;
         }
         return super.equals(o);
     }
 
     @Override // from AbstractSet<Integer>
     public int hashCode ()
     {
         int h = 0;
         for (int ii = 0; ii < _size; ii++) {
             h += _values[ii];
         }
         return h;
     }
 
     @Override // from AbstractSet<Integer>
     public Object clone ()
     {
         try {
             ArrayIntSet nset = (ArrayIntSet)super.clone();
             nset._values = _values.clone();
             return nset;
 
         } catch (CloneNotSupportedException cnse) {
             throw new AssertionError(cnse); // won't happen; we're Cloneable
         }
     }
 
     /**
      * Performs a binary search on our values array, looking for the specified value. Swiped from
      * <code>java.util.Arrays</code> because those wankers didn't provide a means by which to
      * perform a binary search on a subset of an array.
      */
     protected int binarySearch (int key)
     {
 	int low = 0;
 	int high = _size-1;
 
 	while (low <= high) {
 	    int mid = (low + high) >> 1;
 	    int midVal = _values[mid];
 
 	    if (midVal < key) {
 		low = mid + 1;
 	    } else if (midVal > key) {
 		high = mid - 1;
 	    } else {
 		return mid; // key found
             }
 	}
 
 	return -(low + 1);  // key not found.
     }
 
     /**
      * Removes duplicates from our internal array. Only used by our constructors when initializing
      * from a potentially duplicate-containing source array or collection.
      */
     protected void removeDuplicates ()
     {
         if (_size > 1) {
             int last = _values[0];
             for (int ii = 1; ii < _size; ) {
                 if (_values[ii] == last) { // shift everything down 1
                     _size--;
                     System.arraycopy(_values, ii + 1, _values, ii, _size - ii);
                 } else {
                     last = _values[ii++];
                 }
             }
         }
     }
 
     /** An array containing the values in this set. */
     protected int[] _values;
 
     /** The number of elements in this set. */
     protected int _size;
 
     /** The default initial capacity of this set. */
     protected static final int DEFAULT_CAPACITY = 16;
 
     /** Change this if the fields or inheritance hierarchy ever changes (extremely unlikely). */
     private static final long serialVersionUID = 1;
 }
