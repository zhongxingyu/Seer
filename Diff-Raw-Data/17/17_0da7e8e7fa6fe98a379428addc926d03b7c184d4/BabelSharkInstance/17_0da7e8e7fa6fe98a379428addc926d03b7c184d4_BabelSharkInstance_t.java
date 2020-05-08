 package com.vonhof.babelshark;
 
 import com.vonhof.babelshark.annotation.TypeResolver;
 import com.vonhof.babelshark.converter.*;
 import com.vonhof.babelshark.exception.MappingException;
 import com.vonhof.babelshark.node.ObjectNode;
 import com.vonhof.babelshark.node.SharkNode;
 import com.vonhof.babelshark.node.SharkType;
 import com.vonhof.babelshark.node.ValueNode;
 import com.vonhof.babelshark.reflect.ClassInfo;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Timestamp;
 import java.util.*;
 
 /**
  * Singleton instance of the babelshark engine
  *
  * @author Henrik Hofmeister <@vonhofdk>
  */
 public final class BabelSharkInstance {
     private final Map<String, SharkLanguage> languages = new HashMap<String, SharkLanguage>();
     private final Map<String, ObjectReader> readers = new HashMap<String, ObjectReader>();
     private final Map<String, ObjectWriter> writers = new HashMap<String, ObjectWriter>();
     private final TypeRegistry<SharkSerializer> serializers = new TypeRegistry<SharkSerializer>();
     private final TypeRegistry<SharkDeserializer> deserializers = new TypeRegistry<SharkDeserializer>();
     
     public BabelSharkInstance() {
         registerSimple(new SimpleConverter());
         register(Map.class, new MapConverter());
         register(Collection.class, new CollectionConverter());
         register(Object.class, new BeanConverter());
         register(Enum.class, new EnumConverter());
         register(BeanMap.class, new BeanMapConverter());
     }
 
     public void register(SharkLanguage language) {
         final ObjectReader reader = language.getObjectReader();
         final ObjectWriter writer = language.getObjectWriter();
 
         for (String contentType : reader.getContentTypes()) {
             contentType = normalizeContentType(contentType);
             readers.put(contentType, reader);
         }
         writers.put(writer.getContentType(), writer);
 
         //Add language ids as content type - for convenience
         readers.put(language.getId(), reader);
         writers.put(language.getId(), writer);
 
         languages.put(language.getId(), language);
     }
     
     public <T> void registerSimple(SharkConverter<T> converter) {
         //Simple built-in immutables
         register(Class.class, converter);
         register(UUID.class, converter);
         register(Date.class, converter);
         register(java.sql.Date.class, converter);
         register(Timestamp.class, converter);
         register(String.class, converter);
         register(BigDecimal.class, converter);
         register(BigInteger.class, converter);
         register(Calendar.class, converter);
         
         //Primitive types and their wrappers
         register(Boolean.class, converter);
         register(Boolean.TYPE, converter);
         register(Float.class, converter);
         register(Float.TYPE, converter);
         register(Integer.class, converter);
         register(Integer.TYPE, converter);
         register(Long.class, converter);
         register(Long.TYPE, converter);
         register(Short.class, converter);
         register(Short.TYPE, converter);
         register(Double.class, converter);
         register(Double.TYPE, converter);
         register(Byte.class, converter);
         register(Byte.TYPE, converter);
         register(Character.class, converter);
         register(Character.TYPE, converter);
         register(Float.class, converter);
         register(Float.TYPE, converter);
     }
     
     public <T> void register(Class type,SharkConverter<T> converter) {
         register(SharkType.get(type), converter);
     }
     
     public <T> void register(Class type,SharkDeserializer<T> converter) {
         register(SharkType.get(type), converter);
     }
     public <T> void register(Class type,SharkSerializer<T> converter) {
         register(SharkType.get(type), converter);
     }
     public <T> void register(SharkType type,SharkConverter<T> converter) {
         serializers.put(type, converter);
         deserializers.put(type, converter);
     }
     public <T> void register(SharkType type,SharkSerializer<T> converter) {
         serializers.put(type, converter);
     }
     
     public <T> void register(SharkType type,SharkDeserializer converter) {
         deserializers.put(type, converter);
     }
 
     public String getDefaultType() {
         if (!languages.isEmpty()) {
             return languages.keySet().
                     iterator().
                     next();
         }
         return null;
     }
 
     private ObjectReader getReader(String contentType) {
         contentType = normalizeContentType(contentType);
         return readers.get(contentType);
     }
 
     private ObjectWriter getWriter(String contentType) {
         contentType = normalizeContentType(contentType);
         return writers.get(contentType);
     }
 
     public <T> T read(Input input, SharkType<T, ?> type) throws MappingException, IOException {
         ObjectReader reader = getReader(input.getContentType());
         if (reader == null) {
             throw new MappingException(String.format("Unknown content type: %s", input.getContentType()));
         }
         SharkNode map = reader.read(input);
         return read(map, type);
     }
 
     public <T> T read(Input input, Class<T> clz) throws MappingException, IOException {
         return read(input, SharkType.get(clz));
     }
     
     public <T> T read(Input input, ClassInfo<T> clz) throws MappingException, IOException {
         return read(input, SharkType.get(clz));
     }
 
     public <T> T read(String raw, String type, Class<T> clz) throws MappingException, IOException {
         return read(new Input(raw, type), clz);
     }
     
     public <T> T read(String raw, String type, ClassInfo<T> clz) throws MappingException, IOException {
         return read(new Input(raw, type), clz);
     }
     
     public <T> T read(String raw, String type, SharkType<T,?> clz) throws MappingException, IOException {
         return read(new Input(raw, type), clz);
     }
 
     public <T> T read(SharkNode node, SharkType<T, ?> type) throws MappingException {
         
         if (node == null)
             return null;
         
         if (node.is(SharkNode.NodeType.VALUE) 
                 && ((ValueNode)node).getValue() == null) {
             if (!ReflectUtils.isSimple(type.getType()))
                 return null;
         }
         
         if (type.getType().equals(Object.class)) {
             if (node.is(SharkNode.NodeType.VALUE)) {
                 Object value = ((ValueNode)node).getValue();
                 if (value != null)
                     type = SharkType.get((Class<T>)value.getClass());
             }
             
             if (node.is(SharkNode.NodeType.LIST)) {
                 type = (SharkType<T, ?>) SharkType.forCollection(List.class, Object.class);
             }
             
             if (node.is(SharkNode.NodeType.MAP)) {
                 type = (SharkType<T, ?>) SharkType.forMap(Map.class, Object.class);
             }
         }
         
         final SharkDeserializer converter = deserializers.get(type); 
         
         if (SharkNode.class.isAssignableFrom(type.getType()))
             return (T) node;
 
         if (converter != null) {
             return (T) converter.deserialize(this,node,type);
         }
         
         throw new MappingException(String.format("No deserializer could be found for %s",type));
     }
 
     public <T> T read(SharkNode node, Class<T> clz) throws MappingException {
         return read(node, SharkType.get(clz));
     }
     
     public <T> T read(SharkNode node, ClassInfo<T> clz) throws MappingException {
         return read(node, SharkType.get(clz));
     }
 
     public <T> List<T> readAsList(SharkNode node, SharkType<List, T> type) throws MappingException {
         return read(node, type);
     }
 
     public <T> List<T> readAsList(SharkNode node, Class<T> clz) throws MappingException {
         return read(node, SharkType.forCollection(List.class, clz));
     }
 
     public <T> Map<String, T> readAsMap(SharkNode node, SharkType<Map, T> type) throws MappingException {
         return read(node, type);
     }
 
     public <T> Map<String, T> readAsMap(SharkNode node, Class<T> clz) throws MappingException {
         return read(node, SharkType.forMap(Map.class, clz));
     }
     
     public SharkNode write(Object value) throws MappingException {
         if (value != null) {
             if (value instanceof SharkNode)
                 return (SharkNode) value;
             
             SharkSerializer converter = serializers.get(SharkType.get(value.getClass()));
             if (converter != null) {
                 return converter.serialize(this, value);
             }
             throw new MappingException(String.format("No serializer could be found for %s",value.getClass()));
         }
         return new ValueNode(null);
     }
 
     public void write(Output output, Object value) throws MappingException, IOException {
         ObjectWriter writer = getWriter(output.getContentType());
         if (writer == null) {
            throw new MappingException(String.format("Unknown content type: %s", output.getContentType()));
         }
         SharkNode map = write(value);
         writer.write(output, map);
     }
 
     private String normalizeContentType(String contentType) {
         if (contentType == null)
             return getDefaultType();
         return contentType.toLowerCase().trim();
     }
 
     public String writeToString(Object value, String contentType) throws MappingException, IOException {
         return new String(writeToByteArray(value, contentType),"UTF-8");
     }
 
     public byte[] writeToByteArray(Object value, String contentType) throws MappingException, IOException {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         Output output = new Output(stream, contentType);
         write(output, value);
         return stream.toByteArray();
     }
 
     public String getMimeType(String type) {
         return getMimeType(type, false);
     }
     public String getMimeType(String type,boolean getDefault) {
         if (type == null || type.isEmpty())
             return getDefaultType();
         String out = languages.get(type) != null
                         ? languages.get(type).getContentTypes()[0] : null;
         if (out == null) {
             return type;
         }
         return out;
     }
 
     public <T> T convert(Object value, Class<T> type) throws MappingException {
         if (SharkNode.class.isAssignableFrom(type))
             return (T) write(value);
         if (SharkNode.class.isAssignableFrom(value.getClass()))
             return read((SharkNode)value, type);
         
         //None of the arguments are nodes - convert to node and convert back into value
         SharkNode node = write(value);
         return read(node,type);
     }
     
     
 }
