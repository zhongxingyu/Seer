 /*
  * Copyright (c) 2008-2012 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package iudex.jettyhttpclient;
 
 import iudex.http.ContentType;
 import iudex.http.ContentTypeSet;
 import iudex.http.HTTPClient;
 import iudex.http.HTTPSession;
 import iudex.http.Header;
 import iudex.http.Headers;
 import iudex.http.ResponseHandler;
 
 import java.net.SocketTimeoutException;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.channels.UnresolvedAddressException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
 import org.eclipse.jetty.client.api.*;
 import org.eclipse.jetty.http.HttpFields.Field;
 import org.eclipse.jetty.http.HttpMethod;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gravitext.util.Closeable;
 import com.gravitext.util.ResizableByteBuffer;
 
 public class Client
     implements HTTPClient, Closeable
 {
     public static class SessionAbort extends Exception
     {
         public SessionAbort( String msg )
         {
             super( msg );
         }
     }
 
     public Client( HttpClient client )
     {
         _client = client;
     }
 
     public void setExecutor( ExecutorService executor )
     {
         _client.setExecutor( executor );
     }
 
     /**
      * Set the set of accepted Content Type patterns.
      */
     public void setAcceptedContentTypes( ContentTypeSet types )
     {
         _acceptedContentTypes = types;
     }
 
     /**
      * Set maximum length of response body accepted.
      */
     public void setMaxContentLength( int length )
     {
         _maxContentLength = length;
     }
 
     public void setCancelOnExpire( boolean doCancel )
     {
         _doCancelOnExpire = doCancel;
     }
 
     public HttpClient jettyClient()
     {
         return _client;
     }
 
     public void start() throws RuntimeException
     {
         try {
             _client.start();
         }
         catch( RuntimeException x ) {
             throw x;
         }
         catch( Exception x ) {
             throw new RuntimeException( x );
         }
     }
 
     @Override
     public HTTPSession createSession()
     {
         Session session = new Session();
         session.setMaxContentLength( _maxContentLength );
         session.setAcceptedContentTypes( _acceptedContentTypes );
         return session;
     }
 
     @Override
     public void request( HTTPSession session, ResponseHandler handler )
     {
         ((Session) session).execute( handler );
     }
 
     @Override
     public void close()
     {
         try {
             dump();
             _client.stop();
         }
         catch( Exception e ) {
             _log.warn( "On close: {}", e.toString() );
             _log.debug( "On close:", e );
         }
     }
 
     public void dump()
     {
         // Warning: Can deadlock jetty threads!
         // Allow selective logging and only dump if debug logging enabled.
         Logger log = LoggerFactory.getLogger( "iudex.jettyhttpclient.Dumper" );
 
         if( log.isDebugEnabled() ) {
             log.debug( "Jetty Client Dump ::\n{}", _client.dump() );
         }
     }
 
     public static class Expired
         extends TimeoutException
     {
         public Expired()
         {
             super();
         }
     }
 
     private class Session
         extends HTTPSession
         implements Request.HeadersListener,
                    Request.FailureListener,
                    Response.Listener
     {
         public void addRequestHeader( Header header )
         {
             _requestedHeaders.add( header );
         }
 
         public List<Header> requestHeaders()
         {
             return _requestHeaders;
         }
 
         public int statusCode()
         {
             return _statusCode;
         }
 
         public String statusText()
         {
             return _statusText;
         }
 
         public List<Header> responseHeaders()
         {
             if( _responseHeaders != null ) {
                 return _responseHeaders;
             }
 
             return Collections.emptyList();
         }
 
         public ByteBuffer responseBody()
         {
             if ( _body != null ) {
                 return _body.flipAsByteBuffer();
             }
             return null;
         }
 
         public void close()
         {
             //No-op
         }
 
         void execute( ResponseHandler handler )
         {
             _handler = handler;
 
             Request req = _client.newRequest( url() );
             switch( method() ) {
             case GET:
                 req.method( HttpMethod.GET );
                 break;
             case HEAD:
                 req.method( HttpMethod.HEAD );
                 break;
             default:
                 throw new IllegalArgumentException("Unsupported: " + method());
             }
             for( Header h : _requestedHeaders ) {
                 req.header(  h.name().toString(),
                              h.value().toString() );
             }
 
             try {
                 req.send( this );
                 onSend();
             }
             catch( UnresolvedAddressException x ) {
                 onException( x );
             }
         }
 
         private void complete()
         {
             ResponseHandler handler = _handler;
             if( handler != null ) {
                 onComplete();
                 _handler = null;
                 handler.sessionCompleted( this );
                 _latch.countDown();
             }
             else {
                 _log.warn( "Redundant complete" );
             }
         }
 
         @Override
         public void abort()
         {
             //FIXME: do nothing?
         }
 
         @SuppressWarnings("unused")
         public void waitForCompletion() throws InterruptedException
         {
             _latch.await();
         }
 
         /**
          * Called by session for tracking.
          */
         void onSend()
         {
             _log.debug( "onSend Exchange {}", this );
         }
 
         @Override
         public void onHeaders( Request request )
         {
             _log.debug( "onBegin: {}", request );
 
             for( Field f : request.getHeaders() ) {
                 _requestHeaders.add(new Header(f.getName(), f.getValue()));
             }
             setUrl( request.getURI() ); //FIXME: May not be called on redirect
         }
 
         @Override
         public void onBegin( Response response )
         {
             _log.debug( "onBegin: {}", response );
 
         }
 
         @Override
         public void onHeaders( Response response )
         {
             for( Field f : response.getHeaders() ) {
                 _responseHeaders.add(new Header(f.getName(), f.getValue()));
             }
 
             _statusCode = response.getStatus();
             _statusText = response.getReason();
 
             _log.debug( "onHeaders, status: {} {}",
                         _statusCode, _statusText );
 
             if( _statusCode == 200 ) {
 
                 //check Content-Type
                 ContentType ctype = Headers.contentType( _responseHeaders );
 
                 if( ! acceptedContentTypes().contains( ctype ) ) {
                     _statusCode = NOT_ACCEPTED;
                     _statusText = null;
                     response.abort( new SessionAbort( "NOT_ACCEPTED" ) );
                 }
                 else {
                     int length = Headers.contentLength( _responseHeaders );
                     if( length > maxContentLength() ) {
                         _statusCode = TOO_LARGE_LENGTH;
                         _statusText = null;
                         response.abort( new SessionAbort( "TOO_LARGE_LENGTH" ) );
                     }
                     else {
                         _body = new ResizableByteBuffer(
                                     ( length >= 0 ) ? length : 16 * 1024 );
                     }
                 }
             }
 
         }
 
         @Override
         public void onContent( Response response, ByteBuffer content )
         {
             if( _body != null ) {
                 if( ( _body.position() + content.remaining() ) >
                     maxContentLength() ) {
                     _body = null;
                     _statusCode = TOO_LARGE;
                     response.abort( new SessionAbort( "TOO_LARGE" ) );
                 }
                 else {
                     _body.put( content );
                 }
             }
             else {
                 //FIXME: Consume otherwise?
                 _log.debug( "Ignoring onResponseContent" );
             }
         }
 
         @Override
         public void onSuccess( Response response )
         {
            _log.debug( "onSuccess {}", response );
         }
 
         @Override
         public void onFailure( Request request, Throwable failure )
         {
             if( !onException( failure ) ) {
                 request.abort( failure );
             }
         }
 
         @Override
         public void onFailure( Response response, Throwable failure )
         {
             if( !onException( failure ) ) {
                 response.abort( failure );
             }
         }
 
         /**
          * Called by session.complete() for tracking.
          */
         private void onComplete()
         {
            _log.debug( "onComplete Exchange {}", this );
         }
 
         @Override
         public void onComplete( Result result )
         {
             complete();
         }
 
         private boolean onException( Throwable t ) throws Error
         {
             if( t instanceof Exception ) {
 
                 if( _handler == null ) {
                     _log.error( "On Exception (already handled): ", t );
                 }
 
                 if( ( t instanceof IllegalArgumentException ) &&
                     ( t.getCause() instanceof URISyntaxException ) ) {
                     t = t.getCause();
                 }
 
                 if( t instanceof Expired ) {
                     _statusCode = TIMEOUT;
                 }
                 else if( t instanceof TimeoutException ) {
                     _statusCode = TIMEOUT_CONNECT;
                 }
                 else if( t instanceof SocketTimeoutException ) {
                     _statusCode = TIMEOUT_SOCKET;
                 }
                 else if( ( t instanceof UnresolvedAddressException ) ||
                          ( t instanceof UnknownHostException ) ) {
                     _statusCode = UNRESOLVED;
                 }
                 else if( t instanceof URISyntaxException ) {
                     _statusCode = INVALID_REDIRECT_URL;
                 }
                 else if( t instanceof SessionAbort ) {
                     _statusText = null;
                 }
                else if( t instanceof HttpResponseException ) {
                    if( t.getMessage().startsWith( "Max redirects ") ) {
                        _statusCode = MAX_REDIRECTS_EXCEEDED;
                    }
                    else {
                        _statusCode = ERROR;
                    }
                }
                 else {
                     _statusCode = ERROR;
                 }
 
                 setError( (Exception) t );
                 complete();
                 return true;
             }
             else {
                 _log.error( "Session onException (Throwable): ", t );
                 _statusCode = ERROR_CRITICAL;
 
                 if( t instanceof Error) {
                     throw (Error) t;
                 }
                 else {
                     // Weird shit outside Exception or Error branches.
                     throw new RuntimeException( t );
                 }
             }
         }
 
         private ResponseHandler _handler = null;
         private List<Header> _requestedHeaders = new ArrayList<Header>( 8 );
         private int _statusCode = STATUS_UNKNOWN;
         private String _statusText = null;
         private List<Header> _requestHeaders = new ArrayList<Header>( 8 );
         private List<Header> _responseHeaders = new ArrayList<Header>( 8 );
         private ResizableByteBuffer _body = null;
         private final CountDownLatch _latch = new CountDownLatch(1);
     }
 
     private final HttpClient _client;
 
     private int _maxContentLength = 1024 * 1024 - 1;
     private ContentTypeSet _acceptedContentTypes = ContentTypeSet.ANY;
     private boolean _doCancelOnExpire = true;
 
     private final Logger _log = LoggerFactory.getLogger( getClass() );
 }
