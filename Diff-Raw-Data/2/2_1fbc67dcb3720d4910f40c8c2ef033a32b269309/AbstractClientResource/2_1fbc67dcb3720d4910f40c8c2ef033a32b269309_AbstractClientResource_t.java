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
 
 import com.smartitengineering.util.rest.client.jersey.cache.CacheableClient;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import java.net.URI;
 import javax.ws.rs.core.UriBuilder;
 
 /**
  *
  * @author russel
  */
 public abstract class AbstractClientResource<T> implements Resource<T>, WritableResource<T> {
 
   protected static final URI BASE_URI;
   protected static final ConnectionConfig CONNECTION_CONFIG;
 
   static {
     CONNECTION_CONFIG = ConfigFactory.getInstance().getConnectionConfig();
 
     BASE_URI = UriBuilder.fromUri(CONNECTION_CONFIG.getContextPath()).path(CONNECTION_CONFIG.getBasicUri()).host(
         CONNECTION_CONFIG.getHost()).port(CONNECTION_CONFIG.getPort()).scheme("http").build();
   }
   private static Client client;
   private static ClientConfig clientConfig;
   private static HttpClient httpClient;
   private Resource referrer;
   private URI thisResourceUri;
   private URI absoluteThisResourceUri;
   private String representationType;
   private Class<? extends T> entityClass;
   private T lastReadStateOfEntity;
 
   protected AbstractClientResource(Resource referrer, URI thisResourceUri, String representationType,
                                    Class<? extends T> entityClass) {
     this.referrer = referrer;
     this.thisResourceUri = thisResourceUri;
    this.representationType = representationType;
    this.entityClass = entityClass;
     this.absoluteThisResourceUri = getHttpClient().getAbsoluteUri(thisResourceUri, referrer == null ? null : referrer.
         getUri());
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
     lastReadStateOfEntity = ClientUtil.readEntity(getUri(), getHttpClient(), getResourceRepresentationType(),
                                                   getEntityClass());
     return lastReadStateOfEntity;
   }
 
   @Override
   public T getLastReadStateOfEntity() {
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
 
   public URI getBaseUri() {
     return BASE_URI;
   }
 
   protected URI getThisResourceUri() {
     return thisResourceUri;
   }
 
   protected URI getAbsoluteThisResourceUri() {
     return absoluteThisResourceUri;
   }
 
   protected UriBuilder getBaseUriBuilder() {
     return UriBuilder.fromUri(BASE_URI.toString());
   }
 
   protected Client getClient() {
     if (client == null) {
       client = initializeClient();
     }
     return client;
   }
 
   protected Client initializeClient() {
     processClientConfig(getClientConfig());
     return CacheableClient.create(getClientConfig());
   }
 
   protected ClientConfig getClientConfig() {
     if (clientConfig == null) {
       clientConfig = new DefaultClientConfig();
     }
     return clientConfig;
   }
 
   protected final HttpClient getHttpClient() {
     if (httpClient == null) {
       httpClient = new HttpClient(getClient(), BASE_URI.getHost(), CONNECTION_CONFIG.getPort());
     }
     return httpClient;
   }
 
   protected abstract void processClientConfig(ClientConfig clientConfig);
 }
