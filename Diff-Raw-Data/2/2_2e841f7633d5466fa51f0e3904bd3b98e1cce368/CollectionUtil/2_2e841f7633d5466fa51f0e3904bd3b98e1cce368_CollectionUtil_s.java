 package edu.iastate.pdlreasoner.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class CollectionUtil {
 
 	public static <T> List<T> makeList() {
 		return new ArrayList<T>();
 	}
 	
 	public static <T> Set<T> makeSet() {
 		return new HashSet<T>();
 	}
 	
 	public static <K,V> Map<K,V> makeMap() {
 		return new HashMap<K,V>();
 	}
 	
 	public static <T> Set<T> asSet(T... as) {
 		Set<T> set = new HashSet<T>(as.length);
 		for (T a : as) {
 			set.add(a);
 		}
 		return set;
 	}
 	
 	public static <T> Set<T> copy(Collection<? extends T> a) {
 		return new HashSet<T>(a);
 	}
 	
 	public static <T> Set<T> emptySetIfNull(Set<T> a) {
 		return (a == null) ? Collections.<T>emptySet() : a;
 	}
 
 	public static <T extends Object & Comparable<? super T>> T max(T... as) {
 		if (as.length == 0) return null;
 		T currentMax = as[0];
		for (int i = as.length - 1; i >= 0; i--) {
 			if (currentMax.compareTo(as[i]) < 0) {
 				currentMax = as[i];
 			}
 		}
 		return currentMax;
 	}
 }
