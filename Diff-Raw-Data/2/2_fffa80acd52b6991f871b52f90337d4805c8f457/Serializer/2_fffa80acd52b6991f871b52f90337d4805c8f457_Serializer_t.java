 package com.kfuntak.gwt.json.serialization.client;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONException;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.json.client.JSONObject;
 
 public class Serializer {
 
     private static Map SERIALIZABLE_TYPES;
 
     private static Map serializableTypes() {
         if (SERIALIZABLE_TYPES == null) {
             SERIALIZABLE_TYPES = new HashMap();
         }
         return SERIALIZABLE_TYPES;
     }
 
     protected void addObjectSerializer(String name, ObjectSerializer obj) {
         serializableTypes().put(name, obj);
     }
 
     protected ObjectSerializer getObjectSerializer(String name) {
         if (name.equals("java.util.ArrayList")) {
             return new ArrayListSerializer();
         } else if (name.equals("java.util.HashMap")) {
             return new HashMapSerializer();
         }
 
         if(serializableTypes().containsKey(name)){
             return (ObjectSerializer) serializableTypes().get(name);
         } else {
             throw new SerializationException("Can't find object serializer for " + name);
         }
     }
 
     protected Serializer() {
     }
 
     static protected String getTypeName(Object obj) {
         // WARNING: GWT.getTypeName is deprecated
         //String typeName = GWT.getTypeName( obj );
         //typeName = typeName.substring(typeName.lastIndexOf('.')+1);
         //return typeName.toLowerCase();
         String typeName = obj.getClass().getName();
         return typeName;
     }
 
     public String serialize(Object pojo) {
         try {
             Collection<?> col = (Collection<?>) pojo;
             new ArrayListSerializer().serialize(pojo);
         } catch (ClassCastException e) {
         }
         try {
             Map<String,?> map = (Map<String,?>) pojo;
             new HashMapSerializer().serialize(pojo);
         } catch (ClassCastException e) {
         }
         String name = getTypeName(pojo);
         return getObjectSerializer(name).serialize(pojo);
     }
 
     public JSONValue serializeToJson(Object pojo) {
         if (pojo == null) {
             return null;
         }
 
         String name = getTypeName(pojo);
         return getObjectSerializer(name).serializeToJson(pojo);
     }
 
     public Object deSerialize(JSONValue jsonValue, String className) throws JSONException {
         return getObjectSerializer(className).deSerialize(jsonValue, className);
     }
 
     public Object deSerialize(String jsonString, String className) throws JSONException {
         return getObjectSerializer(className).deSerialize(jsonString, className);
     }
 
     public Object deSerialize(String jsonString) {
         return deSerialize(JSONParser.parseLenient(jsonString));
     }
 
     public Object deSerialize(JSONValue jsonValue) {
         JSONObject obj = jsonValue.isObject();
         if (obj != null) {
             if (obj.containsKey("class") && obj.get("class").isString() != null) {
                 return deSerialize(jsonValue, obj.get("class").isString().stringValue());
             }
         }
 
         throw new IllegalArgumentException("Json string must contain \"class\" key.");
     }
 
     public static <T> T marshall(String data, String typeString) {
         return marshall(data, typeString, null);
     }
 
     public static <T> T marshall(String data) {
        return marshall(data, null, null);
     }
 
     public static <T> T marshall(String data, String typeString, T defaultValue) {
         if(GWT.isClient() && data != null && !data.isEmpty()){
             Serializer serializer = new Serializer();
             T object = (T)serializer.deSerialize(data, typeString);
             if (object == null) {
                 return defaultValue;
             } else {
                 return object;
             }
 
         }
         return defaultValue;
     }
 
     public static <T> T marshall(String data, T defaultValue) {
         if(GWT.isClient() && data != null && !data.isEmpty()){
             Serializer serializer = new Serializer();
             T object = (T)serializer.deSerialize(data);
             if (object == null) {
                 return defaultValue;
             } else {
                 return object;
             }
         }
         return defaultValue;
     }
 
     public static String marshall(Object object, String defaultValue) {
         if (GWT.isClient() && object != null) {
             Serializer serializer = new Serializer();
             return serializer.serialize(object);
         }
         return defaultValue;
     }
 
     public static String marshall(Object object) {
         return marshall(object, "");
     }
 }
