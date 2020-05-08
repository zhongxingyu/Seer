 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.server;
 
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Provider;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 
 /**
  * Configure the Jackson module use to marshalling Json to objects.
  */
 @Provider
 public class JacksonConfigurator implements ContextResolver<ObjectMapper> {
 
     /**
      * Mapper to access to the configuration.
      */
     private ObjectMapper mapper = new ObjectMapper();
 
     /**
      * Default constructor.
      */
     public JacksonConfigurator() {
         this.mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
 
        // mapper.setDateFormat(new
         // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ"));
 
         // FIXME Use to avoid Error 500 when a field is unknown
         // this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
         // false);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
      */
     @Override
     public ObjectMapper getContext(final Class<?> arg0) {
         return this.mapper;
     }
 
 }
