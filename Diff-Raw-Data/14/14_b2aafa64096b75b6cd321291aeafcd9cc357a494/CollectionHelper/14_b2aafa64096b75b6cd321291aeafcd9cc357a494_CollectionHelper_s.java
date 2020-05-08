 package org.javashlook.util.hmap;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Collections;
 import java.util.LinkedHashSet;
 
 final class CollectionHelper {
 
 	private CollectionHelper() {
 	}
 
	public static <E> ArrayList<E> newArrayList(E... elements) {
		if (elements == null) {
 			throw new NullPointerException();
 		}
		ArrayList<E> list = new ArrayList<E>(elements.length);
		Collections.addAll(list, elements);
 		return list;
 	}
 
 	public static <E> LinkedHashSet<E> newLinkedHashSet(E... elements) {
 		if (elements == null) {
 			throw new NullPointerException();
 		}
 		return new LinkedHashSet<E>(Arrays.asList(elements));
 	}
 
 }
