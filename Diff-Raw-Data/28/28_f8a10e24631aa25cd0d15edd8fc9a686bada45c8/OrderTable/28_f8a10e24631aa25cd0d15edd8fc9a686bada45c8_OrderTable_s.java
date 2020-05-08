 package com.sk.util;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 public class OrderTable implements Comparator<Object> {
 
 	private Map<Object, Map<Object, Integer>> order = new HashMap<>();
 
 	public OrderTable() {
 	}
 
 	public void addInOrder(List<?> objects) {
 		for (ListIterator<?> outer = objects.listIterator(); outer.hasNext();) {
 			int stopIndex = outer.nextIndex();
 			Object after = outer.next();
 			for (ListIterator<?> inner = objects.listIterator(); inner.nextIndex() < stopIndex && inner.hasNext();) {
 				add(inner.next(), after);
 			}
 		}
 	}
 
 	public void addInOrder(Object... objects) {
 		for (int i = 1; i < objects.length; ++i) {
 			for (int j = 0; j < i; ++j) {
 				add(objects[j], objects[i]);
 			}
 		}
 	}
 
 	public void add(final Object before, final Object after) {
 		if (order.containsKey(before)) {
 			order.get(before).put(after, -1);
 		} else {
 			Map<Object, Integer> newMap = new HashMap<>();
 			newMap.put(after, -1);
 			order.put(before, newMap);
 		}
 		if (order.containsKey(after)) {
 			order.get(after).put(before, 1);
 		} else {
 			Map<Object, Integer> newMap = new HashMap<>();
 			newMap.put(before, 1);
 			order.put(after, newMap);
 		}
 	}
 
 	@Override
 	public int compare(Object o1, Object o2) {
		return order.get(o1).get(o2);
 	}

 }
