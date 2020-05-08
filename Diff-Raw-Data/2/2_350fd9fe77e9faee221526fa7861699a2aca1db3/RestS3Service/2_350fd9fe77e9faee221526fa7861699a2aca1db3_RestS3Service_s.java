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
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.text.ParseException;
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
 import org.apache.commons.httpclient.ProxyHost;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.auth.CredentialsProvider;
 import org.apache.commons.httpclient.contrib.proxy.PluginProxyUtil;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.HeadMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 import org.jets3t.service.S3ObjectsChunk;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.impl.rest.HttpException;
 import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
 import org.jets3t.service.impl.rest.XmlResponsesSaxParser.ListBucketHandler;
 import org.jets3t.service.io.UnrecoverableIOException;
 import org.jets3t.service.model.CreateBucketConfiguration;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3BucketLoggingStatus;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.security.AWSCredentials;
 import org.jets3t.service.utils.Mimetypes;
 import org.jets3t.service.utils.RestUtils;
 import org.jets3t.service.utils.ServiceUtils;
 import org.jets3t.service.utils.signedurl.SignedUrlHandler;
 
 /**
  * REST/HTTP implementation of an S3Service based on the 
  * <a href="http://jakarta.apache.org/commons/httpclient/">HttpClient</a> library.  
  * <p>
  * This class uses properties obtained through {@link Jets3tProperties}. For more information on 
  * these properties please refer to 
  * <a href="http://jets3t.s3.amazonaws.com/toolkit/configuration.html">JetS3t Configuration</a>
  * </p>
  * 
  * @author James Murty
  */
 public class RestS3Service extends S3Service implements SignedUrlHandler {
     private static final long serialVersionUID = 3838005476674207543L;
 
     private final Log log = LogFactory.getLog(RestS3Service.class);
 
     private static final String PROTOCOL_SECURE = "https";
     private static final String PROTOCOL_INSECURE = "http";
     private static final int PORT_SECURE = 443;
     private static final int PORT_INSECURE = 80;
         
     private HttpClient httpClient = null;
     private MultiThreadedHttpConnectionManager connectionManager = null;
     
     /**
      * Constructs the service and initialises the properties.
      * 
      * @param awsCredentials
      * the S3 user credentials to use when communicating with S3, may be null in which case the
      * communication is done as an anonymous user.
      * 
      * @throws S3ServiceException
      */
     public RestS3Service(AWSCredentials awsCredentials) throws S3ServiceException {
         this(awsCredentials, null, null);
     }
     
     /**
      * Constructs the service and initialises the properties.
      * 
      * @param awsCredentials
      * the S3 user credentials to use when communicating with S3, may be null in which case the
      * communication is done as an anonymous user.
      * @param invokingApplicationDescription
      * a short description of the application using the service, suitable for inclusion in a
      * user agent string for REST/HTTP requests. Ideally this would include the application's
      * version number, for example: <code>Cockpit/0.6.0</code> or <code>My App Name/1.0</code>
      * @param credentialsProvider
      * an implementation of the HttpClient CredentialsProvider interface, to provide a means for
      * prompting for credentials when necessary.
      *   
      * @throws S3ServiceException
      */
     public RestS3Service(AWSCredentials awsCredentials, String invokingApplicationDescription, 
         CredentialsProvider credentialsProvider) throws S3ServiceException 
     {
         super(awsCredentials, invokingApplicationDescription);
         
         Jets3tProperties jets3tProperties = Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME);
         
         // Set HttpClient properties based on Jets3t Properties.
         HostConfiguration hostConfig = new HostConfiguration();
                         
         HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
         connectionParams.setConnectionTimeout(jets3tProperties.
             getIntProperty("httpclient.connection-timeout-ms", 60000));
         connectionParams.setSoTimeout(jets3tProperties.
             getIntProperty("httpclient.socket-timeout-ms", 60000));        
         connectionParams.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION,
             jets3tProperties.getIntProperty("httpclient.max-connections", 4));
         connectionParams.setStaleCheckingEnabled(jets3tProperties.
             getBoolProperty("httpclient.stale-checking-enabled", true));
         
         // Connection properties to take advantage of S3 window scaling.
         if (jets3tProperties.containsKey("httpclient.socket-receive-buffer")) {
             connectionParams.setReceiveBufferSize(jets3tProperties.
                 getIntProperty("httpclient.socket-receive-buffer", 0));
         }
         if (jets3tProperties.containsKey("httpclient.socket-send-buffer")) {
             connectionParams.setSendBufferSize(jets3tProperties.
                 getIntProperty("httpclient.socket-send-buffer", 0));
         }
         
         connectionParams.setTcpNoDelay(true);
         
         connectionManager = new MultiThreadedHttpConnectionManager();
         connectionManager.setParams(connectionParams);
         
         // Set user agent string.
         HttpClientParams clientParams = new HttpClientParams();
         String userAgent = jets3tProperties.getStringProperty("httpclient.useragent", null);
         if (userAgent == null) {
             userAgent = ServiceUtils.getUserAgentDescription(
                 getInvokingApplicationDescription());
         }
         log.debug("Setting user agent string: " + userAgent);
         clientParams.setParameter(HttpMethodParams.USER_AGENT, userAgent);
 
         clientParams.setBooleanParameter("http.protocol.expect-continue", true);
 
         // Replace default error retry handler.
         final int retryMaxCount = jets3tProperties.getIntProperty("httpclient.retry-max", 5);
         
         clientParams.setParameter(HttpClientParams.RETRY_HANDLER, new HttpMethodRetryHandler() {
             public boolean retryMethod(HttpMethod httpMethod, IOException ioe, int executionCount) {
                 if (executionCount > retryMaxCount) {
                     log.warn("Retried connection " + executionCount 
                         + " times, which exceeds the maximum retry count of " + retryMaxCount);
                     return false;                    
                 }
                 if  (ioe instanceof UnrecoverableIOException) {
                     log.debug("Deliberate interruption, will not retry");
                     return false;
                 }
                 log.warn("Retrying " + httpMethod.getName() + " request with path '" 
                     + httpMethod.getPath() + "' - attempt " + executionCount 
                     + " of " + retryMaxCount);
                 
                 // Build the authorization string for the method.
                 try {
                     buildAuthorizationString(httpMethod);
                 } catch (S3ServiceException e) {
                     log.warn("Unable to generate updated authorization string for retried request", e);
                 }
                 
                 return true;
             }
         });
         
         httpClient = new HttpClient(clientParams, connectionManager);
         httpClient.setHostConfiguration(hostConfig);
 
         // Retrieve Proxy settings.
         boolean proxyAutodetect = jets3tProperties.getBoolProperty("httpclient.proxy-autodetect", true);        
         String proxyHostAddress = jets3tProperties.getStringProperty("httpclient.proxy-host", null);
         int proxyPort = jets3tProperties.getIntProperty("httpclient.proxy-port", -1);
         
         // Use explicit proxy settings, if available.
         if (proxyHostAddress != null && proxyPort != -1) {
             log.info("Using Proxy: " + proxyHostAddress + ":" + proxyPort);
             hostConfig.setProxy(proxyHostAddress, proxyPort);
         }
         // If no explicit settings are available, try autodetecting proxies (unless autodetect is disabled)
         else if (proxyAutodetect) {        
             // Try to detect any proxy settings from applet.
             ProxyHost proxyHost = null;
             try {            
                 proxyHost = PluginProxyUtil.detectProxy(new URL("http://" + Constants.S3_HOSTNAME));
                 if (proxyHost != null) {
                     log.info("Using Proxy: " + proxyHost.getHostName() + ":" + proxyHost.getPort());
                     hostConfig.setProxyHost(proxyHost);
                 }                
             } catch (Throwable t) {
                 log.debug("Unable to set proxy configuration", t);
             }        
         }
                 
         if (credentialsProvider != null) {
             log.debug("Using credentials provider class: " + credentialsProvider.getClass().getName());
             httpClient.getParams().setParameter(CredentialsProvider.PROVIDER, credentialsProvider);
             httpClient.getParams().setAuthenticationPreemptive(true);
         }                          
     }
     
     /**
      * Performs an HTTP/S request by invoking the provided HttpMethod object. If the HTTP
      * response code doesn't match the expected value, an exception is thrown.
      * 
      * @param httpMethod
      *        the object containing a request target and all other information necessary to perform the 
      *        request
      * @param expectedResponseCode
      *        the HTTP response code that indicates a successful request. If the response code received
      *        does not match this value an error must have occurred, so an exception is thrown.
      * @throws S3ServiceException
      *        all exceptions are wrapped in an S3ServiceException. Depending on the kind of error that 
      *        occurred, this exception may contain additional error information available from an XML
      *        error response document.  
      */
     protected void performRequest(HttpMethodBase httpMethod, int expectedResponseCode) 
         throws S3ServiceException 
     {
         try {
             log.debug("Performing " + httpMethod.getName() 
                     + " request for '" + httpMethod.getURI().toString() 
                     + "', expecting response code " + expectedResponseCode);
 
             // Variables to manage S3 Internal Server 500 errors.
             boolean completedWithoutRecoverableError = true;
             int internalErrorCount = 0;
             int requestTimeoutErrorCount = 0;
             int redirectCount = 0;
             boolean wasRecentlyRedirected = false;
 
             // Perform the request, sleeping and retrying when S3 Internal Errors are encountered.
             int responseCode = -1;
             do {
                 // Build the authorization string for the method (Unless we have just been redirected).
                 if (!wasRecentlyRedirected) {
                     buildAuthorizationString(httpMethod);
                 } else {
                     // Reset redirection flag
                     wasRecentlyRedirected = false;
                 }
                 
                 responseCode = httpClient.executeMethod(httpMethod);
 
                 if (responseCode == 307) {
                     // Retry on Temporary Redirects, using new URI from location header                    
                     Header locationHeader = httpMethod.getResponseHeader("location");
                     httpMethod.setURI(new URI(locationHeader.getValue(), true));
                     
                     completedWithoutRecoverableError = false;
                     redirectCount++;
                     wasRecentlyRedirected = true;
                     
                     if (redirectCount > 5) {
                         throw new S3ServiceException("Encountered too many 307 Redirects, aborting request.");
                     } 
                 } else if (responseCode == 500) {
                     // Retry on S3 Internal Server 500 errors.
                     completedWithoutRecoverableError = false;
                     sleepOnInternalError(++internalErrorCount);
                 } else {
                     completedWithoutRecoverableError = true;                    
                 }
 
                 String contentType = "";
                 if (httpMethod.getResponseHeader("Content-Type") != null) {
                     contentType = httpMethod.getResponseHeader("Content-Type").getValue();
                 }
                 
                 log.debug("Response for '" + httpMethod.getPath() 
                     + "'. Content-Type: " + contentType
                     + ", Headers: " + Arrays.asList(httpMethod.getResponseHeaders()));
                         
                 // Check we received the expected result code.
                 if (responseCode != expectedResponseCode) {                
                     log.warn("Response '" + httpMethod.getPath() + "' - Unexpected response code " 
                         + responseCode + ", expected " + expectedResponseCode);
                                         
                     if (Mimetypes.MIMETYPE_XML.equals(contentType)
                         && httpMethod.getResponseBodyAsStream() != null
                         && httpMethod.getResponseContentLength() != 0) 
                     {
                         log.warn("Response '" + httpMethod.getPath() 
                             + "' - Received error response with XML message");
         
                         StringBuffer sb = new StringBuffer();
                         BufferedReader reader = null;
                         try {
                             reader = new BufferedReader(new InputStreamReader(
                                 new HttpMethodReleaseInputStream(httpMethod)));
                             String line = null;
                             while ((line = reader.readLine()) != null) {
                                 sb.append(line + "\n");
                             }
                         } finally {
                             if (reader != null) {
                                 reader.close();                                
                             }                            
                         }
                         
                         httpMethod.releaseConnection();
                         
                         // Throw exception containing the XML message document.
                         S3ServiceException exception = 
                             new S3ServiceException("S3 " + httpMethod.getName() 
                                 + " failed for '" + httpMethod.getPath() + "'", sb.toString());
                         
                         if ("RequestTimeout".equals(exception.getS3ErrorCode())) {
                             int retryMaxCount = Jets3tProperties
                                 .getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
                                 .getIntProperty("httpclient.retry-max", 5);                            
                             
                             if (requestTimeoutErrorCount < retryMaxCount) {
                                 requestTimeoutErrorCount++;                                
                                 log.warn("Response '" + httpMethod.getPath() 
                                     + "' - Retrying connection that failed with RequestTimeout error"
                                     + ", attempt number " + requestTimeoutErrorCount + " of " 
                                     + retryMaxCount);
                                 completedWithoutRecoverableError = false;
                             } else {
                                 log.warn("Response '" + httpMethod.getPath() 
                                     + "' - Exceeded maximum number of retries for RequestTimeout errors: "
                                     + retryMaxCount);
                                 throw exception;
                             }
                         } else if ("RequestTimeTooSkewed".equals(exception.getS3ErrorCode())) {
                             long timeDifferenceMS = adjustTime();
                             log.warn("Adjusted time offset in response to RequestTimeTooSkewed error. " 
                                 + "Local machine and S3 server disagree on the time by approximately " 
                                 + (timeDifferenceMS / 1000) + " seconds. Retrying connection.");
                             completedWithoutRecoverableError = false;
                         } else if (responseCode == 500) {
                             // Retrying after InternalError 500, don't throw exception.
                         } else if (responseCode == 307) {
                             // Retrying after Temporary Redirect 307, don't throw exception.
                             log.debug("Following Temporary Redirect to: " + httpMethod.getURI().toString());                            
                         } else {
                             throw exception;                            
                         }
                     } else {                        
                         // Consume response content and release connection.
                         String responseText = null; 
                         byte[] responseBody = httpMethod.getResponseBody();
                         if (responseBody != null && responseBody.length > 0) {
                             responseText = new String(responseBody);
                         }
     
                         log.debug("Releasing error response without XML content");
                         httpMethod.releaseConnection();
                         
                         if (responseCode == 500) {
                             // Retrying after InternalError 500, don't throw exception.
                         } else {
                             // Throw exception containing the HTTP error fields.
                         	HttpException httpException = new HttpException(
                         			httpMethod.getStatusCode(), httpMethod.getStatusText());
                             throw new S3ServiceException("S3 " + httpMethod.getName() 
                                 + " request failed for '" + httpMethod.getPath() + "' - " 
                                 + "ResponseCode=" + httpMethod.getStatusCode()
                                 + ", ResponseMessage=" + httpMethod.getStatusText()
                                 + (responseText != null ? "\n" + responseText : ""),
                                 httpException);
                         }
                     }
                 }
             } while (!completedWithoutRecoverableError);
 
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
             throw new S3ServiceException("S3 " + httpMethod.getName() 
                 + " connection failed for '" + httpMethod.getPath() + "'", t);
         } 
     }
     
     /**
      * Adds all the provided request parameters to a URL in GET request format. 
      * 
      * @param urlPath
      *        the target URL
      * @param requestParameters
      *        the parameters to add to the URL as GET request params.
      * @return
      * the target URL including the parameters.
      * @throws S3ServiceException
      */
     protected String addRequestParametersToUrlPath(String urlPath, Map requestParameters) 
         throws S3ServiceException 
     {
         if (requestParameters != null) {
             Iterator reqPropIter = requestParameters.entrySet().iterator();
             while (reqPropIter.hasNext()) {
                 Map.Entry entry = (Map.Entry) reqPropIter.next();
                 Object key = entry.getKey();
                 Object value = entry.getValue();
                 
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
     
     /**
      * Adds the provided request headers to the connection.
      * 
      * @param httpMethod
      *        the connection object 
      * @param requestHeaders
      *        the request headers to add as name/value pairs.
      */
     protected void addRequestHeadersToConnection(
             HttpMethodBase httpMethod, Map requestHeaders) 
     {
         if (requestHeaders != null) {
             Iterator reqHeaderIter = requestHeaders.entrySet().iterator();
             while (reqHeaderIter.hasNext()) {
                 Map.Entry entry = (Map.Entry) reqHeaderIter.next();
                 String key = entry.getKey().toString();
                 String value = entry.getValue().toString();
                 
                 httpMethod.setRequestHeader(key, value);
                 log.debug("Added request header to connection: " + key + "=" + value);
             }
         }                        
     }
     
     /**
      * Converts an array of Header objects to a map of name/value pairs.
      * 
      * @param headers
      * @return
      */
     private Map convertHeadersToMap(Header[] headers) {
         HashMap map = new HashMap();
         for (int i = 0; headers != null && i < headers.length; i++) {
             map.put(headers[i].getName(), headers[i].getValue());
         }
         return map;
     }  
         
     /**
      * Adds all appropriate metadata to the given HTTP method.
      * 
      * @param httpMethod
      * @param metadata
      */
     private void addMetadataToHeaders(HttpMethodBase httpMethod, Map metadata) {
         Iterator metaDataIter = metadata.entrySet().iterator();
         while (metaDataIter.hasNext()) {
             Map.Entry entry = (Map.Entry) metaDataIter.next();
             String key = (String) entry.getKey();
             Object value = entry.getValue();
 
             if (key == null || !(value instanceof String)) {
                 // Ignore invalid metadata.
                 continue;
             }
             
             httpMethod.setRequestHeader(key, (String) value);
         }
     }
     
     /**
      * Performs an HTTP HEAD request using the {@link #performRequest} method.
      *  
      * @param bucketName
      *        the bucket's name
 	 * @param objectKey
      *        the object's key name, may be null if the operation is on a bucket only.
      * @param requestParameters
      *        parameters to add to the request URL as GET params
      * @param requestHeaders
      *        headers to add to the request
      * @return
      *        the HTTP method object used to perform the request
      * @throws S3ServiceException
      */
     protected HttpMethodBase performRestHead(String bucketName, String objectKey, 
         Map requestParameters, Map requestHeaders) throws S3ServiceException 
     {
         HttpMethodBase httpMethod = setupConnection("HEAD", bucketName, objectKey, requestParameters);
         
         // Add all request headers.
         addRequestHeadersToConnection(httpMethod, requestHeaders);
         
         performRequest(httpMethod, 200);
         
         return httpMethod;
     }
 
     /**
      * Performs an HTTP GET request using the {@link #performRequest} method.
      *  
      * @param bucketName
      *        the bucket's name
 	 * @param objectKey
      *        the object's key name, may be null if the operation is on a bucket only.
      * @param requestParameters
      *        parameters to add to the request URL as GET params
      * @param requestHeaders
      *        headers to add to the request
      * @return
      *        The HTTP method object used to perform the request.
      * 
      * @throws S3ServiceException
      */
     protected HttpMethodBase performRestGet(String bucketName, String objectKey, 
         Map requestParameters, Map requestHeaders) throws S3ServiceException 
     {
         HttpMethodBase httpMethod = setupConnection("GET", bucketName, objectKey, requestParameters);
         
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
         
     /**
      * Performs an HTTP PUT request using the {@link #performRequest} method.
      *  
      * @param bucketName
      *        the name of the bucket the object will be stored in.
      * @param objectKey
      *        the key (name) of the object to be stored.        
      * @param metadata
      *        map of name/value pairs to add as metadata to any S3 objects created.  
      * @param requestParameters
      *        parameters to add to the request URL as GET params
      * @param requestEntity
      *        an HttpClient object that encapsulates the object and data contents that will be
      *        uploaded. This object supports the resending of object data, when possible.
      * @return
      *        a package including the HTTP method object used to perform the request, and the 
      *        content length (in bytes) of the object that was PUT to S3.
      * 
      * @throws S3ServiceException
      */
     protected HttpMethodAndByteCount performRestPut(String bucketName, String objectKey, 
         Map metadata, Map requestParameters, RequestEntity requestEntity) throws S3ServiceException 
     {        
         // Add any request parameters.
         HttpMethodBase httpMethod = setupConnection("PUT", bucketName, objectKey, requestParameters);
         
         Map renamedMetadata = RestUtils.renameMetadataKeys(metadata);
         addMetadataToHeaders(httpMethod, renamedMetadata);
 
         long contentLength = 0;
         
         if (requestEntity != null) {
             ((PutMethod)httpMethod).setRequestEntity(requestEntity);
         } else {
             // Need an explicit Content-Length even if no data is being uploaded.
             httpMethod.setRequestHeader("Content-Length", "0");
         }
         
         performRequest(httpMethod, 200);
         
         if (requestEntity != null) {
             // Respond with the actual guaranteed content length of the uploaded data.
             contentLength = ((PutMethod)httpMethod).getRequestEntity().getContentLength();
         }
                 
         // Release connection after PUT (there ought to be no response content)
         httpMethod.releaseConnection();
         
         return new HttpMethodAndByteCount(httpMethod, contentLength);
     }
 
     /**
      * Performs an HTTP DELETE request using the {@link #performRequest} method.
      *  
      * @param bucketName
      * the bucket's name
 	 * @param objectKey
      * the object's key name, may be null if the operation is on a bucket only.
      * @return
      * The HTTP method object used to perform the request.
      * 
      * @throws S3ServiceException
      */
     protected HttpMethodBase performRestDelete(String bucketName, String objectKey) throws S3ServiceException {        
         HttpMethodBase httpMethod = setupConnection("DELETE", bucketName, objectKey, null);
 
         performRequest(httpMethod, 204);
 
         // Release connection after DELETE (there's no response content)
         log.debug("Releasing HttpMethod after delete");
         httpMethod.releaseConnection();
 
         return httpMethod;
     }
         
     /**
      * Creates an {@link HttpMethod} object to handle a particular connection method.
      * 
      * @param method
      *        the HTTP method/connection-type to use, must be one of: PUT, HEAD, GET, DELETE
      * @param bucketName
      *        the bucket's name
 	 * @param objectKey
      *        the object's key name, may be null if the operation is on a bucket only.
      * @return
      *        the HTTP method object used to perform the request
      *        
      * @throws S3ServiceException
      */
     protected HttpMethodBase setupConnection(String method, String bucketName, String objectKey, Map requestParameters) throws S3ServiceException 
     {
         if (bucketName == null) {
             throw new S3ServiceException("Cannot connect to S3 Service with a null path");
         }
 
         String hostname = generateS3HostnameForBucket(bucketName);
 
 		// Determine the resource string (ie the item's path in S3, including the bucket name)
         String resourceString = "/";
         if (hostname.equals(Constants.S3_HOSTNAME) && bucketName != null && bucketName.length() > 0) {
             resourceString += bucketName + "/";
         }
         resourceString += (objectKey != null? RestUtils.encodeUrlString(objectKey) : "");
 
 		// Construct a URL representing a connection for the S3 resource.
         String url = null;
         if (isHttpsOnly()) {
             url = PROTOCOL_SECURE + "://" + hostname + ":" + PORT_SECURE + resourceString;
         } else {
             url = PROTOCOL_INSECURE + "://" + hostname + ":" + PORT_INSECURE + resourceString;        
         }
         log.debug("S3 URL: " + url);
         
         // Add additional request parameters to the URL for special cases (eg ACL operations)
         url = addRequestParametersToUrlPath(url, requestParameters);
 
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
             httpMethod.setRequestHeader("Date", ServiceUtils.formatRfc822Date(
                 getCurrentTimeWithOffset()));
         }
         if (httpMethod.getRequestHeader("Content-Type") == null) {
             httpMethod.setRequestHeader("Content-Type", "");
         }        
                                 
         return httpMethod;
     }
     
     /**
      * Authorizes an HTTP request by signing it. The signature is based on the target URL, and the
      * signed authorization string is added to the {@link HttpMethod} object as an Authorization header.
      * 
      * @param httpMethod
      *        the request object
      * @throws S3ServiceException
      */
     protected void buildAuthorizationString(HttpMethod httpMethod) throws S3ServiceException {
         if (isAuthenticatedConnection()) {
             log.debug("Adding authorization for AWS Access Key '" + getAWSCredentials().getAccessKey() + "'.");
         } else {
             log.debug("Service has no AWS Credential and is un-authenticated, skipping authorization");
             return;
         }
         
         String hostname = null;
         try {
             hostname = httpMethod.getURI().getHost();
         } catch (URIException e) {
             log.error("Unable to determine hostname target for request", e);
         }
 
         /*
          * Determine the complete URL for the S3 resource, including any S3-specific parameters.
          */         
         String fullUrl = httpMethod.getPath();
 
         // If we are using an alternative hostname, include the hostname/bucketname in the resource path.
         if (!Constants.S3_HOSTNAME.equals(hostname)) {
             int subdomainOffset = hostname.indexOf("." + Constants.S3_HOSTNAME);
             if (subdomainOffset > 0) {
                 // Hostname represents an S3 sub-domain, so the bucket's name is the CNAME portion
                 fullUrl = "/" + hostname.substring(0, subdomainOffset) + httpMethod.getPath();                    
             } else {
                 // Hostname represents a virtual host, so the bucket's name is identical to hostname
                 fullUrl = "/" + hostname + httpMethod.getPath();                    
             }
         }
         
         String queryString = httpMethod.getQueryString();
         if (queryString != null && queryString.length() > 0) {
             fullUrl += "?" + queryString;
         }
         
         // Set/update the date timestamp to the current time 
         // Note that this will be over-ridden if an "x-amz-date" header is present.
         httpMethod.setRequestHeader("Date", ServiceUtils.formatRfc822Date(
             getCurrentTimeWithOffset()));
         
         // Generate a canonical string representing the operation.
         String canonicalString = RestUtils.makeCanonicalString(
                 httpMethod.getName(), fullUrl,
                 convertHeadersToMap(httpMethod.getRequestHeaders()), null);
         log.debug("Canonical string ('|' is a newline): " + canonicalString.replace('\n', '|'));
         
         // Sign the canonical string.
         String signedCanonical = ServiceUtils.signWithHmacSha1(
                 getAWSCredentials().getSecretKey(), canonicalString);
         
         // Add encoded authorization to connection as HTTP Authorization header. 
         String authorizationString = "AWS " + getAWSCredentials().getAccessKey() + ":" + signedCanonical;
         httpMethod.setRequestHeader("Authorization", authorizationString);
     }
 
     
     ////////////////////////////////////////////////////////////////
     // Methods below this point implement S3Service abstract methods
     ////////////////////////////////////////////////////////////////    
     
     public boolean isBucketAccessible(String bucketName) throws S3ServiceException {
         log.debug("Checking existence of bucket: " + bucketName);
         
         HttpMethodBase httpMethod = null;
         
         // This request may return an XML document that we're not interested in. Clean this up.
         try {
             // Ensure bucket exists and is accessible by performing a HEAD request
             httpMethod = performRestHead(bucketName, null, null, null);
 
             if (httpMethod.getResponseBodyAsStream() != null) {
                 httpMethod.getResponseBodyAsStream().close();
             }
         } catch (S3ServiceException e) {
             log.debug("Bucket does not exist: " + bucketName, e);
             return false;
         } catch (IOException e) {
             log.warn("Unable to close response body input stream", e);
         } finally {
             log.debug("Releasing un-wanted bucket HEAD response");
             if (httpMethod != null) {
                 httpMethod.releaseConnection();
             }
         }
         
         // If we get this far, the bucket exists.
         return true;
     }    
 
     protected S3Bucket[] listAllBucketsImpl() throws S3ServiceException {
         log.debug("Listing all buckets for AWS user: " + getAWSCredentials().getAccessKey());
         
         String bucketName = ""; // Root path of S3 service lists the user's buckets.
         HttpMethodBase httpMethod =  performRestGet(bucketName, null, null, null);
         String contentType = httpMethod.getResponseHeader("Content-Type").getValue();
             
         if (!Mimetypes.MIMETYPE_XML.equals(contentType)) {
             throw new S3ServiceException("Expected XML document response from S3 but received content type " + 
                 contentType);
         }
 
         S3Bucket[] buckets = (new XmlResponsesSaxParser()).parseListMyBucketsResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getBuckets();
         return buckets;
     }
 
     protected S3Object[] listObjectsImpl(String bucketName, String prefix, String delimiter, 
         long maxListingLength) throws S3ServiceException 
     {        
         return listObjectsInternal(bucketName, prefix, delimiter, maxListingLength, true, null)
             .getObjects();
     }
     
     protected S3ObjectsChunk listObjectsChunkedImpl(String bucketName, String prefix, String delimiter, 
         long maxListingLength, String priorLastKey) throws S3ServiceException 
     {        
         return listObjectsInternal(bucketName, prefix, delimiter, maxListingLength, false, priorLastKey);
     }
 
     protected S3ObjectsChunk listObjectsInternal(String bucketName, String prefix, String delimiter, 
         long maxListingLength, boolean automaticallyMergeChunks, String priorLastKey) throws S3ServiceException 
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
         ArrayList commonPrefixes = new ArrayList();
         
         boolean incompleteListing = true;
         int ioErrorRetryCount = 0;
             
         while (incompleteListing) {
             if (priorLastKey != null) {
                 parameters.put("marker", priorLastKey);
             } else {
                 parameters.remove("marker");
             }
             
             HttpMethodBase httpMethod = performRestGet(bucketName, null, parameters, null);
             ListBucketHandler listBucketHandler = null;
             
             try {
                 listBucketHandler = (new XmlResponsesSaxParser())
                     .parseListBucketObjectsResponse(
                         new HttpMethodReleaseInputStream(httpMethod));
                 ioErrorRetryCount = 0;
             } catch (S3ServiceException e) {
                 if (e.getCause() instanceof IOException && ioErrorRetryCount < 5) {
                     ioErrorRetryCount++;
                     log.warn("Retrying bucket listing failure due to IO error", e);                    
                     continue;
                 } else {
                     throw e;
                 }
             }
             
             S3Object[] partialObjects = listBucketHandler.getObjects();
             log.debug("Found " + partialObjects.length + " objects in one batch");
             objects.addAll(Arrays.asList(partialObjects));
             
             String[] partialCommonPrefixes = listBucketHandler.getCommonPrefixes();
             log.debug("Found " + partialCommonPrefixes.length + " common prefixes in one batch");
             commonPrefixes.addAll(Arrays.asList(partialCommonPrefixes));
             
             incompleteListing = listBucketHandler.isListingTruncated();            
             if (incompleteListing) {
                 priorLastKey = listBucketHandler.getMarkerForNextListing();                
                 log.debug("Yet to receive complete listing of bucket contents, "
                         + "last key for prior chunk: " + priorLastKey);
             } else {
                 priorLastKey = null;
             }
             
             if (!automaticallyMergeChunks)
                 break;
         }
         if (automaticallyMergeChunks) {
             log.debug("Found " + objects.size() + " objects in total");
             return new S3ObjectsChunk(
                 (S3Object[]) objects.toArray(new S3Object[objects.size()]),
                 (String[]) commonPrefixes.toArray(new String[commonPrefixes.size()]),
                 null);
         } else {
             return new S3ObjectsChunk(
                 (S3Object[]) objects.toArray(new S3Object[objects.size()]), 
                 (String[]) commonPrefixes.toArray(new String[commonPrefixes.size()]),
                 priorLastKey);            
         }
     }
     
     protected void deleteObjectImpl(String bucketName, String objectKey) throws S3ServiceException {
         performRestDelete(bucketName, objectKey);
     }    
 
     protected AccessControlList getObjectAclImpl(String bucketName, String objectKey) throws S3ServiceException {
         log.debug("Retrieving Access Control List for bucketName=" + bucketName + ", objectKkey=" + objectKey);
         
         HashMap requestParameters = new HashMap();
         requestParameters.put("acl","");
 
         HttpMethodBase httpMethod = performRestGet(bucketName, objectKey, requestParameters, null);
         return (new XmlResponsesSaxParser()).parseAccessControlListResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getAccessControlList();
     }
     
     protected AccessControlList getBucketAclImpl(String bucketName) throws S3ServiceException {
         log.debug("Retrieving Access Control List for Bucket: " + bucketName);
         
         HashMap requestParameters = new HashMap();
         requestParameters.put("acl","");
 
         HttpMethodBase httpMethod = performRestGet(bucketName, null, requestParameters, null);
         return (new XmlResponsesSaxParser()).parseAccessControlListResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getAccessControlList();
     }
 
     protected void putObjectAclImpl(String bucketName, String objectKey, AccessControlList acl) 
         throws S3ServiceException 
     {        
         putAclImpl(bucketName, objectKey, acl);
     }
     
     protected void putBucketAclImpl(String bucketName, AccessControlList acl) 
         throws S3ServiceException 
     {        
         String fullKey = bucketName;
         putAclImpl(fullKey, null, acl);
     }
 
     protected void putAclImpl(String bucketName, String objectKey, AccessControlList acl) throws S3ServiceException 
     {
         log.debug("Setting Access Control List for bucketName=" + bucketName + ", objectKey=" + objectKey);
 
         HashMap requestParameters = new HashMap();
         requestParameters.put("acl","");
         
         HashMap metadata = new HashMap();
         metadata.put("Content-Type", "text/plain");
 
         try {
             String aclAsXml = acl.toXml();
             metadata.put("Content-Length", String.valueOf(aclAsXml.length()));
             performRestPut(bucketName, objectKey, metadata, requestParameters, 
                 new StringRequestEntity(aclAsXml, "text/plain", Constants.DEFAULT_ENCODING));
         } catch (UnsupportedEncodingException e) {
             throw new S3ServiceException("Unable to encode ACL XML document", e);
         }
     }
     
     protected S3Bucket createBucketImpl(String bucketName, String location, AccessControlList acl) 
         throws S3ServiceException 
     {        
         log.debug("Creating bucket with name: " + bucketName);
         
         HashMap metadata = new HashMap();
         RequestEntity requestEntity = null;
         
        if (location != null) {
             metadata.put("Content-Type", "text/xml");
             try {
                 CreateBucketConfiguration config = new CreateBucketConfiguration(location);
                 metadata.put("Content-Length", String.valueOf(config.toXml().length()));
                 requestEntity = new StringRequestEntity(config.toXml(), "text/xml", Constants.DEFAULT_ENCODING);                
             } catch (UnsupportedEncodingException e) {
                 throw new S3ServiceException("Unable to encode CreateBucketConfiguration XML document", e);
             }    
         }
         
         Map map = createObjectImpl(bucketName, null, null, requestEntity, metadata, acl);
         
         S3Bucket bucket = new S3Bucket(bucketName, location);
         bucket.setAcl(acl);
         bucket.replaceAllMetadata(map);
         return bucket;
     }
     
     protected void deleteBucketImpl(String bucketName) throws S3ServiceException {
         performRestDelete(bucketName, null);
     }    
     
     /**
      * Beware of high memory requirements when creating large S3 objects when the Content-Length
      * is not set in the object.
      */
     protected S3Object putObjectImpl(String bucketName, S3Object object) throws S3ServiceException 
     {        
         log.debug("Creating Object with key " + object.getKey() + " in bucket " + bucketName);
 
         RequestEntity requestEntity = null;
         if (object.getDataInputStream() != null) {
             if (object.containsMetadata("Content-Length")) {
                 log.debug("Uploading object data with Content-Length: " + object.getContentLength());
                 requestEntity = new RepeatableRequestEntity(object.getKey(),                     
                     object.getDataInputStream(), object.getContentType(), object.getContentLength());
             } else {
                 // Use InputStreamRequestEntity for objects with an unknown content length, as the
                 // entity will cache the results and doesn't need to know the data length in advance.
                 log.warn("Content-Length of data stream not set, will automatically determine data length in memory");
                 requestEntity = new InputStreamRequestEntity(
                     object.getDataInputStream(), InputStreamRequestEntity.CONTENT_LENGTH_AUTO);
             }
         }
         
         Map map = createObjectImpl(bucketName, object.getKey(), object.getContentType(), 
             requestEntity, object.getMetadataMap(), object.getAcl());
         
         try {
             object.closeDataInputStream();
         } catch (IOException e) {
             log.warn("Unable to close data input stream for object '" + object.getKey() + "'", e);
         }
 
         object.replaceAllMetadata(map);
         return object;
     }
     
     protected Map createObjectImpl(String bucketName, String objectKey, String contentType, 
         RequestEntity requestEntity, Map metadata, AccessControlList acl) 
         throws S3ServiceException 
     {
         if (metadata == null) {
             metadata = new HashMap();
         } else {
             // Use a new map object in case the one we were provided is immutable.
             metadata = new HashMap(metadata);
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
                         
         log.debug("Creating object bucketName=" + bucketName + ", objectKey=" + objectKey + "." + 
             " Content-Type=" + metadata.get("Content-Type") +
             " Including data? " + (requestEntity != null) +
             " Metadata: " + metadata +
             " ACL: " + acl
             );
         
         HttpMethodAndByteCount methodAndByteCount = performRestPut(
             bucketName, objectKey, metadata, null, requestEntity);
             
         // Consume response content.
         HttpMethodBase httpMethod = methodAndByteCount.getHttpMethod();
             
         Map map = new HashMap();
         map.putAll(metadata); // Keep existing metadata.
         map.putAll(convertHeadersToMap(httpMethod.getResponseHeaders()));
         map.put(S3Object.METADATA_HEADER_CONTENT_LENGTH, String.valueOf(methodAndByteCount.getByteCount()));
         map = ServiceUtils.cleanRestMetadataMap(map);
 
         if (putNonStandardAcl) {
             log.debug("Creating object with a non-canned ACL using REST, so an extra ACL Put is required");
             putAclImpl(bucketName, objectKey, acl);
         }
         
         return map;
     }
     
     protected S3Object getObjectDetailsImpl(String bucketName, String objectKey, Calendar ifModifiedSince, 
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags) 
         throws S3ServiceException 
     { 
         return getObjectImpl(true, bucketName, objectKey, 
             ifModifiedSince, ifUnmodifiedSince, ifMatchTags, ifNoneMatchTags, null, null);
     }
     
     protected S3Object getObjectImpl(String bucketName, String objectKey, Calendar ifModifiedSince, 
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
         
         HttpMethodBase httpMethod = null;        
         if (headOnly) {
             httpMethod = performRestHead(bucketName, objectKey, null, requestHeaders);    
         } else {
             httpMethod = performRestGet(bucketName, objectKey, null, requestHeaders);
         }
         
         HashMap map = new HashMap();
         map.putAll(convertHeadersToMap(httpMethod.getResponseHeaders()));
 
         S3Object responseObject = new S3Object(objectKey);
         responseObject.setBucketName(bucketName);
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
     
     protected String getBucketLocationImpl(String bucketName) 
         throws S3ServiceException
     {
         log.debug("Retrieving location of Bucket: " + bucketName);
         
         HashMap requestParameters = new HashMap();
         requestParameters.put("location","");
     
         HttpMethodBase httpMethod = performRestGet(bucketName, null, requestParameters, null);
         return (new XmlResponsesSaxParser()).parseBucketLocationResponse(
             new HttpMethodReleaseInputStream(httpMethod));        
     }
 
     protected S3BucketLoggingStatus getBucketLoggingStatusImpl(String bucketName) 
         throws S3ServiceException
     {
         log.debug("Retrieving Logging Status for Bucket: " + bucketName);
         
         HashMap requestParameters = new HashMap();
         requestParameters.put("logging","");
 
         HttpMethodBase httpMethod = performRestGet(bucketName, null, requestParameters, null);
         return (new XmlResponsesSaxParser()).parseLoggingStatusResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getBucketLoggingStatus();        
     }
 
     protected void setBucketLoggingStatusImpl(String bucketName, S3BucketLoggingStatus status) 
         throws S3ServiceException
     {
         log.debug("Setting Logging Status for bucket: " + bucketName);
 
         HashMap requestParameters = new HashMap();
         requestParameters.put("logging","");
         
         HashMap metadata = new HashMap();
         metadata.put("Content-Type", "text/plain");
 
         try {
             String statusAsXml = status.toXml();
             metadata.put("Content-Length", String.valueOf(statusAsXml.length()));
             performRestPut(bucketName, null, metadata, requestParameters, 
                 new StringRequestEntity(statusAsXml, "text/plain", Constants.DEFAULT_ENCODING));                
         } catch (UnsupportedEncodingException e) {
             throw new S3ServiceException("Unable to encode LoggingStatus XML document", e);
         }    
     }
 
     /**
      * Puts an object using a pre-signed PUT URL generated for that object.
      * This method is an implementation of the interface {@link SignedUrlHandler}. 
      * <p>
      * This operation does not required any S3 functionality as it merely 
      * uploads the object by performing a standard HTTP PUT using the signed URL.
      * 
      * @param signedPutUrl
      * a signed PUT URL generated with 
      * {@link S3Service#createSignedPutUrl(String, String, Map, AWSCredentials, Date)}.
      * @param object
      * the object to upload, which must correspond to the object for which the URL was signed.
      * The object <b>must</b> have the correct content length set, and to apply a non-standard
      * ACL policy only the REST canned ACLs can be used
      * (eg {@link AccessControlList#REST_CANNED_PUBLIC_READ_WRITE}). 
      * 
      * @return
      * the S3Object put to S3. The S3Object returned will represent the object created in S3.
      * 
      * @throws S3ServiceException
      */
     public S3Object putObjectWithSignedUrl(String signedPutUrl, S3Object object) throws S3ServiceException {
         PutMethod putMethod = new PutMethod(signedPutUrl);
         
         Map renamedMetadata = RestUtils.renameMetadataKeys(object.getMetadataMap());
         addMetadataToHeaders(putMethod, renamedMetadata);                
 
         if (!object.containsMetadata("Content-Length")) {
             throw new IllegalStateException("Content-Length must be specified for objects put using signed PUT URLs");
         }
         
         if (object.getDataInputStream() != null) {
             putMethod.setRequestEntity(new RepeatableRequestEntity(object.getKey(),
                 object.getDataInputStream(), object.getContentType(), object.getContentLength()));
         }
 
         performRequest(putMethod, 200);
 
         // Consume response data and release connection.
         putMethod.releaseConnection();
 
         try {
             S3Object uploadedObject = ServiceUtils.buildObjectFromUrl(putMethod.getURI().getHost(), putMethod.getPath());
             object.setBucketName(uploadedObject.getBucketName());
             object.setKey(uploadedObject.getKey());
             try {
 				object.setLastModifiedDate(ServiceUtils.parseRfc822Date(
 					putMethod.getResponseHeader("Date").getValue()));
 			} catch (ParseException e1) {
 				log.warn("Unable to interpret date of object PUT in S3", e1);
 			}
                        
             try {
                 object.closeDataInputStream();
             } catch (IOException e) {
                 log.warn("Unable to close data input stream for object '" + object.getKey() + "'", e);
             }
         } catch (URIException e) {
             throw new S3ServiceException("Unable to lookup URI for object created with signed PUT", e); 
         } catch (UnsupportedEncodingException e) {
             throw new S3ServiceException("Unable to determine name of object created with signed PUT", e); 
         }        
         return object;
     }
 
     /**
      * Deletes an object using a pre-signed DELETE URL generated for that object.
      * This method is an implementation of the interface {@link SignedUrlHandler}. 
      * <p>
      * This operation does not required any S3 functionality as it merely 
      * deletes the object by performing a standard HTTP DELETE using the signed URL.
      * 
      * @param signedDeleteUrl
      * a signed DELETE URL generated with {@link S3Service#createSignedDeleteUrl}.
      * 
      * @throws S3ServiceException
      */
     public void deleteObjectWithSignedUrl(String signedDeleteUrl) throws S3ServiceException {
         DeleteMethod deleteMethod = new DeleteMethod(signedDeleteUrl);
         
         performRequest(deleteMethod, 204);
 
         deleteMethod.releaseConnection();
     }
 
     /**
      * Gets an object using a pre-signed GET URL generated for that object.
      * This method is an implementation of the interface {@link SignedUrlHandler}. 
      * <p>
      * This operation does not required any S3 functionality as it merely 
      * uploads the object by performing a standard HTTP GET using the signed URL.
      * 
      * @param signedGetUrl
      * a signed GET URL generated with 
      * {@link S3Service#createSignedGetUrl(String, String, AWSCredentials, Date)}.
      * 
      * @return
      * the S3Object in S3 including all metadata and the object's data input stream.
      * 
      * @throws S3ServiceException
      */
     public S3Object getObjectWithSignedUrl(String signedGetUrl) throws S3ServiceException {
         return getObjectWithSignedUrlImpl(signedGetUrl, false);
     }
     
     /**
      * Gets an object's details using a pre-signed HEAD URL generated for that object.
      * This method is an implementation of the interface {@link SignedUrlHandler}. 
      * <p>
      * This operation does not required any S3 functionality as it merely 
      * uploads the object by performing a standard HTTP HEAD using the signed URL.
      * 
      * @param signedHeadUrl
      * a signed HEAD URL generated with 
      * {@link S3Service#createSignedHeadUrl(String, String, AWSCredentials, Date)}.
      * 
      * @return
      * the S3Object in S3 including all metadata, but without the object's data input stream.
      * 
      * @throws S3ServiceException
      */
     public S3Object getObjectDetailsWithSignedUrl(String signedHeadUrl) throws S3ServiceException {
         return getObjectWithSignedUrlImpl(signedHeadUrl, true);
     }
     
     /**
      * Gets an object's ACL details using a pre-signed GET URL generated for that object.
      * This method is an implementation of the interface {@link SignedUrlHandler}. 
      * 
      * @param signedAclUrl
      * a signed URL generated with {@link S3Service#createSignedUrl(String, String, String, String, Map, AWSCredentials, long, boolean)}.
      * 
      * @return
      * the AccessControlList settings of the object in S3.
      * 
      * @throws S3ServiceException
      */
     public AccessControlList getObjectAclWithSignedUrl(String signedAclUrl) 
 	    throws S3ServiceException 
 	{
 	    HttpMethodBase httpMethod = new GetMethod(signedAclUrl);        
         
         HashMap requestParameters = new HashMap();
         requestParameters.put("acl","");
 
 	    performRequest(httpMethod, 200);
         return (new XmlResponsesSaxParser()).parseAccessControlListResponse(
             new HttpMethodReleaseInputStream(httpMethod)).getAccessControlList();
 	}    
     
     /**
      * Sets an object's ACL details using a pre-signed PUT URL generated for that object.
      * This method is an implementation of the interface {@link SignedUrlHandler}. 
      * 
      * @param signedAclUrl
      * a signed URL generated with {@link S3Service#createSignedUrl(String, String, String, String, Map, AWSCredentials, long, boolean)}.
      * @param acl
      * the ACL settings to apply to the object represented by the signed URL.
      * 
      * @throws S3ServiceException
      */
     public void putObjectAclWithSignedUrl(String signedAclUrl, AccessControlList acl) throws S3ServiceException {
         PutMethod putMethod = new PutMethod(signedAclUrl);
         
         if (acl != null) {
             if (AccessControlList.REST_CANNED_PRIVATE.equals(acl)) {
                 putMethod.addRequestHeader(Constants.REST_HEADER_PREFIX + "acl", "private");
             } else if (AccessControlList.REST_CANNED_PUBLIC_READ.equals(acl)) { 
                 putMethod.addRequestHeader(Constants.REST_HEADER_PREFIX + "acl", "public-read");
             } else if (AccessControlList.REST_CANNED_PUBLIC_READ_WRITE.equals(acl)) { 
                 putMethod.addRequestHeader(Constants.REST_HEADER_PREFIX + "acl", "public-read-write");
             } else if (AccessControlList.REST_CANNED_AUTHENTICATED_READ.equals(acl)) {
                 putMethod.addRequestHeader(Constants.REST_HEADER_PREFIX + "acl", "authenticated-read");
             } else {
                 try {
                     String aclAsXml = acl.toXml();
                     putMethod.setRequestEntity(new StringRequestEntity(
                         aclAsXml, "text/xml", Constants.DEFAULT_ENCODING));
                 } catch (UnsupportedEncodingException e) {
                     throw new S3ServiceException("Unable to encode ACL XML document", e);
                 }    
 
             }
         }
         
         performRequest(putMethod, 200);
 
         // Consume response data and release connection.
         putMethod.releaseConnection();
     }
     
     private S3Object getObjectWithSignedUrlImpl(String signedGetOrHeadUrl, boolean headOnly) 
         throws S3ServiceException 
     {
         HttpMethodBase httpMethod = null;        
         if (headOnly) {
             httpMethod = new HeadMethod(signedGetOrHeadUrl);    
         } else {
             httpMethod = new GetMethod(signedGetOrHeadUrl);
         }
         
         performRequest(httpMethod, 200);
 
         HashMap map = new HashMap();
         map.putAll(convertHeadersToMap(httpMethod.getResponseHeaders()));
         
         S3Object responseObject = null;
         try {
             responseObject = ServiceUtils.buildObjectFromUrl(
                 httpMethod.getURI().getHost(),
                 httpMethod.getPath().substring(1));
         } catch (URIException e) {
             throw new S3ServiceException("Unable to lookup URI for object created with signed PUT", e); 
         } catch (UnsupportedEncodingException e) {
             throw new S3ServiceException("Unable to determine name of object created with signed PUT", e); 
         }
         
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
     
     /**
      * Simple container object to store an HttpMethod object representing a request connection, and a 
      * count of the byte size of the S3 object associated with the request.
      * <p>
      * This object is used when S3 objects are created to associate the connection and the actual size
      * of the object as reported back by S3.
      * 
      * @author James Murty
      */
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
