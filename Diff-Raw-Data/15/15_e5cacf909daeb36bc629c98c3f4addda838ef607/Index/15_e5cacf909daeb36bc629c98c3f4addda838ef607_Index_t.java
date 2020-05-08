 package de.fuberlin.wiwiss.jenaext.impl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import de.fuberlin.wiwiss.jenaext.EmptyIterator;
 
 
 /**
  * Indexes objects by identifiers.
  * The main instantiation of this template is an index of identifier-based
 * triples ({@link de.fuberlin.wiwiss.jenaext.IdBasedTriple} objects) where the identifiers are the
  * node identifiers. This class can be used to create S, P, and O indexes
  * of triples.
  *
  * @author Olaf Hartig
  */
 public class Index<T>
 {
 	// members
 
 	/** Bitmask that selects the bits of identifiers used for hash keys. */
 	static final private int DEFAULT_KEYMASKSIZE = 4;
 	final private int indexKeyMask;
 
 	/** the hash table */
 	final private List<T> [] index;
 
 
 	// initialization
 
 	public Index ()
 	{
 		this( DEFAULT_KEYMASKSIZE );
 	}
 
 	public Index ( int keyMaskSize )
 	{
 		indexKeyMask = ( 1 << keyMaskSize ) - 1;
 		index = new List [indexKeyMask+1];
 	}
 
 
 	// accessors
 
 	/**
 	 * Indexes the given object using the given key.
 	 */
 	public void put ( int key, T t )
 	{
 		int indexKey = getIndexKey( key );
 		if ( index[indexKey] == null ) {
 			index[indexKey] = new ArrayList<T> ();
 		}
 
 		index[indexKey].add( t );
 	}
 
 	/**
 	 * Removes the given object with the given key.
 	 *
 	 * @return true if this index contained the given object
 	 */
 	public boolean remove ( int key, T t )
 	{
 		int indexKey = getIndexKey( key );
 		if ( index[indexKey] != null ) {
 			return index[indexKey].remove( t );
 		}
 		return false;
 	}
 
 	/**
 	 * Clears the index completely.
 	 */
 	public void clear ()
 	{
 		for ( int i = index.length - 1; i >= 0; --i ) {
 			if ( index[i] != null ) {
 				index[i].clear();
 			}
 
 			index[i] = null;
 		}
 	}
 
 	/**
 	 * Returns all objects indexed with a key from the class of the given key.
 	 * Attention: the given iterator may provide more object as requested.
 	 */
 	public Iterator<T> get ( int key )
 	{
 		int indexKey = getIndexKey( key );
 		return ( index[indexKey] == null ) ? new EmptyIterator<T>() : index[indexKey].iterator();
 	}
 
 	/**
 	 * Returns all objects in this index.
 	 */
 	public Iterator<T> getAll ()
 	{
 		return new AllEntriesIterator();
 	}
 
 	/**
 	 * Returns the number of entries in this index.
 	 */
 	public int size ()
 	{
 		int result = 0;
 		int buckets = index.length;
 		for ( int i = 0; i< buckets; ++i )
 		{
 			if ( index[i] != null ) {
 				result += index[i].size();
 			}
 		}
 		return result;
 	}
 
 
 	// helpers
 
 	/**
 	 * Calculates the hash key from the given key.
 	 */
 	final protected int getIndexKey ( int key )
 	{
 		return key & indexKeyMask;
 	}
 
 
 	/**
 	 * This iterator provides all objects in this index.
 	 */
 	protected class AllEntriesIterator implements Iterator<T>
 	{
 		private int curBucketIdx = -1;
 		private Iterator<T> itCurEntry = null;
 		private T curEntry = null;
 
 		public boolean hasNext ()
 		{
 			if ( curEntry != null ) {
 				return true;
 			}
 
 			if ( itCurEntry == null || ! itCurEntry.hasNext() )
 			{
 				if ( curBucketIdx == indexKeyMask ) {
 					return false;
 				}
 
 				do {
 					curBucketIdx++;
 				} while ( curBucketIdx <= indexKeyMask && index[curBucketIdx] == null );
 
 				if ( curBucketIdx > indexKeyMask ) {
 					return false;
 				}
 
 				itCurEntry = index[curBucketIdx].iterator();
 			}
 
 			if ( itCurEntry.hasNext() ) {
 				curEntry = itCurEntry.next();
 			}
 
 			return ( curEntry != null );
 		}
 
 		public T next ()
 		{
 			if ( ! hasNext() ) {
 				throw new NoSuchElementException();
 			}
 
 			T t = curEntry;
 			curEntry = null;
 			return t;
 		}
 
 		public void remove () { throw new UnsupportedOperationException(); }
 	}
 
 }
 
 /*
  * (c) Copyright 2009 - 2010 Christian Bizer (chris@bizer.de)
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. The name of the author may not
  * be used to endorse or promote products derived from this software without
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
  * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  */
