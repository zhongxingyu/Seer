 package org.bodytrack.client;
 
import java.util.Collections;
 import java.util.List;
 
 public final class CollectionUtils {
 	private CollectionUtils() {
 		throw new UnsupportedOperationException();
 	}
 
 	public static <T extends Comparable<T>> void insertInOrder(List<T> lst, T elem) {
 		if (lst == null || elem == null)
 			throw new NullPointerException();
 
 		if (lst.size() == 0) {
 			lst.add(elem);
 			return;
 		}
 
		int idx = Collections.binarySearch(lst, elem);
 
		if (idx < 0) {
			// Collections.binarySearch returns (-(insertion point) - 1) if
			// the item is not found
			lst.add(-idx - 1, elem);
		} else {
			lst.add(idx, elem);
		}
 	}
 }
