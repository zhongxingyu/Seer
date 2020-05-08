 package com.bluesky.visualprogramming.core;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Procedure extends _Object {
 
 	public Procedure(long id) {
 		super(id);
 		type = ObjectType.PROCEDURE;
 	}
 
 	public String name;
 
 	boolean _native = false;
 	public String nativeProcedureClassName;
 
 	public String language;
 	String code;
 
 	@Override
 	public String getValue() {
 
 		String attributes = "";
 		if (this._native)
			attributes = String.format("[native=%b, classname=%s]", _native,
 					nativeProcedureClassName);
 		else
 			attributes = "[native=false, language=goo]";
 
 		return attributes + "\r\n" + code;
 	}
 
 	private Map<String, String> parseAttributes(String str) {
 
 		Map<String, String> map = new HashMap<String, String>();
 		String[] attrArray = str.split(",");
 		for (String s : attrArray) {
 			String[] kv = s.split("=");
			map.put(kv[0], kv[1]);
 		}
 		return map;
 
 	}
 
 	@Override
 	public void setValue(String value) {
 		int index1 = value.indexOf('[');
 		String code;
 		if (index1 >= 0) {// has attributes
 			int index2 = value.indexOf(']');
			String attrStr = value.substring(index1, index2);
 			Map<String, String> map = parseAttributes(attrStr);
 
 			if (map.containsKey("native"))
 				_native = Boolean.valueOf(map.get("native"));
 			else
 				_native = false;
 
 			if (map.containsKey("native-class"))
 				nativeProcedureClassName = map.get("native-class");
 
 			if (map.containsKey("language"))
 				language = map.get("language");
 
			code = value.substring(index2);
 		} else
 			code = value;
 
 		try {
 			this.code = code;
 		} catch (Exception e) {
 			this.code = null;
 
 		}
 
 	}
 
 	public boolean isNative() {
 		return _native;
 	}
 }
