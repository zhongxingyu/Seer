 package com.sis.util;
 
 import java.util.Date;
 
 /**
  * provides very simple object cache 
  * @author CR
  *
  * @param <T>
  */
 public class CachedObject<T> {
 	public static interface AcquirerCallback<T> {
 		public T acquire(T value, CachedObject<T> cachedObject);
 	}
 	
 	private long expiresAt = 0;
 	private long lifetime = 0;
 	private T value = null;
 	private final AcquirerCallback<T> acquisitionCallback;
 	
 	private Object data = null;
 	
 	/**
 	 * 
 	 * @param lifetime milliseconds
 	 * @param defaultValue (can be null)
 	 * @param acquisitionCallback 
 	 */
 	public CachedObject(int lifetime, T defaultValue, AcquirerCallback<T> acquisitionCallback) {
 		value = defaultValue;
 		setLifetime(lifetime);
 		expiresAt = 0;
 		
 		this.acquisitionCallback = acquisitionCallback;
 	}
 
 	public long getLifetime() {
 		return lifetime;
 	}
 
 	/**
 	 * 
 	 * @param lifetime milliseconds
 	 */
 	public synchronized void setLifetime(long lifetime) {
 		this.lifetime = lifetime;
 		this.expiresAt = new Date().getTime() + lifetime;
 	}
 	
 	/**
 	 * explicitly expires the cached object
 	 */
 	public synchronized void expire() {
 		this.expiresAt = 0;
 	}
 	
 	/**
 	 * returns cached object (or if expired or not initialized yet, an acquired one)
 	 */
 	public synchronized T getValue() {
 		if (value == null || new Date().getTime() > expiresAt) {
 			acquireValue();
 		}
 		
 		return value;
 	}
 
 	private void acquireValue() {
 		this.value = acquisitionCallback.acquire(value, this);
 		setLifetime(getLifetime());
 	}
 
 	public Object getData() {
 		return data;
 	}
 
 	/**
 	 * sets an optional payload (which can be used by acquirer)
 	 * @param data
 	 */
 	public void setData(Object data) {
 		this.data = data;
 	}
 }
