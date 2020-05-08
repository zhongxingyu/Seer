 package com.grupo3.productConsult.utilities;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
import android.util.Log;

 public class ServerURLGenerator {
 	private String service;
 	private String baseUrl = "http://eiffel.itba.edu.ar/hci/service/";
 	private String url;
 	private Map<String, String> arguments;
 	
 	public ServerURLGenerator(String service) {
 		this.service = service;
 		this.url = this.baseUrl.concat(service).concat(".groovy");
 		this.arguments = new HashMap<String, String>();
 	}
 	
 	public void addParameter(String key, String value) {
		if (this.arguments.get(key) == null) {
 			this.arguments.put(key, value);
 		}
 	}
 	
 	public String getFullUrl() {
 		return this.url.concat(this.argsToString());
 	}
 	
 	private String argsToString() {
 		String out = "";
 		Boolean first = true;
 		Iterator it = this.arguments.entrySet().iterator();
 		
 		while (it.hasNext()) {
 			Map.Entry<String, String> keyVal = (Map.Entry)it.next();
 			if (first == true) {
				out += "?" + keyVal.getKey().toString() + "=" + keyVal.getValue().toString();
 				first = false;
 			} else {
				out += "&" + keyVal.getKey().toString() + "=" + keyVal.getValue().toString();
 			}
 		}
 		return out;
 	}
 }
