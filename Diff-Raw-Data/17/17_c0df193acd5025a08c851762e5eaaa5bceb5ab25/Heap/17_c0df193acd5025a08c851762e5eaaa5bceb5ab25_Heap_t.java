 /*
  * Copyright 2012 ToureNPlaner
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package de.tourenplaner.algorithms;
 
import com.carrotsearch.hppc.ArraySizingStrategy;

 import java.util.Arrays;
 import java.util.logging.Logger;
 
 /**
  * provides heap structures for a minimum heap for ints
  *
  * @author Stefan Bühler, Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  */
 public class Heap {
 	private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
 
 	/* Ternary heap structure.
 	 *
 	 * Index calculations:
 	 *   - Each heap entry has two array slots
 	 *   - If one heap entry is located at array slots [n,n+1], the children
 	 *     of this entry are at [3*n+2,3*n+3], [3*n+4,3*n+5], [3*n+6,3*n+7]
 	 *     For example: the root entry [0,1] has the children [2,3],[4,5],[6,7],
 	 *                  and [2,3] has the children [8,9],[10,11],[12,13].
 	 *     Also for each entry [n,n+1] n is even.
 	 *   - For a not root entry (n > 0) [n,n+1] the parent is:
 	 *          round_down_to_even((n - 2) / 3)
 	 *     because for each c in {3*n+2,3*n+4,3*n+6} and n even,
 	 *          round_down_to_even((c - 2) / 3) == n
 	 *     round_down_to_even((n - 2) / 3) is ((n - 2) / 3) & ~1 in java.
 	 */
 
 	/** the {@link #resizer} is applied to the actual number of ints in the array, not
 	 * the number of entries in the heap.
 	 */
 	protected final ArraySizingStrategy resizer;
 
 	// each heap entry takes two int slots in the heap array; one identifier, and
 	// the value to sort the heap with (named "dist").
 	private int[] heaparr;
 	private int heapentries;
 
 	// TODO: determine good value
 	// with this number of elements in the heap there is no growing from
 	// stuttgart-> hamburg
 	public final static int DEFAULT_CAPACITY = 2048;
 
 	/**
 	 * initial size is {@value #DEFAULT_CAPACITY}
 	 */
 	public Heap() {
 		this(DEFAULT_CAPACITY);
 	}
 
 	/**
 	 * initializes a heap with given initialSize
 	 *
	 * @param initialCapacity number of items to reserve space for
 	 */
 	public Heap(int initialCapacity) {
 		this(initialCapacity, new com.carrotsearch.hppc.BoundedProportionalArraySizingStrategy());
 	}
 
 	/**
 	 * initializes a heap with given initialSize and a custom resizing strategy.
 	 *
	 * @param initialCapacity number of items to reserve space for
 	 */
 	public Heap(int initialCapacity, ArraySizingStrategy resizer) {
 		assert resizer != null;
 		this.resizer = resizer;
 		heaparr = new int[resizer.round(2*initialCapacity)];
 		heapentries = 0;
 	}
 
 	/**
 	 * insert a element to the heap
 	 *
 	 * @param id
 	 * @param dist
 	 */
 
 	public final void insert(int id, int dist) {
 		ensureBufferSpace(2);
 		final int entryNdx = heapentries * 2;
 		heaparr[entryNdx] = id;
 		heaparr[entryNdx + 1] = dist;
 		++heapentries;
 		bubbleUp(entryNdx);
 	}
 
 	/**
 	 * check for empty heap
 	 *
 	 * @return
 	 */
 	public final boolean isEmpty() {
 		return heapentries <= 0;
 	}
 
 	/**
 	 * peeks the id of the minimum of the heap
 	 *
 	 * @return
 	 */
 	public final int peekMinId() {
 		assert heapentries > 0;
 		return heaparr[0];
 	}
 
 
 	/**
 	 * peeks the dist of the minimum of the heap
 	 *
 	 * @return
 	 */
 	public final int peekMinDist() {
 		assert heapentries > 0;
 		return heaparr[1];
 	}
 
 	/**
 	 * removes the min element of the heap
 	 */
 	public final void removeMin() {
 		assert heapentries > 0;
 		--heapentries;
 		heaparr[0] = heaparr[heapentries * 2];
 		heaparr[1] = heaparr[(heapentries * 2) + 1];
 		siftDown(0);
 	}
 
 	/**
 	 * implements bubble up to correct miss alignments from the button of the heap
 	 *
 	 * @param pos  index in the heap array for the item to bubble up
 	 */
 	private final void bubbleUp(int pos) {
 		while (pos > 0) {
 			// find parent index for pos
 			final int parent = ((pos - 2) / 3) & ~1;
 			if (heaparr[parent + 1] > heaparr[pos + 1]) {
 				final int tempid = heaparr[parent];
 				final int tempdist = heaparr[parent + 1];
 				heaparr[parent] = heaparr[pos];
 				heaparr[parent + 1] = heaparr[pos + 1];
 				heaparr[pos] = tempid;
 				heaparr[pos + 1] = tempdist;
 				pos = parent;
 			} else {
 				break;
 			}
 
 		}
 	}
 
 	/**
 	 * implements sift down to correct miss alignments from the top of the heap.
 	 *
 	 * @param pos  index in the heap array for the item to sift down
 	 */
 	private final void siftDown(int pos) {
 		final int heaplen = heapentries * 2;
 		while (true) {
 			// find children indices for pos
 			final int child1 = pos * 3 + 2;
 			final int child2 = child1 + 2;
 			final int child3 = child1 + 4;
 
 			int minChild;
 
 			// all except at most two entries we visit in a "sift down" have 3 children
 			if (child3 < heaplen) {
 				// 3 children
 				minChild = child1;
 				if (heaparr[child2 + 1] < heaparr[minChild + 1]) {
 					minChild = child2;
 				}
 				if (heaparr[child3 + 1] < heaparr[minChild + 1]) {
 					minChild = child3;
 				}
 			} else if (child1 < heaplen) {
 				// 1 or 2 children
 				minChild = child1;
 				if ((child2 < heaplen)
 						&& (heaparr[child2 + 1] < heaparr[minChild + 1])) {
 					minChild = child2;
 				}
 			} else {
 				// no children - reached leaf
 				break;
 			}
 
 			if (heaparr[minChild + 1] < heaparr[pos + 1]) {
 				final int tempid = heaparr[pos];
 				final int tempdist = heaparr[pos + 1];
 				heaparr[pos] = heaparr[minChild];
 				heaparr[pos + 1] = heaparr[minChild + 1];
 				heaparr[minChild] = tempid;
 				heaparr[minChild + 1] = tempdist;
 				pos = minChild;
 			} else {
 				break;
 			}
 		}
 	}
 
 	/**
 	 * resets heap
 	 */
 	public final void resetHeap() {
 		heapentries = 0;
 	}
 
 	/**
 	 * Ensures the internal buffer has enough free slots to store expectedAdditions items in the heap.
 	 */
 	public final void ensureBufferSpace(int expectedAdditions) {
 		final int minlen = (heapentries + expectedAdditions) * 2;
 		if (minlen > heaparr.length) {
            final int oldLen = heaparr.length;
			final int newLen = resizer.grow(oldLen, heapentries * 2, expectedAdditions);
			log.finer("Increased Heap size from " + oldLen + " to " +  newLen);
 			heaparr = Arrays.copyOf(heaparr, newLen);
 		}
 	}
 }
