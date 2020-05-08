 package org.bodytrack.client;
 
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
 
		int idx = 0;
		while (idx < lst.size() && elem.compareTo(lst.get(idx)) < 0)
			idx++;
 
		if (idx == lst.size())
			lst.add(elem);
		else
			lst.add(idx + 1, elem);
 	}
 }
