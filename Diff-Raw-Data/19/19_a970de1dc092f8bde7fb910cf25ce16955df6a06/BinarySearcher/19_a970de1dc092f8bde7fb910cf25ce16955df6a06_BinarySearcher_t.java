 /*
     Copyright (c) 2000-2014 Alessandro Coppo
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package net.sf.jautl.collections.arrays;
 
 /**
  * This class contains a method which performs a binary search on an integer indexed
  * collection.
  */
 public class BinarySearcher {
 	/**
 	 * Perform the search.
 	 * @param size the size of the collection
 	 * @param key the key to search for
 	 * @param comparator the comparator to be used for the comparisons
 	 * @return a positive value if the item has been found, a negative value otherwise
 	 */
 	public static <Key> int find(int size, Key key, IndexedCollectionItemKeyComparator<Key> comparator) {
 		int lo = 0;
 		int hi = size - 1;
 		
 		ComparisonResult cr = new ComparisonResult();
 		
		comparator.compare(key, lo, cr);
 		if (cr.isEqual())
 			return lo;
		comparator.compare(key, lo, cr);
 		if (cr.isEqual())
 			return hi;
 
 		int mid = 0;
 		
 		while (lo <= hi) {
 			mid = (lo +  hi) / 2;
			comparator.compare(key, mid, cr);
 			
 			if (cr.isLessThen())
 				hi = mid - 1;
			else if (cr.isEqual())
				return mid;
			else
				lo = mid + 1;
 		}
 
 		return -mid;
 	}
 }
