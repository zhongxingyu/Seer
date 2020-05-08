 package org.fiteagle.interactors.sfa.util;
 
 import java.util.LinkedHashMap;
 
 public class CacheMap<K, V> extends LinkedHashMap<K, V> {
 
	private static final long serialVersionUID = -2365277691360612710L;
 	private final int maxSize;
 
 	public CacheMap(int maxSize) {
 		this.maxSize = maxSize;
 	}
 	
 	@Override
 	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
 		return size() > maxSize;
 	}
 }
