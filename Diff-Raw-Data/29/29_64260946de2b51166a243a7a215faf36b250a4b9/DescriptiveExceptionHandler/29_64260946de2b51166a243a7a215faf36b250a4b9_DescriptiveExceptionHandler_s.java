 package org.kie.services.remote.rest.exception;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 @Provider
 public class DescriptiveExceptionHandler implements ExceptionMapper<Exception> {
 
     @Context
     HttpServletRequest request; 
     
     @Override
     public Response toResponse(Exception e) {
         ResponseBuilder responseBuilder = Response.serverError();
         String url = request.getRequestURI();
         if( request.getQueryString() != null ) { 
             url += "?" + request.getQueryString();
         }
         String msg = "<errorResponse>\n"
                 + "<link>" + url + "</link>\n"
                 + "<error>" + e.getMessage() + "</error>\n"
                 + "</errorResponse>";
         responseBuilder.entity(msg);
         // TODO: jaxb object for ^^
         // TODO: stack trace
         return responseBuilder.build();
     }
 
 }
