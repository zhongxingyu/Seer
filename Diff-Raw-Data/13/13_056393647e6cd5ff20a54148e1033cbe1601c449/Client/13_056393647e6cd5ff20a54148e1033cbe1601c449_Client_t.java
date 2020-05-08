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
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.UnresolvedAddressException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.jetty.client.Address;
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.client.HttpDestination;
 import org.eclipse.jetty.client.HttpExchange;
 import org.eclipse.jetty.http.HttpFields;
 import org.eclipse.jetty.http.HttpFields.Field;
 import org.eclipse.jetty.io.Buffer;
 import org.eclipse.jetty.io.Connection;
 import org.eclipse.jetty.io.EndPoint;
 import org.eclipse.jetty.util.thread.ExecutorThreadPool;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gravitext.util.Charsets;
 import com.gravitext.util.Closeable;
 import com.gravitext.util.ResizableByteBuffer;
 
 public class Client
     implements HTTPClient, Closeable
 {
     public Client( HttpClient client )
     {
         _client = client;
     }
 
     public void setExecutor( ExecutorService executor )
     {
         _client.setThreadPool( new ExecutorThreadPool( executor ) );
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
 
     private class Session extends HTTPSession
     {
         public void addRequestHeader( Header header )
         {
             _requestedHeaders.add( header );
         }
 
         public List<Header> requestHeaders()
         {
             return _exchange.requestHeaders();
             //FIXME: Give requestedHeaders before execute?
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
 
         public void abort()
         {
             _log.debug( "Aborting with statusCode: {}", _statusCode );
             _body = null;
             _exchange.onResponseComplete();
             _exchange.cancel();
         }
 
         public void close()
         {
             //No-op
         }
 
         void execute( ResponseHandler handler )
         {
             _handler = handler;
 
             _exchange.setMethod( method().name() );
             _exchange.setURL( url() );
 
             for( Header h : _requestedHeaders ) {
                 _exchange.setRequestHeader(  h.name().toString(),
                                              h.value().toString() );
             }
 
             _exchange.onSend();
 
             try {
                 _client.send( _exchange );
             }
             catch( IOException e ) {
                 _exchange.onException( e );
             }
         }
 
         private void complete()
         {
             ResponseHandler handler = _handler;
             if( handler == null ) {
                 throw new IllegalStateException(
                    "Handler already completed!" );
             }
             _exchange.onComplete();
 
             _handler = null;
             handler.sessionCompleted( this );
         }
 
         @SuppressWarnings("unused")
         public void waitForCompletion() throws InterruptedException
         {
             if( _exchange != null ) _exchange.waitForDone();
         }
 
         private class Exchange extends HttpExchange
         {
             /**
              * Called by session for tracking.
              */
             void onSend()
             {
                 _log.debug( "onSend Exchange {}", this );
             }
 
             /**
              * Called by session.complete() for tracking.
              */
             void onComplete()
             {
                _log.debug( "onComplete Exchange {}", this );
             }
 
             @Override
             protected void onRequestComplete()
             {
                 _log.debug( "onRequestComplete: {}", this );
             }
 
             @Override
             protected void onResponseStatus( Buffer version,
                                              int status,
                                              Buffer reason )
             {
                 _statusCode = status;
                 if( reason != null ) {
                     _statusText = decode( reason ).toString();
                 }
                 else {
                     _statusText = null;
                 }
                 _log.debug( "onResponseStatus: {} {}",
                             _statusCode, _statusText );
 
                 try {
                     String lastURL = lastURL();
                     _log.debug( "onResponseStatus: url: {}", lastURL );
                     Session.this.setUrl( lastURL );
                 }
                 catch( URISyntaxException e ) {
                     onException( e );
                     cancel();
                 }
             }
 
             @Override
             public void onResponseHeader( Buffer name, Buffer value )
             {
                 _responseHeaders.add( new Header( decode( name ),
                                                   decode( value ) ) );
             }
 
             @Override
             protected void onResponseHeaderComplete()
             {
                 if( _statusCode == 200 ) {
 
                     //check Content-Type
                     ContentType ctype = Headers.contentType( _responseHeaders );
 
                     if( ! acceptedContentTypes().contains( ctype ) ) {
                         _statusCode = NOT_ACCEPTED;
                         _statusText = null;
                         abort();
                     }
                     else {
                         int length = Headers.contentLength( _responseHeaders );
                         if( length > maxContentLength() ) {
                             _statusCode = TOO_LARGE_LENGTH;
                             _statusText = null;
                             abort();
                         }
                         else {
                             _body = new ResizableByteBuffer(
                                         ( length >= 0 ) ? length : 16 * 1024 );
                         }
                     }
                 }
             }
 
             @Override
             protected void onResponseContent( Buffer content )
             {
                 if( _body != null ) {
                     ByteBuffer chunk = wrap( content );
                     if( ( _body.position() + chunk.remaining() ) >
                         maxContentLength() ) {
                         _statusCode = TOO_LARGE;
                         _statusText = null;
                         abort();
                     }
                     else {
                         _body.put( chunk );
                     }
                 }
                 else {
                     _log.debug( "Ignoring onResponseContent" );
                 }
             }
 
             @Override
             protected void onResponseComplete()
             {
                 complete();
             }
 
             @Override
             protected void onConnectionFailed( Throwable x )
             {
                 onException( x );
             }
 
             @Override
             protected void onExpire()
             {
                _statusCode = TIMEOUT;
                setError( new Expired() );
                if( _doCancelOnExpire ) cancel();
                complete();
             }
 
             @Override
             protected void onException( Throwable t ) throws Error
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
                     else {
                         _statusCode = ERROR;
                     }
 
                     setError( (Exception) t );
                     complete();
                 }
                 else {
                     _log.error( "Session onException (Throwable): ", t );
                     _statusCode = ERROR_CRITICAL;
                     Session.this.abort();
 
                     if( t instanceof Error) {
                         throw (Error) t;
                     }
                     else {
                         // Weird shit outside Exception or Error branches.
                         throw new RuntimeException( t );
                     }
                 }
             }
 
             @Override
             protected void onRetry() throws IOException
             {
                 super.onRetry();
             }
 
             @Override
             protected Connection onSwitchProtocol( EndPoint endp )
                 throws IOException
             {
                 _log.debug( "onSwitchProtocol: host {}",
                             ( endp == null ) ? null : endp.getRemoteHost() );
                 return super.onSwitchProtocol( endp );
             }
 
             @Override
             protected void expire( HttpDestination destination )
             {
                 try {
                     super.expire( destination );
                 }
                 catch( IllegalStateException x ) {
                     _log.debug( "On expire: ", x );
                     onException( x );
                     cancel();
                 }
             }
 
             List<Header> requestHeaders()
             {
                 HttpFields fields = getRequestFields();
 
                 List<Header> hs = new ArrayList<Header>( fields.size() + 1 );
                 hs.add( new Header("Request-Line", reconstructRequestLine()) );
 
                 final int end = fields.size();
                 for( int i = 0; i < end; ++i ) {
                     Field field = fields.getField( i );
                     hs.add( new Header( field.getName(), field.getValue() ) );
                 }
                 return hs;
             }
 
             private CharSequence reconstructRequestLine()
             {
                 StringBuilder req = new StringBuilder( 128 );
 
                 return req.append( getMethod() ).
                            append( ' ' ).
                            append( getRequestURI() );
             }
 
             private String lastURL() throws URISyntaxException
             {
                 try {
                     Address adr = getAddress();
                     String scheme = decode( getScheme() ).toString();
                     int port = adr.getPort();
                     if( ( scheme.equals( "http" ) && port == 80 ) ||
                         ( scheme.equals( "https" ) && port == 443 ) ) {
                         port = -1;
                     }
 
                     URI uri = new URI( scheme,
                                        null,
                                        adr.getHost(),
                                        port,
                                        null,
                                        null,
                                        null );
 
                     uri = uri.resolve( getRequestURI() );
                     return uri.toString();
                 }
                 // URI can also throw IllegalArgumentException wrapping
                 // a URISyntaxException. Unwrap it.
                 catch ( IllegalArgumentException x ) {
                     Throwable cause = x.getCause();
                     if( ( cause != null ) &&
                         ( cause instanceof URISyntaxException ) ) {
                         throw (URISyntaxException) cause;
                     }
                     else {
                         throw x;
                     }
                 }
             }
 
             private CharBuffer decode( Buffer b )
             {
                 return Charsets.ISO_8859_1.decode( wrap( b ) );
             }
 
             private ByteBuffer wrap( Buffer b )
             {
                 byte[] array = b.array();
                 if( array != null ) {
                     return ByteBuffer.wrap( array, b.getIndex(), b.length() );
                 }
                 else {
                     return ByteBuffer.wrap( b.asArray(), 0, b.length() );
                 }
             }
 
         }
 
         private final Exchange _exchange = new Exchange();
         private ResponseHandler _handler = null;
 
         private List<Header> _requestedHeaders = new ArrayList<Header>( 8 );
 
         private int _statusCode = STATUS_UNKNOWN;
         private String _statusText = null;
         private ArrayList<Header> _responseHeaders = new ArrayList<Header>( 8 );
         private ResizableByteBuffer _body = null;
     }
 
     private final HttpClient _client;
 
     private int _maxContentLength = 1024 * 1024 - 1;
     private ContentTypeSet _acceptedContentTypes = ContentTypeSet.ANY;
    private boolean _doCancelOnExpire = true;
 
     private final Logger _log = LoggerFactory.getLogger( getClass() );
 }
