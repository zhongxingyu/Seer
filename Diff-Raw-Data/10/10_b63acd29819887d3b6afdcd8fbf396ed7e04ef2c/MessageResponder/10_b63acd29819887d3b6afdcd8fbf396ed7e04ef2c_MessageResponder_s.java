 package org.twuni.web.c2dm;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.twuni.common.MultiValueMap;
 import org.twuni.common.net.http.request.Request;
 import org.twuni.common.net.http.request.adapter.UrlEncodedParametersAdapter;
 import org.twuni.common.net.http.responder.Responder;
 import org.twuni.common.net.http.response.Response;
 import org.twuni.common.net.http.response.Status;
 
 public class MessageResponder implements Responder {
 
 	private static final UrlEncodedParametersAdapter ADAPTER = new UrlEncodedParametersAdapter();
 
 	private final Map<String, String> users;
 
 	public MessageResponder( Map<String, String> registrations, Map<String, String> users ) {
 		this.users = users;
 	}
 
 	@Override
 	public Response respondTo( Request request ) {
 
 		MultiValueMap<String, String> parameters = ADAPTER.adapt( request );
 
 		if( parameters.keySet().contains( "user_id" ) ) {
 
			if( users.containsKey( parameters.get( "user_id" ) ) ) {
				parameters.put( "registration_id", users.get( parameters.get( "user_id" ) ) );
 			}
 
 		}
 
 		if( parameters.keySet().containsAll( Arrays.asList( new String [] {
 		    "content",
 		    "registration_id",
 		    "auth_token"
 		} ) ) ) {
 
 			String message = parameters.get( "content" );
 			String registrationId = parameters.get( "registration_id" );
 			String authenticationToken = parameters.get( "auth_token" );
 
 			if( message.length() > 1024 ) {
 				return new Response( Status.REQUEST_ENTITY_TOO_LARGE );
 			}
 
 			HttpClient client = new DefaultHttpClient();
 			HttpPost post = new HttpPost( "https://android.apis.google.com/c2dm/send" );
 
 			try {
 
 				post.addHeader( "Authorization", String.format( "GoogleLogin auth=%s", authenticationToken ) );
 
 				post.setEntity( new UrlEncodedFormEntity( Arrays.asList( new NameValuePair [] {
 				    new BasicNameValuePair( "collapse_key", "test" ),
 				    new BasicNameValuePair( "registration_id", registrationId ),
 				    new BasicNameValuePair( "delay_while_idle", "1" ),
 				    new BasicNameValuePair( "data.message", message )
 				} ) ) );
 
 				HttpResponse response = client.execute( post );
 
 				int status = response.getStatusLine().getStatusCode();
 
 				if( status < 400 ) {
 					return new Response( Status.OK );
 				}
 
 			} catch( Exception exception ) {
 			}
 
 			return new Response( Status.NOT_ACCEPTABLE );
 
 		}
 
 		return new Response( Status.BAD_REQUEST );
 
 	}
 
 }
