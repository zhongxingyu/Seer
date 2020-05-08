 package com.adamvduke.dowhatnow.resources.exception.mapper;
 
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 @Provider
public class CatchAllExceptionMapper implements ExceptionMapper <java.lang.Exception> {
 
 	@Context
 	UriInfo uriInfo;
 
 	@Override
	public Response toResponse( Exception exception ) {
 
		String message = "{\"request\":\"" + uriInfo.getPath() + "\",\"error\":\"" + exception.getMessage() + "\"}";
		return Response.status( 400 ).entity( message ).type( MediaType.APPLICATION_JSON ).build();
 	}
 }
