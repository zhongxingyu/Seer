 package org.beequeue.comb;
 
 import org.beequeue.hash.HashKey;
 
 public interface BeeCombStore {
	BeeFrame get(String collectionName);
 	ContentSource read(HashKey key);
 	HashKey save(ContentSource content);
 }
