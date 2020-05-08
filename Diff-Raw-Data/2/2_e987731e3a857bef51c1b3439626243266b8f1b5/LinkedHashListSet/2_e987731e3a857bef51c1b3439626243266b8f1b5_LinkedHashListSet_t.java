 /**
  * The MIT License (MIT)
  * 
  * Copyright (c) 2012 Kazuki Hamasaki
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal in the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY,WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.github.ashphy.LinkedHashListSet;
 
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 /**
 * This collection is implemented {@link List} and {@link Set}, based on {@link LinkedHashSet}
  * 
  * @author Kazuki Hamasaki <ne.vivam.si.abis@gmail.com>
  */
 public class LinkedHashListSet<E> extends LinkedHashSet<E> implements List<E> {
 
 	private static final long serialVersionUID = 3121174076840105963L;
 
 	protected transient int modifiedCount = 0;
 
 	/**
 	 * Constructs a new, empty linked hash set with the default initial capacity
 	 * (16) and load factor (0.75).
 	 */
 	public LinkedHashListSet() {
 		super();
 	}
 
 	/**
 	 * Constructs a new linked hash set with the same elements as the specified
 	 * collection. The linked hash set is created with an initial capacity
 	 * sufficient to hold the elements in the specified collection and the
 	 * default load factor (0.75).
 	 * 
 	 * @param c
 	 *            the collection whose elements are to be placed into this set
 	 */
 	public LinkedHashListSet(Collection<? extends E> c) {
 		super(c);
 	}
 
 	/**
 	 * Constructs a new, empty linked hash set with the specified initial
 	 * capacity and load factor.
 	 * 
 	 * @param initialCapacity the initial capacity of the linked hash set
 	 * @param loadFactor the load factor of the linked hash set
 	 */
 	public LinkedHashListSet(int initialCapacity, float loadFactor) {
 		super(initialCapacity, loadFactor);
 	}
 
 	/**
 	 * Constructs a new, empty linked hash set with the specified initial
 	 * capacity and the default load factor (0.75).
 	 * 
 	 * @param initialCapacity
 	 *            the initial capacity of the LinkedHashSet
 	 */
 	public LinkedHashListSet(int initialCapacity) {
 		super(initialCapacity);
 	}
 
 	/** Add all element. However the index is ignored. */
 	@Override
 	public boolean addAll(int index, Collection<? extends E> c) {
 		boolean changed = addAll(c);
 		if (changed) {
 			modifiedCount++;
 		}
 		return changed;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public E get(int index) {
 
 		checkIndexBounds(index);
 
 		int cursor = 0;
 		for (E element : this) {
 			if (index == cursor++) {
 				return element;
 			}
 		}
 
 		throw new IndexOutOfBoundsException();
 	}
 
 	/**
 	 * This operation is unsupported.
 	 */
 	@Override
 	public E set(int index, E element) {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Inserts the specified element. However the index is ignored.
 	 */
 	@Override
 	public void add(int index, E element) {
 		add(element);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public E remove(int index) {
 
 		checkIndexBounds(index);
 		modifiedCount++;
 
 		int cursor = 0;
 		E removeObject = null;
 
 		for (E element : this) {
 			if (index == cursor++) {
 				removeObject = element;
 				break;
 			}
 		}
 
 		remove(removeObject);
 		return removeObject;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public int indexOf(Object o) {
 
 		int cursor = 0;
 		for (E element : this) {
 			if (element.equals(o)) {
 				return cursor;
 			}
 			cursor++;
 		}
 
 		return -1;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public int lastIndexOf(Object o) {
 		return indexOf(o);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public ListIterator<E> listIterator() {
 		return new LinkedHashListSetListIterator();
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public ListIterator<E> listIterator(int index) {
 		checkIndexBounds(index);
 		return new LinkedHashListSetListIterator(index);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public List<E> subList(int fromIndex, int toIndex) {
 		return new SubList(fromIndex, toIndex);
 	}
 
 	/**
 	 * Check that the index is in the bounds of this set
 	 * 
 	 * @param index
 	 */
 	private void checkIndexBounds(int index) {
 		if (index < 0 || index > size()) {
 			throw new IndexOutOfBoundsException();
 		}
 	}
 
 	/** {@inheritDoc} */
 	public boolean add(E e) {
 		boolean exists = super.add(e);
 		if (exists) {
 			modifiedCount++;
 		}
 		
 		return exists;
 	};
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean addAll(Collection<? extends E> c) {
 		boolean changed = super.addAll(c);
 		if (changed) {
 			modifiedCount++;
 		}
 		return changed;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void clear() {
 		modifiedCount++;
 		super.clear();
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean removeAll(Collection<?> c) {
 		boolean changed = super.removeAll(c);
 		if (changed) {
 			modifiedCount++;
 		}
 		return changed;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean remove(Object o) {
 		boolean exist = super.remove(o);
 		if (exist) {
 			modifiedCount++;
 		}
 		return exist;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean retainAll(Collection<?> c) {
 		boolean changed = super.retainAll(c);
 		if (changed) {
 			modifiedCount++;
 		}
 		return changed;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public Object clone() {
 		@SuppressWarnings("unchecked")
 		LinkedHashListSet<E> clone = (LinkedHashListSet<E>) super.clone();
 		clone.modifiedCount = 0;
 		return clone;
 	}
 
 	/**
 	 * ListIterator for LinkedHashListSet
 	 * 
 	 * @author Kazuki Hamasaki <ne.vivam.si.abis@gmail.com>
 	 * 
 	 * @param <E>
 	 */
 	private class LinkedHashListSetListIterator implements ListIterator<E> {
 
 		/**
 		 * Offset of iterator
 		 */
 		int offset = 0;
 		
 		/**
 		 * Size of this iterator
 		 */
 		int size = 0;
 		
 		/** Position of the iterator */
 		int cursor = 0;
 
 		/**
 		 * For checking the modification of the list
 		 */
 		int expectedModifiedCount = 0;
 
 		/**
 		 * List for efficient iteration
 		 */
 		List<E> innerList;
 
 		public LinkedHashListSetListIterator() {
 			this(0);
 		}
 
 		public LinkedHashListSetListIterator(int index) {
 			this(index, 0, LinkedHashListSet.this.size());
 		}
 		
 		public LinkedHashListSetListIterator(int iterateStartIndex, int fromIndex, int toIndex) {
 			
 			if(fromIndex < 0 || toIndex > LinkedHashListSet.this.size() || fromIndex > toIndex) {
 				throw new IndexOutOfBoundsException();
 			}
 			
 			this.cursor = iterateStartIndex;
 			this.size = toIndex - fromIndex;
 		}
 
 		private void createInnnerList() {
 			this.innerList = new LinkedList<>(LinkedHashListSet.this);
 			this.expectedModifiedCount = modifiedCount;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public boolean hasNext() {
 			return cursor != size;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public E next() {
 			checkIndexBounds(cursor + offset);
 			checkModification();
 			E next = innerList.get(cursor + offset);
 			cursor++;
 			return next;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public boolean hasPrevious() {
 			return cursor != 0;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public E previous() {
 			checkIndexBounds(cursor + offset);
 			checkModification();
 
 			cursor--;
 			E previous = innerList.get(cursor + offset);
 			return previous;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public int nextIndex() {
 			return cursor;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public int previousIndex() {
 			return cursor - 1;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public void remove() {
 			checkModification();
 			checkIndexBounds(cursor + offset);
 			LinkedHashListSet.this.remove(cursor + offset);
 			createInnnerList();
 		}
 
 		/**
 		 * This operation is unsupported
 		 */
 		@Override
 		public void set(E e) {
 			throw new UnsupportedOperationException();
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public void add(E e) {
 			checkModification();
 			LinkedHashListSet.this.add(e);
 			cursor++;
 			size++;
 			createInnnerList();
 		}
 
 		/**
 		 * Check that the index is in the bounds of this set
 		 * 
 		 * @param index
 		 */
 		private void checkIndexBounds(int index) {
 			index += offset;
 			if (index  < 0 || index > size) {
 				throw new IndexOutOfBoundsException();
 			}
 		}
 		
 		private void checkModification() {
 			if (expectedModifiedCount != modifiedCount) {
 				throw new ConcurrentModificationException();
 			}
 		}
 	}
 	
 	private class SubList extends LinkedHashListSet<E> {
 
 		private static final long serialVersionUID = -2893633131499834098L;
 		
 		/** Offset index from original  */
 		private final int offset = 0;
 		
 		/** Size of sublist */
 		private int size = 0;
 		
 		public SubList(int fromIndex, int toIndex) {
 			setModificationcount();
 			
 			if(fromIndex < 0 || toIndex > LinkedHashListSet.this.size() || fromIndex > toIndex) {
 				throw new IndexOutOfBoundsException();
 			}
 			
 			this.size = toIndex - fromIndex;
 		}
 		
 		@Override
 		public E get(int index) {
 			checkModification();
 			return LinkedHashListSet.this.get(index + offset);
 		}
 		
 		@Override
 		public int size() {
 			checkModification();
 			return size;
 		}
 
 		@Override
 		public void add(int index, E element) {
 			add(element);
 		};
 		
 		public boolean add(E element) {
 			checkModification();
 			
 			if(!contains(element)) {
 				size++;
 			}
 			
 			boolean changed = LinkedHashListSet.this.add(element);
 			setModificationcount();
 			
 			return changed;
 		};
 		
 		@Override
 		public E remove(int index) {
 			
 			checkIndexBounds(index + offset);
 			checkModification();
 			
 			E removeObject = LinkedHashListSet.this.remove(index + offset);
 			setModificationcount();
 			size--;
 			return removeObject;
 		}
 
 		@Override
 		public boolean addAll(int index, Collection<? extends E> c) {
 			return addAll(size, c);
 		}
 		
 		@Override
 		public boolean addAll(Collection<? extends E> c) {
 			
 			if(c.size() == 0) {
 				return false;
 			}
 			
 			checkModification();
 			
 			int originalSize = size();
 			boolean changed = LinkedHashListSet.this.addAll(c);
 			size += size() - originalSize;
 			setModificationcount();
 			return changed;
 		}
 		
 		@Override
 		public ListIterator<E> listIterator() {
 			return listIterator();
 		}
 		
 		@Override
 		public ListIterator<E> listIterator(int index) {
 			checkModification();
 			return new LinkedHashListSetListIterator(index, offset, offset + size);
 		}
 		
 		@Override
 		public void clear() {
 			int clearSize = size;
 			for (int i = 0; i < clearSize; i++) {
 				remove(offset);
 			}
 		}
 		
 		private void checkModification() {
 			if (LinkedHashListSet.this.modifiedCount != this.modifiedCount) {
 				throw new ConcurrentModificationException();
 			}
 		}
 		
 		private void setModificationcount() {
 			this.modifiedCount = LinkedHashListSet.this.modifiedCount;
 		}
 		
 		@SuppressWarnings("unchecked")
 		public <T extends Object> T[] toArray(T[] a) {
 			return (T[]) toArray();
 		};
 		
 		@Override
 		public Object[] toArray() {
 			Object[] array = new  Object[size];
 			
 			for (int i = 0; i < size; i++) {
 				array[i] = get(i);
 			}
 			
 			return array;
 		}
 	}
 }
