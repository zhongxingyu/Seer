 package com.github.ddth.plommon.utils;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.libs.Json;
 
 /**
  * Serialize Java object to JSON-string and vice versa.
  * 
  * <p>
  * Many times you just want to serialize your Java object to JSON-string and
  * vice versa, without touching any JsonNode object. This utility class provides
  * methods to do just that.
  * </p>
  * 
  * @author Thanh Nguyen <btnguyen2k@gmail.com>
  * @since 0.1.0
  */
 public class JsonUtils {
     /**
      * Serializes an object to Json string.
      * 
      * @param obj
      * @return
      */
     public static String toJsonString(Object obj) {
         return Json.stringify(Json.toJson(obj));
     }
 
     /**
      * Deserializes a Json string.
      * 
      * @param jsonString
      * @param clazz
      * @return
      */
     public static <T> T fromJsonString(String jsonString, Class<T> clazz) {
        JsonNode jsonNode = Json.parse(jsonString);
        return jsonNode != null ? Json.fromJson(jsonNode, clazz) : null;
     }
 
     /**
      * Deserializes a Json string.
      * 
      * @param jsonString
      * @return
      * @since 0.2.1
      */
     public static Object fromJsonString(String jsonString) {
         JsonNode jsonNode = Json.parse(jsonString);
         return jsonNode != null ? Json.fromJson(jsonNode, Object.class) : null;
     }
 }
