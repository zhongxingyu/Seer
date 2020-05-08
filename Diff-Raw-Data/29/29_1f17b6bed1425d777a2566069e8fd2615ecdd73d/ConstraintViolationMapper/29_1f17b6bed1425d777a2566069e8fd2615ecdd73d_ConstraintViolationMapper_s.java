 package com.ohba.autumn.jersey;
 
 import javax.validation.ConstraintViolationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 @Provider 
 public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
 
 	@Override
 	public Response toResponse(ConstraintViolationException ex) {
		return Response.status(400).entity("{\"exception\":\""+ex.getMessage()+"\"}").type("application/json").build();
 	}
 }
