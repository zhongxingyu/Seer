 package com.bradmcevoy.http.http11;
 
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.Response.Status;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.io.BufferingOutputStream;
 import com.bradmcevoy.io.ReadingException;
 import com.bradmcevoy.io.StreamUtils;
 import com.bradmcevoy.io.WritingException;
 
 /**
  *
  */
 public class DefaultHttp11ResponseHandler implements Http11ResponseHandler {
 
     public enum BUFFERING {
         always,
         never,
         whenNeeded
     }
 
     private static final Logger log = LoggerFactory.getLogger( DefaultHttp11ResponseHandler.class );
     public static final String METHOD_NOT_ALLOWED_HTML = "<html><body><h1>Method Not Allowed</h1></body></html>";
     public static final String NOT_FOUND_HTML = "<html><body><h1>${url} Not Found (404)</h1></body></html>";
     public static final String METHOD_NOT_IMPLEMENTED_HTML = "<html><body><h1>Method Not Implemented</h1></body></html>";
     public static final String CONFLICT_HTML = "<html><body><h1>Conflict</h1></body></html>";
     public static final String SERVER_ERROR_HTML = "<html><body><h1>Server Error</h1></body></html>";
     private final AuthenticationService authenticationService;
     private final ETagGenerator eTagGenerator;
     private int maxMemorySize = 100000;
     private BUFFERING buffering;
 
     public DefaultHttp11ResponseHandler( AuthenticationService authenticationService ) {
         this.authenticationService = authenticationService;
         this.eTagGenerator = new DefaultETagGenerator();
     }
 
     public DefaultHttp11ResponseHandler( AuthenticationService authenticationService, ETagGenerator eTagGenerator ) {
         this.authenticationService = authenticationService;
         this.eTagGenerator = eTagGenerator;
     }
 
     public String generateEtag( Resource r ) {
         return eTagGenerator.generateEtag( r );
     }
 
     public void respondWithOptions( Resource resource, Response response, Request request, List<String> methodsAllowed ) {
         response.setStatus( Response.Status.SC_OK );
         response.setAllowHeader( methodsAllowed );
         response.setContentLengthHeader( (long) 0 );
     }
 
     public void respondNotFound( Response response, Request request ) {
         response.setStatus( Response.Status.SC_NOT_FOUND );
         response.setContentTypeHeader( "text/html" );
         response.setStatus( Response.Status.SC_NOT_FOUND );
         PrintWriter pw = new PrintWriter( response.getOutputStream(), true );
 
         String s = NOT_FOUND_HTML.replace( "${url}", request.getAbsolutePath() );
         pw.print( s );
         pw.flush();
 
     }
 
     public void respondUnauthorised( Resource resource, Response response, Request request ) {
         response.setStatus( Response.Status.SC_UNAUTHORIZED );
         List<String> challenges = authenticationService.getChallenges( resource, request );
         response.setAuthenticateHeader( challenges );
     }
 
     public void respondMethodNotImplemented( Resource resource, Response response, Request request ) {
 //        log.debug( "method not implemented. resource: " + resource.getClass().getName() + " - method " + request.getMethod() );
         try {
             response.setStatus( Response.Status.SC_NOT_IMPLEMENTED );
             OutputStream out = response.getOutputStream();
             out.write( METHOD_NOT_IMPLEMENTED_HTML.getBytes() );
         } catch( IOException ex ) {
             log.warn( "exception writing content" );
         }
     }
 
     public void respondMethodNotAllowed( Resource res, Response response, Request request ) {
         log.debug( "method not allowed. handler: " + this.getClass().getName() + " resource: " + res.getClass().getName() );
         try {
             response.setStatus( Response.Status.SC_METHOD_NOT_ALLOWED );
             OutputStream out = response.getOutputStream();
             out.write( METHOD_NOT_ALLOWED_HTML.getBytes() );
         } catch( IOException ex ) {
             log.warn( "exception writing content" );
         }
     }
 
     /**
      *
      * @param resource
      * @param response
      * @param message - optional message to output in the body content
      */
     public void respondConflict( Resource resource, Response response, Request request, String message ) {
         log.debug( "respondConflict" );
         try {
             response.setStatus( Response.Status.SC_CONFLICT );
             OutputStream out = response.getOutputStream();
             out.write( CONFLICT_HTML.getBytes() );
         } catch( IOException ex ) {
             log.warn( "exception writing content" );
         }
     }
 
     public void respondRedirect( Response response, Request request, String redirectUrl ) {
         if( redirectUrl == null ) {
             throw new NullPointerException( "redirectUrl cannot be null" );
         }
         response.setStatus( Response.Status.SC_MOVED_TEMPORARILY );
         response.setLocationHeader( redirectUrl );
     }
 
     public void respondExpectationFailed( Response response, Request request ) {
         response.setStatus( Response.Status.SC_EXPECTATION_FAILED );
     }
 
     public void respondCreated( Resource resource, Response response, Request request ) {
 //        log.debug( "respondCreated" );
         response.setStatus( Response.Status.SC_CREATED );
     }
 
     public void respondNoContent( Resource resource, Response response, Request request ) {
 //        log.debug( "respondNoContent" );
         response.setStatus( Response.Status.SC_OK );
     }
 
     public void respondPartialContent( GetableResource resource, Response response, Request request, Map<String, String> params, Range range ) throws NotAuthorizedException, BadRequestException {
         log.debug( "respondPartialContent: " + range.getStart() + " - " + range.getFinish() );
         response.setStatus( Response.Status.SC_PARTIAL_CONTENT );
         response.setContentRangeHeader( range.getStart(), range.getFinish(), resource.getContentLength() );
         response.setDateHeader( new Date() );
         String etag = eTagGenerator.generateEtag( resource );
         if( etag != null ) {
             response.setEtag( etag );
         }
         String acc = request.getAcceptHeader();
         String ct = resource.getContentType( acc );
         if( ct != null ) {
             response.setContentTypeHeader( ct );
         }
         try {
             resource.sendContent( response.getOutputStream(), range, params, ct );
         } catch( IOException ex ) {
             log.warn( "IOException writing to output, probably client terminated connection", ex );
         }
     }
 
     public void respondHead( Resource resource, Response response, Request request ) {
         setRespondContentCommonHeaders( response, resource, Response.Status.SC_NO_CONTENT, request.getAuthorization() );
     }
 
     public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException, BadRequestException {
         log.debug( "respondContent: " + resource.getClass() );
         Auth auth = request.getAuthorization();
         setRespondContentCommonHeaders( response, resource, auth );
         if( resource instanceof GetableResource ) {
             GetableResource gr = (GetableResource) resource;
             String acc = request.getAcceptHeader();
             String ct = gr.getContentType( acc );
             if( ct != null ) {
                 response.setContentTypeHeader( ct );
             }
             setCacheControl( gr, response, request.getAuthorization() );
 
             Long contentLength = gr.getContentLength();
             if( buffering == BUFFERING.always || (contentLength != null && buffering == BUFFERING.whenNeeded) ) { // often won't know until rendered
                 log.trace( "sending content with known content length: " + contentLength);
                 response.setContentLengthHeader( contentLength );
                 sendContent( request, response, (GetableResource) resource, params, null, ct );
             } else {
                 log.trace( "buffering content...");
                 BufferingOutputStream tempOut = new BufferingOutputStream( maxMemorySize );
                 try {
                     ( (GetableResource) resource ).sendContent( tempOut, null, params, ct );
                     tempOut.close();
                 } catch( IOException ex ) {
                     throw new RuntimeException( "Exception generating buffered content", ex);
                 }
                 Long bufContentLength = tempOut.getSize();
                 if( contentLength != null ) {
                    if( contentLength != bufContentLength ) {
                         throw new RuntimeException( "Lengthd dont match: " + contentLength + " != " + bufContentLength);
                     }
                 }
                 log.trace( "sending buffered content...");
                 response.setContentLengthHeader( bufContentLength );
                 try {
                     StreamUtils.readTo( tempOut.getInputStream(), response.getOutputStream() );
                 } catch( ReadingException ex ) {
                     throw new RuntimeException( ex );
                 } catch( WritingException ex ) {
                     log.warn( "exception writing, client probably closed connection", ex );
                 }
                 return;
                 
 
             }
 
         }
     }
 
     public void respondNotModified( GetableResource resource, Response response, Request request ) {
         log.trace( "respondNotModified" );
         response.setStatus( Response.Status.SC_NOT_MODIFIED );
         response.setDateHeader( new Date() );
         String etag = eTagGenerator.generateEtag( resource );
         if( etag != null ) {
             response.setEtag( etag );
         }
 
         // Note that we use a simpler modified date handling here then when
         // responding with content, because in a not-modified situation the
         // modified date MUST be that of the actual resource
         Date modDate = resource.getModifiedDate();
         response.setLastModifiedHeader( modDate );
 
         setCacheControl( resource, response, request.getAuthorization() );
     }
 
     public static void setCacheControl( final GetableResource resource, final Response response, Auth auth ) {
         Long delta = resource.getMaxAgeSeconds( auth );
         if( log.isTraceEnabled() ) {
             log.trace( "setCacheControl: " + delta + " - " + resource.getClass() );
         }
         if( delta != null ) {
             if( auth != null ) {
                 response.setCacheControlPrivateMaxAgeHeader( delta );
                 //response.setCacheControlMaxAgeHeader(delta);
             } else {
                 response.setCacheControlMaxAgeHeader( delta );
             }
             Date expiresAt = calcExpiresAt( new Date(), delta.longValue() );
             if( log.isTraceEnabled() ) {
                 log.trace( "set expires: " + expiresAt );
             }
             response.setExpiresHeader( expiresAt );
         } else {
             response.setCacheControlNoCacheHeader();
         }
     }
 
     public static Date calcExpiresAt( Date modifiedDate, long deltaSeconds ) {
         long deltaMs = deltaSeconds * 1000;
         long expiresAt = System.currentTimeMillis() + deltaMs;
         return new Date( expiresAt );
     }
 
     protected void sendContent( Request request, Response response, GetableResource resource, Map<String, String> params, Range range, String contentType ) throws NotAuthorizedException, BadRequestException {
         log.trace( "sendContent" );
         OutputStream out = outputStreamForResponse( request, response, resource );
         try {
             resource.sendContent( out, null, params, contentType );
             out.flush();
         } catch( IOException ex ) {
             log.warn( "IOException sending content", ex );
         }
     }
 
     protected OutputStream outputStreamForResponse( Request request, Response response, GetableResource resource ) {
         OutputStream outToUse = response.getOutputStream();
         return outToUse;
     }
 
     protected void output( final Response response, final String s ) {
         PrintWriter pw = new PrintWriter( response.getOutputStream(), true );
         pw.print( s );
         pw.flush();
     }
 
     protected void setRespondContentCommonHeaders( Response response, Resource resource, Auth auth ) {
         setRespondContentCommonHeaders( response, resource, Response.Status.SC_OK, auth );
     }
 
     protected void setRespondContentCommonHeaders( Response response, Resource resource, Response.Status status, Auth auth ) {
         response.setStatus( status );
         response.setDateHeader( new Date() );
         String etag = eTagGenerator.generateEtag( resource );
         if( etag != null ) {
             response.setEtag( etag );
         }
         setModifiedDate( response, resource, auth );
     }
 
     /**
     The modified date response header is used by the client for content
     caching. It seems obvious that if we have a modified date on the resource
     we should set it.
     BUT, because of the interaction with max-age we should always set it
     to the current date if we have max-age
     The problem, is that if we find that a condition GET has an expired mod-date
     (based on maxAge) then we want to respond with content (even if our mod-date
     hasnt changed. But if we use the actual mod-date in that case, then the
     browser will continue to use the old mod-date, so will forever more respond
     with content. So we send a mod-date of now to ensure that future requests
     will be given a 304 not modified.*
      *
      * @param response
      * @param resource
      * @param auth
      */
     public static void setModifiedDate( Response response, Resource resource, Auth auth ) {
         Date modDate = resource.getModifiedDate();
         if( modDate != null ) {
 
             if( resource instanceof GetableResource ) {
                 GetableResource gr = (GetableResource) resource;
                 Long maxAge = gr.getMaxAgeSeconds( auth );
                 if( maxAge != null ) {
                     modDate = new Date(); // have max-age, so use current date
                 }
             }
             response.setLastModifiedHeader( modDate );
         }
     }
 
     public void respondBadRequest( Resource resource, Response response, Request request ) {
         response.setStatus( Response.Status.SC_BAD_REQUEST );
     }
 
     public void respondForbidden( Resource resource, Response response, Request request ) {
         response.setStatus( Response.Status.SC_FORBIDDEN );
     }
 
     public void respondDeleteFailed( Request request, Response response, Resource resource, Status status ) {
         response.setStatus( status );
     }
 
     public AuthenticationService getAuthenticationService() {
         return authenticationService;
     }
 
     public void respondServerError( Request request, Response response, String reason ) {
         try {
             response.setStatus( Status.SC_INTERNAL_SERVER_ERROR );
             OutputStream out = response.getOutputStream();
             out.write( SERVER_ERROR_HTML.getBytes() );
         } catch( IOException ex ) {
             throw new RuntimeException( ex );
         }
     }
 
     /**
      * Maximum size of data to hold in memory per request when buffering output
      * data.
      *
      * @return
      */
     public int getMaxMemorySize() {
         return maxMemorySize;
     }
 
     public void setMaxMemorySize( int maxMemorySize ) {
         this.maxMemorySize = maxMemorySize;
     }
 
     public BUFFERING getBuffering() {
         return buffering;
     }
 
     public void setBuffering( BUFFERING buffering ) {
         this.buffering = buffering;
     }
 
 
 
 
 }
