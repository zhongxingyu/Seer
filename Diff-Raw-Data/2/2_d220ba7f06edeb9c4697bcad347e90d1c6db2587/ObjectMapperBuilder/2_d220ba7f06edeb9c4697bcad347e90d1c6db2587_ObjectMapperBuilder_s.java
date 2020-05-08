 package edu.uw.zookeeper.clients.trace;
 
 import java.util.List;
 import java.util.Map;
 
 import com.fasterxml.jackson.databind.JsonDeserializer;
 import com.fasterxml.jackson.databind.JsonSerializer;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import edu.uw.zookeeper.common.Builder;
 import edu.uw.zookeeper.jackson.databind.ProtocolRequestDeserializer;
 import edu.uw.zookeeper.jackson.databind.ProtocolRequestSerializer;
 import edu.uw.zookeeper.jackson.databind.Version;
 import edu.uw.zookeeper.protocol.Operation;
 
 public class ObjectMapperBuilder implements Builder<ObjectMapper> {
 
     public static ObjectMapperBuilder defaults() {
         return new ObjectMapperBuilder();
     }
     
     public static final com.fasterxml.jackson.core.Version PROJECT_VERSION = new com.fasterxml.jackson.core.Version(
             Integer.valueOf(Version.VERSION_FIELDS[0]), 
             Integer.valueOf(Version.VERSION_FIELDS[1]), 
             Integer.valueOf(Version.VERSION_FIELDS[2]),
            Version.VERSION_FIELDS[3], 
             Version.GROUP, Version.ARTIFACT);
     
     public ObjectMapper build() {
         List<JsonSerializer<?>> serializers = ImmutableList.<JsonSerializer<?>>of(
                 ProtocolRequestSerializer.create(),
                 ProtocolResponseHeaderSerializer.create(),
                 TraceEventHeader.serializer());
         Map<Class<?>, JsonDeserializer<?>> deserializers = ImmutableMap.<Class<?>, JsonDeserializer<?>>of(
                 Operation.ProtocolRequest.class, ProtocolRequestDeserializer.create(),
                 Operation.ProtocolResponse.class, ProtocolResponseHeaderDeserializer.create(),
                 TraceEventHeader.class, TraceEventHeader.deserializer());
         SimpleModule module = new SimpleModule(Version.PROJECT_NAME, PROJECT_VERSION, deserializers, serializers);
         ObjectMapper instance = new ObjectMapper();
         instance.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
         instance.registerModule(module);
         return instance;
     }
 }
