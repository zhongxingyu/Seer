 package com.atlassian.sal.api.net;
 
 import java.io.InputStream;
 import java.util.Map;
 
 /**
  * Represents the response when calling {@link Request#execute(ResponseHandler)}
  *
  * @since 2.0
  */
 public interface Response
 {
     /**
      * @return status code of the request.
      */
     int getStatusCode();
 
     /**
      * @return the response body of the request.
      * @throws ResponseException If the response cannot be retrieved
      */
     String getResponseBodyAsString() throws ResponseException;
 
     /**
      * @return the response body of the request.
      * @throws ResponseException If the response cannot be retrieved
      */
     InputStream getResponseBodyAsStream() throws ResponseException;
 
     /**
      * Unmarshall the response body as the specified type
      *
      * @param entityClass the type of the response
      * @return the unmarshalled object
      * @throws ResponseException if there was difficulty reading the response or unmarshalling the object
      */
     <T> T getEntity(Class<T> entityClass) throws ResponseException;
 
     /**
      * @return status test of the response
      */
     String getStatusText();
 
     /**
      * @return true if network returned a status code in the 200 range or 300 range
      */
     boolean isSuccessful();
 
     /**
      * Get's the header by the given name
      *
      * @param name The name of the header
     * @return The value of the header.  This will include all values, comma
     * separated, if multiple header fields with the given name were specified,
     * as per RFC2616. When the specified header key is not present,
     * {@code null} is returned.
      */
     String getHeader(String name);
 
     /**
      * Get a map of all the headers
      *
      * @return A map of header names to header values.  The header values will include all values, comma seperated, if
      *         multiple header fields with the given name were specified, as per RFC2616.
      */
     Map<String, String> getHeaders();
 }
