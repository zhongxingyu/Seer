 package com.dottydingo.hyperion.service.marshall;
 
 import com.dottydingo.hyperion.exception.BadRequestException;
 import com.dottydingo.hyperion.exception.InternalException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  */
 public class EndpointMarshaller
 {
     private ObjectMapper objectMapper;
 
     public EndpointMarshaller()
     {
         try
         {
             objectMapper = new ObjectMapperBuilder().getObject();
         }
         catch (Exception ignore){}
     }
 
     public void setObjectMapper(ObjectMapper objectMapper)
     {
         this.objectMapper = objectMapper;
     }
 
     public <T> T unmarshall(InputStream inputStream, Class<T> type)
     {
         try
         {
             return objectMapper.readValue(inputStream,type);
         }
         catch (Exception e)
         {
            throw new BadRequestException(String.format("Error unmarshalling request: %s",e.getMessage()),e);
         }
     }
 
 
     public <T> void marshall(OutputStream outputStream, T value)
     {
         try
         {
             objectMapper.writeValue(outputStream,value);
         }
         catch(Exception e)
         {
             throw new InternalException("Error marhsalling response.",e);
         }
 
     }
 }
