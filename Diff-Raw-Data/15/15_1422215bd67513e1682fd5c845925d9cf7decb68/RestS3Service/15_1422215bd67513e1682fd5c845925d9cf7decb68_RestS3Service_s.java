 /*
  * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
  * This is a java.net project, see https://jets3t.dev.java.net/
  * 
  * Copyright 2006 James Murty
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.jets3t.service.impl.rest.httpclient;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.HttpMethodRetryHandler;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.HeadMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
 import org.jets3t.service.impl.rest.XmlResponsesSaxParser.ListBucketHandler;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.security.AWSCredentials;
 import org.jets3t.service.utils.Mimetypes;
 import org.jets3t.service.utils.RestUtils;
 import org.jets3t.service.utils.ServiceUtils;
 
 /**
  * 
  * <p><b>Properties</b></p>
  * <p>This HttpClient-based implementation uses a small subset of the many 
  * {@link options available for HttpClient}. 
  * The following properties, obtained through {@link Jets3tProperties}, are used by this class:</p>
  * <table>
  * <tr><th>Property</th><th>Description</th><th>Default</th></tr>
  * <tr><td>httpclient.connection-timeout-ms</td>
  *   <td>How many milliseconds to wait before a connection times out. 0 means infinity</td>
  *   <td>60000</td></tr>
  * <tr><td>httpclient.socket-timeout-ms</td>
  *   <td>How many milliseconds to wait before a socket connection times out. 0 means infinity</td>
  *   <td>60000</td></tr>
  * <tr><td>httpclient.max-connections</td>
  *   <td>The maximum number of simultaneous connections to allow</td><td>10</td></tr>
  * <tr><td>httpclient.stale-checking-enabled</td>
  *   <td>"Determines whether stale connection check is to be used. Disabling stale connection check may  
  *   result in slight performance improvement at the risk of getting an I/O error when executing a request 
  *   over a connection that has been closed at the server side."</td><td>true</td></tr>
  * <tr><td>httpclient.tcp-no-delay-enabled</td>
  *   <td>"Determines whether Nagle's algorithm is to be used. The Nagle's algorithm tries to conserve 
  *   bandwidth by minimizing the number of segments that are sent. When applications wish to decrease 
  *   network latency and increase performance, they can disable Nagle's algorithm (by enabling 
  *   TCP_NODELAY). Data will be sent earlier, at the cost of an increase in bandwidth consumption 
  *   and number of packets.</td>
  *   <td>true</td></tr>
  * <tr><td>httpclient.retry-on-errors</td><td></td><td>true</td></tr>
  * </table>
  * 
  * @author James Murty
  */
 public class RestS3Service extends S3Service {
     private final Log log = LogFactory.getLog(RestS3Service.class);
 
     private static final String PROTOCOL_SECURE = "https";
     private static final String PROTOCOL_INSECURE = "http";
     private static final int PORT_SECURE = 443;
     private static final int PORT_INSECURE = 80;
     
     private final HostConfiguration insecureHostConfig = new HostConfiguration();
     private final HostConfiguration secureHostConfig = new HostConfiguration();
     
     private HttpClient httpClient = null;
     private MultiThreadedHttpConnectionManager connectionManager = null;
     
     public RestS3Service(AWSCredentials awsCredentials) throws S3ServiceException {
         super(awsCredentials);
         
         // Set HttpClient properties based on Jets3t Properties.
         insecureHostConfig.setHost(Constants.REST_SERVER_DNS, PORT_INSECURE, PROTOCOL_INSECURE);
         secureHostConfig.setHost(Constants.REST_SERVER_DNS, PORT_SECURE, PROTOCOL_SECURE);
                         
         HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
         connectionParams.setConnectionTimeout(Jets3tProperties.
             getIntProperty("httpclient.connection-timeout-ms", 60000));
         connectionParams.setSoTimeout(Jets3tProperties.
             getIntProperty("httpclient.socket-timeout-ms", 60000));
         connectionParams.setMaxConnectionsPerHost(insecureHostConfig, Jets3tProperties.
             getIntProperty("httpclient.max-connections", 10));
         connectionParams.setMaxConnectionsPerHost(secureHostConfig, Jets3tProperties.
             getIntProperty("httpclient.max-connections", 10));
         connectionParams.setStaleCheckingEnabled(Jets3tProperties.
             getBoolProperty("httpclient.stale-checking-enabled", true));
         connectionParams.setTcpNoDelay(Jets3tProperties.
             getBoolProperty("httpclient.tcp-no-delay-enabled", true));        
 
         connectionParams.setBooleanParameter("http.protocol.expect-continue", true);
                         
         connectionManager = new MultiThreadedHttpConnectionManager();
         connectionManager.setParams(connectionParams);
         
         HttpClientParams clientParams = new HttpClientParams();
         
         boolean retryOnErrors = Jets3tProperties.getBoolProperty("httpclient.retry-on-errors", true);
         if (!retryOnErrors) {
             // Replace default error retry handler with one that never retries.
             clientParams.setParameter(HttpClientParams.RETRY_HANDLER, new HttpMethodRetryHandler() {
                 public boolean retryMethod(HttpMethod method, IOException ioe, int executionCount) {
                     return false;
                 }
             });
         }
         
         httpClient = new HttpClient(clientParams, connectionManager);
     }
     
     protected void performRequest(HttpMethodBase httpMethod, int expectedResponseCode) 
         throws S3ServiceException 
     {
         try {
             log.debug("Performing " + httpMethod.getName() 
                     + " request, expecting response code " + expectedResponseCode);
                 
             // Perform the request.
             int responseCode = httpClient.executeMethod(httpMethod);
 
             String contentType = "";
             if (httpMethod.getResponseHeader("Content-Type") != null) {
                 contentType = httpMethod.getResponseHeader("Content-Type").getValue();
             }
             
             log.debug("Request returned with headers: " + Arrays.asList(httpMethod.getResponseHeaders()));
             log.debug("Request returned with Content-Type: " + contentType);
                     
             // Check we received the expected result code.
             if (responseCode != expectedResponseCode)
             {                                
                 if (Mimetypes.MIMETYPE_XML.equals(contentType)
                     && httpMethod.getResponseBodyAsStream() != null
                     && httpMethod.getResponseContentLength() != 0) 
                 {
                     log.debug("Received error response with XML message");
     
                     StringBuffer sb = new StringBuffer();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(
                         new HttpMethodReleaseInputStream(httpMethod)));
                     String line = null;
                     while ((line = reader.readLine()) != null) {
                         sb.append(line + "\n");
                     }
                     reader.close();
                     
                     // Throw exception containing the XML message document.
                     throw new S3ServiceException("S3 " + httpMethod.getName() 
                             + " failed.", sb.toString());
                 } else {
                     // Consume response content and release connection.
                    log.debug("Releasing error response without XML content");
                    byte[] responseBody = httpMethod.getResponseBody(); 
                    if (responseBody != null && responseBody.length > 0) 
                        throw new S3ServiceException("Should do something useful with this error response body");                
 
                     httpMethod.releaseConnection();
                     
                     // Throw exception containing the HTTP error fields.
                     throw new S3ServiceException("S3 " 
                         + httpMethod.getName() + " request failed. " 
                         + "ResponseCode=" + httpMethod.getStatusCode()
                        + ", ResponseMessage=" + httpMethod.getStatusText());
                 }
             }
             
             // Release immediately any connections without response bodies.
             if ((httpMethod.getResponseBodyAsStream() == null 
                 || httpMethod.getResponseBodyAsStream().available() == 0)
                 && httpMethod.getResponseContentLength() == 0) 
             {
                  log.debug("Releasing response without content");
                 byte[] responseBody = httpMethod.getResponseBody();
 
                 if (responseBody != null && responseBody.length > 0) 
                     throw new S3ServiceException("Oops, too keen to release connection with a non-empty response body");                
                 httpMethod.releaseConnection();
             }
             
             } catch (S3ServiceException e) {
                 throw e;
         } catch (Throwable t) {
             log.debug("Releasing method after error: " + t.getMessage());            
             httpMethod.releaseConnection();
             throw new S3ServiceException("S3 " + httpMethod.getName() + " connection failed", t);
         } 
     }
     
     protected String addRequestParametersToUrlPath(String urlPath, Map requestParameters) 
         throws S3ServiceException 
     {
         if (requestParameters != null) {
             Iterator reqPropIter = requestParameters.keySet().iterator();
             while (reqPropIter.hasNext()) {
                 Object key = reqPropIter.next();
                 Object value = requestParameters.get(key);
                 
                 urlPath += (urlPath.indexOf("?") < 0? "?" : "&")
                     + RestUtils.encodeUrlString(key.toString());
                 if (value != null && value.toString().length() > 0) {
                     urlPath += "=" + RestUtils.encodeUrlString(value.toString());
                     log.debug("Added request parameter: " + key + "=" + value);
                 } else {
                     log.debug("Added request parameter without value: " + key);                    
                 }
             }
         }    
         return urlPath;
     }
     
     protected void addRequestHeadersToConnection(
             HttpMethodBase httpMethod, Map requestHeaders) 
     {
         if (requestHeaders != null) {
             Iterator reqHeaderIter = requestHeaders.keySet().iterator();
             while (reqHeaderIter.hasNext()) {
                 String key = reqHeaderIter.next().toString();
                 String value = requestHeaders.get(key).toString();
                 
                 httpMethod.setRequestHeader(key.toString(), value.toString());
                 log.debug("Added request header to connection: " + key + "=" + value);
             }
         }                        
     }
     
     protected Map buildMapOfHeaders(Header[] headers) {
         Map headersMap = new HashMap();
         for (int i = 0; headers != null && i < headers.length; i++) {
             Header header = headers[i];
             headersMap.put(header.getName(), header.getValue());
         }
         return headersMap;
     }
     
     protected HttpMethodBase performRestHead(
         String path, Map requestParameters, Map requestHeaders) throws S3ServiceException 
     {
         // Add any request parameters.
         path = addRequestParametersToUrlPath(path, requestParameters);
                 
         HttpMethodBase httpMethod = setupConnection("HEAD", path);
         
         // Add authorization header.
         buildAuthorizationString(httpMethod, path);
         
         // Add all request headers.
         addRequestHeadersToConnection(httpMethod, requestHeaders);
         
         performRequest(httpMethod, 200);
         
         return httpMethod;
     }
 
     
     protected HttpMethodBase performRestGet(String path, Map requestParameters, Map requestHeaders) throws S3ServiceException 
     {
         // Add any request parameters.
         path = addRequestParametersToUrlPath(path, requestParameters);
                 
         HttpMethodBase httpMethod = setupConnection("GET", path);
         
         // Add authorization header.
         buildAuthorizationString(httpMethod, path);
         
         // Add all request headers.
         addRequestHeadersToConnection(httpMethod, requestHeaders);
         
         int expectedStatusCode = 200;
         if (requestHeaders != null && requestHeaders.containsKey("Range")) {
             // Partial data responses have a status code of 206. 
             expectedStatusCode = 206;
         }
         performRequest(httpMethod, expectedStatusCode);
         
         return httpMethod;
     }
     
     protected HttpMethodAndByteCount performRestPut(String path, Map metadata, 
             Map requestParameters, InputStream dataInputStream) throws S3ServiceException 
     {        
         // Add any request parameters.
         path = addRequestParametersToUrlPath(path, requestParameters);
 
         HttpMethodBase httpMethod = setupConnection("PUT", path);
         
         // Add all meta-data headers.
         if (metadata != null) {
             Iterator metaDataIter = metadata.keySet().iterator();
             while (metaDataIter.hasNext()) {                
                 String key = (String) metaDataIter.next();
                 Object value = metadata.get(key);
                 if (key == null || !(value instanceof String)) {
                     // Ignore invalid metadata.
                     continue;
                 }
                 
                 if (!key.equalsIgnoreCase("content-type") 
                     && !key.equalsIgnoreCase("content-length")
                     && !key.equalsIgnoreCase("content-language")
                     && !key.equalsIgnoreCase("expires")
                     && !key.equalsIgnoreCase("cache-control")
                     && !key.equalsIgnoreCase("content-disposition")
                     && !key.equalsIgnoreCase("content-encoding")
                     && !key.startsWith(Constants.REST_HEADER_PREFIX)) 
                 {
                     key = Constants.REST_METADATA_PREFIX + key;
                 }                
                 httpMethod.setRequestHeader(key, value.toString());
             }
         }
         
         buildAuthorizationString(httpMethod, path);
 
         long contentLength = 0;
         
         if (dataInputStream != null) {
             // Determine the content length for the data to upload, which will be   
             // automatically set as a header by the InputStreamRequestEntity object.
             contentLength = InputStreamRequestEntity.CONTENT_LENGTH_AUTO;
             if (metadata.containsKey("Content-Length")) {
                 contentLength = Long.parseLong((String) metadata.get("Content-Length"));
                 log.debug("Uploading object data with Content-Length: " + contentLength);                                            
             } else {
                 log.warn("Content-Length of data stream not set, will automatically determine data length in memory");
             }                
             
             ((PutMethod)httpMethod).setRequestEntity(new InputStreamRequestEntity(
                 dataInputStream, contentLength));
         } else {
             // Need an explicit Content-Length even if no data is being uploaded.
             httpMethod.setRequestHeader("Content-Length", "0");
         }
         
         performRequest(httpMethod, 200);
         
         if (dataInputStream != null) {
             // Respond with the actual guaranteed content length of the uploaded data.
             contentLength = ((PutMethod)httpMethod).getRequestEntity().getContentLength();
         }
         
         return new HttpMethodAndByteCount(httpMethod, contentLength);
     }
 
     protected HttpMethodBase performRestDelete(String path) throws S3ServiceException {        
         HttpMethodBase httpMethod = setupConnection("DELETE", path);
         buildAuthorizationString(httpMethod, path);
 
         performRequest(httpMethod, 204);
 
         // Release connection after DELETE (there's no response content)
          log.debug("Releasing HttpMethod after delete");            
         httpMethod.releaseConnection();
 
         return httpMethod;
     }
     
     protected HttpMethodBase setupConnection(String method, String path) throws S3ServiceException 
     {
         if (path == null) {
             throw new S3ServiceException("Cannot connect to S3 Service with a null path");
         }
 
         String resourceString = buildResourceStringFromPath(path);
 
         String url = null;
         if (isHttpsOnly()) {
             log.debug("REST/HTTP service is using HTTPS for all communication");
             url = PROTOCOL_SECURE + "://" + Constants.REST_SERVER_DNS + ":" + PORT_SECURE + resourceString;
         } else {
             url = PROTOCOL_INSECURE + "://" + Constants.REST_SERVER_DNS + ":" + PORT_INSECURE + resourceString;        
         }
         
         HttpMethodBase httpMethod = null;
         if ("PUT".equals(method)) {
             httpMethod = new PutMethod(url);
         } else if ("HEAD".equals(method)) {
             httpMethod = new HeadMethod(url);
         } else if ("GET".equals(method)) {
             httpMethod = new GetMethod(url);            
         } else if ("DELETE".equals(method)) {
             httpMethod = new DeleteMethod(url);            
         } else {
             throw new IllegalArgumentException("Unrecognised HTTP method name: " + method);
         }
         
         // Set mandatory Request headers.
         if (httpMethod.getRequestHeader("Date") == null) {
             httpMethod.setRequestHeader("Date", ServiceUtils.formatRfc822Date(new Date()));
         }
         if (httpMethod.getRequestHeader("Content-Type") == null) {
             httpMethod.setRequestHeader("Content-Type", "");
         }        
                                 
         return httpMethod;
     }
     
     private String buildResourceStringFromPath(String path) throws S3ServiceException {
         String resourceString = "/";            
         int paramsIndex = path.indexOf("?");
         if (paramsIndex >= 0) {
             resourceString += RestUtils.encodeUrlPath(path.substring(0, paramsIndex), "/")
                 + path.substring(paramsIndex, path.length());            
         } else {
             resourceString += RestUtils.encodeUrlPath(path, "/");
         }
         return resourceString;
     }
     
     protected void buildAuthorizationString(HttpMethodBase httpMethod, String path) throws S3ServiceException {
         if (isAuthenticatedConnection()) {
             log.debug("Adding authorization for AWS Access Key '" + getAWSCredentials().getAccessKey() + "'.");
         } else {
             log.warn("Service has no AWS Credential and is un-authenticated, skipping authorization");
             return;
         }
         
         String canonicalString = RestUtils.makeCanonicalString(
                 httpMethod.getName(), buildResourceStringFromPath(path), 
                 buildMapOfHeaders(httpMethod.getRequestHeaders()), null);
         log.debug("Canonical string ('|' is a newline): " + canonicalString.replace('\n', '|'));
         
         String signedCanonical = ServiceUtils.signWithHmacSha1(
                 getAWSCredentials().getSecretKey(), canonicalString);
         
         // Add encoded authorization to connection as HTTP Authorization header. 
         String authorizationString = "AWS " + getAWSCredentials().getAccessKey() + ":" + signedCanonical;
         httpMethod.setRequestHeader("Authorization", authorizationString);                                
     }
     
     public boolean isBucketAccessible(String bucketName) throws S3ServiceException {
         log.debug("Checking existence of bucket: " + bucketName);
 
         // Ensure bucket exists and is accessible by performing a HEAD request
         HttpMethodBase httpMethod = performRestHead(bucketName, null, null);
         
         // This request may return an XML document that we're not interested in. Clean this up.
         try {
             if (httpMethod.getResponseBodyAsStream() != null) {
                 httpMethod.getResponseBodyAsStream().close();
             }
         } catch (IOException e) {
             log.warn("Unable to close response body input stream", e);
         } finally {
              log.debug("Releasing un-wanted bucket HEAD response");            
             httpMethod.releaseConnection();
         }
         
         // If we get this far, the bucket exists.
         return true;
     }    
 
     public S3Bucket[] listAllBucketsImpl() throws S3ServiceException {
         log.debug("Listing all buckets for AWS user: " + getAWSCredentials().getAccessKey());
         
         String s3Path = ""; // Root path of S3 service lists the user's buckets.
         HttpMethodBase httpMethod =  performRestGet(s3Path, null, null);
         String contentType = httpMethod.getResponseHeader("Content-Type").getValue();
             
         if (!Mimetypes.MIMETYPE_XML.equals(contentType)) {
             throw new S3ServiceException("Expected XML document response from S3 but received content type " + 
                 contentType);
         }
 
         S3Bucket[] buckets = (new XmlResponsesSaxParser()).parseListMyBucketsResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getBuckets();
         return buckets;
     }
 
     public S3Object[] listObjectsImpl(String bucketName, String prefix, String delimiter, long maxListingLength) 
         throws S3ServiceException 
     {        
         HashMap parameters = new HashMap();
         if (prefix != null) {
             parameters.put("prefix", prefix);
         } 
         if (delimiter != null) {
             parameters.put("delimiter", delimiter);
         }
         if (maxListingLength > 0) {
             parameters.put("max-keys", String.valueOf(maxListingLength));
         }
 
         ArrayList objects = new ArrayList();        
         String priorLastKey = null;
         boolean incompleteListing = true;            
             
         while (incompleteListing) {
             if (priorLastKey != null) {
                 parameters.put("marker", priorLastKey);
             } else {
                 parameters.remove("marker");
             }
             
             HttpMethodBase httpMethod = performRestGet(bucketName, parameters, null);
             ListBucketHandler listBucketHandler = 
                 (new XmlResponsesSaxParser()).parseListBucketObjectsResponse(
                     new HttpMethodReleaseInputStream(httpMethod));
             
             S3Object[] partialObjects = listBucketHandler.getObjects();
             log.debug("Found " + partialObjects.length + " objects in one batch");
             objects.addAll(Arrays.asList(partialObjects));
             
             incompleteListing = listBucketHandler.isListingTruncated();
             if (incompleteListing) {
                 priorLastKey = listBucketHandler.getLastKey();                
                 log.debug("Yet to receive complete listing of bucket contents, "
                         + "querying for next batch of objects with marker: " + priorLastKey);
             }
         }
         log.debug("Found " + objects.size() + " objects in total");
         return (S3Object[]) objects.toArray(new S3Object[] {});        
     }
     
     public void deleteObjectImpl(String bucketName, String objectKey) throws S3ServiceException {
         deleteObject(bucketName + "/" + objectKey);
     }    
 
     protected void deleteObject(String path) throws S3ServiceException {
         log.debug("Deleting object with path: " + path);
         performRestDelete(path);
     }
 
     public AccessControlList getObjectAclImpl(String bucketName, String objectKey) throws S3ServiceException {
         String fullKey = bucketName + "/" + objectKey; 
         log.debug("Retrieving Access Control List for Object: " + fullKey);
         
         HashMap requestParameters = new HashMap();
         requestParameters.put("acl","");
 
         HttpMethodBase httpMethod = performRestGet(fullKey, requestParameters, null);
         return (new XmlResponsesSaxParser()).parseAccessControlListResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getAccessControlList();
     }
     
     public AccessControlList getBucketAclImpl(String bucketName) throws S3ServiceException {
         String fullKey = bucketName;
         log.debug("Retrieving Access Control List for Bucket: " + fullKey);
         
         HashMap requestParameters = new HashMap();
         requestParameters.put("acl","");
 
         HttpMethodBase httpMethod = performRestGet(fullKey, requestParameters, null);
         return (new XmlResponsesSaxParser()).parseAccessControlListResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getAccessControlList();
     }
 
     public void putObjectAclImpl(String bucketName, String objectKey, AccessControlList acl) 
         throws S3ServiceException 
     {        
         String fullKey = bucketName + "/" + objectKey;
         putAclImpl(fullKey, acl);
     }
     
     public void putBucketAclImpl(String bucketName, AccessControlList acl) 
         throws S3ServiceException 
     {        
         String fullKey = bucketName;
         putAclImpl(fullKey, acl);
     }
 
     protected void putAclImpl(String fullKey, AccessControlList acl) throws S3ServiceException 
     {
         log.debug("Setting Access Control List for key: " + fullKey);
 
         HashMap requestParameters = new HashMap();
         requestParameters.put("acl","");
         
         HashMap metadata = new HashMap();
         metadata.put("Content-Type", "text/plain");
 
         try {
             ByteArrayInputStream bais = new ByteArrayInputStream(
                     acl.toXml().getBytes(Constants.DEFAULT_ENCODING));
             performRestPut(fullKey, metadata, requestParameters, bais);
             bais.close();
         } catch (Exception e) {
             throw new S3ServiceException("Unable to encode ACL XML document", e);
         }
     }
     
     public S3Bucket createBucketImpl(String bucketName, AccessControlList acl) 
         throws S3ServiceException 
     {        
         log.debug("Creating bucket with name: " + bucketName);
         Map map = createObjectImpl(bucketName, null, null, null, null, acl);
         
         S3Bucket bucket = new S3Bucket(bucketName);
         bucket.setAcl(acl);
         bucket.replaceAllMetadata(map);
         return bucket;
     }
     
     public void deleteBucketImpl(String bucketName) throws S3ServiceException {
         deleteObject(bucketName);
     }    
     
     /**
      * Beware of high memory requirements when creating large S3 objects when the Content-Length
      * is not set in the object.
      */
     public S3Object putObjectImpl(String bucketName, S3Object object) throws S3ServiceException 
     {        
         log.debug("Creating Object with key " + object.getKey() + " in bucket " + bucketName);
 
         Map map = createObjectImpl(bucketName, object.getKey(), object.getContentType(), 
             object.getDataInputStream(), object.getMetadata(), object.getAcl());
 
         object.replaceAllMetadata(map);
         return object;
     }
     
     protected Map createObjectImpl(String bucketName, String objectKey, String contentType, 
         InputStream dataInputStream, Map metadata, AccessControlList acl) 
         throws S3ServiceException 
     {
         String s3Path = bucketName;
         s3Path += (objectKey != null? "/" + objectKey : "");
         
         if (metadata == null) {
             metadata = new HashMap();
         }
         if (contentType != null) {
             metadata.put("Content-Type", contentType);
         } else {
             metadata.put("Content-Type", Mimetypes.MIMETYPE_OCTET_STREAM);            
         }
         boolean putNonStandardAcl = false;
         if (acl != null) {
             if (AccessControlList.REST_CANNED_PRIVATE.equals(acl)) {
                 metadata.put(Constants.REST_HEADER_PREFIX + "acl", "private");
             } else if (AccessControlList.REST_CANNED_PUBLIC_READ.equals(acl)) { 
                 metadata.put(Constants.REST_HEADER_PREFIX + "acl", "public-read");
             } else if (AccessControlList.REST_CANNED_PUBLIC_READ_WRITE.equals(acl)) { 
                 metadata.put(Constants.REST_HEADER_PREFIX + "acl", "public-read-write");
             } else if (AccessControlList.REST_CANNED_AUTHENTICATED_READ.equals(acl)) {
                 metadata.put(Constants.REST_HEADER_PREFIX + "acl", "authenticated-read");
             } else {
                 putNonStandardAcl = true;
             }
         }
                         
         log.debug("Creating object with path " + s3Path + "." + 
             " Content-Type=" + metadata.get("Content-Type") +
             " Including data? " + (dataInputStream != null) +
             " Metadata: " + metadata +
             " ACL: " + acl
             );
         
         HttpMethodAndByteCount methodAndByteCount = performRestPut(s3Path, metadata, null, dataInputStream);
             
         // Consume response content.
         HttpMethodBase httpMethod = methodAndByteCount.getHttpMethod();
             
         Map map = new HashMap();
         map.putAll(metadata); // Keep existing metadata.
         map.putAll(convertHeadersToMap(httpMethod.getResponseHeaders()));
         map.put(S3Object.METADATA_HEADER_CONTENT_LENGTH, String.valueOf(methodAndByteCount.getByteCount()));
         map = ServiceUtils.cleanRestMetadataMap(map);
 
         if (putNonStandardAcl) {
             log.debug("Creating object '" + s3Path + "' with a non-canned ACL using REST, so an extra ACL Put is required");
             putAclImpl(s3Path, acl);
         }
         
         return map;
     }
     
     private Map convertHeadersToMap(Header[] headers) {
         HashMap map = new HashMap();
         for (int i = 0; i < headers.length; i++) {
             map.put(headers[i].getName(), headers[i].getValue());
         }
         return map;
     }    
 
     public S3Object getObjectDetailsImpl(String bucketName, String objectKey, Calendar ifModifiedSince, 
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags) 
         throws S3ServiceException 
     { 
         return getObjectImpl(true, bucketName, objectKey, 
             ifModifiedSince, ifUnmodifiedSince, ifMatchTags, ifNoneMatchTags, null, null);
     }
     
     public S3Object getObjectImpl(String bucketName, String objectKey, Calendar ifModifiedSince, 
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags, 
         Long byteRangeStart, Long byteRangeEnd) 
         throws S3ServiceException 
     {
         return getObjectImpl(false, bucketName, objectKey, ifModifiedSince, ifUnmodifiedSince, 
             ifMatchTags, ifNoneMatchTags, byteRangeStart, byteRangeEnd);
     }
 
     private S3Object getObjectImpl(boolean headOnly, String bucketName, String objectKey, 
         Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags, 
         String[] ifNoneMatchTags, Long byteRangeStart, Long byteRangeEnd) 
         throws S3ServiceException
     {
         log.debug("Retrieving " + (headOnly? "Head" : "All") + " information for bucket " + bucketName + " and object " + objectKey);
         
         HashMap requestHeaders = new HashMap();
         if (ifModifiedSince != null) {
             requestHeaders.put("If-Modified-Since", 
                 ServiceUtils.formatRfc822Date(ifModifiedSince.getTime()));
             log.debug("Only retrieve object if-modified-since:" + ifModifiedSince);
         }
         if (ifUnmodifiedSince != null) {
             requestHeaders.put("If-Unmodified-Since", 
                 ServiceUtils.formatRfc822Date(ifUnmodifiedSince.getTime()));
             log.debug("Only retrieve object if-unmodified-since:" + ifUnmodifiedSince);
         }
         if (ifMatchTags != null) {
             StringBuffer tags = new StringBuffer();
             for (int i = 0; i < ifMatchTags.length; i++) {
                 if (i > 0) {
                     tags.append(",");
                 }
                 tags.append(ifMatchTags[i]);
             }
             requestHeaders.put("If-Match", tags.toString());            
             log.debug("Only retrieve object by hash if-match:" + tags.toString());
         }
         if (ifNoneMatchTags != null) {
             StringBuffer tags = new StringBuffer();
             for (int i = 0; i < ifNoneMatchTags.length; i++) {
                 if (i > 0) {
                     tags.append(",");
                 }
                 tags.append(ifNoneMatchTags[i]);
             }
             requestHeaders.put("If-None-Match", tags.toString());            
             log.debug("Only retrieve object by hash if-none-match:" + tags.toString());
         }
         if (byteRangeStart != null || byteRangeEnd != null) {
             String range = "bytes="
                 + (byteRangeStart != null? byteRangeStart.toString() : "") 
                 + "-"
                 + (byteRangeEnd != null? byteRangeEnd.toString() : "");
             requestHeaders.put("Range", range);            
             log.debug("Only retrieve object if it is within range:" + range);
         }
         
         String fullkey = bucketName + (objectKey != null? "/" + objectKey : "");
         
         HttpMethodBase httpMethod = null;        
         if (headOnly) {
             httpMethod = performRestHead(fullkey, null, requestHeaders);    
         } else {
             httpMethod = performRestGet(fullkey, null, requestHeaders);
         }
         
         HashMap map = new HashMap();
         map.putAll(convertHeadersToMap(httpMethod.getResponseHeaders()));
 
         S3Object responseObject = new S3Object();
         responseObject.setBucketName(bucketName);
         responseObject.setKey(objectKey);
         responseObject.replaceAllMetadata(ServiceUtils.cleanRestMetadataMap(map));
         responseObject.setMetadataComplete(true); // Flag this object as having the complete metadata set.
         if (!headOnly) {
             HttpMethodReleaseInputStream releaseIS = new HttpMethodReleaseInputStream(httpMethod);
             responseObject.setDataInputStream(releaseIS);
         } else {                
             // Release connection after HEAD (there's no response content)
             log.debug("Releasing HttpMethod after HEAD");            
             httpMethod.releaseConnection();
         }
         
         return responseObject;
     }
     
     private class HttpMethodAndByteCount {
         private HttpMethodBase httpMethod = null;
         private long byteCount = 0;
         
         public HttpMethodAndByteCount(HttpMethodBase httpMethod, long byteCount) {
             this.httpMethod = httpMethod;
             this.byteCount = byteCount;
         }
 
         public HttpMethodBase getHttpMethod() {
             return httpMethod;
         }
 
         public long getByteCount() {
             return byteCount;
         }
     }
     
 }
