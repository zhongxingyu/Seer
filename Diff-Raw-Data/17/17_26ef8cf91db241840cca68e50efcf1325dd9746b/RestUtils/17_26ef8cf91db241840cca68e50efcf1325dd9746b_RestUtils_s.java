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
 package org.jets3t.service.utils;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpMethodRetryHandler;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.NTCredentials;
 import org.apache.commons.httpclient.ProxyHost;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.auth.CredentialsProvider;
 import org.apache.commons.httpclient.contrib.proxy.PluginProxyUtil;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.impl.rest.httpclient.AWSRequestAuthorizer;
 import org.jets3t.service.impl.rest.httpclient.HttpClientAndConnectionManager;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.io.UnrecoverableIOException;
 
 /**
  * Utilities useful for REST/HTTP S3Service implementations.
  * 
  * @author James Murty
  */
 public class RestUtils {
 
     private static final Log log = LogFactory.getLog(RestUtils.class);
 
     /**
      * A list of HTTP-specific header names, that may be present in S3Objects as metadata but
      * which should be treated as plain HTTP headers during transmission (ie not converted into
      * S3 Object metadata items). All items in this list are in lower case.
      * <p>
      * This list includes the items:
      * <table>
      * <tr><th>Unchanged metadata names</th></tr>
      * <tr><td>content-type</td></tr>
      * <tr><td>content-md5</td></tr>
      * <tr><td>content-length</td></tr>
      * <tr><td>content-language</td></tr>
      * <tr><td>expires</td></tr>
      * <tr><td>cache-control</td></tr>
      * <tr><td>content-disposition</td></tr>
      * <tr><td>content-encoding</td></tr>
      * </table>
      */
     public static final List HTTP_HEADER_METADATA_NAMES = Arrays.asList(new String[] {
         "content-type",
         "content-md5",
         "content-length",
         "content-language",
         "expires",
         "cache-control",
         "content-disposition",
         "content-encoding"
         }); 
 
 
     /**
      * Encodes a URL string, and ensures that spaces are encoded as "%20" instead of "+" to keep
      * fussy web browsers happier.
      * 
      * @param path
      * @return
      * encoded URL.
      * @throws S3ServiceException
      */
     public static String encodeUrlString(String path) throws S3ServiceException {
         try {
             String encodedPath = URLEncoder.encode(path, Constants.DEFAULT_ENCODING);
             // Web browsers do not always handle '+' characters well, use the well-supported '%20' instead.
             encodedPath = encodedPath.replaceAll("\\+", "%20");            
             return encodedPath;
         } catch (UnsupportedEncodingException uee) {
             throw new S3ServiceException("Unable to encode path: " + path, uee);
         }
     }
 
     /**
      * Encodes a URL string but leaves a delimiter string unencoded.
      * Spaces are encoded as "%20" instead of "+".
      * 
      * @param path
      * @param delimiter
      * @return
      * encoded URL string.
      * @throws S3ServiceException
      */
     public static String encodeUrlPath(String path, String delimiter) throws S3ServiceException {
         StringBuffer result = new StringBuffer();
         String tokens[] = path.split(delimiter);
         for (int i = 0; i < tokens.length; i++) {
             result.append(encodeUrlString(tokens[i]));
             if (i < tokens.length - 1) {
                 result.append(delimiter);
             }
         }
         return result.toString();
     }
     
     /**
      * Calculate the canonical string for a REST/HTTP request to S3.  
      * 
      * When expires is non-null, it will be used instead of the Date header.
      */
     public static String makeS3CanonicalString(String method, String resource, Map headersMap, String expires)
     {
         StringBuffer buf = new StringBuffer();
         buf.append(method + "\n");
 
         // Add all interesting headers to a list, then sort them.  "Interesting"
         // is defined as Content-MD5, Content-Type, Date, and x-amz-
         SortedMap interestingHeaders = new TreeMap();
         if (headersMap != null && headersMap.size() > 0) {
             Iterator headerIter = headersMap.entrySet().iterator();
             while (headerIter.hasNext()) {
                 Map.Entry entry = (Map.Entry) headerIter.next();
                 Object key = entry.getKey();
                 Object value = entry.getValue();
                 
                 if (key == null) continue;                
                 String lk = key.toString().toLowerCase(Locale.getDefault());
 
                 // Ignore any headers that are not particularly interesting.
                 if (lk.equals("content-type") || lk.equals("content-md5") || lk.equals("date") ||
                     lk.startsWith(Constants.REST_HEADER_PREFIX))
                 {                        
                     interestingHeaders.put(lk, value);
                 }
             }
         }
 
         // Remove default date timestamp if "x-amz-date" is set. 
         if (interestingHeaders.containsKey(Constants.REST_METADATA_ALTERNATE_DATE)) {
             interestingHeaders.put("date", "");
         }
 
         // Use the expires value as the timestamp if it is available. This trumps both the default
         // "date" timestamp, and the "x-amz-date" header.
         if (expires != null) {
             interestingHeaders.put("date", expires);
         }
 
         // these headers require that we still put a new line in after them,
         // even if they don't exist.
         if (! interestingHeaders.containsKey("content-type")) {
             interestingHeaders.put("content-type", "");
         }
         if (! interestingHeaders.containsKey("content-md5")) {
             interestingHeaders.put("content-md5", "");
         }
 
         // Finally, add all the interesting headers (i.e.: all that startwith x-amz- ;-))
         for (Iterator i = interestingHeaders.entrySet().iterator(); i.hasNext(); ) {
             Map.Entry entry = (Map.Entry) i.next();
             String key = (String) entry.getKey();
             Object value = entry.getValue();
             
             if (key.startsWith(Constants.REST_HEADER_PREFIX)) {
                 buf.append(key).append(':').append(value);
             } else {
                 buf.append(value);
             }
             buf.append("\n");
         }
 
         // don't include the query parameters...
         int queryIndex = resource.indexOf('?');
         if (queryIndex == -1) {
             buf.append(resource);
         } else {
             buf.append(resource.substring(0, queryIndex));
         }
 
         // ...unless there is an acl, torrent, logging or requestPayment parameter.
         if (resource.matches(".*[&?]acl($|=|&).*")) {
             buf.append("?acl");
         } else if (resource.matches(".*[&?]torrent($|=|&).*")) {
             buf.append("?torrent");
         } else if (resource.matches(".*[&?]logging($|=|&).*")) {
             buf.append("?logging");
         } else if (resource.matches(".*[&?]location($|=|&).*")) {
             buf.append("?location");
         } else if (resource.matches(".*[&?]requestPayment($|=|&).*")) {
             buf.append("?requestPayment");
         }
 
         return buf.toString();
     }
     
     /**
      * Renames metadata property names to be suitable for use as HTTP Headers. This is done
      * by renaming any non-HTTP headers to have the prefix <code>x-amz-meta-</code> and leaving the 
      * HTTP header names unchanged. The HTTP header names left unchanged are those found in 
      * {@link #HTTP_HEADER_METADATA_NAMES} 
      * 
      * @param metadata
      * @return
      * a map of metadata property name/value pairs renamed to be suitable for use as HTTP headers.
      */
     public static Map renameMetadataKeys(Map metadata) {
         Map convertedMetadata = new HashMap();
         // Add all meta-data headers.
         if (metadata != null) {
             Iterator metaDataIter = metadata.entrySet().iterator();
             while (metaDataIter.hasNext()) {                
                 Map.Entry entry = (Map.Entry) metaDataIter.next();
                 String key = (String) entry.getKey();
                 Object value = entry.getValue();
 
                 if (!HTTP_HEADER_METADATA_NAMES.contains(key.toLowerCase(Locale.getDefault())) 
                     && !key.startsWith(Constants.REST_HEADER_PREFIX)) 
                 {
                     key = Constants.REST_METADATA_PREFIX + key;
                 }                
                 convertedMetadata.put(key, value);
             }
         }
         return convertedMetadata;
     }    
     
     /**
      * Initialises, or re-initialises, the underlying HttpConnectionManager and 
      * HttpClient objects a service will use to communicate with an AWS service. 
 	 * If proxy settings are specified in this service's {@link Jets3tProperties} object,
      * these settings will also be passed on to the underlying objects.
      * 
      * @param hostConfig
      * Custom HTTP host configuration; e.g to register a custom Protocol Socket Factory.
      * This parameter may be null, in which case a default host configuration will be
      * used.
      */
     public static HttpClientAndConnectionManager initHttpConnection(final AWSRequestAuthorizer awsRequestAuthorizer, 
         HostConfiguration hostConfig, Jets3tProperties jets3tProperties, String userAgentDescription,
         CredentialsProvider credentialsProvider) 
     {
         // Configure HttpClient properties based on Jets3t Properties.
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
        
         MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
         connectionManager.setParams(connectionParams);
         
         // Set user agent string.
         HttpClientParams clientParams = new HttpClientParams();
         String userAgent = jets3tProperties.getStringProperty("httpclient.useragent", null);
         if (log.isDebugEnabled()) {
             log.debug("Setting user agent string: " + userAgent);
         }
         clientParams.setParameter(HttpMethodParams.USER_AGENT, userAgent);
 
         clientParams.setBooleanParameter("http.protocol.expect-continue", true);
 
         // Replace default error retry handler.
         final int retryMaxCount = jets3tProperties.getIntProperty("httpclient.retry-max", 5);        
         
         clientParams.setParameter(HttpClientParams.RETRY_HANDLER, new HttpMethodRetryHandler() {
             public boolean retryMethod(HttpMethod httpMethod, IOException ioe, int executionCount) {
                 if (executionCount > retryMaxCount) {
                     if (log.isWarnEnabled()) {
                         log.warn("Retried connection " + executionCount 
                             + " times, which exceeds the maximum retry count of " + retryMaxCount);
                     }
                     return false;                    
                 }
                 
                 if  (ioe instanceof UnrecoverableIOException) {
                     if (log.isDebugEnabled()) {
                         log.debug("Deliberate interruption, will not retry");
                     }
                     return false;
                 }
                 if (log.isDebugEnabled()) {
                     log.debug("Retrying " + httpMethod.getName() + " request with path '" 
                         + httpMethod.getPath() + "' - attempt " + executionCount 
                         + " of " + retryMaxCount);
                 }
                 
                 // Build the authorization string for the method.
                 try {
                     awsRequestAuthorizer.authorizeHttpRequest(httpMethod);
                 } catch (Exception e) {
                     if (log.isWarnEnabled()) {
                         log.warn("Unable to generate updated authorization string for retried request", e);
                     }
                 }
                 
                 return true;
             }
         });
         
         HttpClient httpClient = new HttpClient(clientParams, connectionManager);
         httpClient.setHostConfiguration(hostConfig);
         
         if (credentialsProvider != null) {
             if (log.isDebugEnabled()) {
                 log.debug("Using credentials provider class: " + credentialsProvider.getClass().getName());
             }
             httpClient.getParams().setParameter(CredentialsProvider.PROVIDER, credentialsProvider);
             if (jets3tProperties.getBoolProperty("httpclient.authentication-preemptive", false)) {
                 httpClient.getParams().setAuthenticationPreemptive(true);
             }
         }              
         
         return new HttpClientAndConnectionManager(httpClient, connectionManager);
     }
     
     /**
      * Initialises this service's HTTP proxy by auto-detecting the proxy settings.
      */
     public static void initHttpProxy(HttpClient httpClient) {
         initHttpProxy(httpClient, true, null, -1, null, null, null);
     }
 
     /**
      * Initialises this service's HTTP proxy with the given proxy settings.
      * 
      * @param proxyHostAddress
      * @param proxyPort
      */
     public static void initHttpProxy(HttpClient httpClient, String proxyHostAddress, int proxyPort) {
         initHttpProxy(httpClient, false, proxyHostAddress, proxyPort, null, null, null);
     }
 
     /**
      * Initialises this service's HTTP proxy for authentication using the given 
      * proxy settings.
      * 
      * @param proxyHostAddress
      * @param proxyPort
      * @param proxyUser
      * @param proxyPassword
      * @param proxyDomain
      * if a proxy domain is provided, an {@link NTCredentials} credential provider
      * will be used. If the proxy domain is null, a 
      * {@link UsernamePasswordCredentials} credentials provider will be used.   
      */
     public static void initHttpProxy(HttpClient httpClient, String proxyHostAddress, 
         int proxyPort, String proxyUser, String proxyPassword, String proxyDomain) 
     {
         initHttpProxy(httpClient, false, proxyHostAddress, proxyPort, 
             proxyUser, proxyPassword, proxyDomain);
     }
 
     /**
      * @param httpClient
      * @param proxyAutodetect
      * @param proxyHostAddress
      * @param proxyPort
      * @param proxyUser
      * @param proxyPassword
      * @param proxyDomain
      */
     protected static void initHttpProxy(HttpClient httpClient, boolean proxyAutodetect, 
         String proxyHostAddress, int proxyPort, String proxyUser, 
         String proxyPassword, String proxyDomain) 
     {
         HostConfiguration hostConfig = httpClient.getHostConfiguration();
         
         // Use explicit proxy settings, if available.
         if (proxyHostAddress != null && proxyPort != -1) {
             if (log.isInfoEnabled()) {
                 log.info("Using Proxy: " + proxyHostAddress + ":" + proxyPort);
             }
             hostConfig.setProxy(proxyHostAddress, proxyPort);
             
             if (proxyUser != null && !proxyUser.trim().equals("")) {
                 if (proxyDomain != null) {
                     httpClient.getState().setProxyCredentials(
                         new AuthScope(proxyHostAddress, proxyPort),
                             new NTCredentials(proxyUser, proxyPassword, proxyHostAddress, proxyDomain));
                 }
                 else {
                     httpClient.getState().setProxyCredentials(
                         new AuthScope(proxyHostAddress, proxyPort),
                             new UsernamePasswordCredentials(proxyUser, proxyPassword));
                 }
             }
         }
         // If no explicit settings are available, try autodetecting proxies (unless autodetect is disabled)
         else if (proxyAutodetect) {        
             // Try to detect any proxy settings from applet.
             ProxyHost proxyHost = null;
             try {            
                 proxyHost = PluginProxyUtil.detectProxy(new URL("http://" + Constants.S3_HOSTNAME));
                 if (proxyHost != null) {
                     if (log.isInfoEnabled()) {
                         log.info("Using Proxy: " + proxyHost.getHostName() + ":" + proxyHost.getPort());
                     }
                     hostConfig.setProxyHost(proxyHost);
                 }                
             } catch (Throwable t) {
                 if (log.isDebugEnabled()) {
                     log.debug("Unable to set proxy configuration", t);
                 }
             }        
         }                
     }
 
     /**
      * Calculates a time offset value to reflect the time difference between your
      * computer's clock and the current time according to an AWS server, and 
      * returns the calculated time difference.
      * 
      * Ideally you should not rely on this method to overcome clock-related
      * disagreements between your computer and AWS. If you computer is set
      * to update its clock periodically and has the correct timezone setting 
      * you should never have to resort to this work-around.
      */
     public static long getAWSTimeAdjustment() throws Exception {
         RestS3Service restService = new RestS3Service(null);
         HttpClient client = restService.getHttpClient();
         long timeOffset = 0;
         
         // Connect to an AWS server to obtain response headers.
         GetMethod getMethod = new GetMethod("http://aws.amazon.com/");
         int result = client.executeMethod(getMethod);
         
         if (result == 200) {
             Header dateHeader = getMethod.getResponseHeader("Date");
             // Retrieve the time according to AWS, based on the Date header
             Date awsTime = ServiceUtils.parseRfc822Date(dateHeader.getValue());            
 
             // Calculate the difference between the current time according to AWS,
             // and the current time according to your computer's clock.
             Date localTime = new Date();
             timeOffset = awsTime.getTime() - localTime.getTime();
 
             if (log.isDebugEnabled()) {
                 log.debug("Calculated time offset value of " + timeOffset +
                         " milliseconds between the local machine and an AWS server");
             }
         } else {
             if (log.isWarnEnabled()) {
                 log.warn("Unable to calculate value of time offset between the "
                     + "local machine and AWS server");
             }            
         }
 
         return timeOffset;
     }
         
 }
