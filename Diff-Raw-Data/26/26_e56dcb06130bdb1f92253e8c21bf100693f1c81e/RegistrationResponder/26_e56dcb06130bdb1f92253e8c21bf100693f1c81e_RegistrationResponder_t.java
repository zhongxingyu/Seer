 package org.twuni.web.c2dm;
 
 import java.util.Arrays;
 import java.util.Map;
 
import org.twuni.common.net.http.UrlEncodedParameters;
 import org.twuni.common.net.http.request.Request;
 import org.twuni.common.net.http.responder.Responder;
 import org.twuni.common.net.http.response.Response;
 import org.twuni.common.net.http.response.Status;
 
 public class RegistrationResponder implements Responder {
 
 	private final Map<String, String> registrations;
 	private final Map<String, String> users;
 
 	public RegistrationResponder( Map<String, String> registrations, Map<String, String> users ) {
 		this.registrations = registrations;
 		this.users = users;
 	}
 
 	@Override
 	public Response respondTo( Request request ) {
 
		UrlEncodedParameters parameters = new UrlEncodedParameters( new String( request.getContent() ) );
 
 		if( parameters.keySet().containsAll( Arrays.asList( new String [] {
 		    "registration_id",
 		    "device_id"
 		} ) ) ) {
 
 			registrations.put( parameters.get( "device_id" ), parameters.get( "registration_id" ) );
 
 			return new Response( Status.OK );
 
 		}
 
 		if( parameters.keySet().containsAll( Arrays.asList( new String [] {
 		    "user_id",
 		    "device_id"
 		} ) ) ) {
 
 			users.put( parameters.get( "user_id" ), parameters.get( "device_id" ) );
 
 			return new Response( Status.OK );
 
 		}
 
 		return new Response( Status.BAD_REQUEST );
 
 	}
 
 }
