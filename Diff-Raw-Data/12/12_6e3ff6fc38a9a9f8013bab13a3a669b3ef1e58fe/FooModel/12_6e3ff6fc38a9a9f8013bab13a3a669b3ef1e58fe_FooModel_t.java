 package com.mjeanroy.springhub.mappers;
 
import com.mjeanroy.springhub.models.entities.AbstractEntity;
 
public class FooModel extends AbstractEntity {
 
 	private Long id;
 
 	private String name;
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
	@Override
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 }
