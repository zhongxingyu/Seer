 package com.aap.rest.exception;
 
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import com.aap.rest.exception.ErrorResponseConverter.ErrorCode;
 
 @Provider
 public class GeneralExceptionMapper implements ExceptionMapper<Exception> {
	
	private static final Logger log = LoggerFactory.getLogger(GeneralExceptionMapper.class);
	
 	@Context
 	private HttpHeaders headers;
 
 	@Override
 	public Response toResponse(Exception e) {
		log.error("Error no esperado", e);
 		return Response.status(Status.INTERNAL_SERVER_ERROR)
 		        .entity(new ErrorResponseConverter(e.getMessage(), e.getCause().getMessage(), ErrorCode.ERR0R))
 		        .type(headers.getMediaType()).build();
 	}
 }
