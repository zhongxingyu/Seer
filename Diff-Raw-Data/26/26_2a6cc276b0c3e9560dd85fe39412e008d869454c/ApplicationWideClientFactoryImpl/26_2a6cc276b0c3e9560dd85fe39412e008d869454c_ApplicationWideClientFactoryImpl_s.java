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
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 
 /**
  *
  * @author imyousuf
  */
 public class ApplicationWideClientFactoryImpl implements ClientFactory {
 
   public static final String TRACE = "com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl.trace";
   private Client client;
   private ClientConfig clientConfig;
   private HttpClient httpClient;
 
   private ApplicationWideClientFactoryImpl(ConnectionConfig connectionConfig, ConfigProcessor processor) {
     if (connectionConfig == null) {
       throw new IllegalArgumentException("Connection config can not be null!");
     }
     clientConfig = new DefaultClientConfig();
     if (processor != null) {
       processor.process(clientConfig);
     }
     client = CacheableClient.create(clientConfig);
     if (Boolean.parseBoolean(System.getProperty(TRACE))) {
       client.addFilter(new LoggingFilter());
     }
     httpClient = new HttpClient(client, connectionConfig.getHost(), connectionConfig.getPort());
   }
  private static ApplicationWideClientFactoryImpl clientFactoryImpl;
 
   public static ClientFactory getClientFactory(ConnectionConfig connectionConfig, ConfigProcessor processor) {
    if (clientFactoryImpl == null) {
      clientFactoryImpl = new ApplicationWideClientFactoryImpl(connectionConfig, processor);
     }
    return clientFactoryImpl;
   }
 
   @Override
   public Client getClient() {
     return client;
   }
 
   @Override
   public HttpClient getHttpClient() {
     return httpClient;
   }
 
   @Override
   public ClientConfig getClientConfig() {
     return clientConfig;
   }
 }
