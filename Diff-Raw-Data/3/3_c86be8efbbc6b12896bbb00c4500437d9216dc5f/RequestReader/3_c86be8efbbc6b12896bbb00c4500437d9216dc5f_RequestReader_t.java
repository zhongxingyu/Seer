 package org.twuni.common.net.http;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.twuni.common.net.exception.ConnectionClosedException;
 import org.twuni.common.net.http.exception.UnsupportedMethodException;
 import org.twuni.common.net.http.request.ConnectRequest;
 import org.twuni.common.net.http.request.DeleteRequest;
 import org.twuni.common.net.http.request.GetRequest;
 import org.twuni.common.net.http.request.HeadRequest;
 import org.twuni.common.net.http.request.OptionsRequest;
 import org.twuni.common.net.http.request.PostRequest;
 import org.twuni.common.net.http.request.PutRequest;
 import org.twuni.common.net.http.request.Request;
 import org.twuni.common.net.http.request.TraceRequest;
 
 final class RequestReader {
 
 	private static final Pattern HEADER = Pattern.compile( "^([^:]+): (.+)$" );
 
 	private final Logger log = LoggerFactory.getLogger( getClass() );
 
 	public Request read( InputStream from ) throws IOException {
 
 		Request request = null;
 		BufferedReader reader = new BufferedReader( new InputStreamReader( from ) );
 
 		for( String line = reader.readLine(); line != null && !"".equals( line ); line = reader.readLine() ) {
 
 			if( request == null ) {
 
 				String [] preamble = line.split( " " );
 
 				try {
 
 					final Method method = Method.valueOf( preamble[0] );
 					final String resource = preamble[1];
 					final float version = Float.parseFloat( preamble[2].split( "/" )[1] );
 
 					request = toHttpRequest( method, resource, version );
 
 				} catch( IllegalArgumentException exception ) {
 					log.warn( String.format( "Discarding junk line: %s", line ) );
 					continue;
 				}
 
 				continue;
 
 			}
 
 			if( "".equals( line ) || line == null ) {
 				break;
 			}
 
 			Matcher matcher = HEADER.matcher( line );
 
 			request.getHeaders().add( matcher.replaceAll( "$1" ), matcher.replaceAll( "$2" ) );
 
 		}
 
 		if( request == null ) {
 			throw new ConnectionClosedException( "The connection was closed by the client." );
 		}
 
 		extractRequestBody( reader, request );
 
 		return request;
 
 	}
 
 	private void extractRequestBody( Reader reader, Request request ) throws IOException {
 
 		int length = Integer.parseInt( request.getHeaders().get( "Content-Length", "0" ) );
 
 		if( length > 0 ) {
 			char [] buffer = new char [length];
			for( int read = 0; read < length; read += reader.read( buffer, read, buffer.length - read ) ) {
			}
 			request.setContent( new String( buffer ) );
 		}
 
 	}
 
 	private Request toHttpRequest( Method method, String resource, float version ) {
 
 		switch( method ) {
 			case GET:
 				return new GetRequest( resource, version );
 			case POST:
 				return new PostRequest( resource, version );
 			case PUT:
 				return new PutRequest( resource, version );
 			case DELETE:
 				return new DeleteRequest( resource, version );
 			case HEAD:
 				return new HeadRequest( resource, version );
 			case OPTIONS:
 				return new OptionsRequest( resource, version );
 			case CONNECT:
 				return new ConnectRequest( resource, version );
 			case TRACE:
 				return new TraceRequest( resource, version );
 		}
 
 		throw new UnsupportedMethodException( method );
 
 	}
 
 }
