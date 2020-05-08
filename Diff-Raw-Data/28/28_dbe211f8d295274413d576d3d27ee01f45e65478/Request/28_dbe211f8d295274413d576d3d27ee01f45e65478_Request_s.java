 package com.bradmcevoy.http;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Map;
 
 public interface Request {
 
     enum Depth {
         ZERO,
         ONE,
         INFINITY
     }
     
     enum CacheControlRequest {
         NO_CACHE("no-cache"),
         NO_STORE("no-store"),
         MAX_AGE("max-age"), // =delta-seconds
         MAX_STALE("max-stale"), // =delta-seconds
         MIN_FRESH("min-fresh"), // =delta-seconds
         NO_TRANSFORM("no-transform"),
         ONLY_IF_CACHED("only-if-cached"),
         CACHE_EXT("cache-extension");         
                 
         public String code;
         
         CacheControlRequest(String code) {
             this.code = code;
         }
     }
     
   
     
     enum Header {
         CACHE_CONTROL("Cache-Control"),
         WWW_AUTHENTICATE("WWW-Authenticate"),
         IF("If"),
         IF_MODIFIED("If-Modified-Since"),
         IF_NOT_MODIFIED("If-Unmodified-Since"),
         CONTENT_LENGTH("Content-Length"),
         CONTENT_TYPE("Content-Type"),
         CONTENT_RANGE("Content-Range"),
         DEPTH("Depth"),
         HOST("Host"),
         DESTINATION("Destination"),
         REFERER("Referer"),
         ACCEPT("Accept"),
         RANGE("Range"),
         ACCEPT_ENCODING("Accept-Encoding"),
         TIMEOUT("Timeout"),
         LOCK_TOKEN("Lock-Token"),
         EXPECT("Expect"),
         OVERWRITE("Overwrite"),
         AUTHORIZATION("Authorization");
         
             
         public String code;        
 
         
 
         Header( String code ) {
             this.code = code;
         }                
     }
     
     enum Method {
         HEAD( "HEAD",false ),
         PROPFIND( "PROPFIND",false ),
         PROPPATCH( "PROPPATCH",false ),
         MKCOL("MKCOL",true ),
         COPY( "COPY",true ),
         MOVE( "MOVE",true ),
         LOCK( "LOCK",true ),
         UNLOCK( "UNLOCK",true ),
         DELETE( "DELETE",true ),
         GET( "GET",false ),
         OPTIONS( "OPTIONS",false ),
         POST( "POST",true ),
         PUT( "PUT",true ),
         TRACE( "TRACE",false );
         
         public String code;
         public boolean isWrite;
         
         Method(String code, boolean isWrite) {
             this.code = code;
             this.isWrite = isWrite;
         }
     };
 
     public Map<String,String> getHeaders();
 
     public String getFromAddress();
 
     public String getLockTokenHeader();
 
     public String getRequestHeader(Request.Header header);
         
     public Method getMethod();
     
     public Auth getAuthorization();
 
     public String getRefererHeader();
 
     public String getTimeoutHeader();
     
     public String getIfHeader();
     
     Date getIfModifiedHeader();
 
     int getDepthHeader();
 
     /** Return the complete URL, including protocol, host and port (if specified)
      *  and path
      */
     String getAbsoluteUrl();
 
     /** Return the path portion of the url. This is everything following the 
      *  host and port. Will always begin with a leading slash
      */
     String getAbsolutePath();
     
     String getHostHeader();
 
     String getDestinationHeader();
 
     String getExpectHeader();
     
     InputStream getInputStream() throws IOException;
     
     void parseRequestParameters(Map<String,String> params, Map<String,FileItem> files) throws RequestParseException;
 
     String getContentTypeHeader();
     
     Long getContentLengthHeader();
 
     String getAcceptHeader();
 
     String getAcceptEncodingHeader();
 
     /**
      *
      * @return a range header, for partial gets
      */
     String getRangeHeader();
 
 
     /**
      * Used for MOVE method. If true it indicates that any existing resource
      * should be deleted before the move.
      *
      * @return - null if no value, true indicates that any existing resource
      * should be deleted
      */
     Boolean getOverwriteHeader();
 }
