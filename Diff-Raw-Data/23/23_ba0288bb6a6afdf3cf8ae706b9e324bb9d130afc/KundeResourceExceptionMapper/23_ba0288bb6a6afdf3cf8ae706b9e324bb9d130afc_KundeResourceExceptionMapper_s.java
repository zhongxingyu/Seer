 package de.shop.Kundenverwaltung.rest;
 
 import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
 import static javax.ws.rs.core.Response.Status.CONFLICT;
 
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
		final Response response = Response.status(CONFLICT)
 		                                  .type(TEXT_PLAIN)
 		                                  .entity(msg)
 		                                  .build();
 		return response;
 	}
 
 }
