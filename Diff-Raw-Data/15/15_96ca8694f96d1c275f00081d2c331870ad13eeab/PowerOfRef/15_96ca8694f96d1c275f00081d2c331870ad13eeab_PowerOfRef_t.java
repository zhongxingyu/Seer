 package org.swordess.toy.javamisc.ref;
 
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 @Ignore("This is not a real test!")
 public class PowerOfRef {
 
 	@Test
 	public void useWeakReferenceOnMap() {
 		Map<Integer, String> map = new HashMap<Integer, String>();
 		Reference<Map<Integer, String>> weakRef = new WeakReference<Map<Integer,String>>(map);
 		map = null;
 		
 		int i = 0;
 		while (true) {
			Map<Integer, String> strongRef = weakRef.get();
			if (strongRef != null) {
				strongRef.put(i++, "test" + i);
 				System.out.println(i + " im still working!!!");
 			} else {
 				System.out.println("******* im free *******");
 				break;
 			}
 		}
 	}
 	
 	@Test
 	public void useWeakHashMap() {
 		Map<Integer, String> cache = new WeakHashMap<Integer, String>();
 		int i = 0;
 		while (true) {
 			cache.put(i++, "test" + i);
 			System.out.println(i + " im still working!!!");
 		}
 	}
 
 }
