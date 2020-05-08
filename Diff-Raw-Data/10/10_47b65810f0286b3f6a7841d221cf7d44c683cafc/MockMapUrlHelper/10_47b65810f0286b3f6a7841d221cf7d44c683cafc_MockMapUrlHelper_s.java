 package com.github.rabid_fish.proxy.mock;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 
 public class MockMapUrlHelper extends MockMapHelper<String> {
 
 	private Map<String, String> mapUrlToFilename = new HashMap<String, String>();
 
 	public MockMapUrlHelper(String resourcePath) {
 		super(resourcePath);
 		fillUrlMap(mockMapItems);
 	}
 
 	void fillUrlMap(MockMapItem[] mockMapItems) {
 		
 		for (MockMapItem item : mockMapItems) {
 			String url = item.getUrl();
 			if (url != null && url.length() > 0) {
 				mapUrlToFilename.put(url, item.getPath());
 			}
 		}
 	}
 
	
 	public String getMockBodyForUrl(String url) throws Exception {
 		
 		String key = getKeyForRequestUrl(url);
 		if (key == null) return null;
 		
 		String path = mapUrlToFilename.get(key);
 		String mock = readFileFromResourcePath(path);
 		return mock;
 	}
 
 	String getKeyForRequestUrl(String url) {
 		
 		Iterator<String> keys = mapUrlToFilename.keySet().iterator();
 		while (keys.hasNext()) {
 			String key = keys.next();
 			if (url.startsWith(key)) {
 				return key;
 			}
 		}
 		
 		return null;
 	}
 }
