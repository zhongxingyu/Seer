 package com.bananity.util;
 
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 /**
  * @author Andreu Correa Casablanca
  */
 public class SortedLists
 {
 	/**
 	 *
 	 */
 	public static <T> boolean sortedInsert (final Comparator<T> c, final ArrayList<T> al, final int limit, T item) {
 		
 		int oldSize = al.size();
		int insertPos;
 
 		// We use a threshold to select between binary search and naive search
 		if (al.size() < 10) {
			insertPos = oldSize;

 			for (int i = al.size()-1; i >= 0; i--) {
 				if (c.compare(al.get(i), item) > 0) {
 					insertPos--;
 				} else {
 					break;
 				}
 			}
 		} else {
			insertPos = Collections.binarySearch(al, item, c);
			if (insertPos < 0) {
				insertPos = -insertPos -1;
			}
 		}
 
 		if (insertPos < limit) {
 			
 			if (oldSize == limit && insertPos == oldSize-1) {
 				al.set(oldSize-1, item);
 			} else if (oldSize < limit && insertPos == oldSize) {
 				al.add(item);
 			} else if (oldSize < limit) {
 				al.add(insertPos, item);
 			} else {
 				for (int i=insertPos; i<oldSize; i++) {
 					item = al.set(i, item);
 				}
 			}
 
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
