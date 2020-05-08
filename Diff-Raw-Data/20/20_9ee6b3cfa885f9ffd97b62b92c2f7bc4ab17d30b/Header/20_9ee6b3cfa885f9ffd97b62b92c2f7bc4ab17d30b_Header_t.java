 package br.com.tealdi.httpclient;
 
 import java.util.HashMap;
 
 public class Header {
 
 	private HashMap<String, String> headers;
 	
 	public Header() {
 		headers = new HashMap<String, String>();
 	}
 	
 	public void add(String key, String value) {
		headers.put(key, value);
 	}
 	
 	public String get(String key) {
		return headers.get(key);
 	}
 
 	public HashMap<String, String> getHeaders() {
 		return headers;
 	}
 }
