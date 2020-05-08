 package com.sk.service;
 
 import java.io.Serializable;
 
 import net.spy.memcached.MemcachedClient;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class CacheService {
 
 	@Autowired
 	private MemcachedClient memcachedClient;
	
	public CacheService() {}
 
 	public CacheService(MemcachedClient cacheClient) {
 		memcachedClient = cacheClient;
 	}
 
 	public void put(String key, Serializable element, int expiry) {
 		memcachedClient.set(key, expiry, element);
 	}
 
 }
