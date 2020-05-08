 package org.pointrel.pointrel20120623.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.TreeMap;
 
 public class Indexes {
	// There are unlikely to be many indexes, but some indexes may have a lot of data
 	// So that is why one is a HashMap and the other a TreeMap
 	HashMap<String,TreeMap<String,ArrayList<String>>> indexes = new HashMap<String,TreeMap<String,ArrayList<String>>>();
 
 	public void indexAdd(String indexName, String indexKey, String indexValueToAdd) {
 		TreeMap<String,ArrayList<String>> index = indexes.get(indexName);
 		if (index == null) {
 			System.out.println("Creating index: " + indexName);
 			index = new TreeMap<String,ArrayList<String>>();
 			indexes.put(indexName, index);
 		}
 		ArrayList<String> items = index.get(indexKey);
 		if (items == null) {
 			items = new ArrayList<String>();
 			index.put(indexKey, items);
 		}
 		items.add(indexValueToAdd);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ArrayList<String> indexGet(String indexName, String indexKey) {
 		TreeMap<String,ArrayList<String>> index = indexes.get(indexName);
 		if (index == null) {
 			return new ArrayList<String>();
 		}
 		ArrayList<String> items = index.get(indexKey);
 		if (items == null) {
 			return new ArrayList<String>();
 		}
 		
 		return (ArrayList<String>)(items.clone());
 	}
 }
