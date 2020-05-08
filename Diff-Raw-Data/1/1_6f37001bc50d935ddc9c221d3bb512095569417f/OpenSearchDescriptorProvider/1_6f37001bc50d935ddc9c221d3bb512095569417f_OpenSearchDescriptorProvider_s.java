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
 package com.smartitengineering.util.opensearch.jaxrs;
 
 import com.smartitengineering.util.opensearch.api.OpenSearchDescriptor;
 import com.smartitengineering.util.opensearch.io.impl.dom.DomIOImpl;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.ext.MessageBodyReader;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Provider;
 
 /**
  *
  * @author imyousuf
  */
 @Provider
 @Produces(com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML)
 public class OpenSearchDescriptorProvider implements MessageBodyWriter<OpenSearchDescriptor>,
                                                      MessageBodyReader<OpenSearchDescriptor> {
 
   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
     if (com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML_TYPE.equals(
         mediaType) && OpenSearchDescriptor.class.isAssignableFrom(type)) {
       return true;
     }
     return false;
   }
 
   @Override
   public long getSize(OpenSearchDescriptor t,
                       Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
     return -1;
   }
 
   @Override
   public void writeTo(OpenSearchDescriptor t,
                       Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                     WebApplicationException {
     if (isWriteable(type, genericType, annotations, mediaType)) {
       DomIOImpl impl = new DomIOImpl();
       impl.writeOpenSearchDescriptor(entityStream, t);
     }
     throw new IOException("Write not supported!");
   }
 
   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
     return isWriteable(type, genericType, annotations, mediaType);
   }
 
   @Override
   public OpenSearchDescriptor readFrom(Class<OpenSearchDescriptor> type, Type genericType, Annotation[] annotations,
                                        MediaType mediaType,
                                        MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws
       IOException,
       WebApplicationException {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 }
