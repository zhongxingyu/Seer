 package org.bioinfo.opencga.lib.storage;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class XObject extends LinkedHashMap<String, Object> {
 
 
 	private static final long serialVersionUID = -242187651119508127L;
 
 	
 	public XObject() {
 		
 	}
 
 	public XObject(int size) {
 		super(size); 
 	}
 	
 	public XObject(String key, Object value) {
 		put(key, value); 
 	}
 	
 	public XObject(Map<String, Object> map) {
 		super(map);
 	}
 
 
 	public boolean containsField(String key) {
 		return this.containsKey(key);
 	}
 	
 	public Object removeField(String key) {
 		return this.remove(key);
 	}
 	
 	public Object get(String key ){
         return super.get(key);
     }
 	
 	
 	public String getString(String field) {
 		return getString(field, "");
 	}
 
 	public String getString(String field, String defaultValue) {
 		if(field != null && this.containsKey(field)) {
			return (String)this.get(field);
 		}
 		return defaultValue;
 	}
 
 
 	public int getInt(String field) {
 		return getInt(field, 0);
 	}
 
 	public int getInt(String field, int defaultValue) {
 		if(field != null && this.containsKey(field)) {
			Object obj = this.get(field);
			if(obj instanceof Integer) {
				return (Integer)obj;
			}else{
				return Integer.parseInt(String.valueOf(obj));								
			}
 		}
 		return defaultValue;
 	}
 
 
 	public float getFloat(String field) {
 		return getFloat(field, 0.0f);
 	}
 
 	public float getFloat(String field, float defaultValue) {
 		if(field != null && this.containsKey(field)) {
 			return Float.parseFloat((String)this.get(field));				
 		}
 		return defaultValue;
 	}
 
 
 	public double getDouble(String field) {
 		return getDouble(field, 0.0);
 	}
 
 	public double getDouble(String field, double defaultValue) {
 		if(field != null && this.containsKey(field)) {
 			return Double.parseDouble((String)this.get(field));
 		}
 		return defaultValue;
 	}
 
 	
 	public boolean getBoolean(String field) {
 		return getBoolean(field, false);
 	}
 
 	public boolean getBoolean(String field, boolean defaultValue) {
 		if(field != null && this.containsKey(field)) {
 			return Boolean.parseBoolean((String)this.get(field));				
 		}
 		return defaultValue;
 	}
 
 }
