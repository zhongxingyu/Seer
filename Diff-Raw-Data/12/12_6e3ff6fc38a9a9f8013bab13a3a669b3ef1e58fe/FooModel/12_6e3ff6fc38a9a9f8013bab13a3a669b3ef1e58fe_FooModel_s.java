 package com.mjeanroy.springhub.mappers;
 
import com.mjeanroy.springhub.models.AbstractModel;
 
public class FooModel extends AbstractModel {
 
 	private Long id;
 
 	private String name;
 
 	@Override
	public Long modelId() {
		return getId();
	}

 	public Long getId() {
 		return id;
 	}
 
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
