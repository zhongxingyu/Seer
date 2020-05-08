 package com.epam.lab.intouch.model.member.info.skill;
 
 public class Skill {
	
	
	private long id;
 	private String name;
 	private Double experience;
 	private String level;
 	private String description;
 
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Double getExperience() {
 		return experience;
 	}
 
 	public void setExperience(Double experience) {
 		this.experience = experience;
 	}
 
 	public String getLevel() {
 		return level;
 	}
 
 	public void setLevel(String level) {
 		this.level = level;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 }
