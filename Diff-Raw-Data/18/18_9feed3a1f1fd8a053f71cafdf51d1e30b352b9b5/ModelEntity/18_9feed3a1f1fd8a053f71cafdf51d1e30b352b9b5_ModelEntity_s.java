 package com.hazardousholdings.unkj.db;
 
 import java.lang.Object;
 import java.lang.Override;
 import java.lang.String;
 
 public abstract class ModelEntity<K extends Key> {
 
 	private K key;
 
 	public ModelEntity() {
 		
 	}
 
 	public K getKey() {
 		return this.key;
 	}
 
 	public void setKey(K key) {
 		this.key = key;
 	}
 
 	public String toString() {
 		if(this.getKey() != null) {
 			return this.getClass().getName() + ": " + this.getKey().toString();
 		} else {
 			return this.getClass().getName() + " (non-persisted): " + super.toString();
 		}
 	}
 
 	@Override
 	public boolean equals(Object otherObject) {
 		if(otherObject instanceof ModelEntity && this.getKey() != null) {
 			ModelEntity otherEntity = (ModelEntity) otherObject;
 			return this.getKey().equals(otherEntity.getKey());
 		}
 
 		return super.equals(otherObject);
 	}
 
 	@Override
 	public int hashCode() {
 		if(this.getKey() != null) {
 			return this.getKey().hashCode();
 		} else {
 			return super.hashCode();
 		}
 	}
 	
 }
