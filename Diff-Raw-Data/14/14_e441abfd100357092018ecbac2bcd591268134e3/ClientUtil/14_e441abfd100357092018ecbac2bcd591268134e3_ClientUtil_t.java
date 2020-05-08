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
 
 import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;
 import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
 import java.net.URI;
 import javax.ws.rs.core.MultivaluedMap;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A utility method to read entities
  * @author imyousuf
  */
 public abstract class ClientUtil {
 
   public static <T> T getResponseEntity(ClientResponse response, Class<? extends T> clazz) {
     return response.getEntity(clazz);
   }
 
   public static ClientResponse readClientResponse(URI uri, HttpClient client, String acceptType) {
     return readEntity(uri, client, acceptType, ClientResponse.class);
   }
 
   public static ResourceLink createResourceLink(String rel, URI uri, String mimeType) {
     DefaultResouceLinkImpl linkImpl = new DefaultResouceLinkImpl();
     linkImpl.setMimeType(mimeType);
     linkImpl.setRel(rel);
     linkImpl.setUri(uri);
     return linkImpl;
   }
 
   public static <T> T readEntity(final URI uri, HttpClient client, String acceptType, Class<? extends T> clazz) {
     if (uri != null && client != null && clazz != null) {
       try {
        UniformInterface uniformInterface;
         WebResource resource = client.getWebResource(uri);
        uniformInterface = resource;
         if (StringUtils.isNotBlank(acceptType)) {
          Builder builder = resource.accept(acceptType);
          uniformInterface = builder;
         }
        T newEntity = uniformInterface.get(clazz);
         return newEntity;
       }
       catch (Exception ex) {
         ex.printStackTrace();
       }
     }
     return null;
   }
 
   public abstract <T> void parseLinks(T entity,
                                       MultivaluedMap<String, ResourceLink> uris)
       throws Exception;
 }
