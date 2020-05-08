 /*******************************************************************************
  * Copyright (c) 2010 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 
 package com.buglabs.osgi.log;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Queue;
 
 /**
  * A queue type that silently truncates older (FIFO) elements after size has been reached.
  * This class decorates  java.util.LinkedList.
  * 
  * @author kgilmer
  *
  */
 class FixedSizeQueue implements Queue<Object> {
 	private final LinkedList<Object> list;
 	private final int size;
 
 	/**
 	 * @param size number of elements to store before removing older entries.  Must be 2 our greater.
 	 */
 	public FixedSizeQueue(int size) {
 		if (size < 2) {
 			throw new IllegalArgumentException("Size of queue must be at least 2.");
 		}
 		
 		this.size = size;
 		list = new LinkedList<Object>();
 	}
 	
 	/**
 	 * @param size number of elements to store before removing older entries.
 	 * @param c collection to add.  Collection may be truncated if size is greater than size.
 	 */
 	public FixedSizeQueue(int size, Collection<Object> c) {
 		if (size < 2) {
 			throw new IllegalArgumentException("Size of queue must be at least 2.");
 		}
 		
 		this.size = size;
 		list = new LinkedList<Object>(c);
 		checkAndRemove();
 	}
 
 	public void add(int index, Object element) {
 		list.add(index, element);
 		checkAndRemove();
 	}
 
 	public boolean add(Object e) {
 		boolean o = list.add(e);
 		checkAndRemove();
 		return o;
 	}
 
 	/**
 	 * Reduce the size of the list until it matches the defined size.
 	 */
	private synchronized void checkAndRemove() {
 		while (list.size() > size) {
 			list.removeFirst();
 		}
 	}
 
 	public boolean addAll(Collection<? extends Object> c) {
 		//TODO it would be more efficient for large collections to only insert up to the size of the queue.
 		boolean b = list.addAll(c);
 		checkAndRemove();
 		return b;
 	}
 
 	public boolean addAll(int index, Collection<? extends Object> c) {
 		//TODO it would be more efficient for large collections to only insert up to the size of the queue.
 		return list.addAll(index, c);
 	}
 
 	public void addFirst(Object e) {
 		list.addFirst(e);
 		checkAndRemove();
 	}
 
 	public void addLast(Object e) {
 		list.addLast(e);
 		checkAndRemove();
 	}
 
 	public void clear() {
 		list.clear();
 	}
 
 	public Object clone() {
 		return list.clone();
 	}
 
 	public boolean contains(Object o) {
 		return list.contains(o);
 	}
 
 	public boolean containsAll(Collection<?> c) {
 		return list.containsAll(c);
 	}
 
 	public Iterator<Object> descendingIterator() {
 		return list.descendingIterator();
 	}
 
 	public Object element() {
 		return list.element();
 	}
 
 	public boolean equals(Object arg0) {
 		return list.equals(arg0);
 	}
 
 	public Object get(int index) {
 		return list.get(index);
 	}
 
 	public Object getFirst() {
 		return list.getFirst();
 	}
 
 	public Object getLast() {
 		return list.getLast();
 	}
 
 	public int hashCode() {
 		return list.hashCode();
 	}
 
 	public int indexOf(Object o) {
 		return list.indexOf(o);
 	}
 
 	public boolean isEmpty() {
 		return list.isEmpty();
 	}
 
 	public Iterator<Object> iterator() {
 		return list.iterator();
 	}
 
 	public int lastIndexOf(Object o) {
 		return list.lastIndexOf(o);
 	}
 
 	public ListIterator<Object> listIterator() {
 		return list.listIterator();
 	}
 
 	public ListIterator<Object> listIterator(int index) {
 		return list.listIterator(index);
 	}
 
 	public boolean offer(Object e) {
 		if (list.size() >= size) {
 			return false;
 		}
 		return list.offer(e);
 	}
 
 	public boolean offerFirst(Object e) {
 		if (list.size() >= size) {
 			return false;
 		}
 		return list.offerFirst(e);
 	}
 
 	public boolean offerLast(Object e) {
 		if (list.size() >= size) {
 			return false;
 		}
 		return list.offerLast(e);
 	}
 
 	public Object peek() {
 		return list.peek();
 	}
 
 	public Object peekFirst() {
 		return list.peekFirst();
 	}
 
 	public Object peekLast() {
 		return list.peekLast();
 	}
 
 	public Object poll() {
 		return list.poll();
 	}
 
 	public Object pollFirst() {
 		return list.pollFirst();
 	}
 
 	public Object pollLast() {
 		return list.pollLast();
 	}
 
 	public Object pop() {
 		return list.pop();
 	}
 
 	public void push(Object e) {
 		list.push(e);
 	}
 
 	public Object remove() {
 		return list.remove();
 	}
 
 	public Object remove(int index) {
 		return list.remove(index);
 	}
 
 	public boolean remove(Object o) {
 		return list.remove(o);
 	}
 
 	public boolean removeAll(Collection<?> c) {
 		return list.removeAll(c);
 	}
 
 	public Object removeFirst() {
 		return list.removeFirst();
 	}
 
 	public boolean removeFirstOccurrence(Object o) {
 		return list.removeFirstOccurrence(o);
 	}
 
 	public Object removeLast() {
 		return list.removeLast();
 	}
 
 	public boolean removeLastOccurrence(Object o) {
 		return list.removeLastOccurrence(o);
 	}
 
 	public boolean retainAll(Collection<?> c) {
 		return list.retainAll(c);
 	}
 
 	public Object set(int index, Object element) {
 		Object o = list.set(index, element);
 		checkAndRemove();
 		return o;
 	}
 
 	public int size() {
 		return list.size();
 	}
 
 	public List<Object> subList(int fromIndex, int toIndex) {
 		return list.subList(fromIndex, toIndex);
 	}
 
 	public Object[] toArray() {
 		return list.toArray();
 	}
 
 	public <T> T[] toArray(T[] a) {
 		return list.toArray(a);
 	}
 
 	public String toString() {
 		return list.toString();
 	}
 }
