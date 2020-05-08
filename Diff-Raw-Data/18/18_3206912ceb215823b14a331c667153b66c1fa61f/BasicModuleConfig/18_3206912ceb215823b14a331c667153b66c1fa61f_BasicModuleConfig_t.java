 /**
  * @author William Speirs <bill.speirs@gmail.com>
  */
 package com.bittrust.config;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @class BasicModuleConfig
  */
 public class BasicModuleConfig {
 
 	private String className;
 	private Map<String, List<String>> params = new HashMap<String, List<String>>();
 
 	public void setClassName(String className) {
 		this.className = className;
 	}
 	
 	public String getClassName() {
 		return className;
 	}
 	public List<String> getParams(String param) {
 		return params.get(param);
 	}
 	
 	public String getParam(String param) {
		List<String> list = params.get(param);
		
		if(list != null)
			return list.get(0);
		else
			return null;
 	}
 	
 	public void addParam(String param, String value) {
 		
 		if(params.containsKey(param)) {
 			params.get(param).add(value);
 		} else {
 			List<String> values = new LinkedList<String>();
 			
 			values.add(value);
 			params.put(param, values);
 		}
 	}
 }
