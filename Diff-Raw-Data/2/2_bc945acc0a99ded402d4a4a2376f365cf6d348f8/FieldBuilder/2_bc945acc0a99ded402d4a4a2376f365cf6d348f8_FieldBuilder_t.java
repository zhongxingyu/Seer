 package com.sk.util;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.google.gson.JsonObject;
 
 public class FieldBuilder {
 
 	private Map<String, StringBuilder> builders = new HashMap<>();
 
 	public void addTo(PersonalData data) {
 		for (Entry<String, StringBuilder> entry : builders.entrySet()) {
 			StringBuilder builder = entry.getValue();
 			if (!builder.toString().matches("[|]*")) {
 				data.put(entry.getKey(), builder.substring(0, builder.length() - 1));
 			}
 		}
 	}
 
 	public String[] get(String key) {
 		if (builders.containsKey(key))
			return builders.get(key).toString().split("[|]");
 		else
 			return new String[0];
 	}
 
 	public void put(String key, Object value) {
 		StringBuilder cur = builders.containsKey(key) ? builders.get(key) : new StringBuilder();
 		if (value != null)
 			cur.append(value);
 		cur.append("|");
 		builders.put(key, cur);
 	}
 
 	public void put(JsonObject source, String skey, String dkey) {
 		if (source.has(skey))
 			put(dkey, source.get(skey).getAsString());
 		else
 			put(dkey, null);
 	}
 
 	public void put(JsonObject user, String key) {
 		put(user, key, key);
 	}
 
 	public void joinNames() {
 		String[] first = get("first-name"), last = get("last-name");
 		for (int i = 0; i < first.length && i < last.length; ++i) {
 			put("name", first[i] + " " + last[i]);
 		}
 	}
 
 }
