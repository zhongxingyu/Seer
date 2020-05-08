 /*
  * caveman - A primitive collection library
  * Copyright 2011-2014 MeBigFatGuy.com
  * Copyright 2011-2014 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.caveman.proto;
 
 import com.mebigfatguy.caveman.proto.aux.CM;
 
 /**
  * The root interface in the <i><b>CM</b> collection hierarchy</i>.  A collection
  * represents a group of <b>CM</b>s, known as its <i>elements</i>.  Some
  * collections allow duplicate elements and others do not.  Some are ordered
  * and others unordered. This interface is typically used to pass collections
  * around and manipulate them where maximum generality is desired.
  */
 public interface CMCollection {
 
     /**
      * Returns the number of <b>CM</b>s in this collection.  If this collection
      * contains more than <tt>Integer.MAX_VALUE</tt> <b>CM</b>s, returns
      * <tt>Integer.MAX_VALUE</tt>.
      *
      * @return the number of <b>CM</b>s in this collection
      */
 	int size();
 
     /**
      * Returns <tt>true</tt> if this collection contains no <b>CM</b>s, otherwise <tt>false</tt>.
      *
      * @return <tt>true</tt> if this collection contains no <b>CM</b>s, otherwise <tt>false</tt>
      */
 	boolean isEmpty();
 
     /**
      * Returns <tt>true</tt> if this collection contains the specified <b>CM</b>.
      * More formally, returns <tt>true</tt> if and only if this collection
      * contains at least one element <tt>e</tt> such that
      * <tt>(o == e)</tt>.
      *
     * @param o <b>CM</b> whose presence in this collection is to be tested
      * @return <tt>true</tt> if this collection contains the specified <b>CM</b>
      */
 	boolean contains(CM item);
 
     /**
      * Returns an iterator over the <b>CM</b>s in this collection.  There are no
      * guarantees concerning the order in which the <b>CM</b>s are returned
      * (unless this collection is an instance of some class that provides a
      * guarantee).
      *
      * @return an <tt>Iterator</tt> over the <b>CM</b>s in this collection
      */
 	CMIterator iterator();
 
     /**
      * Returns an array containing all of the <b>CM</b>s in this collection.
      * If this collection makes any guarantees as to what order its <b>CM</b>s
      * are returned by its iterator, this method must return the <b>CM</b>s in
      * the same order.
      *
      * <p>The returned array will be "safe" in that no references to it are
      * maintained by this collection.  (In other words, this method must
      * allocate a new array even if this collection is backed by an array).
      * The caller is thus free to modify the returned array.
      *
      * <p>This method acts as bridge between array-based and collection-based
      * APIs.
      *
      * @return an array containing all of the <b>CM</b>s in this collection
      */
 	CM[] toArray();
 
     /**
      * Ensures that this collection contains the specified <b>CM</b> (optional
      * operation).  Returns <tt>true</tt> if this collection changed as a
      * result of the call.  (Returns <tt>false</tt> if this collection does
      * not permit duplicates and already contains the specified element.)<p>
      *
      * If a collection refuses to add a particular <b>CM</b> for any reason
      * other than that it already contains the <b>CM</b>, it <i>must</i> throw
      * an exception (rather than returning <tt>false</tt>).  This preserves
      * the invariant that a collection always contains the specified <b>CM</b>
      * after this call returns.
      *
     * @param e <b>CM</b> whose presence in this collection is to be ensured
      * @return <tt>true</tt> if this collection changed as a result of the
      *         call
      * @throws UnsupportedOperationException if the <tt>add</tt> operation
      *         is not supported by this collection
      */
 	boolean add(CM item);
 
     /**
      * Removes a single instance of the specified <b>CM</b> from this
      * collection, if it is present (optional operation).  More formally,
      * removes an <b>CM</b> <tt>e</tt> such that
      * <tt>(o == e)</tt>, if
      * this collection contains one or more such <b>CM</b>s.  Returns
      * <tt>true</tt> if this collection contained the specified <b>CM</b> (or
      * equivalently, if this collection changed as a result of the call).
      *
     * @param o <b>CM</b> to be removed from this collection, if present
      * @return <tt>true</tt> if an <b>CM</b> was removed as a result of this call
      * @throws UnsupportedOperationException if the <tt>remove</tt> operation
      *         is not supported by this collection
      */
 	boolean remove(CM item);
 
     /**
      * Removes all of the <b>CM</b>s from this collection (optional operation).
      * The collection will be empty after this method returns.
      *
      * @throws UnsupportedOperationException if the <tt>clear</tt> operation
      *         is not supported by this collection
      */
 	void clear();
 
     /**
      * Returns <tt>true</tt> if this collection contains all of the <b>CM</b>s
      * in the specified collection.
      *
      * @param  c collection to be checked for containment in this collection
      * @return <tt>true</tt> if this collection contains all of the <b>CM</b>
      *	       in the specified collection
      */
 	boolean containsAll(CMCollection c);
 
     /**
      * Adds all of the <b>CM</b>s in the specified collection to this collection
      * (optional operation).  The behavior of this operation is undefined if
      * the specified collection is modified while the operation is in progress.
      * (This implies that the behavior of this call is undefined if the
      * specified collection is this collection, and this collection is
      * nonempty.)
      *
      * @param c collection containing <b>CM</b>s to be added to this collection
      * @return <tt>true</tt> if this collection changed as a result of the call
      * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
      *         is not supported by this collection
      */
 	boolean addAll(CMCollection c);
 
     /**
      * Retains only the <b>CM</b>s in this collection that are contained in the
      * specified collection (optional operation).  In other words, removes from
      * this collection all of its <b>CM</b>s that are not contained in the
      * specified collection.
      *
      * @param c collection containing <b>CM</b>s to be retained in this collection
      * @return <tt>true</tt> if this collection changed as a result of the call
      * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
      *         is not supported by this collection
      */
 	boolean retainAll(CMCollection c);
 
     /**
      * Removes all of this collection's <b>CM</b>s that are also contained in the
      * specified collection (optional operation).  After this call returns,
      * this collection will contain no <b>CM</b>s in common with the specified
      * collection.
      *
      * @param c collection containing <b>CM</b>s to be removed from this collection
      * @return <tt>true</tt> if this collection changed as a result of the
      *         call
      * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
      *         is not supported by this collection
      */
 	boolean removeAll(CMCollection c);
 
 	/**
 	 * Retrieves an arbitrary object from the collection, for when you just need
 	 * an exemplar object. Which object is selected is not defined.
 	 *
 	 * @return a representative object
 	 * @throws IllegalStateException if the collection is empty
 	 */
 	CM getOne() throws IllegalStateException;
 }
