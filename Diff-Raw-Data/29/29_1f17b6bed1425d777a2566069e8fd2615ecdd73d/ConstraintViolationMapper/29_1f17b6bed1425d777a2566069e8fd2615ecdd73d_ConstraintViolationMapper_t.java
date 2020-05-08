 package com.ohba.autumn.jersey;
 
import java.util.Set;

import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 @Provider 
 public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
 
 	@Override
 	public Response toResponse(ConstraintViolationException ex) {
		Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
		int counter = 1;
		StringBuilder builder = new StringBuilder();
		builder.append("{ \"errors\" : [");
		for(ConstraintViolation<?> v : violations){
			builder.append("{\"object\" : \"");
			builder.append(v.getRootBean().toString());
			builder.append("\"},");
			
			builder.append("{\"message\" : \"");
			builder.append(v.getMessage());
			builder.append("\"},");
			
			builder.append("{\"field\" : \"");
			builder.append(v.getPropertyPath().toString());
			builder.append("\"}");
			if(counter < violations.size()){
				builder.append(",");
			}

		}
		builder.append("]}");
		//return Response.status(400).entity("{\"exception\":\""+ex.getMessage()+"\"}").type("application/json").build();
		return Response.status(400).entity(builder.toString()).type("application/json").build();
 	}
 }
