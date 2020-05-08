 package com.pardot.rhombus;
 
 /**
  * Pardot, An ExactTarget Company
  * User: robrighter
  * Date: 7/5/13
  */
 public class UpdateProcessor {
 
 	private ObjectMapper objectMapper;
 
 	public UpdateProcessor(ObjectMapper om){
 		this.objectMapper = om;
 	}
 
 	public ObjectMapper getObjectMapper() {
 		return objectMapper;
 	}
 
 	public void setObjectMapper(ObjectMapper objectMapper) {
 		this.objectMapper = objectMapper;
 	}
 
 
 
 }
