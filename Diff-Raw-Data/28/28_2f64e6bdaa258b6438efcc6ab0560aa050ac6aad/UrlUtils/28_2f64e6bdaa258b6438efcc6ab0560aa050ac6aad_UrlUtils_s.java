 package org.tsaikd.java.utils;
 
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 public class UrlUtils {
 
 	public static Map<String, LinkedList<String>> getUrlParameters(String url) throws Exception {
 		Map<String, LinkedList<String>> params = new HashMap<String, LinkedList<String>>();
 		String query;
 		String[] urlParts = url.split("\\?");
 		if (urlParts.length > 1) {
 			query = urlParts[1];
 		} else if (url.indexOf("&") >= 0) {
 			query = url;
 		} else {
 			query = "";
 		}
 
 		for (String param : query.split("&")) {
 			String pair[] = param.split("=");
 			String key = URLDecoder.decode(pair[0], "UTF-8");
			String value = URLDecoder.decode(pair[1], "UTF-8");
 			LinkedList<String> values = params.get(key);
 			if (values == null) {
 				values = new LinkedList<String>();
 				params.put(key, values);
 			}
 			values.add(value);
 		}
 
 		return params;
 	}
 
 }
