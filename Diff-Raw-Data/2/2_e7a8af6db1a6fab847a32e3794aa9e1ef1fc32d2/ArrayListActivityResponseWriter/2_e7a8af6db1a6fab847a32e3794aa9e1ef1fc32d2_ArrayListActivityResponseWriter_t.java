 package com.collective.resources.writers;
 
 import com.collective.resources.responses.ArrayListActivityResponse;
 import org.apache.abdera2.activities.model.ASBase;
 import org.apache.abdera2.activities.model.Activity;
 import org.apache.abdera2.activities.model.CollectionWriter;
 import org.apache.abdera2.activities.model.IO;
 import org.apache.log4j.Logger;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Provider;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 
 /**
  * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com )
  */
 @Provider
 @Produces(MediaType.APPLICATION_JSON)
 public class ArrayListActivityResponseWriter implements MessageBodyWriter<ArrayListActivityResponse>
 {
 
     private Logger logger = Logger.getLogger(ArrayListActivityResponseWriter.class);
 
     public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType)
     {
        return ArrayListActivityResponse.class.isAssignableFrom(aClass);
     }
 
     public long getSize(ArrayListActivityResponse arrayListActivityResponse, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType)
     {
         return -1;
     }
 
     public void writeTo(ArrayListActivityResponse arrayListActivityResponse, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream)
             throws IOException, WebApplicationException
     {
         IO io;
         io = IO.make()
                 .autoClose()
                 .charset("UTF-8")
                 .prettyPrint()
                 .get();
 
         StringWriter sw = new StringWriter();
         CollectionWriter cw = io.getCollectionWriter(sw);
 
         //names should be the same as the ServiceResponse class
         cw.writeHeader(
                 ASBase.make()
                         .set("status", arrayListActivityResponse.getStatus())
                         .set("message", arrayListActivityResponse.getMessage())
                         .get());
 
         for (Activity activity : arrayListActivityResponse.getObject()) {
             cw.writeObject(activity);
         }
         cw.complete();
 
         String jsonised = sw.toString();
 
         logger.debug(jsonised);
         BufferedOutputStream baos = new BufferedOutputStream(outputStream);
         baos.write(jsonised.getBytes());
         baos.close();
     }
 }
