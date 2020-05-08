 package com.yuktix.rest.exception;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 
 import com.yuktix.dto.response.ErrorBean;
 import com.yuktix.util.Log;
 
 	/*  
 	 * mapper class to catch jersey runtime errors 
 	 * @pattern All known service errors should be thrown as
 	 * a subclass of RestException. 
 	 * 
 	 */
 	
 	@Provider
 	public class ThrowableMapper implements ExceptionMapper<Throwable> {
 		
 	    @Override
 	    public Response toResponse(Throwable ex) {
 	    	
 	    	String message = "Internal service error" ;
 	    	int code = Status.INTERNAL_SERVER_ERROR.getStatusCode() ;
	    	boolean logIt = true ;
 	    	
 	    	// jackson json parsing exception
 	    	// @todo - jersey exception contains stack trace
 	    	
 	    	if(ex instanceof org.codehaus.jackson.JsonProcessingException) {
 	    		message = String.format("Bad json; json processing error");
	    		logIt = false ;
 	    	}
 	    	
 	    	if(ex instanceof org.glassfish.jersey.server.ParamException) {
 	    		message = String.format("Bad request parameter : %s ", ((org.glassfish.jersey.server.ParamException) ex).getParameterName());
	    		logIt = false ;
 	    	}
 	    	
 	    	if(ex instanceof javax.ws.rs.ClientErrorException) {
 	    		message = ex.getMessage();
 	    		code  = ((javax.ws.rs.ClientErrorException) ex).getResponse().getStatus();
	    		logIt = false ;
	    	}
	    	
	    	if(logIt) {
	    		Log.error(ex.getMessage(), ex);
 	    	}
 	    	
 	        return Response.status(code)
 	        		.entity(new ErrorBean(code,message))
 	        		 .type(MediaType.APPLICATION_JSON)
 	        		.build();
 	    }
 	}
 
