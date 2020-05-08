 package com.operativus.senacrs.audit.model;
 
 public class EssentialSkill {
 
	private String skillDescription = null;
 
	public EssentialSkill(final String skillDescription) {
 
 		super();
		this.skillDescription = skillDescription;
 	}
 
	public String getSkillDescription() {
 
		return this.skillDescription;
 	}
 
	public void setSkillDescription(final String skillDescription) {
 
		this.skillDescription = skillDescription;
 	}
 }
