 package com.yuktix.rest.exception;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 
 import com.yuktix.dto.response.ErrorBean;
 import com.yuktix.util.Log;
 
 	/*  mapper class to catch jersey runtime errors */
 	
 	@Provider
 	public class ThrowableMapper implements ExceptionMapper<Throwable> {
 		
 	    @Override
 	    public Response toResponse(Throwable ex) {
 	    	
 	    	Log.error(ex.getMessage(), ex);
 	    	String message = "Internal service error" ;
 	    	Status status = Status.INTERNAL_SERVER_ERROR ;
 	    	// jackson json parsing exception
 	    	// @todo - jersey exception contains stack trace
 	    	
 	    	if(ex instanceof org.codehaus.jackson.JsonProcessingException) {
	    		message = String.format("Json parsing error : %s ", ex.getMessage());
 	    	}
 	    	
 	    	if(ex instanceof org.glassfish.jersey.server.ParamException) {
 	    		message = String.format("Bad request parameter : %s ", ((org.glassfish.jersey.server.ParamException) ex).getParameterName());
 	    	}
 	    	
 	    	if(ex instanceof com.yuktix.exception.ServiceIOException) {
 	    		message = ex.getMessage();
 	    		status = Status.SERVICE_UNAVAILABLE ;
 	    	}
 	    	
 	        return Response.status(status)
 	        		.entity(new ErrorBean(status.getStatusCode(),message))
 	        		 .type(MediaType.APPLICATION_JSON)
 	        		.build();
 	    }
 	}
 
