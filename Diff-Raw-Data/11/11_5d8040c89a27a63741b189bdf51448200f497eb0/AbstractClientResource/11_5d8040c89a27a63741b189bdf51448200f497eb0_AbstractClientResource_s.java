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
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import java.net.URI;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriBuilder;
 import org.apache.commons.lang.StringUtils;
 
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
 
     BASE_URI = UriBuilder.fromUri(CONNECTION_CONFIG.getContextPath()).path(CONNECTION_CONFIG.getBasicUri()).host(
         CONNECTION_CONFIG.getHost()).port(CONNECTION_CONFIG.getPort()).scheme("http").build();
   }
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
     this(referrer, resouceLink.getUri(), resouceLink.getMimeType(), entityClass, clientUtil, invokeGet, clientFactory);
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
     this(referrer, thisResourceUri, representationType, entityClass, clientUtil, true, null);
   }
 
   /**
    * Construct a generic HTTP client resource's super class with necessary information for it to work properly
    * @param referrer The resource from which one arrived to this resource
    * @param thisResourceUri The URI, could be absolute or relative, of this resource.
    * @param representationType The MIME Type to expect for this resource, a.k.a., Accept HTTP header
    * @param entityClass The Entity class to ask Jersey to de-serialize the GET entity to.
    * @param clientUtil The client util instance to parse linked resources for this representation entity.
    * @param invokeGet If true GET will be issued during construction synchronously. By default its true
    * @throws IllegalArgumentException If thisResourceUri or representationType or entityClass is null
    * @throws UniformInterfaceException If status is anything but < 300 or 304.
    */
   protected AbstractClientResource(Resource referrer, URI thisResourceUri, String representationType,
                                    Class<? extends T> entityClass, ClientUtil clientUtil, boolean invokeGet,
                                    ClientFactory clientFactory) throws IllegalArgumentException,
                                                                        UniformInterfaceException {
     if (thisResourceUri == null) {
       throw new IllegalArgumentException("URI to current resource can not be null");
     }
     if (StringUtils.isBlank(representationType)) {
       throw new IllegalArgumentException("Accept header value can not be null!");
     }
     if (entityClass == null) {
       throw new IllegalArgumentException("Entity class can not be null!");
     }
     if (clientFactory == null) {
       clientFactory = ApplicationWideClientFactoryImpl.getClientFactory(CONNECTION_CONFIG, this);
     }
     this.clientFactory = clientFactory;
     this.referrer = referrer;
     this.thisResourceUri = thisResourceUri;
     this.representationType = representationType;
     this.entityClass = entityClass;
     this.relatedResourceUris = new ConcurrentMultivalueMap<String, ResourceLink>();
     this.clientUtil = clientUtil;
     this.absoluteThisResourceUri = getHttpClient().getAbsoluteUri(thisResourceUri, referrer == null ? null : referrer.
         getUri());
     if (invokeGet) {
       get();
     }
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
   public T get() {
     getInvocationCount++;
     ClientResponse response = ClientUtil.readEntity(getUri(), getHttpClient(), getResourceRepresentationType(),
                                                     ClientResponse.class);
     if (response.getStatus() < 300 ||
         (response.getStatus() == ClientResponse.Status.NOT_MODIFIED.getStatusCode() && response.hasEntity())) {
       lastReadStateOfEntity = response.getEntity(getEntityClass());
       if (getClientUtil() != null) {
         try {
           getClientUtil().parseLinks(lastReadStateOfEntity, getRelatedResourceUris());
         }
         catch (Exception ex) {
           ex.printStackTrace();
         }
       }
       return lastReadStateOfEntity;
     }
     throw new UniformInterfaceException(response);
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
   public ClientResponse delete() {
     WebResource webResource = getHttpClient().getWebResource(getUri());
     return webResource.delete(ClientResponse.class);
   }
 
   @Override
   public <P> ClientResponse put(String contentType, P param) {
     WebResource webResource = getHttpClient().getWebResource(getUri());
    webResource.type(contentType);
    return webResource.put(ClientResponse.class, param);
   }
 
   @Override
   public <P> ClientResponse post(String contentType, P param) {
     WebResource webResource = getHttpClient().getWebResource(getUri());
    webResource.type(contentType);
    return webResource.post(ClientResponse.class, param);
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
 
   protected abstract ResourceLink getNextUri();
 
   protected abstract ResourceLink getPreviousUri();
 
   protected P getPageableResource(ResourceLink link) {
     if (link == null) {
       return null;
     }
     return instantiatePageableResource(link);
   }
 
   protected abstract P instantiatePageableResource(ResourceLink link);
 }
