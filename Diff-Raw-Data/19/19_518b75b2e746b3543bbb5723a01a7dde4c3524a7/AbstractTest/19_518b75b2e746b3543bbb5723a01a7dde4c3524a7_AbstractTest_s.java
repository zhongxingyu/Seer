 package com.redshape.utils.tests;
 
 import java.util.HashMap;
 import java.util.Map;
 
public abstract class AbstractTest<V> {
 	private Map<V, Object> attributes = new HashMap<V, Object>();
 	
 	protected void setAttribute( V key, Object value ) {
 		this.attributes.put(key, value);
 	} 
 	
 	@SuppressWarnings("unchecked")
 	protected <T> T getAttribute( V key ) {
 		return (T) this.attributes.get(key);
 	}
 	
 }
