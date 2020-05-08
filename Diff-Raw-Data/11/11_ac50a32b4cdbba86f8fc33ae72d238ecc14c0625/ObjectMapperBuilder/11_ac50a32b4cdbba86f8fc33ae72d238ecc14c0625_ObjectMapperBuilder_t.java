 package com.dottydingo.hyperion.service.marshall;
 
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import org.springframework.beans.factory.FactoryBean;
 
 /**
  */
 public class ObjectMapperBuilder implements FactoryBean<ObjectMapper>
 {
 
     @Override
     public ObjectMapper getObject() throws Exception
     {
         ObjectMapper mapper = new ObjectMapper();
         mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
 
         return mapper;
     }
 
     @Override
     public Class<?> getObjectType()
     {
         return ObjectMapper.class;
     }
 
     @Override
     public boolean isSingleton()
     {
         return true;
     }
 }
