 package com.adamvduke.dowhatnow.resources.exception.mapper;
 
import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 @Provider
public class CatchAllExceptionMapper implements ExceptionMapper <WebApplicationException> {

	private static final String formatMessage = "{\"request\":\"%s\",\"error\":\"%s\"}";
	private static final String defaultMessage = "Check http response headers.";
 
 	@Context
 	UriInfo uriInfo;
 
 	@Override
	public Response toResponse( WebApplicationException exception ) {
 
		Response jerseyResponse = exception.getResponse();
		ResponseBuilder customResponseBuilder = Response.status( jerseyResponse.getStatus() );
		MultivaluedMap <String, Object> map = jerseyResponse.getMetadata();
		for ( String key : map.keySet() ) {
			customResponseBuilder.header( key, map.get( key ) );
		}
		String exceptionMessage = exception.getMessage();
		if ( exceptionMessage == null ) {
			exceptionMessage = defaultMessage;
		}
		customResponseBuilder.entity( String.format( formatMessage, uriInfo.getPath(), exceptionMessage ) );
		customResponseBuilder.type( MediaType.APPLICATION_JSON );
		return customResponseBuilder.build();
 	}
 }
