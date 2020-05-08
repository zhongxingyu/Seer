 package org.javashlook.util.hmap;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Iterator;
 import java.util.LinkedHashSet;
 
 final class CollectionHelper {
 
 	private CollectionHelper() {
 	}
 
	public static <E> ArrayList<E> newArrayList(Iterable<E> iterable) {
		if (iterable == null) {
 			throw new NullPointerException();
 		}
		ArrayList<E> list = new ArrayList<E>();
		for (Iterator<E> i = iterable.iterator(); i.hasNext(); ) {
			list.add(i.next());
		}
 		return list;
 	}
 
 	public static <E> LinkedHashSet<E> newLinkedHashSet(E... elements) {
 		if (elements == null) {
 			throw new NullPointerException();
 		}
 		return new LinkedHashSet<E>(Arrays.asList(elements));
 	}
 
 }
