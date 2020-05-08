/*
 * Copyright (C) 2006-2007 MOSTRARE INRIA Project
 * 
 * This file is part of XCRF, an implementation of CRFs for trees (http://treecrf.gforge.inria.fr)
 * 
 * XCRF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * XCRF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XCRF; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mostrare.coltAdaptation.list;

import java.util.Comparator;

import mostrare.coltAdaptation.function.IntArrayListProcedure;
import cern.colt.list.AbstractList;
import cern.colt.list.IntArrayList;

/**
 * Resizable list holding <code>IntArrayList</code> elements; implemented with arrays. This class
 * is an adaptation of <code>cern.colt.list.ObjectArrayList</code> of the colt API.
 * 
 * @author missi
 */
public class IntArrayListArrayList extends AbstractList
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3451067542237627807L;

	/**
	 * The array buffer into which the elements of the list are stored. The capacity of the list is
	 * the length of this array buffer.
	 * 
	 * @serial
	 */
	protected IntArrayList[]	elements;

	/**
	 * The size of the list.
	 * 
	 * @serial
	 */
	protected int				size;

	/**
	 * Constructs an empty list.
	 */
	public IntArrayListArrayList()
	{
		this(10);
	}

	/**
	 * Constructs a list containing the specified elements. The initial size and capacity of the
	 * list is the length of the array. <b>WARNING:</b> For efficiency reasons and to keep memory
	 * usage low, <b>the array is not copied</b>. So if subsequently you modify the specified array
	 * directly via the [] operator, be sure you know what you're doing.
	 * 
	 * @param elements
	 *            the array to be backed by the the constructed list
	 */
	public IntArrayListArrayList(IntArrayList[] elements)
	{
		elements(elements);
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * 
	 * @param initialCapacity
	 *            the number of elements the receiver can hold without auto-expanding itself by
	 *            allocating new internal memory.
	 */
	public IntArrayListArrayList(int initialCapacity)
	{
		this(new IntArrayList[initialCapacity]);
		size = 0;
	}

	/**
	 * Appends the specified element to the end of this list.
	 * 
	 * @param element
	 *            element to be appended to this list.
	 */
	public void add(IntArrayList element)
	{
		if (size == elements.length)
			ensureCapacity(size + 1);
		elements[size++] = element;
	}

	/**
	 * Appends the part of the specified list between <code>from</code> (inclusive) and
	 * <code>to</code> (inclusive) to the receiver.
	 * 
	 * @param other
	 *            the list to be added to the receiver.
	 * @param from
	 *            the index of the first element to be appended (inclusive).
	 * @param to
	 *            the index of the last element to be appended (inclusive).
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>other.size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=other.size())</code>).
	 */
	public void addAllOfFromTo(IntArrayListArrayList other, int from, int to)
	{
		beforeInsertAllOfFromTo(size, other, from, to);
	}

	/**
	 * Inserts the specified element before the specified position into the receiver. Shifts the
	 * element currently at that position (if any) and any subsequent elements to the right.
	 * 
	 * @param index
	 *            index before which the specified element is to be inserted (must be in [0,size]).
	 * @param element
	 *            element to be inserted.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>index &lt; 0 || index &gt; size()</code>).
	 */
	public void beforeInsert(int index, IntArrayList element)
	{
		// overridden for performance only.
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		ensureCapacity(size + 1);
		System.arraycopy(elements, index, elements, index + 1, size - index);
		elements[index] = element;
		size++;
	}

	/**
	 * Inserts the part of the specified list between <code>otherFrom</code> (inclusive) and
	 * <code>otherTo</code> (inclusive) before the specified position into the receiver. Shifts
	 * the element currently at that position (if any) and any subsequent elements to the right.
	 * 
	 * @param index
	 *            index before which to insert first element from the specified list (must be in
	 *            [0,size])..
	 * @param other
	 *            list of which a part is to be inserted into the receiver.
	 * @param from
	 *            the index of the first element to be inserted (inclusive).
	 * @param to
	 *            the index of the last element to be inserted (inclusive).
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>other.size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=other.size())</code>).
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>index &lt; 0 || index &gt; size()</code>).
	 */
	public void beforeInsertAllOfFromTo(int index, IntArrayListArrayList other, int from, int to)
	{
		int length = to - from + 1;
		this.beforeInsertDummies(index, length);
		this.replaceFromToWithFrom(index, index + length - 1, other, from);
	}

	/**
	 * Inserts length dummies before the specified position into the receiver. Shifts the element
	 * currently at that position (if any) and any subsequent elements to the right.
	 * 
	 * @param index
	 *            index before which to insert dummies (must be in [0,size])..
	 * @param length
	 *            number of dummies to be inserted.
	 */
	@Override
	protected void beforeInsertDummies(int index, int length)
	{
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		if (length > 0)
		{
			ensureCapacity(size + length);
			System.arraycopy(elements, index, elements, index + length, size - index);
			size += length;
		}
	}

	/**
	 * Searches the receiver for the specified value using the binary search algorithm. The receiver
	 * must be sorted into ascending order according to the <i>natural ordering</i> of its elements
	 * (as by the sort method) prior to making this call. If it is not sorted, the results are
	 * undefined: in particular, the call may enter an infinite loop. If the receiver contains
	 * multiple elements equal to the specified IntArrayList, there is no guarantee which instance
	 * will be found.
	 * 
	 * @param key
	 *            the value to be searched for.
	 * @return index of the search key, if it is contained in the receiver; otherwise,
	 *         <code>(-(<i>insertion point</i>) - 1)</code>. The <i>insertion point</i> is
	 *         defined as the the point at which the value would be inserted into the receiver: the
	 *         index of the first element greater than the key, or <code>receiver.size()</code>,
	 *         if all elements in the receiver are less than the specified key. Note that this
	 *         guarantees that the return value will be &gt;= 0 if and only if the key is found.
	 * @see Comparable
	 * @see java.util.Arrays
	 */
	public int binarySearch(IntArrayList key)
	{
		return this.binarySearchFromTo(key, 0, size - 1);
	}

	/**
	 * Searches the receiver for the specified value using the binary search algorithm. The receiver
	 * must be sorted into ascending order according to the <i>natural ordering</i> of its elements
	 * (as by the sort method) prior to making this call. If it is not sorted, the results are
	 * undefined: in particular, the call may enter an infinite loop. If the receiver contains
	 * multiple elements equal to the specified IntArrayList, there is no guarantee which instance
	 * will be found.
	 * 
	 * @param key
	 *            the value to be searched for.
	 * @param from
	 *            the leftmost search position, inclusive.
	 * @param to
	 *            the rightmost search position, inclusive.
	 * @return index of the search key, if it is contained in the receiver; otherwise,
	 *         <code>(-(<i>insertion point</i>) - 1)</code>. The <i>insertion point</i> is
	 *         defined as the the point at which the value would be inserted into the receiver: the
	 *         index of the first element greater than the key, or <code>receiver.size()</code>,
	 *         if all elements in the receiver are less than the specified key. Note that this
	 *         guarantees that the return value will be &gt;= 0 if and only if the key is found.
	 * @see Comparable
	 * @see java.util.Arrays
	 */
	public int binarySearchFromTo(IntArrayList key, int from, int to)
	{
		int low = from;
		int high = to;

		while (low <= high)
		{
			int mid = (low + high) / 2;
			IntArrayList midVal = elements[mid];
			int cmp = ((Comparable<IntArrayList>) midVal).compareTo(key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Searches the receiver for the specified value using the binary search algorithm. The receiver
	 * must be sorted into ascending order according to the specified comparator. All elements in
	 * the range must be <i>mutually comparable</i> by the specified comparator (that is,
	 * <code>c.compare(e1, e2)</code> must not throw a <code>ClassCastException</code> for any
	 * elements <code>e1</code> and <code>e2</code> in the range).
	 * <p>
	 * If the receiver is not sorted, the results are undefined: in particular, the call may enter
	 * an infinite loop. If the receiver contains multiple elements equal to the specified
	 * IntArrayList, there is no guarantee which instance will be found.
	 * 
	 * @param key
	 *            the value to be searched for.
	 * @param from
	 *            the leftmost search position, inclusive.
	 * @param to
	 *            the rightmost search position, inclusive.
	 * @param comparator
	 *            the comparator by which the receiver is sorted.
	 * @throws ClassCastException
	 *             if the receiver contains elements that are not <i>mutually comparable</i> using
	 *             the specified comparator.
	 * @return index of the search key, if it is contained in the receiver; otherwise,
	 *         <code>(-(<i>insertion point</i>) - 1)</code>. The <i>insertion point</i> is
	 *         defined as the the point at which the value would be inserted into the receiver: the
	 *         index of the first element greater than the key, or <code>receiver.size()</code>,
	 *         if all elements in the receiver are less than the specified key. Note that this
	 *         guarantees that the return value will be &gt;= 0 if and only if the key is found.
	 * @see cern.colt.Sorting
	 * @see java.util.Arrays
	 * @see java.util.Comparator
	 */
	public int binarySearchFromTo(IntArrayList key, int from, int to,
			java.util.Comparator comparator)
	{
		return cern.colt.Sorting.binarySearchFromTo(this.elements, key, from, to, comparator);
	}

	/**
	 * Returns a copy of the receiver such that the copy and the receiver <i>share</i> the same
	 * elements, but do not share the same array to index them; So modifying an IntArrayList in the
	 * copy modifies the IntArrayList in the receiver and vice versa; However, structurally
	 * modifying the copy (for example changing its size, setting other IntArrayLists at indexes,
	 * etc.) does not affect the receiver and vice versa.
	 * 
	 * @return a copy of the receiver.
	 */
	@Override
	public Object clone()
	{
		IntArrayListArrayList v = (IntArrayListArrayList) super.clone();
		v.elements = (IntArrayList[]) elements.clone();
		return v;
	}

	/**
	 * Returns true if the receiver contains the specified element. Tests for equality or identity
	 * as specified by testForEquality.
	 * 
	 * @param elem
	 *            element to search for.
	 * @param testForEquality
	 *            if true -> test for equality, otherwise for identity.
	 */
	public boolean contains(IntArrayList elem, boolean testForEquality)
	{
		return indexOfFromTo(elem, 0, size - 1, testForEquality) >= 0;
	}

	/**
	 * Returns a copy of the receiver; call <code>clone()</code> and casts the result. Returns a
	 * copy such that the copy and the receiver <i>share</i> the same elements, but do not share
	 * the same array to index them; So modifying an IntArrayList in the copy modifies the
	 * IntArrayList in the receiver and vice versa; However, structurally modifying the copy (for
	 * example changing its size, setting other IntArrayLists at indexes, etc.) does not affect the
	 * receiver and vice versa.
	 * 
	 * @return a copy of the receiver.
	 */
	public IntArrayListArrayList copy()
	{
		return (IntArrayListArrayList) clone();
	}

	/**
	 * Deletes the first element from the receiver that matches the specified element. Does nothing,
	 * if no such matching element is contained. Tests elements for equality or identity as
	 * specified by <code>testForEquality</code>. When testing for equality, two elements
	 * <code>e1</code> and <code>e2</code> are <i>equal</i> if <code>(e1==null ? e2==null :
	 * e1.equals(e2))</code>.)
	 * 
	 * @param testForEquality
	 *            if true -> tests for equality, otherwise for identity.
	 * @param element
	 *            the element to be deleted.
	 */
	public void delete(IntArrayList element, boolean testForEquality)
	{
		int index = indexOfFromTo(element, 0, size - 1, testForEquality);
		if (index >= 0)
			removeFromTo(index, index);
	}

	/**
	 * Returns the elements currently stored, including invalid elements between size and capacity,
	 * if any. <b>WARNING:</b> For efficiency reasons and to keep memory usage low, <b>the array is
	 * not copied</b>. So if subsequently you modify the returned array directly via the []
	 * operator, be sure you know what you're doing.
	 * 
	 * @return the elements currently stored.
	 */
	public IntArrayList[] elements()
	{
		return elements;
	}

	/**
	 * Sets the receiver's elements to be the specified array (not a copy of it). The size and
	 * capacity of the list is the length of the array. <b>WARNING:</b> For efficiency reasons and
	 * to keep memory usage low, <b>the array is not copied</b>. So if subsequently you modify the
	 * specified array directly via the [] operator, be sure you know what you're doing.
	 * 
	 * @param elements
	 *            the new elements to be stored.
	 * @return the receiver itself.
	 */
	public IntArrayListArrayList elements(IntArrayList[] elements)
	{
		this.elements = elements;
		this.size = elements.length;
		return this;
	}

	/**
	 * Ensures that the receiver can hold at least the specified number of elements without needing
	 * to allocate new internal memory. If necessary, allocates new internal memory and increases
	 * the capacity of the receiver.
	 * 
	 * @param minCapacity
	 *            the desired minimum capacity.
	 */
	public void ensureCapacity(int minCapacity)
	{
		elements = mostrare.coltAdaptation.ArraysForIntArrayList.ensureCapacity(elements,
				minCapacity);
	}

	/**
	 * Compares the specified IntArrayList with the receiver for equality. Returns true if and only
	 * if the specified IntArrayList is also an IntArrayListArrayList, both lists have the same
	 * size, and all corresponding pairs of elements in the two lists are equal. In other words, two
	 * lists are defined to be equal if they contain the same elements in the same order. Two
	 * elements <code>e1</code> and <code>e2</code> are <i>equal</i> if
	 * <code>(e1==null ? e2==null :
	 * e1.equals(e2))</code>.)
	 * 
	 * @param otherObj
	 *            the IntArrayList to be compared for equality with the receiver.
	 * @return true if the specified IntArrayList is equal to the receiver.
	 */
	@Override
	public boolean equals(Object otherObj)
	{ // delta
		return equals(otherObj, true);
	}

	/**
	 * Compares the specified IntArrayList with the receiver for equality. Returns true if and only
	 * if the specified IntArrayList is also an IntArrayListArrayList, both lists have the same
	 * size, and all corresponding pairs of elements in the two lists are the same. In other words,
	 * two lists are defined to be equal if they contain the same elements in the same order. Tests
	 * elements for equality or identity as specified by <code>testForEquality</code>. When
	 * testing for equality, two elements <code>e1</code> and <code>e2</code> are <i>equal</i>
	 * if <code>(e1==null ? e2==null :
	 * e1.equals(e2))</code>.)
	 * 
	 * @param otherObj
	 *            the IntArrayList to be compared for equality with the receiver.
	 * @param testForEquality
	 *            if true -> tests for equality, otherwise for identity.
	 * @return true if the specified IntArrayList is equal to the receiver.
	 */
	public boolean equals(Object otherObj, boolean testForEquality)
	{ // delta
		if (!(otherObj instanceof IntArrayListArrayList))
		{
			return false;
		}
		if (this == otherObj)
			return true;
		if (otherObj == null)
			return false;
		IntArrayListArrayList other = (IntArrayListArrayList) otherObj;
		if (elements == other.elements())
			return true;
		if (size != other.size())
			return false;

		IntArrayList[] otherElements = other.elements();
		IntArrayList[] theElements = elements;
		if (!testForEquality)
		{
			for (int i = size; --i >= 0;)
			{
				if (theElements[i] != otherElements[i])
					return false;
			}
		}
		else
		{
			for (int i = size; --i >= 0;)
			{
				if (!(theElements[i] == null ? otherElements[i] == null : theElements[i]
						.equals(otherElements[i])))
					return false;
			}
		}

		return true;

	}

	/**
	 * Sets the specified range of elements in the specified array to the specified value.
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be filled with the specified value.
	 * @param to
	 *            the index of the last element (inclusive) to be filled with the specified value.
	 * @param val
	 *            the value to be stored in the specified elements of the receiver.
	 */
	public void fillFromToWith(int from, int to, IntArrayList val)
	{
		checkRangeFromTo(from, to, this.size);
		for (int i = from; i <= to;)
			setQuick(i++, val);
	}

	/**
	 * Applies a procedure to each element of the receiver, if any. Starts at index 0, moving
	 * rightwards.
	 * 
	 * @param procedure
	 *            the procedure to be applied. Stops iteration if the procedure returns
	 *            <code>false</code>, otherwise continues.
	 * @return <code>false</code> if the procedure stopped before all elements where iterated
	 *         over, <code>true</code> otherwise.
	 */
	public boolean forEach(IntArrayListProcedure procedure)
	{
		IntArrayList[] theElements = elements;
		int theSize = size;

		for (int i = 0; i < theSize;)
			if (!procedure.apply(theElements[i++]))
				return false;
		return true;
	}

	/**
	 * Returns the element at the specified position in the receiver.
	 * 
	 * @param index
	 *            index of element to return.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (index &lt; 0 || index &gt;= size()).
	 */
	public IntArrayList get(int index)
	{
		if (index >= size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		return elements[index];
	}

	/**
	 * Returns the element at the specified position in the receiver; <b>WARNING:</b> Does not
	 * check preconditions. Provided with invalid parameters this method may return invalid elements
	 * without throwing any exception! <b>You should only use this method when you are absolutely
	 * sure that the index is within bounds.</b> Precondition (unchecked):
	 * <code>index &gt;= 0 && index &lt; size()</code>.
	 * 
	 * @param index
	 *            index of element to return.
	 */
	public IntArrayList getQuick(int index)
	{
		return elements[index];
	}

	/**
	 * Returns the index of the first occurrence of the specified element. Returns <code>-1</code>
	 * if the receiver does not contain this element. Tests for equality or identity as specified by
	 * testForEquality.
	 * 
	 * @param testForEquality
	 *            if <code>true</code> -> test for equality, otherwise for identity.
	 * @return the index of the first occurrence of the element in the receiver; returns
	 *         <code>-1</code> if the element is not found.
	 */
	public int indexOf(IntArrayList element, boolean testForEquality)
	{
		return this.indexOfFromTo(element, 0, size - 1, testForEquality);
	}

	/**
	 * Returns the index of the first occurrence of the specified element. Returns <code>-1</code>
	 * if the receiver does not contain this element. Searches between <code>from</code>,
	 * inclusive and <code>to</code>, inclusive. Tests for equality or identity as specified by
	 * <code>testForEquality</code>.
	 * 
	 * @param element
	 *            element to search for.
	 * @param from
	 *            the leftmost search position, inclusive.
	 * @param to
	 *            the rightmost search position, inclusive.
	 * @param testForEquality
	 *            if </code>true</code> -> test for equality, otherwise for identity.
	 * @return the index of the first occurrence of the element in the receiver; returns <code>-1</code>
	 *         if the element is not found.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to ||
	 *                to&gt;=size())</code>).
	 */
	public int indexOfFromTo(IntArrayList element, int from, int to, boolean testForEquality)
	{
		if (size == 0)
			return -1;
		checkRangeFromTo(from, to, size);

		IntArrayList[] theElements = elements;
		if (testForEquality && element != null)
		{
			for (int i = from; i <= to; i++)
			{
				if (element.equals(theElements[i]))
				{
					return i;
				} // found
			}

		}
		else
		{
			for (int i = from; i <= to; i++)
			{
				if (element == theElements[i])
				{
					return i;
				} // found
			}
		}
		return -1; // not found
	}

	/**
	 * Determines whether the receiver is sorted ascending, according to the <i>natural ordering</i>
	 * of its elements. All elements in this range must implement the <code>Comparable</code>
	 * interface. Furthermore, all elements in this range must be <i>mutually comparable</i> (that
	 * is, <code>e1.compareTo(e2)</code> must not throw a <code>ClassCastException</code> for
	 * any elements <code>e1</code> and <code>e2</code> in the array).
	 * <p>
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be sorted.
	 * @param to
	 *            the index of the last element (inclusive) to be sorted.
	 * @return <code>true</code> if the receiver is sorted ascending, <code>false</code>
	 *         otherwise.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	public boolean isSortedFromTo(int from, int to)
	{
		if (size == 0)
			return true;
		checkRangeFromTo(from, to, size);

		IntArrayList[] theElements = elements;
		for (int i = from + 1; i <= to; i++)
		{
			if (((Comparable) theElements[i]).compareTo((Comparable) theElements[i - 1]) < 0)
				return false;
		}
		return true;
	}

	/**
	 * Returns the index of the last occurrence of the specified element. Returns <code>-1</code>
	 * if the receiver does not contain this element. Tests for equality or identity as specified by
	 * <code>testForEquality</code>.
	 * 
	 * @param element
	 *            the element to be searched for.
	 * @param testForEquality
	 *            if <code>true</code> -> test for equality, otherwise for identity.
	 * @return the index of the last occurrence of the element in the receiver; returns
	 *         <code>-1</code> if the element is not found.
	 */
	public int lastIndexOf(IntArrayList element, boolean testForEquality)
	{
		return lastIndexOfFromTo(element, 0, size - 1, testForEquality);
	}

	/**
	 * Returns the index of the last occurrence of the specified element. Returns <code>-1</code>
	 * if the receiver does not contain this element. Searches beginning at <code>to</code>,
	 * inclusive until <code>from</code>, inclusive. Tests for equality or identity as specified
	 * by <code>testForEquality</code>.
	 * 
	 * @param element
	 *            element to search for.
	 * @param from
	 *            the leftmost search position, inclusive.
	 * @param to
	 *            the rightmost search position, inclusive.
	 * @param testForEquality
	 *            if <code>true</code> -> test for equality, otherwise for identity.
	 * @return the index of the last occurrence of the element in the receiver; returns
	 *         <code>-1</code> if the element is not found.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	public int lastIndexOfFromTo(IntArrayList element, int from, int to, boolean testForEquality)
	{
		if (size == 0)
			return -1;
		checkRangeFromTo(from, to, size);

		IntArrayList[] theElements = elements;
		if (testForEquality && element != null)
		{
			for (int i = to; i >= from; i--)
			{
				if (element.equals(theElements[i]))
				{
					return i;
				} // found
			}

		}
		else
		{
			for (int i = to; i >= from; i--)
			{
				if (element == theElements[i])
				{
					return i;
				} // found
			}
		}
		return -1; // not found
	}

	/**
	 * Sorts the specified range of the receiver into ascending order, according to the <i>natural
	 * ordering</i> of its elements. All elements in this range must implement the
	 * <code>Comparable</code> interface. Furthermore, all elements in this range must be
	 * <i>mutually comparable</i> (that is, <code>e1.compareTo(e2)</code> must not throw a
	 * <code>ClassCastException</code> for any elements <code>e1</code> and <code>e2</code> in
	 * the array).
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a
	 * result of the sort.
	 * <p>
	 * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest
	 * element in the low sublist is less than the lowest element in the high sublist). This
	 * algorithm offers guaranteed n*log(n) performance, and can approach linear performance on
	 * nearly sorted lists.
	 * <p>
	 * <b>You should never call this method unless you are sure that this particular sorting
	 * algorithm is the right one for your data set.</b> It is generally better to call
	 * <code>sort()</code> or <code>sortFromTo(...)</code> instead, because those methods
	 * automatically choose the best sorting algorithm.
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be sorted.
	 * @param to
	 *            the index of the last element (inclusive) to be sorted.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	@Override
	public void mergeSortFromTo(int from, int to)
	{
		if (size == 0)
			return;
		checkRangeFromTo(from, to, size);
		java.util.Arrays.sort(elements, from, to + 1);
	}

	/**
	 * Sorts the receiver according to the order induced by the specified comparator. All elements
	 * in the range must be <i>mutually comparable</i> by the specified comparator (that is,
	 * <code>c.compare(e1, e2)</code> must not throw a <code>ClassCastException</code> for any
	 * elements <code>e1</code> and <code>e2</code> in the range).
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a
	 * result of the sort.
	 * <p>
	 * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest
	 * element in the low sublist is less than the lowest element in the high sublist). This
	 * algorithm offers guaranteed n*log(n) performance, and can approach linear performance on
	 * nearly sorted lists.
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be sorted.
	 * @param to
	 *            the index of the last element (inclusive) to be sorted.
	 * @param c
	 *            the comparator to determine the order of the receiver.
	 * @throws ClassCastException
	 *             if the array contains elements that are not <i>mutually comparable</i> using the
	 *             specified comparator.
	 * @throws IllegalArgumentException
	 *             if <code>fromIndex &gt; toIndex</code>
	 * @throws ArrayIndexOutOfBoundsException
	 *             if <code>fromIndex &lt; 0</code> or <code>toIndex &gt; a.length</code>
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 * @see Comparator
	 */
	public void mergeSortFromTo(int from, int to, java.util.Comparator<IntArrayList> c)
	{
		if (size == 0)
			return;
		checkRangeFromTo(from, to, size);
		java.util.Arrays.sort(elements, from, to + 1, c);
	}

	/**
	 * Returns a new list of the part of the receiver between <code>from</code>, inclusive, and
	 * <code>to</code>, inclusive.
	 * 
	 * @param from
	 *            the index of the first element (inclusive).
	 * @param to
	 *            the index of the last element (inclusive).
	 * @return a new list
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	public IntArrayListArrayList partFromTo(int from, int to)
	{
		if (size == 0)
			return new IntArrayListArrayList(0);

		checkRangeFromTo(from, to, size);

		IntArrayList[] part = new IntArrayList[to - from + 1];
		System.arraycopy(elements, from, part, 0, to - from + 1);
		return new IntArrayListArrayList(part);
	}

	/**
	 * Sorts the specified range of the receiver into ascending order, according to the <i>natural
	 * ordering</i> of its elements. All elements in this range must implement the
	 * <code>Comparable</code> interface. Furthermore, all elements in this range must be
	 * <i>mutually comparable</i> (that is, <code>e1.compareTo(e2)</code> must not throw a
	 * <code>ClassCastException</code> for any elements <code>e1</code> and <code>e2</code> in
	 * the array).
	 * <p>
	 * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas
	 * McIlroy's "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P.
	 * 1249-1265 (November 1993). This algorithm offers n*log(n) performance on many data sets that
	 * cause other quicksorts to degrade to quadratic performance.
	 * <p>
	 * <b>You should never call this method unless you are sure that this particular sorting
	 * algorithm is the right one for your data set.</b> It is generally better to call
	 * <code>sort()</code> or <code>sortFromTo(...)</code> instead, because those methods
	 * automatically choose the best sorting algorithm.
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be sorted.
	 * @param to
	 *            the index of the last element (inclusive) to be sorted.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	@Override
	public void quickSortFromTo(int from, int to)
	{
		if (size == 0)
			return;
		checkRangeFromTo(from, to, size);
		cern.colt.Sorting.quickSort(elements, from, to + 1);
	}

	/**
	 * Sorts the receiver according to the order induced by the specified comparator. All elements
	 * in the range must be <i>mutually comparable</i> by the specified comparator (that is,
	 * <code>c.compare(e1, e2)</code> must not throw a <code>ClassCastException</code> for any
	 * elements <code>e1</code> and <code>e2</code> in the range).
	 * <p>
	 * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas
	 * McIlroy's "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P.
	 * 1249-1265 (November 1993). This algorithm offers n*log(n) performance on many data sets that
	 * cause other quicksorts to degrade to quadratic performance.
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be sorted.
	 * @param to
	 *            the index of the last element (inclusive) to be sorted.
	 * @param c
	 *            the comparator to determine the order of the receiver.
	 * @throws ClassCastException
	 *             if the array contains elements that are not <i>mutually comparable</i> using the
	 *             specified comparator.
	 * @throws IllegalArgumentException
	 *             if <code>fromIndex &gt; toIndex</code>
	 * @throws ArrayIndexOutOfBoundsException
	 *             if <code>fromIndex &lt; 0</code> or <code>toIndex &gt; a.length</code>
	 * @see Comparator
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	public void quickSortFromTo(int from, int to, java.util.Comparator c)
	{
		if (size == 0)
			return;
		checkRangeFromTo(from, to, size);
		cern.colt.Sorting.quickSort(elements, from, to + 1, c);
	}

	/**
	 * Removes from the receiver all elements that are contained in the specified list. Tests for
	 * equality or identity as specified by <code>testForEquality</code>.
	 * 
	 * @param other
	 *            the other list.
	 * @param testForEquality
	 *            if <code>true</code> -> test for equality, otherwise for identity.
	 * @return <code>true</code> if the receiver changed as a result of the call.
	 */
	public boolean removeAll(IntArrayListArrayList other, boolean testForEquality)
	{
		if (other.size == 0)
			return false; // nothing to do
		int limit = other.size - 1;
		int j = 0;
		IntArrayList[] theElements = elements;
		for (int i = 0; i < size; i++)
		{
			if (other.indexOfFromTo(theElements[i], 0, limit, testForEquality) < 0)
				theElements[j++] = theElements[i];
		}

		boolean modified = (j != size);
		setSize(j);
		return modified;
	}

	/**
	 * Removes from the receiver all elements whose index is between <code>from</code>, inclusive
	 * and <code>to</code>, inclusive. Shifts any succeeding elements to the left (reduces their
	 * index). This call shortens the list by <code>(to - from + 1)</code> elements.
	 * 
	 * @param from
	 *            index of first element to be removed.
	 * @param to
	 *            index of last element to be removed.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	@Override
	public void removeFromTo(int from, int to)
	{
		checkRangeFromTo(from, to, size);
		int numMoved = size - to - 1;
		if (numMoved >= 0)
		{
			System.arraycopy(elements, to + 1, elements, from, numMoved);
			fillFromToWith(from + numMoved, size - 1, null); // delta
		}
		int width = to - from + 1;
		if (width > 0)
			size -= width;
	}

	/**
	 * Replaces a number of elements in the receiver with the same number of elements of another
	 * list. Replaces elements in the receiver, between <code>from</code> (inclusive) and
	 * <code>to</code> (inclusive), with elements of <code>other</code>, starting from
	 * <code>otherFrom</code> (inclusive).
	 * 
	 * @param from
	 *            the position of the first element to be replaced in the receiver
	 * @param to
	 *            the position of the last element to be replaced in the receiver
	 * @param other
	 *            list holding elements to be copied into the receiver.
	 * @param otherFrom
	 *            position of first element within other list to be copied.
	 */
	public void replaceFromToWithFrom(int from, int to, IntArrayListArrayList other, int otherFrom)
	{
		int length = to - from + 1;
		if (length > 0)
		{
			checkRangeFromTo(from, to, size);
			checkRangeFromTo(otherFrom, otherFrom + length - 1, other.size);
			System.arraycopy(other.elements, otherFrom, elements, from, length);
		}
	}

	/**
	 * Replaces the part between <code>from</code> (inclusive) and <code>to</code> (inclusive)
	 * with the other list's part between <code>otherFrom</code> and <code>otherTo</code>.
	 * Powerful (and tricky) method! Both parts need not be of the same size (part A can both be
	 * smaller or larger than part B). Parts may overlap. Receiver and other list may (but most not)
	 * be identical. If <code>from &gt; to</code>, then inserts other part before
	 * <code>from</code>.
	 * 
	 * @param from
	 *            the first element of the receiver (inclusive)
	 * @param to
	 *            the last element of the receiver (inclusive)
	 * @param other
	 *            the other list (may be identical with receiver)
	 * @param otherFrom
	 *            the first element of the other list (inclusive)
	 * @param otherTo
	 *            the last element of the other list (inclusive)
	 *            <p>
	 *            <b>Examples:</b>
	 * 
	 * <pre>
	 *          a=[0, 1, 2, 3, 4, 5, 6, 7]
	 *          b=[50, 60, 70, 80, 90]
	 *          a.R(...)=a.replaceFromToWithFromTo(...)
	 *         
	 *          a.R(3,5,b,0,4)--&gt;[0, 1, 2, 50, 60, 70, 80, 90, 6, 7]
	 *          a.R(1,6,b,0,4)--&gt;[0, 50, 60, 70, 80, 90, 7]
	 *          a.R(0,6,b,0,4)--&gt;[50, 60, 70, 80, 90, 7]
	 *          a.R(3,5,b,1,2)--&gt;[0, 1, 2, 60, 70, 6, 7]
	 *          a.R(1,6,b,1,2)--&gt;[0, 60, 70, 7]
	 *          a.R(0,6,b,1,2)--&gt;[60, 70, 7]
	 *          a.R(5,3,b,0,4)--&gt;[0, 1, 2, 3, 4, 50, 60, 70, 80, 90, 5, 6, 7]
	 *          a.R(5,0,b,0,4)--&gt;[0, 1, 2, 3, 4, 50, 60, 70, 80, 90, 5, 6, 7]
	 *          a.R(5,3,b,1,2)--&gt;[0, 1, 2, 3, 4, 60, 70, 5, 6, 7]
	 *          a.R(5,0,b,1,2)--&gt;[0, 1, 2, 3, 4, 60, 70, 5, 6, 7]
	 *         
	 *          Extreme cases:
	 *          a.R(5,3,b,0,0)--&gt;[0, 1, 2, 3, 4, 50, 5, 6, 7]
	 *          a.R(5,3,b,4,4)--&gt;[0, 1, 2, 3, 4, 90, 5, 6, 7]
	 *          a.R(3,5,a,0,1)--&gt;[0, 1, 2, 0, 1, 6, 7]
	 *          a.R(3,5,a,3,5)--&gt;[0, 1, 2, 3, 4, 5, 6, 7]
	 *          a.R(3,5,a,4,4)--&gt;[0, 1, 2, 4, 6, 7]
	 *          a.R(5,3,a,0,4)--&gt;[0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 6, 7]
	 *          a.R(0,-1,b,0,4)--&gt;[50, 60, 70, 80, 90, 0, 1, 2, 3, 4, 5, 6, 7]
	 *          a.R(0,-1,a,0,4)--&gt;[0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 6, 7]
	 *          a.R(8,0,a,0,4)--&gt;[0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4]
	 * </pre>
	 */
	public void replaceFromToWithFromTo(int from, int to, IntArrayListArrayList other,
			int otherFrom, int otherTo)
	{
		if (otherFrom > otherTo)
		{
			throw new IndexOutOfBoundsException("otherFrom: " + otherFrom + ", otherTo: " + otherTo);
		}
		if (this == other && to - from != otherTo - otherFrom)
		{ // avoid
			// stumbling
			// over my
			// own feet
			replaceFromToWithFromTo(from, to, partFromTo(otherFrom, otherTo), 0, otherTo
					- otherFrom);
			return;
		}

		int length = otherTo - otherFrom + 1;
		int diff = length;
		int theLast = from - 1;

		// System.out.println("from="+from);
		// System.out.println("to="+to);
		// System.out.println("diff="+diff);

		if (to >= from)
		{
			diff -= (to - from + 1);
			theLast = to;
		}

		if (diff > 0)
		{
			beforeInsertDummies(theLast + 1, diff);
		}
		else
		{
			if (diff < 0)
			{
				removeFromTo(theLast + diff, theLast - 1);
			}
		}

		if (length > 0)
		{
			System.arraycopy(other.elements, otherFrom, elements, from, length);
		}
	}

	/**
	 * Replaces the part of the receiver starting at <code>from</code> (inclusive) with all the
	 * elements of the specified collection. Does not alter the size of the receiver. Replaces
	 * exactly <code>Math.max(0,Math.min(size()-from, other.size()))</code> elements.
	 * 
	 * @param from
	 *            the index at which to copy the first element from the specified collection.
	 * @param other
	 *            Collection to replace part of the receiver
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (index &lt; 0 || index &gt;= size()).
	 */
	@Override
	public void replaceFromWith(int from, java.util.Collection other)
	{
		checkRange(from, size);
		java.util.Iterator e = other.iterator();
		int index = from;
		int limit = Math.min(size - from, other.size());
		for (int i = 0; i < limit; i++)
			elements[index++] = (IntArrayList) e.next(); // delta
	}

	/**
	 * Retains (keeps) only the elements in the receiver that are contained in the specified other
	 * list. In other words, removes from the receiver all of its elements that are not contained in
	 * the specified other list. Tests for equality or identity as specified by
	 * <code>testForEquality</code>.
	 * 
	 * @param other
	 *            the other list to test against.
	 * @param testForEquality
	 *            if <code>true</code> -> test for equality, otherwise for identity.
	 * @return <code>true</code> if the receiver changed as a result of the call.
	 */
	public boolean retainAll(IntArrayListArrayList other, boolean testForEquality)
	{
		if (other.size == 0)
		{
			if (size == 0)
				return false;
			setSize(0);
			return true;
		}

		int limit = other.size - 1;
		int j = 0;
		IntArrayList[] theElements = elements;

		for (int i = 0; i < size; i++)
		{
			if (other.indexOfFromTo(theElements[i], 0, limit, testForEquality) >= 0)
				theElements[j++] = theElements[i];
		}

		boolean modified = (j != size);
		setSize(j);
		return modified;
	}

	/**
	 * Reverses the elements of the receiver. Last becomes first, second last becomes second first,
	 * and so on.
	 */
	@Override
	public void reverse()
	{
		IntArrayList tmp;
		int limit = size / 2;
		int j = size - 1;

		IntArrayList[] theElements = elements;
		for (int i = 0; i < limit;)
		{ // swap
			tmp = theElements[i];
			theElements[i++] = theElements[j];
			theElements[j--] = tmp;
		}
	}

	/**
	 * Replaces the element at the specified position in the receiver with the specified element.
	 * 
	 * @param index
	 *            index of element to replace.
	 * @param element
	 *            element to be stored at the specified position.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (index &lt; 0 || index &gt;= size()).
	 */
	public void set(int index, IntArrayList element)
	{
		if (index >= size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		elements[index] = element;
	}

	/**
	 * Replaces the element at the specified position in the receiver with the specified element;
	 * <b>WARNING:</b> Does not check preconditions. Provided with invalid parameters this method
	 * may access invalid indexes without throwing any exception! <b>You should only use this method
	 * when you are absolutely sure that the index is within bounds.</b> Precondition (unchecked):
	 * <code>index &gt;= 0 && index &lt; size()</code>.
	 * 
	 * @param index
	 *            index of element to replace.
	 * @param element
	 *            element to be stored at the specified position.
	 */
	public void setQuick(int index, IntArrayList element)
	{
		elements[index] = element;
	}

	/**
	 * Randomly permutes the part of the receiver between <code>from</code> (inclusive) and
	 * <code>to</code> (inclusive).
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be permuted.
	 * @param to
	 *            the index of the last element (inclusive) to be permuted.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<code>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</code>).
	 */
	@Override
	public void shuffleFromTo(int from, int to)
	{
		if (size == 0)
			return;
		checkRangeFromTo(from, to, size);

		cern.jet.random.Uniform gen = new cern.jet.random.Uniform(new cern.jet.random.engine.DRand(
				new java.util.Date()));
		IntArrayList tmpElement;
		IntArrayList[] theElements = elements;
		int random;
		for (int i = from; i < to; i++)
		{
			random = gen.nextIntFromTo(i, to);

			// swap(i, random)
			tmpElement = theElements[random];
			theElements[random] = theElements[i];
			theElements[i] = tmpElement;
		}
	}

	/**
	 * Returns the number of elements contained in the receiver.
	 * 
	 * @return the number of elements contained in the receiver.
	 */
	@Override
	public int size()
	{
		return size;
	}

	/**
	 * Returns a list which is a concatenation of <code>times</code> times the receiver.
	 * 
	 * @param times
	 *            the number of times the receiver shall be copied.
	 */
	public IntArrayListArrayList times(int times)
	{
		IntArrayListArrayList newList = new IntArrayListArrayList(times * size);
		for (int i = times; --i >= 0;)
		{
			newList.addAllOfFromTo(this, 0, size() - 1);
		}
		return newList;
	}

	/**
	 * Returns an array containing all of the elements in the receiver in the correct order. The
	 * runtime type of the returned array is that of the specified array. If the receiver fits in
	 * the specified array, it is returned therein. Otherwise, a new array is allocated with the
	 * runtime type of the specified array and the size of the receiver.
	 * <p>
	 * If the receiver fits in the specified array with room to spare (i.e., the array has more
	 * elements than the receiver), the element in the array immediately following the end of the
	 * receiver is set to null. This is useful in determining the length of the receiver
	 * <em>only</em> if the caller knows that the receiver does not contain any null elements.
	 * 
	 * @param array
	 *            the array into which the elements of the receiver are to be stored, if it is big
	 *            enough; otherwise, a new array of the same runtime type is allocated for this
	 *            purpose.
	 * @return an array containing the elements of the receiver.
	 * @exception ArrayStoreException
	 *                the runtime type of <code>array</code> is not a supertype of the runtime
	 *                type of every element in the receiver.
	 */
	public IntArrayList[] toArray(IntArrayList array[])
	{
		if (array.length < size)
			array = (IntArrayList[]) java.lang.reflect.Array.newInstance(array.getClass()
					.getComponentType(), size);

		IntArrayList[] theElements = elements;
		for (int i = size; --i >= 0;)
			array[i] = theElements[i];

		if (array.length > size)
			array[size] = null;

		return array;
	}

	/**
	 * Returns a <code>java.util.ArrayList</code> containing all the elements in the receiver.
	 */
	@Override
	public java.util.ArrayList toList()
	{
		int mySize = size();
		IntArrayList[] theElements = elements;
		java.util.ArrayList<IntArrayList> list = new java.util.ArrayList<IntArrayList>(mySize);
		for (int i = 0; i < mySize; i++)
			list.add(theElements[i]);
		return list;
	}

	/**
	 * Returns a string representation of the receiver, containing the String representation of each
	 * element.
	 */
	@Override
	public String toString()
	{
		return cern.colt.Arrays.toString(partFromTo(0, size() - 1).elements());
	}

	/**
	 * Trims the capacity of the receiver to be the receiver's current size. Releases any superfluos
	 * internal memory. An application can use this operation to minimize the storage of the
	 * receiver.
	 */
	@Override
	public void trimToSize()
	{
		elements = mostrare.coltAdaptation.ArraysForIntArrayList.trimToCapacity(elements, size());
	}
}
