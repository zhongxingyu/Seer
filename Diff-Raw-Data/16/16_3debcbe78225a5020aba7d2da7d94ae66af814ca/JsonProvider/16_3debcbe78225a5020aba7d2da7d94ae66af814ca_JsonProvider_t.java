package com.redhat.rhevm.api.resteasy.json;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.Provider;
 
 import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
 
 @Provider
 @Consumes(MediaType.APPLICATION_JSON)
 @Produces(MediaType.APPLICATION_JSON)
 public class JsonProvider extends ResteasyJacksonProvider
 {
     public JsonProvider() {
         super();
         setMapper(CustomObjectMapper.get());
     }
 
     @Override
     public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
         return super.isReadable(aClass, type, annotations, mediaType);
     }
 
     @Override
     public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
         return super.isWriteable(aClass, type, annotations, mediaType);
     }
 }
