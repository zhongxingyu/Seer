 /*
  * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 
 package org.sonatype.sisu.siesta.jackson;
 
 import javax.inject.Named;
 import javax.inject.Provider;
 
 import com.fasterxml.jackson.annotation.JsonInclude.Include;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
 
 /**
  * <a href="http://jackson.codehaus.org">Jackson</a> {@link ObjectMapper} provider.
  *
  * @since 1.2
  */
 @Named
 public class ObjectMapperProvider
     implements Provider<ObjectMapper>
 {
   private final ObjectMapper mapper;
 
   public ObjectMapperProvider() {
     final ObjectMapper mapper = new ObjectMapper();
 
     // Configure Jackson annotations only, JAXB annotations can confuse and produce improper content
     mapper.getDeserializationConfig()
         .with(new JacksonAnnotationIntrospector());
     // do not write null values
     mapper.getSerializationConfig()
         .with(new JacksonAnnotationIntrospector())
         .withSerializationInclusion(Include.NON_NULL);
 
     // Write dates as ISO-8601
     mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
 
     // FIXME: Disable this, as it requires use of @JsonProperty on fields, only required
     // FIXME: ... if object has something that looks like a getter/setter but isn't one
 
     // Disable detection of setters & getters
     //mapper.configure(AUTO_DETECT_IS_GETTERS, false);
     //mapper.configure(AUTO_DETECT_GETTERS, false);
     //mapper.configure(AUTO_DETECT_SETTERS, false);
 
     // Make the output look more readable
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
 
     this.mapper = mapper;
   }
 
   public ObjectMapper get() {
     return mapper;
   }
 }
