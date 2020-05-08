 package com.technion.compile.ore;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * <p>This class store a map of pairs (Integer, List of {@link Object}). 
  * For each level : 0,1,2... you can obtain all the predicates</p>
  * @author XPS_Sapir
  * @param <T>
  *
  */
 public class ObjectsMap<T> {
 	
 	private Map<Integer, List<T> > map;
 
 	public ObjectsMap( ) {
 		this.map = new HashMap<Integer, List<T>>();
 	}
 	
 	public void addObjects(Integer level, List<T> list) {
 		map.put(level, list);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void addObject(Integer index, T object) {
 		this.addObjects(index,  Arrays.asList(object));
 	}
 	
 	public List<T> getObjects(Integer index) {
 		List<T> list = map.get(index);
 		if (list==null) {
 			list = new ArrayList<T>();
 		}
 		return list;
 	}
 	
	/**
	 * <P>Returns <code>list</code> of object type T.
	 * Each member of the list is of type T. 
	 * @param threshold - Only objects that are on level above this value will be in the result
	 * @return
	 */
 	public List<T> getObjectsWithKeyGreaterThan(Integer threshold) {
 		ArrayList<T> result = new ArrayList<T>();
 		Iterator<Entry<Integer, List<T>>> iterator = map.entrySet().iterator();
 		while (iterator.hasNext()) {
 			Map.Entry<Integer, List<T>> entry = (Map.Entry<Integer, List<T>>) iterator.next();
 			if ( entry.getKey() > threshold ) {
				List<T> valuelistlist = entry.getValue();
				result.addAll(valuelistlist);
 			}
 		}
 		return result;
 	}
 
 }
