 package com.vonhof.babelshark;
 
 import com.vonhof.babelshark.exception.MappingException;
 import com.vonhof.babelshark.node.SharkNode;
 import com.vonhof.babelshark.node.SharkType;
 import com.vonhof.babelshark.reflect.ClassInfo;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Singleton instance of the babelshark engine
  *
  * @author Henrik Hofmeister <@vonhofdk>
  */
 public class BabelShark {
 
     private static final BabelSharkInstance instance = new BabelSharkInstance();
 
     private BabelShark() {
     }
 
     public static BabelSharkInstance getDefaultInstance() {
         return instance;
     }
     
     public static void register(SharkLanguage language) {
         instance.register(language);
     }
 
     public static String getDefaultType() {
         return instance.getDefaultType();
     }
 
     public static <T> T read(Input input, SharkType<T, ?> type) throws MappingException, IOException {
         return instance.read(input, type);
     }
 
     public static <T> T read(Input input, Class<T> clz) throws MappingException, IOException {
         return instance.read(input, clz);
     }
 
     public static <T> T read(String raw, String type, Class<T> clz) throws MappingException, IOException {
         return instance.read(raw, type, clz);
     }
    
    public static <T> T read(String raw, Class<T> clz) throws MappingException, IOException {
        return instance.read(raw, getDefaultType(), clz);
    }
 
     public static <T> T readAsValue(SharkNode node, SharkType<T, ?> type) throws MappingException {
         return instance.readAsValue(node, type);
     }
 
     public static <T> T readAsValue(SharkNode node, Class<T> clz) throws MappingException {
         return instance.readAsValue(node, clz);
     }
     
     public static <T> T readAsValue(SharkNode node, ClassInfo<T> clz) throws MappingException {
         return instance.readAsValue(node, clz);
     }
 
     public static <T> List<T> readAsList(SharkNode node, SharkType<List, T> type) throws MappingException {
         return instance.readAsList(node, type);
     }
 
     public static <T> List<T> readAsList(SharkNode node, Class<T> clz) throws MappingException {
         return instance.readAsList(node, clz);
     }
 
     public static <T> Map<String, T> readAsMap(SharkNode node, SharkType<Map, T> type) throws MappingException {
         return instance.readAsMap(node, type);
     }
 
     public static <T> Map<String, T> readAsMap(SharkNode node, Class<T> clz) throws MappingException {
         return instance.readAsMap(node, clz);
     }
 
     public static void write(Output output, Object value) throws MappingException, IOException {
         instance.write(output, value);
     }
    
    public static String writeToString(Object value) throws MappingException, IOException {
        return writeToString(value, getDefaultType());
    }
 
 
     public static String writeToString(Object value, String contentType) throws MappingException, IOException {
         return instance.writeToString(value, contentType);
     }
 
     public static byte[] writeToByteArray(Object value, String contentType) throws MappingException, IOException {
         return instance.writeToByteArray(value, contentType);
     }
 
     public static String getMimeType(String type) {
         return instance.getMimeType(type);
     }
    public static String getMimeType(String type,boolean useDefault) {
        return instance.getMimeType(type,useDefault);
     }
 }
