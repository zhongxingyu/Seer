 package com.bradmcevoy.http;
 
 import com.bradmcevoy.http.http11.DefaultHttp11ResponseHandler;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.zip.GZIPOutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.webdav.WebDavResponseHandler;
 import com.bradmcevoy.io.BufferingOutputStream;
 import com.bradmcevoy.io.FileUtils;
 import com.bradmcevoy.io.ReadingException;
 import com.bradmcevoy.io.StreamUtils;
 import com.bradmcevoy.io.WritingException;
 import java.util.Date;
 
 /**
  * Response Handler which wraps another, and compresses content if appropriate
  * 
  * Usually, this will wrap a DefaultResponseHandler, but custom implementations
  * can be wrapped as well.
  *
  * @author brad
  */
 public class CompressingResponseHandler extends AbstractWrappingResponseHandler {
 
     private static final Logger log = LoggerFactory.getLogger( CompressingResponseHandler.class );
     /**
      * The size to buffer in memory before switching to disk cache.
      */
     private int maxMemorySize = 100000;
 
     public CompressingResponseHandler() {
     }
 
     public CompressingResponseHandler( WebDavResponseHandler wrapped ) {
         super( wrapped );
     }
 
     @Override
     public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException, BadRequestException {
        if( resource instanceof GetableResource ) {            
             GetableResource r = (GetableResource) resource;
 
             String acceptableContentTypes = request.getAcceptHeader();
             String contentType = r.getContentType( acceptableContentTypes );
 
             if( canCompress( r, contentType, request.getAcceptEncodingHeader() ) ) {
                log.trace( "respondContent: compressable" );
 
                 // get the zipped content before sending so we can determine its
                 // compressed size
                 BufferingOutputStream tempOut = new BufferingOutputStream( maxMemorySize );
                 try {
                     OutputStream gzipOut = new GZIPOutputStream( tempOut );
                     r.sendContent( gzipOut, null, params, contentType );
                     gzipOut.flush();
                     gzipOut.close();
                     tempOut.flush();
                 } catch( Exception ex ) {
                     throw new RuntimeException( ex );
                 } finally {
                     FileUtils.close( tempOut );
                 }
 
                 log.trace( "respondContent-compressed: " + resource.getClass() );
                 setRespondContentCommonHeaders( response, resource, Response.Status.SC_OK, request.getAuthorization() );
                 response.setContentEncodingHeader( Response.ContentEncoding.GZIP );
                response.setVaryHeader( "Accept-Encoding" );
                 Long contentLength = tempOut.getSize();
                 response.setContentLengthHeader( contentLength );
                 response.setContentTypeHeader( contentType );
                 DefaultHttp11ResponseHandler.setCacheControl( r, response, request.getAuthorization() );
                 try {
                     StreamUtils.readTo( tempOut.getInputStream(), response.getOutputStream() );
                 } catch( ReadingException ex ) {
                     throw new RuntimeException( ex );
                 } catch( WritingException ex ) {
                     log.warn( "exception writing, client probably closed connection", ex );
                 }
                 return;
            } else {
                log.trace( "respondContent: not compressable" );
                response.setVaryHeader( "Accept-Encoding" );
                wrapped.respondContent( resource, response, request, params );
             }
        } else {
            throw new RuntimeException( "Cant generate content for non-Getable resource: " + resource.getClass());
         }
     }
 
     protected void setRespondContentCommonHeaders( Response response, Resource resource, Response.Status status, Auth auth ) {
         response.setStatus( status );
         response.setDateHeader( new Date() );
         String etag = wrapped.generateEtag( resource );
         if( etag != null ) {
             response.setEtag( etag );
         }
         DefaultHttp11ResponseHandler.setModifiedDate( response, resource, auth );
     }
 
     private boolean canCompress( GetableResource r, String contentType, String acceptableEncodings ) {
         log.trace( "canCompress: contentType: " + contentType + " acceptable-encodings: " + acceptableEncodings );
         if( contentType != null ) {
             contentType = contentType.toLowerCase();
             boolean contentIsCompressable = contentType.contains( "text" ) || contentType.contains( "css" ) || contentType.contains( "js" ) || contentType.contains( "javascript" );
             if( contentIsCompressable ) {
                 boolean supportsGzip = ( acceptableEncodings != null && acceptableEncodings.toLowerCase().indexOf( "gzip" ) > -1 );
                 log.trace( "supports gzip: " + supportsGzip );
                 return supportsGzip;
             }
         }
         return false;
     }
 
     public void setMaxMemorySize( int maxMemorySize ) {
         this.maxMemorySize = maxMemorySize;
     }
 
     public int getMaxMemorySize() {
         return maxMemorySize;
     }
 }
