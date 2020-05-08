 import java.util.*;
 import java.io.*;
 import java.lang.Runtime;
 
 class advs_buffer implements java.io.Serializable {
 	static final long serialVersionUID = -6724870319916642357l;
 	private SortedMap primaryMap;
 	private SortedMap secondaryMap;
 
 	public advs_buffer(SortedMap primary, SortedMap secondary) {
 		if(primary == null)
 			primaryMap = new TreeMap<Integer, Float[]>();
 		else
 			primaryMap = primary;
 		if(secondary == null)
 			secondaryMap = new TreeMap<Integer, Float[]>();
 		else
 			secondaryMap = secondary;
 	}
 
 	public advs_buffer getRange(int start, int end) {
 		SortedMap secRange = secondaryMap.subMap(start, end);
 		SortedMap priRange = primaryMap.subMap(start, end);
 
 		return new advs_buffer(secRange, priRange);
 	}
 
 	public SortedMap getPrimary() {
 		return primaryMap;
 	}
 
 	public SortedMap getSecondary() {
 		return secondaryMap;
 	}
 
 	public int getSize() {
 		return primaryMap.size() + secondaryMap.size();
 	}
 
 	public void print() {
		SortedMap combined = combineMaps(secondaryMap, primaryMap);

 		System.out.println("\n{");
		Iterator iterator = combined.keySet().iterator();
 		while(iterator.hasNext()) {
 			Object key = iterator.next();
			System.out.print("\"" + key + "\"" + ":" + Arrays.toString((float[])(combined.get(key))));
 			if(iterator.hasNext())
 				System.out.println(",");
 		}
 
 		System.out.println("\n}");
 	}
 
 	public void put(int key, float[] val) {
 		primaryMap.put(key, val);
 
 		if(primaryMap.size() > 26280)
 			rebalance();
 	}
 
 	private SortedMap combineMaps(SortedMap a, SortedMap b) {
 		a.putAll(b);
 		return a;
 	}
 
 	public void combine(advs_buffer other) {
 		primaryMap = combineMaps(primaryMap, other.getPrimary());
 		secondaryMap = combineMaps(secondaryMap, other.getSecondary());
 	}
 
 	private void transferChunkToSecondary() {
 		int first = (Integer)primaryMap.firstKey();
 		int last = first + 3600;
 
 		secondaryMap.put(first, primaryMap.get(first));
 
 		primaryMap = primaryMap.tailMap(last);
 	}
 
 	public void rebalance() {
 		int primaryMax = 26280;
 		int secondaryMax = 6570;
 
 		while(primaryMap.size() > primaryMax) {
 			transferChunkToSecondary();
 		}
 
 		while(secondaryMap.size() > secondaryMax) {
 			secondaryMap.remove(secondaryMap.firstKey());
 		}
 	}
 }
