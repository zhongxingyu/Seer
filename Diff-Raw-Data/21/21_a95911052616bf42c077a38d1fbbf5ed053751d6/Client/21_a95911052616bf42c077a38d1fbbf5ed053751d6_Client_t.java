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
 
 import iudex.http.ContentTypeSet;
 import iudex.http.HTTPClient;
 import iudex.http.HTTPSession;
 import iudex.http.Header;
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
 
     private class Session
         extends HTTPSession
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
                     Field field = fields.getField( 0 );
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
 
         public InputStream responseStream()
         {
             return Streams.inputStream( _body.flipAsByteBuffer() );
         }
 
         public void abort()
         {
             _exchange.cancel();
         }
 
         public void close()
         {
             //No-op?
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
             protected void onResponseContent( Buffer content )
             {
                 //FIXME: setup after checking Content-Length
                 if( _body == null ) {
                     _body = new ResizableByteBuffer( 8000 );
                 }
                 _body.put( wrap( content ) );
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
                 super.onExpire(); //FIXME: logs
                 onException( new TimeoutException( "expired" ) );
             }
 
             @Override
            public void onException( Throwable t ) throws Error
             {
                 if( t instanceof Exception ) {
                     // Ignore AbortedException
                     if( !( t instanceof AbortedException ) ) {
                         _handler.handleException( Session.this, (Exception) t );
                     }
                     //FIXME: Aborted useful here?
                 }
                 else {
                    _log.error( "Session onException (Throwable): ", t );
                    Thread.currentThread().interrupt();
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
 
         private List<Header> _requestedHeaders = new ArrayList<Header>( 8 );
 
         private ResponseHandler _handler = null;
 
         private int _responseCode = 0;
         private String _statusText = null;
 
         private ResizableByteBuffer _body = null;
 
         private ArrayList<Header> _responseHeaders = new ArrayList<Header>( 8 );
     }
 
     //FIXME: use?
     private class AbortedException extends Exception
     {
     }
 
     private final HttpClient _client;
 
     private int _maxContentLength = 2 * 1024 * 1024;
     private ContentTypeSet _acceptedContentTypes = ContentTypeSet.ANY;
 
     private final Logger _log = LoggerFactory.getLogger( getClass() );
 }
