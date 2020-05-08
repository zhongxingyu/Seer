 package com.github.nmorel.homework.client.request;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.regexp.shared.MatchResult;
 import com.google.gwt.regexp.shared.RegExp;
 
 public class RestRequestBuilder
 {
     private static final Logger logger = Logger.getLogger( RestRequestBuilder.class.getName() );
 
     private String apiPath = "api/";
 
     private RequestBuilder builder;
 
     public RestRequestBuilder build( Method method, String path, List<String> args, Map<String, String> queryParams )
     {
         builder = new RequestBuilder( method, buildRequestUrl( path, args, queryParams ) );
         return this;
     }
 
     public RestRequestBuilder withHeader( String header, String value )
     {
         assertBuilderNotNull();
         builder.setHeader( header, value );
         return this;
     }
 
     public RestRequestBuilder withData( String data )
     {
         assertBuilderNotNull();
         builder.setRequestData( data );
         return this;
     }
 
     public RestRequestBuilder withCallback( RequestCallback callback )
     {
         assertBuilderNotNull();
         builder.setCallback( callback );
         return this;
     }
 
     public Request fire()
     {
         assertBuilderNotNull();
         try
         {
             return builder.send();
         }
         catch ( RequestException e )
         {
             logger.log( Level.SEVERE, "Error sending the request", e );
             return null;
         }
     }
 
     private void assertBuilderNotNull()
     {
         assert null != builder : "Call build() first";
     }
 
     /**
      * Build the request url
      * 
      * @return the built url
      */
     private String buildRequestUrl( String path, List<String> args, Map<String, String> queryParams )
     {
         StringBuilder sb = new StringBuilder( apiPath );
 
         // Replace arguments
         replaceArgs( path, sb, args );
 
         // Now add query parameters
         addQueryParameters( sb, queryParams );
 
         return sb.toString();
     }
 
     /**
      * Replace the path arguments
      * 
      * @param path
      * @return the path with replaced arguments
      */
     private void replaceArgs( String path, StringBuilder sb, List<String> args )
     {
         if ( null == args || args.isEmpty() )
         {
             sb.append( path );
             return;
         }
 
         RegExp regExp = RegExp.compile( "[{][^}]*[}]", "g" );
 
         Iterator<String> argsIterator = args.iterator();
         int fromIndex = 0;
         int length = path.length();
         MatchResult result;
 
         while ( fromIndex < length && argsIterator.hasNext() )
         {
             // Find the next match
             result = regExp.exec( path );
             if ( result == null )
             {
                 // No more matches
                 break;
             }
             int index = result.getIndex();
             String match = result.getGroup( 0 );
 
             // Append the characters leading up to the match
             sb.append( path.substring( fromIndex, index ) );
             // Append the argument
             sb.append( argsIterator.next() );
 
             // Skip past the matched string
             fromIndex = index + match.length();
             regExp.setLastIndex( fromIndex );
         }
 
         // Append the tail of the string
         if ( fromIndex < length )
         {
             sb.append( path.substring( fromIndex ) );
         }
     }
 
     /**
      * Add the query parameters to the url
      */
     private void addQueryParameters( StringBuilder sb, Map<String, String> queryParams )
     {
         if ( !queryParams.isEmpty() )
         {
             sb.append( "?" );
             boolean needsAmp = false;
             for ( Map.Entry<String, String> entry : queryParams.entrySet() )
             {
                 if ( needsAmp )
                 {
                     sb.append( "&" );
                 }
                 else
                 {
                     needsAmp = true;
                 }
                 sb.append( entry.getKey() ).append( "=" ).append( URL.encodeQueryString( entry.getValue() ) );
             }
         }
     }
 }
