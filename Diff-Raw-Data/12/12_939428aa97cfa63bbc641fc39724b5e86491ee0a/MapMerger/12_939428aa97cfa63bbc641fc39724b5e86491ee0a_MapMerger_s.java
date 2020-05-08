 package com.example.formidable;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class MapMerger {
 
	public Map<String, Object> of(Map<String, Object> under,
 			Map<String, Object> over) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		result.putAll(under);
 		
 		for(Map.Entry<String, Object> entry : over.entrySet()) {
 			if(entry instanceof Map) {
				result.put(entry.getKey(), of((Map<String, Object>) under.get(entry.getKey()), (Map<String, Object>) over.get(entry.getKey())));
 			} else {
 				result.put(entry.getKey(), entry.getValue());
 			}
 		}
 		return result;
 	}
 
 }
