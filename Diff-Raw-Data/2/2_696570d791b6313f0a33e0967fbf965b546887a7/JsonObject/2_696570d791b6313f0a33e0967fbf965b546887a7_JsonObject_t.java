 package com.jin.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 public class JsonObject extends LinkedHashMap<String, Object> {
 	private static final long serialVersionUID = 1L;
 
 	public Object[] getArray(String key) {
 		Object obj = this.get(key);
 		if (obj instanceof Object[]) {
 			return (Object[]) obj;
 		} else if (obj instanceof List) {
 			return ((List<?>) obj).toArray();
 		}
		return null;
 	}
 
 	public JsonObject[] getObjectArray(String key) {
 		ArrayList<?> original = (ArrayList<?>) this.get(key);
 		if (original != null) {
 			JsonObject[] arr = new JsonObject[original.size()];
 			JsonObject[] jsonObjects = ((ArrayList<?>) this.get(key)).toArray(arr);
 			return jsonObjects;
 		} else {
 			return null;
 		}
 	}
 
 	public List<?> getList(String key) {
 		return (List<?>) this.get(key);
 	}
 
 	public Map<String, Object> getMap() {
 		return this;
 	}
 
 	public JsonObject getObject(String k) {
 		return (JsonObject) this.get(k);
 	}
 
 	@Override
 	public String toString() {
 		if (this.size() == 1) {
 			Object single = super.values().iterator().next();
 			if (single instanceof Collection || single instanceof Object[]) {
 				return super.toString();
 			} else {
 				return single.toString();
 			}
 		} else if (this.size() > 1) {
 			return super.toString();
 		}
 		return null;
 	}
 }
