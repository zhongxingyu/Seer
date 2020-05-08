 /*
  *
  * This is a framework for Asynchronous Event processing based on event hub.
  * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.events.async.api.impl.hub;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 import com.smartitengineering.events.async.api.EventPublisher;
 import com.smartitengineering.util.rest.client.jersey.cache.CacheableClient;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author imyousuf
  */
 @Singleton
 public class EventPublisherImpl implements EventPublisher {
 
   @Inject
   @Named("channelHubUri")
   private String eventHubChannelHubUri;
   private final Client cacheableClient = CacheableClient.create();
   private WebResource channelHubResource;
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
 
   @Override
   public boolean publishEvent(String eventContentType, String eventMessage) {
     if (channelHubResource == null) {
       channelHubResource = cacheableClient.resource(eventHubChannelHubUri);
     }
    if (logger.isInfoEnabled()) {
      logger.info("Publishing message " + eventContentType + " of type " + eventContentType);
    }
     ClientResponse response = channelHubResource.accept(MediaType.MEDIA_TYPE_WILDCARD).header(HttpHeaders.CONTENT_TYPE,
                                                                                               eventContentType).post(
         ClientResponse.class, eventMessage);
     final int status = response.getStatus();
    response.close();
     return status >= 200 && status < 400;
   }
 }
