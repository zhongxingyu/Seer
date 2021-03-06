 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
  * 
  * The contents of this file are subject to the terms of the Common Development
  * and Distribution License("CDDL") (the "License").  You may not use this file
  * except in compliance with the License. 
  * 
  * You can obtain a copy of the License at:
  *     https://jersey.dev.java.net/license.txt
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * When distributing the Covered Code, include this CDDL Header Notice in each
  * file and include the License file at:
  *     https://jersey.dev.java.net/license.txt
  * If applicable, add the following below this CDDL Header, with the fields
  * enclosed by brackets [] replaced by your own identifying information:
  *     "Portions Copyrighted [year] [name of copyright owner]"
  */
 
 package com.sun.ws.rest.impl;
 
 import com.sun.jersey.api.core.HttpRequestContext;
 import java.net.URI;
 import java.util.Iterator;
 import java.util.List;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 
 /**
  *
  * @author Paul.Sandoz@Sun.Com
  */
 public final class ResponseImpl extends Response {
     private final int status;
 
     private final Object entity;
 
     private final Object[] values;
     
     private final List<Object> nameValuePairs;
     
     private MultivaluedMap<String, Object> headers;
     
     ResponseImpl(int status, Object entity, Object[] values, List<Object> nameValuePairs) {
         this.status = status;
         this.entity = entity;
         this.values = values;
         this.nameValuePairs = nameValuePairs;
     }
 
     // Response 
     
     public Object getEntity() {
         return entity;
     }
 
     public int getStatus() {
         return status;
     }
 
     public MultivaluedMap<String, Object> getMetadata() {
        if (headers != null)
            return headers;
        
        headers = new ResponseHttpHeadersImpl();
         
         for (int i = 0; i < values.length; i++)
             if (values[i] != null)
                 headers.putSingle(ResponseBuilderImpl.getHeader(i), values[i]);
 
         Iterator i = nameValuePairs.iterator();
         while (i.hasNext()) {
             headers.add((String)i.next(), i.next());
         }
         
         return headers;
     }
     
     public MultivaluedMap<String, Object> getMetadataOptimal(
             HttpRequestContext request, MediaType contentType) {
        if (headers != null)
            return headers;
        
        headers = new ResponseHttpHeadersImpl();
 
         if (values.length == 0 && contentType != null) {
             headers.putSingle(ResponseBuilderImpl.
                     getHeader(ResponseBuilderImpl.CONTENT_TYPE), contentType);
         }
         
         for (int i = 0; i < values.length; i++) {
             switch(i) {
                 case ResponseBuilderImpl.CONTENT_TYPE:
                     if (values[i] != null)
                         headers.putSingle(ResponseBuilderImpl.getHeader(i), values[i]);
                     else if (contentType != null)
                         headers.putSingle(ResponseBuilderImpl.getHeader(i), contentType);
                     break;
                 case ResponseBuilderImpl.LOCATION:
                     Object location = values[i];
                     if (location != null) {
                         if (location instanceof URI) {
                             if (!((URI)location).isAbsolute()) {
                                 String path = ((URI)location).getRawPath();
                                 if (status == 201)
                                     location = UriBuilder.fromUri(request.getAbsolutePath()).
                                             encode(false).path(path).build();
                                 else
                                     location = UriBuilder.fromUri(request.getBaseUri()).
                                             encode(false).path(path).build();
                             }
                         }
                         headers.putSingle(ResponseBuilderImpl.getHeader(i), location);
                     }
                     break;
                 default:
                     if (values[i] != null)
                         headers.putSingle(ResponseBuilderImpl.getHeader(i), values[i]);
             }
         }
 
         Iterator i = nameValuePairs.iterator();
         while (i.hasNext()) {
             headers.add((String)i.next(), i.next());
         }
         
         return headers;
     }
 }
