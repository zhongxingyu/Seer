package com.redhat.rhevm.api.resteasy.json;
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.codehaus.jackson.map.ser.CustomSerializerFactory;
 
 public class CustomObjectMapper extends ObjectMapper
 {
     public static CustomObjectMapper get() {
         CustomObjectMapper mapper = new CustomObjectMapper();
         mapper.setSerializer(new CustomBeanFactory()).includeDefaults(false).indent(true);
         return mapper;
     }
 
     protected CustomObjectMapper setSerializer(CustomSerializerFactory ser) {
         setSerializerFactory(ser);
         getSerializationConfig().setSerializationView(String.class);
         return this;
     }
 
     protected CustomObjectMapper includeDefaults(boolean include) {
         getSerializationConfig().setSerializationInclusion(
             include ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_DEFAULT);
         return this;
     }
 
     protected CustomObjectMapper indent(boolean indent) {
         configure(SerializationConfig.Feature.INDENT_OUTPUT, indent);
         return this;
     }
 }
