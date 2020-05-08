 package org.stjs.javascript;
 
abstract public class Storage extends Map<String, Object> {
	public native <T> T getItem(String key);
 
	public native void setItem(String key, Object data);
 }
