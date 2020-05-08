 package com.ideazworld.amber.restapi.provider;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 import org.springframework.stereotype.Component;
 
 import com.ideazworld.amber.exception.AmberException;
 import com.ideazworld.amber.restapi.exception.UnsupportedApiException;
 import com.ideazworld.amber.service.exception.InvalidOperationException;
 import com.ideazworld.amber.service.exception.StaleDataException;
 
 @Provider
 @Component
 public class AmberExceptionMapper implements ExceptionMapper<AmberException> {
 
 	@Override
	public Response toResponse(AmberException amberException) {
 		Status status = null;
		if(amberException instanceof InvalidOperationException) {
 			status = Status.PRECONDITION_FAILED;
		} else if(amberException instanceof UnsupportedApiException) {
 			status = Status.NOT_ACCEPTABLE;
		} else if(amberException instanceof StaleDataException) {
 			status = Status.CONFLICT;
 		} else {
 			status = Status.INTERNAL_SERVER_ERROR;
 		}
 		return Response.status(status)  
 	            .type(MediaType.TEXT_PLAIN)  
	            .entity(amberException.getLocalizedMessage())  
 	            .build();
 	}
 }
