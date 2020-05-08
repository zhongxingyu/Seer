 package org.cocoa4android.ns;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class NSSet {
 	private ArrayList<Object> set = new ArrayList<Object>();
 	public NSSet(Object[] objects) {
 		set.addAll(Arrays.asList(objects));
 	}
	public Object anyObject() {
 		if (set.size()>0) {
 			return set.get(0);
 		}
 		return null;
 	}
 }
