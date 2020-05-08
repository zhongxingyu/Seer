 /*
  * Copyright (c) 2011 David Kellum
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
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.client.HttpExchange;
 import org.eclipse.jetty.http.HttpFields;
 import org.eclipse.jetty.http.HttpFields.Field;
 import org.eclipse.jetty.io.Buffer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gravitext.util.Charsets;
 import com.gravitext.util.Closeable;
 import com.gravitext.util.ResizableByteBuffer;
 import com.gravitext.util.Streams;
 
 public class Client implements HTTPClient, Closeable
 {
     public Client( HttpClient client )
     {
         _client = client;
     }
 
     @Override
     public HTTPSession createSession()
     {
         return new Session();
     }
 
     @Override
     public void request( HTTPSession session, ResponseHandler handler )
     {
         ((Session) session).execute( handler );
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
 
     @Override
     public void close()
     {
         try {
             _client.stop();
         }
         catch( Exception e ) {
             _log.warn( "On close: {}", e.toString() );
             _log.debug( "On close:", e );
         }
     }
 
     private class Session extends HTTPSession
     {
         public Session()
         {
             super();
             _exchange = new Exchange();
         }
 
         public void addRequestHeader( Header header )
         {
             _requestedHeaders.add( header );
         }
 
         public List<Header> requestHeaders()
         {
             if( _exchange.isDone() ) {
                 HttpFields fields = _exchange.getRequestFields();
 
                 List<Header> hs = new ArrayList<Header>( fields.size() + 1 );
                 hs.add( new Header( "Request-Line",
                                      reconstructRequestLine() ) );
 
                 final int end = fields.size();
                 for( int i = 0; i < end; ++i ) {
                    Field field = fields.getField( i );
                     hs.add( new Header( field.getName(), field.getValue() ) );
                 }
                 return hs;
             }
 
             return _requestedHeaders;
         }
 
         public int responseCode()
         {
             return _responseCode;
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
 
        @SuppressWarnings("unused")
        public ByteBuffer responseBody()
        {
            if ( _body != null ) {
                _body.flipAsByteBuffer();
            }
            return null;
        }

         public InputStream responseStream()
         {
            if ( _body != null ) {
                return Streams.inputStream( _body.flipAsByteBuffer() );
            }
            return null;
         }
 
         public void abort()
         {
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
             try {
                 _client.send( _exchange );
             }
             catch( IOException e ) {
                 _exchange.onException( e );
             }
         }
 
         @SuppressWarnings("unused")
         public void waitForCompletion() throws InterruptedException
         {
             if( _exchange != null ) _exchange.waitForDone();
         }
 
         private CharSequence reconstructRequestLine()
         {
             StringBuilder reqLine = new StringBuilder( 128 );
 
             reqLine.append( method().name() );
             reqLine.append( ' ' );
             reqLine.append( url() );
 
             //FIXME: Not correct
             //reqLine.append( '?' );
             //_request.getQueryParams();
 
             return reqLine;
         }
 
         private class Exchange extends HttpExchange
         {
 
             @Override
             protected void onRequestComplete()
             {
             }
 
             @Override
             protected void onResponseStatus( Buffer version,
                                              int status,
                                              Buffer reason )
             {
                 _responseCode = status;
                 _statusText = decode( reason ).toString();
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
                 //check Content-Type
                 ContentType ctype = Headers.contentType( _responseHeaders );
 
                 if( ! _acceptedContentTypes.contains( ctype ) ) {
                     _responseCode = -20; //FIXME: Constants in iudex.http?
                     abort();
                 }
                 else {
                     int length = Headers.contentLength( _responseHeaders );
                     if( length > _maxContentLength ) {
                         _responseCode = -10;
                         abort();
                     }
                     else {
                         _body = new ResizableByteBuffer(
                             ( length >= 0 ) ? length : 16 * 1024 );
                     }
                 }
             }
 
             @Override
             protected void onResponseContent( Buffer content )
             {
                 ByteBuffer chunk = wrap( content );
                 if( _body.position() + chunk.remaining() > _maxContentLength ) {
                     _responseCode = -11;
                     abort();
                 }
                 else {
                     _body.put( chunk );
                 }
             }
 
             @Override
             protected void onResponseComplete()
             {
                 if( ( _responseCode >= 200 ) && ( _responseCode < 300 ) ) {
                     _handler.handleSuccess( Session.this );
                 }
                 else {
                     _handler.handleError( Session.this, _responseCode );
                 }
             }
 
             @Override
             protected void onConnectionFailed( Throwable x )
             {
                 onException( x );
             }
 
             @Override
             protected void onExpire()
             {
                 onException( new TimeoutException( "expired" ) );
             }
 
             @Override
             public void onException( Throwable t ) throws Error
             {
                 if( t instanceof Exception ) {
                    _responseCode = -1;
                     _handler.handleException( Session.this, (Exception) t );
                 }
                 else {
                     _log.error( "Session onException (Throwable): ", t );
                    _responseCode = -2;
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
 
         private final Exchange _exchange;
         private ResponseHandler _handler = null;
 
         private List<Header> _requestedHeaders = new ArrayList<Header>( 8 );
 
         private int _responseCode = 0;
         private String _statusText = null;
         private ArrayList<Header> _responseHeaders = new ArrayList<Header>( 8 );
         private ResizableByteBuffer _body = null;
     }
 
     private final HttpClient _client;
 
     private int _maxContentLength = 2 * 1024 * 1024;
     private ContentTypeSet _acceptedContentTypes = ContentTypeSet.ANY;
 
     private final Logger _log = LoggerFactory.getLogger( getClass() );
 }
