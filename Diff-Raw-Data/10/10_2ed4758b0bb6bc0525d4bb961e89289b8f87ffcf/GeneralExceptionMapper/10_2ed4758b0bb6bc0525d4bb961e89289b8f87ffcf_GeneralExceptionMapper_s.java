 package com.aap.rest.exception;
 
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 import com.aap.rest.exception.ErrorResponseConverter.ErrorCode;
 
 @Provider
 public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

 	@Context
 	private HttpHeaders headers;
 
 	@Override
 	public Response toResponse(Exception e) {
 		return Response.status(Status.INTERNAL_SERVER_ERROR)
 		        .entity(new ErrorResponseConverter(e.getMessage(), e.getCause().getMessage(), ErrorCode.ERR0R))
 		        .type(headers.getMediaType()).build();
 	}
 }
