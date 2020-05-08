 package de.shop.Kundenverwaltung.rest;
 
 import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
 import static javax.ws.rs.core.Response.Status.CONFLICT;
 
 import java.util.Collection;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.validation.ConstraintViolation;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 import de.shop.Kundenverwaltung.domain.Kunde;
 import de.shop.Kundenverwaltung.service.KundeValidationException;
 
 
 @Provider
 @ApplicationScoped
 public class KundeValidationExceptionMapper implements ExceptionMapper<KundeValidationException> {
 	@Override
 	public Response toResponse(KundeValidationException e) {
 		final Collection<ConstraintViolation<Kunde>> violations = e.getViolations();
 		final StringBuilder sb = new StringBuilder();
 		for (ConstraintViolation<Kunde> v : violations) {
 			sb.append(v.getMessage());
 			sb.append(" ");
 		}
 		
 		final String responseStr = sb.toString();
 		final Response response = Response.status(CONFLICT)
 		                                  .type(TEXT_PLAIN)
 		                                  .entity(responseStr)
 		                                  .build();
 		return response;
 	}
 
 }
