 /*
  * This is a utility project for wide range of applications
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.rest.client;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.WebResource.Builder;
 import com.sun.jersey.api.client.config.ClientConfig;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import javax.ws.rs.core.EntityTag;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriBuilder;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * An abstract resource representation on client side for a client to a RESTful Web Service. It is designed to have one
  * configuration application wide and one Apache Http Client application wide by default. It uses the Jersey Cacheable
  * Client which uses HTTPCache4J and thus if the configuration is changed for username/password it will reflect on next
  * request. But it is possible to change the client, config by simply specifying your own {@link ClientFactory}
  * @author imyousuf
  * @since 0.2
  */
 public abstract class AbstractClientResource<T, P extends Resource> implements Resource<T>, WritableResource<T>,
                                                                                PaginatedResource<P>, ConfigProcessor {
 
   protected static final URI BASE_URI;
   protected static final ConnectionConfig CONNECTION_CONFIG;
 
   static {
     CONNECTION_CONFIG = ConfigFactory.getInstance().getConnectionConfig();
     if (CONNECTION_CONFIG != null) {
       BASE_URI = UriBuilder.fromUri(CONNECTION_CONFIG.getContextPath()).path(CONNECTION_CONFIG.getBasicUri()).host(
           CONNECTION_CONFIG.getHost()).port(CONNECTION_CONFIG.getPort()).scheme("http").build();
     }
     else {
       BASE_URI = null;
     }
   }
   protected Logger logger = LoggerFactory.getLogger(getClass());
   private Resource referrer;
   private URI thisResourceUri;
   private URI absoluteThisResourceUri;
   private String representationType;
   private Class<? extends T> entityClass;
   private T lastReadStateOfEntity;
   private MultivaluedMap<String, ResourceLink> relatedResourceUris;
   private ClientUtil clientUtil;
   private ClientFactory clientFactory;
   private int getInvocationCount;
   private boolean followRedirectionEnabled;
   private boolean invokeGet;
   private final Map<String, Resource> nestedResources;
   private final Map<String, Map<String, Object>> cachedHeaders;
 
   protected AbstractClientResource(Resource referrer, ResourceLink resouceLink) throws
       IllegalArgumentException, UniformInterfaceException {
     this(referrer, resouceLink, null);
   }
 
   protected AbstractClientResource(Resource referrer, ResourceLink resouceLink, Class<? extends T> entityClass) throws
       IllegalArgumentException, UniformInterfaceException {
     this(referrer, resouceLink, entityClass, ClientUtilFactory.getInstance().getClientUtil(entityClass));
   }
 
   protected AbstractClientResource(Resource referrer, ResourceLink resouceLink, Class<? extends T> entityClass,
                                    ClientUtil clientUtil) throws IllegalArgumentException, UniformInterfaceException {
     this(referrer, resouceLink, entityClass, clientUtil, true);
   }
 
   protected AbstractClientResource(Resource referrer, ResourceLink resouceLink, Class<? extends T> entityClass,
                                    ClientUtil clientUtil, boolean invokeGet) throws IllegalArgumentException,
                                                                                     UniformInterfaceException {
     this(referrer, resouceLink, entityClass, clientUtil, invokeGet, null);
   }
 
   protected AbstractClientResource(Resource referrer, ResourceLink resouceLink, Class<? extends T> entityClass,
                                    ClientUtil clientUtil, boolean invokeGet, ClientFactory clientFactory)
       throws IllegalArgumentException, UniformInterfaceException {
     this(referrer, resouceLink.getUri(), resouceLink.getMimeType(), entityClass, clientUtil, invokeGet, clientFactory,
          true);
   }
 
   protected AbstractClientResource(Resource referrer, URI thisResourceUri, String representationType,
                                    Class<? extends T> entityClass) throws IllegalArgumentException,
                                                                           UniformInterfaceException {
     this(referrer, thisResourceUri, representationType, entityClass, ClientUtilFactory.getInstance().getClientUtil(
         entityClass));
   }
 
   protected AbstractClientResource(Resource referrer, URI thisResourceUri, String representationType,
                                    Class<? extends T> entityClass, ClientUtil clientUtil) throws
       IllegalArgumentException, UniformInterfaceException {
     this(referrer, thisResourceUri, representationType, entityClass, clientUtil, true, null, true);
   }
 
   /**
    * Construct a generic HTTP client resource's super class with necessary information for it to work properly
    * @param referrer The resource from which one arrived to this resource
    * @param thisResourceUri The URI, could be absolute or relative, of this resource.
    * @param representationType The MIME Type to expect for this resource, a.k.a., Accept HTTP header
    * @param entityClass The Entity class to ask Jersey to de-serialize the GET entity to.
    * @param clientUtil The client util instance to parse linked resources for this representation entity.
    * @param invokeGet If true GET will be issued during construction synchronously. It also means that on GET nested 
    *                  resources will also be GET so synchronize them. By default its true.
    * @throws IllegalArgumentException If thisResourceUri or representationType or entityClass is null
    * @throws UniformInterfaceException If status is anything but < 300 or 304.
    */
   protected AbstractClientResource(Resource referrer, URI thisResourceUri, String representationType,
                                    Class<? extends T> entityClass, ClientUtil clientUtil, boolean invokeGet,
                                    ClientFactory clientFactory, boolean followRedirection) throws
       IllegalArgumentException, UniformInterfaceException {
     if (thisResourceUri == null) {
       throw new IllegalArgumentException("URI to current resource can not be null");
     }
     if (StringUtils.isBlank(representationType)) {
       throw new IllegalArgumentException("Accept header value can not be null!");
     }
     if (entityClass == null) {
       entityClass = initializeEntityClassFromGenerics();
       if (entityClass == null) {
         throw new IllegalArgumentException("Entity class can not be null!");
       }
     }
     if (clientUtil == null && entityClass != null) {
       clientUtil = ClientUtilFactory.getInstance().getClientUtil(entityClass);
     }
     if (clientFactory == null) {
       if (referrer == null) {
         clientFactory = ApplicationWideClientFactoryImpl.getClientFactory(CONNECTION_CONFIG, this);
       }
       else {
         clientFactory = referrer.getClientFactory();
       }
     }
     this.clientFactory = clientFactory;
     this.referrer = referrer;
     this.thisResourceUri = thisResourceUri;
     this.representationType = representationType;
     this.entityClass = entityClass;
     this.relatedResourceUris = new ConcurrentMultivalueMap<String, ResourceLink>();
     this.clientUtil = clientUtil;
     this.absoluteThisResourceUri = generateAbsoluteUri();
     this.followRedirectionEnabled = followRedirection;
     this.nestedResources = new HashMap<String, Resource>();
     this.cachedHeaders = new HashMap<String, Map<String, Object>>();
     if (invokeGet) {
       get();
     }
   }
 
   protected final Class<? extends T> initializeEntityClassFromGenerics() {
     Class<? extends T> extractedEntityClass = null;
     try {
       Type paramType =
            ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
       if (paramType instanceof ParameterizedType) {
         paramType = ((ParameterizedType) paramType).getRawType();
       }
       Class<T> pesistenceRegistryClass = paramType instanceof Class ? (Class<T>) paramType : null;
       if (logger.isDebugEnabled()) {
         logger.debug("Entity class predicted to: " + pesistenceRegistryClass.toString());
       }
       extractedEntityClass = pesistenceRegistryClass;
     }
     catch (Exception ex) {
       logger.warn("Could not predict entity class ", ex);
     }
     return extractedEntityClass;
   }
 
   protected boolean isFollowRedirectionEnabled() {
     return followRedirectionEnabled;
   }
 
   protected void setFollowRedirectionEnabled(boolean followRedirectionEnabled) {
     this.followRedirectionEnabled = followRedirectionEnabled;
   }
 
   protected boolean isInvokeGet() {
     return invokeGet;
   }
 
   protected void setInvokeGet(boolean invokeGet) {
     this.invokeGet = invokeGet;
   }
 
   protected void addNestedResource(String key, Resource resource) {
     nestedResources.put(key, resource);
   }
 
   protected void removeNestedResource(String key) {
     nestedResources.remove(key);
   }
 
   protected <K> Resource<K> getNestedResource(String key) {
     return nestedResources.get(key);
   }
 
   protected Map<String, Resource> getNestedResources() {
     return Collections.unmodifiableMap(nestedResources);
   }
 
   protected final URI generateAbsoluteUri() {
     final URI thisUri = this.thisResourceUri;
     final URI referrerUri = getReferrerUri();
     return getHttpClient().getAbsoluteUri(thisUri, referrerUri);
   }
 
   protected final URI getReferrerUri() {
     return this.referrer == null ? null : this.referrer.getUri();
   }
 
   protected void getIfFirstTimeRequest() {
     if (getGetInvocationCount() <= 0) {
       get();
     }
   }
 
   protected int getGetInvocationCount() {
     return getInvocationCount;
   }
 
   protected ClientUtil getClientUtil() {
     return clientUtil;
   }
 
   protected MultivaluedMap<String, ResourceLink> getRelatedResourceUris() {
     return relatedResourceUris;
   }
 
   @Override
   public Class<? extends T> getEntityClass() {
     return entityClass;
   }
 
   @Override
   public String getResourceRepresentationType() {
     return representationType;
   }
 
   @Override
   public final T get() {
     return get(getUri());
   }
 
   protected T get(URI uri) {
     ClientResponse response = ClientUtil.readEntity(uri, getHttpClient(), getResourceRepresentationType(),
                                                     ClientResponse.class);
     if (logger.isDebugEnabled()) {
       logger.debug("Request Accept header: " + getResourceRepresentationType());
       logger.debug("Response header: " + response.getType());
     }
     final int status = response.getStatus();
     if (followRedirectionEnabled && status == ClientResponse.Status.MOVED_PERMANENTLY.getStatusCode()) {
       final URI location = response.getLocation();
       if (location != null) {
         this.thisResourceUri = location;
         this.absoluteThisResourceUri = generateAbsoluteUri();
         return get();
       }
     }
     if (followRedirectionEnabled && (status == ClientResponse.Status.FOUND.getStatusCode() ||
                                      status == ClientResponse.Status.SEE_OTHER.getStatusCode())) {
       final URI location = response.getLocation();
       if (location != null) {
         URI absolutionLocation = getHttpClient().getAbsoluteUri(location, getReferrerUri());
         return get(absolutionLocation);
       }
     }
     if (status < 300 ||
         (status == ClientResponse.Status.NOT_MODIFIED.getStatusCode())) {
       if (response.hasEntity() && response.getStatus() != ClientResponse.Status.NO_CONTENT.getStatusCode()) {
         lastReadStateOfEntity = response.getEntity(getEntityClass());
         if (getClientUtil() != null) {
           try {
             getClientUtil().parseLinks(lastReadStateOfEntity, getRelatedResourceUris());
           }
           catch (Exception ex) {
             logger.warn(ex.getMessage(), ex);
           }
         }
         if (invokeGet) {
           invokeGETOnNestedResources();
         }
       }
       else {
         lastReadStateOfEntity = null;
       }
       getInvocationCount++;
       Map<String, Object> headers = new HashMap<String, Object>();
       EntityTag tag = response.getEntityTag();
       if (tag != null) {
         headers.put(HttpHeaders.ETAG, tag);
       }
       Date date = response.getLastModified();
       if (date != null) {
         headers.put(HttpHeaders.LAST_MODIFIED, date);
       }
       cachedHeaders.put(uri.toString(), headers);
       return lastReadStateOfEntity;
     }
    throw new UniformInterfaceException(response);
   }
 
   protected void invokeGETOnNestedResources() {
     final Collection<Resource> values = nestedResources.values();
     for (Resource resource : values) {
       resource.get();
     }
   }
 
   @Override
   public T getLastReadStateOfEntity() {
     getIfFirstTimeRequest();
     return lastReadStateOfEntity;
   }
 
   @Override
   public URI getUri() {
     return getAbsoluteThisResourceUri();
   }
 
   @Override
   public ClientResponse delete(Status... status) {
     WebResource webResource = getHttpClient().getWebResource(getUri());
     Builder builder = addNecessaryHeaders(getUri(), webResource);
     final ClientResponse response = builder.delete(ClientResponse.class);
     checkStatus(response, status);
     return response;
   }
 
   @Override
   public <P> ClientResponse put(String contentType, P param, Status... status) {
     WebResource webResource = getHttpClient().getWebResource(getUri());
     Builder builder = addNecessaryHeaders(getUri(), webResource);
     builder.type(contentType);
     final ClientResponse response = builder.put(ClientResponse.class, param);
     checkStatus(response, status);
     return response;
   }
 
   @Override
   public <P> ClientResponse post(String contentType, P param, Status... status) {
     WebResource webResource = getHttpClient().getWebResource(getUri());
     Builder builder = addNecessaryHeaders(getUri(), webResource);
     builder.type(contentType);
     final ClientResponse response = builder.post(ClientResponse.class, param);
     checkStatus(response, status);
     return response;
   }
 
   @Override
   public <V> Resource<V> getReferrer() {
     return referrer;
   }
 
   @Deprecated
   public URI getBaseUri() {
     return BASE_URI;
   }
 
   protected URI getThisResourceUri() {
     return thisResourceUri;
   }
 
   protected URI getAbsoluteThisResourceUri() {
     return absoluteThisResourceUri;
   }
 
   @Deprecated
   protected UriBuilder getBaseUriBuilder() {
     return UriBuilder.fromUri(BASE_URI.toString());
   }
 
   protected UriBuilder getCurrentUriBuilder() {
     return UriBuilder.fromUri(getAbsoluteThisResourceUri());
   }
 
   protected final Client getClient() {
     return clientFactory.getClient();
   }
 
   protected final ClientConfig getClientConfig() {
     return clientFactory.getClientConfig();
   }
 
   protected final HttpClient getHttpClient() {
     return clientFactory.getHttpClient();
   }
 
   protected abstract void processClientConfig(ClientConfig clientConfig);
 
   @Override
   public final void process(ClientConfig clientConfig) {
     processClientConfig(clientConfig);
   }
 
   @Override
   public ResourceLink nextUri() {
     return getNextUri();
   }
 
   @Override
   public ResourceLink previousUri() {
     return getPreviousUri();
   }
 
   @Override
   public P next() {
     return getPageableResource(nextUri());
   }
 
   @Override
   public P previous() {
     return getPageableResource(previousUri());
   }
 
   @Override
   public ClientFactory getClientFactory() {
     return clientFactory;
   }
 
   protected abstract ResourceLink getNextUri();
 
   protected abstract ResourceLink getPreviousUri();
 
   protected P getPageableResource(ResourceLink link) {
     if (link == null) {
       return null;
     }
     return instantiatePageableResource(link);
   }
 
   protected abstract P instantiatePageableResource(ResourceLink link);
 
   protected void checkStatus(ClientResponse response, Status... status) {
     if (status == null || status.length == 0) {
       return;
     }
     if (Arrays.<Status>asList(status).contains(response.getClientResponseStatus())) {
       return;
     }
     throw new UniformInterfaceException(response);
   }
 
   protected Builder addNecessaryHeaders(URI uri, WebResource resource) {
     Map<String, Object> headers = cachedHeaders.get(uri.toString());
     Builder builder = resource.getRequestBuilder();
     if (headers != null) {
       final Object etag = headers.get(HttpHeaders.ETAG);
       if (etag != null) {
         builder.header(HttpHeaders.IF_MATCH, etag);
       }
       final Object lastModified = headers.get(HttpHeaders.LAST_MODIFIED);
       if (lastModified != null) {
         builder.header(HttpHeaders.IF_UNMODIFIED_SINCE, lastModified);
       }
     }
     return builder;
   }
 }
