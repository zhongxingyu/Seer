 package com.mjeanroy.springhub.models.vo;
 
 import com.mjeanroy.springhub.models.AbstractModel;
 
 public abstract class AbstractVO extends AbstractModel {
 
 	public AbstractVO() {
 		super();
 	}
 
 	public boolean isNew() {
		return getId() != null && getId() > 0;
 	}
 
 	@Override
 	public Long modelId() {
 		return getId();
 	}
 
 	public Long getId() {
 		return null;
 	}
 }
