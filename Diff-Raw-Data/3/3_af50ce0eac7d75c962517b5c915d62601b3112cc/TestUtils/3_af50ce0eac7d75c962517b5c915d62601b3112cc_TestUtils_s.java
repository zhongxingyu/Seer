 package org.motechproject.ananya.kilkari.web;
 
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 public class TestUtils {
     public static String toJson(Object objectToSerialize) {
         ObjectMapper mapper = new ObjectMapper();
         StringWriter stringWriter = new StringWriter();
         try {
             mapper.writeValue(stringWriter, objectToSerialize);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return stringWriter.toString();
     }
 
     public static <T> T fromJson(String jsonString, Class<T> subscriberResponseClass) {
         ObjectMapper mapper = new ObjectMapper();
         T serializedObject = null;
         try {
             serializedObject = mapper.readValue(jsonString, subscriberResponseClass);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return serializedObject;
     }
 }
