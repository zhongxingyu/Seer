 package de.shop.Kundenverwaltung.rest;
 
 import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
 import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 import de.shop.Kundenverwaltung.service.KundeServiceException;
 
 
 @Provider
 @ApplicationScoped
 public class KundeResourceExceptionMapper implements ExceptionMapper<KundeServiceException> {
 	@Override
 	public Response toResponse(KundeServiceException e) {
 		final String msg = e.getMessage();
		final Response response = Response.status(NOT_FOUND)
 		                                  .type(TEXT_PLAIN)
 		                                  .entity(msg)
 		                                  .build();
 		return response;
 	}
 
 }
