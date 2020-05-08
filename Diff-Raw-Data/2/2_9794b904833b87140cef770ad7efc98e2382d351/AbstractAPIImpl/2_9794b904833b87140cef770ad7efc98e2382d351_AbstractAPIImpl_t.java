 /**
  * Copyright 2012 Terremark Worldwide Inc.
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
 package com.terremark.impl;
 
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.net.util.SubnetUtils;
 import org.apache.http.conn.util.InetAddressUtils;
 import org.apache.wink.client.ClientWebException;
 import org.apache.wink.client.Resource;
 import org.apache.wink.client.RestClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.terremark.api.TerremarkError;
 import com.terremark.config.ContentType;
 import com.terremark.config.RetryHandler;
 import com.terremark.config.Version;
 import com.terremark.exception.AccessDeniedException;
 import com.terremark.exception.AuthenticationDeniedException;
 import com.terremark.exception.InternalServerException;
 import com.terremark.exception.InvalidRequestException;
 import com.terremark.exception.NotFoundException;
 import com.terremark.exception.NotImplementedException;
 import com.terremark.exception.RequestFailedException;
 import com.terremark.exception.ServiceUnavailableException;
 import com.terremark.exception.TerremarkException;
 
 /**
  * Abstract class extended by all handler implementations. Provides implementations for generic
  * {@code get/post/put/delete} HTTP calls. Performs query argument validation, if necessary. And is also responsible for
  * exception/retry handling.
  *
  * @author <a href="mailto:spasam@terremark.com">Seshu Pasam</a>
  */
 @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
 abstract class AbstractAPIImpl {
     /** Logger */
     private static final Logger LOG = LoggerFactory.getLogger("com.terremark");
     /** HTML content-type */
     private static final String HTML_CONTENT_TYPE = "text/html";
     /** Rest client instance */
     private final RestClient client;
     /** Client configuration */
     private final ClientConfiguration properties;
     /** API version */
     private final Version clientVersion;
 
     /**
      * Default constructor.
      *
      * @param client Rest client instance.
      * @param properties Client configuration.
      */
     protected AbstractAPIImpl(final RestClient client, final ClientConfiguration properties) {
         this.client = client;
         this.properties = properties;
         this.clientVersion = properties.getVersion();
     }
 
     /**
      * HTTP get call. Delegates the call to {@link #get(Version, String, Map, Map, Class, Object...)}.
      *
      * @param version API version this method call was implemented in.
      * @param relativePath Relative path.
      * @param responseClass Expected response type.
      * @param arguments API call arguments.
      * @return Response.
      * @throws TerremarkException If an error occurs or if an Terremark error is returned.
      */
     <T> T get(final Version version, final String relativePath, final Class<T> responseClass, final Object... arguments)
                     throws TerremarkException {
         return get(version, relativePath, null, null, responseClass, arguments);
     }
 
     /**
      * HTTP get call. Checks for API version mis-match. Validates the arguments, if necessary. Retries the call, if a
      * retry handler is configured and the API invocation fails.
      *
      * @param version API version this method call was implemented in.
      * @param relativePath Relative path.
      * @param queryParams Query parameters.
      * @param extraHeaders Additional headers.
      * @param responseClass Expected response type.
      * @param arguments API call arguments.
      * @return Response.
      * @throws TerremarkException If an error occurs or if an Terremark error is returned.
      */
     @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
     <T> T get(final Version version, final String relativePath, final Map<String, String> queryParams,
                     final Map<String, String> extraHeaders, final Class<T> responseClass, final Object... arguments)
                     throws TerremarkException {
         checkVersion(version);
         validateArguments(arguments);
 
         if (responseClass == null) {
             throw new IllegalArgumentException("Invalid response type");
         }
 
         int failureCount = 0;
         final RetryHandler retryHandler = properties.getRetryHandler();
         do {
             try {
                 return getResource(relativePath, queryParams, extraHeaders, arguments).get(responseClass);
             } catch (final Exception ex) {
                 failureCount++;
 
                 try {
                     handleException(ex);
                 } catch (TerremarkException te) {
                     if (te.getMajorErrorCode() > 499
                                     && retryHandler != null
                                     && retryHandler.shouldRetry(failureCount, te, relativePath, queryParams,
                                                     extraHeaders, responseClass, arguments)) {
                         LOG.warn("Retrying request: {}. Failure count: {}. HTTP status: {}. Code: {}. Message: {}",
                                         new Object[] {relativePath, Integer.valueOf(failureCount),
                                                         Integer.valueOf(te.getMajorErrorCode()),
                                                         te.getMinorErrorCode(), te.getErrorMessage()});
                         continue;
                     }
 
                     throw te;
                 }
             }
         } while (true);
     }
 
     /**
      * HTTP put call. Checks for API version mis-match. Validates the arguments, if necessary.
      *
      * @param version API version this method call was implemented in.
      * @param relativePath Relative path.
      * @param responseClass Expected response type.
      * @param requestEntity Request entity to send a body.
      * @param arguments API call arguments.
      * @throws TerremarkException If an error occurs or if an Terremark error is returned.
      */
     <R, S> S put(final Version version, final String relativePath, final Class<S> responseClass, final R requestEntity,
                     final Object... arguments) throws TerremarkException {
         checkVersion(version);
         validateArguments(arguments);
 
         if (requestEntity == null) {
             throw new IllegalArgumentException("Invalid request entity argument");
         }
 
         try {
             return getResource(relativePath, requestEntity, arguments).put(responseClass, requestEntity);
         } catch (final Exception ex) {
             handleException(ex);
             return null;
         }
     }
 
     /**
      * HTTP post call. Checks for API version mis-match. Validates the arguments, if necessary.
      *
      * @param version API version this method call was implemented in.
      * @param relativePath Relative path.
      * @param responseClass Expected response type.
      * @param requestEntity Request entity to send a body.
      * @param arguments API call arguments.
      * @throws TerremarkException If an error occurs or if an Terremark error is returned.
      */
     <R, S> S post(final Version version, final String relativePath, final Class<S> responseClass,
                     final R requestEntity, final Object... arguments) throws TerremarkException {
         checkVersion(version);
         validateArguments(arguments);
 
         try {
             return getResource(relativePath, requestEntity, arguments).post(responseClass, requestEntity);
         } catch (final Exception ex) {
             handleException(ex);
             return null;
         }
     }
 
     /**
      * HTTP delete call. Checks for API version mis-match. Validates the arguments, if necessary.
      *
      * @param version API version this method call was implemented in.
      * @param relativePath Relative path.
      * @param responseClass Expected response type.
      * @param arguments API call arguments.
      * @throws TerremarkException If an error occurs or if an Terremark error is returned.
      */
     void delete(final Version version, final String relativePath, final Object... arguments) throws TerremarkException {
         checkVersion(version);
         validateArguments(arguments);
 
         try {
             getResource(relativePath, null, arguments).delete();
         } catch (final Exception ex) {
             handleException(ex);
         }
     }
 
     /**
      * HTTP get call. Checks for API version mis-match. Validates the arguments, if necessary.
      *
      * @param version API version this method call was implemented in.
      * @param relativePath Relative path.
      * @param responseClass Expected response type.
      * @param arguments API call arguments.
      * @return Response.
      * @throws TerremarkException If an error occurs or if an Terremark error is returned.
      */
     <S> S delete(final Version version, final String relativePath, final Class<S> responseClass,
                     final Object... arguments) throws TerremarkException {
         checkVersion(version);
         validateArguments(arguments);
 
         try {
             return getResource(relativePath, null, arguments).delete(responseClass);
         } catch (final Exception ex) {
             handleException(ex);
             return null;
         }
     }
 
     /**
      * Returns the ISO 8601 format date/time.
      *
      * @param time Date/time.
      * @return ISO 8601 format date/time.
      */
     protected static String getISO8601Time(final Date time) {
         if (time == null) {
             throw new IllegalArgumentException("Invalid date/time argument");
         }
 
         final SimpleDateFormat sdf = new SimpleDateFormat(TerremarkConstants.ISO_8601_DATE_FORMAT, Locale.getDefault());
         sdf.setTimeZone(TerremarkConstants.GMT_TIME_ZONE);
 
         return sdf.format(time);
     }
 
     /**
      * Validates the query arguments against the metadata. {@link java.lang.IllegalArgumentException} is thrown if the
      * arguments does not match the metadata information.
      *
      * @param filterArguments Query arguments. Can be null.
      * @param metadata Metadata for the query arguments.
      */
     @SuppressWarnings({"unused", "PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidDuplicateLiterals"})
     protected static void validateQueryArguments(final Map<String, String> filterArguments,
                     final Map<String, QueryArgument> metadata) {
         if (filterArguments == null) {
             return;
         }
 
         for (Map.Entry<String, String> entry : filterArguments.entrySet()) {
             final String key = entry.getKey();
             final String value = entry.getValue();
 
             if (key == null) {
                 throw new IllegalArgumentException("Invalid filter argument key");
             }
             if (StringUtils.isEmpty(value)) {
                 throw new IllegalArgumentException("Invalid filter argument value for " + key);
             }
 
             final QueryArgument argInfo = metadata.get(key);
             if (argInfo == null) {
                 throw new IllegalArgumentException("Invalid filter argument: " + key);
             }
 
             switch (argInfo.getType()) {
             case INTEGER:
                 int i;
                 try {
                     i = Integer.parseInt(value);
                 } catch (NumberFormatException ex) {
                     throw new IllegalArgumentException("Invalid filter argument value for '" + key + "': " + value
                                     + ". Must be a valid integer", ex);
                 }
 
                 if (argInfo.getMinValue() != Integer.MAX_VALUE && argInfo.getMaxValue() != Integer.MIN_VALUE
                                 && (i < argInfo.getMinValue() || i > argInfo.getMaxValue())) {
                     throw new IllegalArgumentException("Invalid filter argument value for '" + key + "': " + value
                                     + ". It should be between " + argInfo.getMinValue() + " and "
                                     + argInfo.getMaxValue());
                 }
                 break;
             case LIST:
                 boolean found = false;
                 for (String str : argInfo.getArgs()) {
                     if (value.equalsIgnoreCase(str)) {
                         found = true;
                         break;
                     }
                 }
 
                 if (!found) {
                     throw new IllegalArgumentException("Invalid filter argument value for '" + key + "': " + value
                                     + ". It should be one of: " + Arrays.asList(argInfo.getArgs()));
                 }
                 break;
             case ISO8601_DATE:
                 final SimpleDateFormat sdf = new SimpleDateFormat(TerremarkConstants.ISO_8601_DATE_FORMAT,
                                 Locale.getDefault());
                 try {
                     sdf.parse(value);
                 } catch (ParseException ex) {
                     throw new IllegalArgumentException("Invalid filter argument value for '" + key + "': " + value
                                     + ". Must be a valid date/time in ISO 8601 format: yyyy-MM-dd'T'HH:mm:'00Z'", ex);
                 }
                 break;
             case HOSTNAME:
                 try {
                     InetAddress.getByName(value);
                 } catch (UnknownHostException ex) {
                     throw new IllegalArgumentException("Invalid filter argument value for '" + key + "': " + value
                                     + ". Must be a valid hostname/IP address", ex);
                 }
                 break;
             case IP_ADDRESS:
                 if (!InetAddressUtils.isIPv4Address(value)) {
                     throw new IllegalArgumentException("Invalid filter argument value for '" + key + "': " + value
                                     + ". Must be a valid IPv4 address");
                 }
                 break;
             case SUBNET:
                 new SubnetUtils(value);
                 break;
             case URI:
                 try {
                     new URI(value);
                 } catch (URISyntaxException ex) {
                     throw new IllegalArgumentException("Invalid filter argument value for '" + key + "': " + value
                                     + ". Must be a valid relative URI", ex);
                 }
                 break;
             default:
                 break;
             }
         }
     }
 
     /**
      * Compares the API method version against the client configured version. If the client is configured to use older
      * version and a newer API method is invoked, {@link NotImplementedException} is thrown.
      *
      * @param apiVersion Version the API method was implemented in.
      * @throws NotImplementedException If client is configured to use older version and a newer API method is invoked
      */
     private void checkVersion(final Version apiVersion) throws NotImplementedException {
         if (clientVersion == null) {
             return;
         }
 
         if (clientVersion.ordinal() < apiVersion.ordinal()) {
             throw new NotImplementedException("Terremark client is configured to use API version "
                             + clientVersion.name() + "/" + clientVersion.toString()
                             + ". The API method you are invoking is supported in version " + clientVersion.name() + "/"
                             + clientVersion.toString() + " or later");
         }
     }
 
     /**
      * Validates arguments. If the argument is null, {@link java.lang.IllegalArgumentException} is thrown.
      *
      * @param arguments Can be null or zero size.
      */
     private static void validateArguments(final Object... arguments) {
         if (arguments == null || arguments.length < 1) {
             return;
         }
 
         for (Object arg : arguments) {
             if (arg == null) {
                 throw new IllegalArgumentException("Invalid input argument");
             }
         }
     }
 
     /**
      * Method to process exception. For all 4XX/5XX error codes, an exception is thrown by the REST implementation. In
      * most cases, an appropriate Terremark error is also returned, which contains more details on why the API call
      * failed. This method, throws specific exceptions for the various error conditions.
      *
      * @param exception Root cause.
      * @throws TerremarkException More specific exception.
      */
     @SuppressWarnings("PMD")
     private static void handleException(final Exception exception) throws TerremarkException {
         if (!(exception instanceof ClientWebException)) {
             throw new TerremarkException(exception);
         }
 
         TerremarkError error = null;
         ClientWebException ex = (ClientWebException) exception;
 
         if (ex.getResponse() != null) {
             String contentType = ex.getResponse().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
 
             try {
                 if (contentType != null && contentType.startsWith(HTML_CONTENT_TYPE)) {
                     LOG.error("Got {} error from Terremark with text/html response", Integer.valueOf(ex.getResponse().getStatusCode()));
                 } else {
                     error = ex.getResponse().getEntity(TerremarkError.class);
                 }
             } catch (Exception ignore) {
                 // We don't want this to mask the root cause
                 LOG.error("Terremark Java API error. Please report this to the developers. Exception retrieving Terremark error. "
                                 + "HTTP status: {}. HTTP message: {}. HTTP Headers: {}",
                                 new Object[] {Integer.toString(ex.getResponse().getStatusCode()),
                                                 ex.getResponse().getMessage(), ex.getResponse().getHeaders(), ignore});
            } finally {
                ex.getResponse().consumeContent();
             }
         }
 
         if (ex.getResponse() != null) {
             switch (ex.getResponse().getStatusCode()) {
             case 400: // Replay attack, clock skew etc
             case 412: // Invalid version
             case 415: // Unsupported media type
                 throw new InvalidRequestException(error, ex);
             case 401: // Authentication failed
             case 407: // Proxy authentication required
                 throw new AuthenticationDeniedException(error, ex);
             case 403:
                 throw new AccessDeniedException(error, ex);
             case 404: // Request object not found
                 throw new NotFoundException(error, ex);
             case 409:
             case 420:
             case 421:
                 throw new RequestFailedException(error, ex);
             case 500:
                 throw new InternalServerException(error, ex);
             case 501:
                 throw new NotImplementedException(error, ex);
             case 503:
                 throw new ServiceUnavailableException(error, ex);
             default: // Just to make PMD happy
                 throw new TerremarkException(error, ex);
             }
         }
 
         throw new TerremarkException(error, ex);
     }
 
     /**
      * Generic method used for all HTTP calls. This is responsible for constructing rest client request and returning
      * the response.
      *
      * @param relativePath Relative path.
      * @param queryParams Query arguments.
      * @param extraHeaders Additional headers.
      * @param arguments Arguments for the path.
      * @return Resource that can be deserialized as a response.
      */
     private Resource getResource(final String relativePath, final Map<String, String> queryParams,
                     final Map<String, String> extraHeaders, final Object... arguments) {
         final UriBuilder builder = UriBuilder.fromPath(properties.getUri() + relativePath);
 
         if (queryParams != null) {
             for (final Map.Entry<String, String> entry : queryParams.entrySet()) {
                 if (entry.getValue() != null) {
                     builder.queryParam(entry.getKey(), entry.getValue());
                 }
             }
         }
 
         Resource resource = client.resource(builder.build(arguments)).accept(getContentType());
 
         if (extraHeaders != null) {
             for (final Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                 if (entry.getValue() != null) {
                     resource = resource.header(entry.getKey(), entry.getValue());
                 }
             }
         }
 
         return resource;
     }
 
     /**
      * Used by {@code put}/{@code post}/{@code delete} requests. This just calls
      * {@link #getResource(String, Map, Map, Object...)}. Sets {@code Content-Type} header if the request entity is not
      * null. If the request entity is null, this method sets the {@code Content-Length} header to zero.
      *
      * @param relativePath Relative path of the request.
      * @param arguments Arguments for building the URL.
      * @return The resource on which to execute the HTTP request.
      */
     @SuppressWarnings("PMD.UnusedPrivateMethod")
     private <T> Resource getResource(final String relativePath, final T requestEntity, final Object... arguments) {
         final Resource resource = getResource(relativePath, null, null, arguments);
 
         if (requestEntity == null) {
             return resource.header(HttpHeaders.CONTENT_LENGTH, "0");
         }
 
         return resource.header(HttpHeaders.CONTENT_TYPE, getContentType());
     }
 
     /**
      * Returns the content type as configured by the user.
      *
      * @return Content type.
      */
     private String getContentType() {
         if (properties.getContentType() == ContentType.XML) {
             return MediaType.APPLICATION_XML;
         }
 
         return MediaType.APPLICATION_JSON;
     }
 }
