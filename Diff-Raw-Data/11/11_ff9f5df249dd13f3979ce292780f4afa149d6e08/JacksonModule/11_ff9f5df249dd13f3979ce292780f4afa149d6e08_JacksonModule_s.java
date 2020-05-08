 package edu.uw.zookeeper.jackson.databind;
 
 import java.util.List;
 import java.util.Map;
 
 import com.fasterxml.jackson.databind.JsonDeserializer;
 import com.fasterxml.jackson.databind.JsonSerializer;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 
 public abstract class JacksonModule {
     
     public static SimpleModule create() {
         return create(ImmutableMap.<Class<?>, JsonDeserializer<?>>of(), ImmutableList.<JsonSerializer<?>>of());
     }
 
     public static SimpleModule create(Map<Class<?>, JsonDeserializer<?>> deserializers, List<JsonSerializer<?>> serializers) {
         return new SimpleModule(Version.PROJECT_NAME, PROJECT_VERSION, deserializers, serializers);
     }
     
     public static final com.fasterxml.jackson.core.Version PROJECT_VERSION = new com.fasterxml.jackson.core.Version(
             Integer.valueOf(Version.VERSION_FIELDS[0]), 
             Integer.valueOf(Version.VERSION_FIELDS[1]), 
             Integer.valueOf(Version.VERSION_FIELDS[2]),
            Version.VERSION_FIELDS[3], 
             Version.GROUP, Version.ARTIFACT);
     
     private JacksonModule() {}
 }
