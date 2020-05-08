 package com.operativus.senacrs.audit.model;
 
 public class EssentialSkill {
 
	private String description = null;
 
	public EssentialSkill(final String description) {
 
 		super();
		this.description = description;
 	}
 
	public String getDescription() {
 
		return this.description;
 	}
 
	public void setDescription(final String description) {
 
		this.description = description;
	}
	
	@Override
	public String toString() {
	
		return this.description;
 	}
 }
