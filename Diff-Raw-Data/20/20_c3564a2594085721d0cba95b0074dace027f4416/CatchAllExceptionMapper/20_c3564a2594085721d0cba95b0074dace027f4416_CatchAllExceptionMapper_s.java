 package org.netvogue.rest.exceptions;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
//@Provider
 public class CatchAllExceptionMapper implements ExceptionMapper<Exception> {
 
   public Response toResponse(final Exception ex) {
     // TODO Auto-generated method stub
     /**
      * Intentionally we are not including stacktrace here, becuase showing stacktrace
      * is a security risk in internet apps. In-order to not to loose the stacktrace, because
      *  it is helpful in debugging we need to log the stacktrace either to a logfile or
      *  cloud storage
      */
     return Response.status(500).entity(ex.getMessage()).build();
   }
 
 }
