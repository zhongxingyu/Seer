 package com.dzebsu.acctrip.models;
 
 import java.io.Serializable;
 
 public abstract class BaseModel implements Serializable {
 
	private Long id;
 
 	public BaseModel() {
 	}
 
 	public BaseModel(Long id) {
 		this.id = id;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 }
